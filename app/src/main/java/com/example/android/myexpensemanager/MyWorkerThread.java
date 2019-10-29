package com.example.android.myexpensemanager;

import android.os.HandlerThread;
import android.os.Looper;

public class MyWorkerThread extends HandlerThread {
    static private MyWorkerThread workerThread = null;
    private MyWorkerThread(String name) {
        super(name);
    }

    public static Looper getWorkerThreadLooper() {
        if (workerThread == null) {
            workerThread = new MyWorkerThread("dbOperationHandler");
            workerThread.start();
        }
        return workerThread.getLooper();
    }

    public static void quitWorker() {
        if (workerThread != null) {
            workerThread.quit();
        }
    }
}
