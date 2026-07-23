-- 修改轮播图表结构
-- 确保image字段长度足够存储文件路径
ALTER TABLE carousel MODIFY COLUMN image VARCHAR(500) COMMENT '图片路径';

-- 如果需要，可以将link字段设为可空
ALTER TABLE carousel MODIFY COLUMN link VARCHAR(500) NULL COMMENT '链接地址';

-- 查看表结构
DESCRIBE carousel;
