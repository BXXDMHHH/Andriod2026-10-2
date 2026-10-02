# 第 4 周验收清单：Android 主功能

## 目标
按原开发计划完成 Android 主功能 MVP：
- 登录页
- 会话列表
- 聊天页
- WebSocket 实时接收
- 工作流触发和 WAITING 输入

## 已实现

### 认证
- Retrofit 调用 `POST /api/v1/auth/login`、`POST /api/v1/auth/register`。
- access token 保存到本地会话存储。
- HTTP 请求自动附加 `Authorization: Bearer <token>`。
- App 启动时检测本地 token，恢复到会话列表。

### 会话
- 查询当前用户会话列表。
- 创建新会话。
- 从列表进入聊天页。
- 聊天页可返回会话列表或退出登录。

### 聊天
- 拉取最近 50 条历史消息。
- REST 发送文本消息，并生成 clientMsgId。
- STOMP WebSocket 连接 `/ws`。
- CONNECT 带 JWT，订阅 `/topic/conversations/{conversationId}`。
- 收到 `message.created` 后更新 Compose 消息列表并按 id 去重。

### 工作流
- 在聊天页打开工作流选择弹窗。
- 查询 `GET /api/v1/workflows`。
- 调用 `POST /api/v1/workflows/{id}/run`。
- 工作流进入 WAITING 时显示输入区域。
- 调用 `POST /api/v1/workflow-runs/{id}/input` 提交用户输入。
- 显示运行状态和当前节点。

## 本周技术边界
- API/WebSocket 地址当前默认使用 Android Emulator 的 `10.0.2.2`。
- 本地开发允许 HTTP 明文流量；生产环境应改为 HTTPS/WSS。
- 当前消息发送走 REST，实时接收走 STOMP WebSocket。
- 尚未加入 Room 离线消息缓存、自动重连、消息已读/输入中等增强能力，这些留到联调与测试阶段处理。

## CI
Android workflow 执行：
1. Java 17
2. Android SDK 35
3. Gradle 8.7
4. `gradle assembleDebug`
5. `gradle testDebugUnitTest`
6. 上传 Debug APK artifact

## 第 5 周输入
下一周重点：
- 前后端真实环境联调
- WebSocket 重连与异常处理
- token 失效处理
- Android Repository/ViewModel 单元测试
- Compose UI 测试
- 完整端到端手工验收
