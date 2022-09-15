package ai.txai.login.view

import ai.txai.login.data.LoginInfo
import ai.txai.login.data.LoginToken
import ai.txai.login.data.LoginUser
import ai.txai.common.external.LoginManager
import ai.txai.common.glide.GlideUtils
import ai.txai.common.mvvm.BaseMvvmFragment
import ai.txai.common.router.ARouterConstants
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.VerificationCodeView
import ai.txai.database.user.User
import ai.txai.login.R
import ai.txai.login.databinding.LoginSmsLayoutBinding
import ai.txai.login.viewmodel.LoginViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.blankj.utilcode.util.KeyboardUtils

class SMSCodeFragment: BaseMvvmFragment<LoginSmsLayoutBinding, LoginViewModel>() {
    companion object {
        private const val TAG = "SMSCodeFragment"
    }
    private var curUserPhoneNumber = ""


    override fun initViewObservable() {
        initListener()
        addObserver()
        showInput()
    }

    private fun initListener() {
        binding.loginSmsBackImg.setDebounceClickListener {
            binding.loginSmsCountDownView.cancel()
            activity?.onBackPressed()
            viewModel.getLoginViewStatus().value = null
            viewModel.getLoginUserInfo().value = null
        }
        binding.loginSmsCountDownView.setClickListener {
            viewModel.sendSmsCode(curUserPhoneNumber, LoginViewModel.SMS_TYPE_LOGIN)
        }

        binding.loginSmsCodeView.setInputDownListener(object: VerificationCodeView.InputDownListener {
            override fun onInputDown(smsCode: String) {
                viewModel.userLogin(smsCode, curUserPhoneNumber)
            }
        })

        //TODO  delete this when release
        binding.tvTestSmsCode.text = viewModel.getLoginSmsCode().value?.getSmsCode()
    }

    @SuppressLint("SetTextI18n")
    private fun addObserver() {
        viewModel.getCountDownStatus().observe(this) {
            if (it == null) return@observe

            if (binding.loginSmsCountDownView.isFinish()) {
                binding.loginSmsCountDownView.start()
                //TODO  delete this when release
                binding.tvTestSmsCode.text = viewModel.getLoginSmsCode().value?.getSmsCode()
            }
        }
        viewModel.getLoginUserNumber().observe(this) {
            curUserPhoneNumber = it
            binding.loginSmsPhoneTv.text = "+$it"
        }

        viewModel.getLoginUserInfo().observe(this) { loginInfo ->
            if (loginInfo == null) return@observe

            if (loginInfo.getUser() == null || loginInfo.getToken() == null) {
                loginFailed(loginInfo)
                return@observe
            }

            val loginUser = loginInfo.getUser() as LoginUser
            val loginToken = loginInfo.getToken() as LoginToken
            val toUser = toUser(loginUser, loginToken)
            LoginManager.getInstance().whenLogin(toUser, false)
            GlideUtils.preloadImage(AndroidUtils.getApplicationContext(), loginUser.getAvatar())
            binding.loginSmsCountDownView.cancel()
            ToastUtils.show(R.string.login_login_success, true)
            AndroidUtils.delayOperation(100) {
                viewModel.router(ARouterConstants.PATH_ACTIVITY_V2_MAIN, Bundle())
                activity?.finish()
            }
        }
    }

    private fun loginFailed(info: LoginInfo<LoginToken, LoginUser>?) {
        val failedMessage = info?.getErrorMessage() ?: ""
        if (failedMessage.isEmpty()) {
            ToastUtils.show(R.string.login_login_failed)
        } else {
            ToastUtils.show(failedMessage)
        }
    }

    private fun showInput() {
        KeyboardUtils.showSoftInput(binding.root, 0)
        binding.loginSmsCodeView.smsFirFocusable()
    }

    private fun toUser(userInfo: LoginUser, token: LoginToken): User {
        val user = User()
        user.avatar = userInfo.getAvatar()
        user.email = userInfo.getEmail()
        user.uid = userInfo.getId().toString()
        user.nickname = userInfo.getNickname()
        user.countryCode = getCountryCode()
        user.phoneNumber =
            if (curUserPhoneNumber.isNotEmpty()) {
                val countryCode = getCountryCode()
                val endIndex = curUserPhoneNumber.length
                val phone = curUserPhoneNumber.substring(countryCode.toString().length, endIndex)
                phone.trim().toLong()
            } else {
                0L
            }
        user.apiToken = token.getApiToken()
        user.pushToken = token.getPushToken()
        user.updateToken = token.getUpdateToken()
        user.isoCode = AndroidUtils.curCountryIsoCode
        return user
    }

    private fun getCountryCode(): Int {
        val code = viewModel.getLoginUserNumber().value
        val endIndex = code.toString().indexOf(" ")
        return code.toString().substring(0, endIndex).toInt()
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup
    ): LoginSmsLayoutBinding {
        return LoginSmsLayoutBinding.inflate(inflater, container, false)
    }
}