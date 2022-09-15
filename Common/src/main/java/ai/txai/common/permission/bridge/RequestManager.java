package ai.txai.common.permission.bridge;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class RequestManager {

    private static volatile RequestManager sManager;

    public static RequestManager get() {
        if (sManager == null) {
            synchronized (RequestManager.class) {
                if (sManager == null) {
                    sManager = new RequestManager();
                }
            }
        }
        return sManager;
    }

    private final Executor mExecutor;

    private RequestManager() {
        this.mExecutor = Executors.newCachedThreadPool();
    }

    public void add(BridgeRequest request) {
        mExecutor.execute(new RequestExecutor(request));
    }
}