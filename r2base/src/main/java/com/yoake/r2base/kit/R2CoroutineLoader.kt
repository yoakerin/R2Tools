package com.yoake.r2base.kit


import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.yoake.r2base.R2Log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext


internal class CoroutineLifecycleListener(private val deferred: Deferred<*>) :
    DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        if (!deferred.isCancelled) {
            deferred.cancel()
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
internal val backgroundContext =
    newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors() * 2, "Loader")


fun <T> LifecycleOwner.load(loader: suspend () -> T): Deferred<T> {
    val deferred = lifecycleScope.async(context = backgroundContext, start = CoroutineStart.LAZY) {
        loader()
    }
    lifecycle.addObserver(CoroutineLifecycleListener(deferred))
    return deferred
}

infix fun <T> Deferred<T>.then(block: suspend (T) -> Unit): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        try {
            block(this@then.await())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun <T> Context.load2(loader: suspend () -> T): Deferred<T> {

    return if (this is LifecycleOwner) {
        val deferred =
            lifecycleScope.async(context = backgroundContext, start = CoroutineStart.LAZY) {
                loader()
            }
        lifecycle.addObserver(CoroutineLifecycleListener(deferred))
        deferred
    } else {

        R2Log.w("","Context.load2{}then{} 请使用Activity Context")
        CoroutineScope(backgroundContext).async(start = CoroutineStart.LAZY) {
            loader()
        }
    }
}

