package ai.txai.payment.utils;

import android.util.Base64;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.EncryptUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import ai.txai.common.log.LOG;
import ai.txai.database.user.User;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Time: 02/06/2022
 * Author Hay
 */
public class BankUtils {
    public static final String BANK_CARD = "Bank Card";
    public static final String MASTERCARD = "MASTERCARD";
    public static final String VISA = "VISA";
    public static final String PAY_BY = "PayBy";


    public static final Map<String, String> cardNoFilter = new HashMap<>();

    static {
        cardNoFilter.put(MASTERCARD, "4-4-4-4-3");
        cardNoFilter.put(VISA, "4-4-4-4-3");
    }

    /**
     * Checks if the card is valid
     *
     * @param card
     *           {@link String} card number
     * @return result {@link boolean} true of false
     */
    public static boolean luhnCheck(String card) {
        if (card == null)
            return false;
        char checkDigit = card.charAt(card.length() - 1);
        String digit = calculateCheckDigit(card.substring(0, card.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    /**
     * Calculates the last digits for the card number received as parameter
     *
     * @param card
     *           {@link String} number
     * @return {@link String} the check digit
     */
    public static String calculateCheckDigit(String card) {
        if (card == null)
            return null;
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2)	{
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

    private static boolean luhnVerify(String cardNo) {
        return luhnCheck(cardNo);
    }

    public static boolean isMaster(String cardNo) {
        if (cardNo != null && cardNo.length() == 16) {
            int iin = Integer.parseInt(cardNo.substring(0, 2));
            return iin >= 51 && iin <= 55 && luhnVerify(cardNo);
        }
        return false;
    }

    public static boolean isVisa(String cardNo) {
        return cardNo != null && cardNo.length() == 16 && cardNo.startsWith("4") && luhnVerify(cardNo);
    }


    public static boolean mayVisa(String cardNo) {
        if (cardNo == null) {
            return false;
        }
        return cardNo.startsWith("4");
    }

    public static boolean mayMaster(String cardNo) {
        if (cardNo == null || cardNo.length() < 2) {
            return false;
        }
        int iin = Integer.parseInt(cardNo.substring(0, 2));
        return iin >= 51 && iin <= 55;
    }

    public static int bankLogoRes(String sign) {
        if (isPayby(sign)) {
            return R.mipmap.payment_ic_payby_logo;
        }
        if (MASTERCARD.equals(sign)) {
            return R.mipmap.payment_ic_master_logo;
        }
        if (VISA.equals(sign)) {
            return R.mipmap.payment_ic_visa_logo;
        }
        if (isVisa(sign)) {
            return R.mipmap.payment_ic_visa_logo;
        }
        if (isMaster(sign)) {
            return R.mipmap.payment_ic_master_logo;
        }
        return R.mipmap.payment_ic_card_set;
    }

    public static boolean isBankCard(String method) {

        return BANK_CARD.equals(method);
    }

    public static boolean isPayby(String method) {
        return PAY_BY.equals(method);
    }


    public static String getRealCardNumber(String cardNo) {
        return cardNo.replace(" ", "");
    }

    public static String formatCardNumber(String branch, String cardNo) {
        String filter = cardNoFilter.get(branch.trim());
        if (filter == null) {
            return cardNo;
        }
        String[] list = filter.split("-");
        if (list.length <= 1) {
            return cardNo;
        }
        String replace = cardNo.replace(" ", "");
        int totalLength = 0;
        for (int i = 0; i < list.length; i++) {
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

    /**
     * MM/YY
     *
     * @param date
     * @return
     */
    public static String formatExpiry(String date) {
        String replace = getRealExpireDate(date);
        if (replace.length() > 2) {
            return replace.substring(0, 2) + "/" + replace.substring(2);
        }
        return replace;
    }

    public static String getRealExpireDate(String date) {
        return date.replace("/", "").trim();
    }

    public static byte[] getAESKey(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] authKey = new byte[16];
        buf.skipBytes(32);
        buf.readBytes(authKey);
        buf.release();
        return authKey;
    }

    public static byte[] getAESIv(byte[] appKey) {
        ByteBuf buf = Unpooled.wrappedBuffer(appKey);
        byte[] authKey = new byte[16];
        buf.skipBytes(48);
        buf.readBytes(authKey);
        buf.release();
        return authKey;
    }

    public static String encryptAES(String data) {
        User user = CommonData.getInstance().currentUser();
        byte[] appKey = user.getPushToken().getBytes(StandardCharsets.UTF_8);
        byte[] bytes = EncryptUtils.encryptAES(data.getBytes(StandardCharsets.UTF_8), getAESKey(appKey), "AES/CBC/PKCS5Padding", getAESIv(appKey));
        return EncodeUtils.base64Encode2String(bytes);
    }

    public static String decryptAES(String data) {
        User user = CommonData.getInstance().currentUser();
        byte[] appKey = user.getPushToken().getBytes(StandardCharsets.UTF_8);
        byte[] bytes = EncryptUtils.decryptAES(Base64.decode(data, 0), getAESKey(appKey), "AES/CBC/PKCS5Padding", getAESIv(appKey));
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
