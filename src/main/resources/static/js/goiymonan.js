(function () {
  const zone = document.getElementById('uploadZone');
  const ingGrid = document.getElementById('ingGrid');
  const suggestionDesc = document.getElementById('suggestionDesc');
  const recipeGrid = document.getElementById('recipeGrid');
  const trendingList = document.getElementById('trendingList');
  const suggestSubtitle = document.getElementById('suggestSubtitle');
  const recipePagination = document.getElementById('recipePagination');
  const suggestNavPrev = document.querySelector('.suggest-nav button[aria-label="Trước"]');
  const suggestNavNext = document.querySelector('.suggest-nav button[aria-label="Sau"]');
  const fileInput = document.getElementById('imageInput');
  const btnAddManual = document.querySelector('.btn-add-manual');

  let detectedIngredients = [];
  let allRecipes = [];
  let trendingTimer = null;
  let trendingPool = [];
  let displayedRecipes = [];
  let recipePage = 0;

  const TRENDING_INTERVAL_MS = 3500;
  const TRENDING_SHOW_COUNT = 3;
  const RECIPES_PER_PAGE = 6;

  if (!zone || !ingGrid || !recipeGrid) return;

  setupUploadZone();
  setupManualAdd();
  renderIngredients([]);
  showRecipeLoading();

  zone.addEventListener('dragover', (e) => {
    e.preventDefault();
    zone.classList.add('drag-over');
  });

  zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));

  zone.addEventListener('drop', (e) => {
    e.preventDefault();
    zone.classList.remove('drag-over');
    const file = e.dataTransfer.files[0];
    if (file && file.type.startsWith('image/')) {
      processImage(file);
    }
  });

  window.handleUpload = function (input) {
    const file = input.files[0];
    if (file) {
      processImage(file);
    }
    // Reset value to allow selecting the same file again
    input.value = '';
  };

  function setupUploadZone() {
    zone.addEventListener('click', (e) => {
      if (e.target.closest('.btn-reupload') || zone.classList.contains('has-image')) return;
      fileInput?.click();
    });
  }

  function setupManualAdd() {
    if (!btnAddManual) return;
    btnAddManual.addEventListener('click', () => {
      const name = window.prompt('Nhập tên nguyên liệu:');
      if (name) window.GoiYMonAn.addIngredient(name);
    });
  }

  async function processImage(file) {
    showUploadPreview(file, true);
    try {
      const formData = new FormData();
      formData.append('image', file);

      const response = await fetch('/api/ingredients/detect', {
        method: 'POST',
        body: formData
      });

      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || data.error || 'Không thể nhận diện ảnh');
      }

      detectedIngredients = (data.ingredients || []).map((item) => ({
        name: item.name,
        confidence: item.confidence ?? 1
      }));

      const recipes = (data.recipes || []).map(normalizeRecipe);
      renderIngredients(detectedIngredients);
      updateSuggestionCard(detectedIngredients, recipes);
      renderRecipes(recipes);
      startTrendingRotation(allRecipes.length ? allRecipes : recipes);
      updateSuggestSubtitle(true);
      showUploadPreview(file, false, detectedIngredients.length);
    } catch (error) {
      console.error(error);
      showUploadError(file, error.message);
    }
  }

  function showUploadPreview(file, loading, count) {
    const reader = new FileReader();
    reader.onload = (e) => {
      const statusText = loading
        ? 'Đang quét nguyên liệu bằng Roboflow AI...'
        : count > 0
          ? `✓ Nhận diện ${count} nguyên liệu`
          : '✓ Ảnh đã tải – không phát hiện nguyên liệu rõ ràng';

      zone.innerHTML = `
        <img src="${e.target.result}" class="upload-preview-img" alt="Ảnh nguyên liệu" />
        <p class="upload-status ${loading ? 'loading' : 'success'}">${statusText}</p>
        <small>${escapeHtml(file.name)}</small>
        <button type="button" class="btn-reupload" onclick="document.getElementById('imageInput').click()">Chọn ảnh khác</button>
      `;
      zone.classList.add('has-image');
    };
    reader.readAsDataURL(file);
  }

  function showUploadError(file, message) {
    zone.innerHTML = `
      <div class="upload-icon-wrap">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      </div>
      <p class="upload-status error">${escapeHtml(message)}</p>
      <small>${escapeHtml(file.name)}</small>
      <button type="button" class="btn-reupload" onclick="document.getElementById('imageInput').click()">Thử lại</button>
    `;
  }

  function renderIngredients(ingredients) {
    if (!ingredients.length) {
      ingGrid.innerHTML = `
        <div class="ing-empty">
          Chưa có nguyên liệu. Tải ảnh hoặc bấm &quot;Thêm thủ công&quot; để gợi ý món từ công thức trong hệ thống.
        </div>`;
      return;
    }

    ingGrid.innerHTML = ingredients.map((item, index) => {
      const percent = Math.round((item.confidence ?? 1) * 100);
      const statusClass = percent >= 80 ? '' : percent >= 50 ? 'yellow' : 'low';
      const statusLabel = percent >= 80 ? 'KHỚP' : percent >= 50 ? 'KHỚP' : 'KHỚP';

      return `
        <div class="ing-item" data-index="${index}">
          <div class="ing-item-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--green)" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="9 11 12 14 22 4"/></svg>
          </div>
          <div class="ing-item-info">
            <div class="ing-item-name">${escapeHtml(item.name)}</div>
            <div class="ing-item-status ${statusClass}">${statusLabel} · ${percent}%</div>
            <div class="ing-progress"><div class="ing-progress-fill ${statusClass}" style="width:${percent}%"></div></div>
          </div>
          <div class="ing-item-actions">
            <button type="button" title="Xóa" onclick="removeIngredient(${index})">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/></svg>
            </button>
          </div>
        </div>`;
    }).join('');
  }

  window.removeIngredient = function (index) {
    detectedIngredients.splice(index, 1);
    renderIngredients(detectedIngredients);
    refreshRecipesFromIngredients();
  };

  async function refreshRecipesFromIngredients() {
    if (!detectedIngredients.length) {
      updateSuggestionCard([], []);
      updateSuggestSubtitle(false);
      loadDefaultRecipes();
      return;
    }

    const params = new URLSearchParams();
    detectedIngredients.forEach((item) => params.append('ingredient', item.name));

    try {
      const response = await fetch('/api/recipes/suggest?' + params.toString());
      if (!response.ok) throw new Error('API lỗi');
      const recipes = (await response.json()).map(normalizeRecipe);
      const activeRecipes = filterActiveRecipes(recipes);
      updateSuggestionCard(detectedIngredients, activeRecipes);
      renderRecipes(activeRecipes);
      startTrendingRotation(allRecipes.length ? allRecipes : activeRecipes);
      updateSuggestSubtitle(true);
    } catch (error) {
      console.warn('Không tải được gợi ý món ăn', error);
    }
  }

  function updateSuggestionCard(ingredients, recipes) {
    if (!suggestionDesc) return;
    const count = ingredients.length;
    const recipeCount = recipes.length;
    if (!count) {
      suggestionDesc.textContent = allRecipes.length
        ? `Đang hiển thị ${allRecipes.length} công thức từ GreenFood. Tải ảnh để lọc theo nguyên liệu.`
        : 'Tải ảnh nguyên liệu để AI Chef gợi ý món ăn phù hợp.';
      return;
    }
    suggestionDesc.textContent = `Dựa trên ${count} nguyên liệu, tìm thấy ${recipeCount} công thức khớp trong cơ sở dữ liệu.`;
  }

  function updateSuggestSubtitle(filtered) {
    if (!suggestSubtitle) return;
    const pageHint = 'Hiển thị 6 món/trang (3 món mỗi hàng)';
    suggestSubtitle.textContent = filtered
      ? `Lọc theo nguyên liệu · ${pageHint}`
      : `Công thức từ database · ${pageHint}`;
  }

  function normalizeRecipe(recipe) {
    return {
      id: recipe.id,
      name: recipe.name,
      description: recipe.description,
      ingredients: recipe.ingredients,
      instructions: recipe.instructions,
      status: recipe.status,
      imageUrl: recipe.imageUrl,
      img: recipe.img ?? recipe.id,
      matchPercent: recipe.matchPercent ?? null
    };
  }



  function recipeImageSrc(recipe, index) {
    // Luôn lấy hình từ /img_dishes/{id}.png
    const imgId = recipe?.img ?? recipe?.id ?? index;
    if (imgId != null && imgId !== '' && imgId !== 0) {
      return `/img_dishes/${imgId}.png`;
    }
    return '/img/intro.png';
  }

  function extractRecipeTime(instructions) {
    const match = String(instructions || '').match(/Thời gian:\s*(\d+)\s*phút/i);
    return match ? `${match[1]} phút` : '20 phút';
  }

  function difficultyFromInstructions(instructions) {
    const text = String(instructions || '');
    if (/siêu dễ|rất dễ/i.test(text)) return 'Rất dễ';
    if (/trung bình/i.test(text)) return 'Trung bình';
    if (/khó/i.test(text)) return 'Khó';
    return 'Dễ';
  }

  function buildRecipeCardHtml(recipe, index) {
    const match = recipe.matchPercent != null && recipe.matchPercent > 0
      ? `KHỚP ${recipe.matchPercent}%`
      : 'CÔNG THỨC';
    const time = extractRecipeTime(recipe.instructions);
    const difficulty = difficultyFromInstructions(recipe.instructions);

    return `
      <div class="recipe-card">
        <div class="recipe-img-wrap">
          <img src="${recipeImageSrc(recipe, recipe.id ?? index)}" alt="${escapeHtml(recipe.name || 'Công thức')}" loading="lazy" />
          <span class="match-badge">${match}</span>
          <button type="button" class="fav-btn" onclick="toggleFav(this)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
          </button>
        </div>
        <div class="recipe-body">
          <div class="recipe-meta-row">
            <span class="recipe-meta-item">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              ${escapeHtml(time)}
            </span>
            <span class="recipe-meta-item">·</span>
            <span class="recipe-meta-item">${escapeHtml(difficulty)}</span>
          </div>
          <div class="recipe-title">${escapeHtml(recipe.name || '')}</div>
          <p class="recipe-desc">${escapeHtml(recipe.description || recipe.ingredients || '')}</p>
          <a href="/chi-tiet-mon-an?id=${recipe.id}" class="recipe-link">Chi tiết món ăn</a>
        </div>
      </div>`;
  }

  function getRecipeTotalPages() {
    return Math.max(1, Math.ceil(displayedRecipes.length / RECIPES_PER_PAGE));
  }

  function updateRecipePagination() {
    const total = displayedRecipes.length;
    const totalPages = getRecipeTotalPages();

    if (recipePagination) {
      if (total <= RECIPES_PER_PAGE) {
        recipePagination.hidden = true;
        recipePagination.textContent = '';
      } else {
        recipePagination.hidden = false;
        const start = recipePage * RECIPES_PER_PAGE + 1;
        const end = Math.min((recipePage + 1) * RECIPES_PER_PAGE, total);
        recipePagination.textContent = `Trang ${recipePage + 1}/${totalPages} · Hiển thị ${start}–${end} / ${total} món`;
      }
    }

    if (suggestNavPrev) {
      suggestNavPrev.disabled = recipePage <= 0;
    }
    if (suggestNavNext) {
      suggestNavNext.disabled = recipePage >= totalPages - 1;
    }
  }

  function renderRecipePage() {
    if (!displayedRecipes.length) {
      recipeGrid.innerHTML = `
        <div class="recipe-empty">
          Chưa có công thức khớp. Thêm công thức trong trang quản trị hoặc thử ảnh/nguyên liệu khác.
        </div>`;
      updateRecipePagination();
      return;
    }

    const start = recipePage * RECIPES_PER_PAGE;
    const pageItems = displayedRecipes.slice(start, start + RECIPES_PER_PAGE);
    recipeGrid.innerHTML = pageItems.map((recipe, index) => buildRecipeCardHtml(recipe, start + index)).join('');
    updateRecipePagination();
  }

  function renderRecipes(recipes) {
    displayedRecipes = recipes || [];
    recipePage = 0;
    renderRecipePage();
  }

  function changeRecipePage(dir) {
    if (!displayedRecipes.length) return;
    const totalPages = getRecipeTotalPages();
    const nextPage = recipePage + dir;
    if (nextPage < 0 || nextPage >= totalPages) return;
    recipePage = nextPage;
    renderRecipePage();
    recipeGrid?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function stopTrendingRotation() {
    if (trendingTimer) {
      clearInterval(trendingTimer);
      trendingTimer = null;
    }
  }

  function pickRandomRecipes(pool, count) {
    const source = [...pool];
    const picked = [];
    const limit = Math.min(count, source.length);
    while (picked.length < limit) {
      const index = Math.floor(Math.random() * source.length);
      picked.push(source.splice(index, 1)[0]);
    }
    return picked;
  }

  function buildTrendingHtml(recipes) {
    return recipes.map((recipe, index) => {
      const time = extractRecipeTime(recipe.instructions);
      const difficulty = difficultyFromInstructions(recipe.instructions);
      return `
        <a class="trending-item" href="/chi-tiet-mon-an?id=${recipe.id}">
          <img class="trending-thumb" src="${recipeImageSrc(recipe, recipe.id ?? index)}" alt="${escapeHtml(recipe.name || '')}" loading="lazy" />
          <div class="trending-info">
            <div class="trending-name">${escapeHtml(recipe.name || 'Công thức')}</div>
            <div class="trending-meta">${escapeHtml(time)} · ${escapeHtml(difficulty)}</div>
          </div>
        </a>`;
    }).join('');
  }

  function showTrendingPair() {
    if (!trendingList || !trendingPool.length) return;
    const items = pickRandomRecipes(trendingPool, TRENDING_SHOW_COUNT);
    trendingList.classList.add('is-fading');
    window.setTimeout(() => {
      trendingList.innerHTML = buildTrendingHtml(items);
      trendingList.classList.remove('is-fading');
    }, 180);
  }

  function startTrendingRotation(pool) {
    stopTrendingRotation();
    if (!trendingList) return;

    trendingPool = (pool || []).filter((recipe) => recipe && recipe.id != null);
    if (!trendingPool.length) {
      trendingList.innerHTML = '<p class="trending-empty">Chưa có công thức trong hệ thống.</p>';
      return;
    }

    showTrendingPair();
    if (trendingPool.length > TRENDING_SHOW_COUNT) {
      trendingTimer = window.setInterval(showTrendingPair, TRENDING_INTERVAL_MS);
    }
  }

  window.addEventListener('beforeunload', stopTrendingRotation);

  function showRecipeLoading() {
    recipeGrid.innerHTML = '<div class="recipe-loading">Đang tải công thức từ cơ sở dữ liệu...</div>';
  }

  async function loadDefaultRecipes() {
    showRecipeLoading();
    try {
      const response = await fetch('/api/recipes');
      if (!response.ok) throw new Error('API lỗi');
      allRecipes = (await response.json()).map(normalizeRecipe);
      updateSuggestionCard([], allRecipes);
      renderRecipes(allRecipes);
      startTrendingRotation(allRecipes);
      updateSuggestSubtitle(false);
    } catch (error) {
      console.warn('Không tải được recipes từ API', error);
      recipeGrid.innerHTML = `
        <div class="recipe-empty">
          Không kết nối được API công thức. Kiểm tra server và bảng <code>recipe</code> trong database.
        </div>`;
    }
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  window.toggleFav = function (btn) {
    btn.classList.toggle('active');
    const svg = btn.querySelector('path');
    if (!svg) return;
    if (btn.classList.contains('active')) {
      svg.setAttribute('fill', '#e55');
      svg.setAttribute('stroke', '#e55');
    } else {
      svg.setAttribute('fill', 'none');
      svg.setAttribute('stroke', 'currentColor');
    }
  };

  window.slide = changeRecipePage;

  window.GoiYMonAn = {
    addIngredient(name) {
      const trimmed = String(name || '').trim();
      if (!trimmed) return;
      const exists = detectedIngredients.some(
        (item) => item.name.toLowerCase() === trimmed.toLowerCase()
      );
      if (!exists) {
        detectedIngredients.push({ name: trimmed, confidence: 1 });
      }
      renderIngredients(detectedIngredients);
      refreshRecipesFromIngredients();
    },
    getIngredients() {
      return [...detectedIngredients];
    },
    reloadRecipes() {
      return loadDefaultRecipes();
    }
  };

  function initPage() {
    loadDefaultRecipes();

    if (typeof window.initProductSearch === 'function') {
      window.initProductSearch({
        onEnter(keyword) {
          location.href = keyword ? `/?search=${encodeURIComponent(keyword)}` : '/';
        },
        onSelect(product) {
          if (window.GoiYMonAn?.addIngredient) {
            window.GoiYMonAn.addIngredient(product.name);
          } else {
            location.href = `/chi-tiet-san-pham.html?id=${product.id}`;
          }
        }
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initPage);
  } else {
    initPage();
  }
})();
