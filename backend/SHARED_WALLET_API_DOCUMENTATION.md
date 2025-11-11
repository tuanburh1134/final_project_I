# 📚 SHARED WALLET API - TÀI LIỆU ĐẦY ĐỦ

## 🎯 TỔNG QUAN

Tính năng **Shared Wallet** cho phép người dùng chia sẻ ví với vợ/chồng hoặc thành viên gia đình để cùng quản lý tài chính.

### **Các khái niệm chính:**

- **OWNER**: Chủ sở hữu ví, có toàn quyền quản lý
- **MEMBER**: Thành viên được chia sẻ, có quyền xem và cập nhật
- **Wallet Member**: Mối quan hệ giữa User và Wallet

---

## 📊 DATABASE SCHEMA

### **Bảng: `wallet_members`**

```sql
CREATE TABLE wallet_members (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,  -- 'OWNER' hoặc 'MEMBER'
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_wallet_user (wallet_id, user_id)
);
```

**Giải thích:**
- Một user chỉ có thể là member của một wallet một lần duy nhất
- Khi wallet bị xóa → tất cả members bị xóa (CASCADE)
- Khi user bị xóa → tất cả memberships bị xóa (CASCADE)

---

## 🔐 ROLES & PERMISSIONS

| Action | OWNER | MEMBER |
|--------|:-----:|:------:|
| Xem ví | ✅ | ✅ |
| Cập nhật balance | ✅ | ✅ |
| Share với người khác | ✅ | ❌ |
| Xóa members | ✅ | ❌ |
| Xóa ví | ✅ | ❌ |
| Rời khỏi ví | ❌ | ✅ |

---

## 🚀 API ENDPOINTS

### **1. Lấy tất cả ví có quyền truy cập**

**Endpoint:** `GET /wallets`  
**Auth:** Required (JWT)  
**Mô tả:** Lấy danh sách tất cả ví mà user là owner hoặc member

**Request:**
```http
GET http://localhost:8080/wallets
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response Success (200 OK):**
```json
{
  "wallets": [
    {
      "walletId": 1,
      "walletName": "Ví gia đình",
      "currencyCode": "VND",
      "balance": 5000000.00,
      "description": "Ví chung của vợ chồng",
      "myRole": "OWNER",
      "ownerId": 1,
      "ownerName": "John Doe",
      "totalMembers": 2,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-20T14:45:00"
    },
    {
      "walletId": 3,
      "walletName": "Ví tiết kiệm",
      "currencyCode": "USD",
      "balance": 1000.00,
      "description": null,
      "myRole": "MEMBER",
      "ownerId": 2,
      "ownerName": "Jane Smith",
      "totalMembers": 3,
      "createdAt": "2024-02-01T08:00:00",
      "updatedAt": "2024-02-10T16:20:00"
    }
  ],
  "total": 2
}
```

---

### **2. Chia sẻ ví với người khác**

**Endpoint:** `POST /wallets/{walletId}/share`  
**Auth:** Required (JWT - phải là OWNER)  
**Mô tả:** Chia sẻ ví với user khác qua email

**Request:**
```http
POST http://localhost:8080/wallets/1/share
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "email": "wife@example.com"
}
```

**Response Success (200 OK):**
```json
{
  "message": "Chia sẻ ví thành công",
  "member": {
    "memberId": 5,
    "userId": 3,
    "fullName": "Jane Doe",
    "email": "wife@example.com",
    "avatar": "https://...",
    "role": "MEMBER",
    "joinedAt": "2024-03-15T10:30:00"
  }
}
```

**Response Errors:**

**400 Bad Request - Không phải owner:**
```json
{
  "error": "Chỉ chủ sở hữu mới có thể chia sẻ ví"
}
```

**400 Bad Request - Email không tồn tại:**
```json
{
  "error": "Không tìm thấy người dùng với email: abc@example.com"
}
```

**400 Bad Request - Đã là member:**
```json
{
  "error": "Người dùng này đã là thành viên của ví"
}
```

**400 Bad Request - Share với chính mình:**
```json
{
  "error": "Không thể chia sẻ ví với chính bạn"
}
```

---

### **3. Xem danh sách members của ví**

**Endpoint:** `GET /wallets/{walletId}/members`  
**Auth:** Required (JWT - phải có quyền truy cập)  
**Mô tả:** Xem tất cả thành viên của ví

**Request:**
```http
GET http://localhost:8080/wallets/1/members
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response Success (200 OK):**
```json
{
  "members": [
    {
      "memberId": 1,
      "userId": 1,
      "fullName": "John Doe",
      "email": "john@example.com",
      "avatar": "https://...",
      "role": "OWNER",
      "joinedAt": "2024-01-15T10:30:00"
    },
    {
      "memberId": 5,
      "userId": 3,
      "fullName": "Jane Doe",
      "email": "jane@example.com",
      "avatar": "https://...",
      "role": "MEMBER",
      "joinedAt": "2024-03-15T10:30:00"
    }
  ],
  "total": 2
}
```

**Response Error (403 Forbidden):**
```json
{
  "error": "Bạn không có quyền xem thành viên của ví này"
}
```

---

### **4. Xóa member khỏi ví**

**Endpoint:** `DELETE /wallets/{walletId}/members/{memberUserId}`  
**Auth:** Required (JWT - phải là OWNER)  
**Mô tả:** Owner xóa member ra khỏi ví

**Request:**
```http
DELETE http://localhost:8080/wallets/1/members/3
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response Success (200 OK):**
```json
{
  "message": "Xóa thành viên thành công"
}
```

**Response Errors:**

**400 Bad Request - Không phải owner:**
```json
{
  "error": "Chỉ chủ sở hữu mới có thể xóa thành viên"
}
```

**400 Bad Request - Không thể xóa chính mình:**
```json
{
  "error": "Không thể xóa chủ sở hữu khỏi ví"
}
```

**400 Bad Request - Member không tồn tại:**
```json
{
  "error": "Thành viên không tồn tại trong ví này"
}
```

---

### **5. Rời khỏi ví (Member)**

**Endpoint:** `POST /wallets/{walletId}/leave`  
**Auth:** Required (JWT - phải là MEMBER)  
**Mô tả:** Member tự rời khỏi ví

**Request:**
```http
POST http://localhost:8080/wallets/1/leave
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response Success (200 OK):**
```json
{
  "message": "Bạn đã rời khỏi ví thành công"
}
```

**Response Errors:**

**400 Bad Request - Owner không thể rời:**
```json
{
  "error": "Chủ sở hữu không thể rời khỏi ví. Vui lòng xóa ví hoặc chuyển quyền sở hữu."
}
```

**400 Bad Request - Không phải member:**
```json
{
  "error": "Bạn không phải thành viên của ví này"
}
```

---

### **6. Kiểm tra quyền truy cập**

**Endpoint:** `GET /wallets/{walletId}/access`  
**Auth:** Required (JWT)  
**Mô tả:** Kiểm tra quyền truy cập của user đối với wallet

**Request:**
```http
GET http://localhost:8080/wallets/1/access
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response Success (200 OK):**
```json
{
  "hasAccess": true,
  "isOwner": false,
  "role": "MEMBER"
}
```

Hoặc nếu không có quyền:
```json
{
  "hasAccess": false,
  "isOwner": false,
  "role": "NONE"
}
```

---

## 📝 USE CASES

### **Use Case 1: Vợ chồng chia sẻ ví chung**

**Bước 1:** Chồng tạo ví "Ví gia đình"
```http
POST /wallets/create
{
  "walletName": "Ví gia đình",
  "currencyCode": "VND",
  "initialBalance": 5000000,
  "description": "Ví chung của vợ chồng"
}
```

**Bước 2:** Chồng share ví với vợ
```http
POST /wallets/1/share
{
  "email": "wife@example.com"
}
```

**Bước 3:** Vợ login và xem wallet
```http
GET /wallets
→ Vợ thấy "Ví gia đình" với role: "MEMBER"
```

**Bước 4:** Cả hai có thể xem và cập nhật balance

---

### **Use Case 2: Quản lý gia đình nhiều người**

**Scenario:** Ba mẹ tạo ví, chia sẻ với 2 con

**Bước 1:** Ba tạo ví "Quỹ gia đình"
```http
POST /wallets/create
```

**Bước 2:** Ba share với mẹ
```http
POST /wallets/2/share
{ "email": "me@example.com" }
```

**Bước 3:** Ba share với con 1
```http
POST /wallets/2/share
{ "email": "con1@example.com" }
```

**Bước 4:** Ba share với con 2
```http
POST /wallets/2/share
{ "email": "con2@example.com" }
```

**Bước 5:** Xem members
```http
GET /wallets/2/members
→ Total: 4 members (1 OWNER + 3 MEMBERS)
```

---

### **Use Case 3: Xóa member**

**Scenario:** Con 1 không còn ở nhà, ba xóa khỏi ví

```http
DELETE /wallets/2/members/5
→ "Xóa thành viên thành công"
```

---

### **Use Case 4: Member tự rời khỏi ví**

**Scenario:** Con 2 đã lập gia đình riêng, muốn rời

```http
POST /wallets/2/leave
→ "Bạn đã rời khỏi ví thành công"
```

---

## 🧪 TEST CASES

### **Test Case 1: Share wallet thành công**

**Precondition:**
- User A (owner) có wallet ID = 1
- User B tồn tại với email "userb@example.com"
- User B chưa là member của wallet 1

**Steps:**
1. Login as User A
2. POST `/wallets/1/share` với body: `{"email": "userb@example.com"}`

**Expected Result:**
- Status: 200 OK
- Response chứa member info của User B
- User B có thể GET `/wallets` và thấy wallet 1

---

### **Test Case 2: Share với email không tồn tại**

**Steps:**
1. Login as User A (owner)
2. POST `/wallets/1/share` với body: `{"email": "notexist@example.com"}`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Không tìm thấy người dùng với email: notexist@example.com"

---

### **Test Case 3: Member không thể share**

**Precondition:**
- User B là MEMBER của wallet 1

**Steps:**
1. Login as User B
2. POST `/wallets/1/share` với body: `{"email": "userc@example.com"}`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Chỉ chủ sở hữu mới có thể chia sẻ ví"

---

### **Test Case 4: Owner không thể rời khỏi ví**

**Steps:**
1. Login as User A (owner)
2. POST `/wallets/1/leave`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Chủ sở hữu không thể rời khỏi ví..."

---

### **Test Case 5: Member rời thành công**

**Precondition:**
- User B là MEMBER của wallet 1

**Steps:**
1. Login as User B
2. POST `/wallets/1/leave`

**Expected Result:**
- Status: 200 OK
- Message: "Bạn đã rời khỏi ví thành công"
- User B GET `/wallets` → wallet 1 không còn trong list

---

### **Test Case 6: Xóa member thành công**

**Precondition:**
- User A là OWNER của wallet 1
- User B là MEMBER của wallet 1 (userId = 3)

**Steps:**
1. Login as User A
2. DELETE `/wallets/1/members/3`

**Expected Result:**
- Status: 200 OK
- Message: "Xóa thành viên thành công"
- User B không còn access vào wallet 1

---

### **Test Case 7: Không thể xóa chính mình**

**Steps:**
1. Login as User A (owner, userId = 1)
2. DELETE `/wallets/1/members/1`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Không thể xóa chủ sở hữu khỏi ví"

---

### **Test Case 8: Share với chính mình**

**Steps:**
1. Login as User A (email: usera@example.com)
2. POST `/wallets/1/share` với body: `{"email": "usera@example.com"}`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Không thể chia sẻ ví với chính bạn"

---

### **Test Case 9: Share với user đã là member**

**Precondition:**
- User B đã là member của wallet 1

**Steps:**
1. Login as User A (owner)
2. POST `/wallets/1/share` với body: `{"email": "userb@example.com"}`

**Expected Result:**
- Status: 400 Bad Request
- Error: "Người dùng này đã là thành viên của ví"

---

## 🔒 SECURITY NOTES

1. ✅ **JWT Authentication:** Tất cả endpoints đều yêu cầu JWT token
2. ✅ **Owner Verification:** Các actions nhạy cảm (share, remove) chỉ owner mới được
3. ✅ **Access Control:** User chỉ xem được wallets mà họ có quyền
4. ✅ **Cascade Delete:** Khi xóa wallet hoặc user → tự động xóa relationships
5. ✅ **Unique Constraint:** Một user không thể là member của 1 wallet nhiều lần

---

## 📊 PERFORMANCE CONSIDERATIONS

1. **Indexing:**
   - Index trên `(wallet_id, user_id)` để tăng tốc queries
   - Index trên `user_id` cho việc lấy tất cả wallets của user

2. **Caching:** (Future)
   - Cache danh sách members của wallet
   - Cache permissions check

3. **N+1 Query Problem:**
   - Sử dụng JOIN để lấy owner info cùng wallet trong 1 query

---

## 🎉 HOÀN THÀNH

Tính năng **Shared Wallet** đã được triển khai đầy đủ với:
- ✅ Database schema
- ✅ Entity & Repository
- ✅ Service logic với validation
- ✅ Controller endpoints
- ✅ Security permissions
- ✅ Error handling
- ✅ Documentation & Test cases

**Happy Coding! 🚀**

