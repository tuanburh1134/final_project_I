# 🧪 HƯỚNG DẪN TEST TÍNH NĂNG UPLOAD ẢNH TRONG POSTMAN

## 📋 Chuẩn bị

### Bước 0: Khởi động Backend
```bash
cd backend
mvn spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### Bước 1: Chuẩn bị ảnh test
- Chuẩn bị 1-2 file ảnh (JPG, PNG) có kích thước < 5MB
- Đặt tên dễ nhớ: `invoice1.jpg`, `invoice2.png`

---

## 🔐 PHẦN 1: ĐĂNG KÝ VÀ ĐĂNG NHẬP

### Test 1.1: Đăng ký tài khoản mới

**Request:**
```
POST http://localhost:8080/auth/register
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "fullName": "Nguyễn Văn Test",
  "email": "test@example.com",
  "password": "Test@123456",
  "confirmPassword": "Test@123456",
  "recaptchaToken": "dev-bypass"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản."
}
```

---

### Test 1.2: Xác minh email

**Request:**
```
POST http://localhost:8080/auth/verify
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "test@example.com",
  "code": "123456"
}
```

**Lưu ý:** Lấy mã từ email hoặc check database:
```sql
SELECT verification_code FROM users WHERE email = 'test@example.com';
```

**Expected Response (200 OK):**
```json
{
  "message": "Xác minh thành công",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**📌 QUAN TRỌNG:** Copy `accessToken` để dùng cho các request tiếp theo!

---

### Test 1.3: Đăng nhập (nếu đã có tài khoản)

**Request:**
```
POST http://localhost:8080/auth/login
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "test@example.com",
  "password": "Test@123456"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Đăng nhập thành công",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 1,
    "fullName": "Nguyễn Văn Test",
    "email": "test@example.com",
    ...
  }
}
```

**📌 Copy `accessToken`!**

---

## 💰 PHẦN 2: TẠO VÍ (NẾU CHƯA CÓ)

### Test 2.1: Tạo ví mới

**Request:**
```
POST http://localhost:8080/wallets/create
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (raw JSON):**
```json
{
  "walletName": "Ví tiền mặt",
  "currencyCode": "VND",
  "initialBalance": 5000000,
  "description": "Ví chính dùng hàng ngày"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "Tạo ví thành công",
  "wallet": {
    "walletId": 1,
    "walletName": "Ví tiền mặt",
    "currencyCode": "VND",
    "balance": 5000000.00,
    "description": "Ví chính dùng hàng ngày",
    ...
  }
}
```

**📌 Lưu lại `walletId` = 1**

---

### Test 2.2: Xem danh sách ví

**Request:**
```
GET http://localhost:8080/wallets
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
[
  {
    "walletId": 1,
    "walletName": "Ví tiền mặt",
    "currencyCode": "VND",
    "balance": 5000000.00,
    ...
  }
]
```

---

## 📸 PHẦN 3: UPLOAD HÌNH ẢNH (TÍNH NĂNG MỚI!)

### Test 3.1: Upload 1 ảnh thành công ✅

**Request:**
```
POST http://localhost:8080/api/files/upload
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body:**
- Chọn `form-data` (KHÔNG phải raw!)
- Thêm field:
  - Key: `file` (QUAN TRỌNG: đặt type là `File`)
  - Value: Click "Select Files" → Chọn ảnh của bạn

**Screenshot Postman:**
```
Key        | Value              | Type
-----------|--------------------|------
file       | [Select Files]     | File
```

**Expected Response (200 OK):**
```json
{
  "message": "Upload file thành công",
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "fileUrl": "http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000.jpg",
  "fileSize": 245678,
  "fileType": "image/jpeg"
}
```

**📌 QUAN TRỌNG:** Copy `fileUrl` để dùng cho bước tiếp theo!

**✅ Verify:** Mở `fileUrl` trong browser, ảnh phải hiển thị!

---

### Test 3.2: Xem ảnh đã upload (Public) 👁️

**Request:**
```
GET http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000.jpg
```

**Headers:**
```
(Không cần Authorization!)
```

**Expected Response:**
- Status: 200 OK
- Body: Binary data của ảnh
- Hoặc mở trong browser để xem trực tiếp

---

### Test 3.3: Upload ảnh KHÔNG hợp lệ (File không phải ảnh) ❌

**Request:**
```
POST http://localhost:8080/api/files/upload
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (form-data):**
```
file: [Chọn file PDF hoặc TXT]
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WEBP)"
}
```

---

### Test 3.4: Upload ảnh quá lớn ❌

**Request:**
```
POST http://localhost:8080/api/files/upload
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (form-data):**
```
file: [Chọn file ảnh > 5MB]
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Kích thước file không được vượt quá 5MB"
}
```

---

### Test 3.5: Upload KHÔNG có token ❌

**Request:**
```
POST http://localhost:8080/api/files/upload
```

**Headers:**
```
(KHÔNG có Authorization!)
```

**Body (form-data):**
```
file: [Chọn ảnh]
```

**Expected Response (401 Unauthorized)**

---

### Test 3.6: Upload nhiều ảnh cùng lúc 📤

**Request:**
```
POST http://localhost:8080/api/files/upload-multiple
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (form-data):**
```
Key        | Value              | Type
-----------|--------------------|------
files      | [Select Files]     | File
files      | [Select Files]     | File
files      | [Select Files]     | File
```

**Lưu ý:** Key phải là `files` (có chữ 's'), chọn tối đa 5 ảnh

**Expected Response (200 OK):**
```json
{
  "message": "Upload 3 file thành công",
  "files": [
    {
      "filename": "550e8400-xxx-1.jpg",
      "fileUrl": "http://localhost:8080/api/files/550e8400-xxx-1.jpg"
    },
    {
      "filename": "550e8400-xxx-2.jpg",
      "fileUrl": "http://localhost:8080/api/files/550e8400-xxx-2.jpg"
    },
    {
      "filename": "550e8400-xxx-3.jpg",
      "fileUrl": "http://localhost:8080/api/files/550e8400-xxx-3.jpg"
    }
  ]
}
```

---

## 💸 PHẦN 4: TẠO GIAO DỊCH VỚI ẢNH

### Test 4.1: Tạo giao dịch CHI TIÊU với ảnh ✅

**Request:**
```
POST http://localhost:8080/transactions/expense
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (raw JSON):**
```json
{
  "amount": 150000,
  "transactionDate": "2025-11-10T14:30:00",
  "walletId": 1,
  "categoryId": 1,
  "note": "Mua hàng tại siêu thị Vinmart",
  "imageUrl": "http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

**Lưu ý:** 
- `imageUrl` là URL nhận được từ Test 3.1
- `walletId` là ID ví của bạn (lấy từ Test 2.2)
- `categoryId` = 1 (Ăn uống) hoặc categoryId khác

**Expected Response (200 OK):**
```json
{
  "message": "Thêm chi tiêu thành công",
  "transaction": {
    "transactionId": 1,
    "amount": 150000.00,
    "transactionDate": "2025-11-10T14:30:00",
    "note": "Mua hàng tại siêu thị Vinmart",
    "imageUrl": "http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000.jpg",
    "wallet": {
      "walletId": 1,
      "balance": 4850000.00
    },
    "category": {
      "categoryId": 1,
      "categoryName": "Ăn uống"
    },
    ...
  }
}
```

**✅ Verify:** 
1. Số dư ví giảm: 5,000,000 - 150,000 = 4,850,000
2. `imageUrl` được lưu trong transaction
3. Mở `imageUrl` trong browser để xem ảnh hóa đơn

---

### Test 4.2: Tạo giao dịch THU NHẬP với ảnh ✅

**Request:**
```
POST http://localhost:8080/transactions/income
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (raw JSON):**
```json
{
  "amount": 5000000,
  "transactionDate": "2025-11-10T15:00:00",
  "walletId": 1,
  "categoryId": 9,
  "note": "Nhận lương tháng 11",
  "imageUrl": "http://localhost:8080/api/files/660e8400-yyy.jpg"
}
```

**Lưu ý:** 
- `categoryId` = 9 (Lương) - Category của loại "Thu nhập"

**Expected Response (200 OK):**
```json
{
  "message": "Thêm thu nhập thành công",
  "transaction": {
    "transactionId": 2,
    "amount": 5000000.00,
    "imageUrl": "http://localhost:8080/api/files/660e8400-yyy.jpg",
    "wallet": {
      "balance": 9850000.00
    },
    ...
  }
}
```

---

### Test 4.3: Tạo giao dịch KHÔNG có ảnh (Optional) ✅

**Request:**
```
POST http://localhost:8080/transactions/expense
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Body (raw JSON):**
```json
{
  "amount": 50000,
  "transactionDate": "2025-11-10T16:00:00",
  "walletId": 1,
  "categoryId": 2,
  "note": "Xe bus"
}
```

**Lưu ý:** Không có field `imageUrl` → OK, ảnh là optional!

**Expected Response (200 OK):**
```json
{
  "message": "Thêm chi tiêu thành công",
  "transaction": {
    "transactionId": 3,
    "amount": 50000.00,
    "note": "Xe bus",
    "imageUrl": null,
    ...
  }
}
```

---

## 🗑️ PHẦN 5: XÓA ẢNH

### Test 5.1: Xóa ảnh ✅

**Request:**
```
DELETE http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000.jpg
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response (200 OK):**
```json
{
  "message": "Xóa file thành công"
}
```

**✅ Verify:** Mở lại URL ảnh → Phải trả về 404 Not Found

---

### Test 5.2: Xóa ảnh KHÔNG có token ❌

**Request:**
```
DELETE http://localhost:8080/api/files/550e8400-xxx.jpg
```

**Headers:**
```
(Không có Authorization)
```

**Expected Response (401 Unauthorized)**

---

## 📊 KIỂM TRA DATABASE

### Kiểm tra transaction có lưu imageUrl không:

```sql
SELECT 
    transaction_id,
    amount,
    note,
    image_url,
    transaction_date
FROM transactions
ORDER BY transaction_id DESC
LIMIT 5;
```

**Expected Result:**
```
transaction_id | amount    | note                        | image_url
---------------|-----------|-----------------------------|------------------------------------------
1              | 150000.00 | Mua hàng tại siêu thị...    | http://localhost:8080/api/files/550e...jpg
2              | 5000000.00| Nhận lương tháng 11         | http://localhost:8080/api/files/660e...jpg
3              | 50000.00  | Xe bus                      | NULL
```

---

## 🎯 POSTMAN COLLECTION

### Tạo Collection để test nhanh:

1. **Tạo Environment:**
   - Name: `Finance App Local`
   - Variables:
     ```
     base_url: http://localhost:8080
     access_token: (để trống, sẽ tự động set)
     image_url: (để trống, sẽ tự động set)
     ```

2. **Tạo Collection:** `Finance App - Image Upload`

3. **Thêm các request:**
   - Folder: `1. Authentication`
     - POST Login
     - POST Register
     - POST Verify
   
   - Folder: `2. Wallets`
     - POST Create Wallet
     - GET List Wallets
   
   - Folder: `3. File Upload` ⭐
     - POST Upload Single Image
     - POST Upload Multiple Images
     - GET View Image
     - DELETE Delete Image
   
   - Folder: `4. Transactions`
     - POST Create Expense (with image)
     - POST Create Income (with image)

4. **Auto-save token:** Trong request Login, thêm Test script:
   ```javascript
   if (pm.response.code === 200) {
       const response = pm.response.json();
       pm.environment.set("access_token", response.accessToken);
   }
   ```

5. **Auto-save imageUrl:** Trong request Upload, thêm Test script:
   ```javascript
   if (pm.response.code === 200) {
       const response = pm.response.json();
       pm.environment.set("image_url", response.fileUrl);
   }
   ```

6. **Sử dụng variables:** Trong các request, dùng:
   ```
   URL: {{base_url}}/api/files/upload
   Header: Authorization: Bearer {{access_token}}
   Body: "imageUrl": "{{image_url}}"
   ```

---

## ✅ CHECKLIST TEST HOÀN CHỈNH

### Authentication:
- [ ] Đăng ký tài khoản mới
- [ ] Xác minh email
- [ ] Đăng nhập và lấy token
- [ ] Token được lưu vào environment

### Wallet:
- [ ] Tạo ví mới
- [ ] Xem danh sách ví
- [ ] Lưu walletId

### Upload Image:
- [ ] Upload 1 ảnh JPG thành công
- [ ] Upload 1 ảnh PNG thành công
- [ ] Upload ảnh và lưu imageUrl vào environment
- [ ] Xem ảnh trong browser (public, không cần token)
- [ ] Upload file không phải ảnh → Bị từ chối
- [ ] Upload ảnh > 5MB → Bị từ chối
- [ ] Upload không có token → 401 Unauthorized
- [ ] Upload nhiều ảnh (3-5 ảnh) thành công
- [ ] Upload > 5 ảnh → Bị từ chối

### Create Transaction with Image:
- [ ] Tạo chi tiêu với imageUrl
- [ ] Tạo thu nhập với imageUrl
- [ ] Tạo giao dịch không có ảnh (imageUrl = null)
- [ ] Verify số dư ví thay đổi đúng
- [ ] Verify imageUrl lưu trong database

### Delete Image:
- [ ] Xóa ảnh với token
- [ ] Xóa ảnh không có token → 401
- [ ] Verify ảnh đã bị xóa (GET trả về 404)

---

## 🐛 TROUBLESHOOTING

### ❌ Lỗi: "401 Unauthorized"
**Nguyên nhân:** Token không hợp lệ hoặc thiếu  
**Giải pháp:** 
- Kiểm tra header `Authorization: Bearer <token>`
- Login lại để lấy token mới
- Kiểm tra token chưa hết hạn (24h)

### ❌ Lỗi: "Chỉ chấp nhận file ảnh"
**Nguyên nhân:** File không đúng định dạng  
**Giải pháp:** Chỉ upload JPG, PNG, GIF, WEBP

### ❌ Lỗi: "Kích thước file vượt quá"
**Nguyên nhân:** File > 5MB  
**Giải pháp:** Nén ảnh trước khi upload

### ❌ Lỗi: "Không tìm thấy ví"
**Nguyên nhân:** `walletId` không tồn tại hoặc không thuộc user  
**Giải pháp:** Kiểm tra lại walletId bằng GET /wallets

### ❌ Lỗi: "Danh mục không tồn tại"
**Nguyên nhân:** `categoryId` không hợp lệ  
**Giải pháp:** 
- Chi tiêu: categoryId từ 1-8
- Thu nhập: categoryId từ 9-13

### ❌ Postman không gửi được file
**Giải pháp:** 
1. Đảm bảo Body type là `form-data`
2. Key là `file` (upload single) hoặc `files` (upload multiple)
3. Chọn type là `File` (không phải `Text`)
4. Click "Select Files" để chọn ảnh

---

## 🎉 KẾT QUẢ MONG ĐỢI

Sau khi test xong, bạn sẽ có:

1. ✅ 1 tài khoản đã xác minh
2. ✅ 1 ví tiền với số dư
3. ✅ Ít nhất 2 ảnh đã upload thành công
4. ✅ Ít nhất 2 giao dịch có đính kèm ảnh
5. ✅ Có thể xem ảnh trong browser
6. ✅ Database có lưu imageUrl trong bảng transactions

---

## 📸 SCREENSHOTS THAM KHẢO

### Upload Image trong Postman:
```
POST http://localhost:8080/api/files/upload

[Headers]
Authorization: Bearer eyJhbGci...

[Body] → form-data
┌────────────┬──────────────────┬──────┐
│ KEY        │ VALUE            │ TYPE │
├────────────┼──────────────────┼──────┤
│ file       │ [Select Files]   │ File │
│            │ invoice.jpg      │      │
└────────────┴──────────────────┴──────┘

[Response] 200 OK
{
  "fileUrl": "http://localhost:8080/api/files/550e8400-xxx.jpg"
}
```

### Create Transaction trong Postman:
```
POST http://localhost:8080/transactions/expense

[Headers]
Content-Type: application/json
Authorization: Bearer eyJhbGci...

[Body] → raw (JSON)
{
  "amount": 150000,
  "walletId": 1,
  "categoryId": 1,
  "note": "Mua hàng",
  "imageUrl": "http://localhost:8080/api/files/550e8400-xxx.jpg",
  "transactionDate": "2025-11-10T14:30:00"
}

[Response] 200 OK
{
  "message": "Thêm chi tiêu thành công",
  "transaction": {
    "transactionId": 1,
    "imageUrl": "http://localhost:8080/api/files/550e8400-xxx.jpg"
  }
}
```

---

**✨ Chúc bạn test thành công!**

Nếu gặp vấn đề, hãy kiểm tra lại từng bước và đảm bảo backend đang chạy.

