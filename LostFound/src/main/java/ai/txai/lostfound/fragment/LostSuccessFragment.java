package ai.txai.lostfound.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.router.provider.LostFoundProvider;
import ai.txai.common.router.ProviderManager;
import ai.txai.lostfound.activity.LostFoundMainActivity;
import ai.txai.lostfound.databinding.LfSubmitSuccessBinding;

/**
 * Time: 28/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_LOST_SUCCESS_SUBMIT)
public class LostSuccessFragment extends BaseScrollFragment<LfSubmitSuccessBinding, BaseViewModel> {
    @Override
    protected int getCustomTitle() {
        return 0;
    }

    @Override
    protected LfSubmitSuccessBinding initItemBinding(ViewGroup parent) {
        return LfSubmitSuccessBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();

        itemBinding.btnOk.setNegativeClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean(LostFoundMainActivity.EXTRA_EXIST, true);
            ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_ACTIVITY_LF, args);
        });
    }
}
