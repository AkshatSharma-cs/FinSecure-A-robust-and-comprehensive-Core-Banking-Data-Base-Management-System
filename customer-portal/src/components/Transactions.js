import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Header from './Header';
import { customerAPI } from '../api';
import './Dashboard.css';

function Transactions() {
  const { accountId } = useParams();
  const [txns, setTxns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => { loadTxns(); }, [accountId, page]);

  const loadTxns = async () => {
    setLoading(true);
    try {
      const res = await customerAPI.getTransactions(accountId, page);
      const { content, totalPages } = res.data.data;
      setTxns(content || []);
      setTotalPages(totalPages || 0);
    } finally {
      setLoading(false);
    }
  };

  const fmt = (a) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(a);
  const fmtDate = (d) => new Date(d).toLocaleString('en-IN');

  return (
    <>
      <Header />
      <div className="page-container">
        <h1 className="page-title">Transaction History</h1>
        <div className="section-card">
          {loading ? <div className="loading">Loading transactions...</div> : (
            txns.length > 0 ? (
              <>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid #e2e8f0' }}>
                      {['Reference', 'Date', 'Description', 'Type', 'Mode', 'Amount', 'Balance'].map(h => (
                        <th key={h} style={{ padding: '12px 8px', textAlign: 'left', fontSize: 13, color: '#718096', fontWeight: 700 }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {txns.map(txn => (
                      <tr key={txn.id} style={{ borderBottom: '1px solid #f7fafc' }}>
                        <td style={{ padding: '12px 8px', fontSize: 12, color: '#718096' }}>{txn.referenceNumber}</td>
                        <td style={{ padding: '12px 8px', fontSize: 13 }}>{fmtDate(txn.createdAt)}</td>
                        <td style={{ padding: '12px 8px', fontSize: 14 }}>{txn.description || '-'}</td>
                        <td style={{ padding: '12px 8px' }}>
                          <span style={{ padding: '3px 8px', borderRadius: 4, fontSize: 12, fontWeight: 700,
                            background: txn.type === 'CREDIT' ? '#f0fff4' : '#fff5f5',
                            color: txn.type === 'CREDIT' ? '#276749' : '#c53030' }}>
                            {txn.type}
                          </span>
                        </td>
                        <td style={{ padding: '12px 8px', fontSize: 13, color: '#718096' }}>{txn.mode}</td>
                        <td style={{ padding: '12px 8px', fontWeight: 700, fontSize: 14,
                          color: txn.type === 'CREDIT' ? '#38a169' : '#e53e3e' }}>
                          {txn.type === 'CREDIT' ? '+' : '-'}{fmt(txn.amount)}
                        </td>
                        <td style={{ padding: '12px 8px', fontWeight: 600, fontSize: 14 }}>{fmt(txn.balanceAfter)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <div style={{ display: 'flex', justifyContent: 'center', gap: 12, marginTop: 24 }}>
                  <button className="btn-sm" onClick={() => setPage(p => p - 1)} disabled={page === 0}>Previous</button>
                  <span style={{ padding: '7px 16px', fontSize: 14 }}>Page {page + 1} of {totalPages}</span>
                  <button className="btn-sm" onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>Next</button>
                </div>
              </>
            ) : <div className="empty-state">No transactions found</div>
          )}
        </div>
      </div>
    </>
  );
}

export default Transactions;
