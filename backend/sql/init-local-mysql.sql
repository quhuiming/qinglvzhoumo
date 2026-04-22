CREATE DATABASE IF NOT EXISTS qinglvzhoumo
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'qinglv'@'localhost'
  IDENTIFIED BY 'qinglvzhoumo_dev';

CREATE USER IF NOT EXISTS 'qinglv'@'127.0.0.1'
  IDENTIFIED BY 'qinglvzhoumo_dev';

GRANT ALL PRIVILEGES ON qinglvzhoumo.* TO 'qinglv'@'localhost';
GRANT ALL PRIVILEGES ON qinglvzhoumo.* TO 'qinglv'@'127.0.0.1';

FLUSH PRIVILEGES;
