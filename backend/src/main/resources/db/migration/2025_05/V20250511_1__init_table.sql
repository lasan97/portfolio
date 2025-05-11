CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    provider          VARCHAR(30)         NOT NULL,
    provider_id       VARCHAR(255) UNIQUE NOT NULL,
    email             VARCHAR(255),
    nickname          VARCHAR(255),
    profile_image_url VARCHAR(255),
    role              VARCHAR(50)         NOT NULL,
    created_at        TIMESTAMP(6)        NOT NULL
);

CREATE TABLE introductions
(
    id            BIGSERIAL PRIMARY KEY,
    created_by_id BIGINT       NOT NULL REFERENCES users (id),
    updated_by_id BIGINT REFERENCES users (id),
    title         VARCHAR(255) NOT NULL,
    content       OID          NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6)
);

CREATE TABLE introduction_external_links
(
    introduction_id BIGINT NOT NULL REFERENCES introductions (id),
    name            VARCHAR(255),
    url             VARCHAR(255),
    logo_url        VARCHAR(255)
);

CREATE TABLE products
(
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL,
    price               NUMERIC(38, 2) NOT NULL,
    original_price      NUMERIC(38, 2) NOT NULL,
    description         VARCHAR(1000),
    category            VARCHAR(50)    NOT NULL,
    status              VARCHAR(30)    NOT NULL,
    thumbnail_image_url VARCHAR(255),
    created_at          TIMESTAMP(6)   NOT NULL,
    updated_at          TIMESTAMP(6)
);

CREATE TABLE product_stocks
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT UNIQUE REFERENCES products (id),
    quantity   INTEGER      NOT NULL,
    version    INTEGER      NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6)
);

CREATE TABLE stock_histories
(
    id                BIGSERIAL PRIMARY KEY,
    product_id        BIGINT REFERENCES products (id),
    changed_quantity  INTEGER      NOT NULL,
    previous_quantity INTEGER      NOT NULL,
    current_quantity  INTEGER      NOT NULL,
    reason            VARCHAR(30)  NOT NULL,
    memo              VARCHAR(500),
    created_at        TIMESTAMP(6) NOT NULL
);

CREATE TABLE product_carts
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id)
);

CREATE TABLE product_cart_items
(
    product_cart_id BIGINT  NOT NULL REFERENCES product_carts (id),
    product_id      BIGINT REFERENCES products (id),
    quantity        INTEGER NOT NULL
);

CREATE TABLE orders
(
    id               UUID PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES users (id),
    name             VARCHAR(255)   NOT NULL,
    phone            VARCHAR(255)   NOT NULL,
    order_status     VARCHAR(30)    NOT NULL,
    total_price      NUMERIC(38, 2) NOT NULL,
    address          VARCHAR(255)   NOT NULL,
    detail_address   VARCHAR(255),
    post_code        VARCHAR(255)   NOT NULL,
    delivery_request VARCHAR(255),
    fail_reason      VARCHAR(255),
    created_at       TIMESTAMP(6)   NOT NULL
);

CREATE TABLE order_items
(
    item_seq            INTEGER        NOT NULL,
    order_id            UUID           NOT NULL REFERENCES orders (id),
    product_id          BIGINT,
    name                VARCHAR(255)   NOT NULL,
    price               NUMERIC(38, 2) NOT NULL,
    original_price      NUMERIC(38, 2) NOT NULL,
    thumbnail_image_url VARCHAR(255),
    quantity            INTEGER,
    PRIMARY KEY (item_seq, order_id)
);

CREATE TABLE user_credits
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL REFERENCES users (id),
    amount     NUMERIC(38, 2) NOT NULL,
    version    INTEGER        NOT NULL,
    created_at TIMESTAMP(6)   NOT NULL,
    updated_at TIMESTAMP(6)   NOT NULL
);

CREATE TABLE user_credit_histories
(
    id                        BIGSERIAL PRIMARY KEY,
    user_credit_id            BIGINT         NOT NULL REFERENCES user_credits (id),
    amount                    NUMERIC(38, 2) NOT NULL,
    balance_after_transaction NUMERIC(38, 2) NOT NULL,
    transaction_type          VARCHAR(20)    NOT NULL,
    transaction_date_time     TIMESTAMP(6)   NOT NULL,
    description               VARCHAR(255)
);

CREATE TABLE payment_outbox
(
    id            UUID PRIMARY KEY,
    order_id      UUID         NOT NULL,
    saga_id       UUID         NOT NULL,
    saga_type     VARCHAR(30)  NOT NULL,
    version       INTEGER      NOT NULL,
    order_status  VARCHAR(30)  NOT NULL,
    outbox_status VARCHAR(30),
    saga_status   VARCHAR(30)  NOT NULL,
    payload       VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL,
    processed_at  TIMESTAMP(6)
);

CREATE TABLE product_stock_outbox
(
    id            UUID PRIMARY KEY,
    order_id      UUID         NOT NULL,
    saga_id       UUID         NOT NULL,
    saga_type     VARCHAR(30)  NOT NULL,
    version       INTEGER      NOT NULL,
    order_status  VARCHAR(30)  NOT NULL,
    outbox_status VARCHAR(30),
    saga_status   VARCHAR(30)  NOT NULL,
    payload       VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL,
    processed_at  TIMESTAMP(6)
);

CREATE TABLE product_stock_reduction_outbox
(
    id                   UUID PRIMARY KEY,
    order_id             UUID         NOT NULL,
    saga_id              UUID         NOT NULL,
    version              INTEGER      NOT NULL,
    outbox_status        VARCHAR(30),
    product_stock_status VARCHAR(30),
    payload              VARCHAR(255) NOT NULL,
    created_at           TIMESTAMP(6) NOT NULL,
    processed_at         TIMESTAMP(6)
);

CREATE TABLE user_credit_order_outbox
(
    id             UUID PRIMARY KEY,
    order_id       UUID         NOT NULL,
    saga_id        UUID         NOT NULL,
    outbox_status  VARCHAR(30),
    payment_status VARCHAR(30),
    payload        VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP(6) NOT NULL,
    processed_at   TIMESTAMP(6)
);