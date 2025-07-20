-- 添加会员结算相关字段到transactions表
ALTER TABLE transactions 
ADD COLUMN payment_type VARCHAR(20) DEFAULT 'cash',
ADD COLUMN member_id BIGINT,
ADD COLUMN member_name VARCHAR(50),
ADD COLUMN member_phone VARCHAR(20),
ADD COLUMN member_level VARCHAR(50),
ADD COLUMN original_amount DECIMAL(10,2),
ADD COLUMN discount_rate DECIMAL(3,2),
ADD COLUMN discount_amount DECIMAL(10,2),
ADD COLUMN actual_amount DECIMAL(10,2);

-- 为现有记录设置默认值
UPDATE transactions 
SET 
    payment_type = 'cash',
    original_amount = total_amount,
    actual_amount = total_amount,
    discount_rate = 1.00,
    discount_amount = 0.00
WHERE payment_type IS NULL;

-- 添加注释
COMMENT ON COLUMN transactions.payment_type IS '支付类型：cash-正常结算，member-会员结算';
COMMENT ON COLUMN transactions.member_id IS '会员ID（会员结算时）';
COMMENT ON COLUMN transactions.member_name IS '会员姓名（会员结算时）';
COMMENT ON COLUMN transactions.member_phone IS '会员手机号（会员结算时）';
COMMENT ON COLUMN transactions.member_level IS '会员等级（会员结算时）';
COMMENT ON COLUMN transactions.original_amount IS '原始金额（折扣前）';
COMMENT ON COLUMN transactions.discount_rate IS '折扣率（1.00表示无折扣）';
COMMENT ON COLUMN transactions.discount_amount IS '折扣金额';
COMMENT ON COLUMN transactions.actual_amount IS '实际收款金额（折扣后）';
