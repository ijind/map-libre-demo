package ai.txai.common.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.lang.ref.WeakReference;

import ai.txai.common.R;
import ai.txai.common.base.ParameterField;
import ai.txai.common.databinding.CommonActivityContainerBinding;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.mvvm.BaseMvvmFragment;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.router.ARouterConstants;


/**
 * 盛装Fragment的一个容器(代理)Activity
 * 普通界面只需要编写Fragment,使用此Activity盛装,这样就不需要每个界面都在AndroidManifest中注册一遍
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_CONTAINER)
public class ContainerActivity extends BaseMvvmActivity<CommonActivityContainerBinding, BaseViewModel> {
    private static final String FRAGMENT_TAG = "content_fragment_tag";
    protected WeakReference<Fragment> mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = null;
        if (savedInstanceState != null) {
            fragment = fm.getFragment(savedInstanceState, FRAGMENT_TAG);
        }
        if (fragment == null) {
            Bundle extras = getIntent().getExtras();
            String fragmentPath = extras.getString(ParameterField.AROUTER_PATH);
            fragment = initFromIntent(fragmentPath, extras);
        }
        if (fragment == null) {
            finish();
            return;
        }
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.replace(R.id.framelayout_container, fragment);
        trans.commitAllowingStateLoss();
        mFragment = new WeakReference<>(fragment);
    }

    @Override
    protected CommonActivityContainerBinding initViewBinding() {
        return CommonActivityContainerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, mFragment.get());
    }

    protected Fragment initFromIntent(String path, Bundle bundle) {
        Object navigation = ARouter.getInstance().build(path).with(bundle).navigation();
        return (Fragment) navigation;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
