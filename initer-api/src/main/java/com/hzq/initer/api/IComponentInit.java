package com.hzq.initer.api;

import android.content.Context;

/**
 * 业务组件初始化模板
 * Created by hezhiqiang on 2018/11/26.
 */

public interface IComponentInit {
    void init(Context context, Boolean isDebug);
}
