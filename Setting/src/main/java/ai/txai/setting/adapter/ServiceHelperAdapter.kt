package ai.txai.setting.adapter

import ai.txai.common.data.ArticlesInfo
import ai.txai.common.log.LOG
import ai.txai.common.parallaxrecyclerview.ParallaxRecyclerAdapter
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.setting.R
import ai.txai.setting.databinding.SettingServiceHelperItemLayoutBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView

class ServiceHelperAdapter(private val context: Context) : ParallaxRecyclerAdapter<ArticlesInfo>() {
    private var expendbinding: SettingServiceHelperItemLayoutBinding? = null
    private var isFirstShow = true
    private var curShowIndex = 0

    override fun onBindViewHolderImpl(
        viewHolder: RecyclerView.ViewHolder?,
        adapter: ParallaxRecyclerAdapter<ArticlesInfo>?,
        i: Int) {
        if (adapter == null || viewHolder == null) {
            return
        }

        val holder = viewHolder as ServiceHelperVH
        holder.binding.let {
            if (i == 0 && isFirstShow) {
                isFirstShow = false
                expendbinding = it
                curShowIndex = 0
                it.expandServiceTv.visibility = View.VISIBLE
                it.serviceHelperTitle.setArrowIc(R.mipmap.ic_expand_up)
                it.bottomHolderView.visibility = View.VISIBLE
            }

            if (curShowIndex != i && curShowIndex >= 0) {
                updateVisible(it, View.GONE)
            } else if (curShowIndex == i) {
                updateVisible(it, View.VISIBLE)
            }

            val articlesInfo = adapter.data[i]
            it.serviceHelperTitle.setItemTitle(articlesInfo.getArticleTitle())
            it.expandServiceTv.text = articlesInfo.getContent().trim()
            it.serviceHelperTitle.setOnClickListener { _ ->
                it.expandServiceTv.apply {
                    this.updateStatus(it, i)
                }
            }

            LOG.d("FAQ", "position $i size ${adapter.data.size}")
            it.serviceHelperTitle.visibleBottomLine(true)
            if (i == adapter.data.size - 1) {
                it.serviceHelperTitle.visibleBottomLine(false)
            }
        }
    }

    override fun onCreateViewHolderImpl(
        viewGroup: ViewGroup?,
        adapter: ParallaxRecyclerAdapter<ArticlesInfo>?,
        i: Int): RecyclerView.ViewHolder {
        val inflate = LayoutInflater.from(context)
        val binding = SettingServiceHelperItemLayoutBinding.inflate(inflate, viewGroup, false)
        return ServiceHelperVH(binding)
    }

    override fun getItemCountImpl(adapter: ParallaxRecyclerAdapter<ArticlesInfo>?): Int {
        if (adapter == null) return 0

        return adapter.data.size
    }

    private fun TextView.updateStatus(binding: SettingServiceHelperItemLayoutBinding, pos: Int) {
        curShowIndex = if (this.visibility == View.VISIBLE) {
            updateVisible(binding, View.GONE)
            -1
        } else {
            if (curShowIndex != pos) {
                expendbinding?.let { updateVisible(it, View.GONE) }
            }
            updateVisible(binding, View.VISIBLE)
            pos
        }
    }

    private fun updateVisible(binding: SettingServiceHelperItemLayoutBinding?, visible: Int) {
        binding?.let {
            it.expandServiceTv.visibility = visible
            it.bottomHolderView.visibility = visible
            var icRes = R.mipmap.ic_expand_down
            when(visible) {
                View.VISIBLE -> {
                    icRes = R.mipmap.ic_expand_up
                    expendbinding = it
                }
            }
            it.serviceHelperTitle.setArrowIc(icRes)
        }
    }

    inner class ServiceHelperVH(val binding: SettingServiceHelperItemLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)
}