package com.example.financeapp.wallet.service;

import com.example.financeapp.wallet.dto.request.CreateWalletRequest;
import com.example.financeapp.wallet.dto.request.TransferMoneyRequest;
import com.example.financeapp.wallet.dto.request.UpdateTransferRequest;
import com.example.financeapp.wallet.dto.request.UpdateWalletRequest;
import com.example.financeapp.wallet.dto.response.*;
import com.example.financeapp.wallet.entity.Wallet;
import com.example.financeapp.wallet.entity.WalletTransfer;
import com.example.financeapp.wallet.entity.WalletMember.WalletRole; // [QUAN TRỌNG] Import Enum này
import org.springframework.transaction.annotation.Transactional; // [NÊN DÙNG] Dùng của Spring thay vì jakarta

import java.util.List;

public interface WalletService {

    // --- CORE ---
    Wallet createWallet(Long userId, CreateWalletRequest request);

    Wallet updateWallet(Long userId, Long walletId, UpdateWalletRequest request);

    List<Wallet> getWalletsByUserId(Long userId);

    Wallet getWalletDetails(Long userId, Long walletId);

    @Transactional
    void setDefaultWallet(Long userId, Long walletId);

    List<SharedWalletDTO> getAllAccessibleWallets(Long userId);

    // --- MEMBER MANAGEMENT ---
    WalletMemberDTO shareWallet(Long walletId, Long ownerId, String memberEmail);

    List<WalletMemberDTO> getWalletMembers(Long walletId, Long requesterId);

    void updateMemberRole(Long walletId, Long targetMemberId, WalletRole newRole, Long actorUserId);

    void removeMember(Long walletId, Long ownerId, Long memberUserId);

    void leaveWallet(Long walletId, Long userId);

    // --- ACCESS CHECKS ---
    boolean hasAccess(Long walletId, Long userId);

    boolean isOwner(Long walletId, Long userId);

    // --- MERGE WALLETS ---
    List<MergeCandidateDTO> getMergeCandidates(Long userId, Long sourceWalletId);

    MergeWalletPreviewResponse previewMerge(Long userId, Long sourceWalletId, Long targetWalletId, String targetCurrency);

    @Transactional
    MergeWalletResponse mergeWallets(Long userId, Long sourceWalletId, Long targetWalletId, String targetCurrency);

    // --- DELETE & TRANSFER ---
    @Transactional
    DeleteWalletResponse deleteWallet(Long userId, Long walletId);

    @Transactional
    TransferMoneyResponse transferMoney(Long userId, TransferMoneyRequest request);

    List<WalletTransfer> getAllTransfers(Long userId);

    WalletTransfer updateTransfer(Long userId, Long transferId, UpdateTransferRequest request);

    @Transactional
    void deleteTransfer(Long userId, Long transferId);
}