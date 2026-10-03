-- 事务演示建表脚本：account 转账表 + 四个初始账户（每个测试方法用独立内存库，数据互不干扰）
DROP TABLE IF EXISTS account;
CREATE TABLE account (
    id      INT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL,
    balance INT         NOT NULL
);
INSERT INTO account (id, name, balance) VALUES (1, 'alice', 1000);
INSERT INTO account (id, name, balance) VALUES (2, 'bob', 1000);
INSERT INTO account (id, name, balance) VALUES (3, 'carol', 1000);
INSERT INTO account (id, name, balance) VALUES (4, 'dave', 1000);

-- JDBC 细节演示表（batchUpdate / RowMapper / ResultSetExtractor）
DROP TABLE IF EXISTS order_log;
CREATE TABLE order_log (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    amount   INT         NOT NULL
);
