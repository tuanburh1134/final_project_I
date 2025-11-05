import React, { useState } from "react";

import Notification from "../../components/common/Notification";

const RegisterPage = () => {

  const [form, setForm] = useState({

    username: "",

    contact: "", // email hoặc số điện thoại

    password: "",

    confirm: "",

  });

  const [errors, setErrors] = useState({});

  const [notif, setNotif] = useState({ type: "", message: "" });

  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());

  const isPhone = (v) => /^(0|\+?\d{1,3})?([.\-\s]?\d){9,12}$/.test(v.replace(/\s/g, ""));

  const validate = () => {

    const e = {};

    if (!form.username.trim()) e.username = "Vui lòng nhập tên đăng nhập";

    if (!form.contact.trim()) e.contact = "Vui lòng nhập email hoặc số điện thoại";

    else if (!isEmail(form.contact) && !isPhone(form.contact)) e.contact = "Liên hệ phải là email hợp lệ hoặc số điện thoại";

    if (!form.password) e.password = "Vui lòng nhập mật khẩu";

    if (form.password && form.password.length < 6) e.password = "Mật khẩu tối thiểu 6 ký tự";

    if (!form.confirm) e.confirm = "Nhập lại mật khẩu";

    if (form.password && form.confirm && form.password !== form.confirm) e.confirm = "Mật khẩu nhập lại không khớp";

    setErrors(e);

    return Object.keys(e).length === 0;

  };

  const onSubmit = async (e) => {

    e.preventDefault();

    setNotif({ type: "", message: "" });

    if (!validate()) {

      setNotif({ type: "error", message: "Vui lòng kiểm tra lại các trường bị lỗi." });

      return;

    }

    try {

      setSubmitting(true);

      // TODO: gọi API đăng ký

      // const res = await fetch("/api/auth/register", {...});

      // const data = await res.json();

      setNotif({ type: "success", message: "Tạo tài khoản thành công!" });

      // setTimeout(() => (window.location.href = "/login"), 800);

    } catch (err) {

      setNotif({ type: "error", message: "Đăng ký thất bại. Vui lòng thử lại!" });

    } finally {

      setSubmitting(false);

    }

  };

  return (
<div className="login__main-card" style={{ backgroundColor: "rgba(255,255,255,0.95)", borderRadius: 12, boxShadow: "0 4px 20px rgba(0,0,0,0.2)", padding: "2rem", width: "100%", maxWidth: 420 }}>
<h3 className="text-center mb-4">Tạo tài khoản</h3>
<form onSubmit={onSubmit} noValidate>
<div className="mb-3">
<label className="form-label">Tên đăng nhập</label>
<input

            className={`form-control ${errors.username ? "is-invalid" : ""}`}

            name="username"

            value={form.username}

            onChange={onChange}

            placeholder="Nhập tên đăng nhập"

            autoComplete="username"

          />

          {errors.username && <div className="invalid-feedback">{errors.username}</div>}
</div>
<div className="mb-3">
<label className="form-label">Email </label>
<input

            className={`form-control ${errors.contact ? "is-invalid" : ""}`}

            name="contact"

            value={form.contact}

            onChange={onChange}

            placeholder="email@domain.com hoặc 09xxxxxxxx"

            autoComplete="email"

          />

          {errors.contact && <div className="invalid-feedback">{errors.contact}</div>}
</div>
<div className="mb-3">
<label className="form-label">Mật khẩu</label>
<input

            type="password"

            className={`form-control ${errors.password ? "is-invalid" : ""}`}

            name="password"

            value={form.password}

            onChange={onChange}

            placeholder="Nhập mật khẩu"

            autoComplete="new-password"

          />

          {errors.password && <div className="invalid-feedback">{errors.password}</div>}
</div>
<div className="mb-3">
<label className="form-label">Nhập lại mật khẩu</label>
<input

            type="password"

            className={`form-control ${errors.confirm ? "is-invalid" : ""}`}

            name="confirm"

            value={form.confirm}

            onChange={onChange}

            placeholder="Nhập lại mật khẩu"

            autoComplete="new-password"

          />

          {errors.confirm && <div className="invalid-feedback">{errors.confirm}</div>}
</div>
<div className="d-grid">
<button className="btn btn-primary" type="submit" disabled={submitting}>

            {submitting ? "Đang tạo..." : "Đăng ký"}
</button>
</div>
<div className="text-center mt-3">

          Đã có tài khoản? <a href="/login" className="text-decoration-none">Đăng nhập</a>
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

export default RegisterPage;
 