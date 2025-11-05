import React from "react";

import "./Modal.css";

const LogoutConfirmModal = ({ show, onClose, onConfirm }) => {

  if (!show) return null;

  return (
<div className="modal-overlay">
<div className="modal-box">
<div className="modal-icon warning">
<i className="bi bi-box-arrow-right"></i>
</div>
<h3>Xác nhận đăng xuất</h3>
<p>Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?</p>
<div className="modal-actions">
<button className="btn btn-outline-secondary" onClick={onClose}>

            Hủy
</button>
<button className="btn btn-danger" onClick={onConfirm}>

            Đăng xuất
</button>
</div>
</div>
</div>

  );

};

export default LogoutConfirmModal;
 