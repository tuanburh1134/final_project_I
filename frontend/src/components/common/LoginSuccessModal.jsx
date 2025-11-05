import React, { useEffect, useState } from "react";
import Modal from "./Modal";
import { useNavigate } from "react-router-dom";
/**
* Modal hiển thị sau khi đăng nhập thành công
* - Tự đếm ngược rồi điều hướng về "/"
* - Có nút "Vào trang chủ" để đi ngay
*/
const LoginSuccessModal = ({ open, seconds = 2, onClose }) => {
 const [remain, setRemain] = useState(seconds);
 const navigate = useNavigate();
 useEffect(() => {
   if (!open) return;
   setRemain(seconds);
   const t = setInterval(() => {
     setRemain((s) => {
       if (s <= 1) {
         clearInterval(t);
         onClose?.();
         navigate("/", { replace: true });
         return 0;
       }
       return s - 1;
     });
   }, 1000);
   return () => clearInterval(t);
 }, [open, seconds, navigate, onClose]);
 const goHome = () => {
   onClose?.();
   navigate("/", { replace: true });
 };
 return (
<Modal open={open} onClose={goHome} width={480}>
<div style={{ padding: "18px 20px", borderBottom: "1px solid #eee" }}>
<strong>Đăng nhập</strong>
</div>
<div style={{ padding: 20, textAlign: "center" }}>
       {/* icon + message */}
<div
         style={{
           width: 64,
           height: 64,
           margin: "0 auto 12px",
           borderRadius: "50%",
           background: "rgba(25,135,84,.12)",
           display: "flex",
           alignItems: "center",
           justifyContent: "center",
           fontSize: 34,
           color: "#198754",
         }}
>
         ✓
</div>
<h5 className="mb-2">Đăng nhập thành công!</h5>
<div className="text-muted mb-3">
         Đang chuyển đến trang chủ trong <strong>{remain}s</strong>…
</div>
       {/* spinner nhỏ */}
<div className="d-flex justify-content-center mb-3">
<div className="spinner-border text-success" role="status" style={{ width: 24, height: 24 }}>
<span className="visually-hidden">Loading…</span>
</div>
</div>
<button className="btn btn-primary" onClick={goHome}>
         Vào trang chủ ngay
</button>
</div>
</Modal>
 );
};
export default LoginSuccessModal;