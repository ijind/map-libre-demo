package ai.txai.common.permission.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ai.txai.common.permission.AndPermission;

class Messenger extends BroadcastReceiver {

    public static void send(Context context) {
        Intent broadcast = new Intent(AndPermission.bridgeAction(context));
//        context.sendBroadcast(broadcast);

        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private final Context mContext;
    private final Callback mCallback;

    public Messenger(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public void register() {
        IntentFilter filter = new IntentFilter(AndPermission.bridgeAction(mContext));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, filter);
    }

    public void unRegister() {
        try {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        } catch (Exception ignore) {

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCallback.onCallback();
    }

    public interface Callback {

        void onCallback();
    }
}