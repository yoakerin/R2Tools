package com.yoake.r2base.obj

import android.app.Service
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import java.util.concurrent.atomic.AtomicInteger

/**
 * 开启协程处理完成任务后自动结束的服务
 * 多用于后台更新数据等
 */
abstract class R2TaskService : Service() {
    // 任务列表
    private val serviceJobList = mutableListOf<Job>()

    // 用于跟踪正在执行的任务数
    private val taskCount = AtomicInteger(0)

    override fun onBind(p0: Intent?) = null

    /**
     * 在一个使用 I/O 调度器的新协程中执行指定的代码块，
     * 并将这个协程与 serviceJob 关联起来，以便在需要时进行管理、取消或等待
     */
    fun scope(block: suspend CoroutineScope.() -> Unit) {
        val serviceJob = SupervisorJob()
        // 增加任务数
        taskCount.incrementAndGet()
        // 添加任务到列表
        serviceJobList.add(serviceJob)
        CoroutineScope(Dispatchers.IO + serviceJob).launch {
            try {
                block.invoke(this)
            } catch (e: Exception) {
                // 处理异常
                e.printStackTrace()
            } finally {
                // 完成任务后，记住减少任务数
                taskCount.decrementAndGet()
                // 移除已完成的任务
                serviceJobList.remove(serviceJob)
                // 如果任务数为0，停止 Service
                if (taskCount.get() == 0) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (job in serviceJobList) {
            job.cancel()
        }
    }
}