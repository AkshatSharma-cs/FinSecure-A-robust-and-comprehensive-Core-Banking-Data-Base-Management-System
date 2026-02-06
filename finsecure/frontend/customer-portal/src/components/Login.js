import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../utils/api';
import './Login.css';

function Login({ onLogin }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [showOtpInput, setShowOtpInput] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!showOtpInput) {
        // Step 1: Login with credentials
        const response = await authAPI.login({ email, password });
        
        if (response.data.success) {
          setShowOtpInput(true);
          alert('OTP sent to your email. Please check and enter it below.');
        }
      } else {
        // Step 2: Verify OTP
        const response = await authAPI.verifyOtp({
          email,
          otpCode: otp,
          otpType: 'LOGIN'
        });

        if (response.data.success) {
          onLogin(response.data.data);
          navigate('/dashboard');
        }
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>FinSecure</h1>
          <p>Customer Portal</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <h2>{showOtpInput ? 'Enter OTP' : 'Login'}</h2>

          {error && <div className="error-message">{error}</div>}

          {!showOtpInput ? (
            <>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="Enter your email"
                />
              </div>

              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="Enter your password"
                />
              </div>
            </>
          ) : (
            <div className="form-group">
              <label>OTP</label>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                required
                maxLength="6"
                placeholder="Enter 6-digit OTP"
              />
              <small>OTP sent to {email}</small>
            </div>
          )}

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Processing...' : (showOtpInput ? 'Verify OTP' : 'Login')}
          </button>

          {!showOtpInput && (
            <div className="form-footer">
              <p>Don't have an account? <Link to="/register">Register</Link></p>
            </div>
          )}

          {showOtpInput && (
            <button
              type="button"
              className="btn-secondary"
              onClick={() => setShowOtpInput(false)}
            >
              Back to Login
            </button>
          )}
        </form>
      </div>
    </div>
  );
}

export default Login;
