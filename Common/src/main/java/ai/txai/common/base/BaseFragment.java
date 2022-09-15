package ai.txai.common.base;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.NetworkUtils;
import com.trello.rxlifecycle2.components.support.RxFragment;

import ai.txai.common.R;
import ai.txai.common.network.NetworkObserver;
import ai.txai.common.utils.ToastUtils;

/**
 * Time: 2/17/22
 * Author Hay
 */
public class BaseFragment extends RxFragment implements NetworkObserver {

    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {

    }

    @Override
    public void onDisconnected() {

    }
}
