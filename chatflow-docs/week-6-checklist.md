# 第六周：部署与验收清单

依据《聊天工作流.md》的第六周里程碑：Docker Compose 部署、Nginx 配置、APK 打包、文档完善、验收演示。

## A. 部署
- [x] 后端 Dockerfile
- [x] MySQL/Redis/Backend/Nginx Compose
- [x] 环境变量模板
- [x] Nginx REST 反向代理
- [x] Nginx WebSocket Upgrade
- [x] Docker Compose CI 配置校验
- [x] 后端镜像 CI 构建/集成 smoke 流程

## B. Android
- [x] Debug APK CI 构建
- [x] APK CI artifact
- [x] API_BASE_URL 可通过 Gradle property 覆盖
- [x] WS_URL 可通过 Gradle property 覆盖
- [x] 版本号提升到 0.6.0

## C. 文档
- [x] 部署手册
- [x] OpenAPI YAML
- [x] Week 5 测试报告承接
- [ ] 真实服务器部署结果
- [ ] GitHub Actions 最终 PASS 记录

## D. 验收演示
注册 → 登录 → 创建会话 → 发消息 → WebSocket 收消息 → 触发工作流 → WAITING → 输入 → SUCCESS → 自动回复。

## E. 验收边界
代码和 CI 配置已补齐第六周交付物；真实服务器公网部署、TLS、真实 APK 安装结果必须在对应运行环境执行后再记录 PASS。
