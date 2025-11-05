# Hướng dẫn triển khai tính năng Đăng nhập với CAPTCHA

## 📋 Tổng quan

Tính năng đăng nhập đã được tích hợp với Google reCAPTCHA v2 để bảo vệ ứng dụng khỏi:
- Bot tự động
- Brute-force attacks
- Spam đăng nhập

## 🔧 Các thành phần đã triển khai

### 1. **Dependencies** (`pom.xml`)
- ✅ `spring-boot-starter-webflux` - Để gọi Google reCAPTCHA API

### 2. **DTOs mới**
- ✅ `LoginRequest.java` - Nhận email, password, và captchaToken
- ✅ `LoginResponse.java` - Trả về accessToken, refreshToken, user info
- ✅ `CaptchaResponse.java` - Parse response từ Google API

### 3. **Services**
- ✅ `CaptchaService` - Interface xác thực CAPTCHA
- ✅ `CaptchaServiceImpl` - Implementation gọi Google API
- ✅ `AuthService.login()` - Logic đăng nhập với xác thực CAPTCHA

### 4. **Controller**
- ✅ `POST /api/auth/login` - Endpoint đăng nhập

### 5. **JWT Token**
- ✅ Access Token: Hết hạn sau 1 giờ (mặc định)
- ✅ Refresh Token: Hết hạn sau 7 ngày (mặc định)

## 🚀 Hướng dẫn cấu hình

### Bước 1: Đăng ký Google reCAPTCHA

1. Truy cập: https://www.google.com/recaptcha/admin
2. Đăng nhập bằng tài khoản Google
3. Nhấn "+" để tạo site mới
4. Điền thông tin:
   - **Label**: Finance App (hoặc tên bạn muốn)
   - **reCAPTCHA type**: Chọn "reCAPTCHA v2" → "I'm not a robot" Checkbox
   - **Domains**: 
     - `localhost` (cho development)
     - Domain production của bạn (nếu có)
   - Chấp nhận điều khoản
5. Nhấn "Submit"
6. Lưu lại:
   - **Site Key** (dùng cho frontend)
   - **Secret Key** (dùng cho backend)

### Bước 2: Cấu hình Backend

Mở file `application.properties` và thay đổi:

```properties
# Google reCAPTCHA Configuration
recaptcha.secret.key=YOUR_SECRET_KEY_HERE
recaptcha.verify.url=https://www.google.com/recaptcha/api/siteverify
```

**Lưu ý**: Không commit Secret Key lên Git! Hãy sử dụng environment variables trong production.

### Bước 3: Cấu hình Frontend

Trong frontend React, bạn cần:

1. Cài đặt thư viện reCAPTCHA:
```bash
npm install react-google-recaptcha
```

2. Thêm script vào `index.html`:
```html
<script src="https://www.google.com/recaptcha/api.js" async defer></script>
```

3. Sử dụng trong LoginForm:
```jsx
import ReCAPTCHA from "react-google-recaptcha";

function LoginForm() {
  const [captchaToken, setCaptchaToken] = useState(null);
  
  const handleCaptchaChange = (token) => {
    setCaptchaToken(token);
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!captchaToken) {
      alert("Vui lòng xác thực CAPTCHA");
      return;
    }
    
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email,
        password: password,
        captchaToken: captchaToken
      })
    });
    
    // Handle response...
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Email & Password inputs */}
      
      <ReCAPTCHA
        sitekey="YOUR_SITE_KEY_HERE"
        onChange={handleCaptchaChange}
      />
      
      <button type="submit">Đăng nhập</button>
    </form>
  );
}
```

## 📡 API Endpoint

### POST `/api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "captchaToken": "03AGdBq27..."
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "Nguyễn Văn A",
  "userName": "nguyenvana"
}
```

**Error Responses:**

- **400 Bad Request** - CAPTCHA không hợp lệ hoặc tài khoản chưa kích hoạt
```json
"Xác thực CAPTCHA không thành công. Vui lòng thử lại."
```

- **401 Unauthorized** - Email hoặc mật khẩu sai
```json
"Email hoặc mật khẩu không đúng."
```

## 🔐 Luồng xác thực

1. User nhập email, password và giải CAPTCHA
2. Frontend gửi request với `captchaToken` từ Google
3. Backend verify CAPTCHA với Google API
4. Nếu CAPTCHA hợp lệ, kiểm tra email trong database
5. Kiểm tra tài khoản đã được kích hoạt chưa
6. Verify password với BCrypt
7. Tạo Access Token và Refresh Token
8. Trả về tokens và thông tin user

## ⚙️ Cấu hình JWT

Trong `application.properties`:

```properties
# JWT Configuration
jwt.secret=YOUR_SECRET_KEY
jwt.access.expiration=3600000     # 1 hour
jwt.refresh.expiration=604800000  # 7 days
```

## 🧪 Test API với Postman/cURL

**Lưu ý**: Để test, bạn cần lấy CAPTCHA token thực từ frontend hoặc dùng test token.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "captchaToken": "03AGdBq27..."
  }'
```

## 🔒 Bảo mật

### Đã implement:
- ✅ CAPTCHA để chống bot
- ✅ BCrypt password hashing
- ✅ JWT tokens
- ✅ Validation input
- ✅ Ẩn thông tin chi tiết lỗi (không tiết lộ email có tồn tại hay không)

### Nên implement thêm:
- ⚠️ Rate limiting (giới hạn số lần đăng nhập)
- ⚠️ Account lockout sau N lần đăng nhập sai
- ⚠️ 2FA (Two-Factor Authentication)
- ⚠️ Logging failed login attempts
- ⚠️ IP blacklist

## 🐛 Troubleshooting

### Lỗi: "CAPTCHA verification failed"
- Kiểm tra Secret Key trong application.properties
- Kiểm tra Site Key trong frontend
- Đảm bảo domain đã được thêm vào Google reCAPTCHA Console

### Lỗi: "Cannot read property 'isSuccess' of null"
- Google API không phản hồi - kiểm tra kết nối internet
- Secret Key không đúng

### CAPTCHA không hiển thị ở frontend
- Kiểm tra Site Key
- Kiểm tra script Google đã load chưa
- Xem console log để tìm lỗi

## 📝 Notes

- CAPTCHA token chỉ dùng được 1 lần và có thời hạn ngắn
- Không nên lưu CAPTCHA token
- Trong môi trường dev, có thể tạm thời disable CAPTCHA bằng cách return `true` trong `CaptchaServiceImpl`
- Trong production, nên sử dụng environment variables cho tất cả các keys

## 🎯 Các bước tiếp theo

1. Test kỹ tính năng login
2. Implement Refresh Token endpoint
3. Implement Logout endpoint
4. Thêm JWT Filter để bảo vệ các endpoint khác
5. Implement rate limiting
6. Thêm logging và monitoring

