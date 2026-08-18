package af.shizuku.manager.update

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.shouldBe
import java.util.Locale

class ReleaseNotesTranslatorTest : FunSpec({

    test("keeps Shizuku terms accurate in Chinese") {
        val translated = ReleaseNotesTranslator.translate(
            "- Releases r2141-r2152 all shipped the Drop-In build under the Plus APK's filename - " +
                "anyone who installed \"Shizuku+\" actually got moe.shizuku.privileged.api, " +
                "which fails to install or update alongside stock Shizuku."
        )

        translated shouldContain "Drop-In 版本"
        translated shouldContain "原版 Shizuku"
        translated.shouldNotContain("股票")
    }

    test("translates common release note headings") {
        val translated = ReleaseNotesTranslator.translate(
            """
            # v13.6.0.r2246
            ## Changes since v13.6.0.r2245
            ### Bug Fixes
            - fix(#391): rish shell always treated as unidentified - UID fallback chain
            _1 distinct GitHub issue(s) referenced in this release._
            """.trimIndent()
        )

        translated shouldContain "## 相比 v13.6.0.r2245 的变化"
        translated shouldContain "### 问题修复"
        translated shouldContain "修复(#391)："
        translated shouldContain "UID 回退链"
        translated shouldContain "本版本关联了 1 个 GitHub issue。"
    }

    test("fully localizes latest release notes body used by dialogs") {
        val translated = ReleaseNotesTranslator.translate(
            """
            # v13.6.0.r2246

            ## Changes since v13.6.0.r2245

            ### 🐛 Bug Fixes
            - fix(#391): rish shell always treated as unidentified — UID fallback chain (9f2c01e8)

            _1 distinct GitHub issue(s) referenced in this release._

            ## 📦 Recent Releases

            > 🚀 **Most recent major release — [v13.6.0.r2202](https://github.com/thejaustin/ShizukuPlus/releases/tag/v13.6.0.r2202)**
            > Third-party apps that Shizuku+ grants permissions to (WifiList, App Ops, Hail, Tasker, and others) now reliably detect and connect to the service. Closed the last gap in the Cached Apps Freezer guard: a UserService's very first connect callback could be silently dropped if the client app was frozen in the background while its process was still starting.

            > ⚠️ **Most recent critical fix — [v13.6.0.r2153](https://github.com/thejaustin/ShizukuPlus/releases/tag/v13.6.0.r2153)**
            > Releases r2141–r2152 all shipped the Drop-In build under the Plus APK's filename — anyone who installed "Shizuku+" in that window actually got moe.shizuku.privileged.api, which fails to install or update alongside stock Shizuku. Fixed by selecting release assets by verified applicationId instead of directory order.

            | Release | Highlight |
            |:--|:--|
            | [v13.6.0.r2246](https://github.com/thejaustin/ShizukuPlus/releases/tag/v13.6.0.r2246) _(this release)_ | fix(#391): rish shell always treated as unidentified — UID fallback chain |

            ---
            [Full Changelog](https://github.com/thejaustin/ShizukuPlus/compare/v13.6.0.r2245...v13.6.0.r2246)
            """.trimIndent()
        )

        translated shouldContain "### 问题修复"
        translated shouldContain "修复(#391)：rish shell 总是被识别为未确认身份，完善 UID 回退链"
        translated shouldContain "## 近期版本"
        translated shouldContain "最近的重要版本"
        translated shouldContain "Shizuku+ 授权的第三方应用"
        translated shouldContain "原版 Shizuku"
        translated shouldContain "| 版本 | 重点 |"
        translated shouldContain "_(本版本)_"
        translated shouldContain "[完整变更记录]"
        translated.shouldNotContain("Bug Fixes")
        translated.shouldNotContain("Most recent")
        translated.shouldNotContain("stock Shizuku")
        translated.shouldNotContain("Release | Highlight")
    }

    test("strips recent releases after translation") {
        val translated = ReleaseNotesTranslator.translate(
            """
            # v13.6.0.r2246
            ## Changes since v13.6.0.r2245
            ### 🐛 Bug Fixes
            - fix(#391): rish shell always treated as unidentified — UID fallback chain
            ## 📦 Recent Releases
            | Release | Highlight |
            | [v13.6.0.r2246](https://github.com/thejaustin/ShizukuPlus/releases/tag/v13.6.0.r2246) _(this release)_ | fix(#391): rish shell always treated as unidentified — UID fallback chain |
            """.trimIndent()
        )

        val dialogBody = ReleaseNotesTranslator.stripRecentReleases(translated)
        dialogBody shouldContain "### 问题修复"
        dialogBody.shouldNotContain("近期版本")
        dialogBody.shouldNotContain("版本 | 重点")
    }

    test("translates r2247 through r2260 release summaries") {
        val translated = ReleaseNotesTranslator.translate(
            """
            ### 🐛 Bug Fixes
            - fix(#407): match APK asset by applicationId and enable dev channel releases in UpdateChecker (bbe3ab76)
            - fix(ui): resolve bottom navigation bar overlap and modernize app search bar design (a49baf00)
            - fix(ui): prevent bottom navigation bar overlap on Feature Hub and Settings preferences (b5ea9e35)
            - fix(ui): resolve bottom navigation bar overlap on Activity Log list (ff7bb7d6)
            - fix(settings): immediately persist Dhizuku Mode setting state on preference toggle (a8251831)
            - fix(ui): eliminate theme-change screen flicker and redesign home server ok icon (56a3ea8f)
            - fix(ui): resolve black screen on recreate, clean status icon colors, and add One UI large header + universal one-handed reachability (ef041e46)
            - fix(ui): eliminate black-screen flash on theme/accent/icon/blur changes by using Compose recomposition instead of activity recreation (14c529a9)
            - fix(ui): implement authentic Samsung OneUI one-handed mode (scale+pivot) for home and settings screens (c82127a6)
            ### ✨ New Features
            - feat(ui): enhance OneUI mode Settings with Samsung-authentic ExtraBold header and transparent app bar (1c17f3e9)
            ### 🔧 Improvements & Refactors
            - chore: remove unused AppCompatActivity import from SettingsActivity (648e4b10)
            - docs: update README to remove stale banner, add recent fixes section; create CHANGELOG.md (f7d92aaa)
            - fix(defaults): align Java-side defaults with XML preferences for correct first-run experience (d8665d7e)
            - build: update api submodule for Android 16 NPE fix (#406) (69562ed3)
            """.trimIndent()
        )

        translated shouldContain "按 applicationId 匹配 APK 资产"
        translated shouldContain "底部导航栏重叠"
        translated shouldContain "Dhizuku 模式"
        translated shouldContain "Compose 重组"
        translated shouldContain "Android 16 上的空指针异常"
        translated.shouldNotContain("bottom navigation")
        translated.shouldNotContain("black-screen")
        translated.shouldNotContain("first-run experience")
    }

    test("translates r2284 release notes without english leftovers") {
        val translated = ReleaseNotesTranslator.translate(
            """
            ## Changes since v13.6.0.r2282

            ### 🐛 Bug Fixes
            - fix(security): re-implement always-allow persistence for shell/rish clients safely (#420, #416) (32382e89)
            - fix(server): stop dead branch in checkCallerPermission from masking allowed/denied state; move plus badge onto hexagon icon; bump api submodule (0d6f357a)

            _2 distinct GitHub issue(s) referenced in this release._
            """.trimIndent()
        )

        translated shouldContain "## 相比 v13.6.0.r2282 的变化"
        translated shouldContain "修复(安全)：安全地重新实现 shell/rish 客户端的 always-allow 持久化"
        translated shouldContain "修复(服务端)：停止让调用者权限判断中的死分支遮蔽允许/拒绝状态"
        translated shouldContain "本版本关联了 2 个 GitHub issue。"
        translated.shouldNotContain("always-allow persistence")
        translated.shouldNotContain("dead branch")
        translated.shouldNotContain("checkCallerPermission")
    }

    test("cleans mixed english terms from localized release body used by update dialog") {
        val translated = ReleaseNotesTranslator.translate(
            """
            ### 安全与稳定性
            - 修复 checkCallerPermission 的允许/拒绝判断，避免状态被错误遮蔽。
            ### 界面与汉化
            - 补齐更新公告与 release note 的中文翻译。
            - 更新发布通道配置，继续指向本项目的 GitHub release。
            """.trimIndent()
        )

        translated shouldContain "修复调用者权限判断的允许/拒绝判断"
        translated shouldContain "补齐更新公告与发行说明的中文翻译"
        translated shouldContain "GitHub 发布页"
        translated.shouldNotContain("checkCallerPermission")
        translated.shouldNotContain("release note")
        translated.shouldNotContain("GitHub release")
    }

    test("only translates for Chinese locales") {
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.US) shouldBe "stock Shizuku"
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.SIMPLIFIED_CHINESE) shouldBe
            "原版 Shizuku"
    }
})
