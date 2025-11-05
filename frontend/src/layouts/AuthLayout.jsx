import React from "react";
import Header from "../components/common/Header";
import Footer from "../components/common/Footer";
import Notification from "../components/common/Notification";

// CHỌN 1 TRONG 2 CÁCH DƯỚI ĐÂY (đừng để cả hai)

// Cách A: ảnh nằm trong src/assets/imgdangnhap.png
import bg from "../assets/imgdangnhap.jpg";

// Cách B (nếu để ảnh ở public/imgdangnhap.png):
// const bg = "/imgdangnhap.png";

import "../styles/common.css";

const AuthLayout = ({ children, notice }) => {
  return (
    <div
      className="login"
      style={{
         minHeight: "100vh",
    width: "100vw",            
    backgroundImage: `url(${bg})`,
    backgroundRepeat: "no-repeat",
    backgroundPosition: "center center",
    backgroundSize: "cover",     // ✅ phủ kín, không bị viền trắng
    display: "flex",
    flexDirection: "column",
      }}
    >
      <Header />
      {notice && <Notification message={notice.message} type={notice.type} />}

      <main className="login__main" style={{ flex: 1, display: "flex", justifyContent: "center", alignItems: "center" }}>
        {children}
      </main>

      <Footer />
    </div>
  );
};

export default AuthLayout;
