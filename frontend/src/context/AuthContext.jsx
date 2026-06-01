import { createContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  // On page refresh, check if we are still logged in
  useEffect(() => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const profileId = localStorage.getItem('profileId');
    if (token) {
      setUser({ token, role, profileId });
    }
  }, []);

  const login = (data) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);
    localStorage.setItem('profileId', data.profileId);
    setUser(data);
    
    // Auto-route based on who just logged in
    if (data.role === 'CUSTOMER') navigate('/customer/dashboard');
    else if (data.role === 'RESTAURANT') navigate('/restaurant/dashboard');
    else if (data.role === 'ADMIN') navigate('/admin/dashboard'); // <-- ADDED ADMIN ROUTE
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};