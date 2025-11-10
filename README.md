# Personal Finance App

Monorepo gồm 2 phần:
- backend (Spring Boot / Java)
- frontend (React + Vite)

Đây là skeleton ban đầu cho Sprint 1 (đăng ký / đăng nhập).

---

## 🆕 Tính năng mới: Đính kèm hình ảnh hóa đơn

### 📸 User Story
> **Là người dùng, tôi muốn đính kèm hình ảnh hóa đơn vào giao dịch, để lưu giữ chứng từ cho việc kiểm tra sau này.**

### ✨ Đã triển khai
- ✅ Upload hình ảnh hóa đơn (JPG, PNG, GIF, WEBP, max 5MB)
- ✅ Xem hình ảnh đã upload
- ✅ Xóa hình ảnh
- ✅ Tích hợp với Transaction API
- ✅ Upload nhiều ảnh cùng lúc (bonus)

### 📚 Documentation
- **Tổng quan**: [`backend/IMPLEMENTATION_SUMMARY.md`](backend/IMPLEMENTATION_SUMMARY.md)
- **API Documentation**: [`backend/API_FILE_UPLOAD.md`](backend/API_FILE_UPLOAD.md)
- **Test Cases**: [`backend/TEST_FILE_UPLOAD.md`](backend/TEST_FILE_UPLOAD.md)
- **Feature Details**: [`backend/FEATURE_IMAGE_ATTACHMENT.md`](backend/FEATURE_IMAGE_ATTACHMENT.md)
- **Demo Page**: [`backend/demo-upload.html`](backend/demo-upload.html)

### 🚀 Quick Start

#### 1. Start backend:
```bash
cd backend
mvn spring-boot:run
```

#### 2. Test with demo page:
Mở file `backend/demo-upload.html` trong browser

#### 3. Test với Postman/cURL:
```bash
# Upload ảnh
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@invoice.jpg"

# Tạo transaction với ảnh
curl -X POST http://localhost:8080/transactions/expense \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150000,
    "transactionDate": "2025-11-10T14:30:00",
    "walletId": 1,
    "categoryId": 1,
    "note": "Mua hàng tại siêu thị",
    "imageUrl": "http://localhost:8080/api/files/550e8400-xxx.jpg"
  }'
```

### 📡 API Endpoints
- `POST /api/files/upload` - Upload ảnh (cần JWT)
- `POST /api/files/upload-multiple` - Upload nhiều ảnh (cần JWT)
- `GET /api/files/{filename}` - Xem ảnh (public)
- `DELETE /api/files/{filename}` - Xóa ảnh (cần JWT)

### 🎯 Integration với Frontend
Xem code examples trong [`backend/FEATURE_IMAGE_ATTACHMENT.md`](backend/FEATURE_IMAGE_ATTACHMENT.md)

---