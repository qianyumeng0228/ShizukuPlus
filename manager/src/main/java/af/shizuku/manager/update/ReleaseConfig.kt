package af.shizuku.manager.update

object ReleaseConfig {
    const val OWNER = "qianyumeng0228"
    const val REPO = "ShizukuPlus"
    const val REPOSITORY = "$OWNER/$REPO"
    const val GITHUB_URL = "https://github.com/$REPOSITORY"
    const val RELEASES_URL = "$GITHUB_URL/releases"
    const val API_RELEASES_URL = "https://api.github.com/repos/$REPOSITORY/releases"
    const val ATOM_URL = "$RELEASES_URL.atom"

    fun releaseTagUrl(tagName: String): String = "$RELEASES_URL/tag/$tagName"
}
