-- 添加 transaction_id 字段到 orders 表
ALTER TABLE orders ADD COLUMN transaction_id BIGINT;

-- 添加外键约束
ALTER TABLE orders ADD CONSTRAINT fk_orders_transaction 
    FOREIGN KEY (transaction_id) REFERENCES transactions(id);

-- 添加索引以提高查询性能
CREATE INDEX idx_orders_transaction_id ON orders(transaction_id);
