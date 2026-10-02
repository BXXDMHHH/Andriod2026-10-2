# 第 4 周测试报告

## 测试范围
- Android Gradle 配置
- 登录/注册 UI 与 REST 接口调用链
- 会话列表与创建会话
- 历史消息加载与 REST 发送
- STOMP CONNECT / CONNECTED / SUBSCRIBE / MESSAGE
- 工作流运行与 WAITING 输入
- Token 失效处理
- WebSocket 断线重连

## 已发现并修复
### 1. STOMP 订阅竞态
原实现 CONNECT 后立即发送 SUBSCRIBE，没有等待服务端 CONNECTED。
已修改为：CONNECT -> CONNECTED -> SUBSCRIBE -> MESSAGE

### 2. WebSocket 断线
原实现断线后只显示错误状态，不恢复连接。
现在增加 1s、2s、4s、8s、15s 上限的指数退避重连；离开聊天页或退出登录时取消重连任务。

### 3. Token 失效
HTTP 收到 401 时清理本地会话并回到登录态。
WebSocket 鉴权错误同样触发会话清理。

### 4. STOMP 帧解析
抽出 StompMessageParser，增加 JVM 单元测试，覆盖 CONNECTED、ERROR、MESSAGE body。

## 当前验证结果
### 已完成
- 源码级接口与页面链路检查
- STOMP 时序逻辑检查
- 重连生命周期检查
- 新增并提交 StompMessageParserTest

### 未完成
当前执行环境无法访问 GitHub 网络，因此无法在本地拉取 Gradle/Android 依赖并执行真实 APK 编译；GitHub Actions API 当前对最新 push run 返回空集合，不能据此声称 CI 已通过。

## 第 5 周入口
第 5 周继续做真实环境联调与测试：Android CI/Debug APK、Android 单元测试、Compose UI 测试、后端 + Android 真实服务联调、WebSocket 实机验证、Token 过期验证、工作流完整移动端验收。