-- 查询所有管理员的权限信息
SELECT 
    id,
    username,
    name,
    role,
    permissions,
    status
FROM admin
ORDER BY id;

-- 如果您的管理员用户名是 'SoftWind' 或 'admin'，可以单独查询
SELECT 
    id,
    username,
    name,
    role,
    permissions,
    status
FROM admin
WHERE username IN ('SoftWind', 'admin', 'softwind');
