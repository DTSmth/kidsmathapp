import api from './api';

export interface SubscriptionStatus {
  status: string;
  isPremium: boolean;
  periodEnd: string | null;
}

export async function getSubscription(): Promise<SubscriptionStatus> {
  const res = await api.get('/subscription');
  return res.data.data;
}

export async function createCheckoutSession(successUrl?: string, cancelUrl?: string): Promise<string> {
  const res = await api.post('/subscription/create-checkout', { successUrl, cancelUrl });
  return res.data.data.checkoutUrl;
}
