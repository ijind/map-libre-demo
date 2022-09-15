package ai.txai.database.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BlockingObject<T> {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private T obj;

    public synchronized void set(T t) {
        if (t == null) {
            return;
        }
        this.obj = t;
        countDownLatch.countDown();
    }

    public T get() {
        try {
            countDownLatch.await();
            return (T) obj;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T getImmediately() {
        return obj;
    }

    public T get(long timeout, TimeUnit unit) {
        try {
            countDownLatch.await(timeout, unit);
            return obj;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reset() {
        countDownLatch = new CountDownLatch(1);
        obj = null;
    }
}
