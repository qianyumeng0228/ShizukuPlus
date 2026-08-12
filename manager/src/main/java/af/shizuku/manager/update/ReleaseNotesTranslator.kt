package af.shizuku.manager.update

import java.util.Locale

object ReleaseNotesTranslator {

    private val exactLines = mapOf(
        "## Changes since" to "## 相比",
        "## Recent Changes" to "## 最近更新",
        "### ✨ New Features" to "### 新功能",
        "### 🐛 Bug Fixes" to "### 问题修复",
        "### 🔥 Crash & Stability Fixes" to "### 崩溃与稳定性修复",
        "### 🔧 Improvements & Refactors" to "### 改进与重构",
        "## 📦 Recent Releases" to "## 近期版本",
        "| Release | Highlight |" to "| 版本 | 重点 |",
        "|:--|:--|" to "|:--|:--|",
        "---" to "---"
    )

    private val phraseRules = listOf(
        "Most recent major release" to "最近的重要版本",
        "Most recent critical fix" to "最近的关键修复",
        "this release" to "本版本",
        "Full Changelog" to "完整变更记录",
        "Changes since" to "相比",
        "Recent Changes" to "最近更新",
        "New Features" to "新功能",
        "Bug Fixes" to "问题修复",
        "Crash & Stability Fixes" to "崩溃与稳定性修复",
        "Improvements & Refactors" to "改进与重构",
        "Recent Releases" to "近期版本",
        "Third-party apps" to "第三方应用",
        "that Shizuku+ grants permissions to" to "被 Shizuku+ 授权后",
        "now reliably detect and connect to the service" to "现在可以可靠地检测并连接服务",
        "Closed the last gap" to "补齐了最后一个缺口",
        "Cached Apps Freezer guard" to "缓存应用冻结保护",
        "first connect callback" to "首次连接回调",
        "could be silently dropped" to "可能被静默丢弃",
        "client app" to "客户端应用",
        "frozen in the background" to "在后台被冻结",
        "process was still starting" to "进程仍在启动时",
        "Releases" to "版本",
        "all shipped" to "都发布了",
        "under the Plus APK's filename" to "但使用了 Plus APK 的文件名",
        "anyone who installed" to "安装了",
        "actually got" to "实际得到的是",
        "which fails to install or update alongside" to "因此无法与其并存安装或更新",
        "Fixed by selecting release assets by verified applicationId instead of directory order." to
            "已改为按校验后的 applicationId 选择发布资产，而不是依赖目录顺序。",
        "fix/refactor" to "修复/重构",
        "fix" to "修复",
        "improve" to "改进",
        "chore" to "维护",
        "server" to "服务端",
        "receiver" to "广播接收器",
        "main-thread sleep" to "主线程休眠",
        "caps null-check" to "能力标志空值检查",
        "submodule" to "子模块",
        "remove legacy case" to "移除旧版分支",
        "rish shell" to "rish shell",
        "always treated as unidentified" to "总是被识别为未确认身份",
        "UID fallback chain" to "UID 回退链",
        "power-save whitelist" to "省电白名单",
        "bindApplication retry" to "bindApplication 重试",
        "monitor" to "监控",
        "transport" to "传输方式",
        "clarify" to "明确",
        "Watchdog scope" to "看门狗作用范围",
        "Drop-In build" to "Drop-In 版本",
        "Drop-In" to "Drop-In",
        "Compat" to "兼容包",
        "Plus APK" to "Plus APK",
        "stock Shizuku" to "原版 Shizuku",
        "Stock Shizuku" to "原版 Shizuku",
        "Shizuku+" to "Shizuku+",
        "ShizukuPlus" to "ShizukuPlus",
        "Shizuku" to "Shizuku",
        "UserService" to "UserService",
        "applicationId" to "applicationId",
        "ADB" to "ADB",
        "RNDIS" to "RNDIS",
        "Ethernet" to "以太网",
        "WifiList" to "WifiList",
        "App Ops" to "App Ops",
        "Hail" to "Hail",
        "Tasker" to "Tasker"
    )

    private data class PrefixRule(
        val regex: Regex,
        val replacement: (MatchResult) -> String
    )

    private val commitTypePrefixes = listOf(
        PrefixRule(Regex("""(^|\s)fix\((#[0-9]+)\):""")) { "${it.groupValues[1]}修复(${it.groupValues[2]})：" },
        PrefixRule(Regex("""(^|\s)fix:""")) { "${it.groupValues[1]}修复：" },
        PrefixRule(Regex("""(^|\s)improve\((#[0-9]+)\):""")) { "${it.groupValues[1]}改进(${it.groupValues[2]})：" },
        PrefixRule(Regex("""(^|\s)improve:""")) { "${it.groupValues[1]}改进：" },
        PrefixRule(Regex("""(^|\s)feat\((#[0-9]+)\):""")) { "${it.groupValues[1]}新功能：" },
        PrefixRule(Regex("""(^|\s)feat:""")) { "${it.groupValues[1]}新功能：" },
        PrefixRule(Regex("""(^|\s)chore:""")) { "${it.groupValues[1]}维护：" },
        PrefixRule(Regex("""(^|\s)server:""")) { "${it.groupValues[1]}服务端：" }
    )

    fun shouldTranslate(locale: Locale = Locale.getDefault()): Boolean =
        locale.language.equals("zh", ignoreCase = true)

    fun translateIfNeeded(notes: String, locale: Locale = Locale.getDefault()): String =
        if (shouldTranslate(locale)) translate(notes) else notes

    fun translate(notes: String): String {
        if (notes.isBlank()) return notes
        return notes.lineSequence()
            .map { translateLine(it) }
            .joinToString("\n")
    }

    private fun translateLine(line: String): String {
        exactLines[line.trim()]?.let { return it }

        var result = line
            .replace("—", " - ")
            .replace("–", "-")

        result = result.replace(Regex("""_(\d+) distinct GitHub issue\(s\) referenced in this release\._""")) {
            "_本版本关联了 ${it.groupValues[1]} 个 GitHub issue。_"
        }

        commitTypePrefixes.forEach { rule ->
            result = rule.regex.replace(result, rule.replacement)
        }

        phraseRules.forEach { (source, target) ->
            result = result.replace(source, target)
        }

        result = result.replace(Regex("""## 相比 (v[0-9][^\s]*)""")) {
            "## 相比 ${it.groupValues[1]} 的变化"
        }

        result = result.replace(" | 修复", " | 修复")
        result = result.replace("  ", " ")
        return result
    }
}
