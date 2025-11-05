import React, { useState } from "react";

import Notification from "../../components/common/Notification";

const ForgotPasswordPage = () => {

  const [email, setEmail] = useState("");

  const [notif, setNotif] = useState({ type: "", message: "" });

  const [submitting, setSubmitting] = useState(false);

  const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());

  const handleSubmit = async (e) => {

    e.preventDefault();

    setNotif({ type: "", message: "" });

    if (!email.trim()) {

      setNotif({ type: "error", message: "Vui lòng nhập email." });

      return;

    }

    if (!isEmail(email)) {

      setNotif({ type: "error", message: "Email không hợp lệ." });

      return;

    }

    try {

      setSubmitting(true);

      // TODO: Gọi API quên mật khẩu (gửi mail reset)

      // const res = await fetch("/api/auth/forgot-password", {...});

      // const data = await res.json();

      setNotif({ type: "success", message: "Đã gửi liên kết đặt lại mật khẩu vào email (demo)." });

    } catch (err) {

      setNotif({ type: "error", message: "Có lỗi xảy ra. Vui lòng thử lại!" });

    } finally {

      setSubmitting(false);

    }

  };

  return (
<div className="login__main-card" style={{ backgroundColor: "rgba(255,255,255,0.95)", borderRadius: 12, boxShadow: "0 4px 20px rgba(0,0,0,0.2)", padding: "2rem", width: "100%", maxWidth: 420 }}>
<h3 className="text-center mb-4">Quên mật khẩu</h3>
<form onSubmit={handleSubmit} noValidate>
<div className="mb-3">
<label className="form-label">Email đăng ký</label>
<input

            type="email"

            className="form-control"

            placeholder="Nhập địa chỉ email của bạn"

            value={email}

            onChange={(e) => setEmail(e.target.value)}

            autoComplete="email"

          />
</div>
<div className="d-grid mb-2">
<button className="btn btn-primary" type="submit" disabled={submitting}>

            {submitting ? "Đang gửi..." : "Gửi liên kết đặt lại"}
</button>
</div>
<div className="text-center">
<a href="/login" className="text-decoration-none link-hover">Quay lại đăng nhập</a>
</div>
<Notification

          type={notif.type}

          message={notif.message}

          duration={2500}

          onClose={() => setNotif({ type: "", message: "" })}

        />
</form>
</div>

  );

};

export default ForgotPasswordPage;
 