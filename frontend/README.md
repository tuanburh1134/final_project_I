# Personal Finance Frontend

Frontend React application cho hệ thống quản lý chi tiêu cá nhân.

## Tech Stack

- **React 19.2.0** - UI Library
- **Vite** - Build tool & Dev server
- **React Router 6** - Client-side routing
- **Bootstrap 5** - UI Framework
- **Axios** - HTTP client (sẽ thêm để gọi API)

## Cấu trúc Project

```
src/
├── components/
│   ├── common/           # Reusable components
│   │   ├── Header.jsx
│   │   ├── Footer.jsx
│   │   ├── Modal.jsx
│   │   ├── Notification.jsx
│   │   └── LoginSuccessModal.jsx
│   └── ProtectedRoute.jsx  # Route protection HOC
├── layouts/
│   └── AuthLayout.jsx    # Layout cho auth pages
├── pages/
│   ├── Auth/
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   └── ForgotPasswordPage.jsx
│   └── HomePage.jsx
├── styles/
│   ├── common.css        # Global styles
│   └── app.css
├── assets/               # Images & static files
├── App.jsx              # Main app với routes
└── main.jsx             # Entry point
```

## Setup & Installation

### 1. Cài đặt dependencies

```bash
cd frontend
npm install
```

### 2. Chạy development server

```bash
npm run dev
# hoặc
npm start
```

Frontend sẽ chạy tại: http://localhost:5173

### 3. Build production

```bash
npm run build
```

## Routes

- `/` - Trang chủ (Protected - yêu cầu đăng nhập)
- `/login` - Trang đăng nhập
- `/register` - Trang đăng ký
- `/forgot` - Quên mật khẩu
- `/*` - Redirect về `/login`

## Features Hiện Tại

✅ **UI/UX hoàn chỉnh:**
- Login form với validation
- Register form với validation phức tạp
- Forgot password form
- Responsive design với Bootstrap 5
- Background image layout
- Loading states
- Success/Error notifications
- Modal dialogs

✅ **Protected Routes:**
- Check token trong localStorage
- Auto redirect về /login nếu chưa đăng nhập

✅ **Components tái sử dụng:**
- Modal component
- Notification toast
- Header/Footer
- Auth layout

## TODO - Tích hợp Backend

Frontend hiện tại chỉ có UI, cần tích hợp với Backend API:

### Cần làm:

1. **Tạo API service layer** (`src/services/api.js`):
   - Axios instance với base URL
   - Request/response interceptors
   - Token management

2. **Tích hợp Login API**:
   - POST `/auth/login`
   - Lưu accessToken & refreshToken vào localStorage
   - Navigate to home page

3. **Tích hợp Register API**:
   - POST `/auth/register` với reCAPTCHA
   - Hiển thị form nhập mã xác thực email
   - POST `/auth/verify` với code

4. **Thêm Google OAuth**:
   - Button "Sign in with Google"
   - Redirect flow với OAuth2

5. **Token refresh logic**:
   - Auto refresh token khi hết hạn
   - Logout khi refresh token hết hạn

6. **Forgot Password flow**:
   - Send reset email
   - Reset password form

## Backend API Endpoints

Backend Spring Boot chạy tại: http://localhost:8080

### Auth Endpoints:
- `POST /auth/register` - Đăng ký (cần recaptchaToken)
- `POST /auth/verify` - Xác thực email với code
- `POST /auth/login` - Đăng nhập
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - Đăng xuất
- `POST /auth/change-password` - Đổi mật khẩu
- `GET /auth/google/success` - Google OAuth callback

## Environment Variables (TODO)

Tạo file `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_RECAPTCHA_SITE_KEY=your_site_key_here
```

## Ghi chú

- Code frontend được copy từ repository: https://github.com/VinhTri/LOGIN.git
- Backend đã sẵn sàng, chỉ cần tích hợp API calls
- Hiện tại login/register chỉ demo UI, chưa gọi API thật

