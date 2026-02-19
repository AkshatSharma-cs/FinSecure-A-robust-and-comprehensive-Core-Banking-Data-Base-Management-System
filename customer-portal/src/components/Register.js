import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api';
import './Login.css';

function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: '', username: '', password: '', firstName: '', lastName: '',
    phone: '', dateOfBirth: '', panNumber: '', address: '', city: '', state: '', pinCode: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setLoading(true);
    try {
      await authAPI.register(form);
      setSuccess('Registration successful! Please check your email to verify your account.');
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card" style={{ maxWidth: 560 }}>
        <div className="login-logo">
          <h1>Fin<span>Secure</span></h1>
          <p>Create Your Banking Account</p>
        </div>

        {error && <div className="error-msg">{error}</div>}
        {success && <div className="success-msg">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 20px' }}>
            <div className="form-group">
              <label>First Name</label>
              <input type="text" name="firstName" value={form.firstName} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input type="text" name="lastName" value={form.lastName} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" name="email" value={form.email} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Username</label>
              <input type="text" name="username" value={form.username} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input type="tel" name="phone" value={form.phone} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Date of Birth</label>
              <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={form.password} onChange={handleChange} required
              placeholder="Min 8 chars, uppercase, lowercase, digit, special char" />
          </div>
          <div className="form-group">
            <label>PAN Number (optional)</label>
            <input type="text" name="panNumber" value={form.panNumber} onChange={handleChange} maxLength="10" />
          </div>
          <div className="form-group">
            <label>Address</label>
            <input type="text" name="address" value={form.address} onChange={handleChange} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0 20px' }}>
            <div className="form-group">
              <label>City</label>
              <input type="text" name="city" value={form.city} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>State</label>
              <input type="text" name="state" value={form.state} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>PIN Code</label>
              <input type="text" name="pinCode" value={form.pinCode} onChange={handleChange} maxLength="6" />
            </div>
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <div className="login-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
