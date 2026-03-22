import api from './api';
import type { ParentDashboardData, ChildSummaryDto } from '../types';

export async function getParentDashboard(childId: number): Promise<ParentDashboardData> {
  const res = await api.get(`/parent/children/${childId}/dashboard`);
  return res.data.data;
}

export async function getParentChildren(): Promise<ChildSummaryDto[]> {
  const res = await api.get('/parent/children');
  return res.data.data;
}
