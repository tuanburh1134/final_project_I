-- SQL Script để tạo user test cho tính năng Login
-- Chạy script này sau khi đã chạy backend lần đầu (để tạo tables)

-- Xóa user test cũ nếu có
DELETE FROM Users WHERE email = 'test@example.com';

-- Tạo user test với tài khoản đã được kích hoạt
-- Password: Test123! (đã được hash bằng BCrypt)
INSERT INTO Users (UserID, userName, email, password, fullName, phone, CreatedAt, IsActive)
VALUES (
    UUID(),
    'testuser',
    'test@example.com',
    '$2a$10$XpY5xD8vvQ4jG8wL9r8XHOKz8rHm9BqG5NqXl8Fy7J6jD9L8mXY8e', -- Test123!
    'Test User',
    '0123456789',
    NOW(),
    true -- Account đã được kích hoạt
);

-- Kiểm tra user đã được tạo
SELECT * FROM Users WHERE email = 'test@example.com';

-- =====================================================
-- NOTE: Password hash trên là mẫu, bạn cần tạo hash thực
-- Để tạo BCrypt hash cho password mới, có thể:
-- 1. Dùng online tool: https://bcrypt-generator.com/
-- 2. Hoặc đăng ký qua API /api/auth/register rồi kích hoạt
-- =====================================================

-- Tạo thêm một user test khác (chưa kích hoạt)
DELETE FROM Users WHERE email = 'inactive@example.com';

INSERT INTO Users (UserID, userName, email, password, fullName, phone, CreatedAt, IsActive)
VALUES (
    UUID(),
    'inactiveuser',
    'inactive@example.com',
    '$2a$10$XpY5xD8vvQ4jG8wL9r8XHOKz8rHm9BqG5NqXl8Fy7J6jD9L8mXY8e', -- Test123!
    'Inactive User',
    '0987654321',
    NOW(),
    false -- Account CHƯA được kích hoạt
);

SELECT * FROM Users WHERE email = 'inactive@example.com';

-- =====================================================
-- Test Cases:
-- =====================================================
-- 1. Login thành công:
--    Email: test@example.com
--    Password: Test123!
--    Kết quả: Nhận được access token và refresh token
--
-- 2. Login với tài khoản chưa kích hoạt:
--    Email: inactive@example.com
--    Password: Test123!
--    Kết quả: "Tài khoản chưa được kích hoạt..."
--
-- 3. Login với password sai:
--    Email: test@example.com
--    Password: WrongPassword
--    Kết quả: "Email hoặc mật khẩu không đúng"
--
-- 4. Login với email không tồn tại:
--    Email: notexist@example.com
--    Password: Test123!
--    Kết quả: "Email hoặc mật khẩu không đúng"
-- =====================================================

