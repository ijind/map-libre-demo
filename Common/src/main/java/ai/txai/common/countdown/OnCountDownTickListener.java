package ai.txai.common.countdown;

/**
 * Time: 28/03/2022
 * Author Hay
 */
public interface OnCountDownTickListener {
    void onTick(long time);

    void onFinish();

}
