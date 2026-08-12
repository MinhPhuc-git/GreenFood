-- GreenFood: Tích điểm khách hàng (loyalty_points)
-- Chạy trên database greenfood trước khi khởi động ứng dụng (hoặc để Hibernate ddl-auto=update tự thêm cột)

USE greenfood;

-- 1. Thêm cột điểm tích lũy trên bảng customer
ALTER TABLE `customer`
  ADD COLUMN IF NOT EXISTS `loyalty_points` INT NOT NULL DEFAULT 0
  COMMENT 'Tổng điểm tích lũy của khách hàng';

-- MySQL < 8.0.12 không hỗ trợ IF NOT EXISTS trên ADD COLUMN — dùng procedure hoặc bỏ qua lỗi duplicate
-- Nếu lỗi duplicate column, bỏ qua và chạy các lệnh UPDATE/SELECT bên dưới.

-- 2. Đánh dấu đơn đã cộng điểm (tránh cộng trùng)
ALTER TABLE `order`
  ADD COLUMN IF NOT EXISTS `reward_processed` TINYINT(1) NOT NULL DEFAULT 0
  COMMENT '1 = đã cộng điểm thưởng cho đơn này';

-- 3. Cộng điểm thủ công (ví dụ: đơn #7, khách #12, thanh toán 105.000đ => 105 điểm)
-- UPDATE `customer` c
-- INNER JOIN `order` o ON o.customer_id = c.id
-- SET c.loyalty_points = c.loyalty_points + FLOOR(o.total_amount / 1000),
--     o.reward_processed = 1
-- WHERE o.id = 7 AND o.reward_processed = 0 AND o.status IN ('PAID', 'COMPLETED', 'SUCCESS');

-- 4. Truy vấn tổng điểm của khách hàng
-- SELECT id, name, phone, loyalty_points AS total_points
-- FROM customer
-- WHERE id = 12;
