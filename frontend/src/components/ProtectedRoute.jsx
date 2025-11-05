import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import authService from "../services/authService";

export default function ProtectedRoute({ children }) {
  const location = useLocation();
  const isAuthenticated = authService.isAuthenticated();

  if (!isAuthenticated) {
    // Chưa login -> về /login và nhớ lại URL cũ
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}