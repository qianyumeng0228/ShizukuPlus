package af.shizuku.manager.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import af.shizuku.manager.databinding.HomeBannerBinding
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

/**
 * Fixed decorative hero header at the very top of the home screen. Shows the themed wallpaper
 * (light/dark auto-selected through the drawable-night qualifier) in a rounded banner. Has no
 * data, is not draggable and is never hidden.
 */
class HomeBannerViewHolder(
    private val binding: HomeBannerBinding,
    root: View,
) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val binding = HomeBannerBinding.inflate(inflater, parent, false)
            HomeBannerViewHolder(binding, binding.root)
        }
    }
}
