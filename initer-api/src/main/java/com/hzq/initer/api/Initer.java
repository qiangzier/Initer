package com.hzq.initer.api;

import android.content.Context;
import android.util.Log;

import com.hzq.initer.api.utils.Consts;

/**
 * 初始化组件
 * Created by hezhiqiang on 2018/12/3.
 */

public class Initer {
    private static boolean debuggable;
    private static boolean hasInit = false;

    public static void init(Context context, boolean isDebug) {
        debuggable = isDebug;
        if(!hasInit) {
            InitCenter.init(context, isDebug);
            hasInit = true;
        } else {
            Log.i(Consts.TAG,"Initer already init...");
        }
    }

    public static boolean debuggable(){
        return debuggable;
    }
}
