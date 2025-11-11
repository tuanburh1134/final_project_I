-- ============================================
-- MIGRATION: SHARED WALLET FEATURE
-- Tạo bảng wallet_members để hỗ trợ chia sẻ ví
-- ============================================

-- Tạo bảng wallet_members
CREATE TABLE IF NOT EXISTS wallet_members (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'OWNER hoặc MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys với CASCADE delete
    CONSTRAINT fk_wallet_member_wallet 
        FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_wallet_member_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) 
        ON DELETE CASCADE,
    
    -- Unique constraint: một user chỉ có thể là member của 1 wallet 1 lần
    CONSTRAINT unique_wallet_user 
        UNIQUE (wallet_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo index cho performance
CREATE INDEX idx_wallet_members_wallet_id ON wallet_members(wallet_id);
CREATE INDEX idx_wallet_members_user_id ON wallet_members(user_id);
CREATE INDEX idx_wallet_members_role ON wallet_members(role);

-- ============================================
-- MIGRATE DỮ LIỆU CŨ
-- Chuyển tất cả wallets hiện có thành OWNER trong bảng wallet_members
-- ============================================

INSERT INTO wallet_members (wallet_id, user_id, role, joined_at)
SELECT 
    w.wallet_id,
    w.user_id,
    'OWNER' as role,
    w.created_at as joined_at
FROM wallets w
WHERE NOT EXISTS (
    SELECT 1 FROM wallet_members wm 
    WHERE wm.wallet_id = w.wallet_id AND wm.user_id = w.user_id
);

-- ============================================
-- VERIFY MIGRATION
-- Kiểm tra xem migration có thành công không
-- ============================================

-- Số lượng wallets phải bằng số lượng OWNER members
SELECT 
    'Wallets' as table_name, 
    COUNT(*) as count 
FROM wallets
UNION ALL
SELECT 
    'Owner Members' as table_name, 
    COUNT(*) as count 
FROM wallet_members 
WHERE role = 'OWNER';

-- Hiển thị sample data
SELECT 
    wm.member_id,
    wm.wallet_id,
    w.wallet_name,
    u.full_name as member_name,
    u.email,
    wm.role,
    wm.joined_at
FROM wallet_members wm
JOIN wallets w ON wm.wallet_id = w.wallet_id
JOIN users u ON wm.user_id = u.user_id
LIMIT 10;

-- ============================================
-- ROLLBACK SCRIPT (nếu cần)
-- Uncomment và chạy nếu muốn rollback migration
-- ============================================

-- DROP TABLE IF EXISTS wallet_members;

-- ============================================
-- NOTES
-- ============================================
-- 1. Migration này sẽ tự động chạy khi khởi động Spring Boot nếu ddl-auto=update
-- 2. Nếu dùng Flyway/Liquibase, chuyển script này sang format tương ứng
-- 3. Backup database trước khi chạy migration
-- 4. Test trên môi trường dev/staging trước khi deploy production

