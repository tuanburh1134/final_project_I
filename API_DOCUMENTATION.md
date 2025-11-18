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
  "walletType": "PERSONAL",
  "memberEmails": ["friend1@example.com", "friend2@example.com"],
  "defaultMemberRole": "MEMBER"
}
```

**Lưu ý:**
- `memberEmails` (optional): Danh sách email của các thành viên muốn thêm khi tạo ví (chỉ áp dụng cho ví nhóm)
- `defaultMemberRole` (optional, mặc định "MEMBER"): Quyền mặc định cho thành viên được thêm. Hiện tại chỉ hỗ trợ "MEMBER"
- Thành viên được thêm sẽ có quyền xem ví, giao dịch, danh mục và tạo giao dịch

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
  "walletType": "GROUP",
  "memberEmails": ["friend1@example.com", "friend2@example.com"],
  "defaultMemberRole": "MEMBER"
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
- **Quyền sửa ví:**
  - Ví cá nhân (PERSONAL): Chỉ chủ sở hữu (người tạo ví) mới được sửa
  - Ví nhóm (GROUP): Chỉ chủ sở hữu (owner) mới được sửa, thành viên (member) không có quyền sửa
- `memberEmails` (optional): Danh sách email của các thành viên muốn thêm khi cập nhật ví
- `defaultMemberRole` (optional, mặc định "MEMBER"): Quyền mặc định cho thành viên được thêm

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

### 5. Xóa ví (Soft Delete)
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
- ⚠️ **Xóa mềm (Soft Delete):** Thay vì xóa thực sự, hệ thống đánh dấu ví là đã xóa bằng cách:
  - Cập nhật `is_deleted = true`
  - Cập nhật `deleted_at = thời gian hiện tại`
  - Dữ liệu vẫn tồn tại trong database nhưng không hiển thị trong các truy vấn thông thường
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

**Lưu ý:**
- Tự động bỏ ví mặc định cũ và đặt ví này làm ví mặc định
- Ví mặc định giúp ghi giao dịch nhanh hơn mà không phải chọn lại ví mỗi lần
- Có thể lấy ví mặc định bằng cách lọc từ danh sách ví (tìm ví có `isDefault = true`)

---

### 6.1. Lấy ví mặc định
**Helper function:** Sử dụng `walletAPI.getDefaultWallet()` trong frontend

**Mô tả:** Lấy ví mặc định hiện tại từ danh sách ví (tìm ví có `isDefault = true`)

**Response:**
```json
{
  "walletId": 1,
  "walletName": "Ví chính",
  "isDefault": true,
  ...
}
```

hoặc `null` nếu chưa có ví mặc định

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
  "targetCurrency": "VND",
  "transferDefaultFlag": true
}
```

**Mô tả:** Thực hiện gộp ví nguồn vào ví đích. Ví nguồn sẽ bị xóa sau khi gộp.

**Tham số `transferDefaultFlag`:**
- `true`: Nếu ví nguồn là ví mặc định, ví đích sẽ trở thành ví mặc định (Yes)
- `false`: Nếu ví nguồn là ví mặc định, hủy bỏ ví mặc định (No)
- `null` hoặc không gửi: Tự động chuyển ví mặc định sang ví đích (mặc định, giữ hành vi cũ)

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
- ⚠️ **Xử lý ví mặc định khi gộp:**
  - Nếu ví nguồn là ví mặc định, hệ thống sẽ hiển thị cảnh báo: "Khi gộp ví mặc định vào ví đích, ví đích sẽ trở thành ví mặc định. Bạn có đồng ý không?"
  - Người dùng có thể chọn:
    - **Yes** (`transferDefaultFlag = true`): Ví đích sẽ trở thành ví mặc định
    - **No** (`transferDefaultFlag = false`): Hủy bỏ ví mặc định (không có ví mặc định)
    - **Cancel**: Hủy bỏ thao tác gộp ví
- Lịch sử merge được lưu để audit trail

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

**Lưu ý:** 
- Không thể xóa danh mục hệ thống
- ⚠️ **Không thể xóa danh mục nếu đã có giao dịch sử dụng danh mục đó**
- Nếu cố gắng xóa danh mục có giao dịch, sẽ trả về lỗi: `"Không thể xóa danh mục này vì đã có X giao dịch sử dụng danh mục này. Vui lòng xóa hoặc chuyển các giao dịch trước."`

**Error Response:**
```json
{
  "error": "Không thể xóa danh mục này vì đã có 5 giao dịch sử dụng danh mục này. Vui lòng xóa hoặc chuyển các giao dịch trước."
}
```

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

**Lưu ý:**
- `imageUrl` (optional): URL của ảnh hóa đơn đính kèm
- Có thể upload ảnh trước bằng API `/files/upload`, sau đó sử dụng `fileUrl` từ response
- Hoặc sử dụng helper function `createExpenseWithImage` trong frontend để upload và tạo giao dịch trong một lần

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
    "wallet": {
      "walletId": 1,
      "balance": 950000.00
    }
  }
}
```

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

**Lưu ý:**
- `imageUrl` (optional): URL của ảnh hóa đơn đính kèm
- Có thể upload ảnh trước bằng API `/files/upload`, sau đó sử dụng `fileUrl` từ response
- Hoặc sử dụng helper function `createIncomeWithImage` trong frontend để upload và tạo giao dịch trong một lần

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

## 💰 Fund APIs

### 1. Lấy overview trang "Quỹ của bạn"
**GET** `/funds/overview`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Trả về dữ liệu chia theo 2 khối (Quỹ cá nhân/Quỹ nhóm) và 2 loại kỳ hạn (Có kỳ hạn/Không kỳ hạn), bao gồm tổng số quỹ, danh sách quỹ (tên, tiến độ, số thành viên, thời gian bắt đầu/kết thúc).

**Response:**
```json
{
  "personal": {
    "fixedTerm": {
      "description": "Các quỹ có mục tiêu và ngày kết thúc rõ ràng.",
      "total": 2,
      "funds": [
        {
          "fundId": 1,
          "fundName": "Quỹ du lịch Đà Lạt",
          "currentAmount": 10000000,
          "targetAmount": 30000000,
          "progressPercentage": 33.33,
          "startDate": "2025-01-01",
          "endDate": "2025-06-30"
        }
      ]
    },
    "flexibleTerm": {
      "description": "Quỹ tích lũy dài hạn, không xác định mục tiêu và ngày kết thúc.",
      "total": 1,
      "funds": [
        {
          "fundId": 3,
          "fundName": "Quỹ dự phòng",
          "currentAmount": 5000000,
          "startDate": "2025-01-01"
        }
      ]
    }
  },
  "group": {
    "fixedTerm": {
      "description": "Quỹ góp chung có mục tiêu và thời hạn.",
      "total": 1,
      "funds": [
        {
          "fundId": 2,
          "fundName": "Quỹ lớp 12A",
          "memberCount": 5,
          "currentAmount": 40000000,
          "targetAmount": 100000000,
          "progressPercentage": 40.00,
          "startDate": "2025-02-01",
          "endDate": "2025-12-31"
        }
      ]
    },
    "flexibleTerm": {
      "description": "Quỹ nhóm dùng lâu dài, không cố định mục tiêu tiền và thời hạn.",
      "total": 0,
      "funds": []
    }
  }
}
```

---

### 2. Lấy chi tiết quỹ
**GET** `/funds/{fundId}`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Trả về đầy đủ thông tin tổng quan, chi tiết (mục tiêu, tiến độ, lịch góp tiền, nhắc nhở, tự động nạp) và danh sách thành viên quỹ.

**Response:**
```json
{
  "fundId": 1,
  "fundName": "Quỹ du lịch Đà Lạt",
  "fundType": "PERSONAL",
  "termType": "FIXED_TERM",
  "currentAmount": 10000000,
  "targetAmount": 30000000,
  "progressPercentage": 33.33,
  "currencyCode": "VND",
  "walletId": 5,
  "startDate": "2025-01-01",
  "endDate": "2025-06-30",
  "frequency": "MONTHLY",
  "amountPerCycle": 5000000,
  "reminderType": "SYSTEM_SCHEDULE",
  "reminderTime": "08:00",
  "autoTopupType": "REMINDER_BASED",
  "autoTopupConfig": null,
  "notes": "Tiết kiệm đi du lịch cùng gia đình",
  "isClosed": false,
  "createdAt": "2025-01-01T10:00:00",
  "members": []
}
```

---

### 3. Tạo quỹ
**POST** `/funds`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "fundName": "Quỹ lớp 12A",
  "fundType": "GROUP",
  "termType": "FIXED_TERM",
  "walletId": 7,
  "targetAmount": 100000000,
  "startDate": "2025-02-01",
  "endDate": "2025-12-31",
  "frequency": "MONTHLY",
  "amountPerCycle": 8000000,
  "memberEmails": ["friend1@example.com", "friend2@example.com"],
  "reminderType": "SYSTEM_SCHEDULE",
  "reminderTime": "08:00",
  "autoTopupType": "REMINDER_BASED",
  "autoTopupConfig": null,
  "notes": "Quỹ góp chung lớp 12A"
}
```

**Lưu ý:**
- `fundType`: "PERSONAL" hoặc "GROUP"
- `termType`: "FIXED_TERM" (có kỳ hạn) hoặc "FLEXIBLE_TERM" (không kỳ hạn)
- `targetAmount`: Bắt buộc cho quỹ có kỳ hạn (FIXED_TERM), không cần cho quỹ không kỳ hạn (FLEXIBLE_TERM)
- `endDate`: Bắt buộc cho quỹ có kỳ hạn, không cần cho quỹ không kỳ hạn
- `walletId`: ID của ví được gắn với quỹ (ví này sẽ trở thành "ví quỹ", không dùng cho mục đích khác)
- `memberEmails`: Danh sách email thành viên (chỉ áp dụng cho quỹ nhóm)
- `reminderType`: "NONE", "SYSTEM_SCHEDULE", "CUSTOM"
- `autoTopupType`: "NONE", "REMINDER_BASED", "CUSTOM_SCHEDULE"

**Response:**
```json
{
  "message": "Tạo quỹ thành công",
  "fund": {
    "fundId": 1,
    "fundName": "Quỹ lớp 12A",
    ...
  }
}
```

---

### 4. Cập nhật quỹ
**PUT** `/funds/{fundId}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "fundName": "Quỹ lớp 12A (cập nhật)",
  "targetAmount": 120000000,
  "startDate": "2025-02-01",
  "endDate": "2025-12-31",
  "frequency": "WEEKLY",
  "amountPerCycle": 2000000,
  "reminderType": "CUSTOM",
  "reminderTime": "09:00",
  "autoTopupType": "CUSTOM_SCHEDULE",
  "autoTopupConfig": "{\"schedule\": \"MONTHLY\", \"time\": \"01-01\"}",
  "notes": "Ghi chú mới",
  "memberEmailsToAdd": ["newmember@example.com"],
  "memberIdsToRemove": [5]
}
```

**Lưu ý:**
- Chỉ có thể sửa các trường: tên quỹ, tần suất gửi, số tiền mỗi kỳ, ngày bắt đầu/kết thúc, ghi chú, nhắc nhở, tự động nạp, thành viên
- `memberEmailsToAdd`: Danh sách email thành viên muốn thêm (chỉ áp dụng cho quỹ nhóm)
- `memberIdsToRemove`: Danh sách ID thành viên muốn xóa (chỉ áp dụng cho quỹ nhóm, cần popup xác nhận)

**Response:**
```json
{
  "message": "Cập nhật quỹ thành công",
  "fund": { ... }
}
```

---

### 5. Đóng quỹ
**POST** `/funds/{fundId}/close`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Tạm dừng quỹ (không nhận góp tiền mới). Cần popup xác nhận: "Bạn có chắc chắn muốn tạm dừng quỹ không?"

**Response:**
```json
{
  "message": "Đóng quỹ thành công",
  "fund": {
    "fundId": 1,
    "isClosed": true,
    "closedAt": "2025-01-15T10:00:00"
  }
}
```

---

### 6. Xóa quỹ (Soft Delete)
**DELETE** `/funds/{fundId}`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Đánh dấu `is_deleted = true` cho quỹ và ví gắn kèm (dữ liệu vẫn giữ để khôi phục). Cần popup xác nhận: "Bạn có chắc chắn muốn xóa quỹ không?"

**Response:**
```json
{
  "message": "Xóa quỹ thành công"
}
```

---

### 7. Thêm thành viên vào quỹ nhóm
**POST** `/funds/{fundId}/members`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "email": "friend@example.com"
}
```

**Lưu ý:**
- Chỉ chủ quỹ có quyền thêm thành viên
- Thành viên được thêm sẽ có quyền "member" (xem, góp tiền, chỉnh sửa dữ liệu nhưng cần chủ quỹ xác nhận)

**Response:**
```json
{
  "message": "Thêm thành viên thành công",
  "member": {
    "memberId": 1,
    "userId": 2,
    "fullName": "Người bạn",
    "email": "friend@example.com",
    "role": "MEMBER",
    "status": "ACTIVE"
  }
}
```

---

### 8. Xóa thành viên khỏi quỹ nhóm
**DELETE** `/funds/{fundId}/members/{memberId}`

**Headers:** `Authorization: Bearer <token>`

**Mô tả:** Xóa thành viên khỏi quỹ nhóm. Chỉ chủ quỹ có quyền xóa thành viên. Cần popup xác nhận: "Bạn có chắc chắn muốn xóa thành viên không?"

**Response:**
```json
{
  "message": "Xóa thành viên thành công"
}
```

**Lưu ý:**
- Không có đổi quyền thành viên vì khi thành viên được thêm vào quỹ chỉ có duy nhất 1 quyền là "member"

---

## 📁 File Upload APIs

### 1. Upload ảnh hóa đơn
**POST** `/files/upload`

**Headers:** `Authorization: Bearer <token>`

**Request:**
- Content-Type: `multipart/form-data`
- Body: FormData với field `file` (File object)

**Lưu ý:**
- Chỉ chấp nhận file ảnh: JPEG, PNG, GIF, WEBP
- Kích thước tối đa: **50MB**
- File sẽ được lưu trong thư mục `uploads/` trên server

**Response:**
```json
{
  "success": true,
  "message": "Upload ảnh thành công",
  "fileUrl": "http://localhost:8080/files/uploads/user_1/invoice_1234567890.jpg"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "File phải là ảnh hợp lệ (JPEG, PNG, GIF, WEBP)"
}
```
hoặc
```json
{
  "success": false,
  "error": "Kích thước file không được vượt quá 50MB"
}
```

---

### 2. Xóa ảnh
**DELETE** `/files/delete?fileUrl={encoded_file_url}`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `fileUrl` (required): URL của file cần xóa (phải được encode bằng `encodeURIComponent`)

**Response:**
```json
{
  "success": true,
  "message": "Xóa ảnh thành công"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Không tìm thấy file hoặc không thể xóa"
}
```

**Ví dụ sử dụng:**
```javascript
// Upload ảnh
const fileInput = document.querySelector('input[type="file"]');
const file = fileInput.files[0];
const uploadResult = await fileAPI.uploadImage(file);
const imageUrl = uploadResult.fileUrl;

// Sử dụng imageUrl khi tạo giao dịch
await transactionAPI.createExpense(walletId, categoryId, amount, date, note, imageUrl);

// Hoặc sử dụng helper function để upload và tạo giao dịch trong một lần
await transactionAPI.createExpenseWithImage(walletId, categoryId, amount, date, note, file);
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

