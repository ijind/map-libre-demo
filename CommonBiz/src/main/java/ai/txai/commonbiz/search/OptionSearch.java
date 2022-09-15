package ai.txai.commonbiz.search;

import android.os.Handler;
import android.os.Looper;

/**
 * Time: 11/03/2022
 * Author Hay
 */
public class OptionSearch {
    private String currentWord;
    private Handler handler;
    private SearchRunnable runnable;

    private int INTERVAL_TIME = 300;

    public OptionSearch(SearchRunnable r) {
        handler = new Handler(Looper.getMainLooper());
        this.runnable = r;
    }

    public void optionSearch(String keyword) {
        this.currentWord = keyword;
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable.setKeyword(currentWord);
            handler.postDelayed(runnable, INTERVAL_TIME);
        }
    }

    public abstract static class SearchRunnable implements Runnable {
        private String keyword;

        @Override
        public void run() {
            search(keyword);
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public abstract void search(String keyword);
    }


}
