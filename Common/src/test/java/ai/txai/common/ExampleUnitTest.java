package ai.txai.common;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.FormatUtils;
import ai.txai.common.utils.PhoneNumberUtils;

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
    public void test() {
        String s = AndroidUtils.INSTANCE.buildAmount(10.0);
        String s1 = AndroidUtils.INSTANCE.buildAmount(10.1);
        String s2 = AndroidUtils.INSTANCE.buildAmount(10.11);
        String s3 = AndroidUtils.INSTANCE.buildAmount(10.1111);
        String s4 = AndroidUtils.INSTANCE.buildAmount(0.0);
        System.out.println(s +" "+s1+" "+s2+" "+s3+" "+s4);
    }

    @Test
    public void testHourString() {
        double v = FormatUtils.INSTANCE.hour2Double("08:30 AM");
        double v1 = FormatUtils.INSTANCE.hour2Double("09:30 PM");
        System.out.println("testHourString "+ v +" "+v1);
    }

    @Test
    public void testPhoneUtils() {
        String s = PhoneNumberUtils.formatPhoneNumber("971", "58 560 00 00");
        Assert.assertEquals("58 560 0000", s);

        String s1 = PhoneNumberUtils.formatPhoneNumber("971", "58560000");
        Assert.assertEquals("58 560 000", s1);

        String s2 = PhoneNumberUtils.formatPhoneNumber("971", "5856000003");
        Assert.assertEquals("58 560 0000 3", s2);

    }
}