import apiClient from './client';
import type { Child, ChildSummaryDto, CreateChildRequest } from '../types';

export const listChildren = async (): Promise<Child[]> => {
  const response = await apiClient.get<{ data: ChildSummaryDto[] }>('/children');
  return response.data.data ?? (response.data as unknown as Child[]);
};

export const getChild = async (id: number): Promise<Child> => {
  const response = await apiClient.get<{ data: Child }>(`/children/${id}`);
  return response.data.data ?? (response.data as unknown as Child);
};

export const createChild = async (data: CreateChildRequest): Promise<Child> => {
  const response = await apiClient.post<{ data: Child }>('/children', data);
  return response.data.data ?? (response.data as unknown as Child);
};
