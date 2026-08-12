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

    test("only translates for Chinese locales") {
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.US) shouldBe "stock Shizuku"
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.SIMPLIFIED_CHINESE) shouldBe
            "原版 Shizuku"
    }
})
