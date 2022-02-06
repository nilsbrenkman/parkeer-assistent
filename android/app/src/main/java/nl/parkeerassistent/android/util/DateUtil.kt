package nl.parkeerassistent.android.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtil {

    val amsterdam = TimeZone.getTimeZone("Europe/Amsterdam")

    enum class DateFormat(val pattern: String) {
        DateTime ("yyyy-MM-dd'T'HH:mm:ssX"),
        DateFull ("yyyy-MM-dd"),
        DayMonth ("d MMM"),
        Time     ("HH:mm"),
        YearMonth("yyyy-MM"),
        MonthYear("MMMM yyyy"),
        DayOfWeek("E"),
        Day      ("d"),
        ;
        fun parse(date: String): Date {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            try {
                return sdf.parse(date) ?: throw InvalidDateException()
            } catch (e: Exception) {
                Log.e("DateUtil", "Invalid date: $date", e)
                throw InvalidDateException()
            }
        }
        fun format(date: Date): String {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            return sdf.format(date)
        }
    }

    fun parse(date: String): Date {
        return DateFormat.DateTime.parse(date)
    }

    fun nextUpdate(): Long {
        val now = Date()
        val calendar = Calendar.getInstance(amsterdam)
        calendar.time = now
        calendar.add(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 100)
        return calendar.time.time - now.time
    }

    fun formatParkingDuration(duration: Long): String {
        if (hoursOf(duration) > 0) {
            return String.format("%d:%02d", hoursOf(duration), minutesInHourOf(duration))
        }
        if (minutesInHourOf(duration) >= 10) {
            return String.format("%dm",  minutesInHourOf(duration))
        }
        if (minutesInHourOf(duration) > 0) {
            return String.format("%d:%02d", minutesInHourOf(duration), secondsInMinuteOf(duration))
        }
        return String.format("0:%02d", secondsInMinuteOf(duration))
    }

    fun hoursOf(duration: Long): Int {
        return TimeUnit.MILLISECONDS.toHours(duration).toInt()
    }

    fun minutesInHourOf(duration: Long): Int {
        return (TimeUnit.MILLISECONDS.toMinutes(duration) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration))).toInt()
    }

    fun secondsInMinuteOf(duration: Long): Int {
        return (TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))).toInt()
    }
}

fun Date.addingMinutes(minutes: Int): Date {
    val calendar = Calendar.getInstance(DateUtil.amsterdam)
    calendar.time = this
    calendar.add(Calendar.MINUTE, minutes)
    return calendar.time
}

fun Calendar.isWeekend(): Boolean {
    val dayOfWeek = this.get(Calendar.DAY_OF_WEEK)
    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
}

fun Calendar.getDateWithHour(hour: Int): Date {
    val calendar = Calendar.getInstance(DateUtil.amsterdam)
    calendar.time = this.time
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}
