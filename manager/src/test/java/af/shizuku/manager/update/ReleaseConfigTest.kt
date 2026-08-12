package af.shizuku.manager.update

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ReleaseConfigTest : FunSpec({

    test("localized tags map back to upstream release tags") {
        ReleaseConfig.upstreamTagFor("v13.6.0.r2248-zh.1") shouldBe "v13.6.0.r2248"
        ReleaseConfig.upstreamTagFor("v13.6.0.r2248") shouldBe "v13.6.0.r2248"
    }

    test("project release URLs use localized tags") {
        ReleaseConfig.releaseTagUrl("v13.6.0.r2248") shouldBe
            "https://github.com/qianyumeng0228/ShizukuPlus/releases/tag/v13.6.0.r2248-zh.1"
        ReleaseConfig.releaseTagUrl("v13.6.0.r2248-zh.1") shouldBe
            "https://github.com/qianyumeng0228/ShizukuPlus/releases/tag/v13.6.0.r2248-zh.1"
    }

    test("upstream release URLs strip localized suffixes") {
        ReleaseConfig.upstreamReleaseTagUrl("v13.6.0.r2248-zh.1") shouldBe
            "https://github.com/thejaustin/ShizukuPlus/releases/tag/v13.6.0.r2248"
    }
})
