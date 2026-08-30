## 版本更新

- 合入上游预发布 r2335-r2339，并继续同步你的中文美化分支。
- 修复 `ShizukuStateMachine.update()` 把崩溃误判成主动停止的问题，避免 watchdog 重新拉起失效。
- 新增 `Cut (Angular)` 形状样式。
- 恢复大屏和 DeX 的响应式双列网格。
- 修复 Shape Style 选择器里 `Zen` 选项说明不准确的问题。
- 采用 `AppCompatDelegate.setApplicationLocales()` 作为应用语言来源。
- 外观里新增壁纸功能，内置 3 种壁纸可选：原版、白色初音、黑色初音。
