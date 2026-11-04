

CREATE DATABASE IF NOT EXISTS parfumerie
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE parfumerie;



CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),

    role ENUM('CUSTOMER','ADMIN')
        NOT NULL DEFAULT 'CUSTOMER',

    status ENUM('ACTIVE','LOCKED')
        NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    category_id BIGINT NOT NULL,

    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,

    price DECIMAL(15,2) NOT NULL,
    original_price DECIMAL(15,2),

    gender ENUM('NAM','NU','UNISEX')
        NOT NULL,

    fragrance_notes VARCHAR(255),

    volume VARCHAR(20) DEFAULT '100ml',

    concentration VARCHAR(50),

    description TEXT,

    stock INT NOT NULL DEFAULT 0,

    is_featured BOOLEAN DEFAULT FALSE,

    status ENUM('ACTIVE','INACTIVE')
        DEFAULT 'ACTIVE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT chk_product_price
        CHECK (price >= 0),

    CONSTRAINT chk_product_stock
        CHECK (stock >= 0)
);


CREATE TABLE product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    product_id BIGINT NOT NULL,

    image_url VARCHAR(500) NOT NULL,

    is_primary BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);


CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    recipient_name VARCHAR(100) NOT NULL,

    phone VARCHAR(20) NOT NULL,

    province VARCHAR(100) NOT NULL,

    district VARCHAR(100) NOT NULL,

    ward VARCHAR(100) NOT NULL,

    detail_address VARCHAR(255) NOT NULL,

    is_default BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);



CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);



CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    cart_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    quantity INT NOT NULL DEFAULT 1,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(id),

    UNIQUE (cart_id, product_id),

    CHECK (quantity > 0)
);



CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_code VARCHAR(50) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,

    recipient_name VARCHAR(100) NOT NULL,

    recipient_phone VARCHAR(20) NOT NULL,

    shipping_address TEXT NOT NULL,

    subtotal DECIMAL(15,2) NOT NULL,

    shipping_fee DECIMAL(15,2) DEFAULT 0,

    discount_amount DECIMAL(15,2) DEFAULT 0,

    total_amount DECIMAL(15,2) NOT NULL,

    payment_method ENUM(
        'COD',
        'BANK_TRANSFER',
        'ONLINE'
    ) NOT NULL DEFAULT 'COD',

    payment_status ENUM(
        'UNPAID',
        'PAID',
        'REFUNDED'
    ) DEFAULT 'UNPAID',

    status ENUM(
        'PENDING',
        'CONFIRMED',
        'SHIPPING',
        'DELIVERED',
        'CANCELLED'
    ) DEFAULT 'PENDING',

    note TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id),

    CHECK (subtotal >= 0),
    CHECK (shipping_fee >= 0),
    CHECK (discount_amount >= 0),
    CHECK (total_amount >= 0)
);


CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_id BIGINT NOT NULL,

    product_id BIGINT,

    product_name VARCHAR(200) NOT NULL,

    product_image VARCHAR(500),

    quantity INT NOT NULL,

    unit_price DECIMAL(15,2) NOT NULL,

    subtotal DECIMAL(15,2) NOT NULL,

    FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE SET NULL,

    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
);


CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_id BIGINT NOT NULL,

    method ENUM(
        'COD',
        'BANK_TRANSFER',
        'ONLINE'
    ) NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    status ENUM(
        'PENDING',
        'SUCCESS',
        'FAILED',
        'REFUNDED'
    ) DEFAULT 'PENDING',

    transaction_code VARCHAR(150),

    paid_at DATETIME,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CHECK (amount >= 0)
);



CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    rating INT NOT NULL,

    comment TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id),

    FOREIGN KEY (product_id)
        REFERENCES products(id),

    UNIQUE (user_id, product_id),

    CHECK (rating BETWEEN 1 AND 5)
);



CREATE TABLE order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_id BIGINT NOT NULL,

    old_status VARCHAR(30),

    new_status VARCHAR(30) NOT NULL,

    note TEXT,

    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);



CREATE INDEX idx_products_category
ON products(category_id);

CREATE INDEX idx_products_name
ON products(name);

CREATE INDEX idx_products_gender
ON products(gender);

CREATE INDEX idx_products_status
ON products(status);

CREATE INDEX idx_orders_user
ON orders(user_id);

CREATE INDEX idx_orders_status
ON orders(status);

CREATE INDEX idx_orders_created
ON orders(created_at);

CREATE INDEX idx_reviews_product
ON reviews(product_id);
USE parfumerie;

-- DANH MUC
INSERT INTO categories (name, slug, description)
VALUES (
    'Nước hoa',
    'nuoc-hoa',
    'Bộ sưu tập nước hoa cao cấp PARFUMERIE'
);

-- 6 SAN PHAM
INSERT INTO products (
    category_id,
    sku,
    name,
    slug,
    price,
    gender,
    fragrance_notes,
    volume,
    concentration,
    description,
    stock,
    is_featured
)
VALUES

(
    1,
    'PAR-001',
    'Royal Noir Extrait',
    'royal-noir-extrait-001',
    3850000,
    'NAM',
    'Chàm & Hổ Phách',
    '100ml',
    'Extrait de Parfum',
    'Hương thơm mạnh mẽ, nam tính và đầy vương giả dành cho những buổi tối sang trọng.',
    20,
    TRUE
),

(
    1,
    'PAR-002',
    'Fleur Blanche',
    'fleur-blanche-002',
    4450000,
    'NU',
    'Hoa Hồng & Xạ Hương',
    '100ml',
    'Eau de Parfum',
    'Sự hòa quyện ngọt ngào của các loài hoa trắng tinh khiết, gợi cảm và thanh lịch.',
    15,
    TRUE
),

(
    1,
    'PAR-003',
    'Golden Amber',
    'golden-amber-003',
    5100000,
    'UNISEX',
    'Vanille & Tiêu Đen',
    '100ml',
    'Extrait de Parfum',
    'Hương vanille ngọt ngào kết hợp tiêu đen cay nhẹ, tạo nên dấu ấn quyến rũ.',
    20,
    TRUE
),

(
    1,
    'PAR-004',
    'Royal Noir Extrait',
    'royal-noir-extrait-004',
    2850000,
    'NAM',
    'Gỗ Trầm & Hổ Phách',
    '100ml',
    'Extrait de Parfum',
    'Hương gỗ trầm sâu lắng kết hợp hổ phách ấm áp, mang vẻ nam tính bí ẩn.',
    25,
    FALSE
),

(
    1,
    'PAR-005',
    'Fleur Blanche',
    'fleur-blanche-005',
    2450000,
    'NU',
    'Hoa Nhài & Xạ Hương',
    '100ml',
    'Eau de Parfum',
    'Hương hoa nhài thanh khiết kết hợp xạ hương mềm mại, nữ tính và tinh tế.',
    30,
    FALSE
),

(
    1,
    'PAR-006',
    'Golden Amber',
    'golden-amber-006',
    3100000,
    'UNISEX',
    'Vanille & Tiêu Đen',
    '100ml',
    'Extrait de Parfum',
    'Sự hòa quyện của vanille ngọt dịu và tiêu đen cay nhẹ.',
    20,
    FALSE
);

-- KIEM TRA SAN PHAM
SELECT * FROM products;

