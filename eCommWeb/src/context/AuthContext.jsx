import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginUser, registerUser, fetchCurrentUser } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('ecomm_token') || null);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState(null);
  const [gatewayMeta, setGatewayMeta] = useState(null);

  // Auto-restore session on mount if token exists
  useEffect(() => {
    const initializeAuth = async () => {
      const savedToken = localStorage.getItem('ecomm_token');
      const savedUser = localStorage.getItem('ecomm_user');

      if (savedToken && savedUser) {
        try {
          setUser(JSON.parse(savedUser));
          setToken(savedToken);
          // Verify token against backend
          const profile = await fetchCurrentUser();
          if (profile?.data?.data) {
            setUser(profile.data.data);
            localStorage.setItem('ecomm_user', JSON.stringify(profile.data.data));
          }
        } catch (err) {
          console.warn('Session expired or gateway unavailable, clearing token.');
          logout();
        }
      }
      setInitialLoading(false);
    };

    initializeAuth();
  }, []);

  // Login handler
  const login = async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const result = await loginUser({ email, password });
      const authData = result.data.data; // { token, type, email, name, roles }
      
      const jwtToken = authData.token;
      const userInfo = {
        name: authData.name,
        email: authData.email,
        roles: authData.roles || ['ROLE_USER'],
      };

      localStorage.setItem('ecomm_token', jwtToken);
      localStorage.setItem('ecomm_user', JSON.stringify(userInfo));

      setToken(jwtToken);
      setUser(userInfo);
      setGatewayMeta(result.metadata);

      return { success: true, message: result.data.message || 'Login successful' };
    } catch (err) {
      const message = err.friendlyMessage || 'Authentication failed. Please verify your credentials.';
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  // Register handler
  const register = async (name, email, password, role = 'ROLE_USER') => {
    setLoading(true);
    setError(null);
    try {
      const result = await registerUser({ name, email, password, role });
      const authData = result.data.data;

      const jwtToken = authData.token;
      const userInfo = {
        name: authData.name,
        email: authData.email,
        roles: authData.roles || [role],
      };

      localStorage.setItem('ecomm_token', jwtToken);
      localStorage.setItem('ecomm_user', JSON.stringify(userInfo));

      setToken(jwtToken);
      setUser(userInfo);
      setGatewayMeta(result.metadata);

      return { success: true, message: result.data.message || 'Registration successful' };
    } catch (err) {
      const message = err.friendlyMessage || 'Registration failed.';
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  // Logout handler
  const logout = () => {
    localStorage.removeItem('ecomm_token');
    localStorage.removeItem('ecomm_user');
    setUser(null);
    setToken(null);
    setError(null);
    setGatewayMeta(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        loading,
        initialLoading,
        error,
        setError,
        gatewayMeta,
        setGatewayMeta,
        login,
        register,
        logout,
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
