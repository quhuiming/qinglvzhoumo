CREATE DATABASE IF NOT EXISTS qinglvzhoumo
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'qinglv'@'localhost'
  IDENTIFIED BY 'root123';

CREATE USER IF NOT EXISTS 'qinglv'@'127.0.0.1'
  IDENTIFIED BY 'root123';

CREATE USER IF NOT EXISTS 'qinglv'@'%'
  IDENTIFIED BY 'root123';

ALTER USER 'qinglv'@'localhost'
  IDENTIFIED BY 'root123';

ALTER USER 'qinglv'@'127.0.0.1'
  IDENTIFIED BY 'root123';

ALTER USER 'qinglv'@'%'
  IDENTIFIED BY 'root123';

GRANT ALL PRIVILEGES ON qinglvzhoumo.* TO 'qinglv'@'localhost';
GRANT ALL PRIVILEGES ON qinglvzhoumo.* TO 'qinglv'@'127.0.0.1';
GRANT ALL PRIVILEGES ON qinglvzhoumo.* TO 'qinglv'@'%';

USE qinglvzhoumo;

CREATE TABLE IF NOT EXISTS user_sessions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(96) NOT NULL,
  user_id BIGINT NOT NULL,
  device_id VARCHAR(128) NULL,
  created_at DATETIME(6) NOT NULL,
  last_seen_at DATETIME(6) NOT NULL,
  revoked_at DATETIME(6) NULL,
  UNIQUE KEY uk_user_session_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @table_exists = (
  SELECT COUNT(*) FROM information_schema.tables
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts'
);
SET @sql = IF(@table_exists = 1, 'ALTER TABLE user_accounts MODIFY COLUMN device_id VARCHAR(128) NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'phone'
);
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN phone VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'password_hash'
);
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN password_hash VARCHAR(128) NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'account_type'
);
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN account_type VARCHAR(16) NOT NULL DEFAULT ''ANONYMOUS''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'registered_at'
);
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN registered_at DATETIME(6) NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'last_login_at'
);
SET @sql = IF(@table_exists = 1 AND @column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN last_login_at DATETIME(6) NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND index_name = 'uk_user_accounts_phone'
);
SET @sql = IF(@table_exists = 1 AND @index_exists = 0, 'CREATE UNIQUE INDEX uk_user_accounts_phone ON user_accounts (phone)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

FLUSH PRIVILEGES;
