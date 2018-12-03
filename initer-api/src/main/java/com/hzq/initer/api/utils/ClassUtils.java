package com.hzq.initer.api.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.hzq.initer.api.Initer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by hezhiqiang on 2018/11/22.
 */

public class ClassUtils {
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    //没什么卵用，现在基本上都是14以上
    private static SharedPreferences getMultiDexPreferences (Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    /**
     * 通过指定包名，扫描包下面包含的所有的ClassName
     *
     * @param context     U know
     * @param packageName 包名
     * @return 所有class的集合
     */
    public static Set<String> getFileNameByPackageName(Context context, final String packageName) throws PackageManager.NameNotFoundException,IOException,InterruptedException {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());

        for (final String path : paths) {
            Executors.newCachedThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexfile = null;

                    try {
                        if (path.endsWith(EXTRACTED_SUFFIX)) {
                            //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                            dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                        } else {
                            dexfile = new DexFile(path);
                        }

                        Enumeration<String> dexEntries = dexfile.entries();
                        while (dexEntries.hasMoreElements()) {
                            String className = dexEntries.nextElement();
                            if (className.startsWith(packageName)) {
                                classNames.add(className);
                            }
                        }
                    } catch (Throwable ignore) {
                        Log.e("Router", "Scan map file in dex files made error.", ignore);
                    } finally {
                        if (null != dexfile) {
                            try {
                                dexfile.close();
                            } catch (Throwable ignore) {
                            }
                        }

                        parserCtl.countDown();
                    }
                }
            });
        }

        parserCtl.await();

        Log.d(Consts.TAG, "Filter " + classNames.size() + " classes by packageName <" + packageName + ">");
        return classNames;
    }

    /**
     *
     * @param context
     * @return
     */
    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); //add the default apk path

        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;

        //如果WM已经支持MultiDex，就不要气Secondary Folder 加载Classesx.zip了哪里已经没了
        //通过是否存在sp中的multidex.version是不准确的，因为从低版本升级的用户是包含这个sp配置的
        if (!isWMMultidexCapable()) {
            //the total dex numbers
            int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                //for each dex file, ie: test.classes2.zip, test.classes3.zip...
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    sourcePaths.add(extractedFile.getAbsolutePath());
                    //we ignore the verify zip part
                } else {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        if (Initer.debuggable()) { // Search instant run support only debuggable
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }
        return sourcePaths;
    }

    /**
     * Get instant run dex path, used to catch the branch usingApkSplits=false.
     */
    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            // add the split apk, normally for InstantRun, and newest version.
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            Log.d(Consts.TAG, "Found InstantRun support");
        } else {
            try {
                // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                    Log.d(Consts.TAG, "Found InstantRun support");
                }

            } catch (Exception e) {
                Log.e(Consts.TAG, "InstantRun support error, " + e.getMessage());
            }
        }

        return instantRunSourcePaths;
    }


    private static boolean isWMMultidexCapable() {
        boolean isMultidexCapable = false;

        String vmName = "'Android'";
        try {
            String versionString = System.getProperty("java.vm.version");
            if (versionString != null) {
                Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                if (matcher.matches()) {
                    try {
                        int major = Integer.parseInt(matcher.group(1));
                        int minor = Integer.parseInt(matcher.group(2));
                        isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                                || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                    } catch (NumberFormatException ignore) {
                        // let isMultidexCapable be false
                    }
                }
            }
        } catch (Exception ignore) {

        }

        Log.i(Consts.TAG, "VM with name " + vmName + (isMultidexCapable ? " has multidex support" : " does not have multidex support"));
        return isMultidexCapable;

    }

}
