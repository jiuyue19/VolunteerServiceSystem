-- 为积分商品表添加筛选字段
-- 执行日期：2025-12-03
-- 说明：添加材质、颜色、适用人群、适用季节、其他特性等筛选字段

USE volunteer_system;

-- 添加筛选相关字段
ALTER TABLE goods
ADD COLUMN material VARCHAR(50) COMMENT '材质（纯棉/混纺/塑料/金属/皮革等）' AFTER status,
ADD COLUMN color VARCHAR(50) COMMENT '颜色（红色/蓝色/绿色/黄色/白色/黑色等）' AFTER material,
ADD COLUMN target_group VARCHAR(50) COMMENT '适用人群（成人/儿童/老人/学生等）' AFTER color,
ADD COLUMN season VARCHAR(50) COMMENT '适用季节（春季/夏季/秋季/冬季/四季等）' AFTER target_group,
ADD COLUMN features VARCHAR(200) COMMENT '其他特性（防水/透气/保温/耐磨/易清洗等，多个用逗号分隔）' AFTER season;

-- 查看更新后的表结构
DESC goods;

SELECT 'Goods表筛选字段添加完成！' AS message;
