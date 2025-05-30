package com.yoake.r2base.kit

import android.os.SystemClock
import android.view.View
import com.yoake.r2base.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun throttleClick(wait: Long = 200, block: ((View) -> Unit)): View.OnClickListener {

    return View.OnClickListener { v ->
        val current = SystemClock.uptimeMillis()
        val lastClickTime = (v.getTag(R.id.m_click_timestamp) as? Long) ?: 0
        if (current - lastClickTime > wait) {
            v.setTag(R.id.m_click_timestamp, current)
            block(v)
        }
    }
}

fun debounceClick(wait: Long = 200, block: ((View) -> Unit)): View.OnClickListener {
    return View.OnClickListener { v ->
        var action = (v.getTag(R.id.m_click_debounce_action) as? DebounceAction)
        if (action == null) {
            action = DebounceAction(v, block)
            v.setTag(R.id.m_click_debounce_action, action)
        } else {
            action.block = block
        }
        v.removeCallbacks(action)
        v.postDelayed(action, wait)
    }
}

class DebounceAction(val view: View, var block: ((View) -> Unit)) : Runnable {
    override fun run() {
        if (view.isAttachedToWindow) {
            block(view)
        }
    }
}


/**
 * 在特定时间间隔内，函数最多执行一次
 */
fun View.onClick(wait: Long = 200, block: ((View) -> Unit)) {
    setOnClickListener(throttleClick(wait, block))
}

/**
 * 在特定时间间隔内没有再次触发事件时，函数才会执行
 */
fun View.onDebounceClick(wait: Long = 200, block: ((View) -> Unit)) {
    setOnClickListener(debounceClick(wait, block))
}

/**
 * 防抖动执行命令
 */
fun View.postDebounce(wait: Long = 1000, block: () -> Unit) {
    val job = getTag(R.id.m_debounce_action) as? Job
    // 取消之前的 Job
    job?.cancel()
    // 创建新的 Job 并延迟执行
    val newJob = CoroutineScope(Dispatchers.Main).launch {
        delay(wait)
        block()
    }
    setTag(R.id.m_debounce_action, newJob)
}
