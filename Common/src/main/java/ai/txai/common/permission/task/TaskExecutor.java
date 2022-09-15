package ai.txai.common.permission.task;

import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public abstract class TaskExecutor<T> extends AsyncTask<Void, Void, T> {

    private static Executor sExecutor = Executors.newSingleThreadExecutor();

//    private Dialog mDialog;

    public TaskExecutor(Context context) {
//        this.mDialog = new WaitDialog(context);
//        this.mDialog.setCancelable(false);
    }

    @Override
    protected final void onPreExecute() {
//        if (!mDialog.isShowing()) {
//            mDialog.show();
//        }
    }

    @Override
    protected final void onPostExecute(T t) {
//        if (mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
        onFinish(t);
    }

    protected abstract void onFinish(T t);

    /**
     * Just call this method.
     */
    public final void execute() {
        executeOnExecutor(sExecutor);
    }
}