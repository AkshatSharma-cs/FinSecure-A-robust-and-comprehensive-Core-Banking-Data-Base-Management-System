import React, { useState, useEffect } from 'react';
import { customerAPI } from '../utils/api';
import { Link } from 'react-router-dom';
import './Dashboard.css';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const response = await customerAPI.getDashboard();
      if (response.data.success) {
        setDashboard(response.data.data);
      }
    } catch (err) {
      setError('Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  const { customer, totalAccounts, totalBalance, totalCards, totalLoans, unreadNotifications } = dashboard;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Welcome, {customer.firstName}!</h1>
        <p>Your banking overview</p>
      </div>

      {/* KYC Status Banner */}
      {customer.kycStatus !== 'APPROVED' && (
        <div className={`kyc-banner kyc-${customer.kycStatus.toLowerCase()}`}>
          <div className="kyc-icon">⚠️</div>
          <div className="kyc-content">
            <h3>KYC Status: {customer.kycStatus}</h3>
            <p>
              {customer.kycStatus === 'PENDING' && 'Your KYC is under review. You will be notified once approved.'}
              {customer.kycStatus === 'REJECTED' && 'Your KYC was rejected. Please re-upload valid documents.'}
              {customer.kycStatus === 'INCOMPLETE' && 'Please complete your KYC verification to activate your account.'}
            </p>
            <Link to="/kyc" className="btn-link">
              {customer.kycStatus === 'PENDING' ? 'View Status' : 'Upload Documents'}
            </Link>
          </div>
        </div>
      )}

      {/* Quick Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">💰</div>
          <div className="stat-content">
            <p className="stat-label">Total Balance</p>
            <h2 className="stat-value">₹{totalBalance.toLocaleString()}</h2>
          </div>
          <Link to="/accounts" className="stat-link">View Accounts →</Link>
        </div>

        <div className="stat-card">
          <div className="stat-icon">🏦</div>
          <div className="stat-content">
            <p className="stat-label">Active Accounts</p>
            <h2 className="stat-value">{totalAccounts}</h2>
          </div>
          <Link to="/accounts" className="stat-link">Manage →</Link>
        </div>

        <div className="stat-card">
          <div className="stat-icon">💳</div>
          <div className="stat-content">
            <p className="stat-label">Cards</p>
            <h2 className="stat-value">{totalCards}</h2>
          </div>
          <Link to="/cards" className="stat-link">View Cards →</Link>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <p className="stat-label">Active Loans</p>
            <h2 className="stat-value">{totalLoans}</h2>
          </div>
          <Link to="/loans" className="stat-link">View Loans →</Link>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="actions-grid">
          <Link to="/transactions" className="action-card">
            <div className="action-icon">💸</div>
            <h3>Transfer Money</h3>
            <p>Send money to any account</p>
          </Link>

          <Link to="/cards" className="action-card">
            <div className="action-icon">🔒</div>
            <h3>Block Card</h3>
            <p>Secure your card instantly</p>
          </Link>

          <Link to="/loans" className="action-card">
            <div className="action-icon">📈</div>
            <h3>Apply for Loan</h3>
            <p>Get instant loan approval</p>
          </Link>

          <Link to="/profile" className="action-card">
            <div className="action-icon">👤</div>
            <h3>Update Profile</h3>
            <p>Manage your information</p>
          </Link>
        </div>
      </div>

      {/* Notifications */}
      {unreadNotifications > 0 && (
        <div className="dashboard-notifications">
          <div className="notification-header">
            <h2>Notifications</h2>
            <span className="notification-badge">{unreadNotifications} new</span>
          </div>
          <Link to="/notifications" className="btn-secondary">
            View All Notifications
          </Link>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
