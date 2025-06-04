package com.yoake.r2tools

import android.app.Application
import android.util.Log

import com.yoake.r2base.R2Log



class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        R2Log.setDelegate(object : R2Log.LogDelegate {
            override fun e(tag: String?, msg: String?, vararg obj: Any?) {
                Log.e(tag + "_MKit", msg ?: "")
            }

            override fun w(tag: String?, msg: String?, vararg obj: Any?) {
                Log.w(tag + "_MKit", msg ?: "")
            }

            override fun i(tag: String?, msg: String?, vararg obj: Any?) {
                Log.i(tag + "_MKit", msg ?: "")
            }

            override fun d(tag: String?, msg: String?, vararg obj: Any?) {
                Log.d(tag + "_MKit", msg ?: "")
            }

            override fun printErrStackTrace(
                tag: String?,
                tr: Throwable?,
                format: String?,
                vararg obj: Any?
            ) {

            }
        })
    }
}