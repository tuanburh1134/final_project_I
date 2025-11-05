# 🧪 Test User Profile API

## Prerequisites
1. Đảm bảo backend đang chạy trên `http://localhost:8080`
2. Bạn cần có **access token** hợp lệ (đăng nhập trước)

---

## 📝 Cách lấy Access Token

### Bước 1: Đăng ký tài khoản (nếu chưa có)
```bash
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "fullName": "Test User",
  "email": "testuser@example.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234",
  "recaptchaToken": "dev-bypass"
}
```

### Bước 2: Xác minh email
Kiểm tra email và lấy mã 6 chữ số, sau đó:
```bash
POST http://localhost:8080/auth/verify
Content-Type: application/json

{
  "email": "testuser@example.com",
  "code": "123456"
}
```

Response sẽ trả về `accessToken` và `refreshToken`.

### Bước 3: Hoặc đăng nhập (nếu đã có tài khoản)
```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "Test@1234"
}
```

**Lưu lại `accessToken` để sử dụng cho các request sau.**

---

## 🔬 Test Cases

### Test 1: Lấy thông tin profile
```bash
GET http://localhost:8080/user/profile
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response:**
```json
{
  "success": true,
  "user": {
    "userId": 1,
    "fullName": "Test User",
    "email": "testuser@example.com",
    "avatarUrl": null,
    "provider": "local",
    "createdAt": "2025-01-05T10:00:00",
    "updatedAt": "2025-01-05T10:00:00"
  }
}
```

---

### Test 2: Cập nhật tên
```bash
PUT http://localhost:8080/user/update
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json

{
  "fullName": "Nguyễn Văn Test"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "user": {
    "userId": 1,
    "fullName": "Nguyễn Văn Test",
    "email": "testuser@example.com",
    "avatarUrl": null,
    "updatedAt": "2025-01-05T10:05:00"
  }
}
```

---

### Test 3: Upload avatar
Tạo file `test-avatar.jpg` hoặc sử dụng ảnh bất kỳ.

```bash
POST http://localhost:8080/user/avatar
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: multipart/form-data

[File upload: avatar=test-avatar.jpg]
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Cập nhật avatar thành công",
  "user": {
    "userId": 1,
    "fullName": "Nguyễn Văn Test",
    "email": "testuser@example.com",
    "avatarUrl": "/uploads/avatars/avatar_1_abc-123-xyz.jpg",
    "updatedAt": "2025-01-05T10:10:00"
  }
}
```

Sau khi upload thành công, bạn có thể truy cập avatar qua:
```
http://localhost:8080/uploads/avatars/avatar_1_abc-123-xyz.jpg
```

---

### Test 4: Cập nhật cả tên và avatar
```bash
POST http://localhost:8080/user/update-profile
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: multipart/form-data

fullName=Trần Thị Test
avatar=new-avatar.jpg
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "user": {
    "userId": 1,
    "fullName": "Trần Thị Test",
    "email": "testuser@example.com",
    "avatarUrl": "/uploads/avatars/avatar_1_def-456-uvw.jpg",
    "updatedAt": "2025-01-05T10:15:00"
  }
}
```

---

## ❌ Test Error Cases

### Test 5: Cập nhật với tên quá ngắn
```bash
PUT http://localhost:8080/user/update
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json

{
  "fullName": "A"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "error": "Tên phải từ 2-100 ký tự"
}
```

---

### Test 6: Upload file không phải ảnh
```bash
POST http://localhost:8080/user/avatar
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: multipart/form-data

[File upload: avatar=document.pdf]
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "error": "Chỉ chấp nhận file ảnh: jpg, jpeg, png, gif, webp"
}
```

---

### Test 7: Upload file quá lớn (>5MB)
```bash
POST http://localhost:8080/user/avatar
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: multipart/form-data

[File upload: avatar=large-image.jpg (>5MB)]
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "error": "File không được vượt quá 5MB"
}
```

---

### Test 8: Truy cập không có token
```bash
GET http://localhost:8080/user/profile
# Không có Authorization header
```

**Expected Response (401 Unauthorized):**
```
Token đã hết hạn hoặc không hợp lệ
```

---

### Test 9: Token không hợp lệ
```bash
GET http://localhost:8080/user/profile
Authorization: Bearer INVALID_TOKEN_12345
```

**Expected Response (401 Unauthorized):**
```
Token không hợp lệ
```

---

## 🛠️ Postman Collection

### Import vào Postman:

1. Tạo Collection mới: "User Profile API"
2. Tạo Environment với biến:
   - `base_url`: `http://localhost:8080`
   - `access_token`: `<your_token_here>`

3. Các request:

**1. Get Profile**
- Method: GET
- URL: `{{base_url}}/user/profile`
- Headers: `Authorization: Bearer {{access_token}}`

**2. Update Name**
- Method: PUT
- URL: `{{base_url}}/user/update`
- Headers: 
  - `Authorization: Bearer {{access_token}}`
  - `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "fullName": "New Name"
}
```

**3. Upload Avatar**
- Method: POST
- URL: `{{base_url}}/user/avatar`
- Headers: `Authorization: Bearer {{access_token}}`
- Body (form-data):
  - Key: `avatar`
  - Type: File
  - Value: [Select file]

**4. Update Profile (Name + Avatar)**
- Method: POST
- URL: `{{base_url}}/user/update-profile`
- Headers: `Authorization: Bearer {{access_token}}`
- Body (form-data):
  - Key: `fullName`, Type: Text, Value: "Complete Name"
  - Key: `avatar`, Type: File, Value: [Select file]

---

## 📊 Testing Checklist

- [ ] Lấy được access token từ `/auth/login`
- [ ] Lấy thông tin profile thành công
- [ ] Cập nhật tên thành công
- [ ] Upload avatar thành công (file hợp lệ)
- [ ] Avatar cũ bị xóa khi upload avatar mới
- [ ] Truy cập được avatar qua URL public
- [ ] Cập nhật cả tên và avatar cùng lúc
- [ ] Validation hoạt động (tên quá ngắn → lỗi)
- [ ] Upload file không phải ảnh → lỗi
- [ ] Upload file quá lớn → lỗi
- [ ] Truy cập không có token → 401
- [ ] Token không hợp lệ → 401

---

## 🐛 Debugging Tips

1. **Avatar không hiển thị?**
   - Kiểm tra thư mục `uploads/avatars/` đã được tạo chưa
   - Kiểm tra URL có đúng format: `/uploads/avatars/avatar_X_UUID.ext`

2. **Upload lỗi 500?**
   - Kiểm tra quyền ghi file của ứng dụng
   - Kiểm tra logs backend để xem chi tiết lỗi

3. **Token hết hạn?**
   - Sử dụng `/auth/refresh` để lấy token mới
   - Hoặc đăng nhập lại

4. **File upload không work?**
   - Kiểm tra `application.properties` đã config multipart chưa
   - Kiểm tra Content-Type header là `multipart/form-data`

