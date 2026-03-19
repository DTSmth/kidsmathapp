import axios from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';

// In production (Cloud Run) the entrypoint script writes window.__ENV__.API_URL at startup.
// Falls back to localhost for local development.
const apiBaseUrl = (window as any).__ENV__?.API_URL ?? 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
