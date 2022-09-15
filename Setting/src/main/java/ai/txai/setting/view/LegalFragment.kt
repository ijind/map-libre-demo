package ai.txai.setting.view

import ai.txai.common.activity.WebActivity
import ai.txai.common.api.ApiConfig
import ai.txai.common.base.BaseScrollFragment
import ai.txai.common.base.ParameterField
import ai.txai.common.base.helper.ScrollBindingHelper
import ai.txai.common.data.ArticlesInfo
import ai.txai.common.databinding.CommonFragmentScrollBinding
import ai.txai.common.mvvm.BaseMvvmFragment
import ai.txai.common.router.ARouterConstants
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.setting.R
import ai.txai.setting.databinding.SettingLegalLayoutBinding
import ai.txai.setting.viewmodel.SettingViewModel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup

class LegalFragment: BaseScrollFragment<SettingLegalLayoutBinding, SettingViewModel>() {
    companion object {
        private const val TAG = "LegalFragment"
        private const val USER_LEGAL_NAME = "user-service-agreement"
        private const val PRIVACY_LEGAL_NAME = "privacy-policy"
    }

    override fun initViewObservable() {
        super.initViewObservable()

        itemBinding.settingUserAgreementTv.setDebounceClickListener {
            startWebByLegalName(USER_LEGAL_NAME)
        }

        itemBinding.settingPrivacyTv.setDebounceClickListener {
            startWebByLegalName(PRIVACY_LEGAL_NAME)
        }
    }

    private fun startWebByLegalName(name: String) {
        val urlInfoList = viewModel.getArticles().value ?: return

        var userInfo: ArticlesInfo? = null
        urlInfoList.forEach {
            if (it.getArticleName() == name) {
                userInfo = it
            }
        }
        userInfo?.let {
            val userUrl = "${ApiConfig.baseUrl}/api/biz${it.getArticleLink()}"
            val title = when(name) {
                USER_LEGAL_NAME -> R.string.commonview_user_agreement
                PRIVACY_LEGAL_NAME -> R.string.commonview_privacy
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

    override fun getCustomTitle() = R.string.setting_legal_title

    override fun initItemBinding(parent: ViewGroup): SettingLegalLayoutBinding {
        return SettingLegalLayoutBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
    }
}