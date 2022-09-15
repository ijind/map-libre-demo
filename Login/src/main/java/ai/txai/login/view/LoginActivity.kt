package ai.txai.login.view

import ai.txai.common.log.LOG
import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.router.ARouterConstants
import ai.txai.common.utils.LogUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.login.R
import ai.txai.login.databinding.LoginMainLayoutBinding
import ai.txai.login.viewmodel.LoginViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.NetworkUtils

@Route(path = ARouterConstants.PATH_ACTIVITY_LOGIN)
class LoginActivity: BaseMvvmActivity<LoginMainLayoutBinding, LoginViewModel>() {
    companion object {
        private const val TAG = "LoginMainView"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadCountryList()
        viewModel.loadLegalArticles()
        viewModel.checkUpdateVersion()
    }

    override fun initViewObservable() {
        viewModel.getLoginViewStatus().observe(this) {
            if (it == null) return@observe

            LOG.d(TAG,"login status")
            switchFragment(it)
        }

        viewModel.getLoginSmsCode().observe(this) { smsCode ->
            if (smsCode == null) {
                return@observe
            }

            if (smsCode.getSmsCode().isEmpty()) {
                val failMessage = smsCode.getFailedMessage()
                if (failMessage.isEmpty()) {
                    ToastUtils.show(R.string.login_sms_code_not_sent)
                } else {
                    ToastUtils.show(failMessage)
                }
            } else {
                ToastUtils.show(R.string.login_sms_code_sent, true)
                viewModel.getCountDownStatus().value = true
                val curStatus = viewModel.getLoginViewStatus().value
                if (curStatus != LoginViewModel.LOGIN_SMS_FRAGMENT)
                viewModel.getLoginViewStatus().postValue(LoginViewModel.LOGIN_SMS_FRAGMENT)
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            //super.onBackPressed()
            finish()
        } else {
            supportFragmentManager.popBackStack()
            viewModel.getLoginViewStatus().value = null
            viewModel.getLoginUserInfo().value = null
        }
    }

    private fun switchFragment(index: Int) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = getFragment(index)
        fragment?.let {
            transaction.replace(R.id.fragment_container, it)
            transaction.addToBackStack(null)
            transaction.commit()
        }?.let { LOG.e(TAG,"Fragment can't be null!!") }
    }


    private fun getFragment(index: Int): Fragment? =
        when (index) {
            LoginViewModel.LOGIN_FRAGMENT -> LoginFragment()
            LoginViewModel.LOGIN_SMS_FRAGMENT -> SMSCodeFragment()
            else -> null
        }

    override fun initViewBinding(): LoginMainLayoutBinding {
        return LoginMainLayoutBinding.inflate(layoutInflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.getLoginSmsCode().value = null
        binding = null
    }
}