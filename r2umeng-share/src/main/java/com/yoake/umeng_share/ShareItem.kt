package com.yoake.umeng_share

import androidx.annotation.DrawableRes
import androidx.annotation.IntDef

class ShareItem(val title: String, @DrawableRes val icon: Int, @TYPE val type: Int) {

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @IntDef(
        TYPE_QQ,
        TYPE_QQ_ZONE,
        TYPE_WECHAT,
        TYPE_WECHAT_CIRCLE,
        TYPE_SINA_WEIBO,
        TYPE_COPY_LINK,
        TYPE_SHARE_POSTER,
        TYPE_DOWN_POSTER,
        TYPE_BOOKMARK,
        TYPE_CUSTOM_1,
        TYPE_CUSTOM_2,
        TYPE_CUSTOM_3,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class TYPE
    companion object {
        //QQ
        const val TYPE_QQ = 1

        //QQ空间
        const val TYPE_QQ_ZONE = 2

        //微信
        const val TYPE_WECHAT = 3

        //微信朋友圈
        const val TYPE_WECHAT_CIRCLE = 4

        //新浪微博
        const val TYPE_SINA_WEIBO = 5

        //复制链接
        const val TYPE_COPY_LINK = 6

        //分享海报
        const val TYPE_SHARE_POSTER = 7

        //下载海报
        const val TYPE_DOWN_POSTER = 8

        //收藏
        const val TYPE_BOOKMARK = 9

        //自定义字端1
        const val TYPE_CUSTOM_1 = 11

        //自定义字端2
        const val TYPE_CUSTOM_2 = 12

        //自定义字端3
        const val TYPE_CUSTOM_3 = 13
    }

    override fun equals(other: Any?): Boolean {
        if (other is ShareItem) {
            return type == other.type
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}