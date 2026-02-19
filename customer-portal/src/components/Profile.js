import React, { useState, useEffect } from 'react';
import Header from './Header';
import { customerAPI } from '../api';
import './Dashboard.css';

function Profile() {
  const [profile, setProfile] = useState(null);
  const [kycDocs, setKycDocs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showKycModal, setShowKycModal] = useState(false);
  const [kycForm, setKycForm] = useState({ documentType: 'AADHAAR', documentNumber: '', filePath: '', fileName: '' });
  const [message, setMessage] = useState({ text: '', type: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [profileRes, kycRes] = await Promise.all([customerAPI.getProfile(), customerAPI.getKycDocuments()]);
      setProfile(profileRes.data.data);
      setKycDocs(kycRes.data.data || []);
    } finally {
      setLoading(false);
    }
  };

  const handleKycSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await customerAPI.uploadKyc(kycForm);
      setMessage({ text: 'KYC document uploaded successfully!', type: 'success' });
      setShowKycModal(false);
      loadData();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Upload failed.', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const kycBadgeColor = (status) => ({
    UPLOADED: '#7c3aed', UNDER_REVIEW: '#d97706', APPROVED: '#059669', REJECTED: '#dc2626'
  }[status] || '#6b7280');

  if (loading) return <><Header /><div className="loading">Loading...</div></>;

  return (
    <>
      <Header />
      <div className="page-container">
        <h1 className="page-title">My Profile</h1>

        {message.text && <div className={message.type === 'success' ? 'success-msg' : 'error-msg'} style={{ marginBottom: 16 }}>{message.text}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          <div className="section-card">
            <div className="section-title" style={{ marginBottom: 20 }}>Personal Information</div>
            {profile && (
              <div style={{ display: 'grid', gap: 14 }}>
                {[
                  { label: 'Full Name', value: profile.firstName + ' ' + profile.lastName },
                  { label: 'Email', value: profile.email },
                  { label: 'Username', value: profile.username },
                  { label: 'Phone', value: profile.phone },
                  { label: 'Date of Birth', value: profile.dateOfBirth },
                  { label: 'PAN Number', value: profile.panNumber || 'Not provided' },
                  { label: 'Address', value: [profile.address, profile.city, profile.state, profile.pinCode].filter(Boolean).join(', ') || 'Not provided' },
                ].map(item => (
                  <div key={item.label} style={{ display: 'flex', gap: 12 }}>
                    <span style={{ minWidth: 130, fontSize: 13, color: '#718096', fontWeight: 600 }}>{item.label}:</span>
                    <span style={{ fontSize: 14, color: '#1a202c', fontWeight: 500 }}>{item.value}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <div className="section-card" style={{ marginBottom: 20 }}>
              <div className="section-title" style={{ marginBottom: 16 }}>KYC Status</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
                <span style={{ fontSize: 32 }}>{profile?.kycStatus === 'APPROVED' ? '✅' : profile?.kycStatus === 'REJECTED' ? '❌' : '⏳'}</span>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 16, color: kycBadgeColor(profile?.kycStatus) }}>{profile?.kycStatus}</div>
                  <div style={{ fontSize: 13, color: '#718096' }}>
                    {profile?.kycStatus === 'APPROVED' ? 'All features unlocked' :
                     profile?.kycStatus === 'SUBMITTED' ? 'Under review by our team' :
                     profile?.kycStatus === 'REJECTED' ? 'Please resubmit with correct documents' :
                     'Submit KYC documents to unlock features'}
                  </div>
                </div>
              </div>
              <button onClick={() => setShowKycModal(true)}
                style={{ padding: '10px 20px', background: '#3182ce', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14, width: '100%' }}>
                + Upload KYC Document
              </button>
            </div>

            <div className="section-card">
              <div className="section-title" style={{ marginBottom: 16 }}>Submitted Documents</div>
              {kycDocs.length > 0 ? kycDocs.map(doc => (
                <div key={doc.id} style={{ padding: '12px 0', borderBottom: '1px solid #f7fafc', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, color: '#2d3748' }}>{doc.documentType.replace('_', ' ')}</div>
                    <div style={{ fontSize: 12, color: '#718096' }}>{doc.documentNumber}</div>
                  </div>
                  <span style={{ padding: '3px 10px', background: kycBadgeColor(doc.status) + '20', color: kycBadgeColor(doc.status), borderRadius: 20, fontSize: 12, fontWeight: 700 }}>
                    {doc.status}
                  </span>
                </div>
              )) : <div className="empty-state" style={{ padding: '20px 0' }}>No documents uploaded yet</div>}
            </div>
          </div>
        </div>

        {showKycModal && (
          <div className="modal-overlay" onClick={() => setShowKycModal(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <h3 className="modal-title">Upload KYC Document</h3>
              <form onSubmit={handleKycSubmit}>
                <div className="form-group">
                  <label>Document Type</label>
                  <select value={kycForm.documentType} onChange={e => setKycForm({ ...kycForm, documentType: e.target.value })}>
                    {['AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'VOTER_ID', 'UTILITY_BILL', 'BANK_STATEMENT', 'SALARY_SLIP'].map(t => (
                      <option key={t} value={t}>{t.replace('_', ' ')}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Document Number</label>
                  <input type="text" value={kycForm.documentNumber} onChange={e => setKycForm({ ...kycForm, documentNumber: e.target.value })} required />
                </div>
                <div className="form-group">
                  <label>File Path / URL</label>
                  <input type="text" value={kycForm.filePath} onChange={e => setKycForm({ ...kycForm, filePath: e.target.value })}
                    placeholder="/uploads/doc.pdf or https://..." required />
                </div>
                <div className="form-group">
                  <label>File Name</label>
                  <input type="text" value={kycForm.fileName} onChange={e => setKycForm({ ...kycForm, fileName: e.target.value })}
                    placeholder="document.pdf" />
                </div>
                <div className="modal-actions">
                  <button type="button" className="btn-cancel" onClick={() => setShowKycModal(false)}>Cancel</button>
                  <button type="submit" className="btn-submit" disabled={submitting}>
                    {submitting ? 'Uploading...' : 'Upload Document'}
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

export default Profile;
