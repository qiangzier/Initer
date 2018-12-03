package com.hzq.initer.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

import static com.hzq.initer.api.utils.Consts.INITCOMPONENTS_SP_CHCHE_KEY;
import static com.hzq.initer.api.utils.Consts.LAST_VERSION_CODE;
import static com.hzq.initer.api.utils.Consts.LAST_VERSION_NAME;

/**
 * Created by hezhiqiang on 2018/11/20.
 */

public class PackageUtils {
    private static String NEW_VERSION_NAME;
    private static int NEW_VERSION_CODE;

    public static boolean isNewVersion(Context context) {
        PackageInfo info = getPackageInfo(context);
        if(null != info) {
            String versionName = info.versionName;
            int versionCode = info.versionCode;

            SharedPreferences sp = context.getSharedPreferences(INITCOMPONENTS_SP_CHCHE_KEY, Context.MODE_PRIVATE);
            if (!versionName.equals(sp.getString(LAST_VERSION_NAME, null)) || versionCode != sp.getInt(LAST_VERSION_CODE, -1)) {
                // new version
                NEW_VERSION_NAME = versionName;
                NEW_VERSION_CODE = versionCode;

                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void updateVersion(Context context) {
        if(!TextUtils.isEmpty(NEW_VERSION_NAME) && NEW_VERSION_CODE != 0) {
            SharedPreferences sp = context.getSharedPreferences(INITCOMPONENTS_SP_CHCHE_KEY, Context.MODE_PRIVATE);
            sp.edit().putString(LAST_VERSION_NAME,NEW_VERSION_NAME).putInt(LAST_VERSION_CODE,NEW_VERSION_CODE).apply();
        }
    }

    public static void put(Context context, String key, Set<String> data) {
        context.getSharedPreferences(INITCOMPONENTS_SP_CHCHE_KEY, Context.MODE_PRIVATE).edit().putStringSet(key,data).apply();
    }

    public static Set<String> get(Context context, String key) {
        return new HashSet<>(context.getSharedPreferences(INITCOMPONENTS_SP_CHCHE_KEY, Context.MODE_PRIVATE).getStringSet(key,new HashSet<String>()));
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageInfo;
    }
}
