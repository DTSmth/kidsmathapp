import apiClient from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post('/auth/login', credentials);
  return response.data?.data ?? response.data;
};

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const response = await apiClient.post('/auth/register', data);
  return response.data?.data ?? response.data;
};

export const refreshToken = async (): Promise<AuthResponse> => {
  const response = await apiClient.post('/auth/refresh');
  return response.data?.data ?? response.data;
};

export const getCurrentUser = async () => {
  const response = await apiClient.get('/auth/me');
  return response.data?.data ?? response.data;
};
