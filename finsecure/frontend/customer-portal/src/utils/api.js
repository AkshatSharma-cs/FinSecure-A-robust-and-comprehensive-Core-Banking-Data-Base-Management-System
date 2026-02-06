import axios from 'axios';

const API_BASE_URL = 'https://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken } = response.data;
        localStorage.setItem('accessToken', accessToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed, logout user
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// ============ Authentication APIs ============

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  verifyOtp: (data) => api.post('/auth/verify-otp', data),
  requestOtp: (data) => api.post('/auth/request-otp', data),
  logout: () => api.post('/auth/logout'),
};

// ============ Customer APIs ============

export const customerAPI = {
  getDashboard: () => api.get('/customer/dashboard'),
  getProfile: () => api.get('/customer/profile'),
  updateProfile: (data) => api.put('/customer/profile', data),
  
  // Accounts
  getAccounts: () => api.get('/customer/accounts'),
  getAccountTransactions: (accountId, params) => 
    api.get(`/customer/accounts/${accountId}/transactions`, { params }),
  
  // Transactions
  makeTransaction: (data) => api.post('/customer/transactions', data),
  
  // Cards
  getCards: () => api.get('/customer/cards'),
  cardAction: (data) => api.post('/customer/cards/action', data),
  
  // Loans
  getLoans: () => api.get('/customer/loans'),
  applyLoan: (data) => api.post('/customer/loans/apply', data),
  
  // KYC
  uploadKyc: (data) => api.post('/customer/kyc/upload', data),
  getKycDocuments: () => api.get('/customer/kyc/documents'),
  
  // Notifications
  getNotifications: () => api.get('/customer/notifications'),
  markNotificationRead: (notificationId) => 
    api.put(`/customer/notifications/${notificationId}/read`),
};

// ============ Employee APIs ============

export const employeeAPI = {
  getDashboard: () => api.get('/employee/dashboard'),
  
  // KYC Management
  getPendingKyc: () => api.get('/employee/kyc/pending'),
  getCustomerKyc: (customerId) => api.get(`/employee/kyc/customer/${customerId}`),
  verifyKyc: (data) => api.post('/employee/kyc/verify', data),
  
  // Customer Management
  getPendingKycCustomers: () => api.get('/employee/customers/pending-kyc'),
  getCustomerDetails: (customerId) => api.get(`/employee/customers/${customerId}`),
  searchCustomers: (query) => api.get('/employee/customers/search', { params: { query } }),
  
  // Loan Management
  getPendingLoans: () => api.get('/employee/loans/pending'),
  approveLoan: (data) => api.post('/employee/loans/approve', data),
  rejectLoan: (data) => api.post('/employee/loans/reject', data),
};

export default api;
