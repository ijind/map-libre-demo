package ai.txai.lostfound.viewmodel

import ai.txai.common.api.CommonApiRepository
import ai.txai.common.data.ArticlesDetailInfo
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

class LostFoundViewModel(application: Application): BaseViewModel(application) {

    private var articlesInfo = MutableLiveData<ArticlesDetailInfo>()
    fun getArticles() = articlesInfo

    fun loadLegalArticles() {
        CommonApiRepository.loadArticlesDetail("contact-us-for-help")
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<ArticlesDetailInfo>() {
                override fun onSuccess(t: ArticlesDetailInfo?) {
                    super.onSuccess(t)
                    t?.let { articlesInfo.value = it }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }
}