package com.hzq.initer;

import android.content.Context;
import android.util.Log;

import com.hzq.initer.annotation.Inite;

/**
 * Created by hezhiqiang on 2018/12/3.
 */

@Inite
public class TestIniter2 {

    public static void init(Context context, Boolean isDebug) {
        Log.d("xxxTestIniter2","TestIniter2#init is be call");
    }
}
