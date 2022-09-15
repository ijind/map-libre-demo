package ai.txai.push;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;

import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.push.payload.notify.OrderStatusNotify;
import ai.txai.push.payload.notify.PaymentStatusNotify;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testGson() {
        String str = " {\"orderId\":\"165526958725380\",\"payMethod\":\"Bank Card\",\"paymentOrderId\":\"165526995227343\",\"paymentStatus\":\"Paid_Success\",\"transactionId\":\"131655269952107544\",\"finishedAt\":1655269990561}";
        PaymentStatusNotify jsonObject = GsonManager.getGson().fromJson(str, PaymentStatusNotify.class);

        System.out.println(GsonManager.GsonString(jsonObject));
    }
}