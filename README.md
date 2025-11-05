# 💰 Personal Finance App

Ứng dụng quản lý chi tiêu cá nhân với hệ thống authentication hoàn chỉnh.

## 🏗️ Kiến trúc

Monorepo gồm 2 phần:
- **Backend**: Spring Boot 3.3 + JWT + OAuth2 + MySQL
- **Frontend**: React 19 + Vite + React Router 6 + Bootstrap 5

## ✨ Tính năng (Sprint 1 - Authentication)

### ✅ Đã hoàn thành:
- 🔐 Đăng ký tài khoản với email verification (mã 6 số)
- 📧 Gửi email xác thực tự động
- 🔑 Đăng nhập với JWT (Access Token + Refresh Token)
- 🔄 Auto refresh token khi hết hạn
- 🚪 Đăng xuất
- 🛡️ Password strength validation
- 🤖 reCAPTCHA protection (dev-bypass mode)
- 🔒 Protected routes
- 📱 Responsive UI với Bootstrap 5

### ⏳ Đang phát triển:
- 🔑 Đổi mật khẩu
- 🔓 Quên mật khẩu / Reset password
- 🔵 Google OAuth login

## 🚀 Cách chạy

### Cách 1: Sử dụng script tự động (Windows)

```bash
start-all.bat
```

### Cách 2: Chạy manual

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```
→ Backend: http://localhost:8080

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm run dev
```
→ Frontend: http://localhost:5173

**Terminal 3 - Database:**
```bash
mysql -u root -p
CREATE DATABASE finance_db;
```

## 📋 Yêu cầu hệ thống

- **Java**: 21+
- **Maven**: 3.6+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Git**: 2.0+

## 🧪 Test Flow

### 1. Đăng ký tài khoản
1. Truy cập: http://localhost:5173/register
2. Nhập thông tin (password phải có: chữ hoa, chữ thường, số, ký tự đặc biệt, ≥8 ký tự)
3. Kiểm tra email để lấy mã xác thực
4. Nhập mã 6 số → Xác thực thành công

### 2. Đăng nhập
1. Truy cập: http://localhost:5173/login
2. Nhập email + password
3. Đăng nhập thành công → Chuyển về trang chủ

### 3. Trang chủ (Protected)
- Hiển thị thông tin user
- Nút đăng xuất

## 📚 Tài liệu

- [Backend API Documentation](backend/README.md)
- [Frontend Setup Guide](frontend/SETUP_GUIDE.md)
- [Integration Summary](INTEGRATION_SUMMARY.md)

## 🛠️ Tech Stack

### Backend
- Spring Boot 3.3.0
- Spring Security + JWT
- Spring Data JPA
- Spring Mail
- OAuth2 Client
- MySQL 8
- reCAPTCHA

### Frontend
- React 19.2
- Vite 5
- React Router 6
- Bootstrap 5
- Axios
- Bootstrap Icons

## 📁 Cấu trúc Project

```
final_project_I/
├── backend/                    # Spring Boot application
│   ├── src/main/java/
│   │   └── com/example/financeapp/
│   │       ├── config/        # Security, CORS, JWT config
│   │       ├── controller/    # REST API endpoints
│   │       ├── entity/        # JPA entities
│   │       ├── repository/    # Data access layer
│   │       ├── service/       # Business logic
│   │       └── security/      # Auth filters
│   └── pom.xml
│
├── frontend/                   # React + Vite application
│   ├── src/
│   │   ├── components/        # Reusable components
│   │   ├── pages/            # Page components
│   │   ├── layouts/          # Layout components
│   │   ├── services/         # API services
│   │   └── styles/           # CSS files
│   └── package.json
│
├── start-all.bat              # Script khởi động tự động
├── README.md                  # File này
└── INTEGRATION_SUMMARY.md     # Tổng kết tích hợp

```

## 🔐 Security Features

- ✅ JWT với Access Token (24h) + Refresh Token (7 days)
- ✅ Password hashing với BCrypt
- ✅ Email verification trước khi kích hoạt tài khoản
- ✅ reCAPTCHA để chống bot
- ✅ CORS protection
- ✅ Protected API endpoints
- ✅ Auto token refresh
- ⚠️ **TODO**: Move secrets to environment variables

## 🐛 Troubleshooting

### Backend không start được
```bash
# Kiểm tra MySQL đã chạy chưa
mysql -u root -p -e "SELECT 1"

# Kiểm tra port 8080 có bị chiếm không
netstat -ano | findstr :8080
```

### Frontend không gọi được API
```bash
# Kiểm tra CORS config trong backend
# File: backend/src/main/java/com/example/financeapp/config/CorsConfig.java
# Phải có: config.setAllowedOrigins(List.of("http://localhost:5173"));
```

### Email không gửi được
```bash
# Kiểm tra config trong application.properties
# spring.mail.username=your_email@gmail.com
# spring.mail.password=your_app_password (16 ký tự từ Google App Password)
```

## 📊 API Status

| Endpoint | Method | Status | Frontend Integration |
|----------|--------|--------|---------------------|
| `/auth/register` | POST | ✅ | ✅ |
| `/auth/verify` | POST | ✅ | ✅ |
| `/auth/login` | POST | ✅ | ✅ |
| `/auth/logout` | POST | ✅ | ✅ |
| `/auth/refresh` | POST | ✅ | ✅ |
| `/auth/change-password` | POST | ✅ | ⏳ |
| `/auth/forgot-password` | POST | ❌ | ⏳ |
| `/auth/google/success` | GET | ✅ | ⏳ |

## 👥 Contributors

- Backend: Spring Boot team
- Frontend: VinhTri/LOGIN (base template)
- Integration: AI Assistant

## 📝 License

Private project - All rights reserved

## 🎯 Roadmap

### Sprint 2 (Next)
- [ ] Dashboard với charts
- [ ] Quản lý thu/chi
- [ ] Phân loại giao dịch
- [ ] Báo cáo tài chính

### Sprint 3
- [ ] Budget planning
- [ ] Multi-currency support
- [ ] Export to Excel/PDF
- [ ] Mobile app (React Native)

---

**Status**: ✅ Sprint 1 Complete - Ready for Testing

**Last Updated**: 2025-11-05
