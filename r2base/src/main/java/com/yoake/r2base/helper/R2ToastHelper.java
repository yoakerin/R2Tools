package com.yoake.r2base.helper;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class R2ToastHelper {
    private static final String TAG = "R2ToastHelper";

    public static void show(Toast toast) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            fixToastForAndroidN(toast).show();
        } else {
            toast.show();
        }
    }

    private static Toast fixToastForAndroidN(Toast toast) {
        Object tn = R2ReflectHelper.getFieldValue(toast, "mTN");
        if (tn == null) {
            Log.w(TAG, "The value of field mTN of " + toast + " is null");
            return toast;
        }
        Object handler = R2ReflectHelper.getFieldValue(tn, "mHandler");
        if (handler instanceof Handler) {
            if (R2ReflectHelper.setFieldValue(
                    handler, "mCallback", new FixCallback((Handler) handler))) {
                return toast;
            }
        }

        final Object show = R2ReflectHelper.getFieldValue(tn, "mShow");
        if (show instanceof Runnable) {
            if (R2ReflectHelper.setFieldValue(tn, "mShow", new FixRunnable((Runnable) show))) {
                return toast;
            }
        }
        Log.w(TAG, "Neither field mHandler nor mShow of " + tn + " is accessible");
        return toast;
    }

    public static class FixCallback implements Handler.Callback {

        private final Handler mHandler;

        public FixCallback(final Handler handler) {
            mHandler = handler;
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (Throwable e) {
                // ignore
            }
            return true;
        }
    }

    public static class FixRunnable implements Runnable {

        private final Runnable mRunnable;

        public FixRunnable(final Runnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (final RuntimeException e) {
                // ignore
            }
        }
    }
}
