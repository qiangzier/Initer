# Initer
组件初始化框架

### 项目背景

先来说说开发此项目的背景，由于公司的项目采用组件化开发，各个组件内部需要在application中初始化一些特有的东西。通常的做法是在主项目中直接调用组件中的代码去完成初始化工作，这么做是完全OK的；but现在有这么一个设想，我能不能在编译主项目时不去编译某些组件，这样做能极大的提高了主项目的编译速度。下面我们来按照这个思路走一遍试试，首先要去掉某个组件就直接干掉该组件在项目中的依赖就好了。当我们干掉依赖时发现初始化该组件的地方报错了，原因是找不到类文件，仔细想一下就会发现我们这里调用的是组件的类文件，现在组件都被干掉了，当然找不到这个类了。那该怎么办呢，我们直接注释了吧，然后去跑项目完全ok。然后想要编译该组件时再去手动打开这个注释，好累啊。。。那么有没有别的方案呢，我们的需求就想要去掉每个组件就直接去掉依赖就好了，为什么还要去改动代码呢？？？？
答案是有办法的，我们在初始化的时候其实只需要主项目的Application和当前编译环境（debug/release），那我们就想办法把这个代码自动生成，当我们依赖某个组件时自动去执行初始化的代码，不依赖时自然就不会去执行。就这样Initer就应用而生了。

### 使用方法

1、项目配置

在需要自动初始化的组件的build.gradle文件中添加以下依赖：

```
android {
    //......
    defaultConfig {
        //......
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [moduleName: project.getName()]
            }
        }
    }
    //......
}

dependencies {
   //......
    implementation 'com.hzq.android:initer-api:1.0.1'
    annotationProcessor 'com.hzq.android:initer-compiler:1.0.0'
}

```

2、创建初始化组件工具类，并添加@Inite注解。注意这里是静态调用，所以init方法必须是static的

```
@Inite
public class TestIniter1 {

    public static void init(Context context, Boolean isDebug) {
        Log.d("xxxTestIniter1","TestIniter1#init is be call");
    }
}
```

3、在Application#onCreate中添加如下代码

```
Initer.init(this,BuildConfig.DEBUG);
```
