package ai.txai.feedback.view

import ai.txai.common.activity.WebActivity
import ai.txai.common.api.ApiConfig
import ai.txai.common.base.ParameterField
import ai.txai.common.log.LOG
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.common.utils.WebUtils
import ai.txai.database.utils.CommonData
import ai.txai.feedback.R
import ai.txai.feedback.viewmodel.FeedbackViewModel
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.NoCopySpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.io.File

class ReportIssueDetailFragment: BaseDetailFragment() {
    private var isFromClick = false

    override fun initViewObservable() {
        super.initViewObservable()
        viewModel.getUploadIssueStatus().observe(this) {
            if (it == null) {
                isGoSend = false
                return@observe
            }

            hideLoading()
            viewModel.getFeedbackStatus().postValue(FeedbackViewModel.POP_ALL_VIEW)
            ToastUtils.show("Thanks for your submission")
        }

        viewModel.getUploadFileInfo().observe(this) {
            if (it == null) {
                isGoSend
                return@observe
            }

            uploadIssue(it.getFilePath())
        }

        viewModel.getLegalArticles().observe(this) {
            if (it == null) return@observe

            it.forEach { info ->
                val userUrl = "${ApiConfig.baseUrl}/api/biz${info.getArticleLink()}"
                if (isFromClick && info.getArticleName() == WebUtils.PRIVACY_LEGAL_NAME) {
                    startPrivacy(userUrl)
                    isFromClick = false
                }
                WebUtils.cacheAgreementUrl(info.getArticleName(), userUrl)
            }
        }
    }

    override fun initView() {
        super.initView()
        showReportIssueView()
    }

    override fun invokeSendOperation() {
        super.invokeSendOperation()
        submitIssueOperation()
    }

    override fun getCustomTitle() = R.string.feedback_report

    override fun getOperationString() = R.string.feedback_operation_submit

    @SuppressLint("SetTextI18n")
    private fun showReportIssueView() {
        itemBinding.feedbackEditTitle.text = "Describe your issue"
        itemBinding.feedbackPromptTv.text = getPromptText("")
        itemBinding.feedbackPromptTv.highlightColor = Color.TRANSPARENT
        itemBinding.feedbackPromptTv.movementMethod = LinkMovementMethod.getInstance()
        itemBinding.feedbackDescribeEdit.setInputHint(R.string.feedback_issue_hint)
        itemBinding.sendLogsLayout.visibility = View.VISIBLE
        val isChecked = CommonData.getInstance().get(SEND_LOGS_KEY)
        if (!isChecked.isNullOrEmpty()) {
            itemBinding.sendLogSwitch.isChecked = isChecked.toBoolean()
        }

        val sendLogsString = resources.getString(R.string.feedback_send_logs_prompt)
        val privacy = " Privacy policy"
        val builder = SpannableStringBuilder()
        builder.append(sendLogsString).append(privacy)
        val privacyClickSpan = object: ClickableSpan(), NoCopySpan {
            override fun onClick(widget: View) {
                val url = CommonData.getInstance().get(ParameterField.PRIVACY_URL)
                if (url.isNullOrEmpty()) {
                    isFromClick = true
                    viewModel.loadLegalArticles()
                } else {
                    startPrivacy(url)
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ResourcesCompat.getColor(resources,
                    R.color.commonview_orange_00, null)
            }
        }
        builder.setSpan(privacyClickSpan, sendLogsString.length, builder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        itemBinding.sendLogPromptTv.text = builder
        itemBinding.sendLogPromptTv.highlightColor = Color.TRANSPARENT
        itemBinding.sendLogPromptTv.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun startPrivacy(url: String) {
        val bundle = Bundle()
        bundle.putString(ParameterField.WEB_URL, url)
        bundle.putInt(ParameterField.WEB_TITLE, R.string.commonview_privacy)
        val intent = Intent(requireActivity(), WebActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun submitIssueOperation() {
        showLoading("")
        if (uploadImageList.size > 0) return

        val isSendLogs = itemBinding.sendLogSwitch.isChecked

        if (isSendLogs) {
            val path = LOG.compress(AndroidUtils.getApplicationContext())
            viewModel.uploadFile(File(path))
        } else {
            uploadIssue("")
        }
    }

    private fun uploadIssue(logPath: String) {
        val pathList = ArrayList<String>()
        uploadedFilePathList.forEach { info ->
            pathList.add(info.getFilePath())
        }

        val describe = itemBinding.feedbackDescribeEdit.getInputText()
        val contactStyle = itemBinding.feedbackContactEdit.text?.toString() ?: ""
        val issueType = viewModel.getReportIssueType().value ?: 0L
        viewModel.uploadIssue(contactStyle, describe, issueType, logPath, pathList)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        CommonData.getInstance().put(SEND_LOGS_KEY, itemBinding.sendLogSwitch.isChecked)
        viewModel.getUploadIssueStatus().value = null
        viewModel.getUploadFileInfo().value = null
        itemBinding.feedbackPromptTv.text = ""
    }
}