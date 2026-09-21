import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');

      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
        try {
          const res = await authService.getMe();
          if (res.data && res.data.data) {
            setUser(res.data.data);
            localStorage.setItem('user', JSON.stringify(res.data.data));
          }
        } catch (err) {
          console.error("Failed to re-validate token with backend:", err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (username, password) => {
    const res = await authService.login({ username, password });
    if (res.data && res.data.data) {
      const authData = res.data.data;
      setToken(authData.token);
      const userData = {
        userId: authData.userId,
        username: authData.username,
        email: authData.email,
        role: authData.role,
      };
      setUser(userData);
      localStorage.setItem('token', authData.token);
      localStorage.setItem('user', JSON.stringify(userData));
      return userData;
    }
    throw new Error("Invalid response format from login");
  };

  const signup = async (username, email, password) => {
    return await authService.signup({ username, email, password });
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const isAdmin = user && user.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        signup,
        logout,
        isAuthenticated: !!token && !!user,
        isAdmin,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};