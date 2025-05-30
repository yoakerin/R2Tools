package com.yoake.r2base


abstract class OnceReadValue<P, T> {
    @Volatile
    private var isRead = false
    private var cacheValue: T? = null

    fun get(param: P): T? {
        if (isRead) {
            return cacheValue
        }
        synchronized(this) {
            if (!isRead) {
                cacheValue = read(param)
                isRead = true
            }
        }
        return cacheValue
    }

    protected abstract fun read(param: P): T
}
