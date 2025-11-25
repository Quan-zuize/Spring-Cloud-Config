-- PostgreSQL Database Schema for E-Commerce Core Domain
-- This script creates the necessary tables and indexes

-- Enable UUID extension (if you plan to use UUID in the future)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create sequence for base entity
CREATE SEQUENCE IF NOT EXISTS base_entity_sequence START WITH 1 INCREMENT BY 1;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY DEFAULT nextval('base_entity_sequence'),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_user_deleted ON users(is_deleted);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at);

-- ============================================
-- PRODUCTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY DEFAULT nextval('base_entity_sequence'),
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(12,2) NOT NULL,
    discount_price NUMERIC(12,2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(100),
    brand VARCHAR(100),
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    weight NUMERIC(10,2),
    dimensions VARCHAR(100),
    min_order_quantity INTEGER DEFAULT 1,
    max_order_quantity INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    -- Constraints
    CONSTRAINT chk_product_price CHECK (price > 0),
    CONSTRAINT chk_product_stock CHECK (stock_quantity >= 0)
);

-- Indexes for products table
CREATE INDEX IF NOT EXISTS idx_product_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_product_deleted ON products(is_deleted);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON products(created_at);
CREATE INDEX IF NOT EXISTS idx_product_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_product_stock ON products(stock_quantity);

-- ============================================
-- CART_ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY DEFAULT nextval('base_entity_sequence'),
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    -- Foreign keys
    CONSTRAINT fk_cart_item_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT uk_user_product UNIQUE (user_id, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_cart_unit_price CHECK (unit_price > 0)
);

-- Indexes for cart_items table
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_product_id ON cart_items(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_deleted ON cart_items(is_deleted);
CREATE INDEX IF NOT EXISTS idx_cart_created_at ON cart_items(created_at);

-- ============================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for users table
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for products table
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for cart_items table
DROP TRIGGER IF EXISTS update_cart_items_updated_at ON cart_items;
CREATE TRIGGER update_cart_items_updated_at
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================

-- Insert sample users
-- INSERT INTO users (username, email, password, first_name, last_name, role, status)
-- VALUES
--     ('admin', 'admin@ecommerce.com', '$2a$10$...', 'Admin', 'User', 'ADMIN', 'ACTIVE'),
--     ('customer1', 'customer1@example.com', '$2a$10$...', 'John', 'Doe', 'CUSTOMER', 'ACTIVE');

-- Insert sample products
-- INSERT INTO products (name, sku, description, price, stock_quantity, category, status)
-- VALUES
--     ('Sample Product 1', 'SKU001', 'This is a sample product', 99.99, 100, 'Electronics', 'ACTIVE'),
--     ('Sample Product 2', 'SKU002', 'Another sample product', 149.99, 50, 'Clothing', 'ACTIVE');

-- ============================================
-- USEFUL QUERIES
-- ============================================

-- Get all active products with stock
-- SELECT * FROM products WHERE status = 'ACTIVE' AND stock_quantity > 0 AND is_deleted = FALSE;

-- Get user's cart items with product details
-- SELECT ci.*, p.name, p.sku, u.username
-- FROM cart_items ci
-- JOIN products p ON ci.product_id = p.id
-- JOIN users u ON ci.user_id = u.id
-- WHERE ci.user_id = ? AND ci.is_deleted = FALSE;

-- Get products by category
-- SELECT * FROM products WHERE category = ? AND is_deleted = FALSE ORDER BY created_at DESC;

