package ai.txai.setting.view

import ai.txai.common.base.BaseScrollFragment
import ai.txai.common.data.UpdateVersionInfo
import ai.txai.common.utils.DeviceUtils
import ai.txai.common.utils.ToastUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.common.widget.popupview.UpdateVersionView
import ai.txai.setting.R
import ai.txai.setting.databinding.SettingContentLayoutBinding
import ai.txai.setting.popup.SettingLogoutView
import ai.txai.setting.viewmodel.SettingViewModel
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup

class SettingContentFragment: BaseScrollFragment<SettingContentLayoutBinding, SettingViewModel>() {
    private var logoutView: SettingLogoutView? = null
    private var updateView: UpdateVersionView? = null

    private var isNeedUpdate = false
    private var updateInfo: UpdateVersionInfo? = null

    override fun initViewObservable() {
        super.initViewObservable()
        initView()
        initListener()
        addObserver()
        showLoading("")
        viewModel.checkAppUpdate()
    }

    override fun onRefresh() {
        viewModel.checkAppUpdate()
    }

    private fun initView() {
        bottomShadowVisible(false)
        viewModel.loadLegalArticles(SettingViewModel.LEGAL_ARTICLES, false)

        val version = "V${DeviceUtils.getVersion()}"
        itemBinding.settingUpdateTv.setRightContent(version)

        logoutView = SettingLogoutView(requireActivity(),
            object: SettingLogoutView.OnLogoutConfirmListener {
                override fun onLogoutConfirm() {
                    viewModel.logout()
                }
        })

        updateView = UpdateVersionView(requireActivity())
        updateView!!.setOnUpgradeConfirmListener(object:
            UpdateVersionView.OnUpgradeConfirmListener {
            override fun onUpgradeConfirm() {
                val url = updateInfo?.getDownLoadUrl() ?: ""
                if (url.isNotEmpty()) {
                    val intent = Intent()
                    intent.data = Uri.parse(url)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Intent.ACTION_VIEW
                    startActivity(intent)
                }
            }
        })
    }

    private fun addObserver() {
        viewModel.getAppUpdateInfo().observe(this) {
            hideLoading()
            if (it == null) {
                isNeedUpdate = false
                return@observe
            }

            isNeedUpdate = true
            val version = "V${it.getNewVersion()}"
            itemBinding.settingUpdateTv.setRightContent(version)
            itemBinding.settingUpdateTv.setPreContentVisible(true)
            updateInfo = it
        }
    }

    private fun initListener() {
        itemBinding.settingLegalTv.setDebounceClickListener {
            viewModel.getSettingViewStatus().value = SettingViewModel.SETTING_LEGAL
        }
        positiveClickListener(R.string.setting_sign_out) {
            logoutView?.showPop()
        }

        itemBinding.settingUpdateTv.setDebounceClickListener {
            if (isNeedUpdate) {
                updateInfo?.let {
                    updateView?.showPop("V${it.getNewVersion()}", it.getUpdateBrief()
                        , UpdateVersionView.NORMAL_TYPE)
                } ?: ToastUtils.show(R.string.setting_no_upates)
            } else {
                ToastUtils.show(R.string.setting_no_upates)
            }
        }
    }

    override fun getCustomTitle() = R.string.setting_title

    override fun initItemBinding(parent: ViewGroup): SettingContentLayoutBinding {
        return SettingContentLayoutBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
    }
}