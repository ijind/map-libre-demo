package ai.txai.setting.viewmodel

import ai.txai.common.api.CommonApiRepository
import ai.txai.common.data.ArticlesDetailInfo
import ai.txai.common.data.ArticlesInfo
import ai.txai.common.data.UpdateVersionInfo
import ai.txai.common.external.LoginManager
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import ai.txai.database.utils.CommonData
import ai.txai.setting.api.SettingApiRepository
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ThreadUtils
import io.reactivex.android.schedulers.AndroidSchedulers

class SettingViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        const val SETTING_CONTENT = 100
        const val SETTING_LEGAL = 101

        const val LEGAL_ARTICLES = "legal"
        const val SERVICE_AND_HELP = "services-and-help"
    }

    private var settingViewStatus = MutableLiveData(SETTING_CONTENT)
    fun getSettingViewStatus() = settingViewStatus

    private var logoutStatus = MutableLiveData<Boolean>()
    fun getLogoutStatus() = logoutStatus

    private var appUpdateInfo = MutableLiveData<UpdateVersionInfo>()
    fun getAppUpdateInfo() = appUpdateInfo

    private var articlesInfo = MutableLiveData<ArrayList<ArticlesInfo>>()
    fun getArticles() = articlesInfo

    fun checkAppUpdate() {
        CommonApiRepository.checkAppUpdate()
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<UpdateVersionInfo>() {
                override fun onSuccess(t: UpdateVersionInfo?) {
                    super.onSuccess(t)
                    appUpdateInfo.value = t
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }

    fun logout() {
        SettingApiRepository.logout()
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<Boolean>() {
                override fun onSuccess(t: Boolean?) {
                    super.onSuccess(t)
                    t?.let {
                        if (it) {
                            LifeCycleManager.accountLogout()
                        }
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }

    fun loadLegalArticles(type: String, isLoadContent: Boolean) {
        CommonApiRepository.loadArticlesByType(type, isLoadContent)
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<ArrayList<ArticlesInfo>>() {
                override fun onSuccess(t: ArrayList<ArticlesInfo>?) {
                    super.onSuccess(t)
                    t?.let { articlesInfo.value = it }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }
}