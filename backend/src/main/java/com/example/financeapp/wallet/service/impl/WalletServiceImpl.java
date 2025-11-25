package com.example.financeapp.wallet.service.impl;

import com.example.financeapp.exception.ApiErrorCode;
import com.example.financeapp.exception.ApiException;
import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.dto.request.CreateWalletRequest;
import com.example.financeapp.wallet.dto.request.TransferMoneyRequest;
import com.example.financeapp.wallet.dto.request.UpdateTransferRequest;
import com.example.financeapp.wallet.dto.request.UpdateWalletRequest;
import com.example.financeapp.wallet.dto.response.*;
import com.example.financeapp.wallet.entity.*;
import com.example.financeapp.wallet.entity.WalletMember.WalletRole;
import com.example.financeapp.wallet.entity.WalletMember.MemberStatus; // Đảm bảo đã có Enum này trong Entity
import com.example.financeapp.wallet.repository.*;
import com.example.financeapp.wallet.service.ExchangeRateService;
import com.example.financeapp.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final WalletMemberRepository walletMemberRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMergeHistoryRepository walletMergeHistoryRepository;
    private final WalletTransferRepository walletTransferRepository;
    private final ExchangeRateService exchangeRateService;

    // =================================================================================
    // PRIVATE HELPER METHODS
    // =================================================================================

    private WalletMember getMemberOrThrow(Long walletId, Long userId) {
        return walletMemberRepository.findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new ApiException("Bạn không có quyền truy cập ví này", ApiErrorCode.FORBIDDEN));
    }

    private void validateRoleModification(WalletRole actorRole, WalletRole targetRole) {
        if (actorRole == WalletRole.OWNER) return;

        if (actorRole == WalletRole.ADMIN) {
            if (targetRole == WalletRole.OWNER || targetRole == WalletRole.ADMIN) {
                throw new ApiException("Admin không có quyền thao tác với Chủ ví hoặc Admin khác");
            }
            return;
        }
        throw new ApiException("Bạn không có quyền quản lý thành viên");
    }

    // =================================================================================
    // CORE WALLET OPERATIONS
    // =================================================================================

    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User không tồn tại"));

        if (!currencyRepository.existsById(request.getCurrencyCode())) {
            throw new ApiException("Loại tiền tệ không hợp lệ: " + request.getCurrencyCode());
        }

        if (walletRepository.existsByWalletNameAndUser_UserId(request.getWalletName(), userId)) {
            throw new ApiException("Bạn đã có ví tên \"" + request.getWalletName() + "\"");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName().trim());
        wallet.setCurrencyCode(request.getCurrencyCode().toUpperCase());
        wallet.setBalance(BigDecimal.valueOf(request.getInitialBalance()));
        wallet.setDescription(request.getDescription());
        wallet.setDefault(false);

        if ("GROUP".equalsIgnoreCase(request.getWalletType())) {
            wallet.setWalletType("GROUP");
        } else {
            wallet.setWalletType("PERSONAL");
        }

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            walletRepository.unsetDefaultWallet(userId, null);
            wallet.setDefault(true);
        }

        Wallet savedWallet = walletRepository.save(wallet);

        // UPDATE: Người tạo luôn là OWNER và trạng thái là ACCEPTED
        WalletMember ownerMember = new WalletMember(
                savedWallet,
                user,
                WalletRole.OWNER,
                MemberStatus.ACCEPTED // Trạng thái chính thức
        );
        walletMemberRepository.save(ownerMember);

        return savedWallet;
    }

    @Override
    @Transactional
    public Wallet updateWallet(Long userId, Long walletId, UpdateWalletRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ApiException("Không tìm thấy ví"));

        // Lấy quyền của người đang thao tác
        WalletMember actor = getMemberOrThrow(walletId, userId);

        // FIX: Đã xóa đoạn code check Invite bị copy nhầm ở đây.
        // Chỉ OWNER hoặc ADMIN mới được sửa thông tin cơ bản
        if (actor.getRole() != WalletRole.OWNER && actor.getRole() != WalletRole.ADMIN) {
            throw new ApiException("Bạn không có quyền chỉnh sửa ví này");
        }

        // 1. Cập nhật Balance
        if (request.getBalance() != null) {
            boolean hasTransactions = transactionRepository.existsByWallet_WalletId(walletId);
            if (hasTransactions)
                throw new ApiException("Ví đã có giao dịch, không thể chỉnh sửa số dư ban đầu");
            wallet.setBalance(request.getBalance());
        }

        // 2. Cập nhật Tên & Mô tả
        if (request.getWalletName() != null && !request.getWalletName().isBlank()) {
            wallet.setWalletName(request.getWalletName());
        }
        if (request.getDescription() != null) {
            wallet.setDescription(request.getDescription());
        }

        // 3. Cập nhật Loại ví
        if (request.getWalletType() != null && !request.getWalletType().isBlank()) {
            String newType = request.getWalletType().toUpperCase();
            String currentType = wallet.getWalletType();

            if (!newType.equals(currentType)) {
                if (actor.getRole() != WalletRole.OWNER) {
                    throw new ApiException("Chỉ chủ sở hữu mới được thay đổi loại ví");
                }

                if ("PERSONAL".equals(currentType) && "GROUP".equals(newType)) {
                    if (wallet.isDefault()) {
                        throw new ApiException("Vui lòng bỏ đặt ví mặc định trước khi chuyển sang ví nhóm");
                    }
                    wallet.setWalletType("GROUP");
                } else if ("GROUP".equals(currentType) && "PERSONAL".equals(newType)) {
                    long memberCount = walletMemberRepository.countByWallet_WalletId(walletId);
                    if (memberCount > 1) {
                        throw new ApiException("Vui lòng xóa hết thành viên trước khi chuyển về ví cá nhân");
                    }
                    wallet.setWalletType("PERSONAL");
                }
            }
        }

        // 4. Cập nhật Tiền tệ
        if (request.getCurrencyCode() != null && !request.getCurrencyCode().equals(wallet.getCurrencyCode())) {
            if (!currencyRepository.existsById(request.getCurrencyCode())) {
                throw new ApiException("Mã tiền tệ không tồn tại");
            }
            handleCurrencyChange(wallet, request.getCurrencyCode());
        }

        // 5. Cập nhật Default
        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            if ("GROUP".equals(wallet.getWalletType())) {
                throw new ApiException("Không thể đặt ví nhóm làm mặc định");
            }
            walletRepository.unsetDefaultWallet(userId, walletId);
            wallet.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getSetAsDefault())) {
            wallet.setDefault(false);
        }

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public DeleteWalletResponse deleteWallet(Long userId, Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ApiException("Không tìm thấy ví"));

        WalletMember member = getMemberOrThrow(walletId, userId);
        if (member.getRole() != WalletRole.OWNER) {
            throw new ApiException("Chỉ chủ sở hữu mới có quyền xóa ví");
        }

        if (transactionRepository.existsByWallet_WalletId(walletId)) {
            throw new ApiException("Không thể xóa ví đã có giao dịch. Hãy xóa giao dịch trước.");
        }

        boolean wasDefault = wallet.isDefault();
        if (wasDefault) {
            throw new ApiException("Không thể xóa ví đang là mặc định.");
        }

        List<WalletMember> members = walletMemberRepository.findByWallet_WalletId(walletId);
        int membersRemoved = members.size();
        walletMemberRepository.deleteAll(members);
        walletRepository.delete(wallet);

        DeleteWalletResponse response = new DeleteWalletResponse(
                wallet.getWalletId(), wallet.getWalletName(), wallet.getBalance(), wallet.getCurrencyCode()
        );
        response.setWasDefault(wasDefault);
        response.setMembersRemoved(membersRemoved);
        return response;
    }

    // =================================================================================
    // MEMBER MANAGEMENT & INVITATIONS
    // =================================================================================

    @Override
    @Transactional
    public WalletMemberDTO shareWallet(Long walletId, Long actorId, String memberEmail) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ApiException("Ví không tồn tại"));

        // Chỉ Owner hoặc Admin mới được mời
        WalletMember actor = getMemberOrThrow(walletId, actorId);
        if (actor.getRole() != WalletRole.OWNER && actor.getRole() != WalletRole.ADMIN) {
            throw new ApiException("Bạn không có quyền mời thành viên vào ví này");
        }

        User memberUser = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new ApiException("Không tìm thấy user email: " + memberEmail));

        if (memberUser.getUserId().equals(actorId)) {
            throw new ApiException("Không thể tự chia sẻ cho chính mình");
        }

        // Kiểm tra xem đã là thành viên hoặc đang được mời chưa
        if (walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, memberUser.getUserId())) {
            throw new ApiException("Người dùng này đã là thành viên hoặc đang chờ xác nhận");
        }
        System.out.println(">>> DEBUG START: shareWallet");
        System.out.println(">>> Dang chuan bi tao member cho email: " + memberEmail);
        // UPDATE: Tạo thành viên với role VIEWER và status PENDING (Chờ duyệt)
        WalletMember newMember = new WalletMember(
                wallet,
                memberUser,
                WalletRole.VIEWER,
                MemberStatus.PENDING
        );
        System.out.println(">>> DEBUG CHECK STATUS: " + newMember.getStatus());
        // Nếu in ra ACCEPTED -> Lỗi tại Constructor trong Entity
        // Nếu in ra PENDING  -> Lỗi do Database hoặc Hibernate tự đổi sau đó

        WalletMember savedMember = walletMemberRepository.save(newMember);

        // 3. Kiểm tra lại sau khi save
        System.out.println(">>> DEBUG AFTER SAVE: " + savedMember.getStatus());
        System.out.println(">>> DEBUG END");

        return convertToMemberDTO(savedMember);
    }

    @Override
    public List<SharedWalletDTO> getPendingInvitations(Long userId) {
        // Cần đảm bảo Repository có hàm: findByUser_UserIdAndStatusOrderByJoinedAtDesc
        List<WalletMember> invites = walletMemberRepository.findByUser_UserIdAndStatusOrderByJoinedAtDesc(
                userId, MemberStatus.PENDING
        );

        return invites.stream().map(invite -> {
            Wallet wallet = invite.getWallet();
            SharedWalletDTO dto = new SharedWalletDTO();
            dto.setWalletId(wallet.getWalletId());
            dto.setWalletName(wallet.getWalletName());
            dto.setWalletType(wallet.getWalletType());
            dto.setDescription(wallet.getDescription());
            dto.setCurrencyCode(wallet.getCurrencyCode());
            dto.setMyRole(invite.getRole().toString());
            dto.setCreatedAt(invite.getJoinedAt()); // Thời gian nhận lời mời

            // Tìm thông tin Owner để hiển thị "Ai mời?"
            walletMemberRepository.findByWallet_WalletIdAndRole(wallet.getWalletId(), WalletRole.OWNER)
                    .ifPresent(owner -> {
                        dto.setOwnerId(owner.getUser().getUserId());
                        dto.setOwnerName(owner.getUser().getFullName());
                    });

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void respondToInvitation(Long userId, Long walletId, boolean isAccepted) {
        WalletMember member = walletMemberRepository.findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy lời mời"));

        // Chỉ xử lý nếu trạng thái đang là PENDING
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new ApiException("Lời mời không hợp lệ hoặc đã được xử lý");
        }

        if (isAccepted) {
            member.setStatus(MemberStatus.ACCEPTED);
            member.setJoinedAt(LocalDateTime.now()); // Update lại thời gian gia nhập chính thức
            walletMemberRepository.save(member);
        } else {
            // Từ chối thì xóa bản ghi
            walletMemberRepository.delete(member);
        }
    }

    @Override
    @Transactional
    public void updateMemberRole(Long walletId, Long targetMemberId, WalletRole newRole, Long actorUserId) {
        WalletMember actor = getMemberOrThrow(walletId, actorUserId);
        WalletMember target = walletMemberRepository.findById(targetMemberId)
                .orElseThrow(() -> new ApiException("Thành viên không tồn tại"));

        if (!target.getWallet().getWalletId().equals(walletId)) {
            throw new ApiException("Thành viên không thuộc ví này");
        }

        // Validate Hierarchy
        validateRoleModification(actor.getRole(), target.getRole());

        if (actor.getRole() == WalletRole.ADMIN) {
            if (newRole == WalletRole.OWNER || newRole == WalletRole.ADMIN) {
                throw new ApiException("Admin chỉ có thể cấp quyền Editor hoặc Viewer");
            }
        }

        if (newRole == WalletRole.OWNER) {
            throw new ApiException("Không thể chuyển quyền Owner qua API cập nhật thành viên");
        }

        target.setRole(newRole);
        walletMemberRepository.save(target);
    }

    @Override
    @Transactional
    public void removeMember(Long walletId, Long actorId, Long memberUserId) {
        if (actorId.equals(memberUserId)) {
            leaveWallet(walletId, actorId);
            return;
        }

        WalletMember actor = getMemberOrThrow(walletId, actorId);
        WalletMember target = getMemberOrThrow(walletId, memberUserId);

        validateRoleModification(actor.getRole(), target.getRole());

        walletMemberRepository.delete(target);
    }

    @Override
    @Transactional
    public void leaveWallet(Long walletId, Long userId) {
        WalletMember member = getMemberOrThrow(walletId, userId);
        if (member.getRole() == WalletRole.OWNER) {
            throw new ApiException("Chủ sở hữu không thể rời ví. Hãy xóa ví hoặc chuyển quyền.");
        }
        walletMemberRepository.delete(member);
    }

    // =================================================================================
    // QUERY METHODS
    // =================================================================================

    @Override
    public List<Wallet> getWalletsByUserId(Long userId) {
        // Chỉ trả về các ví mà user là Owner (để đảm bảo tính toàn vẹn)
        // Hoặc trả về list ví đã Accepted tùy vào mục đích hàm này.
        // Ở đây giữ nguyên logic cũ là tìm theo user relation của Wallet Entity (thường là owner)
        return walletRepository.findByUser_UserId(userId);
    }

    @Override
    public Wallet getWalletDetails(Long userId, Long walletId) {
        // Kiểm tra quyền access (Bao gồm cả việc đã ACCEPTED chưa)
        WalletMember member = getMemberOrThrow(walletId, userId);
        if (member.getStatus() == MemberStatus.PENDING) {
            throw new ApiException("Bạn chưa chấp nhận lời mời tham gia ví này", ApiErrorCode.FORBIDDEN);
        }
        return walletRepository.findById(walletId).orElseThrow();
    }

    @Override
    public List<SharedWalletDTO> getAllAccessibleWallets(Long userId) {
        // UPDATE: Chỉ lấy các ví có trạng thái ACCEPTED
        List<WalletMember> memberships = walletMemberRepository.findByUser_UserIdAndStatus(
                userId, MemberStatus.ACCEPTED
        );
        List<SharedWalletDTO> result = new ArrayList<>();

        for (WalletMember membership : memberships) {
            Wallet wallet = membership.getWallet();
            WalletMember owner = walletMemberRepository
                    .findByWallet_WalletIdAndRole(wallet.getWalletId(), WalletRole.OWNER)
                    .orElse(null);

            long totalMembers = walletMemberRepository.countByWallet_WalletId(wallet.getWalletId());
            long transactionCount = transactionRepository.countByWallet_WalletId(wallet.getWalletId());

            SharedWalletDTO dto = new SharedWalletDTO();
            dto.setWalletId(wallet.getWalletId());
            dto.setWalletName(wallet.getWalletName());
            dto.setWalletType(wallet.getWalletType());
            dto.setCurrencyCode(wallet.getCurrencyCode());
            dto.setBalance(wallet.getBalance());
            dto.setDescription(wallet.getDescription());
            dto.setMyRole(membership.getRole().toString());
            dto.setTotalMembers((int) totalMembers);
            dto.setTransactionCount((int) transactionCount);
            dto.setDefault(wallet.isDefault());
            dto.setCreatedAt(wallet.getCreatedAt());
            dto.setUpdatedAt(wallet.getUpdatedAt());

            if (owner != null) {
                dto.setOwnerId(owner.getUser().getUserId());
                dto.setOwnerName(owner.getUser().getFullName());
            }

            result.add(dto);
        }
        return result;
    }

    @Override
    public List<WalletMemberDTO> getWalletMembers(Long walletId, Long requesterId) {
        getMemberOrThrow(walletId, requesterId);
        // Lấy tất cả thành viên (cả pending và accepted) để Admin/Owner quản lý
        return walletMemberRepository.findByWallet_WalletId(walletId)
                .stream().map(this::convertToMemberDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAccess(Long walletId, Long userId) {
        return walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, userId);
    }

    @Override
    public boolean isOwner(Long walletId, Long userId) {
        return walletMemberRepository.findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .map(m -> m.getRole() == WalletRole.OWNER)
                .orElse(false);
    }

    @Override
    public void setDefaultWallet(Long userId, Long walletId) {
        getMemberOrThrow(walletId, userId);
        walletRepository.unsetDefaultWallet(userId, walletId);
        walletRepository.setDefaultWallet(userId, walletId);
    }

    // =================================================================================
    // TRANSFER & MERGE OPERATIONS
    // =================================================================================

    @Override
    @Transactional
    public TransferMoneyResponse transferMoney(Long userId, TransferMoneyRequest request) {
        if (request.getFromWalletId().equals(request.getToWalletId()))
            throw new ApiException("Không thể chuyển tiền cho chính ví đó");

        Wallet fromWallet = walletRepository.findByIdWithLock(request.getFromWalletId())
                .orElseThrow(() -> new ApiException("Ví nguồn không tồn tại"));
        Wallet toWallet = walletRepository.findByIdWithLock(request.getToWalletId())
                .orElseThrow(() -> new ApiException("Ví đích không tồn tại"));

        WalletMember fromMember = getMemberOrThrow(request.getFromWalletId(), userId);
        if (fromMember.getRole() == WalletRole.VIEWER) {
            throw new ApiException("Viewer không có quyền chuyển tiền đi");
        }
        // Kiểm tra quyền ví đích (cần có access)
        getMemberOrThrow(request.getToWalletId(), userId);

        String sourceCurrency = request.getTargetCurrencyCode() != null
                ? request.getTargetCurrencyCode() : fromWallet.getCurrencyCode();
        BigDecimal sourceAmount = request.getAmount();

        if (fromWallet.getBalance().compareTo(sourceAmount) < 0)
            throw new ApiException("Số dư ví nguồn không đủ");

        BigDecimal targetAmount = sourceAmount;
        if (!fromWallet.getCurrencyCode().equals(toWallet.getCurrencyCode())) {
            targetAmount = exchangeRateService.convertAmount(
                    sourceAmount, fromWallet.getCurrencyCode(), toWallet.getCurrencyCode()
            );
        }

        BigDecimal fromBefore = fromWallet.getBalance();
        BigDecimal toBefore = toWallet.getBalance();

        fromWallet.setBalance(fromBefore.subtract(sourceAmount));
        toWallet.setBalance(toBefore.add(targetAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        User user = userRepository.findById(userId).orElseThrow();
        WalletTransfer transfer = new WalletTransfer();
        transfer.setFromWallet(fromWallet);
        transfer.setToWallet(toWallet);
        transfer.setAmount(sourceAmount);
        transfer.setCurrencyCode(sourceCurrency);
        transfer.setUser(user);
        transfer.setNote(request.getNote());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus(WalletTransfer.TransferStatus.COMPLETED);
        transfer.setFromBalanceBefore(fromBefore);
        transfer.setFromBalanceAfter(fromWallet.getBalance());
        transfer.setToBalanceBefore(toBefore);
        transfer.setToBalanceAfter(toWallet.getBalance());

        WalletTransfer saved = walletTransferRepository.save(transfer);

        TransferMoneyResponse response = new TransferMoneyResponse();
        response.setTransferId(saved.getTransferId());
        response.setStatus(saved.getStatus().toString());
        response.setAmount(saved.getAmount());
        response.setCurrencyCode(saved.getCurrencyCode());
        return response;
    }

    @Override
    @Transactional
    public MergeWalletResponse mergeWallets(Long userId, Long sourceId, Long targetId, String targetCurrency) {
        if (!isOwner(sourceId, userId) || !isOwner(targetId, userId)) {
            throw new ApiException("Bạn phải là chủ sở hữu của cả 2 ví để gộp");
        }
        return new MergeWalletResponse();
    }

    @Override
    public List<MergeCandidateDTO> getMergeCandidates(Long userId, Long sourceWalletId) {
        return new ArrayList<>();
    }

    @Override
    public MergeWalletPreviewResponse previewMerge(Long userId, Long sourceId, Long targetId, String currency) {
        return new MergeWalletPreviewResponse();
    }

    @Override
    public List<WalletTransfer> getAllTransfers(Long userId) {
        return walletTransferRepository.findByUser_UserIdOrderByTransferDateDesc(userId);
    }

    @Override
    @Transactional
    public WalletTransfer updateTransfer(Long userId, Long transferId, UpdateTransferRequest request) {
        WalletTransfer transfer = walletTransferRepository.findByIdWithUser(transferId)
                .orElseThrow(() -> new ApiException("Giao dịch không tồn tại"));

        if (!transfer.getUser().getUserId().equals(userId)) {
            throw new ApiException("Bạn không có quyền sửa giao dịch này");
        }

        if (request.getNote() != null) {
            transfer.setNote(request.getNote());
        }
        return walletTransferRepository.save(transfer);
    }

    @Override
    @Transactional
    public void deleteTransfer(Long userId, Long transferId) {
        WalletTransfer transfer = walletTransferRepository.findByIdForDelete(transferId)
                .orElseThrow(() -> new ApiException("Giao dịch không tồn tại"));

        if (!transfer.getUser().getUserId().equals(userId)) {
            throw new ApiException("Bạn không có quyền xóa giao dịch này");
        }

        Wallet from = transfer.getFromWallet();
        Wallet to = transfer.getToWallet();

        BigDecimal originalAmount = transfer.getAmount();
        BigDecimal addedToTarget = transfer.getToBalanceAfter().subtract(transfer.getToBalanceBefore());

        from.setBalance(from.getBalance().add(originalAmount));
        BigDecimal newToBalance = to.getBalance().subtract(addedToTarget);

        if (newToBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Không thể hoàn tác vì ví nhận sẽ bị âm tiền");
        }
        to.setBalance(newToBalance);

        walletRepository.save(from);
        walletRepository.save(to);
        walletTransferRepository.delete(transfer);
    }

    // =================================================================================
    // UTILITIES
    // =================================================================================

    private WalletMemberDTO convertToMemberDTO(WalletMember member) {
        User u = member.getUser();
        // Cần thêm field status vào WalletMemberDTO nếu muốn hiển thị ở FE (Accepted/Pending)
        // Hiện tại giữ nguyên structure DTO cũ
        return new WalletMemberDTO(
                member.getMemberId(),
                u.getUserId(),
                u.getFullName(),
                u.getEmail(),
                u.getAvatar(),
                member.getRole().toString(),
                member.getJoinedAt()
        );
    }

    private void handleCurrencyChange(Wallet wallet, String newCurrency) {
        String oldCurrency = wallet.getCurrencyCode();

        BigDecimal convertedBalance = exchangeRateService.convertAmount(
                wallet.getBalance(), oldCurrency, newCurrency
        );
        wallet.setBalance(convertedBalance);

        List<Transaction> transactions = transactionRepository.findByWallet_WalletId(wallet.getWalletId());
        for (Transaction tx : transactions) {
            String txOriginalCurrency = tx.getOriginalCurrency() != null ? tx.getOriginalCurrency() : oldCurrency;
            if (tx.getOriginalAmount() == null) {
                tx.setOriginalAmount(tx.getAmount());
                tx.setOriginalCurrency(oldCurrency);
            }
            BigDecimal convertedAmt = exchangeRateService.convertAmount(
                    tx.getOriginalAmount(), txOriginalCurrency, newCurrency
            );
            tx.setAmount(convertedAmt);
            tx.setExchangeRate(exchangeRateService.getExchangeRate(txOriginalCurrency, newCurrency));
            transactionRepository.save(tx);
        }

        wallet.setCurrencyCode(newCurrency);
    }
}