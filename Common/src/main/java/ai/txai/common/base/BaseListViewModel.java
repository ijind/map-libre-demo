package ai.txai.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ai.txai.common.mvvm.BaseViewModel;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public abstract class BaseListViewModel<E,F> extends BaseViewModel {
    protected List<E> list;

    /**
     * 请求下一页的参数
     */
    protected F flag;

    /**
     *  true --> 重新获取数据， 从头开始
     *
     */
    protected MutableLiveData<Boolean> refresh = new MutableLiveData<>();

    /**
     * true --> 增量获取数据， 根据当前最后一条数据，再获取后续数据
     */
    protected MutableLiveData<Boolean> loadMore = new MutableLiveData<>();

    /**
     * 是否有更多数据， 如果没有取消上拉加载
     */
    protected MutableLiveData<Boolean> hasMore = new MutableLiveData<>();


    public BaseListViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void loadData(F f, boolean forMore);

    public List<E> getList() {
        return list;
    }

    public MutableLiveData<Boolean> getRefresh() {
        return refresh;
    }

    public MutableLiveData<Boolean> getHasMore() {
        return hasMore;
    }
}
