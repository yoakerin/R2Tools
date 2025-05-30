package com.yoake.r2base

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle


abstract class AbsActivityLifecycleCallbacks(private val mTargetActivity: Activity) :
    ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity === mTargetActivity) {
            mTargetActivity.application.unregisterActivityLifecycleCallbacks(this)
            onTargetActivityDestroyed()
        }
    }

    protected abstract fun onTargetActivityDestroyed()
}
