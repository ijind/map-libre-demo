package ai.txai.push.listener;

/**
 * @Description PushInitializedListener
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public interface PushInitializedListener {

	/**
	 * 初始化连接
	 */
	public void onInitialized(int code, String message);

}
