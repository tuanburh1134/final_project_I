import React from "react";

import { Routes, Route, Navigate } from "react-router-dom";

import AuthLayout from "./layouts/AuthLayout";

import LoginPage from "./pages/Auth/LoginPage";

import RegisterPage from "./pages/Auth/RegisterPage";

import ForgotPasswordPage from "./pages/Auth/ForgotPasswordPage";

import HomePage from "./pages/HomePage";

import ProtectedRoute from "./components/ProtectedRoute";

export default function App() {

  return (
<Routes>

      {/* Trang chủ (bảo vệ) */}
<Route

        path="/"

        element={
<ProtectedRoute>
<HomePage />
</ProtectedRoute>

        }

      />

      {/* Auth pages (không bảo vệ) */}
<Route

        path="/login"

        element={
<AuthLayout>
<LoginPage />
</AuthLayout>

        }

      />
<Route

        path="/register"

        element={
<AuthLayout>
<RegisterPage />
</AuthLayout>

        }

      />
<Route

        path="/forgot"

        element={
<AuthLayout>
<ForgotPasswordPage />
</AuthLayout>

        }

      />

      {/* Sai URL → về /login */}
<Route path="*" element={<Navigate to="/login" replace />} />
</Routes>

  );

}
 