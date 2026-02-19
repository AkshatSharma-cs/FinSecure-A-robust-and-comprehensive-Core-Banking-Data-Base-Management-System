import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Header from './Header';
import { customerAPI } from '../api';
import './Accounts.css';
import './Dashboard.css';

function Accounts() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showTransferModal, setShowTransferModal] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [form, setForm] = useState({ accountType: 'SAVINGS' });
  const [transferForm, setTransferForm] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '', mode: 'NEFT', description: '' });
  const [message, setMessage] = useState({ text: '', type: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const res = await customerAPI.getDashboard();
      setDashboard(res.data.data);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (a) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(a || 0);

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerAPI.createAccount(form);
      setMessage({ text: 'Account created successfully!', type: 'success' });
      setShowModal(false);
      loadData();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed to create account.', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerAPI.transfer({ ...transferForm, amount: parseFloat(transferForm.amount) });
      setMessage({ text: 'Transfer successful!', type: 'success' });
      setShowTransferModal(false);
      loadData();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Transfer failed.', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <><Header /><div className="loading">Loading...</div></>;

  return (
    <>
      <Header />
      <div className="page-container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h1 className="page-title" style={{ margin: 0 }}>My Accounts</h1>
          <div style={{ display: 'flex', gap: 12 }}>
            <button className="btn-submit" onClick={() => setShowTransferModal(true)}>Transfer Funds</button>
            <button className="btn-submit" style={{ background: '#38a169' }} onClick={() => setShowModal(true)}>+ Open Account</button>
          </div>
        </div>

        {message.text && (
          <div className={message.type === 'success' ? 'success-msg' : 'error-msg'} style={{ marginBottom: 20 }}>
            {message.text}
          </div>
        )}

        <div className="accounts-grid">
          {dashboard?.accounts?.map(acc => (
            <div className="account-card" key={acc.id}>
              <div className="account-card-type">{acc.accountType.replace('_', ' ')}</div>
              <div className="account-card-number">{acc.accountNumber}</div>
              <div className="account-card-balance">{formatCurrency(acc.balance)}</div>
              <div className="account-card-details">IFSC: {acc.ifscCode}</div>
              <div className="account-card-details">Branch: {acc.branchName}</div>
              <div className="account-card-details">Status: <span style={{ color: acc.status === 'ACTIVE' ? '#38a169' : '#e53e3e', fontWeight: 600 }}>{acc.status}</span></div>
              <div className="account-card-actions">
                <Link to={`/transactions/${acc.id}`} className="btn-sm">Transactions</Link>
                <button className="btn-sm" onClick={() => { setSelectedAccount(acc); setTransferForm({ ...transferForm, fromAccountNumber: acc.accountNumber }); setShowTransferModal(true); }}>
                  Transfer
                </button>
              </div>
            </div>
          ))}
          {(!dashboard?.accounts || dashboard.accounts.length === 0) && (
            <div className="empty-state" style={{ gridColumn: '1/-1' }}>
              No accounts found. Open your first account!
            </div>
          )}
        </div>

        {/* Create Account Modal */}
        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <h3 className="modal-title">Open New Account</h3>
              <form onSubmit={handleCreateAccount}>
                <div className="form-group">
                  <label>Account Type</label>
                  <select value={form.accountType} onChange={e => setForm({ ...form, accountType: e.target.value })}>
                    <option value="SAVINGS">Savings Account</option>
                    <option value="CURRENT">Current Account</option>
                    <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                    <option value="RECURRING_DEPOSIT">Recurring Deposit</option>
                  </select>
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn-submit" disabled={submitting}>
                    {submitting ? 'Creating...' : 'Create Account'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Transfer Modal */}
        {showTransferModal && (
          <div className="modal-overlay" onClick={() => setShowTransferModal(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <h3 className="modal-title">Fund Transfer</h3>
              <form onSubmit={handleTransfer}>
                <div className="form-group">
                  <label>From Account</label>
                  <select value={transferForm.fromAccountNumber} onChange={e => setTransferForm({ ...transferForm, fromAccountNumber: e.target.value })}>
                    {dashboard?.accounts?.map(a => <option key={a.id} value={a.accountNumber}>{a.accountNumber}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>To Account Number</label>
                  <input type="text" value={transferForm.toAccountNumber} onChange={e => setTransferForm({ ...transferForm, toAccountNumber: e.target.value })} placeholder="Beneficiary account number" required />
                </div>
                <div className="form-group">
                  <label>Amount (₹)</label>
                  <input type="number" value={transferForm.amount} onChange={e => setTransferForm({ ...transferForm, amount: e.target.value })} placeholder="Enter amount" min="1" required />
                </div>
                <div className="form-group">
                  <label>Transfer Mode</label>
                  <select value={transferForm.mode} onChange={e => setTransferForm({ ...transferForm, mode: e.target.value })}>
                    <option>NEFT</option><option>RTGS</option><option>IMPS</option><option>UPI</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <input type="text" value={transferForm.description} onChange={e => setTransferForm({ ...transferForm, description: e.target.value })} placeholder="Optional description" />
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn-cancel" onClick={() => setShowTransferModal(false)}>Cancel</button>
                  <button type="submit" className="btn-submit" disabled={submitting}>{submitting ? 'Processing...' : 'Transfer'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default Accounts;
