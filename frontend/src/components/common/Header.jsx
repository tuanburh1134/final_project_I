import React from "react";
import logo from "../../assets/images/logo.png";

const Header = ({ title = "Hệ thống quản lý chi tiêu", }) => {
  return (
    <header className="login__header d-flex justify-content-between align-items-center p-3">
      <div className="login__brand-logo d-flex align-items-center">
        <img src={logo} alt="Logo" className="login__logo-img me-3" />
        <div className="login__brand-name">
          <strong className="login__title">{title}</strong>
        </div>
      </div>
    </header>
  );
};

export default Header;
