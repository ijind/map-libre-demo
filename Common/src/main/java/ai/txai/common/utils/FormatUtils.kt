package ai.txai.common.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Time: 23/03/2022
 * Author Hay
 */
object FormatUtils {

    private const val KILO_METER = "km";
    private const val METER = "m";
    private const val MINUTES = "mins";
    private const val MINUTE = "min";
    private const val SECONDS = "s";

    fun buildDistance(distance: Double?): String {
        if (distance == null || distance <= 0.0) {
            return "1 $METER"
        }

        if (distance < 1) {
            var toInt = (distance * 1000).toInt()
            if (toInt <= 1) {
                return "1 $METER"
            }
            return "$toInt $METER"
        }

        val numberFormat =
            NumberFormat.getNumberInstance(Locale.US)
        val decimalFormat = numberFormat as DecimalFormat
        decimalFormat.applyPattern("#.#")
        val formatDistance = decimalFormat.format(distance)
        return "$formatDistance $KILO_METER"
    }

    fun buildDistanceWithKm(distance: Double?): String {
        if (distance == null || distance <= 0.0) {
            return "0 $KILO_METER"
        }
        val numberFormat =
            NumberFormat.getNumberInstance(Locale.US)
        val decimalFormat = numberFormat as DecimalFormat
        decimalFormat.applyPattern("#.#")
        val alpha = decimalFormat.format(distance)
        return "$alpha $KILO_METER"
    }


    fun buildTimeGap(gap: Int?): String {
        var time = buildTimeGapNoUnit(gap)
        if (time == "1") {
            return "$time $MINUTE"
        }
        return "$time $MINUTES"
    }

    fun buildTimeGapNoUnit(gap: Int?): String {
        if (gap == null || gap <= 0.0) {
            return "1"
        }
        var min = gap / 60
        if (min <= 0) {
            return "1"
        }

        if (gap % 60 != 0) {
            min++
        }
        return "$min"
    }

    fun hour2Double(dateString: String): Double {
        var result = 0.0
        val hourNoon = dateString.split(" ")
        if ("PM" == hourNoon[1].uppercase(Locale.getDefault())) {
            result = 12.0
        }
        val hour = hourNoon[0].split(":")
        result += if (hour[0].toInt() == 12) 0.0 else hour[0].toDouble()
        result += hour[1].toDouble() / 100
        return result
    }

    fun compareHour(hour1: String, hour2: String): Int {
        return hour2Double(hour1).compareTo(hour2Double(hour2))
    }

    fun replaceRangeToStar(endIndex: Int, source: String): String {
        var startIndex = endIndex - 3
        if (startIndex < 0) startIndex = 0
        if (endIndex - startIndex < 0) return source

        var range = endIndex - startIndex
        var string = ""
        while (range >= 0) {
            string += "*"
            range --
        }
        return source.replaceRange(startIndex..endIndex, string)
    }
}