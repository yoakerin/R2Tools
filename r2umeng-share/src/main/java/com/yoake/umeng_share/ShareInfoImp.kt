package com.yoake.umeng_share


class ShareInfoImp(
    private val title: String?,
    private val thumb: String?,
    private val url: String?,
    private val description: String?
) : ShareInfo {
    override fun shareTitle() = title
    override fun shareThumb() = thumb
    override fun shareUrl() = url
    override fun shareDescription() = description
}