package ai.txai.common.countdown;

import android.os.CountDownTimer;

import java.util.concurrent.TimeUnit;

/**
 * Time: 28/03/2022
 * Author Hay
 */
public class CountDownTimerManager {

    public static CountDownTimer startCountdown(final long time,final TimeUnit timeUnit, final long gap, OnCountDownTickListener countDownTickListener) {
        final long millisInFuture = timeUnit.toMillis(time);
        long interval = TimeUnit.SECONDS.toMillis(gap);
        CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                long l = TimeUnit.MINUTES.convert(millisUntilFinished, TimeUnit.MILLISECONDS) + 1;
                if (countDownTickListener != null) {
                    countDownTickListener.onTick(l);
                }
            }

            @Override
            public void onFinish() {
                if (countDownTickListener != null) {
                    countDownTickListener.onFinish();
                }
            }
        };

        countDownTimer.start();
        return countDownTimer;
    }

    public static void cancelTimer(CountDownTimer timer) {
        timer.cancel();
    }

}
