package ai.txai.commonbiz.bean;

/**
 * Time: 11/05/2022
 * Author Hay
 */
public class PendingCountDown {
    public long start;//单位毫秒
    public int duration;//单位秒



    public static PendingCountDown newInstance(long start, int duration) {
        PendingCountDown pendingCountDown = new PendingCountDown();
        pendingCountDown.start = start;
        pendingCountDown.duration = duration;
        return pendingCountDown;
    }
}
