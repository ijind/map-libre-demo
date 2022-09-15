package ai.txai.feedback.view

import ai.txai.common.mvvm.BaseMvvmFragment
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.feedback.R
import ai.txai.feedback.adapter.ChooseIssueAdapter
import ai.txai.feedback.databinding.FeedbackReportIssueLayoutBinding
import ai.txai.feedback.viewmodel.FeedbackViewModel
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils

class ChooseIssueFragment: BaseMvvmFragment<FeedbackReportIssueLayoutBinding, FeedbackViewModel>() {

    override fun initViewObservable() {
        super.initViewObservable()
        binding.feedbackChooseBackImg.setDebounceClickListener {
            activity?.onBackPressed()
        }

        binding.contentRecycler.layoutManager = LinearLayoutManager(context)
        val adapter = createAdapter(binding.contentRecycler)
        binding.contentRecycler.adapter = adapter
        adapter.setItemClickListener {
            viewModel.getFeedbackStatus().value = FeedbackViewModel.ISSUE_DETAIL
            viewModel.getReportIssueType().value = it
        }

        viewModel.getIssueType().observe(this) {
            if (it == null) return@observe

            val issueAdapter = binding.contentRecycler.adapter as ChooseIssueAdapter
            issueAdapter.data = it
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FeedbackReportIssueLayoutBinding {
        return FeedbackReportIssueLayoutBinding.inflate(inflater, container, false)
    }

    private fun createAdapter(recyclerView: RecyclerView): ChooseIssueAdapter {
        val adapter = ChooseIssueAdapter(requireContext())
        adapter.setOnParallaxScroll { percentage, _, _ ->
            binding.feedbackReportTitleTv.setTextColor(Color.parseColor("#00040818"))
            updateTopShadow(View.GONE)
            if (percentage >= 0.8) {
                val color = ColorUtils.setAlphaComponent(Color.parseColor("#040818"), percentage)
                binding.feedbackReportTitleTv.setTextColor(color)
                updateTopShadow(View.VISIBLE)
            }
        }
        val header = layoutInflater.inflate(R.layout.feedback_choose_issue_header_layout, recyclerView, false)
        adapter.setParallaxHeader(header, recyclerView)
        return adapter
    }

    private fun updateTopShadow(visible: Int) {
        binding.chooseIssue.visibility = visible
        binding.chooseIssueShadow.visibility = visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}