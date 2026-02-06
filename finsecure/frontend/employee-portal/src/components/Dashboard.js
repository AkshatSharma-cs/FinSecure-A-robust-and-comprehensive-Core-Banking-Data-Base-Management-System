import React, { useState, useEffect } from 'react';
import { employeeAPI } from '../utils/api';
import { Link } from 'react-router-dom';
import './Dashboard.css';

function EmployeeDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const response = await employeeAPI.getDashboard();
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

  const { employeeName, department, designation, pendingKycDocuments, pendingLoanApplications, pendingKycApprovals } = dashboard;

  return (
    <div className="employee-dashboard">
      <div className="dashboard-header">
        <h1>Welcome, {employeeName}</h1>
        <div className="employee-info">
          <span className="department">{department}</span>
          <span className="designation">{designation}</span>
        </div>
      </div>

      {/* Pending Work Stats */}
      <div className="stats-grid">
        <div className="stat-card priority-high">
          <div className="stat-icon">📄</div>
          <div className="stat-content">
            <p className="stat-label">Pending KYC Documents</p>
            <h2 className="stat-value">{pendingKycDocuments}</h2>
          </div>
          <Link to="/kyc-verification" className="stat-link">Review Now →</Link>
        </div>

        <div className="stat-card priority-medium">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <p className="stat-label">Pending KYC Approvals</p>
            <h2 className="stat-value">{pendingKycApprovals}</h2>
          </div>
          <Link to="/customers" className="stat-link">View Customers →</Link>
        </div>

        <div className="stat-card priority-low">
          <div className="stat-icon">💰</div>
          <div className="stat-content">
            <p className="stat-label">Loan Applications</p>
            <h2 className="stat-value">{pendingLoanApplications}</h2>
          </div>
          <Link to="/loans" className="stat-link">Process Loans →</Link>
        </div>
      </div>

      {/* Department-specific Quick Actions */}
      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="actions-grid">
          {(department === 'KYC_VERIFICATION' || department === 'MANAGEMENT') && (
            <Link to="/kyc-verification" className="action-card">
              <div className="action-icon">✅</div>
              <h3>Verify KYC</h3>
              <p>Review pending documents</p>
            </Link>
          )}

          <Link to="/customers/search" className="action-card">
            <div className="action-icon">🔍</div>
            <h3>Search Customer</h3>
            <p>Find customer details</p>
          </Link>

          {(department === 'LOAN_PROCESSING' || department === 'MANAGEMENT') && (
            <Link to="/loans" className="action-card">
              <div className="action-icon">📊</div>
              <h3>Process Loans</h3>
              <p>Approve/reject applications</p>
            </Link>
          )}

          <Link to="/reports" className="action-card">
            <div className="action-icon">📈</div>
            <h3>View Reports</h3>
            <p>Analytics and insights</p>
          </Link>
        </div>
      </div>

      {/* Recent Activity */}
      <div className="recent-activity">
        <h2>Recent Activity</h2>
        <p className="coming-soon">Activity log coming soon...</p>
      </div>
    </div>
  );
}

export default EmployeeDashboard;
