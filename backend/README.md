# 情侣周末 Java 后端

Spring Boot 后端 MVP，用于让两个人共享愿望、回忆和每日记录。

## 本地 MySQL80 运行

你当前电脑已经有 Windows 本机 MySQL80 服务，端口是 `3306`。先创建项目数据库和账号。

方式一：在 Navicat 或 MySQL 命令行里执行：

```sql
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
```

方式二：如果你有 root 密码，可以执行：

```bash
cd backend
mysql -u root -p < sql/init-local-mysql.sql
```

然后启动后端：

```bash
cd backend
mvn -s maven-settings.xml spring-boot:run
```

默认连接：

- 数据库：`qinglvzhoumo`
- 用户名：`qinglv`
- 密码：`qinglvzhoumo_dev`
- 端口：`3306`

如需连接其他 MySQL，可覆盖环境变量：

```bash
set DB_URL=jdbc:mysql://localhost:3306/qinglvzhoumo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
set DB_USER=qinglv
set DB_PASSWORD=qinglvzhoumo_dev
mvn -s maven-settings.xml spring-boot:run
```

## Docker MySQL 备用

如果以后 Docker Desktop 可用，也可以启动项目自带的 MySQL。为了避免和本机 MySQL80 冲突，Docker MySQL 暴露在 `3307`：

```bash
cd backend
docker compose up -d
set DB_URL=jdbc:mysql://localhost:3307/qinglvzhoumo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
mvn -s maven-settings.xml spring-boot:run
```

## 测试

测试环境使用 H2 内存数据库，不依赖 MySQL：

```bash
mvn -s maven-settings.xml test "-Dspring.profiles.active=test"
```

## 核心接口

- `POST /api/auth/anonymous`：匿名设备登录，返回 `token`
- `POST /api/couples/invite`：生成或获取情侣邀请码
- `POST /api/couples/join`：输入邀请码加入情侣空间
- `GET /api/sync`：拉取当前情侣空间共享数据
- `POST /api/sync`：上传愿望、回忆、每日记录等同步项

除登录外，前端请求需要带：

```http
Authorization: Bearer <token>
```
