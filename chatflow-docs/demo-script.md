# ChatFlow 验收演示脚本

## 1. 启动
执行 Docker Compose，确认 MySQL、Redis、Backend、Nginx 均运行。
打开 /actuator/health，确认服务健康。

## 2. 注册与登录
在 Android 输入新用户名、密码和昵称。
注册成功后登录，进入会话列表。

## 3. 会话与消息
创建会话。
发送“hello chatflow”。
确认消息进入历史，并通过 WebSocket 在聊天页实时出现。

## 4. 工作流
选择可运行工作流并触发。
确认页面显示“工作流：WAITING”。
输入答案并提交。
确认状态变为“工作流：SUCCESS”。

## 5. 自动回复
确认工作流 SEND_MESSAGE 产生的自动回复通过 WebSocket 出现在当前会话。
返回会话列表，再进入聊天页，确认历史仍可读取。

## 6. 结束验收
记录 Compose 服务状态、/actuator/health 返回、Android APK 版本、后端 CI/Android CI/Deployment CI 结果和最终 E2E 结果。
任何未实际执行的项目标记为“未验证”，不要标记 PASS。
