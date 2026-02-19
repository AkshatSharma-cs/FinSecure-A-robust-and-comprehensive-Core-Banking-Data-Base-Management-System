import React from 'react';
import { Link } from 'react-router-dom';
import Header from './Header';

function Dashboard() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const cards = [
    { title: 'Customer Management', icon: '👥', desc: 'View and search all registered customers', link: '/customers', color: '#1e40af' },
    { title: 'KYC Verification', icon: '🔍', desc: 'Review and approve KYC documents', link: '/kyc', color: '#7c3aed' },
    { title: 'Loan Approvals', icon: '💰', desc: 'Process pending loan applications', link: '/loans', color: '#059669' },
  ];

  return (
    <>
      <Header />
      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 24px' }}>
        <h1 style={{ fontSize: 26, fontWeight: 700, color: '#0f172a', marginBottom: 8 }}>
          Employee Dashboard
        </h1>
        <p style={{ color: '#64748b', marginBottom: 32 }}>Welcome back, {user.username}. Manage banking operations below.</p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 20 }}>
          {cards.map(card => (
            <Link key={card.title} to={card.link} style={{ textDecoration: 'none' }}>
              <div style={{ background: 'white', borderRadius: 14, padding: 28, boxShadow: '0 2px 8px rgba(0,0,0,0.07)', borderTop: `4px solid ${card.color}`, transition: 'transform 0.2s', cursor: 'pointer' }}
                onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-3px)'}
                onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}>
                <div style={{ fontSize: 40, marginBottom: 12 }}>{card.icon}</div>
                <div style={{ fontSize: 18, fontWeight: 700, color: '#0f172a', marginBottom: 8 }}>{card.title}</div>
                <div style={{ fontSize: 14, color: '#64748b', lineHeight: 1.5 }}>{card.desc}</div>
                <div style={{ marginTop: 16, fontSize: 13, fontWeight: 600, color: card.color }}>Go to {card.title} →</div>
              </div>
            </Link>
          ))}
        </div>

        <div style={{ background: 'white', borderRadius: 14, padding: 24, marginTop: 24, boxShadow: '0 2px 8px rgba(0,0,0,0.07)' }}>
          <h2 style={{ fontSize: 18, fontWeight: 700, color: '#0f172a', marginBottom: 12 }}>Quick Reference</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
            {[
              { label: 'OTP Threshold', value: '₹10,000+' },
              { label: 'KYC Docs Required', value: 'Min 2 docs' },
              { label: 'Credit Card Rule', value: 'KYC must be APPROVED' },
              { label: 'Support Email', value: 'support@finsecure.com' },
            ].map(item => (
              <div key={item.label} style={{ background: '#f8fafc', borderRadius: 8, padding: '12px 16px' }}>
                <div style={{ fontSize: 12, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4 }}>{item.label}</div>
                <div style={{ fontSize: 15, fontWeight: 700, color: '#0f172a' }}>{item.value}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

export default Dashboard;
