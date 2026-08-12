/* Shared cart utility */
function resetCart() {
    localStorage.removeItem('cart');
    localStorage.setItem('cartRefreshTrigger', Date.now());
    
    // Cập nhật badge giỏ hàng ngay lập tức nếu có
    const badges = document.querySelectorAll('.cart-btn span, #cartBadge, .cart-badge');
    if (badges.length) {
        badges.forEach(b => b.textContent = '0');
    }
}
