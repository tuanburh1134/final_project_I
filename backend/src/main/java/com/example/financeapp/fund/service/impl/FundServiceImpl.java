package com.example.financeapp.fund.service.impl;

import com.example.financeapp.budget.repository.BudgetRepository;
import com.example.financeapp.fund.dto.*;
import com.example.financeapp.fund.entity.*;
import com.example.financeapp.fund.repository.FundMemberRepository;
import com.example.financeapp.fund.repository.FundRepository;
import com.example.financeapp.fund.service.FundService;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FundServiceImpl implements FundService {

    private static final Map<FundTermType, String> PERSONAL_DESCRIPTIONS = Map.of(
            FundTermType.FIXED_TERM, "Các quỹ có mục tiêu và ngày kết thúc rõ ràng.",
            FundTermType.OPEN_TERM, "Quỹ tích lũy dài hạn, không xác định mục tiêu và ngày kết thúc."
    );

    private static final Map<FundTermType, String> GROUP_DESCRIPTIONS = Map.of(
            FundTermType.FIXED_TERM, "Quỹ góp chung có mục tiêu và thời hạn.",
            FundTermType.OPEN_TERM, "Quỹ nhóm dùng lâu dài, không cố định mục tiêu tiền và thời hạn."
    );

    @Autowired private FundRepository fundRepository;
    @Autowired private FundMemberRepository fundMemberRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletService walletService;
    @Autowired private UserRepository userRepository;
    @Autowired private BudgetRepository budgetRepository;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public FundDetailResponse createFund(Long userId, CreateFundRequest request) {
        User owner = requireUser(userId);
        Wallet targetWallet = requireWallet(request.getWalletId());

        if (!walletService.hasAccess(targetWallet.getWalletId(), userId)) {
            throw new RuntimeException("Bạn không có quyền sử dụng ví này cho quỹ.");
        }

        ensureWalletAvailableForFund(targetWallet.getWalletId());
        validateCreateRequest(owner, request);

        Fund fund = buildFundEntity(owner, targetWallet, request);
        Fund saved = fundRepository.save(fund);

        FundMember ownerMember = new FundMember();
        ownerMember.setFund(saved);
        ownerMember.setUser(owner);
        ownerMember.setMemberEmail(owner.getEmail());
        ownerMember.setMemberName(owner.getFullName());
        ownerMember.setRole(FundMemberRole.OWNER);
        fundMemberRepository.save(ownerMember);

        if (saved.getFundType() == FundType.GROUP) {
            addGroupMembers(saved, request.getMembers(), owner);
        }

        Fund reloaded = fundRepository.findFundWithMembers(saved.getFundId());
        if (reloaded == null) {
            reloaded = saved;
        }
        return buildDetailResponse(reloaded);
    }

    @Override
    @Transactional
    public FundDashboardResponse getFundDashboard(Long userId) {
        FundDashboardResponse response = new FundDashboardResponse();

        List<Fund> accessibleFunds = fundRepository.findAccessibleFunds(userId);
        List<FundCardResponse> personalFixed = new ArrayList<>();
        List<FundCardResponse> personalOpen = new ArrayList<>();
        List<FundCardResponse> groupFixed = new ArrayList<>();
        List<FundCardResponse> groupOpen = new ArrayList<>();

        for (Fund fund : accessibleFunds) {
            BigDecimal currentBalance = fund.getWallet().getBalance();
            int memberCount = (int) fundMemberRepository.countByFund_FundIdAndActiveIsTrue(fund.getFundId());
            FundCardResponse card = FundCardResponse.from(fund, currentBalance, memberCount);
            if (fund.getFundType() == FundType.PERSONAL) {
                if (fund.getTermType() == FundTermType.FIXED_TERM) {
                    personalFixed.add(card);
                } else {
                    personalOpen.add(card);
                }
            } else {
                if (fund.getTermType() == FundTermType.FIXED_TERM) {
                    groupFixed.add(card);
                } else {
                    groupOpen.add(card);
                }
            }
        }

        response.setPersonalFixed(buildSection("Quỹ cá nhân có kỳ hạn",
                PERSONAL_DESCRIPTIONS.get(FundTermType.FIXED_TERM), personalFixed));
        response.setPersonalOpen(buildSection("Quỹ cá nhân không kỳ hạn",
                PERSONAL_DESCRIPTIONS.get(FundTermType.OPEN_TERM), personalOpen));
        response.setGroupFixed(buildSection("Quỹ nhóm có kỳ hạn",
                GROUP_DESCRIPTIONS.get(FundTermType.FIXED_TERM), groupFixed));
        response.setGroupOpen(buildSection("Quỹ nhóm không kỳ hạn",
                GROUP_DESCRIPTIONS.get(FundTermType.OPEN_TERM), groupOpen));

        return response;
    }

    @Override
    @Transactional
    public FundDetailResponse getFundDetail(Long userId, Long fundId) {
        Fund fund = fundRepository.findFundWithMembers(fundId);
        if (fund == null) {
            throw new RuntimeException("Không tìm thấy quỹ");
        }
        ensureAccessible(userId, fund);
        return buildDetailResponse(fund);
    }

    private void addGroupMembers(Fund fund, List<FundMemberRequest> members, User owner) {
        if (CollectionUtils.isEmpty(members)) {
            throw new RuntimeException("Quỹ nhóm phải có ít nhất 1 thành viên ngoài chủ quỹ.");
        }
        Set<String> seenEmails = new HashSet<>();
        seenEmails.add(owner.getEmail().toLowerCase(Locale.ROOT));

        for (FundMemberRequest memberRequest : members) {
            String normalizedEmail = memberRequest.getEmail().trim().toLowerCase(Locale.ROOT);
            if (seenEmails.contains(normalizedEmail)) {
                throw new RuntimeException("Email thành viên bị trùng: " + memberRequest.getEmail());
            }
            seenEmails.add(normalizedEmail);

            User memberUser = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException(
                            "Tài khoản " + memberRequest.getEmail() + " không tồn tại. Vui lòng mời người dùng đăng ký trước khi tham gia quỹ."));

            FundMember member = new FundMember();
            member.setFund(fund);
            member.setUser(memberUser);
            member.setMemberEmail(memberUser.getEmail());
            member.setMemberName(
                    StringUtils.hasText(memberRequest.getFullName()) ? memberRequest.getFullName() : memberUser.getFullName()
            );
            member.setRole(memberRequest.getRole() != null ? memberRequest.getRole() : FundMemberRole.CONTRIBUTOR);
            fundMemberRepository.save(member);
        }
    }

    private void ensureAccessible(Long userId, Fund fund) {
        boolean isOwner = fund.getOwner().getUserId().equals(userId);
        if (isOwner) {
            return;
        }
        boolean isMember = fundMemberRepository.findByFund_FundId(fund.getFundId()).stream()
                .anyMatch(member -> member.isActive()
                        && member.getUser() != null
                        && Objects.equals(member.getUser().getUserId(), userId));
        if (!isMember) {
            throw new RuntimeException("Bạn không có quyền xem quỹ này");
        }
    }

    private Fund buildFundEntity(User owner, Wallet wallet, CreateFundRequest request) {
        Fund fund = new Fund();
        fund.setOwner(owner);
        fund.setWallet(wallet);
        fund.setFundName(request.getFundName().trim());
        fund.setFundType(request.getFundType());
        fund.setTermType(request.getTermType());
        fund.setCurrencyCode(wallet.getCurrencyCode());
        fund.setStartDate(request.getStartDate());
        fund.setEndDate(request.getEndDate());
        fund.setTargetAmount(request.getTargetAmount());
        fund.setContributionFrequency(
                request.getContributionFrequency() != null ? request.getContributionFrequency() : ContributionFrequency.NONE);
        fund.setContributionAmount(request.getContributionAmount());
        fund.setNote(StringUtils.hasText(request.getNote()) ? request.getNote().trim() : null);

        boolean reminderEnabled = Boolean.TRUE.equals(request.getReminderEnabled());
        fund.setReminderEnabled(reminderEnabled);
        if (reminderEnabled) {
            fund.setReminderType(request.getReminderType());
            fund.setReminderTime(request.getReminderTime());
            fund.setReminderDayOfWeek(request.getReminderDayOfWeek());
            fund.setReminderDayOfMonth(request.getReminderDayOfMonth());
            fund.setReminderMonthOfYear(request.getReminderMonthOfYear());
        }

        boolean autoTopUpEnabled = Boolean.TRUE.equals(request.getAutoTopUpEnabled());
        fund.setAutoTopUpEnabled(autoTopUpEnabled);
        if (autoTopUpEnabled) {
            fund.setAutoTopUpMode(request.getAutoTopUpMode());
            fund.setAutoTopUpScheduleType(request.getAutoTopUpScheduleType());
            fund.setAutoTopUpTime(request.getAutoTopUpTime());
            fund.setAutoTopUpDayOfWeek(request.getAutoTopUpDayOfWeek());
            fund.setAutoTopUpDayOfMonth(request.getAutoTopUpDayOfMonth());
            fund.setAutoTopUpMonthOfYear(request.getAutoTopUpMonthOfYear());
            fund.setAutoTopUpAmount(request.getAutoTopUpAmount());
            if (request.getAutoTopUpSourceWalletId() != null) {
                Wallet sourceWallet = requireWallet(request.getAutoTopUpSourceWalletId());
                fund.setAutoTopUpSourceWallet(sourceWallet);
            }
        }
        return fund;
    }

    private FundDetailResponse buildDetailResponse(Fund fund) {
        BigDecimal currentBalance = fund.getWallet().getBalance();

        FundReminderConfig reminderConfig = new FundReminderConfig();
        reminderConfig.setEnabled(fund.isReminderEnabled());
        reminderConfig.setReminderType(fund.getReminderType());
        reminderConfig.setReminderTime(fund.getReminderTime());
        reminderConfig.setReminderDayOfWeek(fund.getReminderDayOfWeek());
        reminderConfig.setReminderDayOfMonth(fund.getReminderDayOfMonth());
        reminderConfig.setReminderMonthOfYear(fund.getReminderMonthOfYear());

        FundAutoTopUpConfig autoTopUpConfig = new FundAutoTopUpConfig();
        autoTopUpConfig.setEnabled(fund.isAutoTopUpEnabled());
        autoTopUpConfig.setMode(fund.getAutoTopUpMode());
        autoTopUpConfig.setScheduleType(fund.getAutoTopUpScheduleType());
        autoTopUpConfig.setTime(fund.getAutoTopUpTime());
        autoTopUpConfig.setDayOfWeek(fund.getAutoTopUpDayOfWeek());
        autoTopUpConfig.setDayOfMonth(fund.getAutoTopUpDayOfMonth());
        autoTopUpConfig.setMonthOfYear(fund.getAutoTopUpMonthOfYear());
        autoTopUpConfig.setAmount(fund.getAutoTopUpAmount());
        if (fund.getAutoTopUpSourceWallet() != null) {
            autoTopUpConfig.setSourceWalletId(fund.getAutoTopUpSourceWallet().getWalletId());
            autoTopUpConfig.setSourceWalletName(fund.getAutoTopUpSourceWallet().getWalletName());
        }

        List<FundMemberResponse> memberResponses = fundMemberRepository.findByFund_FundId(fund.getFundId()).stream()
                .filter(FundMember::isActive)
                .map(FundMemberResponse::from)
                 .collect(Collectors.toList());

        return FundDetailResponse.from(
                fund,
                currentBalance,
                fund.getWallet().getWalletName(),
                reminderConfig,
                autoTopUpConfig,
                memberResponses
        );
    }

    private FundSectionResponse buildSection(String title, String description, List<FundCardResponse> cards) {
        cards.sort(Comparator.comparing(FundCardResponse::getStartDate, Comparator.nullsLast(LocalDate::compareTo)));
        FundSectionResponse response = new FundSectionResponse();
        response.setTitle(title);
        response.setDescription(description);
        response.setFunds(cards);
        response.setTotal(cards.size());
        return response;
    }

    private void validateCreateRequest(User owner, CreateFundRequest request) {
        FundType fundType = request.getFundType();
        FundTermType termType = request.getTermType();

        if (termType == FundTermType.FIXED_TERM) {
            if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Quỹ có kỳ hạn phải nhập số tiền mục tiêu.");
            }
            if (request.getEndDate() == null) {
                throw new RuntimeException("Quỹ có kỳ hạn phải có ngày kết thúc.");
            }
            validateFrequencyRange(request.getContributionFrequency(), request.getStartDate(), request.getEndDate());
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày bắt đầu phải lớn hơn hoặc bằng ngày hiện tại.");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu.");
        }

        if (fundType == FundType.GROUP) {
            if (CollectionUtils.isEmpty(request.getMembers())) {
                throw new RuntimeException("Vui lòng thêm ít nhất một thành viên cho quỹ nhóm.");
            }
            long distinct = request.getMembers().stream()
                    .map(member -> member.getEmail().trim().toLowerCase(Locale.ROOT))
                    .filter(email -> !email.equals(owner.getEmail().toLowerCase(Locale.ROOT)))
                    .count();
            if (distinct == 0) {
                throw new RuntimeException("Quỹ nhóm phải có thành viên khác chủ quỹ.");
            }
        }

        if (Boolean.TRUE.equals(request.getReminderEnabled())) {
            validateReminderConfig(request.getReminderType(), request.getReminderTime(),
                    request.getReminderDayOfWeek(), request.getReminderDayOfMonth(), request.getReminderMonthOfYear());
        }

        if (Boolean.TRUE.equals(request.getAutoTopUpEnabled())) {
            validateAutoTopUpConfig(owner.getUserId(), request);
        }
    }

    private void validateReminderConfig(FundReminderType type,
                                        LocalTime time,
                                        DayOfWeek dayOfWeek,
                                        Integer dayOfMonth,
                                        Integer monthOfYear) {
        if (type == null) {
            throw new RuntimeException("Vui lòng chọn kiểu nhắc nhở.");
        }
        if (time == null) {
            throw new RuntimeException("Vui lòng chọn giờ nhắc.");
        }
        switch (type) {
            case WEEKLY -> {
                if (dayOfWeek == null) {
                    throw new RuntimeException("Nhắc theo tuần cần chọn thứ.");
                }
            }
            case MONTHLY -> {
                if (dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31) {
                    throw new RuntimeException("Nhắc theo tháng cần chọn ngày hợp lệ.");
                }
            }
            case YEARLY -> {
                if (dayOfMonth == null || monthOfYear == null) {
                    throw new RuntimeException("Nhắc theo năm cần chọn ngày và tháng.");
                }
            }
            default -> {
            }
        }
    }

    private void validateAutoTopUpConfig(Long ownerId, CreateFundRequest request) {
        if (request.getAutoTopUpAmount() == null || request.getAutoTopUpAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền tự động nạp phải lớn hơn 0.");
        }
        if (request.getAutoTopUpMode() == null) {
            throw new RuntimeException("Hãy chọn chế độ tự động nạp.");
        }
        if (request.getAutoTopUpMode() == AutoTopUpMode.REMINDER_LINKED && !Boolean.TRUE.equals(request.getReminderEnabled())) {
            throw new RuntimeException("Bạn phải bật nhắc nhở nếu muốn nạp theo lịch nhắc nhở.");
        }
        if (request.getAutoTopUpMode() == AutoTopUpMode.CUSTOM_SCHEDULE) {
            if (request.getAutoTopUpScheduleType() == null) {
                throw new RuntimeException("Hãy chọn kiểu lịch tự nạp.");
            }
            if (request.getAutoTopUpTime() == null) {
                throw new RuntimeException("Hãy chọn thời gian nạp.");
            }
        }
        if (request.getAutoTopUpSourceWalletId() == null) {
            throw new RuntimeException("Hãy chọn ví nguồn để tự động nạp tiền.");
        }
        if (request.getAutoTopUpSourceWalletId().equals(request.getWalletId())) {
            throw new RuntimeException("Ví nguồn không được trùng với ví quỹ.");
        }
        Wallet sourceWallet = requireWallet(request.getAutoTopUpSourceWalletId());
        if (!walletService.hasAccess(sourceWallet.getWalletId(), ownerId)) {
            throw new RuntimeException("Bạn không có quyền sử dụng ví nguồn đã chọn.");
        }
        ensureWalletAvailableForFund(sourceWallet.getWalletId());
    }

    private void validateFrequencyRange(ContributionFrequency frequency, LocalDate start, LocalDate end) {
        if (frequency == null || frequency == ContributionFrequency.NONE) {
            throw new RuntimeException("Quỹ có kỳ hạn cần cấu hình tần suất gửi quỹ.");
        }
        long daysBetween = end.toEpochDay() - start.toEpochDay();
        switch (frequency) {
            case DAILY -> {
                if (daysBetween < 1) {
                    throw new RuntimeException("Khoảng thời gian không đủ cho ít nhất một kỳ gửi (hàng ngày).");
                }
            }
            case WEEKLY -> {
                if (daysBetween < 7) {
                    throw new RuntimeException("Khoảng thời gian không đủ cho ít nhất một kỳ gửi (hàng tuần).");
                }
            }
            case MONTHLY -> {
                if (daysBetween < 30) {
                    throw new RuntimeException("Khoảng thời gian không đủ cho ít nhất một kỳ gửi (hàng tháng).");
                }
            }
            case YEARLY -> {
                if (daysBetween < 365) {
                    throw new RuntimeException("Khoảng thời gian không đủ cho ít nhất một kỳ gửi (hàng năm).");
                }
            }
            default -> {
            }
        }
    }

    private void ensureWalletAvailableForFund(Long walletId) {
        if (budgetRepository.existsByWallet_WalletId(walletId)) {
            throw new RuntimeException("Ví này đang được sử dụng cho ngân sách khác.");
        }
        if (fundRepository.existsByWallet_WalletId(walletId)) {
            throw new RuntimeException("Ví này đang được sử dụng cho một quỹ khác.");
        }
    }

    private User requireUser(Long userId) {
        return userRepository.findById(Objects.requireNonNull(userId, "User không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Wallet requireWallet(Long walletId) {
        return walletRepository.findById(Objects.requireNonNull(walletId, "Ví không hợp lệ"))
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));
    }
}

