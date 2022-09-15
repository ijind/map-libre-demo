package ai.txai.imageselector.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ai.txai.imageselector.R;

public class DateUtils {

    public static String getImageTime(Context context,long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Calendar imageTime = Calendar.getInstance();
        imageTime.setTimeInMillis(time);
        if (sameDay(calendar, imageTime)) {
            return context.getString(R.string.image_selector_this_today);
        } else if (sameWeek(calendar, imageTime)) {
            return  context.getString(R.string.image_selector_this_week);
        } else if (sameMonth(calendar, imageTime)) {
            return  context.getString(R.string.image_selector_this_month);
        } else {
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
            return sdf.format(date);
        }
    }

    public static boolean sameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean sameWeek(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean sameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    public static String buildDate(Long dateTime) {
        final SimpleDateFormat dateFormat = newSimpleDateFormat();
        return dateFormat.format(new Date(dateTime));
    }

    private static SimpleDateFormat newSimpleDateFormat() {
        final String pattern = "hh:mm a, dd MMM, yyyy";
        final SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        sDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+4:00"));
        return sDateFormat;
    }
}
