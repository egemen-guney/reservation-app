CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- CUSTOMER, RESTAURANT_OWNER, ADMIN
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer (
    customer_id UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE REFERENCES account(account_id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS address (
    address_id UUID PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS menu (
    menu_id UUID PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS restaurant (
    restaurant_id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    address_id UUID NOT NULL UNIQUE REFERENCES address(address_id) ON DELETE CASCADE,
    bus_phone VARCHAR(20) NOT NULL,
    menu_id UUID NOT NULL UNIQUE REFERENCES menu(menu_id) ON DELETE CASCADE,
    stars DECIMAL(2, 1) NOT NULL CHECK (stars >= 0 AND stars <= 5),
    opening_hours TIME NOT NULL,
    closing_hours TIME NOT NULL,
    -- capacity INTEGER NOT NULL CHECK (capacity > 0),
    is_open BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS menu_item (
    menu_item_id UUID PRIMARY KEY,
    menu_id UUID NOT NULL REFERENCES menu(menu_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    price DECIMAL(10, 2) NOT NULL,
    satisfaction INTEGER NOT NULL CHECK (satisfaction >= 0 AND satisfaction <= 100)
);

CREATE TABLE IF NOT EXISTS seating_area (
    area_id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    name VARCHAR(30) NOT NULL CHECK (name IN ('INDOOR', 'OUTDOOR', 'BAR')),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    UNIQUE (restaurant_id, name)
);

CREATE TABLE IF NOT EXISTS reservation (
    res_id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
    area_id UUID NOT NULL REFERENCES seating_area(area_id) ON DELETE CASCADE,
    res_date DATE NOT NULL,                               -- e.g., '2026-05-25'
    start_time TIME WITH TIME ZONE NOT NULL,               -- e.g., '19:00:00+03'
    end_time TIME WITH TIME ZONE NOT NULL,                 -- e.g., '21:00:00+03'
    num_people INTEGER NOT NULL CHECK (num_people > 0),
    note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, COMPLETED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY,
    res_id UUID NOT NULL REFERENCES reservation(res_id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
    total_price DECIMAL(10, 2) NOT NULL,
    cc_num VARCHAR(20) NOT NULL, -- will be encrypted or tokenized in the real application
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PAID, PREPARING, COMPLETED, REFUNDED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL REFERENCES menu_item(menu_item_id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, menu_item_id)
);

-- Enforces that a customer can only have ONE pending or confirmed booking at any given moment
CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active_reservation_per_customer
ON reservation (customer_id)
WHERE status IN ('PENDING', 'CONFIRMED');