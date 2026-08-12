(function () {
  const money = (value) =>
    `${new Intl.NumberFormat("vi-VN").format(Number(value || 0))}đ`;

  function escapeHtml(text) {
    return String(text ?? "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  window.initProductSearch = function initProductSearch(options = {}) {
    const input = document.getElementById("productSearch") || document.getElementById("navbarSearchInput");
    if (!input) return;

    const parent = input.closest(".search-box");
    if (!parent) return;

    parent.classList.add("search-box-autocomplete");

    let dropdown = parent.querySelector(".product-search-dropdown");
    if (!dropdown) {
      dropdown = document.createElement("div");
      dropdown.className = "product-search-dropdown";
      dropdown.hidden = true;
      parent.appendChild(dropdown);
    }

    if (input.dataset.searchBound === "1") return;
    input.dataset.searchBound = "1";

    let debounceTimer = null;
    let items = [];
    let activeIndex = -1;

    const hide = () => {
      dropdown.hidden = true;
      dropdown.innerHTML = "";
      items = [];
      activeIndex = -1;
    };

    const show = () => {
      dropdown.hidden = false;
    };

    function renderDropdown(products, keyword) {
      items = products;
      activeIndex = -1;

      if (!products.length) {
        dropdown.innerHTML = `<div class="product-search-empty">Không tìm thấy "<strong>${escapeHtml(keyword)}</strong>"</div>`;
        show();
        return;
      }

      dropdown.innerHTML = products
        .map(
          (product, index) => `
  <button type="button" class="product-search-item" data-index="${index}">
    <img class="product-search-thumb" src="/img_product/${product.id}.png" alt="${escapeHtml(product.name)}" />
    <span class="product-search-item-main">
      <strong>${escapeHtml(product.name)}</strong>
      <small>${escapeHtml(product.categoryName || "GreenFood")}</small>
    </span>
    <span class="product-search-item-price">${money(product.price)}</span>
  </button>`
        )
        .join("");

      dropdown.querySelectorAll(".product-search-item").forEach((btn) => {
        btn.addEventListener("click", () => {
          const product = items[Number(btn.dataset.index)];
          if (!product) return;
          hide();
          input.value = product.name || "";
          if (typeof options.onSelect === "function") {
            options.onSelect(product);
          } else {
            location.href = `/chi-tiet-san-pham.html?id=${product.id}`;
          }
        });
      });

      show();
    }

    async function fetchProducts(keyword) {
      const url = `/api/products?keyword=${encodeURIComponent(keyword)}&size=8&page=0`;
      const response = await fetch(url);
      if (!response.ok) throw new Error("Không tải được sản phẩm");
      const data = await response.json();
      return data.content || [];
    }

    async function runSearch(keyword) {
      if (keyword.length < 1) {
        hide();
        return;
      }

      dropdown.innerHTML = `<div class="product-search-loading">Đang tìm kiếm...</div>`;
      show();

      try {
        const products = await fetchProducts(keyword);
        renderDropdown(products, keyword);
      } catch (error) {
        dropdown.innerHTML = `<div class="product-search-empty">Lỗi tìm kiếm. Thử lại sau.</div>`;
        show();
        console.warn(error);
      }
    }

    input.addEventListener("input", () => {
      const keyword = input.value.trim();
      if (typeof options.onInput === "function") {
        options.onInput(keyword);
      }
      clearTimeout(debounceTimer);
      if (!keyword) {
        hide();
        return;
      }
      debounceTimer = setTimeout(() => runSearch(keyword), 280);
    });

    input.addEventListener("keydown", (event) => {
      const visible = !dropdown.hidden && items.length > 0;

      if (event.key === "ArrowDown" && visible) {
        event.preventDefault();
        activeIndex = Math.min(activeIndex + 1, items.length - 1);
        highlightActive();
        return;
      }

      if (event.key === "ArrowUp" && visible) {
        event.preventDefault();
        activeIndex = Math.max(activeIndex - 1, 0);
        highlightActive();
        return;
      }

      if (event.key === "Escape") {
        hide();
        return;
      }

      if (event.key === "Enter") {
        event.preventDefault();
        const keyword = input.value.trim();
        if (visible && activeIndex >= 0 && items[activeIndex]) {
          const product = items[activeIndex];
          hide();
          input.value = product.name || "";
          if (typeof options.onSelect === "function") {
            options.onSelect(product);
          } else {
            location.href = `/chi-tiet-san-pham.html?id=${product.id}`;
          }
          return;
        }
        if (typeof options.onEnter === "function") {
          options.onEnter(keyword);
        } else if (keyword) {
          location.href = `/?search=${encodeURIComponent(keyword)}`;
        } else {
          location.href = "/";
        }
      }
    });

    function highlightActive() {
      dropdown.querySelectorAll(".product-search-item").forEach((el, i) => {
        el.classList.toggle("is-active", i === activeIndex);
      });
    }

    document.addEventListener("click", (event) => {
      if (!parent.contains(event.target)) hide();
    });
  };
})();
