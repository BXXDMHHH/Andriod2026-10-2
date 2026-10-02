# 第 5 周计划：联调与稳定性

## 目标
把第 4 周 Android MVP 从代码链路完成推进到真实环境可验收。

## 任务

### A. Android 构建与测试
- [ ] Android CI assembleDebug
- [ ] JVM unit tests
- [ ] Compose UI tests
- [ ] Debug APK artifact 验证

### B. 真实后端联调
- [ ] 登录 / 注册
- [ ] 会话创建与列表
- [ ] 历史消息
- [ ] REST 发消息
- [ ] WebSocket 实时消息
- [ ] 工作流启动
- [ ] WAITING 输入
- [ ] 工作流 SUCCESS

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
- [ ] Compose UI smoke test
- [ ] Android emulator end-to-end test

### E. 完整验收
最终移动端主链路：注册 -> 登录 -> 会话 -> 聊天 -> WebSocket 实时消息 -> 工作流 -> WAITING -> 用户输入 -> SUCCESS

## 暂不扩展
Room 离线数据库、推送通知、多媒体消息、复杂工作流编辑器、HTTP_REQUEST 节点公网访问能力继续留在后续迭代。