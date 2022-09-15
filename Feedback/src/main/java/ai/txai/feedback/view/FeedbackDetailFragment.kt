package ai.txai.feedback.view

import ai.txai.common.utils.ToastUtils
import ai.txai.feedback.R
import ai.txai.feedback.viewmodel.FeedbackViewModel
import android.annotation.SuppressLint
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import java.io.File

class FeedbackDetailFragment: BaseDetailFragment() {

    override fun initViewObservable() {
        super.initViewObservable()
        viewModel.getUploadFeedbackStatus().observe(this) {
            if (it == null) {
                isGoSend = false
                return@observe
            }

            hideLoading()
            viewModel.getFeedbackStatus().postValue(FeedbackViewModel.POP_ALL_VIEW)
            ToastUtils.show("Thanks for your feedback")
        }
    }

    override fun initView() {
        super.initView()
        showSendFeedbackView()
    }

    override fun invokeSendOperation() {
        super.invokeSendOperation()
        sendFeedbackOperation()
    }

    override fun getCustomTitle() = R.string.feedback_send

    override fun getOperationString() = R.string.feedback_operation_send

    @SuppressLint("SetTextI18n")
    private fun showSendFeedbackView() {
        itemBinding.feedbackEditTitle.text = "Feedback"
        itemBinding.feedbackPromptTv.text = getPromptText(REPORT_ISSUE)
        itemBinding.feedbackPromptTv.highlightColor = Color.TRANSPARENT
        itemBinding.feedbackPromptTv.movementMethod = LinkMovementMethod.getInstance()
        itemBinding.feedbackDescribeEdit.setInputHint(R.string.feedback_describe_hint)
        itemBinding.sendLogsLayout.visibility = View.GONE
    }

    private fun sendFeedbackOperation() {
        showLoading("")
        if (uploadImageList.size > 0) return

        val pathList = ArrayList<String>()
        uploadedFilePathList.forEach {
            pathList.add(it.getFilePath())
        }

        val describe = itemBinding.feedbackDescribeEdit.getInputText()
        val contactStyle = itemBinding.feedbackContactEdit.text?.toString() ?: ""
        viewModel.uploadFeedback(contactStyle, describe, pathList)
    }
}