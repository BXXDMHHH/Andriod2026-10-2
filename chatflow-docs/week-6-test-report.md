# 第六周部署与验收报告

## 目标
完成 Docker Compose 部署基线、Nginx、APK 打包、OpenAPI/部署文档，并保留第 5 周真实联调链路作为最终验收主流程。

## 已完成
1. chatflow-backend/Dockerfile：Maven 构建阶段 + Java 17 JRE 运行阶段，非 root 用户运行。
2. chatflow-deploy/docker-compose.yml：MySQL、Redis、Backend、Nginx 四服务。
3. chatflow-deploy/nginx.conf：REST 反向代理与 /ws WebSocket Upgrade。
4. chatflow-deploy/.env.example：部署配置模板。
5. chatflow-docs/deployment.md：启动、健康检查、Android 地址、发布检查。
6. chatflow-docs/openapi.yaml：当前已实现 REST API 的 OpenAPI 3.0.3 基线。
7. Android：版本 0.6.0，API_BASE_URL/WS_URL 支持 Gradle property 覆盖。
8. Deployment CI：Compose 配置、MySQL/Redis smoke、后端 API smoke，并纳入后端镜像部署栈。
9. Android CI：assembleDebug、JVM test、instrumentation、真实后端 E2E 和 APK artifact。

## 最终验收流程
注册 → 登录 → 创建会话 → 发消息 → WebSocket 收消息 → 触发 WAITING 工作流 → 提交输入 → SUCCESS → 自动回复。

## 当前验证边界
本会话环境不能直接启动 GitHub Actions Runner，也没有可用的真实服务器/Android 设备，因此不能把“配置完成”写成“远端 PASS”。实际 Runner、服务器部署、APK 安装结果应在对应环境执行后补入本报告。
