package com.example.android.myexpensemanager;

import android.os.HandlerThread;

public class MyWorkerThread extends HandlerThread {
    MyWorkerThread(String name) {
        super(name);
    }
}
