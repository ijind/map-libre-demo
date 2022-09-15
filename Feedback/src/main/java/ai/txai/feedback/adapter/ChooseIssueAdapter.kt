package ai.txai.feedback.adapter

import ai.txai.common.parallaxrecyclerview.ParallaxRecyclerAdapter
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.feedback.data.IssueInfo
import ai.txai.feedback.databinding.FeedbackIssueItemLayoutBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ChooseIssueAdapter(private val context: Context) : ParallaxRecyclerAdapter<IssueInfo>() {
    private var clickOperation: (issueType: Long) -> Unit = {}

    override fun onBindViewHolderImpl(
        viewHolder: RecyclerView.ViewHolder?,
        adapter: ParallaxRecyclerAdapter<IssueInfo>?,
        i: Int
    ) {
        if (adapter == null || viewHolder == null) {
            return
        }

        val holder = viewHolder as FeedbackVH
        holder.binding.apply {
            val info = adapter.data[i]
            this.chooseIssueItem.setItemTitle(info.getTitle())
            this.chooseIssueItem.setDebounceClickListener {
                clickOperation.invoke(info.value)
            }
        }
    }

    override fun onCreateViewHolderImpl(
        viewGroup: ViewGroup?,
        adapter: ParallaxRecyclerAdapter<IssueInfo>?,
        i: Int
    ): RecyclerView.ViewHolder {
        val inflate = LayoutInflater.from(context)
        val binding = FeedbackIssueItemLayoutBinding.inflate(inflate, viewGroup, false)
        return FeedbackVH(binding)
    }

    override fun getItemCountImpl(adapter: ParallaxRecyclerAdapter<IssueInfo>?): Int {
        if (adapter == null) return 0

        return adapter.data.size
    }

    fun setItemClickListener(clickListener: (issueType: Long) -> Unit) {
        clickOperation = clickListener
    }

    inner class FeedbackVH(val binding: FeedbackIssueItemLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)
}