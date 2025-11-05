import api from './api';

const authService = {
  /**
   * Đăng ký tài khoản mới
   * @param {Object} data - {fullName, email, password, confirmPassword, recaptchaToken}
   * @returns {Promise}
   */
  register: async (data) => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  /**
   * Xác thực email bằng mã 6 số
   * @param {Object} data - {email, code}
   * @returns {Promise}
   */
  verify: async (data) => {
    const response = await api.post('/auth/verify', data);
    return response.data;
  },

  /**
   * Đăng nhập
   * @param {Object} data - {email, password}
   * @returns {Promise}
   */
  login: async (data) => {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  /**
   * Đăng xuất (client-side: xóa token)
   * @returns {Promise}
   */
  logout: async () => {
    const response = await api.post('/auth/logout');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    return response.data;
  },

  /**
   * Refresh access token
   * @param {string} refreshToken
   * @returns {Promise}
   */
  refreshToken: async (refreshToken) => {
    const response = await api.post('/auth/refresh', { refreshToken });
    return response.data;
  },

  /**
   * Đổi mật khẩu
   * @param {Object} data - {email, oldPassword, newPassword}
   * @returns {Promise}
   */
  changePassword: async (data) => {
    const response = await api.post('/auth/change-password', data);
    return response.data;
  },

  /**
   * Quên mật khẩu - Gửi email reset (TODO: Backend cần implement endpoint này)
   * @param {Object} data - {email}
   * @returns {Promise}
   */
  forgotPassword: async (data) => {
    const response = await api.post('/auth/forgot-password', data);
    return response.data;
  },

  /**
   * Lưu thông tin đăng nhập vào localStorage
   * @param {Object} data - {accessToken, refreshToken, user}
   */
  saveAuthData: (data) => {
    if (data.accessToken) {
      localStorage.setItem('accessToken', data.accessToken);
    }
    if (data.refreshToken) {
      localStorage.setItem('refreshToken', data.refreshToken);
    }
    if (data.user) {
      localStorage.setItem('user', JSON.stringify(data.user));
    }
  },

  /**
   * Lấy thông tin user từ localStorage
   * @returns {Object|null}
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Kiểm tra user đã đăng nhập chưa
   * @returns {boolean}
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('accessToken');
  },
};

export default authService;

