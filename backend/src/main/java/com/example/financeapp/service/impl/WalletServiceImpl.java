package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.repository.CurrencyRepository;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    /**
     * Tạo ví mới cho người dùng.
     */
    @Override
    @Transactional
    public Wallet createWallet(Long userId, CreateWalletRequest request) {
        // ✅ 1. Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // ✅ 2. Kiểm tra mã tiền tệ tồn tại trong bảng currencies
        String currencyCode = request.getCurrencyCode().toUpperCase().trim();
        if (!currencyRepository.existsById(currencyCode)) {
            throw new RuntimeException("Mã tiền tệ không hợp lệ: " + currencyCode);
        }

        // ✅ 3. Kiểm tra tên ví trùng trong phạm vi user
        boolean walletExists = walletRepository.existsByWalletNameAndUser_UserId(
                request.getWalletName().trim(), userId
        );
        if (walletExists) {
            throw new RuntimeException("Bạn đã có ví tên \"" + request.getWalletName() + "\". Vui lòng chọn tên khác.");
        }

        // ✅ 4. Tạo ví mới
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName().trim());
        wallet.setCurrencyCode(currencyCode);
        wallet.setDescription(request.getDescription());

        // Nếu initialBalance null hoặc âm → đặt 0
        BigDecimal initialBalance = (request.getInitialBalance() == null
                || request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0)
                ? BigDecimal.ZERO
                : request.getInitialBalance();

        wallet.setBalance(initialBalance);

        // ✅ 5. Lưu ví vào DB
        return walletRepository.save(wallet);
    }

    /**
     * Lấy danh sách ví theo userId.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Wallet> getWalletsByUserId(Long userId) {
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Người dùng không tồn tại (ID: " + userId + ")");
        }

        return walletRepository.findByUser_UserId(userId);
    }

    /**
     * Cập nhật thông tin ví.
     */
    @Override
    @Transactional
    public Wallet updateWallet(Long walletId, String name, String currencyCode) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví với ID: " + walletId));

        if (name != null && !name.trim().isEmpty()) {
            wallet.setWalletName(name.trim());
        }

        if (currencyCode != null && !currencyCode.trim().isEmpty()) {
            String upperCurrency = currencyCode.toUpperCase().trim();
            if (!currencyRepository.existsById(upperCurrency)) {
                throw new RuntimeException("Mã tiền tệ không hợp lệ: " + upperCurrency);
            }
            wallet.setCurrencyCode(upperCurrency);
        }

        return walletRepository.save(wallet);
    }
}
