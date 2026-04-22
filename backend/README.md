# 情侣周末 Java 后端

Spring Boot 后端 MVP，用于让两个人共享愿望、回忆和每日记录。

## Docker MySQL 运行

当前默认使用 Docker MySQL。为了避免和本机 MySQL80 的 `3306` 冲突，Docker MySQL 暴露在 `3307`。

先启动 MySQL：

```bash
cd backend
docker compose up -d
```

再启动后端：

```bash
mvn -s maven-settings.xml spring-boot:run
```

默认连接：

- 数据库：`qinglvzhoumo`
- 用户名：`qinglv`
- 密码：`qinglvzhoumo_dev`
- 端口：`3307`

前端 H5 默认连接 Java 后端：

```text
http://localhost:8080
```

Android 模拟器默认连接：

```text
http://10.0.2.2:8080
```

真机测试时，请在 App 的“我们”页把后端地址改成电脑局域网 IP，例如：

```text
http://192.168.x.x:8080
```

## 本机 MySQL80 备用

如果临时不用 Docker，可以覆盖环境变量改回本机 MySQL80：

```bash
set DB_URL=jdbc:mysql://localhost:3306/qinglvzhoumo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
set DB_USER=qinglv
set DB_PASSWORD=qinglvzhoumo_dev
mvn -s maven-settings.xml spring-boot:run
```

本机 MySQL 初始化脚本在：

```text
backend/sql/init-local-mysql.sql
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
