# Frontend React - Ví dụ tích hợp Login với CAPTCHA

## 📦 Cài đặt packages

```bash
cd frontend
npm install react-google-recaptcha
npm install axios  # nếu chưa có
```

## 🔧 Cấu hình

### 1. Tạo file `.env` trong thư mục frontend

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_RECAPTCHA_SITE_KEY=YOUR_SITE_KEY_HERE
```

### 2. Cập nhật `vite.config.js` (nếu chưa có)

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173
  }
})
```

## 📝 Code mẫu

### 1. Tạo `src/services/authApi.js` (cập nhật)

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const authApi = {
  // Đăng ký
  register: async (data) => {
    const response = await axios.post(`${API_BASE_URL}/api/auth/register`, data);
    return response.data;
  },

  // Đăng nhập với CAPTCHA
  login: async (email, password, captchaToken) => {
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
      email,
      password,
      captchaToken
    });
    return response.data;
  },

  // Xác thực tài khoản
  verifyAccount: async (token) => {
    const response = await axios.get(`${API_BASE_URL}/api/auth/verify-account`, {
      params: { token }
    });
    return response.data;
  },

  // Quên mật khẩu
  forgotPassword: async (email) => {
    const response = await axios.post(`${API_BASE_URL}/api/auth/forgot-password`, {
      email
    });
    return response.data;
  },

  // Đặt lại mật khẩu
  resetPassword: async (token, newPassword, confirmPassword) => {
    const response = await axios.post(`${API_BASE_URL}/api/auth/reset-password`, {
      token,
      newPassword,
      confirmPassword
    });
    return response.data;
  }
};

export default authApi;
```

### 2. Tạo context để quản lý auth state: `src/contexts/AuthContext.jsx`

```jsx
import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Kiểm tra localStorage khi app load
    const accessToken = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('user');
    
    if (accessToken && userData) {
      setUser(JSON.parse(userData));
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  const login = (userData, tokens) => {
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    localStorage.setItem('user', JSON.stringify(userData));
    
    setUser(userData);
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

### 3. Cập nhật `src/components/auth/LoginForm.jsx`

```jsx
import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import ReCAPTCHA from 'react-google-recaptcha';
import authApi from '../../services/authApi';
import { useAuth } from '../../contexts/AuthContext';

const LoginForm = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const recaptchaRef = useRef(null);

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [captchaToken, setCaptchaToken] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setError(''); // Clear error khi user typing
  };

  const handleCaptchaChange = (token) => {
    setCaptchaToken(token);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validation
    if (!formData.email || !formData.password) {
      setError('Vui lòng điền đầy đủ thông tin');
      return;
    }

    if (!captchaToken) {
      setError('Vui lòng xác thực CAPTCHA');
      return;
    }

    setLoading(true);

    try {
      const response = await authApi.login(
        formData.email,
        formData.password,
        captchaToken
      );

      // Lưu thông tin user và tokens
      const userData = {
        userId: response.userId,
        email: response.email,
        fullName: response.fullName,
        userName: response.userName
      };

      login(userData, {
        accessToken: response.accessToken,
        refreshToken: response.refreshToken
      });

      // Chuyển hướng đến dashboard
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err);
      
      if (err.response) {
        setError(err.response.data);
      } else {
        setError('Đăng nhập thất bại. Vui lòng thử lại.');
      }

      // Reset CAPTCHA
      if (recaptchaRef.current) {
        recaptchaRef.current.reset();
      }
      setCaptchaToken(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <h2>Đăng nhập</h2>
      
      <form onSubmit={handleSubmit}>
        {error && (
          <div className="error-message" style={{
            padding: '10px',
            backgroundColor: '#fee',
            color: '#c00',
            borderRadius: '4px',
            marginBottom: '15px'
          }}>
            {error}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="example@email.com"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Mật khẩu</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Nhập mật khẩu"
            required
          />
        </div>

        <div className="form-group">
          <ReCAPTCHA
            ref={recaptchaRef}
            sitekey={import.meta.env.VITE_RECAPTCHA_SITE_KEY}
            onChange={handleCaptchaChange}
          />
        </div>

        <button 
          type="submit" 
          disabled={loading || !captchaToken}
          className="btn-primary"
        >
          {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </button>

        <div className="form-links">
          <a href="/forgot-password">Quên mật khẩu?</a>
          <span> | </span>
          <a href="/register">Đăng ký tài khoản mới</a>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;
```

### 4. Cập nhật `src/main.jsx`

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import { AuthProvider } from './contexts/AuthContext'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>,
)
```

### 5. Tạo Protected Route: `src/components/ProtectedRoute.jsx`

```jsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
```

### 6. Cập nhật `src/router/routes.jsx`

```jsx
import { createBrowserRouter } from 'react-router-dom';
import AppLayout from '../layouts/AppLayout';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import DashboardPage from '../pages/DashboardPage';
import ProtectedRoute from '../components/ProtectedRoute';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      {
        path: 'login',
        element: <LoginPage />
      },
      {
        path: 'register',
        element: <RegisterPage />
      },
      {
        path: 'forgot-password',
        element: <ForgotPasswordPage />
      },
      {
        path: 'dashboard',
        element: (
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        )
      },
      {
        path: '/',
        element: <Navigate to="/login" replace />
      }
    ]
  }
]);

export default router;
```

### 7. Tạo Dashboard page mẫu: `src/pages/DashboardPage.jsx`

```jsx
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header>
        <h1>Dashboard</h1>
        <button onClick={handleLogout}>Đăng xuất</button>
      </header>

      <div className="user-info">
        <h2>Chào mừng, {user?.fullName}!</h2>
        <p>Email: {user?.email}</p>
        <p>Username: {user?.userName}</p>
      </div>

      <div className="dashboard-content">
        <p>Nội dung dashboard của bạn ở đây...</p>
      </div>
    </div>
  );
};

export default DashboardPage;
```

### 8. Thêm CSS cơ bản cho LoginForm: `src/components/auth/LoginForm.css`

```css
.login-form-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-form-container h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4CAF50;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.form-links {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.form-links a {
  color: #4CAF50;
  text-decoration: none;
}

.form-links a:hover {
  text-decoration: underline;
}

.error-message {
  animation: shake 0.3s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
```

## 🧪 Testing

### Test với console.log

Thêm vào `handleSubmit`:

```javascript
console.log('Captcha Token:', captchaToken);
console.log('Form Data:', formData);
console.log('Login Response:', response);
```

### Test CAPTCHA trong localhost

Google reCAPTCHA hoạt động trên localhost, nhưng đảm bảo:
1. Đã thêm `localhost` vào domains trong reCAPTCHA console
2. Site Key đúng trong `.env`

## 🚀 Chạy ứng dụng

```bash
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm run dev
```

Truy cập: http://localhost:5173/login

## 📝 Checklist

- [ ] Cài đặt `react-google-recaptcha`
- [ ] Tạo file `.env` với CAPTCHA Site Key
- [ ] Cập nhật `authApi.js`
- [ ] Tạo `AuthContext.jsx`
- [ ] Cập nhật `LoginForm.jsx` với CAPTCHA
- [ ] Tạo `ProtectedRoute.jsx`
- [ ] Cập nhật routes
- [ ] Test đăng nhập thành công
- [ ] Test các trường hợp lỗi (sai password, chưa kích hoạt, không giải CAPTCHA)

## 🎯 Tips

1. **Development mode**: Có thể tạm comment logic CAPTCHA để test nhanh
2. **Error handling**: Xử lý tất cả các loại lỗi từ backend
3. **Loading state**: Hiển thị loading khi đang gọi API
4. **Token refresh**: Implement interceptor để tự động refresh token khi hết hạn
5. **Persistent login**: Sử dụng refresh token để giữ login lâu dài

