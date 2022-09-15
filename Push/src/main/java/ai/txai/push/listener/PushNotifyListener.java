package ai.txai.push.listener;

import ai.txai.push.payload.PushNotifyPayload;

/**
 * @Description PushNotifyListener
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public interface PushNotifyListener {

	/**
	 *  监听通知
	 */
	public void onReceived(PushNotifyPayload pushNotifyPayload);
}
