import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../services/authService";

export default function HomePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Lấy thông tin user từ localStorage
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
  }, []);

  const logout = async () => {
    try {
      await authService.logout();
      navigate("/login", { replace: true });
    } catch (err) {
      console.error("Logout error:", err);
      // Vẫn logout phía client dù API fail
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
      navigate("/login", { replace: true });
    }
  };

  return (
    <div className="container py-5 text-center">
      <h2 className="mb-2">Trang chủ</h2>
      <p className="text-muted">
        Chào mừng <strong>{user?.fullName || user?.email}</strong> quay lại 👋
      </p>
      
      {user && (
        <div className="card mx-auto mt-4" style={{ maxWidth: "500px" }}>
          <div className="card-body">
            <h5 className="card-title">Thông tin tài khoản</h5>
            <div className="text-start mt-3">
              <p><strong>Họ tên:</strong> {user.fullName}</p>
              <p><strong>Email:</strong> {user.email}</p>
              <p><strong>Provider:</strong> {user.provider || "local"}</p>
              <p><strong>Trạng thái:</strong> 
                {user.enabled ? (
                  <span className="badge bg-success ms-2">Đã xác thực</span>
                ) : (
                  <span className="badge bg-warning ms-2">Chưa xác thực</span>
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <button className="btn btn-outline-danger mt-4" onClick={logout}>
        <i className="bi bi-box-arrow-right me-2" />
        Đăng xuất
      </button>
    </div>
  );
}
 