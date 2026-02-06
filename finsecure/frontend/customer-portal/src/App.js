import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

// Components
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Accounts from './components/Accounts';
import Transactions from './components/Transactions';
import Cards from './components/Cards';
import Loans from './components/Loans';
import KYC from './components/KYC';
import Profile from './components/Profile';
import Notifications from './components/Notifications';
import Sidebar from './components/Sidebar';
import Header from './components/Header';

/**
 * FinSecure Customer Portal
 * Main application component with routing
 */
function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated
    const token = localStorage.getItem('accessToken');
    const userData = localStorage.getItem('user');
    
    if (token && userData) {
      setIsAuthenticated(true);
      setUser(JSON.parse(userData));
    }
    setLoading(false);
  }, []);

  const handleLogin = (loginData) => {
    localStorage.setItem('accessToken', loginData.accessToken);
    localStorage.setItem('refreshToken', loginData.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      userId: loginData.userId,
      email: loginData.email,
      role: loginData.role
    }));
    setIsAuthenticated(true);
    setUser({
      userId: loginData.userId,
      email: loginData.email,
      role: loginData.role
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <Router>
      <div className="App">
        {!isAuthenticated ? (
          <Routes>
            <Route path="/login" element={<Login onLogin={handleLogin} />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </Routes>
        ) : (
          <div className="app-container">
            <Sidebar onLogout={handleLogout} />
            <div className="main-content">
              <Header user={user} />
              <div className="content">
                <Routes>
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/accounts" element={<Accounts />} />
                  <Route path="/transactions" element={<Transactions />} />
                  <Route path="/cards" element={<Cards />} />
                  <Route path="/loans" element={<Loans />} />
                  <Route path="/kyc" element={<KYC />} />
                  <Route path="/profile" element={<Profile />} />
                  <Route path="/notifications" element={<Notifications />} />
                  <Route path="*" element={<Navigate to="/dashboard" />} />
                </Routes>
              </div>
            </div>
          </div>
        )}
      </div>
    </Router>
  );
}

export default App;
