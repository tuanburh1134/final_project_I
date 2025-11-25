-- =====================================================
-- Migration: Tạo bảng wallet_transfers
-- Mục đích: Tách riêng chuyển tiền nội bộ khỏi transactions
-- Ngày: 2024-01-15
-- =====================================================

CREATE TABLE IF NOT EXISTS wallet_transfers (
    transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Wallet information
    from_wallet_id BIGINT NOT NULL,
    to_wallet_id BIGINT NOT NULL,
    
    -- Amount information
    amount DECIMAL(15, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    
    -- Balance tracking (before & after)
    from_balance_before DECIMAL(15, 2),
    from_balance_after DECIMAL(15, 2),
    to_balance_before DECIMAL(15, 2),
    to_balance_after DECIMAL(15, 2),
    
    -- Metadata
    user_id BIGINT NOT NULL,
    note VARCHAR(500),
    transfer_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_transfer_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_from_wallet (from_wallet_id),
    INDEX idx_to_wallet (to_wallet_id),
    INDEX idx_user (user_id),
    INDEX idx_transfer_date (transfer_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Optional: Migrate existing transfer transactions
-- (Nếu bạn muốn migrate dữ liệu cũ)
-- =====================================================

-- NOTE: Bước này tùy chọn. Nếu bạn đã có dữ liệu transactions 
-- là chuyển tiền (có note chứa "Chuyển đến:" hoặc "Nhận từ:")
-- bạn có thể migrate sang bảng mới.

-- Ví dụ migration (cần customize theo logic của bạn):
-- INSERT INTO wallet_transfers (
--     from_wallet_id, 
--     to_wallet_id, 
--     amount, 
--     currency_code,
--     user_id,
--     note,
--     transfer_date,
--     status
-- )
-- SELECT 
--     t1.wallet_id as from_wallet_id,
--     t2.wallet_id as to_wallet_id,
--     t1.amount,
--     w1.currency_code,
--     t1.user_id,
--     t1.note,
--     t1.transaction_date,
--     'COMPLETED'
-- FROM transactions t1
-- INNER JOIN transactions t2 ON t1.transaction_date = t2.transaction_date 
--     AND t1.user_id = t2.user_id
-- INNER JOIN wallets w1 ON t1.wallet_id = w1.wallet_id
-- WHERE t1.note LIKE 'Chuyển đến:%'
--   AND t2.note LIKE 'Nhận từ:%'
--   AND t1.transaction_type_id = (SELECT type_id FROM transaction_types WHERE type_name = 'Chi tiêu')
--   AND t2.transaction_type_id = (SELECT type_id FROM transaction_types WHERE type_name = 'Thu nhập');

-- =====================================================
-- Verification queries
-- =====================================================

-- Kiểm tra bảng đã tạo thành công
SELECT COUNT(*) as total_transfers FROM wallet_transfers;

-- Xem cấu trúc bảng
DESCRIBE wallet_transfers;

