/**
 * API Client cho Personal Finance App
 * Sử dụng trong React project
 */

const API_BASE_URL = 'http://localhost:8080';

/**
 * Helper function để gọi API
 */
async function apiCall(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('accessToken');
  
  const defaultHeaders = {
    'Content-Type': 'application/json',
  };
  
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }
  
  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };
  
  try {
    const response = await fetch(url, config);
    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.error || 'Có lỗi xảy ra');
    }
    
    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

/**
 * Helper function để upload file (FormData)
 */
async function apiCallFileUpload(endpoint, file, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('accessToken');
  
  const formData = new FormData();
  formData.append('file', file);
  
  const defaultHeaders = {};
  
  if (token) {
    defaultHeaders['Authorization'] = `Bearer ${token}`;
  }
  
  const config = {
    method: 'POST',
    body: formData,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };
  
  try {
    const response = await fetch(url, config);
    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.error || 'Có lỗi xảy ra');
    }
    
    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

// ==================== AUTHENTICATION APIs ====================

export const authAPI = {
  /**
   * Đăng ký tài khoản mới
   */
  register: async (fullName, email, password, confirmPassword, recaptchaToken) => {
    return apiCall('/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        fullName,
        email,
        password,
        confirmPassword,
        recaptchaToken,
      }),
    });
  },

  /**
   * Xác minh email với mã OTP
   */
  verify: async (email, code) => {
    return apiCall('/auth/verify', {
      method: 'POST',
      body: JSON.stringify({ email, code }),
    });
  },

  /**
   * Đăng nhập
   */
  login: async (email, password) => {
    return apiCall('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },

  /**
   * Làm mới access token
   */
  refreshToken: async (refreshToken) => {
    return apiCall('/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });
  },

  /**
   * Quên mật khẩu - Gửi OTP
   */
  forgotPassword: async (email) => {
    return apiCall('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  /**
   * Xác thực OTP
   */
  verifyOtp: async (email, otp) => {
    return apiCall('/auth/verify-otp', {
      method: 'POST',
      body: JSON.stringify({
        email,
        'Mã xác thực': otp,
      }),
    });
  },

  /**
   * Đặt lại mật khẩu
   */
  resetPassword: async (email, otp, newPassword, confirmPassword) => {
    return apiCall('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({
        email,
        'Mã xác thực': otp,
        newPassword,
        confirmPassword,
      }),
    });
  },

  /**
   * Đăng nhập Google OAuth2
   */
  googleLogin: () => {
    window.location.href = `${API_BASE_URL}/auth/oauth2/authorization/google`;
  },
};

// ==================== PROFILE APIs ====================

export const profileAPI = {
  /**
   * Lấy thông tin profile
   */
  getProfile: async () => {
    return apiCall('/profile');
  },

  /**
   * Cập nhật profile
   */
  updateProfile: async (fullName, avatar) => {
    return apiCall('/profile/update', {
      method: 'POST',
      body: JSON.stringify({ fullName, avatar }),
    });
  },

  /**
   * Đổi mật khẩu
   */
  changePassword: async (oldPassword, newPassword, confirmPassword) => {
    return apiCall('/profile/change-password', {
      method: 'POST',
      body: JSON.stringify({
        oldPassword,
        newPassword,
        confirmPassword,
      }),
    });
  },
};

// ==================== WALLET APIs ====================

export const walletAPI = {
  /**
   * Tạo ví mới
   * @param {string} walletName - Tên ví
   * @param {string} currencyCode - Mã tiền tệ (VD: "VND", "USD")
   * @param {string} description - Mô tả ví (optional)
   * @param {boolean} setAsDefault - Đặt làm ví mặc định (optional)
   * @param {string} walletType - Loại ví: "PERSONAL" hoặc "GROUP" (optional, mặc định "PERSONAL")
   * @param {Array<string>} memberEmails - Danh sách email của các thành viên muốn thêm (optional)
   * @param {string} defaultMemberRole - Quyền mặc định cho thành viên: "MEMBER" (optional, mặc định "MEMBER")
   * @returns {Promise<{message: string, wallet: object}>}
   */
  createWallet: async (walletName, currencyCode, description, setAsDefault, walletType, memberEmails, defaultMemberRole) => {
    return apiCall('/wallets/create', {
      method: 'POST',
      body: JSON.stringify({
        walletName,
        currencyCode,
        initialBalance: 0.0,
        description,
        setAsDefault,
        walletType: walletType || 'PERSONAL',
        memberEmails: memberEmails || null,
        defaultMemberRole: defaultMemberRole || 'MEMBER',
      }),
    });
  },

  /**
   * Lấy danh sách tất cả ví
   * @returns {Promise<{wallets: Array, total: number}>} Danh sách ví, mỗi ví có field isDefault
   */
  getAllWallets: async () => {
    return apiCall('/wallets');
  },

  /**
   * Lấy ví mặc định hiện tại
   * @returns {Promise<object|null>} Ví mặc định hoặc null nếu chưa có ví mặc định
   */
  getDefaultWallet: async () => {
    const { wallets } = await walletAPI.getAllWallets();
    const defaultWallet = wallets.find(wallet => wallet.isDefault === true);
    return defaultWallet || null;
  },

  /**
   * Lấy chi tiết ví
   */
  getWalletDetails: async (walletId) => {
    return apiCall(`/wallets/${walletId}`);
  },

  /**
   * Cập nhật ví (áp dụng cho cả ví cá nhân và ví nhóm)
   * Có thể cập nhật: tên, mô tả, số dư (nếu chưa có giao dịch), loại ví (PERSONAL/GROUP), trạng thái ví mặc định, thêm thành viên
   * 
   * ⚠️ LƯU Ý VỀ QUYỀN:
   * - Ví cá nhân (PERSONAL): Chỉ chủ sở hữu (người tạo ví) mới được sửa
   * - Ví nhóm (GROUP): Chỉ chủ sở hữu (owner) mới được sửa, thành viên (member) không có quyền sửa
   * 
   * @param {number} walletId - ID của ví
   * @param {string} walletName - Tên ví mới
   * @param {string} description - Mô tả ví (ghi chú)
   * @param {string} currencyCode - Mã tiền tệ (có thể cập nhật, sẽ tự động chuyển đổi số dư và giao dịch)
   * @param {number} balance - Số dư (chỉ có thể sửa nếu ví chưa có giao dịch)
   * @param {boolean|null} setAsDefault - Đặt làm ví mặc định: true = đặt làm mặc định, false = bỏ ví mặc định, null = không thay đổi
   * @param {string} walletType - Loại ví: "PERSONAL" hoặc "GROUP" (có thể chuyển PERSONAL -> GROUP, không thể GROUP -> PERSONAL)
   * @param {Array<string>} memberEmails - Danh sách email của các thành viên muốn thêm (optional)
   * @param {string} defaultMemberRole - Quyền mặc định cho thành viên: "MEMBER" (optional, mặc định "MEMBER")
   * @returns {Promise<{message: string, wallet: object}>}
   * @throws {Error} Nếu không có quyền sửa ví (chỉ owner mới được sửa)
   */
  updateWallet: async (walletId, walletName, description, currencyCode, balance, setAsDefault, walletType, memberEmails, defaultMemberRole) => {
    return apiCall(`/wallets/${walletId}`, {
      method: 'PUT',
      body: JSON.stringify({
        walletName,
        description,
        currencyCode,
        balance,
        setAsDefault,
        walletType,
        memberEmails: memberEmails || null,
        defaultMemberRole: defaultMemberRole || 'MEMBER',
      }),
    });
  },

  /**
   * Chuyển đổi ví cá nhân sang ví nhóm
   * Tự động thêm owner vào WalletMember và có thể thêm các thành viên khác
   * Thành viên được thêm sẽ có quyền MEMBER (có thể xem ví, giao dịch, danh mục và tạo giao dịch)
   * 
   * @param {number} walletId - ID của ví cần chuyển đổi
   * @param {Array<string>} memberEmails - Danh sách email của các thành viên muốn thêm khi chuyển đổi (optional)
   * @param {string} defaultMemberRole - Quyền mặc định cho thành viên: "MEMBER" (optional, mặc định "MEMBER")
   * @returns {Promise<{message: string, wallet: object}>}
   * @throws {Error} Nếu ví là ví mặc định hoặc không có quyền chuyển đổi
   */
  convertToGroupWallet: async (walletId, memberEmails, defaultMemberRole) => {
    // Lấy thông tin ví hiện tại để giữ nguyên các giá trị
    const walletDetails = await walletAPI.getWalletDetails(walletId);
    const currentWallet = walletDetails.wallet;
    
    return apiCall(`/wallets/${walletId}`, {
      method: 'PUT',
      body: JSON.stringify({
        walletName: currentWallet.walletName, // Giữ nguyên tên
        description: currentWallet.description || null, // Giữ nguyên mô tả
        currencyCode: currentWallet.currencyCode, // Giữ nguyên currency
        balance: null, // Không thay đổi balance
        setAsDefault: null, // Không thay đổi default
        walletType: 'GROUP', // Chuyển sang GROUP
        memberEmails: memberEmails || null, // Thêm thành viên nếu có
        defaultMemberRole: defaultMemberRole || 'MEMBER', // Quyền mặc định
      }),
    });
  },

  /**
   * Chuyển đổi ví nhóm về ví cá nhân
   * ⚠️ LƯU Ý: Không thể chuyển từ GROUP về PERSONAL. Sẽ báo lỗi.
   * @param {number} walletId - ID của ví cần chuyển đổi
   * @returns {Promise<{message: string, wallet: object}>}
   * @throws {Error} Nếu cố gắng chuyển GROUP -> PERSONAL
   */
  convertToPersonalWallet: async (walletId) => {
    return apiCall(`/wallets/${walletId}`, {
      method: 'PUT',
      body: JSON.stringify({
        walletType: 'PERSONAL',
      }),
    });
  },

  /**
   * Xóa ví
   * ⚠️ LƯU Ý: Không thể xóa ví có giao dịch hoặc ví mặc định
   * @param {number} walletId - ID của ví cần xóa
   * @returns {Promise<{message: string, deletedWallet: {deletedWalletId: number, deletedWalletName: string, balance: number, currencyCode: string, wasDefault: boolean, membersRemoved: number, transactionsDeleted: number}}>}
   * @throws {Error} Nếu ví có giao dịch hoặc là ví mặc định
   */
  deleteWallet: async (walletId) => {
    return apiCall(`/wallets/${walletId}`, {
      method: 'DELETE',
    });
  },

  /**
   * Đặt ví làm ví mặc định
   * Tự động bỏ ví mặc định cũ và đặt ví này làm ví mặc định
   * Ví mặc định giúp ghi giao dịch nhanh hơn mà không phải chọn lại ví mỗi lần
   * @param {number} walletId - ID của ví cần đặt làm mặc định
   * @returns {Promise<{message: string}>}
   */
  setDefaultWallet: async (walletId) => {
    return apiCall(`/wallets/${walletId}/set-default`, {
      method: 'PATCH',
    });
  },

  /**
   * Bỏ ví mặc định
   * Sử dụng updateWallet với setAsDefault = false để bỏ ví mặc định
   * @param {number} walletId - ID của ví cần bỏ mặc định
   * @returns {Promise<{message: string, wallet: object}>}
   */
  unsetDefaultWallet: async (walletId) => {
    return apiCall(`/wallets/${walletId}`, {
      method: 'PUT',
      body: JSON.stringify({
        setAsDefault: false,
      }),
    });
  },

  /**
   * Chia sẻ ví
   */
  shareWallet: async (walletId, email) => {
    return apiCall(`/wallets/${walletId}/share`, {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  /**
   * Lấy danh sách thành viên ví
   */
  getWalletMembers: async (walletId) => {
    return apiCall(`/wallets/${walletId}/members`);
  },

  /**
   * Xóa thành viên khỏi ví
   */
  removeMember: async (walletId, memberUserId) => {
    return apiCall(`/wallets/${walletId}/members/${memberUserId}`, {
      method: 'DELETE',
    });
  },

  /**
   * Rời khỏi ví
   */
  leaveWallet: async (walletId) => {
    return apiCall(`/wallets/${walletId}/leave`, {
      method: 'POST',
    });
  },

  /**
   * Kiểm tra quyền truy cập ví
   */
  checkAccess: async (walletId) => {
    return apiCall(`/wallets/${walletId}/access`);
  },

  /**
   * Chuyển tiền giữa các ví
   */
  transferMoney: async (fromWalletId, toWalletId, amount, note) => {
    return apiCall('/wallets/transfer', {
      method: 'POST',
      body: JSON.stringify({
        fromWalletId,
        toWalletId,
        amount,
        note,
      }),
    });
  },

  /**
   * Lấy danh sách ví đích để chuyển tiền
   */
  getTransferTargets: async (walletId) => {
    return apiCall(`/wallets/${walletId}/transfer-targets`);
  },

  /**
   * Lấy danh sách ví có thể gộp
   * Chỉ trả về các ví mà user là OWNER
   * @param {number} sourceWalletId - ID của ví nguồn
   * @returns {Promise<{candidateWallets: Array, ineligibleWallets: Array, total: number}>}
   */
  getMergeCandidates: async (sourceWalletId) => {
    return apiCall(`/wallets/${sourceWalletId}/merge-candidates`);
  },

  /**
   * Xem trước kết quả gộp ví
   * Hiển thị số dư, số giao dịch, và các cảnh báo trước khi gộp
   * @param {number} targetWalletId - ID của ví đích (ví sẽ giữ lại)
   * @param {number} sourceWalletId - ID của ví nguồn (ví sẽ bị xóa)
   * @param {string} targetCurrency - Loại tiền tệ sau khi gộp (VD: "VND", "USD")
   * @returns {Promise<{preview: object}>}
   */
  previewMerge: async (targetWalletId, sourceWalletId, targetCurrency) => {
    return apiCall(
      `/wallets/${targetWalletId}/merge-preview?sourceWalletId=${sourceWalletId}&targetCurrency=${targetCurrency}`
    );
  },

  /**
   * Gộp ví nguồn vào ví đích
   * 
   * ⚠️ LƯU Ý: Ví nguồn sẽ BỊ XÓA sau khi gộp thành công!
   * 
   * Quy trình:
   * 1. Chuyển đổi số dư nếu khác currency
   * 2. Chuyển tất cả transactions từ ví nguồn sang ví đích
   * 3. Chuyển đổi amount của transactions nếu cần (lưu thông tin gốc)
   * 4. Chuyển tất cả members từ ví nguồn sang ví đích
   * 5. Xử lý flag "default wallet" theo lựa chọn của người dùng (nếu ví nguồn là default)
   * 6. Xóa ví nguồn và lưu lịch sử merge
   * 
   * ⚠️ LƯU Ý VỀ VÍ MẶC ĐỊNH:
   * - Nếu ví nguồn là ví mặc định, người dùng có thể chọn:
   *   + transferDefaultFlag = true: Ví đích sẽ trở thành ví mặc định (Yes)
   *   + transferDefaultFlag = false: Hủy bỏ ví mặc định, không có ví mặc định (No)
   *   + transferDefaultFlag = null: Tự động chuyển sang ví đích (mặc định, giữ hành vi cũ)
   * 
   * @param {number} targetWalletId - ID của ví đích (ví sẽ giữ lại)
   * @param {number} sourceWalletId - ID của ví nguồn (ví sẽ bị xóa)
   * @param {string} targetCurrency - Loại tiền tệ sau khi gộp (VD: "VND", "USD")
   * @param {boolean|null} transferDefaultFlag - Có chuyển ví mặc định sang ví đích không: true = chuyển (Yes), false = hủy bỏ (No), null = tự động chuyển (mặc định)
   * @returns {Promise<{success: boolean, message: string, result: object}>}
   */
  mergeWallets: async (targetWalletId, sourceWalletId, targetCurrency, transferDefaultFlag) => {
    return apiCall(`/wallets/${targetWalletId}/merge`, {
      method: 'POST',
      body: JSON.stringify({
        sourceWalletId,
        targetCurrency,
        transferDefaultFlag: transferDefaultFlag !== undefined ? transferDefaultFlag : null,
      }),
    });
  },
};

// ==================== CATEGORY APIs ====================

export const categoryAPI = {
  /**
   * Tạo danh mục mới thuộc thu/chi
   * @param {string} categoryName - Tên danh mục (VD: "Ăn uống", "Lương")
   * @param {number} transactionTypeId - ID loại giao dịch: 1 = Chi tiêu, 2 = Thu nhập
   * @param {string} description - Mô tả danh mục (optional)
   * @returns {Promise<Category>} Category object đã tạo
   */
  createCategory: async (categoryName, transactionTypeId, description) => {
    return apiCall('/categories/create', {
      method: 'POST',
      body: JSON.stringify({
        categoryName,
        transactionTypeId,
        description: description || null,
      }),
    });
  },

  /**
   * Cập nhật danh mục
   * @param {number} categoryId - ID của danh mục
   * @param {string} categoryName - Tên danh mục mới
   * @param {string} description - Mô tả danh mục mới (optional)
   * @returns {Promise<Category>} Category object đã cập nhật
   */
  updateCategory: async (categoryId, categoryName, description) => {
    return apiCall(`/categories/${categoryId}`, {
      method: 'PUT',
      body: JSON.stringify({
        categoryName,
        description: description || null,
      }),
    });
  },

  /**
   * Xóa danh mục
   * ⚠️ LƯU Ý: Không thể xóa danh mục nếu đã có giao dịch sử dụng danh mục đó
   * @param {number} categoryId - ID của danh mục cần xóa
   * @returns {Promise<string>} Thông báo xóa thành công
   * @throws {Error} Nếu danh mục không tồn tại, không có quyền, là danh mục hệ thống, hoặc đã có giao dịch
   */
  deleteCategory: async (categoryId) => {
    return apiCall(`/categories/${categoryId}`, {
      method: 'DELETE',
    });
  },

  /**
   * Lấy danh sách danh mục
   */
  getCategories: async () => {
    return apiCall('/categories');
  },
};

// ==================== FUND APIs ====================

export const fundAPI = {
  /**
   * Lấy overview trang "Quỹ của bạn"
   */
  getOverview: async () => {
    return apiCall('/funds/overview');
  },

  /**
   * Lấy chi tiết một quỹ
   */
  getFundDetail: async (fundId) => {
    return apiCall(`/funds/${fundId}`);
  },

  /**
   * Tạo quỹ (cá nhân hoặc nhóm)
   */
  createFund: async (payload) => {
    return apiCall('/funds', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Cập nhật quỹ (chỉ các trường được phép chỉnh sửa)
   * @param {number} fundId - ID của quỹ
   * @param {object} payload - Dữ liệu cập nhật:
   *   - fundName (optional): Tên quỹ
   *   - frequency (optional): Tần suất gửi quỹ
   *   - amountPerCycle (optional): Số tiền gửi mỗi kỳ
   *   - startDate (optional): Ngày bắt đầu (ISO date string)
   *   - endDate (optional): Ngày kết thúc (ISO date string, chỉ cho quỹ có kỳ hạn)
   *   - notes (optional): Ghi chú
   *   - reminderType (optional): Loại nhắc nhở ('NONE', 'SYSTEM_SCHEDULE', 'CUSTOM')
   *   - reminderTime (optional): Thời gian nhắc nhở (format: 'HH:mm')
   *   - autoTopupType (optional): Loại tự động nạp ('NONE', 'REMINDER_BASED', 'CUSTOM_SCHEDULE')
   *   - autoTopupConfig (optional): Cấu hình tự động nạp (JSON string)
   *   - memberEmailsToAdd (optional): Danh sách email thành viên muốn thêm (chỉ cho quỹ nhóm)
   *   - memberIdsToRemove (optional): Danh sách ID thành viên muốn xóa (chỉ cho quỹ nhóm)
   * @returns {Promise<{message: string, fundId: number}>}
   */
  updateFund: async (fundId, payload) => {
    return apiCall(`/funds/${fundId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  /**
   * Đóng quỹ (tạm dừng)
   */
  closeFund: async (fundId) => {
    return apiCall(`/funds/${fundId}/close`, {
      method: 'POST',
    });
  },

  /**
   * Xóa quỹ (soft delete)
   */
  deleteFund: async (fundId) => {
    return apiCall(`/funds/${fundId}`, {
      method: 'DELETE',
    });
  },

  /**
   * Thêm thành viên vào quỹ nhóm
   */
  addMember: async (fundId, email) => {
    return apiCall(`/funds/${fundId}/members`, {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  },

  /**
   * Xóa thành viên khỏi quỹ nhóm
   */
  removeMember: async (fundId, memberId) => {
    return apiCall(`/funds/${fundId}/members/${memberId}`, {
      method: 'DELETE',
    });
  },
};

// ==================== FILE APIs ====================

export const fileAPI = {
  /**
   * Upload ảnh hóa đơn
   * @param {File} file - File ảnh cần upload (JPEG, PNG, GIF, WEBP)
   * @returns {Promise<{success: boolean, message: string, fileUrl: string}>}
   */
  uploadImage: async (file) => {
    if (!file) {
      throw new Error('File không được để trống');
    }
    
    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('File phải là ảnh hợp lệ (JPEG, PNG, GIF, WEBP)');
    }
    
    // Validate file size (50MB)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      throw new Error('Kích thước file không được vượt quá 50MB');
    }
    
    return apiCallFileUpload('/files/upload', file);
  },

  /**
   * Xóa ảnh
   * @param {string} fileUrl - URL của file cần xóa
   * @returns {Promise<{success: boolean, message: string}>}
   */
  deleteImage: async (fileUrl) => {
    if (!fileUrl) {
      throw new Error('File URL không được để trống');
    }
    
    // Encode fileUrl để tránh lỗi với các ký tự đặc biệt
    const encodedUrl = encodeURIComponent(fileUrl);
    
    return apiCall(`/files/delete?fileUrl=${encodedUrl}`, {
      method: 'DELETE',
    });
  },
};

// ==================== TRANSACTION APIs ====================

export const transactionAPI = {
  /**
   * Tạo giao dịch chi tiêu
   * @param {number} walletId - ID của ví
   * @param {number} categoryId - ID của danh mục
   * @param {number} amount - Số tiền
   * @param {string|Date} transactionDate - Ngày giao dịch (ISO string hoặc Date object)
   * @param {string} note - Ghi chú (optional)
   * @param {string} imageUrl - URL của ảnh hóa đơn (optional, có thể upload trước bằng fileAPI.uploadImage)
   * @returns {Promise<{message: string, transaction: object}>}
   */
  createExpense: async (walletId, categoryId, amount, transactionDate, note, imageUrl) => {
    return apiCall('/transactions/expense', {
      method: 'POST',
      body: JSON.stringify({
        walletId,
        categoryId,
        amount,
        transactionDate,
        note,
        imageUrl,
      }),
    });
  },

  /**
   * Tạo giao dịch chi tiêu với upload ảnh tự động
   * Helper function để upload ảnh và tạo giao dịch trong một lần gọi
   * @param {number} walletId - ID của ví
   * @param {number} categoryId - ID của danh mục
   * @param {number} amount - Số tiền
   * @param {string|Date} transactionDate - Ngày giao dịch
   * @param {string} note - Ghi chú (optional)
   * @param {File} imageFile - File ảnh hóa đơn (optional)
   * @returns {Promise<{message: string, transaction: object}>}
   */
  createExpenseWithImage: async (walletId, categoryId, amount, transactionDate, note, imageFile) => {
    let imageUrl = null;
    
    // Upload ảnh nếu có
    if (imageFile) {
      try {
        const uploadResult = await fileAPI.uploadImage(imageFile);
        imageUrl = uploadResult.fileUrl;
      } catch (error) {
        throw new Error(`Lỗi khi upload ảnh: ${error.message}`);
      }
    }
    
    // Tạo giao dịch với imageUrl
    return transactionAPI.createExpense(walletId, categoryId, amount, transactionDate, note, imageUrl);
  },

  /**
   * Tạo giao dịch thu nhập
   * @param {number} walletId - ID của ví
   * @param {number} categoryId - ID của danh mục
   * @param {number} amount - Số tiền
   * @param {string|Date} transactionDate - Ngày giao dịch (ISO string hoặc Date object)
   * @param {string} note - Ghi chú (optional)
   * @param {string} imageUrl - URL của ảnh hóa đơn (optional, có thể upload trước bằng fileAPI.uploadImage)
   * @returns {Promise<{message: string, transaction: object}>}
   */
  createIncome: async (walletId, categoryId, amount, transactionDate, note, imageUrl) => {
    return apiCall('/transactions/income', {
      method: 'POST',
      body: JSON.stringify({
        walletId,
        categoryId,
        amount,
        transactionDate,
        note,
        imageUrl,
      }),
    });
  },

  /**
   * Tạo giao dịch thu nhập với upload ảnh tự động
   * Helper function để upload ảnh và tạo giao dịch trong một lần gọi
   * @param {number} walletId - ID của ví
   * @param {number} categoryId - ID của danh mục
   * @param {number} amount - Số tiền
   * @param {string|Date} transactionDate - Ngày giao dịch
   * @param {string} note - Ghi chú (optional)
   * @param {File} imageFile - File ảnh hóa đơn (optional)
   * @returns {Promise<{message: string, transaction: object}>}
   */
  createIncomeWithImage: async (walletId, categoryId, amount, transactionDate, note, imageFile) => {
    let imageUrl = null;
    
    // Upload ảnh nếu có
    if (imageFile) {
      try {
        const uploadResult = await fileAPI.uploadImage(imageFile);
        imageUrl = uploadResult.fileUrl;
      } catch (error) {
        throw new Error(`Lỗi khi upload ảnh: ${error.message}`);
      }
    }
    
    // Tạo giao dịch với imageUrl
    return transactionAPI.createIncome(walletId, categoryId, amount, transactionDate, note, imageUrl);
  },
};

// ==================== EXPORT ALL APIs ====================

export default {
  auth: authAPI,
  profile: profileAPI,
  wallet: walletAPI,
  fund: fundAPI,
  category: categoryAPI,
  transaction: transactionAPI,
  file: fileAPI,
};

/**
 * Ví dụ sử dụng:
 * 
 * import api from './api';
 * 
 * // ============ AUTHENTICATION ============
 * // Đăng nhập
 * const loginData = await api.auth.login('user@example.com', 'password');
 * localStorage.setItem('accessToken', loginData.accessToken);
 * localStorage.setItem('refreshToken', loginData.refreshToken);
 * 
 * // ============ FUND API ============
 * // Lấy overview trang quỹ
 * const fundsOverview = await api.fund.getOverview();
 * console.log(fundsOverview.personal.fixedTerm.total);
 * 
 * // Xem chi tiết quỹ
 * const fundDetail = await api.fund.getFundDetail(10);
 * console.log('Tên quỹ:', fundDetail.fundName);
 * console.log('Ví ID:', fundDetail.walletId); // ID của ví gắn với quỹ
 * 
 * // Tạo quỹ cá nhân có kỳ hạn
 * const personalFund = await api.fund.createFund({
 *   fundName: 'Quỹ du lịch Đà Lạt',
 *   fundType: 'PERSONAL',
 *   termType: 'FIXED_TERM',
 *   walletId: 5,
 *   targetAmount: 30000000,
 *   startDate: '2025-01-01',
 *   endDate: '2025-06-30',
 *   frequency: 'MONTHLY',
 *   amountPerCycle: 5000000,
 *   reminderType: 'SYSTEM_SCHEDULE',
 *   reminderTime: '08:00',
 *   autoTopupType: 'REMINDER_BASED',
 *   notes: 'Tiết kiệm đi du lịch cùng gia đình'
 * });
 * 
 * // Tạo quỹ nhóm và thêm thành viên
 * const groupFund = await api.fund.createFund({
 *   fundName: 'Quỹ lớp 12A',
 *   fundType: 'GROUP',
 *   termType: 'FIXED_TERM',
 *   walletId: 7,
 *   targetAmount: 100000000,
 *   startDate: '2025-02-01',
 *   endDate: '2025-12-31',
 *   frequency: 'MONTHLY',
 *   amountPerCycle: 8000000,
 *   memberEmails: ['friend1@example.com', 'friend2@example.com']
 * });
 * 
 * // Cập nhật quỹ (có thể quản lý thành viên cùng lúc)
 * const updatedFund = await api.fund.updateFund(1, {
 *   fundName: 'Quỹ du lịch Đà Lạt (cập nhật)',
 *   frequency: 'WEEKLY',
 *   amountPerCycle: 1000000,
 *   startDate: '2025-01-15',
 *   endDate: '2025-07-30',
 *   notes: 'Ghi chú mới',
 *   reminderType: 'CUSTOM',
 *   reminderTime: '09:00',
 *   autoTopupType: 'CUSTOM_SCHEDULE',
 *   autoTopupConfig: JSON.stringify({ schedule: 'MONTHLY', time: '01-01' }),
 *   memberEmailsToAdd: ['newmember@example.com'], // Chỉ cho quỹ nhóm
 *   memberIdsToRemove: [5] // Chỉ cho quỹ nhóm
 * });
 * 
 * // Đóng quỹ (tạm dừng)
 * await api.fund.closeFund(1);
 * 
 * // Xóa quỹ (soft delete)
 * await api.fund.deleteFund(1);
 * 
 * // Gộp quỹ mặc định vào quỹ đích với lựa chọn người dùng
 * // Cách 1: người dùng chọn Yes -> ví đích trở thành ví mặc định
 * await api.wallet.mergeWallets(2, 1, 'VND', true);
 * 
 * // Cách 2: người dùng chọn No -> hủy bỏ ví mặc định
 * await api.wallet.mergeWallets(2, 1, 'VND', false);
 * 
 * // Cách 3: không truyền tham số -> tự động chuyển như hành vi cũ
 * await api.wallet.mergeWallets(2, 1, 'VND');
 * 
 * // ============ WALLET ============
 * // Lấy danh sách ví
 * const { wallets } = await api.wallet.getAllWallets();
 * 
 * // Tạo ví mới
 * const newWallet = await api.wallet.createWallet(
 *   'Ví mới',
 *   'VND',
 *   'Mô tả ví',
 *   true, // setAsDefault - đặt làm ví mặc định ngay khi tạo
 *   'PERSONAL', // walletType
 *   ['member1@example.com', 'member2@example.com'], // memberEmails (optional)
 *   'MEMBER' // defaultMemberRole (optional, mặc định "MEMBER")
 * );
 * 
 * // Chuyển đổi ví cá nhân sang ví nhóm
 * // Cách 1: Chuyển đổi không thêm thành viên
 * const groupWallet = await api.wallet.convertToGroupWallet(1); // walletId
 * 
 * // Cách 2: Chuyển đổi và thêm thành viên với quyền mặc định (MEMBER = view permission)
 * // Thành viên được thêm sẽ có quyền xem ví, giao dịch, danh mục và tạo giao dịch
 * const groupWalletWithMembers = await api.wallet.convertToGroupWallet(
 *   1, // walletId
 *   ['member1@example.com', 'member2@example.com'], // memberEmails - thêm thành viên khi chuyển đổi
 *   'MEMBER' // defaultMemberRole - quyền mặc định (MEMBER = có thể xem và tạo giao dịch)
 * );
 * 
 * // Chuyển đổi ví nhóm về ví cá nhân (sẽ báo lỗi)
 * try {
 *   const personalWallet = await api.wallet.convertToPersonalWallet(1); // walletId
 * } catch (error) {
 *   console.error('Lỗi:', error.message); // "Không thể chuyển ví nhóm về ví cá nhân..."
 * }
 * 
 * // Hoặc cập nhật loại ví thông qua updateWallet
 * const updated = await api.wallet.updateWallet(
 *   1, // walletId
 *   'Ví nhóm', // walletName
 *   'Mô tả', // description
 *   'VND', // currencyCode
 *   null, // balance
 *   false, // setAsDefault (false = bỏ ví mặc định, true = đặt làm mặc định, null = không thay đổi)
 *   'GROUP', // walletType
 *   ['member1@example.com', 'member2@example.com'], // memberEmails (optional) - thêm thành viên khi sửa ví
 *   'MEMBER' // defaultMemberRole (optional, mặc định "MEMBER")
 * );
 * 
 * // Cập nhật ví cá nhân
 * const updatedPersonal = await api.wallet.updateWallet(
 *   1, // walletId
 *   'Ví cá nhân mới', // walletName
 *   'Ghi chú về ví', // description (ghi chú)
 *   'VND', // currencyCode (có thể cập nhật, sẽ tự động chuyển đổi)
 *   null, // balance
 *   null, // setAsDefault (không thay đổi)
 *   null, // walletType (không thay đổi)
 *   null, // memberEmails (không thêm thành viên)
 *   null // defaultMemberRole
 * );
 * 
 * // Cập nhật ví nhóm (chỉ owner mới được sửa)
 * const updatedGroup = await api.wallet.updateWallet(
 *   2, // walletId (ví nhóm)
 *   'Ví nhóm mới', // walletName
 *   'Ghi chú về ví nhóm', // description (ghi chú)
 *   'USD', // currencyCode (có thể cập nhật, sẽ tự động chuyển đổi số dư và giao dịch)
 *   null, // balance
 *   null, // setAsDefault (không thay đổi)
 *   null, // walletType (không thay đổi)
 *   ['friend@example.com'], // memberEmails - thêm thành viên mới
 *   'MEMBER' // defaultMemberRole - quyền mặc định (MEMBER = có thể xem và tạo giao dịch)
 * );
 * 
 * // Đặt ví làm mặc định
 * await api.wallet.setDefaultWallet(1); // walletId
 * // Lưu ý: Tự động bỏ ví mặc định cũ và đặt ví này làm mặc định
 * 
 * // Lấy ví mặc định hiện tại
 * const defaultWallet = await api.wallet.getDefaultWallet();
 * if (defaultWallet) {
 *   console.log('Ví mặc định:', defaultWallet.walletName);
 *   // Sử dụng defaultWallet.walletId khi tạo giao dịch để ghi nhanh hơn
 * } else {
 *   console.log('Chưa có ví mặc định');
 * }
 * 
 * // Bỏ ví mặc định
 * await api.wallet.unsetDefaultWallet(1); // walletId
 * 
 * // Hoặc sử dụng updateWallet với setAsDefault = false
 * await api.wallet.updateWallet(1, null, null, null, null, false, null, null, null);
 * 
 * // ============ MERGE WALLET ============
 * // 1. Lấy danh sách ví có thể gộp
 * const { candidateWallets } = await api.wallet.getMergeCandidates(1); // sourceWalletId
 * 
 * // 2. Xem trước kết quả gộp ví
 * const preview = await api.wallet.previewMerge(
 *   2, // targetWalletId (ví đích)
 *   1, // sourceWalletId (ví nguồn - sẽ bị xóa)
 *   'VND' // targetCurrency
 * );
 * 
 * console.log('Số dư sau khi gộp:', preview.preview.finalBalance);
 * console.log('Cảnh báo:', preview.preview.warnings);
 * 
 * // 3. Thực hiện gộp ví
 * // ⚠️ LƯU Ý: Ví nguồn sẽ BỊ XÓA sau khi gộp thành công!
 * // 
 * // Khi gộp ví mặc định, hệ thống sẽ hiển thị cảnh báo:
 * // "Khi gộp ví mặc định vào ví đích, ví đích sẽ trở thành ví mặc định. Bạn có đồng ý không?"
 * // 
 * // Cách 1: Người dùng chọn Yes (transferDefaultFlag = true)
 * // Ví đích sẽ trở thành ví mặc định
 * const mergeResultYes = await api.wallet.mergeWallets(
 *   2, // targetWalletId (ví đích - giữ lại)
 *   1, // sourceWalletId (ví nguồn - là ví mặc định, sẽ bị xóa)
 *   'VND', // targetCurrency
 *   true // transferDefaultFlag = true (Yes) - Ví đích sẽ trở thành ví mặc định
 * );
 * 
 * // Cách 2: Người dùng chọn No (transferDefaultFlag = false)
 * // Hủy bỏ ví mặc định (không có ví mặc định)
 * const mergeResultNo = await api.wallet.mergeWallets(
 *   2, // targetWalletId
 *   1, // sourceWalletId (ví nguồn - là ví mặc định)
 *   'VND', // targetCurrency
 *   false // transferDefaultFlag = false (No) - Hủy bỏ ví mặc định
 * );
 * 
 * // Cách 3: Không truyền tham số (transferDefaultFlag = null)
 * // Tự động chuyển ví mặc định sang ví đích (mặc định, giữ hành vi cũ)
 * const mergeResultAuto = await api.wallet.mergeWallets(
 *   2, // targetWalletId
 *   1, // sourceWalletId
 *   'VND' // targetCurrency
 *   // transferDefaultFlag = null (mặc định) - Tự động chuyển
 * );
 * 
 * if (mergeResultAuto.success) {
 *   console.log('Gộp ví thành công!');
 *   console.log('Số dư cuối cùng:', mergeResultAuto.result.finalBalance);
 *   console.log('Số giao dịch đã gộp:', mergeResultAuto.result.mergedTransactions);
 * }
 * 
 * // ============ TRANSACTION ============
 * // Tạo giao dịch chi tiêu
 * const expense = await api.transaction.createExpense(
 *   1, // walletId
 *   1, // categoryId
 *   50000, // amount
 *   new Date().toISOString(), // transactionDate
 *   'Ăn trưa', // note
 *   null // imageUrl
 * );
 * 
 * // Tạo giao dịch thu nhập
 * const income = await api.transaction.createIncome(
 *   1, // walletId
 *   5, // categoryId
 *   1000000, // amount
 *   new Date().toISOString(), // transactionDate
 *   'Lương tháng 1', // note
 *   null // imageUrl
 * );
 * 
 * // ============ UPLOAD FILE ============
 * // Upload ảnh hóa đơn
 * const fileInput = document.querySelector('input[type="file"]');
 * const file = fileInput.files[0];
 * 
 * if (file) {
 *   const uploadResult = await api.file.uploadImage(file);
 *   console.log('Upload thành công:', uploadResult.fileUrl);
 *   
 *   // Sử dụng fileUrl để tạo giao dịch
 *   const incomeWithImage = await api.transaction.createIncome(
 *     1, // walletId
 *     5, // categoryId
 *     1000000, // amount
 *     new Date().toISOString(), // transactionDate
 *     'Lương tháng 1', // note
 *     uploadResult.fileUrl // imageUrl từ upload
 *   );
 * }
 * 
 * // Hoặc sử dụng helper function để upload và tạo giao dịch trong một lần
 * const incomeWithImageAuto = await api.transaction.createIncomeWithImage(
 *   1, // walletId
 *   5, // categoryId
 *   1000000, // amount
 *   new Date().toISOString(), // transactionDate
 *   'Lương tháng 1', // note
 *   file // imageFile (File object)
 * );
 * 
 * // Tạo giao dịch chi tiêu với ảnh hóa đơn
 * // Cách 1: Upload ảnh trước, sau đó tạo giao dịch
 * if (file) {
 *   const uploadResult = await api.file.uploadImage(file);
 *   
 *   const expenseWithImage = await api.transaction.createExpense(
 *     1, // walletId
 *     1, // categoryId
 *     50000, // amount
 *     new Date().toISOString(), // transactionDate
 *     'Ăn trưa', // note
 *     uploadResult.fileUrl // imageUrl từ upload
 *   );
 * }
 * 
 * // Cách 2: Sử dụng helper function để upload và tạo giao dịch chi tiêu trong một lần
 * const expenseWithImageAuto = await api.transaction.createExpenseWithImage(
 *   1, // walletId
 *   1, // categoryId
 *   50000, // amount
 *   new Date().toISOString(), // transactionDate
 *   'Ăn trưa', // note
 *   file // imageFile (File object)
 * );
 * 
 * // ============ TRANSFER MONEY ============
 * // Chuyển tiền giữa các ví
 * const transfer = await api.wallet.transferMoney(
 *   1, // fromWalletId
 *   2, // toWalletId
 *   100000, // amount
 *   'Chuyển tiền' // note
 * );
 * 
 * // ============ CATEGORY ============
 * // Lưu ý: transactionTypeId thường là:
 * // - 1 = Chi tiêu
 * // - 2 = Thu nhập
 * // (Có thể kiểm tra từ categories hiện có hoặc database)
 * 
 * // Tạo danh mục chi tiêu
 * const expenseCategory = await api.category.createCategory(
 *   'Quà tặng sinh nhật', // categoryName
 *   1, // transactionTypeId (1 = Chi tiêu)
 *   'Các khoản chi cho quà tặng sinh nhật' // description (optional)
 * );
 * 
 * // Tạo danh mục thu nhập
 * const incomeCategory = await api.category.createCategory(
 *   'Tiền thưởng', // categoryName
 *   2, // transactionTypeId (2 = Thu nhập)
 *   'Thưởng hiệu suất, thưởng dự án' // description (optional)
 * );
 * 
 * // Lấy danh sách danh mục
 * const categories = await api.category.getCategories();
 * 
 * // Cập nhật danh mục
 * const updated = await api.category.updateCategory(
 *   1, // categoryId
 *   'Tên mới', // categoryName
 *   'Mô tả mới' // description (optional)
 * );
 * 
 * // Xóa danh mục (chỉ xóa được nếu chưa có giao dịch sử dụng)
 * try {
 *   await api.category.deleteCategory(1); // categoryId
 * } catch (error) {
 *   console.error('Lỗi:', error.message); // "Không thể xóa danh mục này vì đã có X giao dịch..."
 * }
 */

