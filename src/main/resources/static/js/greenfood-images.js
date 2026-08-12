(function () {
  const PRODUCT_IMAGES = [
    "/img/caixoanorganic.webp",
    "/img/otchuongdo.webp",
    "/img/supcachuatrung.jpg",
    "/img/shakshuka.webp",
    "/img/omeletteraucu.png",
    "/img/intro.png"
  ];

  const RECIPE_IMAGES = [
    "/img/supcachuatrung.jpg",
    "/img/shakshuka.webp",
    "/img/omeletteraucu.png",
    "/img/otchuongdo.webp",
    "/img/caixoanorganic.webp"
  ];

  function pickFromList(list, idOrIndex) {
    const slot = Number(idOrIndex);
    const index = Number.isFinite(slot) && slot > 0 ? slot : 0;
    return list[Math.abs(index) % list.length];
  }

  window.GreenFoodImages = {
    PRODUCT_IMAGES,
    RECIPE_IMAGES,
    product(idOrIndex, imageUrl) {
      if (imageUrl) return imageUrl;
      const productId = Number(idOrIndex);
      if (Number.isFinite(productId) && productId > 0) {
        return `/img_product/${productId}.png`;
      }
      return pickFromList(PRODUCT_IMAGES, idOrIndex);
    },
    recipe(recipe, index) {
      if (recipe?.imageUrl) return recipe.imageUrl;
      // Use correct folder for dish images – /img_dishes/{img}.png
      const imgId = recipe?.img ?? recipe?.id ?? index;
      if (imgId != null && imgId !== '' && imgId !== 0) {
        return `/img_dishes/${imgId}.png`;
      }
      return pickFromList(RECIPE_IMAGES, recipe?.id ?? index);
    },
    hero() {
      return "/img/intro.png";
    }
  };
})();
