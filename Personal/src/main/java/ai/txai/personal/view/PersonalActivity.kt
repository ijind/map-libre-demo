package ai.txai.personal.view

import ai.txai.common.mvvm.BaseMvvmActivity
import ai.txai.common.router.ARouterConstants
import ai.txai.personal.R
import ai.txai.personal.databinding.PersonalActivityLayoutBinding
import ai.txai.personal.viewmodel.PersonalViewModel
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = ARouterConstants.PATH_ACTIVITY_PERSONAL)
class PersonalActivity: BaseMvvmActivity<PersonalActivityLayoutBinding, PersonalViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = UserInfoFragment()
        fragment.let {
            transaction.replace(R.id.fragment_container, it)
            transaction.commit()
        }
    }

    override fun initViewBinding(): PersonalActivityLayoutBinding {
        return PersonalActivityLayoutBinding.inflate(layoutInflater)
    }
}