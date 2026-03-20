import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChild } from '../context/ChildContext';
import { getInventory } from '../services/inventory';
import type { InventoryDto, InventoryItemDto } from '../types';

const PAGE_SIZE = 20;

const tierColors: Record<string, string> = {
  COMMON: 'bg-gray-50 border-gray-200',
  RARE: 'bg-blue-50 border-blue-300',
  LEGENDARY: 'bg-yellow-50 border-yellow-400',
};

const tierBadge: Record<string, string> = {
  COMMON: 'bg-gray-200 text-gray-600',
  RARE: 'bg-blue-200 text-blue-700',
  LEGENDARY: 'bg-yellow-200 text-yellow-700',
};

export default function CollectorAlbum() {
  const navigate = useNavigate();
  const { selectedChild } = useChild();
  const [inventory, setInventory] = useState<InventoryDto | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!selectedChild?.id) return;
    setLoading(true);
    getInventory(selectedChild.id)
      .then(setInventory)
      .catch(() => setError('Could not load collection.'))
      .finally(() => setLoading(false));
  }, [selectedChild?.id]);

  const items = inventory?.items ?? [];
  const displayed = items.slice(0, page * PAGE_SIZE);
  const hasMore = displayed.length < items.length;

  return (
    <div className="min-h-screen bg-[#FFF9E8] p-4">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center gap-3 mb-6">
          <button
            onClick={() => navigate('/')}
            className="text-2xl hover:scale-110 transition-transform"
          >
            ←
          </button>
          <h1 className="text-2xl font-bold text-gray-800">🏆 Collector's Album</h1>
        </div>

        {loading && (
          <div className="text-center py-12 text-gray-400">
            <div className="text-4xl mb-2 animate-spin">⭐</div>
            Loading...
          </div>
        )}

        {error && <div className="text-center text-red-500 py-8">{error}</div>}

        {!loading && !error && items.length === 0 && (
          <div className="text-center py-16">
            <div className="text-6xl mb-4">🎁</div>
            <div className="text-xl font-bold text-gray-700 mb-2">No items yet!</div>
            <div className="text-gray-500 mb-6">Play games and complete lessons to earn items!</div>
            <button
              onClick={() => navigate('/play')}
              className="px-6 py-3 bg-teal-400 text-white rounded-full font-bold hover:bg-teal-500 transition-colors"
            >
              Play Now 🎮
            </button>
          </div>
        )}

        {!loading && !error && items.length > 0 && (
          <>
            <p className="text-gray-500 text-sm mb-4">
              {items.length} item{items.length !== 1 ? 's' : ''} collected
            </p>
            <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
              {displayed.map((item: InventoryItemDto) => (
                <div
                  key={item.inventoryId}
                  className={`rounded-2xl border-2 p-3 flex flex-col items-center gap-1 ${tierColors[item.tier] ?? tierColors.COMMON}`}
                >
                  <span className="text-4xl">{item.emoji}</span>
                  <div className="text-xs font-bold text-center leading-tight text-gray-800">{item.name}</div>
                  <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-semibold capitalize ${tierBadge[item.tier] ?? tierBadge.COMMON}`}>
                    {item.tier.toLowerCase()}
                  </span>
                </div>
              ))}
            </div>
            {hasMore && (
              <button
                onClick={() => setPage(p => p + 1)}
                className="mt-6 w-full py-3 bg-white border-2 border-gray-200 rounded-xl font-bold text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Load More
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}
