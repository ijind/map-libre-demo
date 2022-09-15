package ai.txai.login.viewmodel

import ai.txai.common.api.CommonApiRepository
import ai.txai.common.data.ArticlesInfo
import ai.txai.login.data.LoginInfo
import ai.txai.login.data.LoginToken
import ai.txai.login.data.LoginUser
import ai.txai.common.log.LOG
import ai.txai.common.mvvm.BaseViewModel
import ai.txai.common.observer.CommonObserver
import ai.txai.common.thread.TScheduler
import ai.txai.login.api.LoginApiRepository
import ai.txai.common.countrycode.Country
import ai.txai.login.data.SmsCode
import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

class LoginViewModel(application: Application): BaseViewModel(application) {
    companion object {
        private const val TAG = "LoginViewModel"
        const val LOGIN_FRAGMENT = 1
        const val LOGIN_SMS_FRAGMENT = 2

        const val SMS_TYPE_LOGIN = "LOGIN"
        const val SMS_TYPE_FIND_PWD = "FIND_PWD"
    }

    private val loginViewStatus = MutableLiveData(LOGIN_FRAGMENT)
    fun getLoginViewStatus() = loginViewStatus

    private val loginUserNumber = MutableLiveData<String>()
    fun getLoginUserNumber() = loginUserNumber

    private val loginSmsCode = MutableLiveData<SmsCode>()
    fun getLoginSmsCode() = loginSmsCode

    private val loginUserInfo = MutableLiveData<LoginInfo<LoginToken, LoginUser>>()
    fun getLoginUserInfo() = loginUserInfo

    private val countryInfoList = MutableLiveData<ArrayList<Country>>()
    fun getCountryList() = countryInfoList

    private val countDownStatus = MutableLiveData<Boolean>()
    fun getCountDownStatus() = countDownStatus

    private var legalArticlesInfo = MutableLiveData<ArrayList<ArticlesInfo>>()
    fun getLegalArticles() = legalArticlesInfo

    fun sendSmsCode(mobile: String, type: String) {
        LoginApiRepository.sendSms(mobile, type)
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<SmsCode>() {
                override fun onSuccess(t: SmsCode?) {
                    super.onSuccess(t)
                    LOG.d(TAG,"on success $t")
                    t?.let { loginSmsCode.value = it }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    LOG.d(TAG,"on failed $msg")
                    val smsCode = SmsCode()
                    smsCode.setFailedMessage(msg ?: "")
                    loginSmsCode.value = smsCode
                }
            })
    }

    fun userLogin(code: String, mobile: String) {
        LoginApiRepository.userLogin(code, mobile)
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<LoginInfo<LoginToken, LoginUser>>() {
                override fun onSuccess(t: LoginInfo<LoginToken, LoginUser>?) {
                    super.onSuccess(t)
                    LOG.d(TAG,"user login on success $t")
                    t?.let {
                        loginUserInfo.value = it
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                    LOG.d(TAG,"user login on failed $msg")
                    val info = LoginInfo<LoginToken, LoginUser>()
                    info.setErrorMessage(msg ?: "")
                    loginUserInfo.value = info
                }
            })
    }

    fun loadCountryList() {
        LoginApiRepository.getCountryList()
            .subscribeOn(TScheduler.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: CommonObserver<MutableList<Country>>() {
                override fun onSuccess(t: MutableList<Country>?) {
                    super.onSuccess(t)
                    t?.let {
                        countryInfoList.value = t as ArrayList<Country>
                    }
                }

                override fun onFailed(msg: String?) {
                    super.onFailed(msg)
                }
            })
    }

    fun  loadLegalArticles() {
        CommonApiRepository.loadArticlesByType("legal", false)
            .subscribeOn(TScheduler.io())
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
}