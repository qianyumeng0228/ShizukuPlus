package af.shizuku.manager.update

import af.shizuku.manager.ShizukuApplication
import java.io.File
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

object ReleaseNotesTranslator {

    private const val TAG = "ReleaseNotesTranslator"
    private const val ZH_LIBRARY_ASSET = "i18n/release_notes_zh.json"

    private data class Rule(
        val source: String,
        val target: String
    )

    private data class LanguageLibrary(
        val exactLines: List<Rule>,
        val summaryRules: List<Rule>,
        val phraseRules: List<Rule>
    )

    private val fallbackLibrary = LanguageLibrary(
        exactLines = listOf(
            Rule("## Recent Changes", "## 最近更新"),
            Rule("### ✨ New Features", "### 新功能"),
            Rule("### 🐛 Bug Fixes", "### 问题修复"),
            Rule("### 🔥 Crash & Stability Fixes", "### 崩溃与稳定性修复"),
            Rule("### 🔧 Improvements & Refactors", "### 改进与重构"),
            Rule("## 📦 Recent Releases", "## 近期版本"),
            Rule("| Release | Highlight |", "| 版本 | 重点 |"),
            Rule("|:--|:--|", "|:--|:--|"),
            Rule("---", "---")
        ),
        summaryRules = listOf(
            Rule(
                "fix(#391): rish shell always treated as unidentified - UID fallback chain",
                "修复(#391)：rish shell 总是被识别为未确认身份，完善 UID 回退链"
            ),
            Rule(
                "fix/refactor: receiver main-thread sleep, ONEWAY reply, caps null-check",
                "修复/重构：修复广播接收器主线程休眠、ONEWAY 应答和能力标志空值检查"
            ),
            Rule(
                "server: re-apply power-save whitelist on each bindApplication retry",
                "服务端：每次 bindApplication 重试时重新应用省电白名单"
            ),
            Rule(
                "improve(#403): monitor RNDIS/Ethernet transport and clarify Watchdog scope",
                "改进(#403)：监控 RNDIS/以太网传输方式，并明确 Watchdog 作用范围"
            ),
            Rule(
                "chore: bump api submodule - remove legacy case 14 from isLegacy switch",
                "维护：更新 api 子模块，移除 isLegacy 判断中的旧版 case 14"
            ),
            Rule(
                "fix(#387): retry binder delivery and show actionable feedback on frozen-app failure",
                "修复(#387)：重试 binder 投递，并在应用冻结导致失败时显示可操作提示"
            ),
            Rule(
                "fix(#400, #402): fix live activity notification respecting toggle, fix SU bridge redeploy on read-only dex",
                "修复(#400, #402)：实时活动通知遵循开关设置，并修复只读 dex 环境下 SU bridge 重新部署的问题"
            ),
            Rule(
                "chore: add settings XML string reference check to pre-push guard",
                "维护：为 pre-push guard 增加设置 XML 字符串引用检查"
            ),
            Rule(
                "fix: correct string references in settings_shizuku_plus.xml",
                "修复：修正 settings_shizuku_plus.xml 中的字符串引用"
            ),
            Rule(
                "feat: auto-run snippets on Shizuku service start (#399)",
                "新功能：Shizuku 服务启动时自动运行代码片段 (#399)"
            )
        ),
        phraseRules = listOf(
            Rule(
                "Third-party apps that Shizuku+ grants permissions to (WifiList, App Ops, Hail, Tasker, and others) now reliably detect and connect to the service. Closed the last gap in the Cached Apps Freezer guard: a UserService's very first connect callback could be silently dropped if the client app was frozen in the background while its process was still starting.",
                "Shizuku+ 授权的第三方应用（WifiList、App Ops、Hail、Tasker 等）现在可以可靠地检测并连接服务。补齐了缓存应用冻结保护的最后一个缺口：如果客户端应用在后台被冻结，且其进程仍在启动，UserService 的首次连接回调可能会被静默丢弃。"
            ),
            Rule(
                "Releases r2141-r2152 all shipped the Drop-In build under the Plus APK's filename - anyone who installed \"Shizuku+\" in that window actually got moe.shizuku.privileged.api, which fails to install or update alongside stock Shizuku. Fixed by selecting release assets by verified applicationId instead of directory order.",
                "r2141-r2152 这些版本都把 Drop-In 版本打包到了 Plus APK 的文件名下；在此期间安装“Shizuku+”的用户实际得到的是 moe.shizuku.privileged.api，因此无法与原版 Shizuku 并存安装或更新。现已改为按校验后的 applicationId 选择发布资产，而不是依赖目录顺序。"
            ),
            Rule("Most recent major release", "最近的重要版本"),
            Rule("Most recent critical fix", "最近的关键修复"),
            Rule("Full Changelog", "完整变更记录"),
            Rule("Changes since", "相比"),
            Rule("Recent Changes", "最近更新"),
            Rule("New Features", "新功能"),
            Rule("Bug Fixes", "问题修复"),
            Rule("Crash & Stability Fixes", "崩溃与稳定性修复"),
            Rule("Improvements & Refactors", "改进与重构"),
            Rule("Recent Releases", "近期版本"),
            Rule("stock Shizuku", "原版 Shizuku"),
            Rule("Stock Shizuku", "原版 Shizuku"),
            Rule("Drop-In build", "Drop-In 版本"),
            Rule("UID fallback chain", "UID 回退链"),
            Rule("power-save whitelist", "省电白名单"),
            Rule("bindApplication retry", "bindApplication 重试"),
            Rule("Watchdog scope", "Watchdog 作用范围"),
            Rule("bump api submodule", "更新 api 子模块"),
            Rule("remove legacy case 14 from isLegacy switch", "移除 isLegacy 判断中的旧版 case 14"),
            Rule("shipped the Drop-In build under the Plus APK's filename", "把 Drop-In 版本打包到了 Plus APK 的文件名下"),
            Rule(
                "anyone who installed \"Shizuku+\" in that window actually got moe.shizuku.privileged.api",
                "在此期间安装“Shizuku+”的用户实际得到的是 moe.shizuku.privileged.api"
            ),
            Rule("which fails to install or update alongside stock Shizuku", "因此无法与原版 Shizuku 并存安装或更新"),
            Rule(
                "Fixed by selecting release assets by verified applicationId instead of directory order.",
                "现已改为按校验后的 applicationId 选择发布资产，而不是依赖目录顺序。"
            )
        )
    )

    private val library: LanguageLibrary by lazy { loadLibrary() }

    private data class PrefixRule(
        val regex: Regex,
        val replacement: (MatchResult) -> String
    )

    private val commitTypePrefixes = listOf(
        PrefixRule(Regex("""(^|\s)fix/refactor:""")) { "${it.groupValues[1]}修复/重构：" },
        PrefixRule(Regex("""(^|\s)fix\((#[0-9]+(?:,\s*#[0-9]+)*)\):""")) {
            "${it.groupValues[1]}修复(${it.groupValues[2]})："
        },
        PrefixRule(Regex("""(^|\s)fix:""")) { "${it.groupValues[1]}修复：" },
        PrefixRule(Regex("""(^|\s)improve\((#[0-9]+(?:,\s*#[0-9]+)*)\):""")) {
            "${it.groupValues[1]}改进(${it.groupValues[2]})："
        },
        PrefixRule(Regex("""(^|\s)improve:""")) { "${it.groupValues[1]}改进：" },
        PrefixRule(Regex("""(^|\s)feat\((#[0-9]+(?:,\s*#[0-9]+)*)\):""")) {
            "${it.groupValues[1]}新功能(${it.groupValues[2]})："
        },
        PrefixRule(Regex("""(^|\s)feat:""")) { "${it.groupValues[1]}新功能：" },
        PrefixRule(Regex("""(^|\s)chore:""")) { "${it.groupValues[1]}维护：" },
        PrefixRule(Regex("""(^|\s)server:""")) { "${it.groupValues[1]}服务端：" }
    )

    private val recentReleasesHeadings = listOf(
        "## 📦 Recent Releases",
        "## 近期版本"
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

    fun stripRecentReleases(notes: String): String {
        var result = notes
        recentReleasesHeadings.forEach { heading ->
            result = result.substringBefore(heading)
        }
        return result
    }

    fun hasMeaningfulEnglishText(notes: String): Boolean {
        val text = notes
            .replace(Regex("""https?://\S+"""), "")
            .replace(Regex("""`[^`]+`"""), "")
            .replace(Regex("""[A-Za-z0-9._-]+/[A-Za-z0-9._-]+"""), "")
        return Regex("""\b(Changes since|Recent Changes|Bug Fixes|New Features|Improvements|Recent Releases|Full Changelog|Most recent|Third-party|actually got|fails to install|always treated|unidentified|retry|correct|string references|notification|permission|release|highlight)\b""")
            .containsMatchIn(text)
    }

    private fun translateLine(line: String): String {
        val trimmed = line.trim()
        library.exactLines.firstOrNull { it.source == trimmed }?.let { rule ->
            return line.replace(trimmed, rule.target)
        }

        val normalized = normalizeText(line)
        translateIssueCount(normalized)?.let { return it }
        translateChangesHeading(normalized)?.let { return it }
        translateFullChangelog(normalized)?.let { return it }
        translateRecentReleaseCallout(normalized)?.let { return it }
        translateReleaseTableRow(normalized)?.let { return it }
        translateBullet(normalized)?.let { return it }

        var result = normalized
        result = replaceSummaries(result)
        result = replacePhrases(result)
        result = applyCommitTypePrefixes(result)
        return result.replace("  ", " ").trimEnd()
    }

    private fun normalizeText(value: String): String =
        value.replace("—", "-")
            .replace("–", "-")
            .replace("&amp;", "&")
            .replace(Regex("""\s+-\s+"""), " - ")
            .replace(Regex("""[ \t]{2,}"""), " ")

    private fun translateIssueCount(line: String): String? {
        val match = Regex("""^_(\d+) distinct GitHub issue\(s\) referenced in this release\._$""").find(line.trim())
            ?: return null
        return "_本版本关联了 ${match.groupValues[1]} 个 GitHub issue。_"
    }

    private fun translateChangesHeading(line: String): String? {
        val match = Regex("""^## Changes since (v[0-9][^\s]*)$""").find(line.trim())
            ?: return null
        return "## 相比 ${match.groupValues[1]} 的变化"
    }

    private fun translateFullChangelog(line: String): String? =
        Regex("""^\[Full Changelog]\(([^)]+)\)$""").find(line.trim())?.let {
            "[完整变更记录](${it.groupValues[1]})"
        }

    private fun translateRecentReleaseCallout(line: String): String? {
        val match = Regex("""^>\s*([^\s]+)\s+\*\*(Most recent (?:major release|critical fix)) - \[([^\]]+)]\(([^)]+)\)\*\*$""")
            .find(line.trim()) ?: return null
        val label = replacePhrases(match.groupValues[2])
        return "> ${match.groupValues[1]} **$label - [${match.groupValues[3]}](${match.groupValues[4]})**"
    }

    private fun translateReleaseTableRow(line: String): String? {
        val match = Regex("""^\| \[([^\]]+)]\(([^)]+)\)([^|]*) \| (.+) \|$""").find(line.trim())
            ?: return null
        val marker = translateReleaseMarker(match.groupValues[3].trim())
        val summary = translateSummary(match.groupValues[4].trim())
        return "| [${match.groupValues[1]}](${match.groupValues[2]})${marker} | $summary |"
    }

    private fun translateReleaseMarker(marker: String): String =
        marker.replace("_(this release)_", "_(本版本)_")
            .replace("**major**", "**重要版本**")
            .replace("**critical fix**", "**关键修复**")
            .let { if (it.isBlank()) "" else " $it" }

    private fun translateBullet(line: String): String? {
        val match = Regex("""^(\s*[-*]\s+)(.+?)(\s+\([0-9a-f]{7,8}\))?$""").find(line)
            ?: return null
        val summary = translateSummary(match.groupValues[2])
        return "${match.groupValues[1]}$summary${match.groupValues[3]}"
    }

    private fun translateSummary(summary: String): String {
        val cleaned = normalizeText(summary).trim()
        val protectedTail = Regex("""(\s+\([0-9a-f]{7,8}\))$""").find(cleaned)?.value.orEmpty()
        val body = cleaned.removeSuffix(protectedTail).trim()

        library.summaryRules.firstOrNull { normalizeText(it.source).equals(body, ignoreCase = false) }?.let {
            return it.target + protectedTail
        }

        return replacePhrases(applyCommitTypePrefixes(replaceSummaries(body))).trim() + protectedTail
    }

    private fun replaceSummaries(text: String): String {
        var result = text
        library.summaryRules.forEach { rule ->
            result = result.replace(normalizeText(rule.source), rule.target)
        }
        return result
    }

    private fun replacePhrases(text: String): String {
        var result = text
        library.phraseRules.forEach { rule ->
            result = result.replace(normalizeText(rule.source), rule.target)
            result = result.replace(rule.source, rule.target)
        }
        return result
    }

    private fun applyCommitTypePrefixes(text: String): String {
        var result = text
        commitTypePrefixes.forEach { rule ->
            result = rule.regex.replace(result, rule.replacement)
        }
        return result
    }

    private fun loadLibrary(): LanguageLibrary {
        val assetLibrary = runCatching {
            ShizukuApplication.appContext.assets.open(ZH_LIBRARY_ASSET).use { input ->
                parseLibrary(JSONObject(input.bufferedReader(Charsets.UTF_8).readText()))
            }
        }.onFailure {
            Timber.tag(TAG).w(it, "Failed to load $ZH_LIBRARY_ASSET, using fallback release-note library")
        }.getOrNull()

        return assetLibrary ?: loadLocalSourceLibrary() ?: fallbackLibrary
    }

    private fun loadLocalSourceLibrary(): LanguageLibrary? =
        runCatching {
            val file = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
                .flatMap { dir ->
                    sequenceOf(
                        File(dir, "manager/src/main/assets/$ZH_LIBRARY_ASSET"),
                        File(dir, "src/main/assets/$ZH_LIBRARY_ASSET")
                    )
                }
                .firstOrNull { it.isFile } ?: return@runCatching null
            parseLibrary(JSONObject(file.readText(Charsets.UTF_8)))
        }.onFailure {
            Timber.tag(TAG).w(it, "Failed to load local $ZH_LIBRARY_ASSET, using fallback release-note library")
        }.getOrNull()

    private fun parseLibrary(json: JSONObject): LanguageLibrary =
        LanguageLibrary(
            exactLines = json.optRules("exactLines"),
            summaryRules = json.optRules("summaryRules"),
            phraseRules = json.optRules("phraseRules")
        )

    private fun JSONObject.optRules(name: String): List<Rule> {
        val array = optJSONArray(name) ?: JSONArray()
        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val source = item.optString("source", "").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val target = item.optString("target", "").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            Rule(source, target)
        }
    }
}
