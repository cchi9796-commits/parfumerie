/* =========================================================
   PARFUMERIE - Core Frontend Script
   API • Cart • Auth helpers • Search/Filter • Shared utilities
   ========================================================= */

const CART_KEY = 'parfumerie_cart';
const USER_KEY = 'parfumerie_user';
const TOKEN_KEY = 'parfumerie_token';
const ORDERS_KEY = 'parfumerie_orders';

// -------------------- API --------------------
async function api(path, method = 'GET', body) {
    const token = localStorage.getItem(TOKEN_KEY);
    const res = await fetch('/api' + path, {
        method,
        headers: {
            'Content-Type': 'application/json',
            ...(token && { Authorization: 'Bearer ' + token })
        },
        body: body ? JSON.stringify(body) : undefined
    });
    const data = await res.json().catch(() => ({}));
    if (res.status === 401 && !path.startsWith('/auth/login')) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        location.href = 'auth.html';
    }
    if (!res.ok) throw new Error(data.message || 'Có lỗi xảy ra');
    return data;
}

function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// -------------------- CART --------------------
function getCart() {
    try {
        return JSON.parse(localStorage.getItem(CART_KEY)) || [];
    } catch {
        return [];
    }
}

function saveCart(cart) {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    updateCartUI();
}

function addToCart(variantId, name, price, image, size = 50, qty = 1) {
    const cart = getCart();
    const existing = cart.find(i => i.variantId === variantId);
    if (existing) {
        existing.qty += qty;
    } else {
        cart.push({ variantId, name, price, image, size, qty });
    }
    saveCart(cart);
    openCart();
    showToast(`Đã thêm "${name}" vào giỏ hàng`);
}

function removeFromCart(index) {
    const cart = getCart();
    cart.splice(index, 1);
    saveCart(cart);
}

function updateQty(index, delta) {
    const cart = getCart();
    if (!cart[index]) return;
    cart[index].qty += delta;
    if (cart[index].qty < 1) cart[index].qty = 1;
    saveCart(cart);
}

function getCartTotal() {
    return getCart().reduce((sum, i) => sum + i.price * i.qty, 0);
}

function formatPrice(n) {
    return n.toLocaleString('vi-VN') + ' đ';
}

function updateCartUI() {
    const cart = getCart();
    const countEl = document.getElementById('cart-count');
    const itemsEl = document.getElementById('cart-items');
    const totalEl = document.getElementById('cart-total');

    if (countEl) countEl.textContent = cart.reduce((s, i) => s + i.qty, 0);

    if (totalEl) totalEl.textContent = formatPrice(getCartTotal());

    if (itemsEl) {
        if (cart.length === 0) {
            itemsEl.innerHTML = `
                <div class="text-center text-gray-500 py-12">
                    <i class="fa-solid fa-bag-shopping text-4xl mb-4 opacity-40"></i>
                    <p class="text-sm">Giỏ hàng đang trống</p>
                </div>`;
        } else {
            itemsEl.innerHTML = cart.map((item, idx) => `
                <div class="flex gap-4 border-b border-gray-800 pb-4">
                    <img src="${item.image}" alt="${esc(item.name)}"
                         class="w-20 h-24 object-cover rounded border border-gray-800">
                    <div class="flex-1">
                        <h4 class="font-serif text-sm font-semibold text-gray-100">${esc(item.name)}</h4>
                        <p class="text-xs text-gray-400 mt-0.5">${item.size}ml</p>
                        <p class="text-gold text-sm font-semibold mt-1">${formatPrice(item.price)}</p>
                        <div class="flex items-center gap-3 mt-2">
                            <button onclick="updateQty(${idx}, -1)"
                                class="w-7 h-7 border border-gray-700 text-gray-400 hover:text-gold hover:border-gold transition-colors text-xs">−</button>
                            <span class="text-sm font-medium w-6 text-center">${item.qty}</span>
                            <button onclick="updateQty(${idx}, 1)"
                                class="w-7 h-7 border border-gray-700 text-gray-400 hover:text-gold hover:border-gold transition-colors text-xs">+</button>
                            <button onclick="removeFromCart(${idx})"
                                class="ml-auto text-gray-500 hover:text-red-400 transition-colors text-sm">
                                <i class="fa-solid fa-trash-can"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        }
    }
}

function openCart() {
    const drawer = document.getElementById('cart-drawer');
    if (drawer) {
        drawer.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }
}

function closeCart() {
    const drawer = document.getElementById('cart-drawer');
    if (drawer) {
        drawer.classList.add('hidden');
        document.body.style.overflow = '';
    }
}

function checkout() {
    const cart = getCart();
    if (cart.length === 0) {
        showToast('Giỏ hàng trống!', 'error');
        return;
    }
    window.location.href = 'checkout.html';
}

// -------------------- AUTH HELPERS --------------------
function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem(USER_KEY));
    } catch {
        return null;
    }
}

function setCurrentUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function setSession(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    setCurrentUser(user);
}

function logout() {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
    showToast('Đã đăng xuất');
    setTimeout(() => window.location.href = 'index.html', 600);
}

function requireLogin() {
    if (!getCurrentUser()) {
        showToast('Vui lòng đăng nhập để tiếp tục', 'error');
        setTimeout(() => window.location.href = 'auth.html', 800);
        return false;
    }
    return true;
}

// Update header auth links if present
function renderAuthLinks() {
    const user = getCurrentUser();
    const authLink = document.querySelector('a[href="auth.html"]');
    if (!authLink) return;

    if (user) {
        authLink.outerHTML = `
            <div class="relative group">
                <button class="hover:text-gold transition-colors flex items-center gap-1 text-sm uppercase tracking-wider">
                    <i class="fa-solid fa-user text-gold"></i>
                    <span class="hidden lg:inline">${esc(user.name?.split(' ')[0] || 'Tài khoản')}</span>
                </button>
                <div class="absolute right-0 top-full mt-2 w-48 bg-cardBg border border-gray-800 rounded shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
                    <a href="profile.html" class="block px-4 py-3 text-sm hover:bg-darkBg hover:text-gold transition-colors">
                        <i class="fa-solid fa-user-pen mr-2"></i> Thông tin cá nhân
                    </a>
                    <a href="orders.html" class="block px-4 py-3 text-sm hover:bg-darkBg hover:text-gold transition-colors">
                        <i class="fa-solid fa-box mr-2"></i> Đơn hàng của tôi
                    </a>
                    <button onclick="logout()" class="w-full text-left px-4 py-3 text-sm hover:bg-darkBg hover:text-red-400 transition-colors border-t border-gray-800">
                        <i class="fa-solid fa-right-from-bracket mr-2"></i> Đăng xuất
                    </button>
                </div>
            </div>`;
    }
}

// -------------------- TOAST --------------------
function showToast(msg, type = 'success') {
    let toast = document.getElementById('global-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'global-toast';
        toast.className = 'fixed bottom-6 right-6 z-[9999] px-5 py-3 rounded shadow-lg text-sm font-medium transition-all duration-300 translate-y-20 opacity-0';
        document.body.appendChild(toast);
    }
    toast.textContent = msg;
    toast.className = `fixed bottom-6 right-6 z-[9999] px-5 py-3 rounded shadow-lg text-sm font-medium transition-all duration-300 ${
        type === 'error' ? 'bg-red-600 text-white' : 'bg-gold text-black'
    }`;
    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-20', 'opacity-0');
    });
    setTimeout(() => {
        toast.classList.add('translate-y-20', 'opacity-0');
    }, 2500);
}

// -------------------- PRODUCTS (index) --------------------
let PRODUCTS = [];

function addFromCard(variantId) {
    const p = PRODUCTS.find(x => x.variantId === variantId);
    if (p) addToCart(p.variantId, p.name, p.price, p.imageUrl, p.size);
}

function productCard(p) {
    return `
    <div class="bg-cardBg rounded border border-gray-800 group hover:border-gold/50 transition-all duration-300 flex flex-col justify-between overflow-hidden">
        <a href="product-detail.html?id=${p.id}" class="relative overflow-hidden bg-black/40 h-80 block">
            <img src="${esc(p.imageUrl)}" alt="${esc(p.name)}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
        </a>
        <div class="p-6 flex flex-col flex-grow justify-between">
            <div>
                <span class="text-xs text-gray-400 tracking-wider uppercase block mb-1">${esc(p.brand)} • ${esc(p.gender)}</span>
                <a href="product-detail.html?id=${p.id}">
                    <h3 class="font-serif text-xl font-bold mb-2 group-hover:text-gold transition-colors">${esc(p.name)}</h3>
                </a>
                <p class="text-gray-400 text-sm font-light line-clamp-2 mb-4">${esc(p.description)}</p>
            </div>
            <div class="flex items-center justify-between pt-4 border-t border-gray-800">
                <div>
                    <span class="text-xs text-gray-500 block">Từ ${p.size}ml</span>
                    <span class="font-serif text-lg font-bold text-gold">${formatPrice(p.price)}</span>
                </div>
                <button onclick="addFromCard(${p.variantId})" class="bg-transparent border border-gold text-gold hover:bg-gold hover:text-black px-4 py-2 text-xs uppercase tracking-wider font-semibold transition-all">Thêm</button>
            </div>
        </div>
    </div>`;
}

async function filterProducts() {
    const grid = document.getElementById('product-grid');
    if (!grid) return;
    const q = document.getElementById('search-input')?.value || '';
    const gender = document.getElementById('filter-gender')?.value || 'all';
    const brand = document.getElementById('filter-brand')?.value || 'all';
    try {
        PRODUCTS = await api(`/products?q=${encodeURIComponent(q)}&gender=${encodeURIComponent(gender)}&brand=${encodeURIComponent(brand)}`);
        grid.innerHTML = PRODUCTS.length
            ? PRODUCTS.map(productCard).join('')
            : '<p class="text-gray-500 col-span-full text-center py-12">Không tìm thấy sản phẩm</p>';
    } catch (e) {
        grid.innerHTML = '<p class="text-red-400 col-span-full text-center py-12">Không tải được sản phẩm</p>';
    }
}

async function loadBrands() {
    const sel = document.getElementById('filter-brand');
    if (!sel) return;
    try {
        const brands = await api('/brands');
        sel.innerHTML = '<option value="all">Tất cả thương hiệu</option>' +
            brands.map(b => `<option value="${esc(b)}">${esc(b)}</option>`).join('');
    } catch (e) { /* giữ danh sách cũ nếu lỗi */ }
}

// -------------------- INIT --------------------
document.addEventListener('DOMContentLoaded', () => {
    updateCartUI();
    renderAuthLinks();
    loadBrands();
    filterProducts();

    // Cart open/close
    const cartBtn = document.getElementById('cart-btn');
    const closeBtn = document.getElementById('close-cart');
    const backdrop = document.getElementById('cart-backdrop');
    if (cartBtn) cartBtn.addEventListener('click', openCart);
    if (closeBtn) closeBtn.addEventListener('click', closeCart);
    if (backdrop) backdrop.addEventListener('click', closeCart);

    // Mobile menu
    const menuBtn = document.getElementById('menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');
    if (menuBtn && mobileMenu) {
        menuBtn.addEventListener('click', () => mobileMenu.classList.toggle('hidden'));
    }

    // Search / filter listeners
    const searchInput = document.getElementById('search-input');
    const genderSelect = document.getElementById('filter-gender');
    const brandSelect = document.getElementById('filter-brand');
    if (searchInput) searchInput.addEventListener('input', filterProducts);
    if (genderSelect) genderSelect.addEventListener('change', filterProducts);
    if (brandSelect) brandSelect.addEventListener('change', filterProducts);
});