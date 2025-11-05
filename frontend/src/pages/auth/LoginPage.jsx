import React, { useState } from "react";

import LoginSuccessModal from "../../components/common/LoginSuccessModal";

const LoginPage = () => {

  const [form, setForm] = useState({ username: "", password: "" });

  const [submitting, setSubmitting] = useState(false);

  const [showSuccess, setShowSuccess] = useState(false);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {

    e.preventDefault();

    if (!form.username || !form.password) return; // bạn có thể thêm validate + notification

    try {

      setSubmitting(true);

      // TODO: gọi API đăng nhập thật

      // const res = await fetch("/api/auth/login", {...});

      // const ok = res.ok;

      // demo thành công:

      setShowSuccess(true);

    } catch (err) {

      // TODO: hiển thị lỗi (Notification)

      console.error(err);

    } finally {

      setSubmitting(false);

    }

  };

  return (
<>
<div

        className="login__main-card"

        style={{

          backgroundColor: "rgba(255,255,255,0.95)",

          borderRadius: 12,

          boxShadow: "0 4px 20px rgba(0,0,0,0.2)",

          padding: "2rem",

          width: "100%",

          maxWidth: 420,

        }}
>
<h3 className="text-center mb-4">Đăng nhập</h3>
<form onSubmit={onSubmit} noValidate>
<div className="mb-3">
<label className="form-label">Tên đăng nhập</label>
<input

              className="form-control"

              name="username"

              value={form.username}

              onChange={onChange}

              placeholder="Nhập tên đăng nhập hoặc email"

              autoComplete="username"

            />
</div>
<div className="mb-3">
<label className="form-label">Mật khẩu</label>
<input

              type="password"

              className="form-control"

              name="password"

              value={form.password}

              onChange={onChange}

              placeholder="Nhập mật khẩu"

              autoComplete="current-password"

            />
</div>
<div className="d-grid">
<button className="btn btn-primary" type="submit" disabled={submitting}>

              {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
</button>
</div>
<div className="text-center mt-3">
<a href="/forgot" className="text-decoration-none me-3">Quên mật khẩu?</a>
<a href="/register" className="text-decoration-none">Tạo tài khoản</a>
</div>
</form>
</div>

      {/* Modal thành công */}
<LoginSuccessModal open={showSuccess} onClose={() => setShowSuccess(false)} seconds={2} />
</>

  );

};

export default LoginPage;
 