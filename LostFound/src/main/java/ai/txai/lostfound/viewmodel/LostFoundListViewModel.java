package ai.txai.lostfound.viewmodel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ai.txai.common.base.BaseListViewModel;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.thread.TScheduler;
import ai.txai.lostfound.activity.LostFoundMainActivity;
import ai.txai.lostfound.bean.LostDetailEntry;
import ai.txai.lostfound.bean.LostItem;
import ai.txai.lostfound.bean.LostListEntry;
import ai.txai.lostfound.repository.LostFoundApiRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Time: 10/05/2022
 * Author Hay
 */
public class LostFoundListViewModel extends BaseListViewModel<LostItem, Integer> {

    public String orderId;

    public LostFoundListViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        orderId = args.getString(LostFoundMainActivity.EXTRA_ORDER_ID);
    }

    @Override
    public void loadData(Integer page, boolean forMore) {
        showLoading("");
        LostFoundApiRepository.INSTANCE.lostList(orderId, page == null ? 0 : page + 1, 20)
                .subscribeOn(TScheduler.INSTANCE.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable LostListEntry t) {
                        super.onSuccess(t);
                        hideLoading();
                        if (t != null) {
                            list = new ArrayList<>(t.data);
                            flag = t.page;
                            hasMore.postValue(t.totalPage > t.page);
                            if (forMore) {
                                loadMore.postValue(true);
                            } else {
                                refresh.postValue(true);
                            }
                        }
                    }

                    @Override
                    public void onFailed(@Nullable String msg) {
                        super.onFailed(msg);
                        hideLoading();
                    }
                });

    }
}
