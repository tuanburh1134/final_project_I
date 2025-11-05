# 🧪 Hướng dẫn Test tính năng Login với CAPTCHA

## 📌 Tổng quan

Có 2 cách để test tính năng login:
1. **Test với CAPTCHA thực** (production-like)
2. **Test skip CAPTCHA** (development mode)

## 🚀 Phương pháp 1: Test skip CAPTCHA (Recommended cho Dev)

### Bước 1: Tạm thời disable CAPTCHA verification

Mở file `CaptchaServiceImpl.java` và thêm điều kiện:

```java
@Override
public boolean verifyCaptcha(String captchaToken) {
    // DEVELOPMENT MODE: Tạm thời skip CAPTCHA verification
    // TODO: XÓA DÒNG NÀY KHI DEPLOY PRODUCTION
    if ("DEV_MODE".equals(captchaToken)) {
        log.info("DEV MODE: Skipping CAPTCHA verification");
        return true;
    }
    
    if (captchaToken == null || captchaToken.isEmpty()) {
        log.warn("CAPTCHA token is null or empty");
        return false;
    }
    
    // ... phần còn lại giữ nguyên
}
```

### Bước 2: Tạo test user

#### Option 1: Chạy SQL Script
```bash
# Kết nối MySQL
mysql -u root -p

# Chọn database
use financeapp;

# Chạy script
source test-user.sql;
```

#### Option 2: Chạy PasswordHashGenerator
```bash
# Trong IDE (IntelliJ/Eclipse)
1. Mở file: src/test/java/.../PasswordHashGenerator.java
2. Right-click -> Run 'PasswordHashGenerator.main()'
3. Copy password hash từ output
4. Insert vào database
```

#### Option 3: Đăng ký qua API
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "confirmPassword": "Test123!",
    "fullName": "Test User",
    "userName": "testuser"
  }'

# Sau đó vào database và set IsActive = true
UPDATE Users SET IsActive = true WHERE email = 'test@example.com';
```

### Bước 3: Test Login với Postman/cURL

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "captchaToken": "DEV_MODE"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "test@example.com",
  "fullName": "Test User",
  "userName": "testuser"
}
```

## 🌐 Phương pháp 2: Test với CAPTCHA thực

### Bước 1: Cấu hình Google reCAPTCHA

1. Truy cập: https://www.google.com/recaptcha/admin
2. Tạo site mới:
   - Label: Finance App Test
   - Type: reCAPTCHA v2 - "I'm not a robot"
   - Domains: `localhost`
3. Lấy **Site Key** và **Secret Key**

### Bước 2: Cấu hình Backend

Cập nhật `application.properties`:
```properties
recaptcha.secret.key=YOUR_SECRET_KEY_HERE
```

### Bước 3: Tạo HTML test page

Tạo file `test-captcha.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Test Login with CAPTCHA</title>
    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>
<body>
    <h2>Test Login</h2>
    <form id="loginForm">
        <div>
            <label>Email:</label>
            <input type="email" id="email" value="test@example.com" required>
        </div>
        <div>
            <label>Password:</label>
            <input type="password" id="password" value="Test123!" required>
        </div>
        <div>
            <div class="g-recaptcha" data-sitekey="YOUR_SITE_KEY_HERE"></div>
        </div>
        <button type="submit">Login</button>
    </form>

    <div id="response"></div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const captchaResponse = grecaptcha.getResponse();
            if (!captchaResponse) {
                alert('Please complete CAPTCHA');
                return;
            }

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        email: email,
                        password: password,
                        captchaToken: captchaResponse
                    })
                });

                const data = await response.json();
                document.getElementById('response').innerHTML = 
                    '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
                
                if (response.ok) {
                    console.log('Access Token:', data.accessToken);
                    console.log('Refresh Token:', data.refreshToken);
                }
            } catch (error) {
                console.error('Error:', error);
                document.getElementById('response').innerHTML = 
                    '<pre>Error: ' + error.message + '</pre>';
            }
        });
    </script>
</body>
</html>
```

Mở file này trong browser và test!

## 📋 Test Cases

### ✅ Test Case 1: Login thành công
```json
{
  "email": "test@example.com",
  "password": "Test123!",
  "captchaToken": "DEV_MODE"
}
```
**Expected**: 200 OK với tokens

### ❌ Test Case 2: Password sai
```json
{
  "email": "test@example.com",
  "password": "WrongPassword",
  "captchaToken": "DEV_MODE"
}
```
**Expected**: 401 Unauthorized - "Email hoặc mật khẩu không đúng"

### ❌ Test Case 3: Account chưa kích hoạt
```json
{
  "email": "inactive@example.com",
  "password": "Test123!",
  "captchaToken": "DEV_MODE"
}
```
**Expected**: 400 Bad Request - "Tài khoản chưa được kích hoạt..."

### ❌ Test Case 4: Email không tồn tại
```json
{
  "email": "notexist@example.com",
  "password": "Test123!",
  "captchaToken": "DEV_MODE"
}
```
**Expected**: 401 Unauthorized - "Email hoặc mật khẩu không đúng"

### ❌ Test Case 5: Không có CAPTCHA token
```json
{
  "email": "test@example.com",
  "password": "Test123!"
}
```
**Expected**: 400 Bad Request - Validation error

### ❌ Test Case 6: CAPTCHA không hợp lệ
```json
{
  "email": "test@example.com",
  "password": "Test123!",
  "captchaToken": "INVALID_TOKEN"
}
```
**Expected**: 400 Bad Request - "Xác thực CAPTCHA không thành công..."

## 🔧 Sử dụng Postman Collection

### Import Collection
1. Mở Postman
2. File -> Import
3. Chọn file `FinanceApp-Login-API.postman_collection.json`

### Test với Collection
1. Chọn request "Login (Successful)"
2. Thay `captchaToken` thành `DEV_MODE`
3. Click Send
4. Access token và refresh token sẽ tự động được lưu vào variables

### Auto-save Tokens
Collection đã có script để tự động lưu tokens sau khi login thành công.

## 🎯 Verify JWT Token

### Online Tool
1. Copy access token từ response
2. Truy cập: https://jwt.io
3. Paste token vào ô "Encoded"
4. Xem payload để verify thông tin user

### Expected Payload
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "test@example.com",
  "userName": "testuser",
  "fullName": "Test User",
  "type": "access",
  "sub": "test@example.com",
  "iat": 1699200000,
  "exp": 1699203600
}
```

## 📊 Check Database

```sql
-- Xem tất cả users
SELECT UserID, userName, email, fullName, IsActive, CreatedAt 
FROM Users;

-- Xem user test
SELECT * FROM Users WHERE email = 'test@example.com';

-- Kích hoạt user thủ công
UPDATE Users SET IsActive = true WHERE email = 'test@example.com';
```

## 🐛 Troubleshooting

### Lỗi: Connection refused
- ✅ Backend đã chạy chưa? (`mvn spring-boot:run`)
- ✅ Port 8080 có bị chiếm không?

### Lỗi: CAPTCHA verification failed (DEV_MODE)
- ✅ Đã thêm điều kiện `if ("DEV_MODE".equals(captchaToken))` chưa?
- ✅ Code đã recompile chưa?

### Lỗi: Email hoặc mật khẩu không đúng
- ✅ User đã tồn tại trong database chưa?
- ✅ Password hash đúng chưa?
- ✅ Account đã được kích hoạt chưa? (IsActive = true)

### Lỗi: Database connection
- ✅ MySQL đã chạy chưa?
- ✅ Database `financeapp` đã tạo chưa?
- ✅ Credentials trong `application.properties` đúng chưa?

## 🎓 Tips

1. **Logging**: Xem console log của backend để debug
2. **Postman**: Dùng Postman Tests tab để tự động verify response
3. **Database**: Dùng MySQL Workbench để xem data trực quan
4. **JWT Debug**: Dùng jwt.io để inspect token
5. **Browser DevTools**: F12 -> Network tab để xem request/response

## ⚠️ Nhớ trước khi deploy Production

```java
// XÓA ĐOẠN NÀY trong CaptchaServiceImpl.java
if ("DEV_MODE".equals(captchaToken)) {
    return true;
}
```

✅ Cấu hình CAPTCHA secret key thực  
✅ Remove DEV_MODE check  
✅ Enable CORS cho domain thực  
✅ Configure HTTPS  
✅ Change JWT secret  

---

**Happy Testing!** 🚀

