# 情侣周末 Java 后端

Spring Boot 后端 MVP，用于让两个人共享愿望、回忆和每日记录。

## MySQL 运行

当前默认使用你本机已有的 Docker 容器 `mysql8`，端口是 `3306`。

我已经在 `mysql8` 中创建了项目数据库和账号：

- 数据库：`qinglvzhoumo`
- 用户名：`qinglv`
- 密码：`root123`
- 端口：`3306`

启动后端：

```bash
cd backend
mvn -s maven-settings.xml spring-boot:run
```

默认连接：

```text
jdbc:mysql://localhost:3306/qinglvzhoumo
```

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

## 初始化 SQL

如果换了一台机器或重新建库，可以执行：

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
- `GET /api/couples/me`：查看当前设备的情侣空间状态
- `POST /api/couples/invite`：生成或获取情侣邀请码
- `POST /api/couples/join`：输入邀请码加入情侣空间
- `POST /api/couples/leave`：当前设备退出情侣空间，本机数据不删除
- `GET /api/sync`：拉取当前情侣空间共享数据
- `POST /api/sync`：上传愿望、回忆、每日记录等同步项

除登录外，前端请求需要带：

```http
Authorization: Bearer <token>
```
