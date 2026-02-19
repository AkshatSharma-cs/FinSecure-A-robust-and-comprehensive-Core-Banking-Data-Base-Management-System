import React, { useState, useEffect } from 'react';
import Header from './Header';
import { customerAPI } from '../api';
import './Dashboard.css';

function Loans() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ loanType: 'PERSONAL', principalAmount: '', tenureMonths: '', purpose: '' });
  const [message, setMessage] = useState({ text: '', type: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { loadLoans(); }, []);

  const loadLoans = async () => {
    try {
      const res = await customerAPI.getLoans();
      setLoans(res.data.data || []);
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerAPI.applyLoan({
        ...form,
        principalAmount: parseFloat(form.principalAmount),
        tenureMonths: parseInt(form.tenureMonths)
      });
      setMessage({ text: 'Loan application submitted successfully!', type: 'success' });
      setShowModal(false);
      loadLoans();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Application failed.', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const fmt = (a) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(a || 0);

  const statusColor = (s) => ({
    APPLIED: '#d97706', UNDER_REVIEW: '#7c3aed', APPROVED: '#059669',
    REJECTED: '#dc2626', ACTIVE: '#2563eb', DISBURSED: '#059669', CLOSED: '#6b7280'
  }[s] || '#6b7280');

  if (loading) return <><Header /><div className="loading">Loading...</div></>;

  return (
    <>
      <Header />
      <div className="page-container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h1 className="page-title" style={{ margin: 0 }}>My Loans</h1>
          <button onClick={() => setShowModal(true)}
            style={{ padding: '10px 20px', background: '#3182ce', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14 }}>
            Apply for Loan
          </button>
        </div>

        {message.text && (
          <div className={message.type === 'success' ? 'success-msg' : 'error-msg'} style={{ marginBottom: 16 }}>
            {message.text}
          </div>
        )}

        <div style={{ display: 'grid', gap: 16 }}>
          {loans.map(loan => (
            <div key={loan.id} className="section-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ fontSize: 12, color: '#718096', fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1 }}>
                    {loan.loanType.replace('_', ' ')} LOAN
                  </div>
                  <div style={{ fontSize: 18, fontWeight: 700, color: '#1a202c', margin: '4px 0' }}>
                    {loan.loanNumber}
                  </div>
                  <div style={{ fontSize: 13, color: '#718096' }}>{loan.purpose}</div>
                </div>
                <span style={{ padding: '4px 12px', background: statusColor(loan.status) + '20', color: statusColor(loan.status), borderRadius: 20, fontSize: 12, fontWeight: 700 }}>
                  {loan.status}
                </span>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginTop: 20 }}>
                {[
                  { label: 'Principal', value: fmt(loan.principalAmount) },
                  { label: 'Interest Rate', value: loan.interestRate + '% p.a.' },
                  { label: 'Tenure', value: loan.tenureMonths + ' months' },
                  { label: 'EMI', value: fmt(loan.emiAmount) },
                  { label: 'Outstanding', value: fmt(loan.outstandingAmount) },
                  { label: 'Total Interest', value: fmt(loan.totalInterest) },
                ].map(item => (
                  <div key={item.label} style={{ background: '#f7fafc', borderRadius: 8, padding: '10px 14px' }}>
                    <div style={{ fontSize: 11, color: '#a0aec0', textTransform: 'uppercase', letterSpacing: 0.5 }}>{item.label}</div>
                    <div style={{ fontSize: 15, fontWeight: 700, color: '#2d3748', marginTop: 4 }}>{item.value}</div>
                  </div>
                ))}
              </div>
              {loan.rejectionReason && (
                <div style={{ marginTop: 12, padding: '10px 14px', background: '#fff5f5', borderRadius: 8, color: '#c53030', fontSize: 14 }}>
                  Rejection reason: {loan.rejectionReason}
                </div>
              )}
            </div>
          ))}
          {loans.length === 0 && <div className="empty-state">No loan applications found.</div>}
        </div>

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal" style={{ width: 480 }} onClick={e => e.stopPropagation()}>
              <h3 className="modal-title">Apply for a Loan</h3>
              <form onSubmit={handleApply}>
                <div className="form-group">
                  <label>Loan Type</label>
                  <select value={form.loanType} onChange={e => setForm({ ...form, loanType: e.target.value })}>
                    {['HOME', 'PERSONAL', 'CAR', 'EDUCATION', 'BUSINESS', 'GOLD'].map(t => (
                      <option key={t} value={t}>{t.replace('_', ' ')}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Loan Amount (₹)</label>
                  <input type="number" value={form.principalAmount} onChange={e => setForm({ ...form, principalAmount: e.target.value })}
                    placeholder="Min: 10,000 | Max: 1,00,00,000" min="10000" max="10000000" required />
                </div>
                <div className="form-group">
                  <label>Tenure (months)</label>
                  <input type="number" value={form.tenureMonths} onChange={e => setForm({ ...form, tenureMonths: e.target.value })}
                    placeholder="6 - 360 months" min="6" max="360" required />
                </div>
                <div className="form-group">
                  <label>Purpose</label>
                  <input type="text" value={form.purpose} onChange={e => setForm({ ...form, purpose: e.target.value })}
                    placeholder="Brief description of loan purpose" required />
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn-submit" disabled={submitting}>
                    {submitting ? 'Submitting...' : 'Apply Now'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default Loans;
