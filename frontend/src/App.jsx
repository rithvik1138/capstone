import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppLayout } from './layouts/AppLayout';
import { LoginPage } from './pages/auth/LoginPage';
import { SignupPage } from './pages/auth/SignupPage';
import { UserDashboard } from './pages/user/UserDashboard';
import { DeviceDetailPage } from './pages/devices/DeviceDetailPage';
import { AlertsPage } from './pages/alerts/AlertsPage';
import { IncidentsListPage } from './pages/incidents/IncidentsListPage';
import { IncidentDetailPage } from './pages/incidents/IncidentDetailPage';
import { AdminDashboard } from './pages/admin/AdminDashboard';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) {
    return (
      <div className="min-h-screen bg-dark-950 flex items-center justify-center font-mono text-xs text-orange-500">
        INITIALIZING SECURITY CONTEXT...
      </div>
    );
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  if (loading) {
    return (
      <div className="min-h-screen bg-dark-950 flex items-center justify-center font-mono text-xs text-orange-500">
        EVALUATING RBAC PERMISSIONS...
      </div>
    );
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
};

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Protected Application Layout Routes */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<UserDashboard />} />
            <Route path="/devices" element={<UserDashboard />} />
            <Route path="/devices/register" element={<UserDashboard openRegister={true} />} />
            <Route path="/devices/:id" element={<DeviceDetailPage />} />
            <Route path="/alerts" element={<AlertsPage />} />
            <Route path="/incidents" element={<IncidentsListPage />} />
            <Route path="/incidents/:id" element={<IncidentDetailPage />} />

            {/* Admin Exclusive Route */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminDashboard />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/devices"
              element={
                <AdminRoute>
                  <AdminDashboard defaultTab="devices" />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <AdminRoute>
                  <AdminDashboard defaultTab="users" />
                </AdminRoute>
              }
            />
            <Route
              path="/admin/logs"
              element={
                <AdminRoute>
                  <AdminDashboard defaultTab="logs" />
                </AdminRoute>
              }
            />
          </Route>

          {/* Catch-all fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;