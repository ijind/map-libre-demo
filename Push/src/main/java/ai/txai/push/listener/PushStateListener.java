package ai.txai.push.listener;

/**
 * Time: 13/04/2022
 * Author Hay
 */
public interface PushStateListener {
    void onStateChanged(int from, int to);
}
