import api from './api';
import type { InventoryDto, DailyBonusResponse, ItemType } from '../types';

export const getInventory = (childId: number): Promise<InventoryDto> =>
  api.get(`/inventory/${childId}`).then(r => r.data.data);

export const equipItem = (childId: number, itemId: number, slot: ItemType): Promise<InventoryDto> =>
  api.post(`/inventory/${childId}/equip`, { itemId, slot }).then(r => r.data.data);

export const claimDailyBonus = (childId: number): Promise<DailyBonusResponse> =>
  api.post(`/inventory/${childId}/daily-bonus`).then(r => r.data.data);
