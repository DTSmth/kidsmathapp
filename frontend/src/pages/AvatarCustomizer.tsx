import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import { getInventory, equipItem } from '../services/inventory';
import type { InventoryDto, InventoryItemDto, ItemType } from '../types';

const SLOTS: { slot: ItemType; label: string; emoji: string }[] = [
  { slot: 'HAT', label: 'Hat', emoji: '🎩' },
  { slot: 'PET', label: 'Pet', emoji: '🐾' },
  { slot: 'CAPE', label: 'Cape', emoji: '🦸' },
  { slot: 'FRAME', label: 'Frame', emoji: '🖼️' },
  { slot: 'BACKGROUND', label: 'BG', emoji: '🌄' },
];

const avatarEmojis = ['😊', '🐱', '🐶', '🦊', '🐸', '🦄', '🐼', '🐨', '🦁', '🐯'];

export default function AvatarCustomizer() {
  const navigate = useNavigate();
  const { selectedChild } = useChild();
  const [inventory, setInventory] = useState<InventoryDto | null>(null);
  const [activeSlot, setActiveSlot] = useState<ItemType>('HAT');
  const [loading, setLoading] = useState(true);
  const [equipping, setEquipping] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!selectedChild?.id) return;
    setLoading(true);
    getInventory(selectedChild.id)
      .then(setInventory)
      .finally(() => setLoading(false));
  }, [selectedChild?.id]);

  const handleEquip = async (item: InventoryItemDto) => {
    if (!selectedChild?.id || equipping) return;
    setEquipping(true);
    setError('');
    try {
      const updated = await equipItem(selectedChild.id, item.itemId, activeSlot);
      setInventory(updated);
    } catch {
      setError('Could not equip item. Try again!');
    } finally {
      setEquipping(false);
    }
  };

  const equipped = inventory?.equipped ?? {};
  const equippedInSlot = equipped[activeSlot] ?? null;
  const itemsForSlot = inventory?.items.filter(i => i.itemType === activeSlot) ?? [];
  const avatarEmoji = avatarEmojis[((selectedChild?.avatarId ? Number(selectedChild.avatarId) : 1) - 1) % avatarEmojis.length];

  return (
    <div className="min-h-screen bg-[#FFF9E8] p-4">
      <div className="max-w-md mx-auto">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={() => navigate('/')} className="text-2xl hover:scale-110 transition-transform">←</button>
          <h1 className="text-2xl font-bold text-gray-800">✨ Avatar</h1>
        </div>

        {/* Avatar preview */}
        <div className="bg-white rounded-3xl p-6 shadow-md mb-4 flex flex-col items-center">
          <div className="relative">
            <div className="text-7xl">{avatarEmoji}</div>
            {Object.entries(equipped).filter(([, item]) => item).length > 0 && (
              <div className="absolute -top-2 -right-2 flex flex-wrap gap-0.5 max-w-[48px]">
                {Object.entries(equipped).map(([slot, item]) => item && (
                  <span key={slot} className="text-base" title={item.name}>{item.emoji}</span>
                ))}
              </div>
            )}
          </div>
          <div className="text-lg font-bold text-gray-800 mt-2">{selectedChild?.name}</div>
        </div>

        {/* Slot tabs */}
        <div className="flex gap-2 mb-4 overflow-x-auto pb-1">
          {SLOTS.map(({ slot, label, emoji }) => (
            <button
              key={slot}
              onClick={() => setActiveSlot(slot)}
              className={`flex-shrink-0 px-3 py-2 rounded-xl text-sm font-bold transition-all
                ${activeSlot === slot
                  ? 'bg-teal-400 text-white shadow-md'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-teal-300'}
              `}
            >
              {emoji} {label}
              {equipped[slot] && <span className="ml-1 text-xs">✓</span>}
            </button>
          ))}
        </div>

        {/* Currently equipped */}
        {equippedInSlot && (
          <div className="bg-teal-50 border border-teal-300 rounded-xl p-3 mb-3 flex items-center gap-3">
            <span className="text-3xl">{equippedInSlot.emoji}</span>
            <div>
              <div className="font-bold text-teal-700">{equippedInSlot.name}</div>
              <div className="text-xs text-gray-500">Currently equipped</div>
            </div>
          </div>
        )}

        {error && <div className="text-red-500 text-sm mb-2 text-center">{error}</div>}

        {/* Items grid */}
        {loading ? (
          <div className="text-center py-8 text-gray-400">Loading...</div>
        ) : itemsForSlot.length === 0 ? (
          <div className="text-center py-8">
            <div className="text-4xl mb-2">🎁</div>
            <div className="text-gray-500">No {activeSlot.toLowerCase()} items yet</div>
            <div className="text-sm text-gray-400 mt-1">Play games to earn items!</div>
          </div>
        ) : (
          <div className="grid grid-cols-3 gap-3">
            {itemsForSlot.map(item => (
              <button
                key={item.inventoryId}
                onClick={() => handleEquip(item)}
                disabled={equipping}
                className={`rounded-2xl p-3 flex flex-col items-center gap-1 border-2 transition-all
                  ${item.equipped
                    ? 'border-teal-400 bg-teal-50'
                    : 'border-gray-200 bg-white hover:border-teal-300 hover:bg-teal-50/50'}
                  ${equipping ? 'opacity-50 cursor-not-allowed' : ''}
                `}
              >
                <span className="text-3xl">{item.emoji}</span>
                <div className="text-xs font-bold text-center leading-tight text-gray-800">{item.name}</div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
