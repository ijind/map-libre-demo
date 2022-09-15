package ai.txai.common.permission.bridge;

import android.content.Context;


final class RequestExecutor extends Thread implements Messenger.Callback {

    private BridgeRequest mRequest;
    private Messenger mMessenger;

    public RequestExecutor(BridgeRequest request) {
        this.mRequest = request;
    }

    @Override
    public void run() {
        Context context = mRequest.getSource().getContext();
        mMessenger = new Messenger(context, this);
        mMessenger.register();

        executeCurrent(mRequest);
    }

    private void executeCurrent(BridgeRequest mRequest) {
        switch (mRequest.getType()) {
            case BridgeRequest.TYPE_APP_DETAILS: {
                BridgeActivity.requestAppDetails(mRequest);
                break;
            }
            case BridgeRequest.TYPE_PERMISSION: {
                BridgeActivity.requestPermission(mRequest);
                break;
            }
            case BridgeRequest.TYPE_INSTALL: {
                BridgeActivity.requestInstall(mRequest);
                break;
            }
            case BridgeRequest.TYPE_OVERLAY: {
                BridgeActivity.requestOverlay(mRequest);
                break;
            }
            case BridgeRequest.TYPE_ALERT_WINDOW: {
                BridgeActivity.requestAlertWindow(mRequest);
                break;
            }
            case BridgeRequest.TYPE_NOTIFY: {
                BridgeActivity.requestNotify(mRequest);
                break;
            }
            case BridgeRequest.TYPE_NOTIFY_LISTENER: {
                BridgeActivity.requestNotificationListener(mRequest);
                break;
            }
            case BridgeRequest.TYPE_WRITE_SETTING: {
                BridgeActivity.requestWriteSetting(mRequest);
                break;
            }
        }
    }


    @Override
    public void onCallback() {
        synchronized (this) {
            mMessenger.unRegister();
            mRequest.getCallback().onCallback();
            mMessenger = null;
            mRequest = null;
        }
    }
}