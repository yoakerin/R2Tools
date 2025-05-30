package com.yoake.r2base.kit

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 *格式化数据大小
 */
fun Long.formatDataSize(): String {
    val df = DecimalFormat("#.##")
    var unit = "B"
    var len = toFloat()
    if (len > 900) {
        len /= 1024f
        unit = "KB"
    }
    if (len > 900) {
        len /= 1024f
        unit = "MB"
    }
    if (len > 900) {
        len /= 1024f
        unit = "GB"
    }
    if (len > 900) {
        len /= 1024f
        unit = "TB"
    }
    return df.format(len.toDouble()) + unit
}

/**
 *格式化时长s 用于音视频的duration格式化
 */
fun Int.formatDuration(): String {
    if (this == 0) return "00:00"
    return (if (this % 3600 / 60 < 10) 0.toString() + "" + this % 3600 / 60 else this % 3600 / 60).toString() + ":" + if (this % 60 < 10) 0.toString() + "" + this % 60 else this % 60
}

/**
 *格式化时长ms 用于音视频的duration格式化
 */
fun Long.formatDuration(): String {
    if (this == 0L) return "00:00"
    val _this = this / 1000
    return (if (_this % 3600 / 60 < 10) 0.toString() + "" + _this % 3600 / 60 else _this % 3600 / 60).toString() + ":" + if (_this % 60 < 10) 0.toString() + "" + _this % 60 else _this % 60
}


/**
 * 保留小数点后 newScale 位
 */
fun Double.formatPoint(newScale: Int): Double {
    val bg = BigDecimal(this).setScale(newScale, RoundingMode.UP)
    return bg.toDouble()
}

/**
 * 格式化日期转时间戳
 */
fun String.dateToTimeStamp(pattern: String): Long {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)!!.time
    } catch (e: ParseException) {
        -1L
    }
}


