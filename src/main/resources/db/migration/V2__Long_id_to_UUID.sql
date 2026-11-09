-- V2: category.id and product.category_id change from BIGINT to BINARY(16).
-- product.id is already BINARY(16) in the current schema, so it is left untouched.

-- Step 1: give every category a new UUID, then point each product at its category's new id
ALTER TABLE category ADD COLUMN new_id BINARY(16);
UPDATE category SET new_id = UUID_TO_BIN(UUID());

ALTER TABLE product ADD COLUMN new_category_id BINARY(16);
UPDATE product p JOIN category c ON c.id = p.category_id SET p.new_category_id = c.new_id;

-- Step 2: drop the old foreign key and the old numeric columns
ALTER TABLE product DROP FOREIGN KEY FK1mtsbur82frn64de7balymq9s;
ALTER TABLE product DROP COLUMN category_id;

ALTER TABLE category MODIFY id BIGINT NOT NULL;  -- drop AUTO_INCREMENT so the primary key can be dropped
ALTER TABLE category DROP PRIMARY KEY, DROP COLUMN id;

-- Step 3: the new columns take the old names and become the keys
ALTER TABLE category CHANGE new_id id BINARY(16) NOT NULL, ADD PRIMARY KEY (id);
ALTER TABLE product
    CHANGE new_category_id category_id BINARY(16) NULL,
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id);
