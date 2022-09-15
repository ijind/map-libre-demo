package ai.txai.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Time: 01/06/2022
 * Author Hay
 */
public class PhoneNumberUtils {
    public static final Map<String, String> phoneFilter = new HashMap<>();

    static {
        phoneFilter.put("971", "2-3-4-11");
        phoneFilter.put("86", "3-4-4-13");
    }

    public static String getRealNumber(String phone) {
        return phone.replace(" ", "");
    }

    public static int maxLengthPhoneNumber(String isoCode) {
        String filter = phoneFilter.get(isoCode.trim());
        if (filter != null) {
            String[] list = filter.split("-");
            return Integer.parseInt(list[list.length - 1]);
        }
        return 11;
    }

    public static String formatPhoneNumber(String isoCode, String phone) {
        String filter = phoneFilter.get(isoCode.trim());
        if (filter == null) {
            return phone;
        }
        String[] list = filter.split("-");
        if (list.length <= 1) {
            return phone;
        }
        String replace = phone.replace(" ", "");
        int totalLength = 0;
        for (int i = 0; i < list.length - 1; i++) {
            String s = list[i];
            int itemLen = Integer.parseInt(s);
            totalLength += itemLen;
            if (replace.length() > totalLength) {
                replace = replace.substring(0, totalLength) + " " + replace.substring(totalLength);
            } else {
                break;
            }
            totalLength += 1;
        }
        return replace;
    }

}
