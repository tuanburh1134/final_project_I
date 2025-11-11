package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.dto.SharedWalletDTO;
import com.example.financeapp.dto.UpdateWalletRequest;
import com.example.financeapp.dto.WalletMemberDTO;
import com.example.financeapp.entity.Currency;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.entity.WalletMember;
import com.example.financeapp.entity.WalletMember.WalletRole;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.repository.TransactionRepository;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletMemberRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private WalletMemberRepository walletMemberRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // ================== CREATE ==================
    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!currencyRepository.existsById(request.getCurrencyCode())) {
            throw new RuntimeException("Loại tiền tệ không hợp lệ: " + request.getCurrencyCode());
        }

        if (walletRepository.existsByWalletNameAndUser_UserId(request.getWalletName(), userId)) {
            throw new RuntimeException("Bạn đã có ví tên \"" + request.getWalletName() + "\"");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName().trim());
        wallet.setCurrencyCode(request.getCurrencyCode().toUpperCase());
        wallet.setBalance(BigDecimal.valueOf(request.getInitialBalance()));
        wallet.setDescription(request.getDescription());
        wallet.setDefault(false);

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            walletRepository.unsetDefaultWallet(userId, null);
            wallet.setDefault(true);
        }

        Wallet savedWallet = walletRepository.save(wallet);

        // tạo thành viên owner
        WalletMember ownerMember = new WalletMember(savedWallet, user, WalletRole.OWNER);
        walletMemberRepository.save(ownerMember);

        return savedWallet;
    }

    // ================== UPDATE ==================
    @Override
    @Transactional
    public Wallet updateWallet(Long walletId, Long userId, UpdateWalletRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!wallet.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa ví này");
        }

        if (request.getBalance() != null) {
            boolean hasTransactions = transactionRepository.existsByWallet_WalletId(walletId);
            if (hasTransactions) {
                throw new RuntimeException("Ví đã có giao dịch, không thể chỉnh sửa số dư nữa");
            }
            wallet.setBalance(request.getBalance());
        }

        if (request.getWalletName() != null && !request.getWalletName().isBlank()) {
            wallet.setWalletName(request.getWalletName());
        }

        if (request.getDescription() != null) {
            wallet.setDescription(request.getDescription());
        }

        if (request.getCurrencyCode() != null) {
            Object currency = currencyRepository.findByCurrencyCode(request.getCurrencyCode())
                    .orElseThrow(() -> new RuntimeException("Mã tiền tệ không tồn tại"));
            wallet.setCurrency(currency);
            wallet.setCurrencyCode(request.getCurrencyCode());
        }

        return walletRepository.save(wallet);
    }

    // ================== DEFAULT WALLET ==================
    @Override
    @Transactional
    public void setDefaultWallet(Long userId, Long walletId) {
        walletRepository.findByWalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        walletRepository.unsetDefaultWallet(userId, walletId);
        walletRepository.setDefaultWallet(userId, walletId);
    }

    // ================== GET WALLETS ==================
    @Override
    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUser_UserIdAndDeletedFalse(userId);
    }

    @Override
    public Wallet getWalletDetails(Long userId, Long walletId) {
        if (!hasAccess(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));
    }

    // ================== SHARED WALLET ==================
    @Override
    public List<SharedWalletDTO> getAllAccessibleWallets(Long userId) {
        List<WalletMember> memberships = walletMemberRepository.findByUser_UserId(userId);
        List<SharedWalletDTO> result = new ArrayList<>();

        for (WalletMember membership : memberships) {
            Wallet wallet = membership.getWallet();
            if (wallet.isDeleted()) continue; // ẩn ví đã xoá

            WalletMember owner = walletMemberRepository
                    .findByWallet_WalletIdAndRole(wallet.getWalletId(), WalletRole.OWNER)
                    .orElse(null);

            long totalMembers = walletMemberRepository.countByWallet_WalletId(wallet.getWalletId());

            SharedWalletDTO dto = new SharedWalletDTO();
            dto.setWalletId(wallet.getWalletId());
            dto.setWalletName(wallet.getWalletName());
            dto.setCurrencyCode(wallet.getCurrencyCode());
            dto.setBalance(wallet.getBalance());
            dto.setDescription(wallet.getDescription());
            dto.setMyRole(membership.getRole().toString());
            dto.setTotalMembers((int) totalMembers);
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
    @Transactional
    public WalletMemberDTO shareWallet(Long walletId, Long ownerId, String memberEmail) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!isOwner(walletId, ownerId)) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có thể chia sẻ ví");
        }

        User memberUser = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + memberEmail));

        if (memberUser.getUserId().equals(ownerId)) {
            throw new RuntimeException("Không thể chia sẻ ví với chính bạn");
        }

        if (walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, memberUser.getUserId())) {
            throw new RuntimeException("Người dùng này đã là thành viên của ví");
        }

        WalletMember newMember = new WalletMember(wallet, memberUser, WalletRole.MEMBER);
        WalletMember savedMember = walletMemberRepository.save(newMember);

        return convertToMemberDTO(savedMember);
    }

    // ================== MEMBER MANAGEMENT ==================
    @Override
    public List<WalletMemberDTO> getWalletMembers(Long walletId, Long requesterId) {
        if (!hasAccess(walletId, requesterId)) {
            throw new RuntimeException("Bạn không có quyền xem thành viên của ví này");
        }

        return walletMemberRepository.findByWallet_WalletId(walletId)
                .stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long walletId, Long ownerId, Long memberUserId) {
        if (!isOwner(walletId, ownerId)) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có thể xóa thành viên");
        }

        if (ownerId.equals(memberUserId)) {
            throw new RuntimeException("Không thể xóa chủ sở hữu khỏi ví");
        }

        WalletMember member = walletMemberRepository
                .findByWallet_WalletIdAndUser_UserId(walletId, memberUserId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong ví này"));

        if (member.getRole() == WalletRole.OWNER) {
            throw new RuntimeException("Không thể xóa chủ sở hữu");
        }

        walletMemberRepository.delete(member);
    }

    // ================== SOFT DELETE & ARCHIVE ==================
    @Override
    @Transactional
    public void softDeleteWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!wallet.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xoá ví này");
        }

        wallet.setDeleted(true);
        wallet.setDeletedAt(LocalDateTime.now());
        wallet.setArchived(true); // đưa vào kho lưu trữ
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void restoreWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!wallet.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền khôi phục ví này");
        }

        if (!wallet.isDeleted()) {
            throw new RuntimeException("Ví này chưa bị xoá");
        }

        wallet.setDeleted(false);
        wallet.setDeletedAt(null);
        wallet.setArchived(false);
        walletRepository.save(wallet);
    }

    @Override
    public List<Wallet> getArchivedWallets(Long userId) {
        return walletRepository.findByUser_UserIdAndDeletedTrue(userId);
    }

    @Override
    @Transactional
    public void permanentlyDeleteWallet(Long walletId, Long userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!wallet.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xoá ví này");
        }

        if (!wallet.isDeleted()) {
            throw new RuntimeException("Vui lòng xoá mềm ví trước khi xoá vĩnh viễn");
        }

        walletMemberRepository.deleteAll(walletMemberRepository.findByWallet_WalletId(walletId));
        walletRepository.delete(wallet);
    }

    @Override
    @Transactional
    public void deleteExpiredWallets() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Wallet> expiredWallets = walletRepository.findWalletsToPermanentlyDelete(threshold);
        for (Wallet w : expiredWallets) {
            walletMemberRepository.deleteAll(walletMemberRepository.findByWallet_WalletId(w.getWalletId()));
            walletRepository.delete(w);
        }
    }

    // ================== ACCESS ==================
    @Override
    public boolean hasAccess(Long walletId, Long userId) {
        return walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, userId);
    }

    @Override
    public boolean isOwner(Long walletId, Long userId) {
        return walletMemberRepository.isOwner(walletId, userId);
    }

    // ================== HELPER ==================
    private WalletMemberDTO convertToMemberDTO(WalletMember member) {
        User user = member.getUser();
        return new WalletMemberDTO(
                member.getMemberId(),
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                member.getRole().toString(),
                member.getJoinedAt()
        );
    }
    @Override
    @Transactional
    public void leaveWallet(Long walletId, Long userId) {
        WalletMember member = walletMemberRepository
                .findByWallet_WalletIdAndUser_UserId(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên của ví này"));

        if (member.getRole() == WalletRole.OWNER) {
            throw new RuntimeException("Chủ sở hữu không thể rời khỏi ví. Vui lòng xóa ví hoặc chuyển quyền sở hữu.");
        }

        walletMemberRepository.delete(member);
    }
}
