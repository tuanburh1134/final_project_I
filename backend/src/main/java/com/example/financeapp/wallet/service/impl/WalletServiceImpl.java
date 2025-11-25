package com.example.financeapp.wallet.service.impl;

import com.example.financeapp.transaction.entity.Transaction;
import com.example.financeapp.transaction.repository.TransactionRepository;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.wallet.dto.request.CreateWalletRequest;
import com.example.financeapp.wallet.dto.request.TransferMoneyRequest;
import com.example.financeapp.wallet.dto.request.UpdateTransferRequest;
import com.example.financeapp.wallet.dto.request.UpdateWalletRequest;
import com.example.financeapp.wallet.dto.response.DeleteWalletResponse;
import com.example.financeapp.wallet.dto.response.MergeCandidateDTO;
import com.example.financeapp.wallet.dto.response.MergeWalletPreviewResponse;
import com.example.financeapp.wallet.dto.response.MergeWalletResponse;
import com.example.financeapp.wallet.dto.response.SharedWalletDTO;
import com.example.financeapp.wallet.dto.response.TransferMoneyResponse;
import com.example.financeapp.wallet.dto.response.WalletMemberDTO;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.entity.WalletMember;
import com.example.financeapp.wallet.entity.WalletMember.WalletRole;
import com.example.financeapp.wallet.entity.WalletMergeHistory;
import com.example.financeapp.wallet.entity.WalletTransfer;
import com.example.financeapp.wallet.repository.CurrencyRepository;
import com.example.financeapp.wallet.repository.WalletMemberRepository;
import com.example.financeapp.wallet.repository.WalletMergeHistoryRepository;
import com.example.financeapp.wallet.repository.WalletRepository;
import com.example.financeapp.wallet.repository.WalletTransferRepository;
import com.example.financeapp.wallet.service.ExchangeRateService;
import com.example.financeapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired private WalletRepository walletRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private WalletMemberRepository walletMemberRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private WalletMergeHistoryRepository walletMergeHistoryRepository;
    @Autowired private WalletTransferRepository walletTransferRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    // ---------------- CREATE WALLET ----------------
    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!currencyRepository.existsById(request.getCurrencyCode())) {
            throw new RuntimeException("Loại tiền tệ không hợp lệ: " + request.getCurrencyCode());
        }

        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        if (walletRepository.existsByWalletNameAndUser_Id(request.getWalletName(), userId)) {
            throw new RuntimeException("Bạn đã có ví tên \"" + request.getWalletName() + "\"");
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
            wallet.setWalletType("PERSONAL"); // Mặc định là cá nhân
        }

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            walletRepository.unsetDefaultWallet(userId, null);
            wallet.setDefault(true);
        }

        Wallet savedWallet = walletRepository.save(wallet);

        WalletMember ownerMember = new WalletMember(savedWallet, user, WalletRole.OWNER);
        walletMemberRepository.save(ownerMember);

        return savedWallet;
    }

    @Override
    @Transactional
    public void setDefaultWallet(Long userId, Long walletId) {
        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        walletRepository.findByWalletIdAndUser_Id(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        walletRepository.unsetDefaultWallet(userId, walletId);
        walletRepository.setDefaultWallet(userId, walletId);
    }

    @Override
    public List<Wallet> getWalletsByUserId(Long userId) {
        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        return walletRepository.findByUser_Id(userId);
    }

    @Override
    public Wallet getWalletDetails(Long userId, Long walletId) {
        if (!hasAccess(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));
    }

    // ============= SHARED WALLET =============
    @Override
    public List<SharedWalletDTO> getAllAccessibleWallets(Long userId) {

        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        List<WalletMember> memberships = walletMemberRepository.findByUser_Id(userId);
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
                dto.setOwnerId(owner.getUser().getId());
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

        if (memberUser.getId().equals(ownerId)) {
            throw new RuntimeException("Không thể chia sẻ ví với chính bạn");
        }

        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        if (walletMemberRepository.existsByWallet_WalletIdAndUser_Id(walletId, memberUser.getId())) {
            throw new RuntimeException("Người dùng này đã là thành viên của ví");
        }

        WalletMember newMember = new WalletMember(wallet, memberUser, WalletRole.MEMBER);
        WalletMember saved = walletMemberRepository.save(newMember);

        return convertToMemberDTO(saved);
    }

    @Override
    public List<WalletMemberDTO> getWalletMembers(Long walletId, Long requesterId) {

        if (!hasAccess(walletId, requesterId)) {
            throw new RuntimeException("Bạn không có quyền xem thành viên ví này");
        }

        return walletMemberRepository.findByWallet_WalletId(walletId)
                .stream().map(this::convertToMemberDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long walletId, Long ownerId, Long memberUserId) {

        if (!isOwner(walletId, ownerId)) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có thể xóa thành viên");
        }

        if (ownerId.equals(memberUserId)) {
            throw new RuntimeException("Không thể xóa chủ sở hữu");
        }

        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        WalletMember member = walletMemberRepository
                .findByWallet_WalletIdAndUser_Id(walletId, memberUserId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại trong ví"));

        walletMemberRepository.delete(member);
    }

    // ---------------- UPDATE WALLET (NEW STYLE) ----------------
    @Override
    @Transactional
    public Wallet updateWallet(Long userId, Long walletId, UpdateWalletRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa ví này");
        }

        if (request.getBalance() != null) {
            boolean hasTransactions = transactionRepository.existsByWallet_WalletId(walletId);
            if (hasTransactions)
                throw new RuntimeException("Ví đã có giao dịch, không thể chỉnh sửa số dư nữa");
            wallet.setBalance(request.getBalance());
        }

        if (request.getWalletName() != null && !request.getWalletName().isBlank()) {
            wallet.setWalletName(request.getWalletName());
        }

        if (request.getDescription() != null) {
            wallet.setDescription(request.getDescription());
        }

        if (request.getCurrencyCode() != null) {
            if (!currencyRepository.existsById(request.getCurrencyCode())) {
                throw new RuntimeException("Mã tiền tệ không tồn tại");
            }

            String oldCurrency = wallet.getCurrencyCode();
            String newCurrency = request.getCurrencyCode();

            if (!oldCurrency.equals(newCurrency)) {
                BigDecimal convertedBalance = exchangeRateService.convertAmount(
                        wallet.getBalance(),
                        oldCurrency,
                        newCurrency
                );
                wallet.setBalance(convertedBalance);

                List<Transaction> transactions = transactionRepository.findByWallet_WalletId(walletId);
                for (Transaction tx : transactions) {
                    String txOriginalCurrency = tx.getOriginalCurrency() != null
                            ? tx.getOriginalCurrency()
                            : oldCurrency;

                    if (tx.getOriginalAmount() == null) {
                        tx.setOriginalAmount(tx.getAmount());
                        tx.setOriginalCurrency(oldCurrency);
                        txOriginalCurrency = oldCurrency;
                    }

                    BigDecimal convertedAmount = exchangeRateService.convertAmount(
                            tx.getOriginalAmount(),
                            txOriginalCurrency,
                            newCurrency
                    );
                    tx.setAmount(convertedAmount);

                    BigDecimal rate = exchangeRateService.getExchangeRate(
                            txOriginalCurrency,
                            newCurrency
                    );
                    tx.setExchangeRate(rate);

                    transactionRepository.save(tx);
                }

                List<WalletTransfer> fromTransfers = walletTransferRepository.findByFromWallet_WalletIdOrderByTransferDateDesc(walletId);
                for (WalletTransfer transfer : fromTransfers) {
                    String transferOriginalCurrency = transfer.getOriginalCurrency() != null
                            ? transfer.getOriginalCurrency()
                            : oldCurrency;

                    if (transfer.getOriginalAmount() == null) {
                        transfer.setOriginalAmount(transfer.getAmount());
                        transfer.setOriginalCurrency(oldCurrency);
                        transferOriginalCurrency = oldCurrency;
                    }

                    BigDecimal convertedAmount = exchangeRateService.convertAmount(
                            transfer.getOriginalAmount(),
                            transferOriginalCurrency,
                            newCurrency
                    );
                    transfer.setAmount(convertedAmount);
                    transfer.setCurrencyCode(newCurrency);

                    BigDecimal rate = exchangeRateService.getExchangeRate(
                            transferOriginalCurrency,
                            newCurrency
                    );
                    transfer.setExchangeRate(rate);

                    if (transfer.getFromBalanceBefore() != null) {
                        BigDecimal convertedBefore = exchangeRateService.convertAmount(
                                transfer.getFromBalanceBefore(),
                                oldCurrency,
                                newCurrency
                        );
                        transfer.setFromBalanceBefore(convertedBefore);
                    }
                    if (transfer.getFromBalanceAfter() != null) {
                        BigDecimal convertedAfter = exchangeRateService.convertAmount(
                                transfer.getFromBalanceAfter(),
                                oldCurrency,
                                newCurrency
                        );
                        transfer.setFromBalanceAfter(convertedAfter);
                    }

                    walletTransferRepository.save(transfer);
                }

                List<WalletTransfer> toTransfers = walletTransferRepository.findByToWallet_WalletIdOrderByTransferDateDesc(walletId);
                for (WalletTransfer transfer : toTransfers) {
                    if (transfer.getToBalanceBefore() != null) {
                        BigDecimal convertedBefore = exchangeRateService.convertAmount(
                                transfer.getToBalanceBefore(),
                                oldCurrency,
                                newCurrency
                        );
                        transfer.setToBalanceBefore(convertedBefore);
                    }
                    if (transfer.getToBalanceAfter() != null) {
                        BigDecimal convertedAfter = exchangeRateService.convertAmount(
                                transfer.getToBalanceAfter(),
                                oldCurrency,
                                newCurrency
                        );
                        transfer.setToBalanceAfter(convertedAfter);
                    }

                    walletTransferRepository.save(transfer);
                }
            }

            wallet.setCurrencyCode(newCurrency);
        }

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            walletRepository.unsetDefaultWallet(userId, walletId);
            wallet.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getSetAsDefault())) {
            wallet.setDefault(false);
        }

        if (request.getWalletType() != null && !request.getWalletType().isBlank()) {
            String newWalletType = request.getWalletType().toUpperCase();

            if (!"PERSONAL".equals(newWalletType) && !"GROUP".equals(newWalletType)) {
                throw new RuntimeException("Loại ví không hợp lệ. Chỉ chấp nhận PERSONAL hoặc GROUP");
            }

            String currentWalletType = wallet.getWalletType();

            if ("PERSONAL".equals(currentWalletType) && "GROUP".equals(newWalletType)) {
                if (wallet.isDefault()) {
                    throw new RuntimeException("Không thể chuyển đổi ví mặc định sang ví nhóm. Vui lòng bỏ ví mặc định trước.");
                }

                wallet.setWalletType("GROUP");

                // ✅ ĐÃ ĐỔI User_UserId → User_Id
                boolean ownerExists = walletMemberRepository.existsByWallet_WalletIdAndUser_Id(
                        walletId, userId
                );

                if (!ownerExists) {
                    User owner = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
                    WalletMember ownerMember = new WalletMember(wallet, owner, WalletRole.OWNER);
                    walletMemberRepository.save(ownerMember);
                }
            } else if ("GROUP".equals(currentWalletType) && "PERSONAL".equals(newWalletType)) {
                throw new RuntimeException("Không thể chuyển ví nhóm về ví cá nhân. Vui lòng xóa các thành viên trước.");
            }
        }

        return walletRepository.save(wallet);
    }

    // ---------------- LEAVE WALLET ----------------
    @Override
    @Transactional
    public void leaveWallet(Long walletId, Long userId) {

        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        WalletMember member = walletMemberRepository
                .findByWallet_WalletIdAndUser_Id(walletId, userId)
                .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên ví này"));

        if (member.getRole() == WalletRole.OWNER) {
            throw new RuntimeException("Chủ sở hữu không thể tự rời ví");
        }

        walletMemberRepository.delete(member);
    }

    // ---------------- ACCESS CHECK ----------------
    @Override
    public boolean hasAccess(Long walletId, Long userId) {
        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        return walletMemberRepository.existsByWallet_WalletIdAndUser_Id(walletId, userId);
    }

    @Override
    public boolean isOwner(Long walletId, Long userId) {
        return walletMemberRepository.isOwner(walletId, userId);
    }

    // ---------------- MERGE WALLET ----------------
    @Override
    public List<MergeCandidateDTO> getMergeCandidates(Long userId, Long sourceWalletId) {
        if (!isOwner(sourceWalletId, userId)) {
            throw new RuntimeException("Bạn không có quyền gộp ví này");
        }

        if (!walletRepository.existsById(sourceWalletId)) {
            throw new RuntimeException("Ví nguồn không tồn tại");
        }

        List<SharedWalletDTO> allWallets = getAllAccessibleWallets(userId);

        List<MergeCandidateDTO> candidates = new ArrayList<>();

        for (SharedWalletDTO wallet : allWallets) {
            if (wallet.getWalletId().equals(sourceWalletId)) {
                continue;
            }

            if (!isOwner(wallet.getWalletId(), userId)) {
                continue;
            }

            MergeCandidateDTO candidate = new MergeCandidateDTO();
            candidate.setWalletId(wallet.getWalletId());
            candidate.setWalletName(wallet.getWalletName());
            candidate.setCurrencyCode(wallet.getCurrencyCode());
            candidate.setBalance(wallet.getBalance());
            candidate.setDefault(wallet.isDefault());

            long transactionCount = transactionRepository.countByWallet_WalletId(wallet.getWalletId());
            candidate.setTransactionCount((int) transactionCount);

            candidate.setCanMerge(true);
            candidate.setReason(null);

            candidates.add(candidate);
        }

        return candidates;
    }

    @Override
    public MergeWalletPreviewResponse previewMerge(Long userId, Long sourceWalletId, Long targetWalletId, String targetCurrency) {
        if (!isOwner(sourceWalletId, userId)) {
            throw new RuntimeException("Bạn không có quyền gộp ví nguồn này");
        }
        if (!isOwner(targetWalletId, userId)) {
            throw new RuntimeException("Bạn không có quyền gộp vào ví đích này");
        }

        if (sourceWalletId.equals(targetWalletId)) {
            throw new RuntimeException("Không thể gộp ví với chính nó");
        }

        Wallet sourceWallet = walletRepository.findById(sourceWalletId)
                .orElseThrow(() -> new RuntimeException("Ví nguồn không tồn tại"));

        Wallet targetWallet = walletRepository.findById(targetWalletId)
                .orElseThrow(() -> new RuntimeException("Ví đích không tồn tại"));

        if (!currencyRepository.existsById(targetCurrency)) {
            throw new RuntimeException("Loại tiền tệ không hợp lệ: " + targetCurrency);
        }

        long sourceTransactionCount = transactionRepository.countByWallet_WalletId(sourceWalletId);
        long targetTransactionCount = transactionRepository.countByWallet_WalletId(targetWalletId);

        BigDecimal sourceBalanceConverted = sourceWallet.getBalance();
        if (!sourceWallet.getCurrencyCode().equals(targetCurrency)) {
            sourceBalanceConverted = exchangeRateService.convertAmount(
                    sourceWallet.getBalance(),
                    sourceWallet.getCurrencyCode(),
                    targetCurrency
            );
        }

        BigDecimal targetBalanceConverted = targetWallet.getBalance();
        if (!targetWallet.getCurrencyCode().equals(targetCurrency)) {
            targetBalanceConverted = exchangeRateService.convertAmount(
                    targetWallet.getBalance(),
                    targetWallet.getCurrencyCode(),
                    targetCurrency
            );
        }

        BigDecimal finalBalance = sourceBalanceConverted.add(targetBalanceConverted);

        MergeWalletPreviewResponse preview = new MergeWalletPreviewResponse();
        preview.setSourceWalletId(sourceWalletId);
        preview.setSourceWalletName(sourceWallet.getWalletName());
        preview.setSourceCurrency(sourceWallet.getCurrencyCode());
        preview.setSourceBalance(sourceWallet.getBalance());
        preview.setSourceTransactionCount((int) sourceTransactionCount);
        preview.setSourceIsDefault(sourceWallet.isDefault());

        preview.setTargetWalletId(targetWalletId);
        preview.setTargetWalletName(targetWallet.getWalletName());
        preview.setTargetCurrency(targetWallet.getCurrencyCode());
        preview.setTargetBalance(targetWallet.getBalance());
        preview.setTargetTransactionCount((int) targetTransactionCount);

        preview.setFinalWalletName(targetWallet.getWalletName());
        preview.setFinalCurrency(targetCurrency);
        preview.setFinalBalance(finalBalance);
        preview.setTotalTransactions((int) (sourceTransactionCount + targetTransactionCount));
        preview.setWillTransferDefaultFlag(sourceWallet.isDefault());

        preview.setCanProceed(true);
        List<String> warnings = new ArrayList<>();

        if (!sourceWallet.getCurrencyCode().equals(targetCurrency) ||
                !targetWallet.getCurrencyCode().equals(targetCurrency)) {
            warnings.add("Số dư sẽ được chuyển đổi sang " + targetCurrency);
        }

        if (sourceWallet.isDefault()) {
            warnings.add("Ví mặc định sẽ được chuyển sang ví đích");
        }

        preview.setWarnings(warnings);

        return preview;
    }

    @Override
    @Transactional
    public MergeWalletResponse mergeWallets(Long userId, Long sourceWalletId, Long targetWalletId, String targetCurrency) {
        long startTime = System.currentTimeMillis();

        if (!isOwner(sourceWalletId, userId)) {
            throw new RuntimeException("Bạn không có quyền gộp ví nguồn này");
        }
        if (!isOwner(targetWalletId, userId)) {
            throw new RuntimeException("Bạn không có quyền gộp vào ví đích này");
        }

        if (sourceWalletId.equals(targetWalletId)) {
            throw new RuntimeException("Không thể gộp ví với chính nó");
        }

        Wallet sourceWallet = walletRepository.findByIdWithLock(sourceWalletId)
                .orElseThrow(() -> new RuntimeException("Ví nguồn không tồn tại"));

        Wallet targetWallet = walletRepository.findByIdWithLock(targetWalletId)
                .orElseThrow(() -> new RuntimeException("Ví đích không tồn tại"));

        if (!currencyRepository.existsById(targetCurrency)) {
            throw new RuntimeException("Loại tiền tệ không hợp lệ: " + targetCurrency);
        }

        String sourceWalletName = sourceWallet.getWalletName();
        String sourceCurrency = sourceWallet.getCurrencyCode();
        BigDecimal sourceBalance = sourceWallet.getBalance();
        int sourceTransactionCount = (int) transactionRepository.countByWallet_WalletId(sourceWalletId);

        BigDecimal targetBalanceBefore = targetWallet.getBalance();
        int targetTransactionCountBefore = (int) transactionRepository.countByWallet_WalletId(targetWalletId);
        boolean wasSourceDefault = sourceWallet.isDefault();

        BigDecimal sourceBalanceConverted = sourceBalance;
        if (!sourceCurrency.equals(targetCurrency)) {
            sourceBalanceConverted = exchangeRateService.convertAmount(
                    sourceBalance,
                    sourceCurrency,
                    targetCurrency
            );
        }

        BigDecimal targetBalanceConverted = targetBalanceBefore;
        if (!targetWallet.getCurrencyCode().equals(targetCurrency)) {
            targetBalanceConverted = exchangeRateService.convertAmount(
                    targetBalanceBefore,
                    targetWallet.getCurrencyCode(),
                    targetCurrency
            );
        }

        targetWallet.setCurrencyCode(targetCurrency);
        targetWallet.setBalance(sourceBalanceConverted.add(targetBalanceConverted));

        if (wasSourceDefault) {
            walletRepository.unsetDefaultWallet(userId, targetWalletId);
            targetWallet.setDefault(true);
        }

        walletRepository.save(targetWallet);

        List<Transaction> sourceTransactions = transactionRepository.findByWallet_WalletId(sourceWalletId);
        LocalDateTime mergeDate = LocalDateTime.now();

        String originalSourceCurrency = sourceWallet.getCurrencyCode();

        for (Transaction tx : sourceTransactions) {
            String txOriginalCurrency = tx.getOriginalCurrency() != null
                    ? tx.getOriginalCurrency()
                    : originalSourceCurrency;

            BigDecimal txOriginalAmount = tx.getOriginalAmount() != null
                    ? tx.getOriginalAmount()
                    : tx.getAmount();

            if (tx.getOriginalAmount() == null) {
                tx.setOriginalAmount(txOriginalAmount);
                tx.setOriginalCurrency(txOriginalCurrency);
            }

            if (!txOriginalCurrency.equals(targetCurrency)) {
                BigDecimal convertedAmount = exchangeRateService.convertAmount(
                        txOriginalAmount,
                        txOriginalCurrency,
                        targetCurrency
                );
                tx.setAmount(convertedAmount);

                BigDecimal rate = exchangeRateService.getExchangeRate(
                        txOriginalCurrency,
                        targetCurrency
                );
                tx.setExchangeRate(rate);
            } else {
                tx.setAmount(txOriginalAmount);
                tx.setExchangeRate(BigDecimal.ONE);
            }

            tx.setWallet(targetWallet);
            tx.setMergeDate(mergeDate);
            transactionRepository.save(tx);
        }

        List<WalletMember> sourceMembers = walletMemberRepository.findByWallet_WalletId(sourceWalletId);
        for (WalletMember member : sourceMembers) {
            // ✅ ĐÃ ĐỔI User_UserId → User_Id
            boolean existsInTarget = walletMemberRepository.existsByWallet_WalletIdAndUser_Id(
                    targetWalletId,
                    member.getUser().getId()
            );

            if (!existsInTarget) {
                WalletMember newMember = new WalletMember(
                        targetWallet,
                        member.getUser(),
                        WalletRole.MEMBER
                );
                walletMemberRepository.save(newMember);
            }
        }

        walletMemberRepository.deleteAll(sourceMembers);

        List<WalletTransfer> sourceTransfers = walletTransferRepository.findByWalletId(sourceWalletId);

        for (WalletTransfer transfer : sourceTransfers) {
            boolean fromIsSource = transfer.getFromWallet().getWalletId().equals(sourceWalletId);
            boolean toIsSource = transfer.getToWallet().getWalletId().equals(sourceWalletId);

            if (fromIsSource && toIsSource) {
                walletTransferRepository.delete(transfer);
                continue;
            }

            if (fromIsSource) {
                transfer.setFromWallet(targetWallet);
            }

            if (toIsSource) {
                transfer.setToWallet(targetWallet);
            }

            if (transfer.getFromWallet().getWalletId().equals(targetWalletId) &&
                    transfer.getToWallet().getWalletId().equals(targetWalletId)) {
                walletTransferRepository.delete(transfer);
                continue;
            }

            walletTransferRepository.save(transfer);
        }

        walletRepository.delete(sourceWallet);

        WalletMergeHistory history = new WalletMergeHistory();
        history.setUserId(userId);
        history.setSourceWalletId(sourceWalletId);
        history.setSourceWalletName(sourceWalletName);
        history.setSourceCurrency(sourceCurrency);
        history.setSourceBalance(sourceBalance);
        history.setSourceTransactionCount(sourceTransactionCount);

        history.setTargetWalletId(targetWalletId);
        history.setTargetWalletName(targetWallet.getWalletName());
        history.setTargetCurrency(targetCurrency);
        history.setTargetBalanceBefore(targetBalanceBefore);
        history.setTargetBalanceAfter(targetWallet.getBalance());
        history.setTargetTransactionCountBefore(targetTransactionCountBefore);
        history.setMergedAt(mergeDate);
        history.setMergeDurationMs(System.currentTimeMillis() - startTime);

        WalletMergeHistory savedHistory = walletMergeHistoryRepository.save(history);

        MergeWalletResponse response = new MergeWalletResponse();
        response.setSuccess(true);
        response.setMessage("Gộp ví thành công");
        response.setTargetWalletId(targetWalletId);
        response.setTargetWalletName(targetWallet.getWalletName());
        response.setFinalBalance(targetWallet.getBalance());
        response.setFinalCurrency(targetCurrency);
        response.setMergedTransactions(sourceTransactionCount);
        response.setSourceWalletName(sourceWalletName);
        response.setWasDefaultTransferred(wasSourceDefault);
        response.setMergeHistoryId(savedHistory.getMergeId());
        response.setMergedAt(mergeDate);

        return response;
    }

    @Override
    @Transactional
    public DeleteWalletResponse deleteWallet(Long userId, Long walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        if (!isOwner(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền xóa ví này");
        }

        boolean hasTransactions = transactionRepository.existsByWallet_WalletId(walletId);
        if (hasTransactions) {
            throw new RuntimeException("Không thể xóa ví. Bạn phải xóa các giao dịch trong ví này trước.");
        }

        boolean wasDefault = wallet.isDefault();

        if (wasDefault) {
            throw new RuntimeException("Không thể xóa ví mặc định.");
        }

        List<WalletMember> members = walletMemberRepository.findByWallet_WalletId(walletId);
        int membersRemoved = members.size();
        walletMemberRepository.deleteAll(members);

        walletRepository.delete(wallet);

        DeleteWalletResponse response = new DeleteWalletResponse(
                wallet.getWalletId(),
                wallet.getWalletName(),
                wallet.getBalance(),
                wallet.getCurrencyCode()
        );
        response.setWasDefault(wasDefault);
        response.setMembersRemoved(membersRemoved);
        response.setTransactionsDeleted(0);

        return response;
    }

    // ---------------- TRANSFER MONEY ----------------
    @Override
    @Transactional
    public TransferMoneyResponse transferMoney(Long userId, TransferMoneyRequest request) {

        if (request.getFromWalletId() == null || request.getToWalletId() == null)
            throw new RuntimeException("Vui lòng chọn ví nguồn và ví đích");

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Số tiền phải lớn hơn 0");

        if (request.getFromWalletId().equals(request.getToWalletId()))
            throw new RuntimeException("Không thể chuyển tiền cho chính ví");

        Wallet fromWallet = walletRepository.findByIdWithLock(request.getFromWalletId())
                .orElseThrow(() -> new RuntimeException("Ví nguồn không tồn tại"));

        Wallet toWallet = walletRepository.findByIdWithLock(request.getToWalletId())
                .orElseThrow(() -> new RuntimeException("Ví đích không tồn tại"));

        if (!hasAccess(request.getFromWalletId(), userId))
            throw new RuntimeException("Bạn không có quyền ví nguồn");

        if (!hasAccess(request.getToWalletId(), userId))
            throw new RuntimeException("Bạn không có quyền ví đích");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String sourceCurrency = request.getTargetCurrencyCode() != null
                ? request.getTargetCurrencyCode()
                : fromWallet.getCurrencyCode();

        BigDecimal sourceAmount = request.getAmount();

        if (fromWallet.getBalance().compareTo(sourceAmount) < 0)
            throw new RuntimeException("Số dư ví nguồn không đủ");

        BigDecimal targetAmount = sourceAmount;
        if (!fromWallet.getCurrencyCode().equals(toWallet.getCurrencyCode())) {
            targetAmount = exchangeRateService.convertAmount(
                    sourceAmount,
                    fromWallet.getCurrencyCode(),
                    toWallet.getCurrencyCode()
            );
        }

        BigDecimal fromBefore = fromWallet.getBalance();
        BigDecimal toBefore = toWallet.getBalance();

        long sourceMembers = walletMemberRepository.countByWallet_WalletId(fromWallet.getWalletId());
        long targetMembers = walletMemberRepository.countByWallet_WalletId(toWallet.getWalletId());

        boolean sourceShared = sourceMembers > 1;
        boolean targetShared = targetMembers > 1;

        LocalDateTime time = LocalDateTime.now();

        fromWallet.setBalance(fromBefore.subtract(sourceAmount));
        walletRepository.save(fromWallet);

        toWallet.setBalance(toBefore.add(targetAmount));
        walletRepository.save(toWallet);

        WalletTransfer transfer = new WalletTransfer();
        transfer.setFromWallet(fromWallet);
        transfer.setToWallet(toWallet);
        transfer.setAmount(sourceAmount);
        transfer.setCurrencyCode(sourceCurrency);
        transfer.setUser(user);
        transfer.setNote(request.getNote());
        transfer.setTransferDate(time);
        transfer.setStatus(WalletTransfer.TransferStatus.COMPLETED);
        transfer.setFromBalanceBefore(fromBefore);
        transfer.setFromBalanceAfter(fromWallet.getBalance());
        transfer.setToBalanceBefore(toBefore);
        transfer.setToBalanceAfter(toWallet.getBalance());

        WalletTransfer saved = walletTransferRepository.save(transfer);

        TransferMoneyResponse response = new TransferMoneyResponse();
        response.setTransferId(saved.getTransferId());
        response.setStatus(saved.getStatus().toString());
        response.setAmount(sourceAmount);
        response.setCurrencyCode(sourceCurrency);
        response.setTransferredAt(time);
        response.setNote(request.getNote());

        response.setFromWalletId(fromWallet.getWalletId());
        response.setFromWalletName(fromWallet.getWalletName());
        response.setFromWalletBalanceBefore(fromBefore);
        response.setFromWalletBalanceAfter(fromWallet.getBalance());

        response.setToWalletId(toWallet.getWalletId());
        response.setToWalletName(toWallet.getWalletName());
        response.setToWalletBalanceBefore(toBefore);
        response.setToWalletBalanceAfter(toWallet.getBalance());

        response.setFromWalletIsShared(sourceShared);
        response.setFromWalletMemberCount((int) sourceMembers);
        response.setToWalletIsShared(targetShared);
        response.setToWalletMemberCount((int) targetMembers);

        return response;
    }

    // ---------------- GET ALL TRANSFERS ----------------
    @Override
    public List<WalletTransfer> getAllTransfers(Long userId) {
        // ✅ ĐÃ ĐỔI User_UserId → User_Id
        return walletTransferRepository.findByUser_IdOrderByTransferDateDesc(userId);
    }

    // ---------------- UPDATE TRANSFER ----------------
    @Override
    @Transactional
    public WalletTransfer updateTransfer(Long userId, Long transferId, UpdateTransferRequest request) {
        WalletTransfer transfer = walletTransferRepository.findByIdWithUser(transferId)
                .orElseThrow(() -> new RuntimeException("Giao dịch chuyển tiền không tồn tại"));

        if (transfer.getUser() == null || !transfer.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa giao dịch này");
        }

        if (request.getNote() != null) {
            transfer.setNote(request.getNote().trim().isEmpty() ? null : request.getNote().trim());
        } else {
            transfer.setNote(null);
        }

        return walletTransferRepository.save(transfer);
    }

    // ---------------- DELETE TRANSFER ----------------
    @Override
    @Transactional
    public void deleteTransfer(Long userId, Long transferId) {
        WalletTransfer transfer = walletTransferRepository.findByIdForDelete(transferId)
                .orElseThrow(() -> new RuntimeException("Giao dịch chuyển tiền không tồn tại"));

        if (transfer.getUser() == null || !transfer.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa giao dịch này");
        }

        Wallet fromWallet = walletRepository.findByIdWithLock(transfer.getFromWallet().getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví gửi không tồn tại"));
        Wallet toWallet = walletRepository.findByIdWithLock(transfer.getToWallet().getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví nhận không tồn tại"));

        BigDecimal originalAmount = transfer.getAmount();

        BigDecimal targetAmountAdded = transfer.getToBalanceAfter().subtract(transfer.getToBalanceBefore());

        BigDecimal newFromBalance = fromWallet.getBalance().add(originalAmount);

        BigDecimal newToBalance = toWallet.getBalance().subtract(targetAmountAdded);

        if (newToBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Không thể xóa giao dịch vì ví âm tiền");
        }

        fromWallet.setBalance(newFromBalance);
        walletRepository.save(fromWallet);

        toWallet.setBalance(newToBalance);
        walletRepository.save(toWallet);

        walletTransferRepository.delete(transfer);
    }

    // ---------------- HELPER ----------------
    private WalletMemberDTO convertToMemberDTO(WalletMember member) {
        User u = member.getUser();
        return new WalletMemberDTO(
                member.getMemberId(),
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getAvatar(),
                member.getRole().toString(),
                member.getJoinedAt()
        );
    }
}
