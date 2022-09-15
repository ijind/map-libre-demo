package ai.txai.common.value;

/**
 * Time: 27/06/2022
 * Author Hay
 */
public class Result<T> {
    private boolean complete;
    private T data;
    private String msg;

    public synchronized void setData(T t) {
        this.data = t;
        if (t != null) {
            this.msg = null;
        }
        complete = true;
    }

    public T getData() {
        return data;
    }

    public synchronized void setMsg(String msg) {
        this.msg = msg;
        complete = true;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isComplete() {
        return complete;
    }
}
