package com.yoake.umeng_share

/**
 * 分享的数据类
 */
interface ShareInfo {
    fun shareTitle(): String?
    fun shareThumb(): String?
    fun shareUrl(): String?
    fun shareDescription(): String?
}