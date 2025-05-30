package com.yoake.r2base.utils

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object R2TimeUtils {
    @SuppressLint("ConstantLocale")
    private val DEFAULT_FORMAT: DateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun parse(text: String?, format: DateFormat = DEFAULT_FORMAT): Date? {
        if (text == null) return null
        return try {
            format.parse(text)
        } catch (e: ParseException) {
            null
        }

    }

    fun parse(timeMillis: Long, format: DateFormat = DEFAULT_FORMAT): String {
        return format.format(timeMillis)
    }
}