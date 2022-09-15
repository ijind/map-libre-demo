package ai.txai.feedback.viewmodel

import ai.txai.common.api.CommonApiRepository
import ai.txai.common.base.BaseListViewModel
import ai.txai.common.data.ArticlesInfo
import ai.txai.common.data.FileInfo
import ai.txai.common.data.ImageInfo
import ai.txai.common.log.LOG
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import ai.txai.common.thread.TScheduler.io
import ai.txai.common.utils.BitmapUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.feedback.api.FeedbackApiRepository
import ai.txai.feedback.data.IssueInfo
import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.File

class FeedbackViewModel(application: Application): BaseListViewModel<IssueInfo, Int>(application) {
    companion object {
        private const val TAG = "FeedbackViewModel"
        const val POP_ALL_VIEW = -1
        const val CHOOSE_ISSUE = 1
        const val FEEDBACK_DETAIL = 2
        const val ISSUE_DETAIL = 3
    }

    private val feedbackStatus = MutableLiveData<Int>()
    fun getFeedbackStatus() = feedbackStatus

    private val issueType = MutableLiveData<ArrayList<IssueInfo>>()
    fun getIssueType() = issueType

    private var uploadImageFileInfo = MutableLiveData<ArrayList<ImageInfo>>()
    fun getUploadImageInfo() = uploadImageFileInfo

    private var uploadFileInfo = MutableLiveData<FileInfo>()
    fun getUploadFileInfo() = uploadFileInfo

    private val uploadFeedbackStatus = MutableLiveData<Boolean>()
    fun getUploadFeedbackStatus() = uploadFeedbackStatus

    private val uploadIssueStatus = MutableLiveData<Boolean>()
    fun getUploadIssueStatus() = uploadIssueStatus

    private var reportIssueType = MutableLiveData<Long>()
    fun getReportIssueType() = reportIssueType

    private var legalArticlesInfo = MutableLiveData<ArrayList<ArticlesInfo>>()
    fun getLegalArticles() = legalArticlesInfo

    fun loadIssueType() {
        FeedbackApiRepository.loadIssueType()
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<ArrayList<IssueInfo>>() {
                override fun onSuccess(t: ArrayList<IssueInfo>?) {
                    super.onSuccess(t)
                    t?.let { issueType.value = it }
                }
            })
    }

    fun uploadUserImg(fileList: ArrayList<File>) {
        CommonApiRepository.uploadImageFile("common", "feedback", fileList)
            ?.subscribeOn(io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object: CommonObserver<ArrayList<ImageInfo>>() {
                override fun onSuccess(t: ArrayList<ImageInfo>?) {
                    super.onSuccess(t)
                    LOG.d(TAG, "on upload success $t")
                    t?.let {
                        uploadImageFileInfo.postValue(it)
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    msg?.let { ToastUtils.show(it) }
                }
            })
    }

    fun uploadFile(file: File) {
        CommonApiRepository.uploadFile("common", "feedback", file)
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<FileInfo>() {
                override fun onSuccess(t: FileInfo?) {
                    super.onSuccess(t)
                    t?.let {
                        uploadFileInfo.postValue(it)
                        if (file.exists() && file.isFile) {
                            file.delete()
                        }
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    msg?.let { ToastUtils.show(it) }
                }
            })
    }

    fun uploadFeedback(contactStyle: String, describe: String,
                       imageList: ArrayList<String>) {
        FeedbackApiRepository.uploadFeedback(contactStyle, describe, imageList)
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<Long>() {
                override fun onSuccess(t: Long?) {
                    super.onSuccess(t)
                    BitmapUtils.clearPicturesDir()
                    uploadFeedbackStatus.value = true
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    msg?.let { ToastUtils.show(it) }
                }
            })
    }

    fun uploadIssue(contactStyle: String, describe: String, issueType: Long, path: String,
                    imageList: ArrayList<String>) {
        FeedbackApiRepository.uploadIssue(contactStyle, describe,
            issueType, path, imageList)
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<Long>() {
                override fun onSuccess(t: Long?) {
                    super.onSuccess(t)
                    BitmapUtils.clearPicturesDir()
                    uploadIssueStatus.value = true
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    msg?.let { ToastUtils.show(it) }
                }
            })
    }

    fun  loadLegalArticles() {
        CommonApiRepository.loadArticlesByType("legal", false)
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<ArrayList<ArticlesInfo>>() {
                override fun onSuccess(t: ArrayList<ArticlesInfo>?) {
                    super.onSuccess(t)
                    t?.let { legalArticlesInfo.value = it }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }

    override fun loadData(integer: Int?, forMore: Boolean) {
        showLoading("")
        FeedbackApiRepository.loadIssueType()
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<ArrayList<IssueInfo>>() {
                override fun onSuccess(t: ArrayList<IssueInfo>?) {
                    super.onSuccess(t)
                    list = if (t != null) {
                        java.util.ArrayList(t)
                    } else {
                        java.util.ArrayList()
                    }
                    refresh.postValue(true)
                    hasMore.postValue(false)
                    hideLoading()
                }
                override fun onFailed(msg: String?) {
                    showToast(msg, false)
                    hideLoading()
                }
            })
    }
}