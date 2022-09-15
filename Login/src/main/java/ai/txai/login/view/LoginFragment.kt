package ai.txai.login.view

import ai.txai.common.activity.WebActivity
import ai.txai.common.api.ApiConfig
import ai.txai.common.base.ParameterField
import ai.txai.common.data.ArticlesInfo
import ai.txai.common.log.LOG
import ai.txai.common.mvvm.BaseMvvmFragment
import ai.txai.common.utils.AndroidUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.popupview.ChangeEnvView
import ai.txai.login.R
import ai.txai.common.countrycode.Country
import ai.txai.common.utils.WebUtils
import ai.txai.common.widget.txaiedittext.AutoSeparationInputView
import ai.txai.login.databinding.LoginFragmentLayoutBinding
import ai.txai.login.utils.LoginModuleUtils
import ai.txai.login.viewmodel.LoginViewModel
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat

class LoginFragment : BaseMvvmFragment<LoginFragmentLayoutBinding, LoginViewModel>() {
    companion object {
        private const val TAG = "loginFragment"
    }

    private var isSelected = false
    private var isCanContinue = false
    private var curCountryCode = ""
    
    private var changeEnv: ChangeEnvView? = null
    
    private var clickWelcome = 0
    private var clickTime = 0L

    override fun initViewObservable() {
        initView()
        initListener()
        addObserver()
    }

    private fun initView() {
        val stringBuilder = SpannableStringBuilder()
        stringBuilder.append("Agree ")
            .append("User Agreement ")
            .append("& ")
            .append("Privacy Policy")
        val userStart = 5
        val userEnd = 20
        val privacyStart = 22

        val privacyClickSpan = LoginModuleUtils.createClickSpan(resources) {
            startWebByLegalName(WebUtils.PRIVACY_LEGAL_NAME)
        }

        val userClickSpan  = LoginModuleUtils.createClickSpan(resources) {
            startWebByLegalName(WebUtils.USER_LEGAL_NAME)
        }

        stringBuilder.setSpan(userClickSpan, userStart, userEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        stringBuilder.setSpan(
            privacyClickSpan, privacyStart, stringBuilder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.loginPrivacyTv.text = stringBuilder
        binding.loginPrivacyTv.highlightColor = Color.TRANSPARENT
        binding.loginPrivacyTv.movementMethod = LinkMovementMethod.getInstance()

        binding.loginInputNumber.setAutoSeparationStatusListener(object :
            AutoSeparationInputView.AutoSeparationStatusListener {
            override fun onInputAfter(text: Editable?) {
                if (text == null) return

                if (text.trim().isEmpty()) {
                    updateContinueBtnStatus(false)
                } else if (isSelected) {
                    updateContinueBtnStatus(true)
                }
            }

            override fun onIsoCodeChange(areaCode: String, isoCode: String) {
                curCountryCode = areaCode
                AndroidUtils.curCountryIsoCode = isoCode
            }

            override fun requestCountries() {

            }
        })

        var resId = R.mipmap.login_select_default_ic
        if (isSelected) {
            resId = R.mipmap.login_selected_ic
        }
        binding.loginSelectImg.setImageResource(resId)
        binding.loginContinueTv.setPositiveEnable(isCanContinue)
        initRegionTv()
    }

    @SuppressLint("SetTextI18n")
    private fun initRegionTv() {
        var isoCodeString = "\uD83C\uDDE6\uD83C\uDDEA  +971"
        if (curCountryCode.isNotEmpty()) {
            isoCodeString = curCountryCode
        }
        binding.loginInputNumber.setIsoCode(isoCodeString)
    }

    private fun initListener() {
        binding.loginSelectImg.setOnClickListener {
            isSelected = !isSelected
            if (isSelected) {
                (it as ImageView).setImageResource(R.mipmap.login_selected_ic)
                if (binding.loginInputNumber.getInputPhoneNumber().isNotEmpty()) {
                    updateContinueBtnStatus(true)
                }
            } else {
                (it as ImageView).setImageResource(R.mipmap.login_select_default_ic)
                updateContinueBtnStatus(false)
            }
        }

        binding.loginContinueTv.setPositiveClickListener {
            LOG.d(TAG, "login continue click")
            if (!isCanContinue) return@setPositiveClickListener

            val number = binding.loginInputNumber.getInputPhoneNumber().replace(" ", "")
            if (number.isEmpty() || number.length < 5) {
                ToastUtils.show(R.string.login_wrong_number_prompt)
                return@setPositiveClickListener
            }

            val inputNumber = binding.loginInputNumber.getPhoneNumberWithIsoCode()
            viewModel.sendSmsCode(inputNumber, LoginViewModel.SMS_TYPE_LOGIN)
            viewModel.getLoginUserNumber().value = inputNumber
        }

//        if (BuildConfig.DEBUG) {
            binding.loginWelcomeTv.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - clickTime > 2000) {
                    clickWelcome = 1
                } else {
                    clickWelcome++
                    if (clickWelcome >= 5) {
                        activity?.let {
                            if (changeEnv == null) {
                                changeEnv = ChangeEnvView(requireActivity())
                            }
                            changeEnv?.showPop()
                        }
                    }
                }
                clickTime = currentTime
            }
//        }
    }

    private fun addObserver() {
        viewModel.getCountryList().observe(this) {
            val popCountryList = ArrayList<Country>()
            it.forEach { country ->
                if (country.isPop()) {
                    val countryContainer = Country()
                    countryContainer.copyAllValues(country)
                    countryContainer.setFirstLetter("#")
                    popCountryList.add(countryContainer)
                }
            }
            val finalList = ArrayList<Country>()
            finalList.addAll(popCountryList)
            finalList.addAll(it)
            binding.loginInputNumber.setRegionData(finalList, it)
        }

        viewModel.getLegalArticles().observe(this) {
            if (it == null) return@observe

            it.forEach { info ->
                val userUrl = "${ApiConfig.baseUrl}/api/biz${info.getArticleLink()}"
                WebUtils.cacheAgreementUrl(info.getArticleName(), userUrl)
            }
        }
    }

    private fun updateContinueBtnStatus(isEnable: Boolean) {
        if (isEnable && !isCanContinue) {
            binding.loginContinueTv.setPositiveEnable(true)
            isCanContinue = true
            return
        }

        if (!isEnable && isCanContinue) {
            binding.loginContinueTv.setPositiveEnable(false)
            isCanContinue = false
        }
    }

    private fun startWebByLegalName(name: String) {
        val urlInfoList = viewModel.getLegalArticles().value ?: return

        var userInfo: ArticlesInfo? = null
        urlInfoList.forEach {
            if (it.getArticleName() == name) {
                userInfo = it
            }
        }
        userInfo?.let {
            val userUrl = "${ApiConfig.baseUrl}/api/biz${it.getArticleLink()}"
            val title = when (name) {
                WebUtils.USER_LEGAL_NAME -> R.string.commonview_user_agreement
                WebUtils.PRIVACY_LEGAL_NAME -> R.string.commonview_privacy
                else -> 0
            }
            val bundle = Bundle()
            bundle.putString(ParameterField.WEB_URL, userUrl)
            bundle.putInt(ParameterField.WEB_TITLE, title)
            val intent = Intent(requireActivity(), WebActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        changeEnv?.dismissPop()
        binding.loginInputNumber.removeEditTextWatcher()
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup
    ): LoginFragmentLayoutBinding {
        return LoginFragmentLayoutBinding.inflate(inflater, container, false)
    }
}