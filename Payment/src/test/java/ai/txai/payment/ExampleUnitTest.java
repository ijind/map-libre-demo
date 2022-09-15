package ai.txai.payment;

import org.junit.Test;

import static org.junit.Assert.*;

import ai.txai.payment.utils.BankUtils;

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
    public void testAES() {
//        String data = "ULV0K07w0/eezKvb7L2UTw\u003d\u003d\n";

        boolean s = BankUtils.isMaster("5123457293579234");

        System.out.println(s);
    }
}