# 情侣周末 Java 后端

Spring Boot 后端 MVP，用于让两个人共享愿望、回忆和每日记录。

## 本地运行

```bash
cd backend
mvn -s maven-settings.xml spring-boot:run
```

默认使用 H2 文件数据库：`backend/data/qinglvzhoumo`。

## MySQL 运行

```bash
set SPRING_PROFILES_ACTIVE=mysql
set DB_URL=jdbc:mysql://localhost:3306/qinglvzhoumo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
set DB_USER=root
set DB_PASSWORD=your_password
mvn -s maven-settings.xml spring-boot:run
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
