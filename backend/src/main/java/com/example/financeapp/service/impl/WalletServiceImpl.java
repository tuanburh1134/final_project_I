package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CreateWalletRequest;
import com.example.financeapp.dto.UpdateWalletRequest;
import com.example.financeapp.dto.WalletResponse;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.entity.WalletType;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.WalletRepository;
import com.example.financeapp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public WalletResponse createWallet(String userEmail, CreateWalletRequest request) {
        // Tìm user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Tạo wallet mới
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.getWalletName());
        wallet.setWalletType(request.getWalletType());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setCurrency(request.getCurrency() != null ? request.getCurrency() : "VND");
        wallet.setDescription(request.getDescription());
        wallet.setIcon(request.getIcon());
        wallet.setActive(true);

        Wallet savedWallet = walletRepository.save(wallet);
        return new WalletResponse(savedWallet);
    }

    @Override
    public List<WalletResponse> getAllWallets(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        List<Wallet> wallets = walletRepository.findByUser(user);
        return wallets.stream()
                .map(WalletResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletResponse> getActiveWallets(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        List<Wallet> wallets = walletRepository.findByUserAndIsActive(user, true);
        return wallets.stream()
                .map(WalletResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletResponse> getWalletsByType(String userEmail, WalletType walletType) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        List<Wallet> wallets = walletRepository.findByUserAndWalletType(user, walletType);
        return wallets.stream()
                .map(WalletResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponse getWalletById(String userEmail, Long walletId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Wallet wallet = walletRepository.findByWalletIdAndUser(walletId, user)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại hoặc không thuộc về bạn"));

        return new WalletResponse(wallet);
    }

    @Override
    public WalletResponse updateWallet(String userEmail, Long walletId, UpdateWalletRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Wallet wallet = walletRepository.findByWalletIdAndUser(walletId, user)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại hoặc không thuộc về bạn"));

        // Cập nhật tên ví nếu có
        if (request.getWalletName() != null && !request.getWalletName().trim().isEmpty()) {
            wallet.setWalletName(request.getWalletName().trim());
        }

        // Cập nhật loại ví nếu có
        if (request.getWalletType() != null) {
            wallet.setWalletType(request.getWalletType());
        }

        // Cập nhật đơn vị tiền tệ nếu có
        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            wallet.setCurrency(request.getCurrency().trim().toUpperCase());
        }

        // Cập nhật mô tả nếu có
        if (request.getDescription() != null) {
            wallet.setDescription(request.getDescription());
        }

        // Cập nhật icon nếu có
        if (request.getIcon() != null) {
            wallet.setIcon(request.getIcon());
        }

        // Cập nhật trạng thái nếu có
        if (request.getIsActive() != null) {
            wallet.setActive(request.getIsActive());
        }

        Wallet updatedWallet = walletRepository.save(wallet);
        return new WalletResponse(updatedWallet);
    }

    @Override
    public String getTotalBalance(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        List<Wallet> activeWallets = walletRepository.findByUserAndIsActive(user, true);

        BigDecimal total = activeWallets.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Format số theo định dạng Việt Nam
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        return formatter.format(total);
    }
}

