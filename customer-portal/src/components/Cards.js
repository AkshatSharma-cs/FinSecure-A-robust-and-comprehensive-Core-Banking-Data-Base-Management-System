import React, { useState, useEffect } from 'react';
import Header from './Header';
import { customerAPI } from '../api';
import './Dashboard.css';

function Cards() {
  const [cards, setCards] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [showIssueModal, setShowIssueModal] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [cardsRes, dashRes] = await Promise.all([customerAPI.getCards(), customerAPI.getDashboard()]);
      setCards(cardsRes.data.data || []);
      setAccounts(dashRes.data.data?.accounts || []);
    } finally {
      setLoading(false);
    }
  };

  const handleCardAction = async (cardId, action) => {
    setSubmitting(true);
    try {
      await customerAPI.cardAction({ cardId, action });
      setMessage({ text: `Card ${action.toLowerCase()}ed successfully`, type: 'success' });
      loadData();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Action failed', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleIssueDebit = async () => {
    if (!selectedAccount) return;
    setSubmitting(true);
    try {
      await customerAPI.issueDebitCard(selectedAccount);
      setMessage({ text: 'Debit card issued successfully!', type: 'success' });
      setShowIssueModal(false);
      loadData();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed to issue card', type: 'error' });
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
          <h1 className="page-title" style={{ margin: 0 }}>My Cards</h1>
          <button className="btn-submit" style={{ padding: '10px 20px', background: '#3182ce', color: 'white', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600 }}
            onClick={() => setShowIssueModal(true)}>
            + Issue Debit Card
          </button>
        </div>

        {message.text && <div className={message.type === 'success' ? 'success-msg' : 'error-msg'} style={{ marginBottom: 16 }}>{message.text}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 20 }}>
          {cards.map(card => (
            <div key={card.id} style={{ background: card.cardType === 'CREDIT' ? 'linear-gradient(135deg, #1a365d, #2b6cb0)' : 'linear-gradient(135deg, #276749, #38a169)', borderRadius: 16, padding: 24, color: 'white', boxShadow: '0 8px 24px rgba(0,0,0,0.2)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
                <div>
                  <div style={{ fontSize: 12, opacity: 0.8, textTransform: 'uppercase', letterSpacing: 1 }}>FinSecure Bank</div>
                  <div style={{ fontSize: 14, fontWeight: 700 }}>{card.cardType} CARD</div>
                </div>
                <span style={{ padding: '4px 10px', background: card.status === 'ACTIVE' ? 'rgba(255,255,255,0.25)' : 'rgba(239,68,68,0.5)', borderRadius: 20, fontSize: 12, fontWeight: 600 }}>
                  {card.status}
                </span>
              </div>
              <div style={{ fontSize: 20, letterSpacing: 3, fontWeight: 700, marginBottom: 20 }}>
                {card.maskedCardNumber}
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                <div>
                  <div style={{ fontSize: 11, opacity: 0.7 }}>CARD HOLDER</div>
                  <div style={{ fontSize: 14, fontWeight: 600 }}>{card.cardHolderName}</div>
                </div>
                <div>
                  <div style={{ fontSize: 11, opacity: 0.7 }}>EXPIRES</div>
                  <div style={{ fontSize: 14, fontWeight: 600 }}>{card.expiryDate?.substring(0, 7)}</div>
                </div>
              </div>
              {card.cardType === 'CREDIT' && (
                <div style={{ background: 'rgba(255,255,255,0.1)', borderRadius: 8, padding: '10px 12px', marginBottom: 12 }}>
                  <span style={{ fontSize: 12, opacity: 0.8 }}>Credit Limit: </span>
                  <span style={{ fontWeight: 700 }}>₹{card.creditLimit?.toLocaleString('en-IN')}</span>
                  <span style={{ fontSize: 12, opacity: 0.8, marginLeft: 12 }}>Available: </span>
                  <span style={{ fontWeight: 700 }}>₹{card.availableLimit?.toLocaleString('en-IN')}</span>
                </div>
              )}
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {card.status === 'ACTIVE' && (
                  <button onClick={() => handleCardAction(card.id, 'BLOCK')} disabled={submitting}
                    style={{ padding: '6px 12px', background: 'rgba(239,68,68,0.3)', color: 'white', border: '1px solid rgba(255,255,255,0.3)', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}>
                    Block Card
                  </button>
                )}
                {card.status === 'BLOCKED' && (
                  <button onClick={() => handleCardAction(card.id, 'UNBLOCK')} disabled={submitting}
                    style={{ padding: '6px 12px', background: 'rgba(72,187,120,0.3)', color: 'white', border: '1px solid rgba(255,255,255,0.3)', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}>
                    Unblock Card
                  </button>
                )}
                <button onClick={() => handleCardAction(card.id, card.internationalEnabled ? 'DISABLE_INTERNATIONAL' : 'ENABLE_INTERNATIONAL')} disabled={submitting}
                  style={{ padding: '6px 12px', background: 'rgba(255,255,255,0.15)', color: 'white', border: '1px solid rgba(255,255,255,0.3)', borderRadius: 6, cursor: 'pointer', fontSize: 12 }}>
                  {card.internationalEnabled ? 'Disable' : 'Enable'} Intl
                </button>
              </div>
            </div>
          ))}
          {cards.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}>No cards found. Issue your first debit card!</div>}
        </div>

        {showIssueModal && (
          <div className="modal-overlay" onClick={() => setShowIssueModal(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <h3 className="modal-title">Issue Debit Card</h3>
              <div className="form-group">
                <label>Select Account</label>
                <select value={selectedAccount} onChange={e => setSelectedAccount(e.target.value)}>
                  <option value="">Select an account</option>
                  {accounts.map(a => <option key={a.id} value={a.id}>{a.accountNumber} - {a.accountType}</option>)}
                </select>
              </div>
              <div className="modal-actions">
                <button className="btn-cancel" onClick={() => setShowIssueModal(false)}>Cancel</button>
                <button className="btn-submit" onClick={handleIssueDebit} disabled={submitting || !selectedAccount}>
                  {submitting ? 'Issuing...' : 'Issue Card'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default Cards;
