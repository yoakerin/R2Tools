package com.yoake.umeng_share

import android.content.Context
import android.content.Intent
import android.os.Build
import com.tencent.tauth.Tencent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.umeng.socialize.UMShareAPI


object UmengKit {
    fun preInit(context: Context) {
        UMConfigure.preInit(
            context, context.getString(R.string.umeng_app_key),
            context.getString(R.string.app_channel),
        )
    }

    fun init(context: Context) {

        UMConfigure.init(
            context,
            context.getString(R.string.umeng_app_key),
            context.getString(R.string.app_channel),
            UMConfigure.DEVICE_TYPE_PHONE,
            context.getString(R.string.umeng_push_secret)
        )
        //分享v7.2.0 SDK中的QQ SDK版本是v3.5.12，需要调用授权api才可以正常使用授权和分享功能
        Tencent.setIsPermissionGranted(true)

        setWechatPlatformConfig(
            context,
            context.getString(R.string.wx_app_id),
            context.getString(R.string.wx_app_key),
        )
        setQQPlatformConfig(
            context,
            context.getString(R.string.qq_app_id),
            context.getString(R.string.qq_app_key),
        )
        setSinaWeiboPlatformConfig(
            context,
            context.getString(R.string.sina_app_id),
            context.getString(R.string.sina_app_key),
            context.getString(R.string.sina_redirect_url),
        )
    }


    fun onActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        UMShareAPI.get(context).onActivityResult(requestCode, resultCode, data)
    }

    fun release(context: Context) {
        UMShareAPI.get(context).release();
    }


    private fun setWechatPlatformConfig(context: Context, appId: String, appKey: String) {
        // 微信设置
        PlatformConfig.setWeixin(appId, appKey)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PlatformConfig.setWXFileProvider("${context.packageName}.fileprovider")
        }
    }

    private fun setQQPlatformConfig(context: Context, appId: String, appKey: String) {
        // QQ设置
        PlatformConfig.setQQZone(appId, appKey)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PlatformConfig.setQQFileProvider("${context.packageName}.fileprovider")
        }
    }

    private fun setSinaWeiboPlatformConfig(
        context: Context,
        appId: String,
        appKey: String,
        redirectUrl: String
    ) {
        // 新浪微博设置
        PlatformConfig.setSinaWeibo(appId, appKey, redirectUrl)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PlatformConfig.setSinaFileProvider("${context.packageName}.fileprovider")
        }
    }

}