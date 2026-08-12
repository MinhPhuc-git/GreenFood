/**
 * GreenFood — Tích điểm khách hàng
 * API: GET /api/customer/{id}/points
 */
(function (global) {
  const STORAGE_KEY = 'loyaltyPoints';

  async function fetchCustomerPoints(customerId) {
    if (!customerId) return null;
    try {
      const res = await fetch(`/api/customer/${customerId}/points`);
      if (!res.ok) return null;
      return await res.json();
    } catch (e) {
      console.warn('Không tải được điểm tích lũy', e);
      return null;
    }
  }

  async function refreshLoyaltyPoints(customerId) {
    const data = await fetchCustomerPoints(customerId);
    if (data && typeof data.loyaltyPoints === 'number') {
      localStorage.setItem(STORAGE_KEY, String(data.loyaltyPoints));
    }
    return data;
  }

  function getCachedPoints() {
    const v = localStorage.getItem(STORAGE_KEY);
    return v != null ? parseInt(v, 10) || 0 : 0;
  }

  function formatLoyaltyLabel(points) {
    const n = Number(points) || 0;
    return `Điểm tích lũy của bạn: ${n.toLocaleString('vi-VN')} điểm`;
  }

  function renderLoyaltyBadge(container, points) {
    if (!container) return;
    const n = Number(points) || 0;
    container.innerHTML = `<span class="loyalty-badge" title="Điểm thưởng GreenFood">⭐ ${formatLoyaltyLabel(n)}</span>`;
    container.style.display = localStorage.getItem('customerId') ? '' : 'none';
  }

  async function mountLoyaltyBadge(selector) {
    const el = typeof selector === 'string' ? document.querySelector(selector) : selector;
    if (!el) return;
    const customerId = localStorage.getItem('customerId');
    if (!customerId) {
      el.style.display = 'none';
      return;
    }
    const cached = getCachedPoints();
    renderLoyaltyBadge(el, cached);
    const data = await refreshLoyaltyPoints(customerId);
    if (data) renderLoyaltyBadge(el, data.loyaltyPoints);
  }

  global.GreenFoodLoyalty = {
    fetchCustomerPoints,
    refreshLoyaltyPoints,
    getCachedPoints,
    formatLoyaltyLabel,
    renderLoyaltyBadge,
    mountLoyaltyBadge
  };
})(window);
