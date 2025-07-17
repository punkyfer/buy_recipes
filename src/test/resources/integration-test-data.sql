TRUNCATE TABLE cart_recipes, cart_items, carts, recipe_products, recipes, products RESTART IDENTITY CASCADE;

-- Seed Data
INSERT INTO products (id, name, price_in_cents) VALUES
(1, 'Flour', 200),
(2, 'Sugar', 150),
(3, 'Eggs', 300);

INSERT INTO recipes (id, name) VALUES
(101, 'Simple Cake'),
(102, 'Pancakes');

INSERT INTO recipe_products (recipe_id, product_id) VALUES (101, 1), (101, 2), (101, 3);
INSERT INTO recipe_products (recipe_id, product_id) VALUES (102, 1), (102, 3);

INSERT INTO carts (id, total_in_cents) VALUES (1, 0);