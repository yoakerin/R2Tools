package com.yoake.r2base.utils

import android.content.Context
import java.io.IOException
import java.nio.charset.StandardCharsets


object R2AssetUtils {

    fun readStringFromAsset(context: Context, fileName: String): String {
        val assetManager = context.assets
        try {
            // 打开文件
            val inputStream = assetManager.open(fileName)
            // 读取文件内容为字符串
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}