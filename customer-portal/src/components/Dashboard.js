import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Header from './Header';
import { customerAPI } from '../api';
import './Dashboard.css';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const res = await customerAPI.getDashboard();
      setDashboard(res.data.data);
    } catch {
      setError('Failed to load dashboard. Please refresh.');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount || 0);

  const formatDate = (dateStr) =>
    new Date(dateStr).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });

  if (loading) return <><Header /><div className="loading">Loading your dashboard...</div></>;

  return (
    <>
      <Header />
      <div className="page-container">
        <h1 className="page-title">
          Welcome back, {dashboard?.profile?.firstName || 'Customer'} 👋
        </h1>

        {dashboard?.profile?.kycStatus !== 'APPROVED' && (
          <div className="kyc-banner">
            <span style={{ fontSize: 24 }}>⚠️</span>
            <div className="kyc-banner-text">
              <strong>KYC Verification Pending</strong> — Complete your KYC to unlock all banking features including credit cards and loans.
              <Link to="/profile" style={{ color: '#d97706', marginLeft: 8, fontWeight: 600 }}>Complete KYC →</Link>
            </div>
          </div>
        )}

        {error && <div className="error-msg">{error}</div>}

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Total Balance</div>
            <div className="stat-value">{formatCurrency(dashboard?.totalBalance)}</div>
            <div className="stat-sub">Across all accounts</div>
          </div>
          <div className="stat-card green">
            <div className="stat-label">Accounts</div>
            <div className="stat-value">{dashboard?.totalAccounts || 0}</div>
            <div className="stat-sub">Active accounts</div>
          </div>
          <div className="stat-card purple">
            <div className="stat-label">Active Loans</div>
            <div className="stat-value">{dashboard?.activeLoans || 0}</div>
            <div className="stat-sub">Running loans</div>
          </div>
          <div className="stat-card orange">
            <div className="stat-label">Notifications</div>
            <div className="stat-value">{dashboard?.unreadNotifications || 0}</div>
            <div className="stat-sub">Unread alerts</div>
          </div>
        </div>

        <div className="section-card">
          <div className="section-header">
            <span className="section-title">Your Accounts</span>
            <Link to="/accounts" className="btn-sm">View All</Link>
          </div>
          {dashboard?.accounts?.length > 0 ? dashboard.accounts.map(acc => (
            <div className="account-row" key={acc.id}>
              <div>
                <div className="account-type">{acc.accountType.replace('_', ' ')}</div>
                <div className="account-number">{acc.accountNumber} • {acc.ifscCode}</div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div className="account-balance">{formatCurrency(acc.balance)}</div>
                <Link to={`/transactions/${acc.id}`} className="btn-sm" style={{ fontSize: 12 }}>
                  View Transactions
                </Link>
              </div>
            </div>
          )) : (
            <div className="empty-state">
              No accounts found. <Link to="/accounts">Open an account</Link>
            </div>
          )}
        </div>

        <div className="section-card">
          <div className="section-header">
            <span className="section-title">Recent Transactions</span>
          </div>
          {dashboard?.recentTransactions?.length > 0 ? dashboard.recentTransactions.map(txn => (
            <div className="txn-row" key={txn.id}>
              <div>
                <div className="txn-desc">{txn.description || txn.mode}</div>
                <div className="txn-ref">{txn.referenceNumber} • {formatDate(txn.createdAt)}</div>
              </div>
              <div className={`txn-amount ${txn.type.toLowerCase()}`}>
                {txn.type === 'CREDIT' ? '+' : '-'}{formatCurrency(txn.amount)}
              </div>
            </div>
          )) : (
            <div className="empty-state">No recent transactions</div>
          )}
        </div>
      </div>
    </>
  );
}

export default Dashboard;
