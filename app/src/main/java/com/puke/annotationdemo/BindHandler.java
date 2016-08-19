package com.puke.annotationdemo;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zijiao
 * @version 16/8/18
 */
public class BindHandler {

    /**
     * 处理对Activity的注解
     *
     * @param activity 目标Activity
     */
    public static void handleBind(Activity activity) {
        Class cls = activity.getClass();
        handleSetContentView(activity);
        handleFindView(cls.getDeclaredFields(), activity);
        handleClickEvent(cls.getDeclaredMethods(), activity);
    }

    //绑定xml布局
    private static void handleSetContentView(Activity activity) {
        Class<?> cls = activity.getClass();
        if (cls.isAnnotationPresent(Bind.class)) {
            //Activity中加入Bind注解时取出注解配置
            Bind bind = cls.getAnnotation(Bind.class);
            int layout = bind.value();
            if (layout != 0) {
                activity.setContentView(layout);
            }
        }
    }

    //View的注入
    private static void handleFindView(Field[] declaredFields, Activity activity) {
        if (declaredFields == null || declaredFields.length == 0) {
            return;
        }
        for (Field field : declaredFields) {
            //找到被Bind注解且是View的所有属性
            if (field.isAnnotationPresent(Bind.class) && View.class.isAssignableFrom(field.getType())) {
                Bind bind = field.getAnnotation(Bind.class);
                int id = bind.value();
                if (id != 0) {
                    View view = activity.findViewById(id);
                    field.setAccessible(true);
                    try {
                        //直接通过反射set进去
                        field.set(activity, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //点击事件的绑定
    private static void handleClickEvent(Method[] declaredMethods, final Activity activity) {
        if (declaredMethods == null || declaredMethods.length == 0) {
            return;
        }
        for (final Method method : declaredMethods) {
            //找到被Bind注解且无参的所有方法（注意这里限制无参是为了与下面调用method.invoke(activity)的无参保持一致）
            if (method.isAnnotationPresent(Bind.class) && method.getParameterTypes().length == 0) {
                Bind bind = method.getAnnotation(Bind.class);
                int id = bind.value();
                if (id != 0) {
                    activity.findViewById(id).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                method.invoke(activity);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

}
