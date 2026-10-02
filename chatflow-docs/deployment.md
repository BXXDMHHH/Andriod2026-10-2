# ChatFlow 部署手册

## 1. 部署组成
- mysql：MySQL 8，持久化业务数据。
- redis：Redis 7，提供运行时基础设施依赖。
- backend：Spring Boot API + STOMP WebSocket，容器内监听 8080。
- nginx：统一 HTTP 入口，监听 80，并转发 /ws WebSocket Upgrade。

生产环境请把敏感配置放入部署环境或秘密管理系统，不要提交真实密码、JWT_SECRET 或证书。

## 2. 配置
复制 chatflow-deploy/.env.example 为 chatflow-deploy/.env，并修改 MYSQL_PASSWORD、MYSQL_ROOT_PASSWORD、DB_PASSWORD、JWT_SECRET。
Compose 内 DB_URL 使用 mysql:3306，不要改成 localhost。

## 3. 启动
docker compose --env-file chatflow-deploy/.env -f chatflow-deploy/docker-compose.yml up -d --build

查看状态：
docker compose --env-file chatflow-deploy/.env -f chatflow-deploy/docker-compose.yml ps

健康检查：
curl http://localhost/actuator/health

停止：
docker compose --env-file chatflow-deploy/.env -f chatflow-deploy/docker-compose.yml down

## 4. Android 连接
模拟器开发环境可使用 http://10.0.2.2:8080/。
通过 Nginx 部署后，应把 Android API_BASE_URL 和 WS_URL 指向实际服务器。
正式 HTTPS 部署使用 HTTPS/WSS。

## 5. Nginx
nginx.conf 已包含 REST/Actuator 反向代理、/ws Upgrade、X-Forwarded-* 请求头和 WebSocket 长连接超时。
TLS、域名和证书由正式环境配置；当前文件提供 HTTP 部署基线。

## 6. 发布检查
1. docker compose ... config 校验 Compose。
2. docker compose ... up -d --build 构建并启动。
3. 检查 /actuator/health。
4. 注册、登录、创建会话、发消息。
5. 验证 WebSocket 实时消息。
6. 启动 WAITING 工作流、提交输入、验证 SUCCESS。
7. 运行后端测试和 Android 测试。
