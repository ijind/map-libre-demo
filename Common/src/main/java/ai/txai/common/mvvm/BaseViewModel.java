package ai.txai.common.mvvm;

import android.app.Application;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.trello.rxlifecycle2.LifecycleProvider;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import ai.txai.common.api.CommonApiRepository;
import ai.txai.common.base.ParameterField;
import ai.txai.common.data.ToastInfo;
import ai.txai.common.data.UpdateVersionInfo;
import ai.txai.common.observer.CommonObserver;
import ai.txai.common.thread.TScheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class BaseViewModel extends AndroidViewModel implements IBaseViewModel, Consumer<Disposable> {

    private UIChangeLiveData uc;
    //弱引用持有
    private WeakReference<LifecycleProvider> lifecycle;
    //管理RxJava，主要针对RxJava异步操作造成的内存泄漏
    private CompositeDisposable mCompositeDisposable;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        mCompositeDisposable = new CompositeDisposable();
    }

    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onAny(LifecycleOwner owner, Lifecycle.Event event) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        //ViewModel销毁时会执行，同时取消所有异步任务
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    @Override
    public void accept(Disposable disposable) {
        addSubscribe(disposable);
    }

    protected void addSubscribe(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }
    /**
     * 注入RxLifecycle生命周期
     *
     * @param lifecycle
     */
    public void injectLifecycleProvider(LifecycleProvider lifecycle) {
        this.lifecycle = new WeakReference<>(lifecycle);
    }

    public LifecycleProvider getLifecycleProvider() {
        return lifecycle.get();
    }

    public UIChangeLiveData getUC() {
        if (uc == null) {
            uc = new UIChangeLiveData();
        }
        return uc;
    }

    public void showToast(String content, boolean isShowDrawable) {
        ToastInfo info = new ToastInfo();
        info.setContent(content);
        info.setIsShowDrawable(isShowDrawable);
        uc.showToastEvent.postValue(info);
    }

    public void router(String path, Bundle bundle) {
        router(path, bundle, -1);
    }

    public void router(String path, Bundle bundle, int flags) {
        Map<String, Object> params = new HashMap<>();
        params.put(ParameterField.AROUTER_PATH, path);
        if (bundle != null) {
            params.put(ParameterField.BUNDLE, bundle);
        }
        if (flags > 0) {
            params.put(ParameterField.FLAGS, flags);
        }
        uc.jumpPage.postValue(params);
    }

    public void finishAllActivities(Bundle bundle) {
        uc.finishAllActivities.postValue(bundle);
    }

    /**
     * Check app new version
     */
    public void checkUpdateVersion() {
        CommonApiRepository.INSTANCE.checkAppUpdate()
                .subscribeOn(TScheduler.INSTANCE.io())
                .subscribe(new CommonObserver<>() {
                    @Override
                    public void onSuccess(@Nullable UpdateVersionInfo updateVersionInfo) {
                        super.onSuccess(updateVersionInfo);
                        uc.checkUpdateVersion.postValue(updateVersionInfo);
                    }
                });
    }

    /**
     * 关闭界面
     */
    public void finish() {
        uc.finishEvent.call();
    }

    public void showLoading(String text) {
        uc.showLoading.postValue(new Pair<>(text, Boolean.TRUE));
    }

    public void hideLoading() {
        uc.showLoading.postValue(new Pair<>("", Boolean.FALSE));
    }

    /**
     * 返回上一层
     */
    public void onBackPressed() {
        uc.onBackPressedEvent.call();
    }

}
