## 更新内容

本次发布合入了上游预发布 r2335-r2339，并继续同步你的中文美化分支。

### 上游更新
- 修复 `ShizukuStateMachine.update()` 把崩溃误判成主动停止的问题，避免 watchdog 重新拉起失效。
- 新增 `Cut (Angular)` 形状样式。
- 恢复大屏和 DeX 的响应式双列网格。
- 修复 Shape Style 选择器里 `Zen` 选项说明不准确的问题。
- 采用 `AppCompatDelegate.setApplicationLocales()` 作为应用语言来源。

### 美化包更新
- 汉化设置、关于、帮助和反馈邮件相关文案。
- 反馈邮件目标改为 `support@xiaoyuanqiang.xyz`，设备信息仍保持自动获取。
- GitHub issue、帮助页和更新链接全部指向你的项目。
- 补回下载选择，区分标准版、Drop-In 版和 Compat-Hub。
- 修复预发布 / 稳定版更新频道切换，以及手动检查更新仍回到稳定版的问题。
- 同步图标 plus badge、One UI 风格、无障碍和触感细节。
