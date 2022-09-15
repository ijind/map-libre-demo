package ai.txai.lostfound.viewmodel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ThreadUtils;

import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.provider.BizProvider;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.thread.TScheduler;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.lostfound.bean.LostDetailEntry;
import ai.txai.lostfound.fragment.LostProgressFragment;
import ai.txai.lostfound.repository.LostFoundApiRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Time: 11/05/2022
 * Author Hay
 */
public class LostDetailViewModel extends BaseViewModel {
    public LostDetailViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<LostDetailEntry> detail = new MutableLiveData<>();

    private MutableLiveData<Order> order = new MutableLiveData();

    public MutableLiveData<LostDetailEntry> getDetail() {
        return detail;
    }

    public MutableLiveData<Order> getOrder() {
        return order;
    }

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        String lostId = args.getString(LostProgressFragment.EXTRA_LOST_ID);
        requestDetails(lostId);
    }

    private void requestDetails(String id) {
        showLoading("");
        LostFoundApiRepository.INSTANCE.lostDetail(id)
                .subscribeOn(TScheduler.INSTANCE.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable LostDetailEntry lostDetailEntry) {
                        super.onSuccess(lostDetailEntry);
                        hideLoading();
                        detail.postValue(lostDetailEntry);
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        hideLoading();
                    }
                });
    }

    public void requestOrderInfo(String orderId) {
        ThreadUtils.getIoPool().submit(new Runnable() {
            @Override
            public void run() {
                BizProvider bizProvider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_BIZ);
                Order realOrder = bizProvider.getOrder(orderId);
                if (realOrder == null) {
                    return;
                }
                order.postValue(realOrder);
            }
        });
    }
}
