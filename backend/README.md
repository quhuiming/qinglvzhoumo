# 情侣周末 Java 后端

Spring Boot 后端 MVP，用于让两个人共享愿望、回忆和每日记录。

## 本地运行

先启动 MySQL：

```bash
cd backend
docker compose up -d
```

再启动后端：

```bash
mvn -s maven-settings.xml spring-boot:run
```

默认连接本机 Docker MySQL：

- 数据库：`qinglvzhoumo`
- 用户名：`qinglv`
- 密码：`qinglvzhoumo_dev`
- 端口：`3307`，避免和本机已安装的 MySQL80 默认端口冲突

如需连接其他 MySQL，可覆盖环境变量：

```bash
set DB_URL=jdbc:mysql://localhost:3307/qinglvzhoumo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
set DB_USER=qinglv
set DB_PASSWORD=qinglvzhoumo_dev
mvn -s maven-settings.xml spring-boot:run
```

## 测试

测试环境使用 H2 内存数据库，不依赖 Docker：

```bash
mvn -s maven-settings.xml test -Dspring.profiles.active=test
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
