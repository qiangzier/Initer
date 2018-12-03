package com.hzq.initer;

import android.content.Context;
import android.util.Log;

import com.hzq.initer.annotation.Inite;
import com.hzq.initer.api.IComponentInit;

/**
 * Created by hezhiqiang on 2018/12/3.
 */

@Inite
public class TestIniter1 implements IComponentInit {

    @Override
    public void init(Context context, Boolean isDebug) {
        Log.d("xxxTestIniter1","TestIniter1#init is be call");
    }
}
