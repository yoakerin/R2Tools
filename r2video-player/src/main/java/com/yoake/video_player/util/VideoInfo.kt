package com.yoake.video_player.util

data class VideoInfo(
    val title: String?,
    val poster: String?,
    val lines: MutableList<Line> = ArrayList()
) {
    data class Line(val name: String?, val url: String?)


    fun addLine(name: String?, url: String?): VideoInfo {
        lines.add(Line(name, url))
        return this
    }
}
