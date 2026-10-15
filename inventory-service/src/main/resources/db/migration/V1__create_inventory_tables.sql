CREATE TABLE inventory_items (
    product_id VARCHAR(255) PRIMARY KEY,
    available_quantity INTEGER NOT NULL,
    price NUMERIC (12, 2) NOT NULL CHECK (price > 0),
    CONSTRAINT check_inventory_non_negative CHECK (available_quantity >= 0)
)