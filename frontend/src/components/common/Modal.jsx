import React, { useEffect } from "react";

const Modal = ({ open, onClose, width = 480, children }) => {

  useEffect(() => {

    const onKey = (e) => e.key === "Escape" && onClose?.();

    if (open) document.addEventListener("keydown", onKey);

    return () => document.removeEventListener("keydown", onKey);

  }, [open, onClose]);

  if (!open) return null;

  return (
<div

      aria-modal="true"

      role="dialog"

      onClick={onClose}

      style={{

        position: "fixed",

        inset: 0,

        zIndex: 1050,

        display: "flex",

        alignItems: "center",

        justifyContent: "center",

      }}
>

      {/* nền mờ */}
<div

        style={{

          position: "absolute",

          inset: 0,

          background: "rgba(0,0,0,0.45)",

          backdropFilter: "blur(2px)",

        }}

      />

      {/* hộp trắng */}
<div

        onClick={(e) => e.stopPropagation()}

        style={{

          position: "relative",

          width: typeof width === "number" ? `${width}px` : width,

          maxWidth: "92vw",

          background: "#fff",

          borderRadius: 12,

          boxShadow: "0 12px 40px rgba(0,0,0,0.25)",

          overflow: "hidden",

          animation: "modalIn .2s ease-out",

        }}
>

        {children}
</div>
<style>{`

        @keyframes modalIn {

          from { transform: translateY(10px); opacity: .5; }

          to   { transform: translateY(0);   opacity: 1;  }

        }

      `}</style>
</div>

  );

};

export default Modal;
 