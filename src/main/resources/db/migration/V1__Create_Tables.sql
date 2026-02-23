-- =====================================================
-- Week7 Practice1 - Create Tables Script
-- Smart Inventory & Order Management System

-- =====================================================
-- DROP TABLES (for clean re-run in development)
-- =====================================================
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- CREATE PRODUCTS TABLE
-- =====================================================
CREATE TABLE products (
    -- Primary Key
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for each product',

    -- Business Fields
    name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Product name, must be unique',
    category VARCHAR(50) NOT NULL COMMENT 'Product category: ELECTRONICS, FOOD, or FASHION',
    price DECIMAL(19, 2) NOT NULL COMMENT 'Product price in IDR (must be positive)',
    stock INT NOT NULL DEFAULT 0 COMMENT 'Available quantity in inventory',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Soft delete flag (TRUE = active, FALSE = deleted)',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP NULL COMMENT 'Last update timestamp',
    created_by VARCHAR(255) COMMENT 'User who created the record',
    updated_by VARCHAR(255) COMMENT 'User who last updated the record',

    -- Constraints
    CONSTRAINT chk_price_positive CHECK (price > 0)  ,
    CONSTRAINT chk_stock_positive CHECK (stock >= 0) ,
    CONSTRAINT chk_food_max_price CHECK (
        category != 'FOOD' OR price <= 1000000
    )
) COMMENT 'Product catalog with inventory and pricing information';

-- Indexes for performance
CREATE INDEX idx_products_category_active ON products(category, active) COMMENT 'For filtering products by category and active status';
CREATE INDEX idx_products_name ON products(name) COMMENT 'For searching products by name';
CREATE INDEX idx_products_active ON products(active) COMMENT 'For filtering active/inactive products';

-- =====================================================
-- CREATE CUSTOMERS TABLE
-- =====================================================
CREATE TABLE customers (
    -- Primary Key
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for each customer',

    -- Business Fields
    name VARCHAR(255) NOT NULL COMMENT 'Customer full name',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Customer email address, must be unique',
    membership_level VARCHAR(50) NOT NULL DEFAULT 'REGULAR' COMMENT 'Membership tier: REGULAR, GOLD, or PLATINUM',
    total_spent DECIMAL(19, 2) NOT NULL DEFAULT 0.00 COMMENT 'Total amount spent by customer (for membership upgrade)',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Soft delete flag (TRUE = active, FALSE = deleted)',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP NULL COMMENT 'Last update timestamp',
    created_by VARCHAR(255) COMMENT 'User who created the record',
    updated_by VARCHAR(255) COMMENT 'User who last updated the record',

    -- Constraints
    CONSTRAINT chk_total_spent CHECK (total_spent >= 0) ,
    CONSTRAINT chk_valid_membership CHECK (membership_level IN ('REGULAR', 'GOLD', 'PLATINUM'))
) COMMENT 'Customer information with membership tracking';

-- Indexes for performance
CREATE INDEX idx_customers_email ON customers(email) COMMENT 'For looking up customers by email (unique)';
CREATE INDEX idx_customers_membership_active ON customers(membership_level, active) COMMENT 'For filtering by membership and active status';
CREATE INDEX idx_customers_total_spent ON customers(total_spent) COMMENT 'For sorting customers by spending';

-- =====================================================
-- CREATE ORDERS TABLE
-- =====================================================
CREATE TABLE orders (
    -- Primary Key
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for each order',

    -- Foreign Key
    customer_id BIGINT NOT NULL COMMENT 'Reference to customer who placed the order',

    -- Business Fields
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00 COMMENT 'Total order amount before discount',
    discount_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00 COMMENT 'Discount amount applied (based on membership)',
    final_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00 COMMENT 'Final amount to pay (total - discount)',
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED' COMMENT 'Order status: CREATED, PAID, or CANCELLED',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP NULL COMMENT 'Last update timestamp',
    created_by VARCHAR(255) COMMENT 'User who created the record',
    updated_by VARCHAR(255) COMMENT 'User who last updated the record',

    -- Foreign Key Constraints
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT ON UPDATE CASCADE ,

    -- Check Constraints
    CONSTRAINT chk_total_amount CHECK (total_amount >= 0) ,
    CONSTRAINT chk_discount_amount CHECK (discount_amount >= 0) ,
    CONSTRAINT chk_final_amount CHECK (final_amount >= 0) ,
    CONSTRAINT chk_valid_status CHECK (status IN ('CREATED', 'PAID', 'CANCELLED'))
) COMMENT 'Order header with customer, pricing, and status information';

-- Indexes for performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id) COMMENT 'For finding orders by customer';
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC) COMMENT 'For filtering by status with recent orders first';
CREATE INDEX idx_orders_created_at ON orders(created_at DESC) COMMENT 'For sorting orders by date (newest first)';

-- =====================================================
-- CREATE ORDER_ITEMS TABLE
-- =====================================================
CREATE TABLE order_items (
    -- Primary Key
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for each order item',

    -- Foreign Keys
    order_id BIGINT NOT NULL COMMENT 'Reference to parent order',
    product_id BIGINT NOT NULL COMMENT 'Reference to product being ordered',

    -- Business Fields
    quantity INT NOT NULL COMMENT 'Quantity of product ordered',
    price_at_purchase DECIMAL(19, 2) NOT NULL COMMENT 'Product price at time of order (for historical accuracy)',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP NULL COMMENT 'Last update timestamp',

    -- Foreign Key Constraints
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE ,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT ON UPDATE CASCADE ,

    -- Check Constraints
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0) ,
    CONSTRAINT chk_price_at_purchase CHECK (price_at_purchase >= 0)
) COMMENT 'Order line items linking orders to products with quantities and prices';

-- Indexes for performance
CREATE INDEX idx_order_items_order_id ON order_items(order_id) COMMENT 'For finding all items in an order';
CREATE INDEX idx_order_items_product_id ON order_items(product_id) COMMENT 'For finding all orders for a product';

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
SELECT 'Products table created successfully!' AS status;
SELECT COUNT(*) AS table_count FROM information_schema.tables
WHERE table_name IN ('products', 'customers', 'orders', 'order_items');
