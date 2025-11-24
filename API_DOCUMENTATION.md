# 📚 API Documentation - Personal Finance App

**Base URL:** `http://localhost:8080`

**Authentication:** Sử dụng JWT Bearer Token trong header
```
Authorization: Bearer <accessToken>
```

---

## 🔐 Authentication APIs

### 1. Đăng ký tài khoản
**POST** `/auth/register`

**Request Body:**
```json
{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "recaptchaToken": "token_from_recaptcha"
}
```

**Response:**
```json
{
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản."
}
```

**Lưu ý:**
- Mật khẩu phải ≥8 ký tự, có chữ hoa, thường, số, ký tự đặc biệt
- Email sẽ nhận mã xác minh 6 chữ số

---

### 2. Xác minh email
**POST** `/auth/verify`

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response:**
```json
{
  "message": "Xác minh thành công",
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here"
}
```

---

### 3. Đăng nhập
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response:**
```json
{
  "message": "Đăng nhập thành công",
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "userId": 1,
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "provider": "local",
    "avatar": null,
    "enabled": true
  }
}
```

---

### 4. Làm mới token
**POST** `/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

**Response:**
```json
{
  "accessToken": "new_jwt_token_here",
  "message": "Làm mới token thành công"
}
```

---

### 5. Quên mật khẩu
**POST** `/auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "Mã xác thực đã gửi đến email"
}
```

---

### 6. Xác thực OTP
**POST** `/auth/verify-otp`

**Request Body:**
```json
{
  "email": "user@example.com",
  "Mã xác thực": "123456"
}
```

**Response:**
```json
{
  "message": "Xác thực mã thành công"
}
```

---

### 7. Đặt lại mật khẩu
**POST** `/auth/reset-password`

**Request Body:**
```json
{
  "email": "user@example.com",
  "Mã xác thực": "123456",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Response:**
```json
{
  "message": "Đổi mật khẩu thành công"
}
```

---

### 8. Đăng nhập Google OAuth2
**GET** `/auth/oauth2/authorization/google`

Redirect đến Google login, sau đó redirect về:
`http://localhost:3000/oauth/callback?token=<jwt_token>`

---

## 👤 Profile APIs

### 1. Lấy thông tin profile
**GET** `/profile`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "user": {
    "userId": 1,
    "fullName": "Nguyễn Văn A",
    "email": "user@example.com",
    "provider": "local",
    "avatar": "base64_or_url",
    "enabled": true
  }
}
```

---

### 2. Cập nhật profile
**POST** `/profile/update`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "fullName": "Nguyễn Văn B",
  "avatar": "base64_string_or_url"
}
```

**Response:**
```json
{
  "message": "Cập nhật profile thành công",
  "user": { ... }
}
```

---

### 3. Đổi mật khẩu
**POST** `/profile/change-password`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Lưu ý:** Nếu user chưa có password (Google user), không cần `oldPassword`

**Response:**
```json
{
  "message": "Đổi mật khẩu thành công"
}
```

---

## 💰 Wallet APIs

### 1. Tạo ví mới
**POST** `/wallets/create`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "walletName": "Ví chính",
  "currencyCode": "VND",
  "initialBalance": 0.0,
  "description": "Ví mặc định",
  "setAsDefault": true,
  "walletType": "PERSONAL"
}
```

**Response:**
```json
{
  "message": "Tạo ví thành công",
  "wallet": {
    "walletId": 1,
    "walletName": "Ví chính",
    "currencyCode": "VND",
    "balance": 0.0,
    "description": "Ví mặc định",
    "isDefault": true,
    "walletType": "PERSONAL"
  }
}
```

---

### 2. Lấy danh sách ví
**GET** `/wallets`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "wallets": [
    {
      "walletId": 1,
      "walletName": "Ví chính",
      "walletType": "PERSONAL",
      "currencyCode": "VND",
      "balance": 1000000.00,
      "description": "Ví mặc định",
      "myRole": "OWNER",
      "ownerId": 1,
      "ownerName": "Nguyễn Văn A",
      "totalMembers": 1,
      "isDefault": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "total": 1
}
```

---

### 3. Lấy chi tiết ví
**GET** `/wallets/{walletId}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "wallet": {
    "walletId": 1,
    "walletName": "Ví chính",
    "currencyCode": "VND",
    "balance": 1000000.00,
    "description": "Ví mặc định",
    "isDefault": true,
    "walletType": "PERSONAL",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

---

### 4. Cập nhật ví
**PUT** `/wallets/{walletId}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "walletName": "Ví mới",
  "description": "Mô tả mới",
  "currencyCode": "VND",
  "balance": 0.0,
  "setAsDefault": false,
  "walletType": "GROUP"
}
```

**Lưu ý:**
- Chỉ có thể sửa balance nếu ví chưa có giao dịch
- **Ví mặc định (`setAsDefault`):**
  - `true`: Đặt ví này làm ví mặc định (tự động bỏ ví mặc định cũ)
  - `false`: Bỏ ví mặc định (nếu ví này đang là ví mặc định)
  - `null` hoặc không gửi: Không thay đổi trạng thái ví mặc định
- Có thể chuyển đổi loại ví: `PERSONAL` → `GROUP`
- **Không thể** chuyển từ `GROUP` → `PERSONAL` (sẽ báo lỗi)
- Khi chuyển `PERSONAL` → `GROUP`, hệ thống tự động đảm bảo owner được thêm vào WalletMember (nếu chưa có)

**Response:**
```json
{
  "message": "Cập nhật ví thành công",
  "wallet": {
    "walletId": 1,
    "walletName": "Ví mới",
    "walletType": "GROUP",
    "currencyCode": "VND",
    "balance": 0.0,
    "description": "Mô tả mới",
    "isDefault": false
  }
}
```

**Ví dụ chuyển đổi loại ví:**
```json
// Chuyển từ ví cá nhân sang ví nhóm
{
  "walletName": "Ví nhóm gia đình",
  "walletType": "GROUP"
}

// Lỗi: Không thể chuyển từ ví nhóm về ví cá nhân
{
  "walletType": "PERSONAL"
}
// Response: {
//   "error": "Không thể chuyển ví nhóm về ví cá nhân. Vui lòng xóa các thành viên trước."
// }
```

---

### 5. Xóa ví
**DELETE** `/wallets/{walletId}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "Xóa ví thành công",
  "deletedWallet": {
    "deletedWalletId": 1,
    "deletedWalletName": "Ví cũ",
    "balance": 0.0,
    "currencyCode": "VND",
    "wasDefault": false,
    "membersRemoved": 0,
    "transactionsDeleted": 0
  }
}
```

**Lưu ý:** 
- Không thể xóa ví có giao dịch hoặc ví mặc định
- Response bao gồm:
  - `wasDefault`: Ví có phải là ví mặc định không (luôn là `false` vì không thể xóa ví mặc định)
  - `membersRemoved`: Số thành viên đã bị xóa khỏi ví
  - `transactionsDeleted`: Số giao dịch đã bị xóa (luôn là `0` vì không thể xóa ví có giao dịch)

**Error Response:**
```json
{
  "error": "Không thể xóa ví. Bạn phải xóa các giao dịch trong ví này trước."
}
```
hoặc
```json
{
  "error": "Không thể xóa ví mặc định."
}
```
hoặc
```json
{
  "error": "Lỗi máy chủ nội bộ: ..."
}
```

---

### 6. Đặt ví mặc định
**PATCH** `/wallets/{walletId}/set-default`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "Đặt ví mặc định thành công"
}
```

---

### 7. Chia sẻ ví
**POST** `/wallets/{walletId}/share`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "email": "friend@example.com"
}
```

**Response:**
```json
{
  "message": "Chia sẻ ví thành công",
  "member": {
    "memberId": 2,
    "userId": 2,
    "fullName": "Người bạn",
    "email": "friend@example.com",
    "avatar": null,
    "role": "MEMBER",
    "joinedAt": "2024-01-01T10:00:00"
  }
}
```

---

### 8. Lấy danh sách thành viên ví
**GET** `/wallets/{walletId}/members`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "members": [
    {
      "memberId": 1,
      "userId": 1,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "avatar": null,
      "role": "OWNER",
      "joinedAt": "2024-01-01T10:00:00"
    }
  ],
  "total": 1
}
```

---

### 9. Xóa thành viên khỏi ví
**DELETE** `/wallets/{walletId}/members/{memberUserId}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "Xóa thành viên thành công"
}
```

---

### 10. Rời khỏi ví
**POST** `/wallets/{walletId}/leave`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "message": "Bạn đã rời khỏi ví thành công"
}
```

**Lưu ý:** Owner không thể rời ví

---

### 11. Kiểm tra quyền truy cập ví
**GET** `/wallets/{walletId}/access`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "hasAccess": true,
  "isOwner": true,
  "role": "OWNER"
}
```

---

### 12. Chuyển tiền giữa các ví
**POST** `/wallets/transfer`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "fromWalletId": 1,
  "toWalletId": 2,
  "amount": 100000.00,
  "note": "Chuyển tiền"
}
```

**Response:**
```json
{
  "message": "Chuyển tiền thành công",
  "transfer": {
    "transferId": 1,
    "amount": 100000.00,
    "currencyCode": "VND",
    "transferredAt": "2024-01-01T10:00:00",
    "note": "Chuyển tiền",
    "status": "COMPLETED",
    "fromWalletId": 1,
    "fromWalletName": "Ví nguồn",
    "fromWalletBalanceBefore": 1000000.00,
    "fromWalletBalanceAfter": 900000.00,
    "toWalletId": 2,
    "toWalletName": "Ví đích",
    "toWalletBalanceBefore": 0.00,
    "toWalletBalanceAfter": 100000.00
  }
}
```

---

### 13. Lấy danh sách ví đích để chuyển tiền
**GET** `/wallets/{walletId}/transfer-targets`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "sourceWallet": {
    "walletId": 1,
    "walletName": "Ví nguồn",
    "currencyCode": "VND",
    "balance": 1000000.00
  },
  "targetWallets": [
    {
      "walletId": 2,
      "walletName": "Ví đích",
      "currencyCode": "VND",
      "balance": 0.00
    }
  ],
  "total": 1
}
```

---

### 14. Lấy danh sách ví có thể gộp
**GET** `/wallets/{sourceWalletId}/merge-candidates`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Lấy danh sách tất cả ví mà user có thể gộp với ví nguồn. Chỉ trả về các ví mà user là owner.

**Response:**
```json
{
  "candidateWallets": [
    {
      "walletId": 2,
      "walletName": "Ví có thể gộp",
      "currencyCode": "VND",
      "balance": 500000.00,
      "transactionCount": 5,
      "isDefault": false,
      "canMerge": true,
      "reason": null
    }
  ],
  "ineligibleWallets": [],
  "total": 1
}
```

**Lưu ý:**
- Chỉ trả về các ví mà user là OWNER
- Không bao gồm chính ví nguồn
- Có thể gộp ví khác loại tiền tệ (sẽ tự động chuyển đổi)

---

### 15. Xem trước gộp ví
**GET** `/wallets/{targetWalletId}/merge-preview?sourceWalletId={sourceWalletId}&targetCurrency={currency}`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `sourceWalletId` (required): ID của ví nguồn sẽ bị xóa
- `targetCurrency` (required): Loại tiền tệ sau khi gộp (VD: "VND", "USD")

**Mô tả:** Xem trước kết quả trước khi thực hiện gộp ví. Hiển thị số dư, số giao dịch, và các cảnh báo.

**Response:**
```json
{
  "preview": {
    "sourceWalletId": 1,
    "sourceWalletName": "Ví nguồn",
    "sourceCurrency": "VND",
    "sourceBalance": 1000000.00,
    "sourceTransactionCount": 10,
    "sourceIsDefault": false,
    "targetWalletId": 2,
    "targetWalletName": "Ví đích",
    "targetCurrency": "USD",
    "targetBalance": 50.00,
    "targetTransactionCount": 5,
    "finalWalletName": "Ví đích",
    "finalCurrency": "USD",
    "finalBalance": 91.10,
    "totalTransactions": 15,
    "willTransferDefaultFlag": false,
    "canProceed": true,
    "warnings": [
      "Số dư sẽ được chuyển đổi sang USD"
    ]
  }
}
```

**Lưu ý:**
- Nếu ví nguồn và ví đích khác currency, số dư sẽ được chuyển đổi tự động
- Nếu ví nguồn là ví mặc định, flag sẽ được chuyển sang ví đích
- Tất cả transactions từ ví nguồn sẽ được chuyển sang ví đích
- Nếu transactions có currency khác, amount sẽ được chuyển đổi và lưu thông tin gốc

---

### 16. Gộp ví
**POST** `/wallets/{targetWalletId}/merge`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "sourceWalletId": 1,
  "targetCurrency": "VND"
}
```

**Mô tả:** Thực hiện gộp ví nguồn vào ví đích. Ví nguồn sẽ bị xóa sau khi gộp.

**Quy trình gộp ví:**
1. Kiểm tra quyền sở hữu cả 2 ví
2. Chuyển đổi số dư nếu khác currency
3. Chuyển tất cả transactions từ ví nguồn sang ví đích
4. Chuyển đổi amount của transactions nếu cần (lưu thông tin gốc)
5. Chuyển tất cả members từ ví nguồn sang ví đích (nếu chưa có)
6. Chuyển flag "default wallet" nếu ví nguồn là default
7. Xóa ví nguồn và các dữ liệu liên quan
8. Lưu lịch sử merge

**Response:**
```json
{
  "success": true,
  "message": "Gộp ví thành công",
  "result": {
    "success": true,
    "message": "Gộp ví thành công",
    "targetWalletId": 2,
    "targetWalletName": "Ví đích",
    "finalBalance": 1500000.00,
    "finalCurrency": "VND",
    "mergedTransactions": 10,
    "sourceWalletName": "Ví nguồn",
    "wasDefaultTransferred": false,
    "mergeHistoryId": 1,
    "mergedAt": "2024-01-01T10:00:00"
  }
}
```

**Lưu ý quan trọng:**
- ⚠️ **Ví nguồn sẽ bị XÓA** sau khi gộp thành công
- Chỉ có thể gộp ví mà bạn là OWNER của cả 2 ví
- Không thể gộp ví với chính nó
- Tất cả transactions sẽ được giữ nguyên, chỉ chuyển sang ví đích
- Nếu transactions có currency khác, amount sẽ được chuyển đổi và lưu:
  - `originalAmount`: Số tiền gốc
  - `originalCurrency`: Loại tiền gốc
  - `exchangeRate`: Tỷ giá đã áp dụng
- Tất cả members của ví nguồn sẽ được thêm vào ví đích (nếu chưa có)
- Nếu ví nguồn là ví mặc định, flag sẽ được chuyển sang ví đích
- Lịch sử merge được lưu để audit trail

---

## 📊 Budget APIs

### 1. Tạo ngân sách
**POST** `/budgets/create`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "categoryId": 3,
  "walletId": 1,
  "amountLimit": 5000000,
  "startDate": "2024-02-01",
  "endDate": "2024-02-29",
  "note": "Ăn uống tháng 2"
}
```

**Response:**
```json
{
  "message": "Tạo hạn mức chi tiêu thành công",
  "budget": {
    "budgetId": 1,
    "categoryId": 3,
    "walletId": 1,
    "amountLimit": 5000000,
    "startDate": "2024-02-01",
    "endDate": "2024-02-29"
  }
}
```

---

### 2. Lấy danh sách ngân sách + số tiền còn lại
**GET** `/budgets`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "budgets": [
    {
      "budgetId": 1,
      "categoryId": 3,
      "categoryName": "Ăn uống",
      "walletId": 1,
      "walletName": "Ví cá nhân",
      "appliesToAllWallets": false,
      "amountLimit": 5000000,
      "spentAmount": 1200000,
      "remainingAmount": 3800000,
      "progressPercentage": 24.00,
      "overLimit": false,
      "overBudgetAmount": 0,
      "hasExceededBudget": false,
      "budgetStatus": "ACTIVE",
      "warningTriggered": false,
      "overLimitAlertTriggered": false,
      "warningThresholdPercent": 20,
      "startDate": "2024-02-01",
      "endDate": "2024-02-29",
      "note": "Ăn uống tháng 2"
    }
  ],
  "total": 1
}
```

**Lưu ý:**
- `remainingAmount` có thể âm nếu đã chi quá ngân sách.
- `appliesToAllWallets = true` nghĩa là ngân sách áp dụng cho mọi ví của bạn.
- Khi `warningTriggered = true`, hệ thống đã gửi email cảnh báo (mặc định khi còn ≤20%). Khi `overLimitAlertTriggered = true`, bạn đã vượt hạn mức và đã nhận email thông báo.
- `budgetStatus` có thể là `ACTIVE`, `OVER_LIMIT`, `COMPLETED`. `overBudgetAmount` thể hiện số tiền vượt hạn mức; `hasExceededBudget = true` nghĩa là ngân sách này đang âm.

---

### 3. Lấy giao dịch của một ngân sách
**GET** `/budgets/{budgetId}/transactions`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "transactions": [
    {
      "transactionId": 12,
      "amount": 250000,
      "transactionDate": "2024-02-05T08:00:00",
      "note": "Ăn trưa",
      "category": { ... },
      "wallet": { ... }
    }
  ],
  "total": 1
}
```

**Nguyên tắc lọc:**
- Chỉ trả các giao dịch của người dùng hiện tại.
- Khớp đúng danh mục của ngân sách.
- Giới hạn trong khoảng `startDate` → `endDate` của ngân sách.
- Nếu ngân sách gắn một ví cụ thể, chỉ lấy giao dịch của ví đó; nếu để “Tất cả các ví” thì không lọc theo ví.
- Nếu hệ thống tự động gửi cảnh báo vượt/chuẩn bị vượt ngân sách, các trường `warningTriggered` và `overLimitAlertTriggered` trong `GET /budgets` sẽ phản ánh trạng thái hiện tại.

---

## 📄 Report APIs

### 1. Xuất báo cáo giao dịch (Excel/PDF)
**GET** `/reports/transactions/export?format=excel&startDate=2024-01-01&endDate=2024-01-31&walletId=1`

**Query params (tùy chọn):**
- `format`: `excel` (mặc định) hoặc `pdf`
- `startDate`, `endDate`: ngày lọc (ISO `yyyy-MM-dd`)
- `walletId`: chỉ xuất giao dịch của một ví cụ thể

**Headers:** `Authorization: Bearer <token>`

**Response:** File đính kèm (`Content-Disposition: attachment; filename=...`) dạng Excel hoặc PDF.

**Lưu ý:**
- Nếu không truyền `startDate` / `endDate` hệ thống xuất toàn bộ lịch sử giao dịch.
- Nếu không truyền `walletId` sẽ lấy tất cả ví mà bạn có quyền.

---

## 💰 Fund / Savings Goal APIs

Trang “Quỹ của bạn” được dựng thành các nhóm dữ liệu sau:

- **Quỹ cá nhân có kỳ hạn** – quỹ tiết kiệm có mục tiêu, ngày bắt đầu/kết thúc rõ ràng.
- **Quỹ cá nhân không kỳ hạn** – quỹ tích lũy dài hạn, không đặt mục tiêu cụ thể.
- **Quỹ nhóm có kỳ hạn** – quỹ góp chung với mục tiêu & thời hạn.
- **Quỹ nhóm không kỳ hạn** – quỹ nhóm dùng lâu dài, không mục tiêu cố định.

### 1. Lấy dashboard “Quỹ của bạn”
**GET** `/funds/dashboard`

```json
{
  "data": {
    "personalFixed": {
      "title": "Quỹ cá nhân có kỳ hạn",
      "description": "Các quỹ có mục tiêu và ngày kết thúc rõ ràng.",
      "total": 2,
      "funds": [
        {
          "fundId": 11,
          "fundName": "Quỹ mua xe",
          "fundType": "PERSONAL",
          "termType": "FIXED_TERM",
          "currentAmount": 35000000,
          "targetAmount": 80000000,
          "progressPercent": 43.75,
          "startDate": "2025-01-05",
          "endDate": "2025-12-31",
          "memberCount": 1,
          "currencyCode": "VND"
        }
      ]
    },
    "personalOpen": { "...": "..." },
    "groupFixed": { "...": "..." },
    "groupOpen": { "...": "..." }
  }
}
```

### 2. Tạo quỹ mới
**POST** `/funds`

| Trường | Kiểu | Bắt buộc | Ghi chú |
| --- | --- | --- | --- |
| `fundName` | string | ✅ | Tối đa 150 ký tự |
| `fundType` | enum | ✅ | `PERSONAL` / `GROUP` |
| `termType` | enum | ✅ | `FIXED_TERM` / `OPEN_TERM` |
| `walletId` | number | ✅ | Ví đích, phải chưa dùng cho ngân sách/quỹ khác |
| `targetAmount` | decimal | ✅ với quỹ có kỳ hạn | > 0 |
| `contributionFrequency` | enum | ✅ với quỹ có kỳ hạn | `DAILY/WEEKLY/MONTHLY/YEARLY` |
| `contributionAmount` | decimal | ✅ nếu muốn gợi ý tiến độ | >= 0 |
| `startDate` | date | ✅ | ≥ ngày hiện tại |
| `endDate` | date | ✅ với quỹ có kỳ hạn | > `startDate` |
| `reminderEnabled` + cấu hình nhắc | tùy chọn | Theo bảng dưới |
| `autoTopUpEnabled` + cấu hình auto nạp | tùy chọn | Theo bảng dưới |
| `members[]` | array | Bắt buộc với quỹ nhóm | Ít nhất 1 thành viên khác chủ quỹ |
| `note` | string | ❌ | Tối đa 2000 ký tự |

**Ví dụ – Quỹ nhóm có kỳ hạn:**
```json
{
  "fundName": "Quỹ du lịch Đà Lạt",
  "fundType": "GROUP",
  "termType": "FIXED_TERM",
  "walletId": 9,
  "targetAmount": 15000000,
  "contributionFrequency": "MONTHLY",
  "contributionAmount": 1500000,
  "startDate": "2025-03-01",
  "endDate": "2025-12-01",
  "note": "Đi chơi cuối năm",
  "reminderEnabled": true,
  "reminderType": "MONTHLY",
  "reminderTime": "08:00:00",
  "reminderDayOfMonth": 5,
  "autoTopUpEnabled": true,
  "autoTopUpMode": "CUSTOM_SCHEDULE",
  "autoTopUpScheduleType": "MONTHLY",
  "autoTopUpTime": "20:00:00",
  "autoTopUpDayOfMonth": 8,
  "autoTopUpSourceWalletId": 3,
  "autoTopUpAmount": 1500000,
  "members": [
    { "fullName": "Lan", "email": "lan@example.com", "role": "CONTRIBUTOR" }
  ]
}
```

**Validation chính:**

- Ví đích không được dùng song song cho ngân sách/quỹ khác. Nếu đang bị khóa → trả lỗi `"Ví này đang được sử dụng cho một quỹ tiết kiệm"`.
- Quỹ nhóm phải có ≥1 thành viên khác chủ quỹ. Không cho phép trùng email hoặc email chưa có tài khoản.
- Quỹ có kỳ hạn bắt buộc `targetAmount`, `contributionFrequency`, `startDate`, `endDate` và khoảng thời gian đủ dài cho tối thiểu 1 kỳ gửi (>=1 ngày/7 ngày/30 ngày/365 ngày tùy tần suất).
- Nếu bật nhắc nhở: phải chọn kiểu nhắc + cấu hình tương ứng (giờ/ ngày/ thứ).
- Nếu bật auto nạp:
  - Phải chọn chế độ (`REMINDER_LINKED` hoặc `CUSTOM_SCHEDULE`).
  - Chế độ `REMINDER_LINKED` chỉ khả dụng khi nhắc nhở đang bật.
  - Chế độ `CUSTOM_SCHEDULE` yêu cầu `autoTopUpScheduleType`, `time` và ngày/thứ phù hợp.
  - Phải chọn ví nguồn khác ví quỹ, ví nguồn cũng không được là ví của quỹ/ngân sách khác.

**Response:**
```json
{
  "message": "Tạo quỹ thành công",
  "fund": {
    "fundId": 21,
    "fundName": "Quỹ du lịch Đà Lạt",
    "fundType": "GROUP",
    "termType": "FIXED_TERM",
    "status": "ACTIVE",
    "walletId": 9,
    "walletName": "Ví Du lịch",
    "currencyCode": "VND",
    "currentAmount": 0,
    "targetAmount": 15000000,
    "progressPercent": 0,
    "startDate": "2025-03-01",
    "endDate": "2025-12-01",
    "note": "Đi chơi cuối năm",
    "contributionFrequency": "MONTHLY",
    "contributionAmount": 1500000,
    "reminderConfig": {
      "enabled": true,
      "reminderType": "MONTHLY",
      "reminderTime": "08:00:00",
      "reminderDayOfMonth": 5
    },
    "autoTopUpConfig": {
      "enabled": true,
      "mode": "CUSTOM_SCHEDULE",
      "scheduleType": "MONTHLY",
      "time": "20:00:00",
      "dayOfMonth": 8,
      "sourceWalletId": 3,
      "sourceWalletName": "Ví chính",
      "amount": 1500000
    },
    "members": [
      { "userId": 1, "fullName": "Chủ quỹ", "email": "owner@example.com", "role": "OWNER" },
      { "userId": 5, "fullName": "Lan", "email": "lan@example.com", "role": "CONTRIBUTOR" }
    ]
  }
}
```

### 3. Xem chi tiết quỹ
**GET** `/funds/{fundId}`

Hiển thị toàn bộ thông tin: loại quỹ, tiến độ, thông tin ví đích, mục tiêu, tần suất gửi, nhắc nhở, tự động nạp, danh sách thành viên (tên, email, quyền). API này được dùng cho:

- Trang chi tiết quỹ (Bước 4 trong luồng mô tả).
- Popup “Chỉnh sửa” / “Đóng quỹ” / “Xóa quỹ” (backend hiện tại mới hỗ trợ xem & tạo; các hành động nâng cao sẽ được bổ sung trong sprint sau).

---

## ☁️ Cloud Backup APIs

> Hệ thống tự động sao lưu dữ liệu của từng người dùng lên cloud lúc **02:00** mỗi ngày. File backup (JSON + GZIP) chứa ví, giao dịch, ngân sách, quỹ, lịch giao dịch và được lưu trong bucket S3 mà bạn cấu hình.

**Biến môi trường cần thiết**

| Biến | Ý nghĩa |
| --- | --- |
| `CLOUD_BACKUP_BUCKET` | Tên bucket chứa file backup (bắt buộc) |
| `CLOUD_BACKUP_PREFIX` | Tiền tố key (mặc định `backups`) |
| `AWS_REGION` | Region của bucket (mặc định `ap-southeast-1`) |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Credentials IAM có quyền `s3:PutObject` |

Nếu `CLOUD_BACKUP_BUCKET` chưa được cấu hình, API chủ động trả lỗi hướng dẫn thiết lập để tránh backup “ảo”.

### 1. Tạo sao lưu thủ công
**POST** `/backup/me`

Headers: `Authorization: Bearer <token>`

```json
{ "message": "Đã tạo bản sao lưu mới cho tài khoản của bạn." }
```

### 2. Xem trạng thái backup gần nhất
**GET** `/backup/status`

```json
{
  "lastBackupAt": "2025-02-21T02:01:03.512",
  "lastBackupLocation": "s3://finance-backups/backups/user-5/LTIwMjUtMDItMjFUMDI6MDE6MDNfZDVk.json.gz",
  "lastStatus": "SUCCESS",
  "lastError": null
}
```

**Luồng hoạt động**

1. `BackupScheduler` gọi `BackupService.backupAllUsers()` mỗi đêm.
2. `BackupService` gom dữ liệu từng user → JSON → nén GZIP → upload lên S3 qua `S3CloudStorageService`.
3. Bảng `user_backup_status` lưu `lastBackupAt`, `lastBackupLocation`, `lastStatus`, `lastError` để hiển thị ở `GET /backup/status`.
4. Người dùng vẫn có thể tự chạy nhanh bằng `POST /backup/me` trước khi đổi thiết bị.

---

## 📮 Feedback / Bug Report APIs

Cho phép người dùng gửi phản hồi/báo lỗi trực tiếp tới đội ngũ phát triển. Email nhận phản hồi cấu hình bằng biến môi trường `SUPPORT_FEEDBACK_EMAIL` (nếu bỏ trống, hệ thống dùng chính email liên hệ mà người dùng cung cấp).

### 1. Gửi phản hồi
**POST** `/feedback`

| Trường | Kiểu | Bắt buộc | Ghi chú |
| --- | --- | --- | --- |
| `feedbackType` | enum | ✅ | `BUG`, `FEATURE_REQUEST`, `IMPROVEMENT`, `OTHER` |
| `priority` | enum | ✅ | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` (mặc định MEDIUM) |
| `title` | string | ✅ | Tối đa 200 ký tự |
| `description` | string | ✅ | Tối đa 4000 ký tự |
| `module` | string | ❌ | Tên màn hình/chức năng gây lỗi |
| `platform` | string | ❌ | VD: `iOS`, `Android`, `Web` |
| `appVersion` | string | ❌ | Phiên bản app |
| `screenshotUrl` | string | ❌ | Link ảnh minh họa |
| `contactEmail` | string | ❌ | Nếu bỏ trống → dùng email tài khoản |

```json
{
  "feedbackType": "BUG",
  "priority": "HIGH",
  "title": "Không thể thêm ví mới",
  "description": "Nhấn Lưu thì bị quay vòng và không thông báo.",
  "module": "Ví cá nhân",
  "platform": "Android",
  "appVersion": "2.3.1",
  "screenshotUrl": "https://i.imgur.com/example.png",
  "contactEmail": "user@example.com"
}
```

**Response:**
```json
{
  "message": "Cảm ơn bạn! Phản hồi đã được ghi nhận.",
  "feedback": {
    "feedbackId": 42,
    "feedbackType": "BUG",
    "priority": "HIGH",
    "status": "NEW",
    "title": "Không thể thêm ví mới",
    "description": "...",
    "module": "Ví cá nhân",
    "platform": "Android",
    "appVersion": "2.3.1",
    "screenshotUrl": "https://i.imgur.com/example.png",
    "adminReply": null,
    "createdAt": "2025-02-21T10:11:12",
    "updatedAt": "2025-02-21T10:11:12"
  }
}
```

### 2. Xem các phản hồi đã gửi
**GET** `/feedback`

Trả về danh sách phản hồi của người dùng hiện tại (sắp xếp mới nhất trước) để họ theo dõi trạng thái xử lý (`NEW`, `IN_PROGRESS`, `RESOLVED`, `DISMISSED`).

---

## ⏰ Scheduled Transaction APIs

### 1. Tạo giao dịch đặt lịch
**POST** `/scheduled-transactions`

| Trường | Kiểu | Bắt buộc | Ghi chú |
| --- | --- | --- | --- |
| `walletId` | number | ✅ | Ví thực hiện giao dịch |
| `categoryId` | number | ✅ | Danh mục của giao dịch |
| `transactionTypeId` | number | ✅ | `Chi tiêu` hoặc `Thu nhập` |
| `amount` | decimal | ✅ | > 0 |
| `note` | string | ❌ | Ghi chú thêm |
| `scheduleType` | enum | ✅ | `ONE_TIME`, `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` |
| `scheduleTime` | datetime | ✅ | Lần thực hiện đầu tiên (ISO 8601) |
| `endDate` | date | 🔁 | Bắt buộc khi `scheduleType` ≠ `ONE_TIME` |

```json
{
  "walletId": 1,
  "categoryId": 3,
  "transactionTypeId": 1,
  "amount": 250000,
  "note": "Thanh toán tiền điện",
  "scheduleType": "MONTHLY",
  "scheduleTime": "2025-01-05T08:00:00",
  "endDate": "2025-12-31"
}
```

**Response:**
```json
{
  "message": "Tạo lịch giao dịch thành công",
  "schedule": {
    "scheduleId": 10,
    "walletId": 1,
    "walletName": "Ví chính",
    "categoryId": 3,
    "categoryName": "Hóa đơn",
    "transactionTypeId": 1,
    "transactionTypeName": "Chi tiêu",
    "amount": 250000,
    "note": "Thanh toán tiền điện",
    "scheduleType": "MONTHLY",
    "scheduleTime": "2025-01-05T08:00:00",
    "nextRunAt": "2025-01-05T08:00:00",
    "endDate": "2025-12-31",
    "status": "PENDING",
    "lastRunStatus": null,
    "totalSuccess": 0,
    "totalFailed": 0,
    "executedAt": null,
    "failureReason": null
  }
}
```

**Quy tắc:**
- Chi tiêu không bị kiểm tra số dư tại thời điểm tạo lịch; số dư được kiểm tra khi chạy thật.
- Lịch định kỳ yêu cầu `endDate` để tự động dừng.
- `note` được chép lại khi giao dịch thật được tạo.

---

### 2. Danh sách giao dịch đặt lịch
**GET** `/scheduled-transactions`

```json
{
  "schedules": [
    {
      "scheduleId": 10,
      "walletId": 1,
      "walletName": "Ví chính",
      "categoryId": 3,
      "categoryName": "Hóa đơn",
      "transactionTypeId": 1,
      "transactionTypeName": "Chi tiêu",
      "amount": 250000,
      "note": "Thanh toán tiền điện",
      "scheduleType": "MONTHLY",
      "scheduleTime": "2025-01-05T08:00:00",
      "nextRunAt": "2025-03-05T08:00:00",
      "endDate": "2025-12-31",
      "status": "PENDING",
      "lastRunStatus": "FAILED",
      "totalSuccess": 1,
      "totalFailed": 1,
      "executedAt": "2025-02-05T08:01:03",
      "failureReason": "Không đủ số dư để thực hiện giao dịch định kỳ."
    }
  ],
  "total": 1
}
```

**Trạng thái:**
- `PENDING`: chờ đến lần chạy kế tiếp.
- `PROCESSING`: đang chạy (ít khi thấy vì xử lý nhanh).
- `COMPLETED`: đã hoàn tất và không còn lần chạy nào nữa.
- `FAILED`: lần chạy gần nhất thất bại và đã hết chu kỳ.
- `CANCELLED`: người dùng hủy.

`lastRunStatus` thể hiện kết quả lần chạy gần nhất (thành công/ thất bại) ngay cả khi lịch vẫn quay về `PENDING`. `totalSuccess/totalFailed` giúp hiển thị “Số lần lặp đã hoàn thành”.

---

### 3. Lịch sử thực thi
**GET** `/scheduled-transactions/{scheduleId}/logs`

```json
{
  "logs": [
    {
      "logId": 15,
      "runAt": "2025-02-05T08:00:00",
      "status": "FAILED",
      "amount": 250000,
      "message": "Không đủ số dư để thực hiện giao dịch định kỳ."
    },
    {
      "logId": 14,
      "runAt": "2025-01-05T08:00:00",
      "status": "COMPLETED",
      "amount": 250000,
      "message": "Thực hiện giao dịch tự động thành công"
    }
  ],
  "total": 2
}
```

Hệ thống lưu lại từng lần thực thi (thành công hoặc thất bại) để người dùng theo dõi trong màn hình “Lịch giao dịch”.

---

### 4. Hủy lịch giao dịch
**DELETE** `/scheduled-transactions/{scheduleId}`

```json
{ "message": "Đã hủy lịch giao dịch" }
```

Không thể hủy khi lịch đã hoàn tất (`COMPLETED`) hoặc đã bị hủy trước đó.

---

**Luồng xử lý tự động:**
1. Scheduler chạy mỗi phút, lấy tối đa 50 lịch `PENDING` có `nextRunAt ≤ now`.
2. Nếu là thu nhập → tạo giao dịch thu nhập ngay và cộng tiền vào ví.
3. Nếu là chi tiêu → kiểm tra số dư. Nếu thiếu tiền:
   - Ghi log `FAILED`, tăng `totalFailed`, lưu `failureReason`.
   - Gửi email “Không đủ số dư để thực hiện giao dịch định kỳ”.
   - Nếu còn trong khoảng `endDate`, sinh lần chạy tiếp theo và trạng thái quay về `PENDING`.
4. Nếu thực thi thành công:
   - Ghi log `COMPLETED`, tăng `totalSuccess`.
   - Nếu lịch còn trong hiệu lực (`nextRunAt` mới ≤ `endDate`) → đặt `status = PENDING` để chờ lần kế tiếp.
   - Nếu hết hiệu lực → `status = COMPLETED`.

---

## 🔔 Daily Reminder APIs

### 1. Xem cấu hình nhắc nhở
**GET** `/reminders`

**Response:**
```json
{
  "reminderId": 1,
  "enabled": true,
  "reminderTime": "21:00:00",
  "sendEmail": true,
  "sendPush": false,
  "lastSentAt": "2024-02-04T21:00:01"
}
```

### 2. Cập nhật bật/tắt nhắc nhở
**POST** `/reminders`

**Request Body:**
```json
{
  "reminderTime": "20:30:00",
  "sendEmail": true,
  "sendPush": false,
  "enabled": true
}
```

**Response:**
```json
{
  "message": "Cập nhật nhắc nhở thành công",
  "reminder": {
    "reminderId": 1,
    "enabled": true,
    "reminderTime": "20:30:00",
    "sendEmail": true,
    "sendPush": false,
    "lastSentAt": null
  }
}
```

**Luồng hoạt động:**
- Người dùng chọn giờ nhắc nhở (mặc định 21:00) và kênh nhận (email, push).
- Scheduler chạy mỗi 5 phút, gửi nhắc nhở khi đến giờ và chưa gửi hôm đó.
- Email nội dung: “Đã đến giờ 20:30. Đừng quên ghi lại các giao dịch trong ngày…”.

---

## 📁 Category APIs

### 1. Tạo danh mục mới
**POST** `/categories/create`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "categoryName": "Ăn uống",
  "icon": "food",
  "transactionTypeId": 1
}
```

**Response:**
```json
{
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "icon": "food",
  "transactionType": {
    "typeId": 1,
    "typeName": "Chi tiêu"
  },
  "isSystem": false
}
```

---

### 2. Cập nhật danh mục
**PUT** `/categories/{id}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "categoryName": "Ăn uống mới",
  "icon": "restaurant"
}
```

**Response:**
```json
{
  "categoryId": 1,
  "categoryName": "Ăn uống mới",
  "icon": "restaurant"
}
```

---

### 3. Xóa danh mục
**DELETE** `/categories/{id}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```
"Danh mục đã được xóa thành công"
```

**Lưu ý:** Không thể xóa danh mục hệ thống

---

### 4. Lấy danh sách danh mục
**GET** `/categories`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
[
  {
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "icon": "food",
    "transactionType": {
      "typeId": 1,
      "typeName": "Chi tiêu"
    },
    "isSystem": true
  }
]
```

---

## 💸 Transaction APIs

### 1. Tạo giao dịch chi tiêu
**POST** `/transactions/expense`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "walletId": 1,
  "categoryId": 1,
  "amount": 50000.00,
  "transactionDate": "2024-01-01T10:00:00",
  "note": "Ăn trưa",
  "imageUrl": "optional_image_url"
}
```

**Response:**
```json
{
  "message": "Thêm chi tiêu thành công",
  "transaction": {
    "transactionId": 1,
    "amount": 50000.00,
    "transactionDate": "2024-01-01T10:00:00",
    "note": "Ăn trưa",
    "imageUrl": "optional_image_url",
    "overBudget": true,
    "overBudgetAmount": 500000,
    "wallet": {
      "walletId": 1,
      "balance": 950000.00
    }
  },
  "budgetAlert": "Ngân sách Ăn uống đã vượt hạn mức 500000",
  "budgetAlertLevel": "OVER_LIMIT",
  "overBudgetAmount": 500000
}
```

> Nếu chỉ còn ≤20% nhưng chưa vượt, `budgetAlertLevel = "WARNING"`, `overBudgetAmount = 0`, ví dụ: `"budgetAlert": "Ngân sách Ăn uống chỉ còn 500000 (<=20%)"`.

---

### 2. Tạo giao dịch thu nhập
**POST** `/transactions/income`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "walletId": 1,
  "categoryId": 5,
  "amount": 1000000.00,
  "transactionDate": "2024-01-01T10:00:00",
  "note": "Lương tháng 1",
  "imageUrl": null
}
```

**Response:**
```json
{
  "message": "Thêm thu nhập thành công",
  "transaction": {
    "transactionId": 2,
    "amount": 1000000.00,
    "transactionDate": "2024-01-01T10:00:00",
    "note": "Lương tháng 1",
    "wallet": {
      "walletId": 1,
      "balance": 1950000.00
    }
  }
}
```

---

## 📝 Lưu ý quan trọng

### Error Response Format
Tất cả API trả về lỗi theo format:
```json
{
  "error": "Thông báo lỗi"
}
```

### Status Codes
- `200 OK` - Thành công
- `400 Bad Request` - Dữ liệu không hợp lệ
- `401 Unauthorized` - Chưa đăng nhập hoặc token hết hạn
- `403 Forbidden` - Không có quyền truy cập
- `404 Not Found` - Không tìm thấy resource
- `500 Internal Server Error` - Lỗi server

### Currency Codes
Hỗ trợ các loại tiền tệ: `VND`, `USD`, `EUR`, `JPY`, `GBP`, `CNY`

### Transaction Types
- `1` - Chi tiêu
- `2` - Thu nhập

### Wallet Types
- `PERSONAL` - Ví cá nhân
- `GROUP` - Ví nhóm (chia sẻ)

### Wallet Roles
- `OWNER` - Chủ sở hữu
- `MEMBER` - Thành viên

---

## 🔧 Cấu hình CORS

Backend đã cấu hình CORS cho các origin:
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:3001`

---

## 📞 Liên hệ

Nếu có vấn đề với API, vui lòng kiểm tra:
1. Token có còn hạn không
2. Request body format đúng chưa
3. Headers có đầy đủ không
4. User có quyền truy cập resource không

