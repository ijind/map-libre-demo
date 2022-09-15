package ai.txai.lostfound.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ThreadUtils;

import java.util.List;

import ai.txai.common.base.BaseActivity;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.router.provider.BizProvider;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.thread.TScheduler;
import ai.txai.database.order.Order;
import ai.txai.database.site.Site;
import ai.txai.login.api.LoginApiRepository;
import ai.txai.common.countrycode.Country;
import ai.txai.lostfound.bean.ItemTypeEntry;
import ai.txai.lostfound.repository.LostFoundApiRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Time: 11/05/2022
 * Author Hay
 */
public class LostReportViewModel extends BaseViewModel {
    public LostReportViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<List<ItemTypeEntry>> itemTypes = new MutableLiveData<>();
    private MutableLiveData<List<Country>> countries = new MutableLiveData();
    private MutableLiveData<Order> order = new MutableLiveData();

    public MutableLiveData<List<ItemTypeEntry>> getItemTypes() {
        return itemTypes;
    }

    public MutableLiveData<List<Country>> getCountries() {
        return countries;
    }

    public MutableLiveData<Order> getOrder() {
        return order;
    }

    public void requestItemTypes() {
        List<ItemTypeEntry> value = itemTypes.getValue();
        if (value == null || value.isEmpty()) {
            showLoading("");
            LostFoundApiRepository.INSTANCE.itemTypes().subscribeOn(TScheduler.INSTANCE.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CommonObserver<List<ItemTypeEntry>>() {
                        @Override
                        public void onSuccess(@Nullable List<ItemTypeEntry> itemTypeEntries) {
                            super.onSuccess(itemTypeEntries);
                            itemTypes.postValue(itemTypeEntries);
                            hideLoading();
                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            super.onFailed(msg);
                            showToast(msg, false);
                            hideLoading();
                        }
                    });
        } else {
            itemTypes.postValue(value);
        }
    }

    public void requestCounties() {
        List<Country> value = countries.getValue();
        if (value == null || value.isEmpty()) {
            showLoading("");
            LoginApiRepository.INSTANCE.getCountryList()
                    .subscribeOn(TScheduler.INSTANCE.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CommonObserver<List<Country>>() {
                        @Override
                        public void onSuccess(@Nullable List<Country> it) {
                            super.onSuccess(it);
                            countries.postValue(it);
                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            super.onFailed(msg);
                            showToast(msg, false);
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            hideLoading();
                        }
                    });
        } else {
            countries.postValue(value);
        }
    }

    public void requestOrderInfo(String orderId) {
        ThreadUtils.getIoPool().submit(new Runnable() {
            @Override
            public void run() {
                BizProvider bizProvider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_BIZ);
                if(bizProvider == null) {
                    return;
                }
                Order realOrder = bizProvider.getOrder(orderId);
                if (realOrder == null) {
                    return;
                }
                order.postValue(realOrder);
            }
        });
    }

    public void lostAdd(String callingCode, ItemTypeEntry itemTypeEntry, String email, String summary, String phone, String orderId) {
        showLoading("");
        callingCode = callingCode.replace("+", "").trim();
        LostFoundApiRepository.INSTANCE.lostAdd(Integer.parseInt(callingCode), email, itemTypeEntry, orderId, phone, summary)
                .subscribeOn(TScheduler.INSTANCE.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable Integer integer) {
                        super.onSuccess(integer);
                        hideLoading();
                        ARouterUtils.navigation(BaseActivity.getLastActivity(), ARouterConstants.PATH_FRAGMENT_LOST_SUCCESS_SUBMIT);
                        finish();
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        hideLoading();
                        showToast(msg, false);
                    }
                });
    }
}
