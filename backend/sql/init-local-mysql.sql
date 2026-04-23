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

FLUSH PRIVILEGES;
