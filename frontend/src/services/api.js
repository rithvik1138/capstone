import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and redirect to login on 401 Unauthorized
      if (window.location.pathname !== '/login' && window.location.pathname !== '/signup') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  signup: (userData) => api.post('/auth/signup', userData),
  getMe: () => api.get('/auth/me'),
};

export const deviceService = {
  getDevices: (params) => api.get('/devices', { params }),
  getMyDevices: (params) => api.get('/devices', { params }),
  getDeviceById: (id) => api.get(`/devices/${id}`),
  registerDevice: (deviceData) => api.post('/devices', deviceData),
  updateDevice: (id, deviceData) => api.put(`/devices/${id}`, deviceData),
  deleteDevice: (id) => api.delete(`/devices/${id}`),
  regenerateToken: (id) => api.post(`/devices/${id}/regenerate-token`),
};

export const metricService = {
  getCurrentMetrics: (deviceId) => api.get(`/devices/${deviceId}/metrics/current`),
  getMetricHistory: (deviceId, range = '1h', step = '15s') =>
    api.get(`/devices/${deviceId}/metrics/history`, { params: { range, step } }),
};

export const logService = {
  getDeviceLogs: (deviceId, params) => api.get(`/devices/${deviceId}/logs`, { params }),
};

export const alertService = {
  getAlerts: (params) => api.get('/alerts', { params }),
  resolveAlert: (id) => api.post(`/alerts/${id}/resolve`),
};

export const incidentService = {
  getIncidents: (params) => api.get('/incidents', { params }),
  getIncidentById: (id) => api.get(`/incidents/${id}`),
  createIncident: (data) => api.post('/incidents', data),
  updateStatus: (id, status) => api.patch(`/incidents/${id}/status`, { status }),
  updateIncidentStatus: (id, status) => api.patch(`/incidents/${id}/status`, { status }),
  triggerAiAnalysis: (id) => api.post(`/ai/incidents/${id}/analyze`),
};

export const aiService = {
  analyzeIncident: (incidentId) => api.post(`/ai/incidents/${incidentId}/analyze`),
};

export const adminService = {
  getDashboardStats: () => api.get('/admin/dashboard'),
  getDashboardSummary: () => api.get('/admin/dashboard'),
  getAllDevices: (params) => api.get('/admin/devices', { params }),
  getDevicesTable: (params) => api.get('/admin/devices', { params }),
  getAllUsers: (params) => api.get('/admin/users', { params }),
  getUsers: (params) => api.get('/admin/users', { params }),
  toggleUserStatus: (id, active) => api.patch(`/admin/users/${id}/status`, { active }),
  updateUserStatus: (id, active) => api.patch(`/admin/users/${id}/status`, { active }),
  getGlobalLogs: (params) => api.get('/admin/logs', { params }),
  getAuditLogs: (params) => api.get('/admin/audit-logs', { params }),
};

export default api;