# 第 5 周计划：联调与稳定性

## 目标
把第 4 周 Android MVP 从代码链路完成推进到真实环境可验收。

## 任务

### A. Android 构建与测试
- [x] Android CI assembleDebug（CI 已配置）
- [x] JVM unit tests（CI 已配置）
- [x] Compose UI tests（CI 已配置）
- [x] Debug APK artifact 验证（CI 已配置）

### B. 真实后端联调
- [x] 登录 / 注册（后端 MySQL smoke + Android Emulator E2E）
- [x] 会话创建与列表（Android Emulator E2E）
- [x] 历史消息（Android Emulator E2E）
- [x] REST 发消息（Android Emulator E2E）
- [x] WebSocket 实时消息（Android Emulator E2E）
- [x] 工作流启动（Android Emulator E2E）
- [x] WAITING 输入（Android Emulator E2E）
- [x] 工作流 SUCCESS（Android Emulator E2E）

### C. 稳定性
- [x] STOMP 等待 CONNECTED 后订阅
- [x] WebSocket 指数退避重连
- [x] 401 自动退出
- [ ] 网络切换后的恢复
- [ ] 重复消息去重
- [ ] 页面生命周期下 Socket 释放

### D. 测试
- [x] STOMP parser unit test
- [ ] ViewModel 状态测试
- [ ] Repository/API mock 测试
- [x] Compose UI smoke test
- [x] Android emulator end-to-end test（真实 MySQL/Redis/Spring Boot）

### E. 完整验收
最终移动端主链路：注册 -> 登录 -> 会话 -> 聊天 -> WebSocket 实时消息 -> 工作流 -> WAITING -> 用户输入 -> SUCCESS

## 暂不扩展
Room 离线数据库、推送通知、多媒体消息、复杂工作流编辑器、HTTP_REQUEST 节点公网访问能力继续留在后续迭代。
## 本轮闭环实现

- `chatflow-deploy/docker-compose.yml` 启动 MySQL 8 + Redis 7。
- `.github/scripts/mysql_api_smoke.py` 验证注册、JWT、会话、消息、工作流 WAITING → SUCCESS 及数据库持久化。
- `RealBackendE2ETest` 在 Android Emulator 中使用真实 UI + 真实 HTTP + 真实 WebSocket 后端完成注册 → 登录 → 会话 → 消息 → 工作流 WAITING → 输入 → SUCCESS，并回查历史消息。
- `.github/workflows/android.yml` 新增 `real-backend-e2e`，先启动依赖与 Spring Boot，再运行 Emulator 测试；后端、部署脚本变化也会触发 Android E2E。

## 当前验证边界

代码和 CI 流程已闭环配置，但当前会话环境无法直接执行 GitHub Actions Runner，因此尚未把该次提交标记为“CI 已通过”。最终通过条件是 Android assemble/unit/instrumentation、真实后端 E2E 与 MySQL integration smoke 全部成功。

### 稳定性收尾实现

- `MessageDeduplicator` 统一处理历史消息与 WebSocket/REST 回来的重复 `message.id`。
- `ReconnectBackoff` 固化 1/2/4/8/15 秒退避，并增加 JVM 单测。
- Chat 返回、退出登录、ViewModel 清理均关闭 WebSocket 并取消重连任务；真实 E2E 增加返回会话页断言。
- 网络异常已有失败重连路径；“系统级网络断开→恢复”仍需 Android Emulator Runner 实际切换网络后验证，因此不标记为已通过。
