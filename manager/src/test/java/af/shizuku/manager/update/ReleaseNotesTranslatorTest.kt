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

    test("only translates for Chinese locales") {
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.US) shouldBe "stock Shizuku"
        ReleaseNotesTranslator.translateIfNeeded("stock Shizuku", Locale.SIMPLIFIED_CHINESE) shouldBe
            "原版 Shizuku"
    }
})
