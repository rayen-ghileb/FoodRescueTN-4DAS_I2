CREATE TABLE basket_descriptions (
    id UUID PRIMARY KEY,
    basket_id UUID NOT NULL,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    allergen_info VARCHAR(1000),
    CONSTRAINT fk_description_basket FOREIGN KEY (basket_id) REFERENCES surprise_baskets (id)
);

CREATE INDEX idx_basket_descriptions_basket_id ON basket_descriptions (basket_id);

CREATE TABLE payment_simulations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    amount_tnd NUMERIC(12, 2) NOT NULL CHECK (amount_tnd >= 0),
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT fk_payment_simulation_order FOREIGN KEY (order_id) REFERENCES basket_orders (id)
);
