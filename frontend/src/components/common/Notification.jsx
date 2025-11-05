import React, { useEffect, useState } from "react";

const Notification = ({ type = "info", message, duration = 3000, onClose }) => {

  const [visible, setVisible] = useState(Boolean(message));

  useEffect(() => {

    setVisible(Boolean(message));

    if (message && duration) {

      const timer = setTimeout(() => {

        setVisible(false);

        onClose && onClose();

      }, duration);

      return () => clearTimeout(timer);

    }

  }, [message, duration, onClose]);

  if (!visible || !message) return null;

  // Map màu và icon theo kiểu

  const styles = {

    success: {

      border: "2px solid #28a745",

      background: "rgba(40,167,69,0.1)",

      color: "#155724",

      icon: "✅",

    },

    error: {

      border: "2px solid #dc3545",

      background: "rgba(220,53,69,0.1)",

      color: "#721c24",

      icon: "❌",

    },

    warning: {

      border: "2px solid #ffc107",

      background: "rgba(255,193,7,0.1)",

      color: "#856404",

      icon: "⚠️",

    },

    info: {

      border: "2px solid #0dcaf0",

      background: "rgba(13,202,240,0.1)",

      color: "#055160",

      icon: "ℹ️",

    },

  };

  const style = styles[type] || styles.info;

  return (
<div

      style={{

        ...style,

        borderRadius: "10px",

        padding: "16px 20px",

        marginTop: "20px",

        display: "flex",

        alignItems: "center",

        justifyContent: "center",

        gap: "8px",

        fontWeight: "500",

        fontSize: "16px",

        transition: "opacity 0.3s ease",

        boxShadow: "0 2px 8px rgba(0,0,0,0.15)",

      }}
>
<span style={{ fontSize: "18px" }}>{style.icon}</span>
<span>{message}</span>
</div>

  );

};

export default Notification;
 