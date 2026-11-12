CREATE TABLE surprise_baskets (
    id UUID PRIMARY KEY,
    offer_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    status VARCHAR(50) NOT NULL
);

CREATE TABLE basket_orders (
    id UUID PRIMARY KEY,
    basket_id UUID NOT NULL,
    buyer_user_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price NUMERIC(12, 2) NOT NULL CHECK (total_price >= 0),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_order_basket FOREIGN KEY (basket_id) REFERENCES surprise_baskets (id)
);

CREATE INDEX idx_basket_orders_basket_id ON basket_orders (basket_id);

CREATE TABLE pickup_codes (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    redeemed_at TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT fk_pickup_code_order FOREIGN KEY (order_id) REFERENCES basket_orders (id)
);

-- offer_id and buyer_user_id are external UUIDs, with no cross-service foreign keys.
