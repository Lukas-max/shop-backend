CREATE TABLE IF NOT EXISTS product_category (
category_id BIGSERIAL PRIMARY KEY,
category_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS product (
product_id BIGSERIAL PRIMARY KEY,
sku VARCHAR (255),
name VARCHAR (255),
description TEXT,
unit_price NUMERIC (19,2),
product_image bytea,
active boolean,
units_in_stock INTEGER,
date_time_created TIMESTAMP without time zone,
date_time_updated TIMESTAMP without time zone,
category_id BIGINT REFERENCES product_category(category_id)
);

CREATE TABLE IF NOT EXISTS users (
user_id BIGSERIAL PRIMARY KEY ,
username VARCHAR(45),
password VARCHAR(250),
email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
role_id SERIAL PRIMARY KEY,
role VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
user_id BIGINT REFERENCES users(user_id),
role_id BIGINT REFERENCES roles(role_id),
PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS customer (
customer_id BIGSERIAL PRIMARY KEY,
first_name VARCHAR (255),
last_name VARCHAR (255),
telephone BIGINT,
email VARCHAR (255),
country VARCHAR (255),
street VARCHAR (255),
house_number VARCHAR (255),
apartment_number VARCHAR (255),
postal_code VARCHAR (255),
city VARCHAR (255)
);

CREATE TABLE IF NOT EXISTS customer_order (
order_id BIGSERIAL PRIMARY KEY,
total_price NUMERIC(19,2),
total_quantity INTEGER,
customer_id BIGINT REFERENCES customer(customer_id),
user_id BIGINT REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
product_id BIGINT,
product_name VARCHAR (255),
unit_price_at_bought NUMERIC(19,2),
quantity INTEGER,
order_id BIGINT REFERENCES customer_order(order_id)
);
