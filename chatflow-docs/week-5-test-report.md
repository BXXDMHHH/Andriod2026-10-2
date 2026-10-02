# 第 5 周真实联调与自动化测试报告

## 目标

建立“依赖服务 -> 后端 -> Android Emulator -> UI -> REST/WebSocket -> 数据库回查”的可重复自动化闭环。

## 已实现

### 1. 后端真实服务链路

CI 使用 `chatflow-deploy/docker-compose.yml` 启动：

- MySQL 8
- Redis 7

随后使用 Spring Boot 连接 CI MySQL，等待 `/actuator/health` 就绪。

### 2. 后端 API + 数据库 Smoke

`.github/scripts/mysql_api_smoke.py` 已覆盖：

1. 注册并取得 JWT
2. JWT `/auth/me` 校验
3. 创建会话
4. REST 发送消息
5. 历史消息回查
6. 未认证请求返回 401
7. 创建工作流
8. 启动工作流并断言 WAITING
9. 提交输入
10. 断言 SUCCESS
11. 回查 workflow run / node records
12. 回查工作流生成的消息

### 3. Android Emulator 真联调

新增 `RealBackendE2ETest`，Android Emulator 使用真实应用代码连接 `http://10.0.2.2:8080`，不是 Mock。

主链路：

`注册 -> Android UI 登录 -> 新建会话 -> UI 发消息 -> REST/历史回查 -> 创建工作流 -> UI 启动 -> WAITING -> UI 输入 -> SUCCESS -> 历史消息回查`

WebSocket 路径由真实 ChatFlowClient 建立 STOMP CONNECT/CONNECTED/SUBSCRIBE，并由工作流生成消息验证实时消息链路。

### 4. CI 触发闭环

Android workflow 新增 `real-backend-e2e` job：

- 启动 MySQL/Redis
- 启动 Spring Boot
- 等待健康检查
- 创建 Android Emulator
- 安装/运行 Android instrumentation tests
- 输出后端日志
- 无论成功失败都清理 Compose 服务

Android workflow 同时监听 Android、backend、deploy、CI scripts 变化，避免后端接口变化后 E2E 不执行。

## 通过标准

本轮不是“代码存在即通过”。最终闭环只有在 GitHub Actions 实际运行后，以下全部成功才记为 PASS：

- Android assembleDebug
- Android JVM unit tests
- Compose instrumentation smoke tests
- Real backend Android Emulator E2E
- MySQL/Redis integration smoke
- 后端 Maven verify

## 当前实际验证边界

本会话可以完成仓库代码、测试和 CI 配置，但当前执行环境无法直接启动 GitHub-hosted Android Emulator，因此没有虚构 CI 通过结果。已提交的 E2E 流程需要 GitHub Actions Runner 实际执行后才能得到最终 PASS/FAIL。

## 后续稳定性项

- 网络切换恢复
- 重复消息去重
- 页面生命周期 Socket 释放验证
- ViewModel 状态测试
- Repository/API mock 测试
