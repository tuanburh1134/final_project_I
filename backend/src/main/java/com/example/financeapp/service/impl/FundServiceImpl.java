package com.example.financeapp.service.impl;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.*;
import com.example.financeapp.repository.*;
import com.example.financeapp.service.FundService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FundServiceImpl implements FundService {

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private FundMemberRepository fundMemberRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public FundOverviewResponse getMyFundsOverview(Long userId) {
        Long safeUserId = requireUserId(userId);
        FundOverviewResponse response = new FundOverviewResponse();

        List<Fund> personalFunds = fundRepository.findByOwner_UserIdAndDeletedFalse(safeUserId)
                .stream()
                .filter(fund -> fund.getFundType() == FundType.PERSONAL)
                .collect(Collectors.toList());

        List<Fund> groupFunds = new ArrayList<>();

        // Funds where user is owner
        groupFunds.addAll(fundRepository.findByOwner_UserIdAndDeletedFalse(safeUserId)
                .stream()
                .filter(fund -> fund.getFundType() == FundType.GROUP)
                .collect(Collectors.toList()));

        // Funds where user is just a member
        groupFunds.addAll(
                fundRepository.findByFundMembers_User_UserIdAndDeletedFalse(safeUserId)
                        .stream()
                        .filter(fund -> fund.getFundType() == FundType.GROUP)
                        .collect(Collectors.toList())
        );

        // Deduplicate group funds
        Map<Long, Fund> uniqueGroupFunds = new LinkedHashMap<>();
        for (Fund fund : groupFunds) {
            uniqueGroupFunds.put(fund.getFundId(), fund);
        }

        buildSections(response.getPersonal(), personalFunds,
                "Các quỹ tiết kiệm do riêng bạn sở hữu và quản lý.",
                "Các quỹ có mục tiêu và ngày kết thúc rõ ràng.",
                "Quỹ tích lũy dài hạn, không xác định mục tiêu và ngày kết thúc.");

        buildSections(response.getGroup(), new ArrayList<>(uniqueGroupFunds.values()),
                "Quỹ góp chung với bạn bè, gia đình và lớp/nhóm.",
                "Quỹ góp chung có mục tiêu và thời hạn.",
                "Quỹ nhóm dùng lâu dài, không cố định mục tiêu tiền và thời hạn.");

        return response;
    }

    @Override
    public FundDetailResponse getFundDetail(Long userId, Long fundId) {
        Long safeUserId = requireUserId(userId);
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        ensureHasAccess(fund, safeUserId);

        return mapFundToDetail(fund);
    }

    @Override
    @Transactional
    public Fund createFund(Long userId, CreateFundRequest request) {
        Long safeUserId = requireUserId(userId);
        User owner = userRepository.findById(safeUserId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Wallet wallet = walletRepository.findByIdNotDeleted(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại hoặc đã bị xóa"));

        if (!wallet.getUser().getUserId().equals(safeUserId)) {
            throw new RuntimeException("Bạn không có quyền gắn ví này vào quỹ");
        }

        if (fundRepository.existsByWallet_WalletIdAndDeletedFalse(wallet.getWalletId())) {
            throw new RuntimeException("Ví này đã được gắn với một quỹ khác");
        }

        if (request.getTermType() == FundTermType.FIXED_TERM && request.getTargetAmount() == null) {
            throw new RuntimeException("Quỹ có kỳ hạn cần số tiền mục tiêu");
        }

        Fund fund = new Fund();
        fund.setFundName(request.getFundName());
        fund.setFundType(request.getFundType());
        fund.setTermType(request.getTermType());
        fund.setOwner(owner);
        fund.setWallet(wallet);
        fund.setCurrencyCode(wallet.getCurrencyCode());
        fund.setDescription(request.getDescription());
        fund.setTargetAmount(request.getTargetAmount());
        fund.setStartDate(request.getStartDate());
        fund.setEndDate(request.getEndDate());
        fund.setFrequency(request.getFrequency());
        fund.setAmountPerCycle(request.getAmountPerCycle());
        fund.setReminderType(request.getReminderType());
        fund.setReminderTime(request.getReminderTime());
        fund.setAutoTopupType(request.getAutoTopupType());
        fund.setAutoTopupConfig(request.getAutoTopupConfig());
        fund.setNotes(request.getNotes());

        Fund savedFund = fundRepository.save(fund);

        // Owner as fund member
        FundMember ownerMember = new FundMember(savedFund, owner, FundMemberRole.OWNER, FundMemberStatus.ACTIVE);
        fundMemberRepository.save(ownerMember);

        if (request.getFundType() == FundType.GROUP && !CollectionUtils.isEmpty(request.getMemberEmails())) {
            inviteMembers(savedFund, owner, request.getMemberEmails());
        }

        return savedFund;
    }

    @Override
    @Transactional
    public Fund updateFund(Long userId, Long fundId, UpdateFundRequest request) {
        Long safeUserId = requireUserId(userId);
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        ensureOwner(fund, safeUserId);

        if (request.getFundName() != null && !request.getFundName().isBlank()) {
            fund.setFundName(request.getFundName());
        }
        if (request.getFrequency() != null) {
            fund.setFrequency(request.getFrequency());
        }
        if (request.getAmountPerCycle() != null) {
            fund.setAmountPerCycle(request.getAmountPerCycle());
        }
        if (request.getStartDate() != null) {
            fund.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            fund.setEndDate(request.getEndDate());
        }
        if (request.getNotes() != null) {
            fund.setNotes(request.getNotes());
        }
        if (request.getReminderType() != null) {
            fund.setReminderType(request.getReminderType());
        }
        if (request.getReminderTime() != null) {
            fund.setReminderTime(request.getReminderTime());
        }
        if (request.getAutoTopupType() != null) {
            fund.setAutoTopupType(request.getAutoTopupType());
        }
        if (request.getAutoTopupConfig() != null) {
            fund.setAutoTopupConfig(request.getAutoTopupConfig());
        }

        // Xử lý thêm/xóa thành viên (chỉ cho quỹ nhóm)
        if (fund.getFundType() == FundType.GROUP) {
            // Thêm thành viên mới
            if (request.getMemberEmailsToAdd() != null && !request.getMemberEmailsToAdd().isEmpty()) {
                inviteMembers(fund, fund.getOwner(), request.getMemberEmailsToAdd());
            }

            // Xóa thành viên
            if (request.getMemberIdsToRemove() != null && !request.getMemberIdsToRemove().isEmpty()) {
                for (Long memberId : request.getMemberIdsToRemove()) {
                    FundMember member = fundMemberRepository.findById(memberId)
                            .orElse(null);
                    if (member != null && member.getFund().getFundId().equals(fundId) 
                            && member.getRole() != FundMemberRole.OWNER) {
                        fundMemberRepository.delete(member);
                    }
                }
            }
        }

        return fundRepository.save(fund);
    }

    @Override
    @Transactional
    public void closeFund(Long userId, Long fundId) {
        Long safeUserId = requireUserId(userId);
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        ensureOwner(fund, safeUserId);

        fund.setClosed(true);
        fund.setClosedAt(LocalDateTime.now());
        fundRepository.save(fund);
    }

    @Override
    @Transactional
    public void deleteFund(Long userId, Long fundId) {
        Long safeUserId = requireUserId(userId);
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        ensureOwner(fund, safeUserId);

        fund.setDeleted(true);
        fund.setDeletedAt(LocalDateTime.now());
        fundRepository.save(fund);

        Wallet wallet = fund.getWallet();
        wallet.setDeleted(true);
        wallet.setDeletedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public FundMember addMember(Long userId, Long fundId, FundMemberInviteRequest request) {
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));
        ensureOwner(fund, userId);

        User memberUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có email: " + request.getEmail()));

        if (memberUser.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn đã là chủ quỹ này");
        }

        if (fundMemberRepository.existsByFund_FundIdAndUser_UserId(fundId, memberUser.getUserId())) {
            throw new RuntimeException("Người dùng này đã tham gia quỹ");
        }

        FundMember member = new FundMember(fund, memberUser, FundMemberRole.MEMBER, FundMemberStatus.ACTIVE);
        return fundMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(Long userId, Long fundId, Long memberId) {
        Fund fund = fundRepository.findByFundIdAndDeletedFalse(fundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        ensureOwner(fund, userId);

        FundMember member = fundMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        if (member.getRole() == FundMemberRole.OWNER) {
            throw new RuntimeException("Không thể xóa chủ quỹ");
        }

        fundMemberRepository.delete(member);
    }

    private void buildSections(FundOverviewResponse.OverviewGroup group,
                               List<Fund> funds,
                               String groupDescription,
                               String fixedDescription,
                               String flexibleDescription) {

        FundSectionDTO fixed = new FundSectionDTO("Quỹ có kỳ hạn", fixedDescription);
        FundSectionDTO flexible = new FundSectionDTO("Quỹ không kỳ hạn", flexibleDescription);

        for (Fund fund : funds) {
            FundListItemDTO item = mapFundToListItem(fund);
            if (fund.getTermType() == FundTermType.FIXED_TERM) {
                fixed.getFunds().add(item);
            } else {
                flexible.getFunds().add(item);
            }
        }

        fixed.setTotal(fixed.getFunds().size());
        flexible.setTotal(flexible.getFunds().size());

        group.getFixedTerm().setTitle("Quỹ có kỳ hạn");
        group.getFixedTerm().setDescription(fixedDescription);
        group.getFixedTerm().setFunds(fixed.getFunds());
        group.getFixedTerm().setTotal(fixed.getTotal());

        group.getFlexible().setTitle("Quỹ không kỳ hạn");
        group.getFlexible().setDescription(flexibleDescription);
        group.getFlexible().setFunds(flexible.getFunds());
        group.getFlexible().setTotal(flexible.getTotal());
        group.setDescription(groupDescription);
    }

    private FundListItemDTO mapFundToListItem(Fund fund) {
        FundListItemDTO item = new FundListItemDTO();
        item.setFundId(fund.getFundId());
        item.setFundName(fund.getFundName());
        BigDecimal currentAmount = fund.getWallet() != null ? fund.getWallet().getBalance() : BigDecimal.ZERO;
        item.setCurrentAmount(currentAmount);
        item.setTargetAmount(fund.getTargetAmount());
        item.setProgress(calculateProgress(currentAmount, fund.getTargetAmount()));
        item.setStartDate(fund.getStartDate());
        item.setEndDate(fund.getEndDate());
        item.setMemberCount(fundMemberRepository.countByFund_FundIdAndStatus(fund.getFundId(), FundMemberStatus.ACTIVE));
        item.setClosed(fund.isClosed());
        return item;
    }

    private FundDetailResponse mapFundToDetail(Fund fund) {
        FundDetailResponse detail = new FundDetailResponse();
        detail.setFundId(fund.getFundId());
        detail.setFundName(fund.getFundName());
        detail.setFundType(fund.getFundType());
        detail.setTermType(fund.getTermType());
        BigDecimal currentAmount = fund.getWallet() != null ? fund.getWallet().getBalance() : BigDecimal.ZERO;
        detail.setCurrentAmount(currentAmount);
        detail.setTargetAmount(fund.getTargetAmount());
        detail.setCurrencyCode(fund.getCurrencyCode());
        detail.setWalletId(fund.getWallet() != null ? fund.getWallet().getWalletId() : null);
        detail.setProgress(calculateProgress(currentAmount, fund.getTargetAmount()));
        detail.setDescription(fund.getDescription());
        detail.setStartDate(fund.getStartDate());
        detail.setEndDate(fund.getEndDate());
        detail.setFrequency(fund.getFrequency());
        detail.setAmountPerCycle(fund.getAmountPerCycle());
        detail.setClosed(fund.isClosed());
        detail.setClosedAt(fund.getClosedAt());
        detail.setNotes(fund.getNotes());
        detail.setReminderType(fund.getReminderType());
        detail.setReminderTime(fund.getReminderTime());
        detail.setAutoTopupType(fund.getAutoTopupType());
        detail.setAutoTopupConfig(fund.getAutoTopupConfig());
        detail.setCreatedAt(fund.getCreatedAt());
        detail.setUpdatedAt(fund.getUpdatedAt());

        List<FundMemberSummaryDTO> memberDTOs = fundMemberRepository.findByFund_FundId(fund.getFundId())
                .stream()
                .map(this::mapFundMember)
                .collect(Collectors.toList());
        detail.setMembers(memberDTOs);

        return detail;
    }

    private FundMemberSummaryDTO mapFundMember(FundMember member) {
        FundMemberSummaryDTO dto = new FundMemberSummaryDTO();
        dto.setMemberId(member.getFundMemberId());
        dto.setUserId(member.getUser().getUserId());
        dto.setFullName(member.getUser().getFullName());
        dto.setEmail(member.getUser().getEmail());
        dto.setRole(member.getRole().name());
        dto.setStatus(member.getStatus().name());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }

    private double calculateProgress(BigDecimal currentAmount, BigDecimal targetAmount) {
        if (currentAmount == null || targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal progress = currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(progress.doubleValue(), 100.0);
    }

    private void ensureOwner(Fund fund, Long userId) {
        if (!fund.getOwner().getUserId().equals(userId)) {
            throw new RuntimeException("Chỉ chủ quỹ mới có quyền thực hiện thao tác này");
        }
    }

    private void ensureHasAccess(Fund fund, Long userId) {
        if (fund.getOwner().getUserId().equals(userId)) {
            return;
        }
        boolean isMember = fundMemberRepository.existsByFund_FundIdAndUser_UserId(fund.getFundId(), userId);
        if (!isMember) {
            throw new RuntimeException("Bạn không có quyền truy cập quỹ này");
        }
    }

    private void inviteMembers(Fund fund, User owner, List<String> emails) {
        for (String email : emails) {
            if (email == null || email.isBlank()) continue;

            String normalized = email.trim().toLowerCase();
            if (normalized.equals(owner.getEmail().toLowerCase())) {
                continue;
            }

            User memberUser = userRepository.findByEmail(normalized).orElse(null);
            if (memberUser == null) continue;

            if (fundMemberRepository.existsByFund_FundIdAndUser_UserId(fund.getFundId(), memberUser.getUserId())) {
                continue;
            }

            FundMember member = new FundMember(fund, memberUser, FundMemberRole.MEMBER, FundMemberStatus.ACTIVE);
            fundMemberRepository.save(member);
        }
    }

    private Long requireUserId(Long userId) {
        return Objects.requireNonNull(userId, "User không hợp lệ");
    }
}

