package af.shizuku.manager.update

object ReleaseConfig {
    const val OWNER = "qianyumeng0228"
    const val REPO = "ShizukuPlus"
    const val REPOSITORY = "$OWNER/$REPO"
    const val LOCALIZED_TAG_SUFFIX = "-zh.1"
    const val GITHUB_URL = "https://github.com/$REPOSITORY"
    const val RELEASES_URL = "$GITHUB_URL/releases"
    const val API_RELEASES_URL = "https://api.github.com/repos/$REPOSITORY/releases"
    const val ATOM_URL = "$RELEASES_URL.atom"

    const val UPSTREAM_OWNER = "thejaustin"
    const val UPSTREAM_REPO = "ShizukuPlus"
    const val UPSTREAM_REPOSITORY = "$UPSTREAM_OWNER/$UPSTREAM_REPO"
    const val UPSTREAM_GITHUB_URL = "https://github.com/$UPSTREAM_REPOSITORY"
    const val UPSTREAM_RELEASES_URL = "$UPSTREAM_GITHUB_URL/releases"
    const val UPSTREAM_API_RELEASES_URL = "https://api.github.com/repos/$UPSTREAM_REPOSITORY/releases"

    private val localizedTagSuffix = Regex("""-zh(?:\.\d+)?$""")

    fun upstreamTagFor(tagName: String): String = tagName.replace(localizedTagSuffix, "")

    fun localizedTagFor(tagName: String): String =
        if (localizedTagSuffix.containsMatchIn(tagName)) tagName else "$tagName$LOCALIZED_TAG_SUFFIX"

    fun releaseTagUrl(tagName: String): String = "$RELEASES_URL/tag/${localizedTagFor(tagName)}"

    fun upstreamReleaseTagUrl(tagName: String): String = "$UPSTREAM_RELEASES_URL/tag/${upstreamTagFor(tagName)}"
}
