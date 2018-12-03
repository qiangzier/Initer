package com.hzq.initer;

import android.app.Application;

import com.hzq.initer.api.Initer;

/**
 * Created by hezhiqiang on 2018/12/3.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Initer.init(this,BuildConfig.DEBUG);
    }
}
