package ai.txai.common.mvvm;

import android.os.Bundle;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import ai.txai.common.data.ToastInfo;
import ai.txai.common.data.UpdateVersionInfo;
import ai.txai.common.event.SingleLiveEvent;


public class UIChangeLiveData extends SingleLiveEvent {

    public SingleLiveEvent<Map<String, Object>> jumpPage;
    public SingleLiveEvent<Void> finishEvent;
    public SingleLiveEvent<Void> onBackPressedEvent;
    public SingleLiveEvent<Bundle> finishAllActivities;
    public SingleLiveEvent<UpdateVersionInfo> checkUpdateVersion;
    public MutableLiveData<ToastInfo> showToastEvent = new MutableLiveData<>();
    public MutableLiveData<Pair<String, Boolean>> showLoading = new MutableLiveData<>();

    public SingleLiveEvent<Map<String, Object>> getJumpPage() {
        return jumpPage = createLiveData(jumpPage);
    }

    public SingleLiveEvent<Void> getFinishEvent() {
        return finishEvent = createLiveData(finishEvent);
    }

    public SingleLiveEvent<Void> getOnBackPressedEvent() {
        return onBackPressedEvent = createLiveData(onBackPressedEvent);
    }

    public MutableLiveData<ToastInfo> getShowToastEvent() {
        return showToastEvent;
    }

    public MutableLiveData<Pair<String, Boolean>> getShowLoading() {
        return showLoading;
    }

    public SingleLiveEvent<Bundle> getFinishAllActivities() {
        return finishAllActivities = createLiveData(finishAllActivities);
    }

    public SingleLiveEvent<UpdateVersionInfo> getCheckUpdateVersionEvent() {
        return checkUpdateVersion = createLiveData(checkUpdateVersion);
    }

    private <T> SingleLiveEvent<T> createLiveData(SingleLiveEvent<T> liveData) {
        if (liveData == null) {
            liveData = new SingleLiveEvent<>();
        }
        return liveData;
    }
}
