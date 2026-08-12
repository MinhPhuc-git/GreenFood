const money = (value) => `${new Intl.NumberFormat("vi-VN").format(Number(value || 0))}đ`;
const qs = (selector) => document.querySelector(selector);

function imageForProduct(product) {
  if (window.GreenFoodImages) {
    return window.GreenFoodImages.product(product?.id, product?.imageUrl);
  }
  return "/img/caixoanorganic.webp";
}

function heroImage() {
  return window.GreenFoodImages?.hero() || "/img/intro.png";
}

function formatProductStatus(product) {
  const stockVal = Math.round(parseFloat(product.stock) || 0);
  if (stockVal <= 0) return "Hết hàng";
  const s = (product.status || "Còn hàng").trim().toLowerCase();
  if (s === "in-stock" || s === "active" || s === "còn hàng") return "Còn hàng";
  if (s === "low-stock" || s === "sắp hết") return "Sắp hết";
  if (s === "out-of-stock" || s === "hết hàng") return "Hết hàng";
  return product.status || "Còn hàng";
}

const navItems = [
  ["cua-hang", "Cửa Hàng", "index.html"],
  ["rau-cu", "Rau củ", "index.html#category-rau-cu"],
  ["thit-tuoi", "Thực phẩm tươi", "index.html#category-thit-tuoi"],
  ["recipe", "Công thức", "goiymonan.html"],
];

const navHeader = [
  ["cua-hang", "Cửa Hàng", "index.html"],
  ["rau-cu", "Rau củ", "index.html#category-rau-cu"],
  ["thit-tuoi", "Thực phẩm tươi", "index.html#category-thit-tuoi"],
  ["recipe", "Công thức", "goiymonan.html"],
];

let store = {
  categories: [],
  products: [],
  selectedCategoryId: "",
  keyword: ""
};

function normalizeValue(value) {
  if (value === undefined || value === null) return "";
  return String(value).trim();
}

function normalizeText(value) {
  return normalizeValue(value).toLowerCase();
}

function getProductCategoryId(product) {
  if (!product) return "";

  const fromRelation = product.category?.id;
  if (fromRelation !== undefined && fromRelation !== null) {
    const relationId = normalizeValue(fromRelation);
    if (relationId) return relationId;
  }

  const directId = normalizeValue(
    product.categoryId ?? product.categoryID ?? product.category_id
  );
  if (directId && store.categories.some((category) => normalizeValue(category.id) === directId)) {
    return directId;
  }

  const categoryName = normalizeText(product.categoryName ?? product.category?.name);
  if (categoryName) {
    const matched = store.categories.find((category) => normalizeText(category.name) === categoryName);
    if (matched) return normalizeValue(matched.id);
  }

  return directId;
}

function productsForCategory(category) {
  const list = keywordFilteredProducts();
  if (!category) return list;
  const targetId = normalizeValue(category.id);
  return list.filter((product) => getProductCategoryId(product) === targetId);
}

async function apiJson(url) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
  return response.json();
}

function normalizeTextForSearch(text) {
  if (!text) return "";
  return text.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d");
}

function keywordFilteredProducts() {
  return store.products.filter((product) => {
    const text = normalizeTextForSearch(`${product.name || ""}`);
    const key = normalizeTextForSearch(store.keyword);
    return !key || text.includes(key);
  });
}

function visibleProducts() {
  return keywordFilteredProducts().filter((product) => {
    if (!store.selectedCategoryId) return true;
    return getProductCategoryId(product) === normalizeValue(store.selectedCategoryId);
  });
}

function header(active = "shop") {
  const links = navItems.map(([key, label, href]) => {
    const activeClass = active === key ? "active" : "";
    return `<a class="${activeClass}" href="${href}">${label}</a>`;
  }).join("");

  return `
    <header class="site-header">
      <div class="container navbar">
        <a class="brand" href="index.html" aria-label="GreenFood">
          <img src="/icon/leaf.png" alt="leaf" style="width:22px;height:22px;object-fit:contain; margin-right:6px; vertical-align:middle;">GreenFood
        </a>
        <nav class="nav-links" aria-label="Điều hướng chính">${links}</nav>
        <div class="nav-actions">
          <div id="loyaltyPointsBadge" class="loyalty-points-wrap"></div>
          <label class="search-box"><span></span><input id="productSearch" placeholder="Tìm kiếm thực phẩm tươi sống..." value="${store.keyword}" /></label>
          <button class="account-btn" id="accountBtn" onclick="(localStorage.getItem('customerId') ? location.href='/account' : location.href='/login')">${localStorage.getItem("customerName") || "Tài khoản"}</button>
          <button class="cart-btn" aria-label="Giỏ hàng" onclick="location.href='giohang.html'"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-top: 4px;"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 001.98 1.61h9.72a2 2 0 001.98-1.61L23 6H6"/></svg><span>0</span></button>
        </div>
      </div>
    </header>`;
}

function footer() {
  return `
    <footer class="site-footer">
      <div class="container footer-grid">
        <div>
          <a class="brand footer-brand" href="index.html"><img src="/icon/leaf.png" alt="GreenFood leaf icon" style="width:22px;height:22px;object-fit:contain; margin-right:6px; vertical-align:middle;">GreenFood</a>
          <p>Mang trải nghiệm chợ nông sản đến tận cửa nhà bạn. Thuần khiết, hữu cơ và luôn tươi mới.</p>
          <div class="socials"><span>f</span><span>in</span></div>
        </div>
        <div><h3>Liên Kết Nhanh</h3><a href="index.html">Cửa hàng</a><a href="goiymonan.html">Gợi ý món ăn</a><a href="register.html">Đăng ký</a><a href="lichsu.html">Lịch sử mua hàng</a></div>
        <div><h3>Hỗ Trợ</h3><a href="chinhsachbaomat.html">Chính Sách Giao Hàng</a><a href="quanlynguoidung3.html">Điều Khoản Dịch vụ</a><a href="chinhsachbaomat.html">Chính Sách Bảo Mật</a><a href="quanlynguoidung1.html">Tài khoản của tôi</a></div>
        <div><h3>Bản Tin</h3><p>Nhận công thức và ưu đãi qua email của bạn.</p><label class="newsletter"><input placeholder="your@email.com"><button>Đăng Ký</button></label></div>
      </div>
      <div class="container copyright">2026 GreenFood. Bảo lưu mọi quyền.</div>
    </footer>`;
}

function productCard(product) {
  return `
    <article class="product-card">
      <a class="product-image" href="chi-tiet-san-pham.html?id=${product.id}" style="background-image:url('${imageForProduct(product)}')">
        ${product.averageRating ? `<span class="discount">${product.averageRating} sao</span>` : ""}
      </a>
      <div class="product-body">
        <span class="product-tag">${product.categoryName || product.category?.name || "GreenFood"}</span>
        <h3><a href="chi-tiet-san-pham.html?id=${product.id}">${product.name || "Sản phẩm"}</a></h3>
        <div class="product-meta"><span>${product.unit || "đơn vị"}</span><span style="${formatProductStatus(product) === 'Hết hàng' ? 'color:#ef4444;font-weight:600;' : ''}">${formatProductStatus(product)}</span></div>
        <div class="price-row"><div class="price">${money(product.price)}</div><button class="add-btn" onclick="addToCart(${product.id})">Thêm</button></div>
      </div>
    </article>`;
}

function categoryList() {
  const allActive = !store.selectedCategoryId ? "active" : "";
  const totalAfterSearch = keywordFilteredProducts().length;
  return `
    <button class="category-btn ${allActive}" data-category-id=""><span>Tất cả</span><b>${totalAfterSearch}</b></button>
    ${store.categories.map((category) => {
    const active = String(store.selectedCategoryId) === String(category.id) ? "active" : "";
    return `<button class="category-btn ${active}" data-category-id="${category.id}"><span>${category.name}</span><b>${countByCategory(category.id)}</b></button>`;
  }).join("")}`;
}

function convertToSlug(text) {
  return text.toLowerCase()
    .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/[^a-z0-9\s-]/g, "")
    .replace(/\s+/g, "-");
}
function countByCategory(categoryId) {
  const targetCategoryId = normalizeValue(categoryId);
  if (!targetCategoryId) return keywordFilteredProducts().length;
  return productsForCategory({ id: targetCategoryId }).length;
}

function sectionBlock(category) {
  const products = category ? productsForCategory(category) : keywordFilteredProducts();
  if (!products.length) return "";

  const id = category ? `category-${convertToSlug(category.name)}` : "all-products";
  const title = category ? category.name : "Tất cả sản phẩm";

  return `
    <section id="${id}" class="product-section" style="scroll-margin-top: 120px;">
      <div class="section-heading"><h2>${title}</h2><span>${products.length} sản phẩm</span></div>
      <div class="product-grid">${products.map(productCard).join("")}</div>
    </section>`;
}

function buildProductSections() {
  if (store.keyword && keywordFilteredProducts().length === 0) {
    return `<div style="padding: 40px 20px; text-align: center; width: 100%;">
              <h3 style="color: #555; font-size: 1.2rem;">Không tìm thấy sản phẩm phù hợp với từ khóa '${store.keyword}'</h3>
            </div>`;
  }

  const selectedCategory = store.categories.find(
    (category) => normalizeValue(category.id) === normalizeValue(store.selectedCategoryId)
  );

  if (selectedCategory) {
    return sectionBlock(selectedCategory) || sectionBlock(null);
  }

  const groupedSections = store.categories.map(sectionBlock).filter(Boolean).join("");
  if (groupedSections) return groupedSections;

  return sectionBlock(null);
}

function renderList() {
  const app = qs("#app");
  if (!app) return;

  const sections = buildProductSections();

  let currentActiveTab = "shop";
  if (store.selectedCategoryId) {
    const selectedCat = store.categories.find(
      (category) => normalizeValue(category.id) === normalizeValue(store.selectedCategoryId)
    );
    if (selectedCat) {
      const foundNav = navItems.find(
        (item) => normalizeText(item[1]) === normalizeText(selectedCat.name)
      );
      if (foundNav) currentActiveTab = foundNav[0];
    }
  }

  app.innerHTML = `${header(currentActiveTab)}
    <main>
      <section class="container hero">
        <div class="hero-card">
          <div>
            <span class="eyebrow">Sống xanh hôm nay</span>
            <h1>Sản phẩm tươi sạch cho bữa ăn mỗi ngày</h1>
            <p class="lead">Danh sách rau củ, trái cây, thực phẩm tươi và đồ uống lành mạnh được phân loại rõ ràng, giúp khách hàng dễ dàng lựa chọn nhanh chóng theo nhu cầu.</p>
          </div>
          <div class="hero-visual" style="background-image:url('${heroImage()}')">
            <div class="floating-card"><strong>${store.products.length}+</strong><span>sản phẩm đang bán</span></div>
          </div>
        </div>
      </section>
      <section class="container shop-layout">
        <aside class="panel">
          <h2>Phân loại</h2>
          <div class="category-list">${categoryList()}</div>
        </aside>
        <div class="shop-content">
          <div class="quick-tabs"><button class="${!store.selectedCategoryId ? "active" : ""}" data-category-id="">Tất cả</button>${store.categories.slice(0, 4).map((category) => `<button class="${normalizeValue(store.selectedCategoryId) === normalizeValue(category.id) ? "active" : ""}" data-category-id="${category.id}">${category.name}</button>`).join("")}<span>Sắp xếp: <b>Mới nhất</b></span></div>
          <div id="product-sections-wrapper">${sections}</div>
          <div id="sale" class="promo-strip"><div><h2>Combo rau củ tuần này</h2><p>Tiết kiệm đến 25% với combo rau củ mới tuần này</p></div><a class="promo-btn" href="goiymonan.html">Gợi ý món ăn</a></div>
        </div>
      </section>
    </main>${footer()}`;
  bindListEvents();
  if (typeof updateCartBadge === "function") updateCartBadge();
  if (window.GreenFoodLoyalty) {
    GreenFoodLoyalty.mountLoyaltyBadge("#loyaltyPointsBadge");
  }
}

function scrollToCategorySection(categoryName) {
  const slug = convertToSlug(categoryName);
  const targetSection = qs(`#category-${slug}`);
  if (targetSection) {
    targetSection.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

function bindListEvents() {
  document.querySelectorAll("[data-category-id]").forEach((button) => {
    button.addEventListener("click", () => {
      store.selectedCategoryId = button.dataset.categoryId;
      const selectedCat = store.categories.find(
        (category) => normalizeValue(category.id) === normalizeValue(store.selectedCategoryId)
      );

      renderList();

      if (selectedCat) {
        scrollToCategorySection(selectedCat.name);
      } else {
        qs("#product-sections-wrapper")?.scrollIntoView({ behavior: "smooth" });
      }
    });
  });

  if (typeof window.initProductSearch === "function") {
    window.initProductSearch({
      onEnter(keyword) {
        store.keyword = keyword;
        renderList();
        qs("#product-sections-wrapper")?.scrollIntoView({ behavior: "smooth" });
      }
    });
  }
}

function handleHashChange() {
  const hash = window.location.hash.replace("#", "");
  if (!hash) return;

  const navMap = navItems.find((item) => item[0] === hash);
  if (navMap) {
    const catName = navMap[1];
    const targetCategory = store.categories.find(
      (category) => normalizeText(category.name) === normalizeText(catName)
    );
    if (targetCategory) {
      store.selectedCategoryId = targetCategory.id;
      renderList();
      setTimeout(() => {
        scrollToCategorySection(targetCategory.name);
      }, 200);
    }
  }
}

async function loadListData() {
  const urlKeyword = new URLSearchParams(location.search).get("search");
  if (urlKeyword) {
    store.keyword = urlKeyword.trim();
  }

  const [productPage, categories] = await Promise.all([
    apiJson("/api/products?size=500"),
    apiJson("/api/categories")
  ]);
  store.categories = categories || [];
  store.products = productPage.content || [];
  renderList();
  handleHashChange();
}

async function renderDetail() {
  const app = qs("#detail-app");
  if (!app) return;
  const id = new URLSearchParams(location.search).get("id");
  const product = await apiJson(`/api/products/${id}`);
  const relatedPage = await apiJson(`/api/products?categoryId=${product.categoryId}&size=4`);
  const related = (relatedPage.content || []).filter((item) => item.id !== product.id).slice(0, 3);

  app.innerHTML = `${header(product.categoryName || "vegetable")}
    <main>
      <section class="container detail-wrap">
        <div class="breadcrumb"><a href="index.html">Cửa hàng</a> / <a href="index.html">${product.categoryName || "Sản phẩm"}</a> / <b>${product.name}</b></div>
        <div class="detail-hero">
          <div class="gallery"><div class="main-img" id="main-img" style="background-image:url('${imageForProduct(product)}')"></div></div>
          <div class="product-info"><span class="eyebrow">${product.categoryName || "GreenFood"}</span><span class="status" style="${formatProductStatus(product) === 'Hết hàng' ? 'background-color:#fee2e2;color:#b91c1c;' : ''}">${formatProductStatus(product)}</span><h1>${product.name}</h1><p class="lead">${product.description || "Sản phẩm tươi được sơ chế, đóng gói và kiểm tra chất lượng trước khi giao."}</p><div class="detail-price"><div class="price">${money(product.price)}</div><span>/${product.unit || "đơn vị"}</span></div><div class="metrics"><div class="metric"><small>Danh mục</small><strong>${product.categoryName || "N/A"}</strong></div><div class="metric"><small>Tồn kho</small><strong>${Math.round(product.stock || 0)}</strong></div><div class="metric"><small>Đánh giá</small><strong>${product.averageRating || 0}/5</strong></div></div><div class="actions"><button class="account-btn big" onclick="addToCart(${product.id})">Thêm vào giỏ hàng</button><button class="secondary-btn" onclick="addToCart(${product.id}); setTimeout(()=>location.href='giohang.html',500)">Mua ngay</button></div></div>
        </div>
      </section>
      <section class="container content-sections"><div class="info-card"><h2>Thông tin đóng gói</h2><p class="lead">${product.description || "Dữ liệu sản phẩm được lấy từ database qua API."}</p><div class="facts"><div class="fact">Đơn vị: ${product.unit || "N/A"}</div><div class="fact">Tồn kho: ${Math.round(product.stock || 0)}</div><div class="fact">Trạng thái: ${formatProductStatus(product)}</div><div class="fact">Điểm trung bình: ${product.averageRating || 0}/5</div></div><h2 class="related-title">Sản phẩm liên quan</h2><div class="product-grid related-grid">${related.map(productCard).join("")}</div></div></section>
    </main>${footer()}`;
  if (typeof updateCartBadge === "function") updateCartBadge();
  if (typeof window.initProductSearch === "function") {
    window.initProductSearch({
      onEnter(keyword) {
        if (keyword) {
          location.href = `/?search=${encodeURIComponent(keyword)}`;
        } else {
          location.href = "/";
        }
      }
    });
  }
}

window.GreenFoodLayout = { header, footer, navItems };

window.onload = () => {
  if (qs("#site-header")) {
    qs("#site-header").outerHTML = header("recipe");
  }
  if (qs("#site-footer")) {
    qs("#site-footer").outerHTML = footer();
  }

  if (qs("#app")) {
    loadListData().catch((error) => {
      qs("#app").innerHTML = `${header()}<main class="container"><p class="lead">Không tải được sản phẩm: ${error.message}</p></main>${footer()}`;
    });
    window.addEventListener("hashchange", handleHashChange);
  }

  if (qs("#detail-app")) {
    renderDetail().catch((error) => {
      qs("#detail-app").innerHTML = `${header()}<main class="container"><p class="lead">Không tải được chi tiết sản phẩm: ${error.message}</p></main>${footer()}`;
    });
  }

  updateCartBadge();

  if (typeof window.initProductSearch === "function") {
    window.initProductSearch({
      onEnter(keyword) {
        if (keyword) {
          location.href = `/?search=${encodeURIComponent(keyword)}`;
        } else {
          location.href = "/";
        }
      }
    });
  }

  // Load latest user info on page load
  if (typeof window.refreshUserInfo === "function") {
    window.refreshUserInfo();
  }
  if (window.GreenFoodLoyalty) {
    GreenFoodLoyalty.mountLoyaltyBadge("#loyaltyPointsBadge");
  }
};

window.addToCart = async function (productId) {
  const customerId = localStorage.getItem("customerId");
  if (!customerId) {
    window.location.href = "/login";
    return;
  }

  // Client-side check if store is loaded
  if (typeof store !== "undefined" && store.products) {
    const product = store.products.find(p => String(p.id) === String(productId));
    if (product) {
      const status = (product.status || "").trim().toLowerCase();
      if (status !== "active" && status !== "sẵn sàng" && status !== "còn hàng") {
        alert("Sản phẩm đã ngừng kinh doanh hoặc không có sẵn.");
        return;
      }
      if (product.stock <= 0) {
        alert("Sản phẩm đã hết hàng.");
        return;
      }
    }
  }

  try {
    const response = await fetch(`/api/cart/${customerId}/add`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ productId: productId, quantity: 1 })
    });

    const text = await response.text();
    let result = {};
    try { result = JSON.parse(text); } catch (e) { }

    if (response.ok) {
      alert("Đã thêm vào giỏ hàng!");
      if (typeof updateCartBadge === "function") updateCartBadge();
    } else {
      alert(result.error || result.message || "Lỗi khi thêm vào giỏ");
    }
  } catch (err) {
    alert("Lỗi kết nối tới máy chủ");
  }
}

window.updateCartBadge = async function () {
  const customerId = localStorage.getItem("customerId");
  const badges = document.querySelectorAll(".cart-btn span, .cart-badge");

  if (!customerId) {
    badges.forEach(b => b.textContent = "0");
    return;
  }

  try {
    const response = await fetch(`/api/cart/${customerId}`);
    if (response.ok) {
      const cart = await response.json();
      const totalItems = (cart.listCartItems || []).reduce((sum, item) => sum + item.quantity, 0);
      badges.forEach(badge => {
        badge.textContent = totalItems;
      });
    }
  } catch (e) {
    console.error("Cart badge update error:", e);
  }

  // Refresh user info from DB and update UI
  if (typeof window.refreshUserInfo !== "function") {
    window.refreshUserInfo = async function () {
      const custId = localStorage.getItem("customerId");
      if (!custId) return;
      try {
        const res = await fetch(`/api/customers/${custId}`);
        if (res.ok) {
          const data = await res.json();
          const name = data.name || data.account;
          localStorage.setItem("customerName", name);
          const accBtn = document.getElementById("accountBtn");
          if (accBtn) accBtn.textContent = name;
        }
      } catch (e) {
        console.error("Failed to refresh user info:", e);
      }
    };
  }
}

window.addEventListener('pageshow', updateCartBadge);
window.addEventListener('storage', (e) => {
  if (e.key === 'cartRefreshTrigger' || e.key === 'cart') {
    if (typeof updateCartBadge === "function") updateCartBadge();
  }
});