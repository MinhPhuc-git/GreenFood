window.storeData = {
  categories: [
    { id: "all", name: "Tất cả", count: 128 },
    { id: "vegetable", name: "Rau củ", count: 36 },
    { id: "fruit", name: "Trái cây", count: 28 },
    { id: "fresh", name: "Thực phẩm tươi", count: 24 },
    { id: "drink", name: "Đồ uống", count: 40 },
    { id: "sale", name: "Giảm giá", count: 22 }
  ],
  sections: [
    { id: "vegetable", title: "Rau củ sạch", count: 36 },
    { id: "fruit", title: "Trái cây tươi", count: 28 },
    { id: "fresh", title: "Thực phẩm tươi", count: 24 },
    { id: "drink", title: "Đồ uống", count: 40 }
  ],
  products: [
    {
      id: "green-mix",
      name: "Giỏ Rau Củ Hữu Cơ Green Mix",
      category: "vegetable",
      tag: "Rau củ",
      price: 189000,
      oldPrice: 229000,
      unit: "2.5kg",
      origin: "Đà Lạt",
      badge: "-17%",
      image: "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=85",
      desc: "Giỏ rau củ tươi gồm cải kale, cà rốt baby, dưa leo, cà chua bi và bông cải xanh. Thu hoạch trong ngày, đóng gói thoáng khí và giao nhanh.",
      stock: "Còn hàng",
      metrics: [["Xuất xứ", "Nông trại Đà Lạt"], ["Chuẩn sạch", "Hữu cơ 98%"], ["Giao hàng", "Trong 2 giờ"]],
      facts: ["Cải kale 350g", "Bông cải xanh 500g", "Cà rốt baby 400g", "Dưa leo baby 500g", "Cà chua bi 450g", "Rau thơm 300g"],
      gallery: [
        "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=90",
        "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1506806732259-39c2d0268443?auto=format&fit=crop&w=900&q=85"
      ]
    },
    { id: "kale", name: "Cải Kale Hữu Cơ Cắt Sẵn", category: "vegetable", tag: "Rau củ", price: 52000, unit: "350g", origin: "Mộc Châu", badge: "Mới", image: "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?auto=format&fit=crop&w=900&q=85" },
    { id: "bell-pepper", name: "Ớt Chuông Đỏ Tươi Giòn", category: "vegetable", tag: "Rau củ", price: 38000, unit: "500g", origin: "Đà Lạt", image: "https://images.unsplash.com/photo-1525607551316-4a8e16d1f9ba?auto=format&fit=crop&w=900&q=85" },
    { id: "strawberry", name: "Dâu Tây Mộc Châu", category: "fruit", tag: "Trái cây", price: 89000, unit: "500g", origin: "Mộc Châu", badge: "Hot", image: "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=900&q=85" },
    { id: "apple", name: "Táo Úc Giòn Ngọt", category: "fruit", tag: "Trái cây", price: 96000, unit: "1kg", origin: "Nhập khẩu", image: "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?auto=format&fit=crop&w=900&q=85" },
    { id: "banana", name: "Chuối Cau", category: "fruit", tag: "Trái cây", price: 42000, unit: "1 nải", origin: "Đồng Nai", image: "https://images.unsplash.com/photo-1587132137056-bfbf0166836e?auto=format&fit=crop&w=900&q=85" },
    { id: "chicken", name: "Ức Gà Không Da Đóng Gói", category: "fresh", tag: "Thực phẩm", price: 68000, unit: "500g", origin: "Trong ngày", image: "https://images.unsplash.com/photo-1604503468506-a8da13d82791?auto=format&fit=crop&w=900&q=85" },
    { id: "salmon", name: "Cá Hồi Phi Lê", category: "fresh", tag: "Thực phẩm", price: 145000, unit: "300g", origin: "Cấp lạnh", badge: "Premium", image: "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=900&q=85" },
    { id: "eggs", name: "Trứng Gà Thả Vườn", category: "fresh", tag: "Thực phẩm", price: 39000, unit: "10 quả", origin: "Ba Vì", image: "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=900&q=85" },
    { id: "orange-juice", name: "Nước Ép Cam Tươi Nguyên Chất", category: "drink", tag: "Đồ uống", price: 35000, unit: "300ml", origin: "Ngọt", image: "https://images.unsplash.com/photo-1622597467836-f3285f2131b8?auto=format&fit=crop&w=900&q=85" },
    { id: "nuts", name: "Hạt Mix Ăn Sáng GreenFood", category: "drink", tag: "Hạt dinh dưỡng", price: 78000, unit: "250g", origin: "Không đường", image: "https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=85" },
    { id: "mango-smoothie", name: "Sữa Xoài Tươi", category: "drink", tag: "Đồ uống", price: 59000, unit: "500ml", origin: "Ít ngọt", image: "https://images.unsplash.com/photo-1623065422902-30a2d299bbe4?auto=format&fit=crop&w=900&q=85" }
  ]
};