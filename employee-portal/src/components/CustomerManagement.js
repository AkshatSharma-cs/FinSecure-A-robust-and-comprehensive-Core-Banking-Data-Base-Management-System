import React, { useState, useEffect, useCallback } from 'react';
import Header from './Header';
import { employeeAPI } from '../api';

function CustomerManagement() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await employeeAPI.getCustomers(page, search);
      const { content, totalPages } = res.data.data;
      setCustomers(content || []);
      setTotalPages(totalPages || 0);
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => { loadCustomers(); }, [loadCustomers]);

  const kycColor = { PENDING: '#d97706', SUBMITTED: '#7c3aed', APPROVED: '#059669', REJECTED: '#dc2626' };

  return (
    <>
      <Header />
      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 24px' }}>
        <h1 style={{ fontSize: 26, fontWeight: 700, color: '#0f172a', marginBottom: 24 }}>Customer Management</h1>

        <div style={{ background: 'white', borderRadius: 14, padding: 20, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.07)' }}>
          <input type="text" placeholder="Search customers by name..." value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            style={{ width: '100%', padding: '12px 16px', border: '2px solid #e2e8f0', borderRadius: 8, fontSize: 15, outline: 'none' }} />
        </div>

        <div style={{ background: 'white', borderRadius: 14, boxShadow: '0 2px 8px rgba(0,0,0,0.07)', overflow: 'hidden' }}>
          {loading ? (
            <div style={{ padding: 48, textAlign: 'center', color: '#64748b' }}>Loading...</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead style={{ background: '#f8fafc' }}>
                <tr>
                  {['Name', 'Email', 'Phone', 'PAN', 'KYC Status', 'Joined'].map(h => (
                    <th key={h} style={{ padding: '14px 16px', textAlign: 'left', fontSize: 13, color: '#475569', fontWeight: 700, borderBottom: '2px solid #e2e8f0' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {customers.map(c => (
                  <tr key={c.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ fontWeight: 600, fontSize: 14, color: '#0f172a' }}>{c.firstName} {c.lastName}</div>
                      <div style={{ fontSize: 12, color: '#94a3b8' }}>@{c.username}</div>
                    </td>
                    <td style={{ padding: '14px 16px', fontSize: 13, color: '#334155' }}>{c.email}</td>
                    <td style={{ padding: '14px 16px', fontSize: 13, color: '#475569' }}>{c.phone}</td>
                    <td style={{ padding: '14px 16px', fontSize: 13, color: '#475569', fontFamily: 'monospace' }}>{c.panNumber || '-'}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '3px 10px', borderRadius: 20, fontSize: 12, fontWeight: 700, background: (kycColor[c.kycStatus] || '#94a3b8') + '20', color: kycColor[c.kycStatus] || '#94a3b8' }}>
                        {c.kycStatus}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', fontSize: 13, color: '#64748b' }}>
                      {new Date(c.createdAt).toLocaleDateString('en-IN')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {customers.length === 0 && !loading && (
            <div style={{ padding: 48, textAlign: 'center', color: '#94a3b8' }}>No customers found</div>
          )}
        </div>

        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 12, marginTop: 20 }}>
            <button onClick={() => setPage(p => p - 1)} disabled={page === 0}
              style={{ padding: '8px 16px', background: 'white', border: '1px solid #e2e8f0', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>Previous</button>
            <span style={{ padding: '8px 16px', fontSize: 13, color: '#64748b' }}>Page {page + 1} of {totalPages}</span>
            <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}
              style={{ padding: '8px 16px', background: 'white', border: '1px solid #e2e8f0', borderRadius: 8, cursor: 'pointer', fontSize: 13 }}>Next</button>
          </div>
        )}
      </div>
    </>
  );
}

export default CustomerManagement;
