package com.hzq.initer.api;

import android.content.Context;
import android.util.Log;

import com.hzq.initer.api.utils.ClassUtils;
import com.hzq.initer.api.utils.Consts;
import com.hzq.initer.api.utils.PackageUtils;

import java.util.Set;

import static com.hzq.initer.api.utils.Consts.GENERATED_CLASS_START_PRIX;
import static com.hzq.initer.api.utils.Consts.INITCOMPONENTS_SP_KEY_MAP;
import static com.hzq.initer.api.utils.Consts.PACKAGE_OF_GENERATE_FILE;

/**
 * Created by hezhiqiang on 2018/11/26.
 */

class InitCenter {

    private static boolean hasInit = false;

    public synchronized static void init(Context context,boolean isDebug) {
        Set<String> classMap = null;
        if(hasInit) return;
        try {
            if(isDebug || PackageUtils.isNewVersion(context)) {
                classMap = ClassUtils.getFileNameByPackageName(context,PACKAGE_OF_GENERATE_FILE);
                if(!classMap.isEmpty()) {
                    PackageUtils.put(context,INITCOMPONENTS_SP_KEY_MAP,classMap);
                }
                PackageUtils.updateVersion(context);
            } else {
                classMap = PackageUtils.get(context,INITCOMPONENTS_SP_KEY_MAP);
            }

            if(classMap != null && classMap.size() > 0) {
                for (String className : classMap) {
                    if(className.startsWith(PACKAGE_OF_GENERATE_FILE + "." + GENERATED_CLASS_START_PRIX)) {
                        Object o = Class.forName(className).getConstructor().newInstance();
                        if (o instanceof IComponentInit) {
                            ((IComponentInit) o).init(context, isDebug);
                        }
                        Log.i(Consts.TAG, o.toString());
                    }
                }
            }
            hasInit = true;
        } catch (Exception e) {
            Log.e(Consts.TAG,e.getMessage());
            e.printStackTrace();

        }
    }
}
