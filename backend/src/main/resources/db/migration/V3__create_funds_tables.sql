-- =====================================================
-- Migration: Thêm cột soft delete cho wallets & tạo bảng funds
-- Mục đích: Hỗ trợ chức năng quỹ và xóa mềm ví/quỹ
-- =====================================================

-- Thêm cột soft delete cho bảng wallets (nếu bảng tồn tại và cột chưa có)
-- Kiểm tra bảng wallets tồn tại
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wallets'
);

-- Chỉ thêm cột nếu bảng tồn tại
SET @sql1 = IF(@table_exists > 0 AND NOT EXISTS(
    SELECT 1 FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wallets' 
    AND COLUMN_NAME = 'is_deleted'
), 
    'ALTER TABLE wallets ADD COLUMN is_deleted BIT NOT NULL DEFAULT 0',
    'SELECT ''Column is_deleted already exists or table wallets does not exist'' AS message'
);
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @sql2 = IF(@table_exists > 0 AND NOT EXISTS(
    SELECT 1 FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wallets' 
    AND COLUMN_NAME = 'deleted_at'
), 
    'ALTER TABLE wallets ADD COLUMN deleted_at DATETIME NULL',
    'SELECT ''Column deleted_at already exists or table wallets does not exist'' AS message'
);
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Bảng funds (tạo không có foreign key constraints trước)
CREATE TABLE IF NOT EXISTS funds (
    fund_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    fund_type VARCHAR(20) NOT NULL,
    term_type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL UNIQUE,
    currency_code VARCHAR(3) NOT NULL,
    target_amount DECIMAL(15, 2),
    start_date DATE,
    end_date DATE,
    frequency VARCHAR(50),
    amount_per_cycle DECIMAL(15, 2),
    reminder_type VARCHAR(30) DEFAULT 'NONE',
    reminder_time VARCHAR(10),
    auto_topup_type VARCHAR(30) DEFAULT 'NONE',
    auto_topup_config VARCHAR(255),
    notes VARCHAR(1000),
    is_closed BIT NOT NULL DEFAULT 0,
    closed_at DATETIME,
    is_deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fund_owner (owner_id),
    INDEX idx_fund_type (fund_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng fund_members (tạo không có foreign key constraints trước)
CREATE TABLE IF NOT EXISTS fund_members (
    fund_member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fund_member UNIQUE (fund_id, user_id),
    INDEX idx_fund_member_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm foreign key constraints sau khi bảng tham chiếu tồn tại
-- Kiểm tra và thêm foreign key cho funds.owner_id -> users.user_id
SET @users_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users'
);

SET @fk_fund_owner_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'funds' 
    AND CONSTRAINT_NAME = 'fk_fund_owner'
);

SET @sql_fk_owner = IF(@users_exists > 0 AND @fk_fund_owner_exists = 0,
    'ALTER TABLE funds ADD CONSTRAINT fk_fund_owner FOREIGN KEY (owner_id) REFERENCES users(user_id)',
    'SELECT ''Foreign key fk_fund_owner already exists or users table does not exist'' AS message'
);
PREPARE stmt_fk_owner FROM @sql_fk_owner;
EXECUTE stmt_fk_owner;
DEALLOCATE PREPARE stmt_fk_owner;

-- Kiểm tra và thêm foreign key cho funds.wallet_id -> wallets.wallet_id
SET @wallets_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wallets'
);

SET @fk_fund_wallet_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'funds' 
    AND CONSTRAINT_NAME = 'fk_fund_wallet'
);

SET @sql_fk_wallet = IF(@wallets_exists > 0 AND @fk_fund_wallet_exists = 0,
    'ALTER TABLE funds ADD CONSTRAINT fk_fund_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id)',
    'SELECT ''Foreign key fk_fund_wallet already exists or wallets table does not exist'' AS message'
);
PREPARE stmt_fk_wallet FROM @sql_fk_wallet;
EXECUTE stmt_fk_wallet;
DEALLOCATE PREPARE stmt_fk_wallet;

-- Kiểm tra và thêm foreign key cho fund_members.fund_id -> funds.fund_id
SET @fk_fund_member_fund_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'fund_members' 
    AND CONSTRAINT_NAME = 'fk_fund_member_fund'
);

SET @sql_fk_member_fund = IF(@fk_fund_member_fund_exists = 0,
    'ALTER TABLE fund_members ADD CONSTRAINT fk_fund_member_fund FOREIGN KEY (fund_id) REFERENCES funds(fund_id) ON DELETE CASCADE',
    'SELECT ''Foreign key fk_fund_member_fund already exists'' AS message'
);
PREPARE stmt_fk_member_fund FROM @sql_fk_member_fund;
EXECUTE stmt_fk_member_fund;
DEALLOCATE PREPARE stmt_fk_member_fund;

-- Kiểm tra và thêm foreign key cho fund_members.user_id -> users.user_id
SET @fk_fund_member_user_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'fund_members' 
    AND CONSTRAINT_NAME = 'fk_fund_member_user'
);

SET @sql_fk_member_user = IF(@users_exists > 0 AND @fk_fund_member_user_exists = 0,
    'ALTER TABLE fund_members ADD CONSTRAINT fk_fund_member_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE',
    'SELECT ''Foreign key fk_fund_member_user already exists or users table does not exist'' AS message'
);
PREPARE stmt_fk_member_user FROM @sql_fk_member_user;
EXECUTE stmt_fk_member_user;
DEALLOCATE PREPARE stmt_fk_member_user;

