package ai.txai.setting.view

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.router.ARouterConstants
import ai.txai.setting.R
import ai.txai.setting.databinding.SettingActivityLayoutBinding
import ai.txai.setting.viewmodel.SettingViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = ARouterConstants.PATH_ACTIVITY_SETTING)
class SettingActivity: BaseMvvmActivity<SettingActivityLayoutBinding, SettingViewModel>() {
    companion object {
        private const val NORMAL_VIEW = "normal"
        private const val HELP_INFO_VIEW = "helper"
    }
    // Use "normal" to show Setting fragment. Use "helper" to show service and helper fragment
    private var showType = "normal"

    override fun initViewBinding(): SettingActivityLayoutBinding {
        return SettingActivityLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addObserver()
        intent?.extras?.let {
            if (it.containsKey("showType")) {
                showType = it["showType"].toString()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            //super.onBackPressed()
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private fun addObserver() {
        viewModel.getSettingViewStatus().observe(this) {
            switchFragment(it)
        }
    }

    private fun switchFragment(index: Int) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = createFragmentByShowType(index)
        fragment.let {
            transaction.replace(R.id.fragment_container, it)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun createFragmentByShowType(index: Int): Fragment {
        return when (showType) {
            NORMAL_VIEW -> getFragment(index)
            HELP_INFO_VIEW -> ServiceHelperFragment()
            else -> getFragment(index)
        }
    }

    private fun getFragment(index: Int): Fragment =
        when (index) {
            SettingViewModel.SETTING_CONTENT -> SettingContentFragment()
            SettingViewModel.SETTING_LEGAL -> LegalFragment()
            else -> SettingContentFragment()
        }
}