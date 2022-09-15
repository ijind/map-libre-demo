package ai.txai.common.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import ai.txai.common.router.ARouterUtils;

/**
 * Time: 2020/8/19
 * Author Hay
 */
public class DispatcherActivity extends Activity {
    public static final int REQUEST_CODE = 1001;
    public static final String EXTRA_PATH = "extra.path";
    public static final String EXTRA_CONTENT = "extra.content";

    public interface ICallback {
        void onSuccess(String content);
        void onFailed();
    }

    private static ICallback sCallback;

    public static void startActivity(String path, Bundle args, Activity activity, ICallback callback) {
        sCallback = callback;
        Intent intent = new Intent(activity, DispatcherActivity.class);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtras(args);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            if (sCallback != null) {
                sCallback.onFailed();
                sCallback = null;
                finish();
            }
            return;
        }
        String path = intent.getStringExtra(EXTRA_PATH);
        Bundle args = intent.getExtras();
        ARouterUtils.navigationWithBundleForResult(this, path, args, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Activity.RESULT_OK == resultCode) {
            String content = data.getStringExtra(EXTRA_CONTENT);
            if (!TextUtils.isEmpty(content)) {
                if (sCallback != null) {
                    sCallback.onSuccess(content);
                    sCallback = null;
                }
                finish();
                return;
            }
            finish();
            return;
        }
        if (sCallback != null) {
            sCallback.onFailed();
            sCallback = null;
        }
        finish();
    }
}
