package ai.txai.feedback.view

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.router.ARouterConstants
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.feedback.R
import ai.txai.feedback.databinding.FeedbackMainActivityBinding
import ai.txai.feedback.viewmodel.FeedbackViewModel
import android.graphics.Color
import android.view.View
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.NetworkUtils

@Route(path = ARouterConstants.PATH_ACTIVITY_FEEDBACK)
class FeedbackActivity: BaseMvvmActivity<FeedbackMainActivityBinding, FeedbackViewModel>() {
    private var fragmentTag = ""

    private fun initView() {
        binding.toolbarTitleTv.setText(R.string.feedback_title)
        binding.bigTitle.titleTv.setText(R.string.feedback_title)
        binding.feedbackBackImg.setDebounceClickListener {
            finish()
        }
        binding.feedbackReport.setDebounceClickListener {
            viewModel.getFeedbackStatus().value = FeedbackViewModel.CHOOSE_ISSUE
        }

        binding.feedbackSend.setDebounceClickListener {
            viewModel.getFeedbackStatus().value = FeedbackViewModel.FEEDBACK_DETAIL
        }
    }

    private fun switchFragment(index: Int) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = getFragment(index)
        transaction.replace(R.id.fragment_container, fragment, fragmentTag)
        transaction.addToBackStack(fragmentTag)
        transaction.commit()
    }

    private fun getFragment(index: Int): Fragment {
        var fragment: Fragment
        when (index) {
            FeedbackViewModel.CHOOSE_ISSUE -> {
                fragmentTag = "issue"
                fragment = ChooseIssueFragmentV2()
            }
            FeedbackViewModel.ISSUE_DETAIL -> {
                fragmentTag = "issueDetail"
                fragment = ReportIssueDetailFragment()
            }
            else -> {
                fragmentTag = "feedbackDetail"
                fragment = FeedbackDetailFragment()
            }
        }

        return fragment
    }

    override fun initViewObservable() {
        super.initViewObservable()
        initView()
//        viewModel.loadIssueType()
        viewModel.getFeedbackStatus().observe(this) {
            if (it == FeedbackViewModel.POP_ALL_VIEW) {
                val size = supportFragmentManager.backStackEntryCount
                for (i in 0 until size) {
                    supportFragmentManager.popBackStack()
                }
                return@observe
            }

            switchFragment(it)
        }
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        binding.statusView.visibility = View.GONE
        binding.statusView.loadSuccess()
        binding.toolbarTitleTv.visibility = View.GONE
    }

    override fun onDisconnected() {
        binding.statusView.visibility = View.VISIBLE
        binding.statusView.loadError()
        binding.toolbarTitleTv.visibility = View.VISIBLE
        binding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"))
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun finish() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
        super.finish()
    }

    override fun initViewBinding(): FeedbackMainActivityBinding {
        return FeedbackMainActivityBinding.inflate(layoutInflater)
    }

    companion object {
        private const val TAG = "FeedbackActivityTST"
    }
}