-- =====================================================
-- Week7 Practice1 - Dummy Data Script
-- Smart Inventory & Order Management System
-- =====================================================

-- =====================================================
-- INSERT PRODUCTS
-- =====================================================

-- Electronics Products (high value items)
INSERT INTO products (name, category, price, stock, active, created_at, updated_at) VALUES
('MacBook Pro M3', 'ELECTRONICS', 32500000, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iPhone 15 Pro Max', 'ELECTRONICS', 24999000, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Samsung Galaxy S24 Ultra', 'ELECTRONICS', 22999000, 25, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iPad Air M2', 'ELECTRONICS', 12999000, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sony WH-1000XM5 Headphones', 'ELECTRONICS', 4999000, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dell XPS 15 Laptop', 'ELECTRONICS', 28500000, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Logitech MX Master 3S', 'ELECTRONICS', 1599000, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Samsung 49 Inch Odyssey G9 Monitor', 'ELECTRONICS', 18500000, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ASUS ROG Strix G16', 'ELECTRONICS', 26500000, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GoPro Hero 12 Black', 'ELECTRONICS', 6999000, 35, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Food Products (max price 1,000,000 as per business rule)
INSERT INTO products (name, category, price, stock, active, created_at, updated_at) VALUES
('Kopi Arabika Premium 250g', 'FOOD', 85000, 200, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Teh Hijau Jasmine', 'FOOD', 45000, 150, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Keripik Singkong Balado', 'FOOD', 25000, 300, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Biskuit Coklat Belgia', 'FOOD', 75000, 120, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Madu Hutan Murni 500ml', 'FOOD', 150000, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kacang Almond Panggang', 'FOOD', 95000, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Minyak Zaitun Extra Virgin', 'FOOD', 180000, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sambal Bawang Spesial', 'FOOD', 35000, 250, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Rendang Daging Sapi', 'FOOD', 120000, 90, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kue Basah Lapis Legit', 'FOOD', 95000, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Fashion Products
INSERT INTO products (name, category, price, stock, active, created_at, updated_at) VALUES
('Kemeja Batik Premium', 'FASHION', 450000, 50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Celana Chino Slim Fit', 'FASHION', 350000, 80, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jaket Kulit Asli', 'FASHION', 1850000, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sneakers Casual Canvas', 'FASHION', 550000, 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tas Selempang Kulit', 'FASHION', 750000, 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jam Tangan Chronograph', 'FASHION', 1250000, 25, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kacamata Sun Polarized', 'FASHION', 450000, 55, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Topi Baseball Cap', 'FASHION', 150000, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ikat Pinggang Kulit', 'FASHION', 250000, 90, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Syal Rajut Winter', 'FASHION', 175000, 70, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- INSERT CUSTOMERS
-- =====================================================

-- Regular Customers (total_spent < 10,000,000)
INSERT INTO customers (name, email, membership_level, total_spent, active, created_at, updated_at) VALUES
('Ahmad Rizky', 'ahmad.rizky@email.com', 'REGULAR', 2500000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Siti Nurhaliza', 'siti.nurhaliza@email.com', 'REGULAR', 5750000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Budi Santoso', 'budi.santoso@email.com', 'REGULAR', 8900000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dewi Lestari', 'dewi.lestari@email.com', 'REGULAR', 3200000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Eko Prasetyo', 'eko.prasetyo@email.com', 'REGULAR', 4500000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Gold Customers (10,000,000 <= total_spent < 50,000,000)
INSERT INTO customers (name, email, membership_level, total_spent, active, created_at, updated_at) VALUES
('Feri Irawan', 'feri.irawan@email.com', 'GOLD', 15750000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Gita Pertiwi', 'gita.pertiwi@email.com', 'GOLD', 22500000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hendro Wijaya', 'hendro.wijaya@email.com', 'GOLD', 31250000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Indah Saraswati', 'indah.saraswati@email.com', 'GOLD', 18500000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Joko Anwar', 'joko.anwar@email.com', 'GOLD', 28750000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Platinum Customers (total_spent >= 50,000,000)
INSERT INTO customers (name, email, membership_level, total_spent, active, created_at, updated_at) VALUES
('Kartika Sari', 'kartika.sari@email.com', 'PLATINUM', 75000000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lukman Hakim', 'lukman.hakim@email.com', 'PLATINUM', 92500000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Maya Indah', 'maya.indah@email.com', 'PLATINUM', 58750000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- INSERT ORDERS
-- =====================================================

-- Created Orders (status: CREATED) - ready for payment
INSERT INTO orders (customer_id, total_amount, discount_amount, final_amount, status, created_at, updated_at) VALUES
(1, 32500000, 0, 32500000, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 130000, 0, 130000, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 4500000, 450000, 4050000, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 28500000, 2850000, 25650000, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 32500000, 9750000, 22750000, 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Paid Orders (status: PAID) - completed transactions
INSERT INTO orders (customer_id, total_amount, discount_amount, final_amount, status, created_at, updated_at) VALUES
(1, 24999000, 0, 24999000, 'PAID', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
(3, 12999000, 0, 12999000, 'PAID', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP),
(6, 22999000, 2299990, 20699010, 'PAID', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
(8, 1850000, 0, 1850000, 'PAID', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP),
(11, 24999000, 4999800, 19999200, 'PAID', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP);

-- Cancelled Orders (status: CANCELLED) - cancelled transactions
INSERT INTO orders (customer_id, total_amount, discount_amount, final_amount, status, created_at, updated_at) VALUES
(2, 4999000, 0, 4999000, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP),
(4, 1750000, 0, 1750000, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
(7, 550000, 55000, 495000, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP);

-- =====================================================
-- INSERT ORDER_ITEMS
-- =====================================================

-- Order Items for Created Orders
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, created_at, updated_at) VALUES
-- Order 1: MacBook Pro (Customer: Ahmad - Regular)
(1, 1, 1, 32500000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 2: Coffee & Tea combo (Customer: Siti - Regular)
(2, 11, 2, 85000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 12, 1, 45000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 3: Electronics combo (Customer: Feri - Gold, 10% discount)
(3, 6, 1, 28500000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Order 4: Phone + Accessories (Customer: Gita - Gold, 10% discount)
(4, 2, 1, 24999000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 21, 1, 450000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 22, 1, 350000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Order Items for Paid Orders
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, created_at, updated_at) VALUES
-- Order 6: iPhone purchase (Customer: Ahmad - Regular)
(6, 2, 1, 24999000, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
-- Order 7: iPad purchase (Customer: Budi - Regular)
(7, 4, 1, 12999000, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP),
-- Order 8: Samsung Galaxy (Customer: Feri - Gold, 10% discount)
(8, 3, 1, 22999000, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
-- Order 9: Jacket purchase (Customer: Hendro - Gold)
(9, 17, 1, 1850000, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP),
-- Order 10: iPhone purchase (Customer: Kartika - Platinum, 20% discount)
(10, 2, 1, 24999000, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP);

-- Order Items for Cancelled Orders
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase, created_at, updated_at) VALUES
-- Order 11: Headphones (Customer: Siti - Regular)
(11, 5, 1, 4999000, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP),
-- Order 12: Fashion item (Customer: Dewi - Regular)
(12, 25, 1, 175000, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
-- Order 13: Shoes (Customer: Gita - Gold)
(13, 19, 1, 550000, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
SELECT 'Dummy data inserted successfully!' AS status;

SELECT 'Products:' AS table_name, COUNT(*) AS row_count FROM products
UNION ALL
SELECT 'Customers:', COUNT(*) FROM customers
UNION ALL
SELECT 'Orders:', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items:', COUNT(*) FROM order_items;
