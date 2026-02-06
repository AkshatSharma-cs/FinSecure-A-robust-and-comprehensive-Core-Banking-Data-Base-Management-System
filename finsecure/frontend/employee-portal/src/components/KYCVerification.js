import React, { useState, useEffect } from 'react';
import { employeeAPI, authAPI } from '../utils/api';
import './KYCVerification.css';

function KYCVerification() {
  const [pendingKyc, setPendingKyc] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [customerDocuments, setCustomerDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');

  // Verification form
  const [selectedDoc, setSelectedDoc] = useState(null);
  const [verificationStatus, setVerificationStatus] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [otp, setOtp] = useState('');
  const [showOtpInput, setShowOtpInput] = useState(false);

  useEffect(() => {
    fetchPendingKyc();
  }, []);

  const fetchPendingKyc = async () => {
    try {
      const response = await employeeAPI.getPendingKyc();
      if (response.data.success) {
        setPendingKyc(response.data.data);
      }
    } catch (err) {
      setError('Failed to load pending KYC');
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomerDocuments = async (customerId) => {
    try {
      const response = await employeeAPI.getCustomerKyc(customerId);
      if (response.data.success) {
        setSelectedCustomer(response.data.data.customer);
        setCustomerDocuments(response.data.data.documents);
      }
    } catch (err) {
      setError('Failed to load customer documents');
    }
  };

  const handleRequestOtp = async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      await authAPI.requestOtp({
        email: user.email,
        otpType: 'KYC_CONFIRMATION'
      });
      setShowOtpInput(true);
      alert('OTP sent to your email');
    } catch (err) {
      setError('Failed to send OTP');
    }
  };

  const handleVerifyDocument = async () => {
    if (!otp) {
      alert('Please enter OTP');
      return;
    }

    if (verificationStatus === 'REJECTED' && !rejectionReason) {
      alert('Please provide rejection reason');
      return;
    }

    setProcessing(true);
    setError('');

    try {
      const user = JSON.parse(localStorage.getItem('user'));
      
      // Verify OTP first
      await authAPI.verifyOtp({
        email: user.email,
        otpCode: otp,
        otpType: 'KYC_CONFIRMATION'
      });

      // Then verify KYC
      await employeeAPI.verifyKyc({
        kycId: selectedDoc.kycId,
        status: verificationStatus,
        rejectionReason: verificationStatus === 'REJECTED' ? rejectionReason : null,
        employeeId: user.userId
      });

      alert('KYC verification completed successfully');
      
      // Reset form
      setSelectedDoc(null);
      setVerificationStatus('');
      setRejectionReason('');
      setOtp('');
      setShowOtpInput(false);
      
      // Refresh data
      fetchPendingKyc();
      if (selectedCustomer) {
        fetchCustomerDocuments(selectedCustomer.customerId);
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Verification failed');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading KYC documents...</div>;
  }

  return (
    <div className="kyc-verification">
      <div className="page-header">
        <h1>KYC Verification</h1>
        <p>Review and verify customer KYC documents</p>
      </div>

      <div className="kyc-container">
        {/* Pending Documents List */}
        <div className="pending-list">
          <h2>Pending Documents ({pendingKyc.length})</h2>
          {pendingKyc.length === 0 ? (
            <p className="no-data">No pending KYC documents</p>
          ) : (
            <div className="document-list">
              {pendingKyc.map((doc) => (
                <div
                  key={doc.kycId}
                  className={`document-item ${selectedDoc?.kycId === doc.kycId ? 'active' : ''}`}
                  onClick={() => {
                    setSelectedDoc(doc);
                    fetchCustomerDocuments(doc.customerId);
                  }}
                >
                  <div className="doc-icon">📄</div>
                  <div className="doc-info">
                    <h4>{doc.documentType}</h4>
                    <p>Customer ID: {doc.customerId}</p>
                    <small>{new Date(doc.uploadDate).toLocaleDateString()}</small>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Document Verification Panel */}
        <div className="verification-panel">
          {selectedDoc ? (
            <>
              <h2>Verify Document</h2>
              
              {selectedCustomer && (
                <div className="customer-info">
                  <h3>Customer Information</h3>
                  <div className="info-grid">
                    <div><strong>Name:</strong> {selectedCustomer.firstName} {selectedCustomer.lastName}</div>
                    <div><strong>Email:</strong> {selectedCustomer.email}</div>
                    <div><strong>Phone:</strong> {selectedCustomer.phone}</div>
                    <div><strong>Account Type:</strong> {selectedCustomer.accountType}</div>
                    <div><strong>KYC Status:</strong> {selectedCustomer.kycStatus}</div>
                  </div>
                </div>
              )}

              <div className="document-details">
                <h3>Document Details</h3>
                <div className="detail-item">
                  <strong>Document Type:</strong> {selectedDoc.documentType}
                </div>
                <div className="detail-item">
                  <strong>Document Number:</strong> {selectedDoc.documentNumber || 'N/A'}
                </div>
                <div className="detail-item">
                  <strong>Upload Date:</strong> {new Date(selectedDoc.uploadDate).toLocaleString()}
                </div>
                <div className="detail-item">
                  <strong>Document:</strong>
                  <a href={selectedDoc.documentUrl} target="_blank" rel="noopener noreferrer" className="btn-link">
                    View Document
                  </a>
                </div>
              </div>

              <div className="verification-form">
                <h3>Verification Decision</h3>
                
                <div className="form-group">
                  <label>Status</label>
                  <select
                    value={verificationStatus}
                    onChange={(e) => setVerificationStatus(e.target.value)}
                    required
                  >
                    <option value="">Select status</option>
                    <option value="APPROVED">Approve</option>
                    <option value="REJECTED">Reject</option>
                  </select>
                </div>

                {verificationStatus === 'REJECTED' && (
                  <div className="form-group">
                    <label>Rejection Reason</label>
                    <textarea
                      value={rejectionReason}
                      onChange={(e) => setRejectionReason(e.target.value)}
                      placeholder="Provide detailed reason for rejection"
                      rows="4"
                      required
                    />
                  </div>
                )}

                {!showOtpInput ? (
                  <button
                    className="btn-primary"
                    onClick={handleRequestOtp}
                    disabled={!verificationStatus}
                  >
                    Request OTP
                  </button>
                ) : (
                  <>
                    <div className="form-group">
                      <label>Enter OTP</label>
                      <input
                        type="text"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value)}
                        maxLength="6"
                        placeholder="6-digit OTP"
                        required
                      />
                    </div>

                    <div className="form-actions">
                      <button
                        className={`btn-${verificationStatus === 'APPROVED' ? 'success' : 'danger'}`}
                        onClick={handleVerifyDocument}
                        disabled={processing}
                      >
                        {processing ? 'Processing...' : `Confirm ${verificationStatus}`}
                      </button>
                      <button
                        className="btn-secondary"
                        onClick={() => {
                          setShowOtpInput(false);
                          setOtp('');
                        }}
                      >
                        Cancel
                      </button>
                    </div>
                  </>
                )}
              </div>

              {error && <div className="error-message">{error}</div>}

              {/* All Customer Documents */}
              {customerDocuments.length > 0 && (
                <div className="all-documents">
                  <h3>All Customer Documents</h3>
                  <div className="documents-grid">
                    {customerDocuments.map((doc) => (
                      <div key={doc.kycId} className={`doc-card status-${doc.verificationStatus.toLowerCase()}`}>
                        <h4>{doc.documentType}</h4>
                        <p className="status">{doc.verificationStatus}</p>
                        <a href={doc.documentUrl} target="_blank" rel="noopener noreferrer">
                          View
                        </a>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="no-selection">
              <p>Select a document from the list to verify</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default KYCVerification;
