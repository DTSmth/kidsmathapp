// Re-export from new split service files for backwards compatibility
import axios from 'axios';
import apiClient from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export { default } from './client';

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data?.data ?? response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data?.data ?? response.data;
  },

  refreshToken: async (): Promise<AuthResponse> => {
    const response = await apiClient.post('/auth/refresh');
    return response.data?.data ?? response.data;
  },

  getCurrentUser: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data?.data ?? response.data;
  },
};

export const childService = {
  getChildren: async () => {
    const response = await apiClient.get('/children');
    // Handle both wrapped { data: [...] } and unwrapped array responses
    return response.data?.data ?? response.data;
  },

  getChild: async (id: number) => {
    const response = await apiClient.get(`/children/${id}`);
    return response.data?.data ?? response.data;
  },

  createChild: async (data: {
    name: string;
    avatarId: string;
    gradeLevel: string;
    birthDate?: string;
  }) => {
    const response = await apiClient.post('/children', data);
    return response.data?.data ?? response.data;
  },
};

export interface ApiError {
  message: string;
  status: number;
}

export const handleApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    return {
      message: error.response?.data?.message || error.message || 'An error occurred',
      status: error.response?.status || 500,
    };
  }
  return {
    message: 'An unexpected error occurred',
    status: 500,
  };
};
