### Android注解应用

*在Java中经常会用到注解，通过注解的方式可以实现很多灵活性的东西。很多优秀的框架都支持注解的方式，如Spring的中对Bean的注解，Hibernate中对POJO类的注解，Mybatis中对Mapper的注解，ButterKnife中对View的注解，Dagger中对各个Component的注解， Retrofit对Api的注解。一言以蔽之，使用注解可以让整个代码风格看起来清爽明了。*

#### 一、 传统的代码风格

activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <EditText
        android:id="@+id/username"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="请输入用户名.." />

    <Button
        android:id="@+id/submit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="提交" />
</LinearLayout>
```

很简单的一个界面，只有一个EditText和一个Button，就不过多解释了。

MainActivity.java

```java
package com.puke.annotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText mUsername;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        initListener();
    }

    private void findView() {
        mUsername = (EditText) findViewById(R.id.username);
        mSubmit = (Button) findViewById(R.id.submit);
    }

    private void initListener() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString().trim();
                Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

```

我们这里要做到是用户点击提交的时候，Toast弹出EditText输入的内容，比较简单，略过~

传统的风格是这样的，这样看来也许觉得没什么问题，但是实际的开发当中我们一个页面当中包含的View，以及对应的View的一些事件回调要远远比这个繁琐。我相信一个Activity中要处理十几二十几个View也不算是什么稀奇的事情，那这样会造成什么结果呢。。。

你的属性声明会是这样的

```java
private Button mButton1;
private Button mButton2;
private Button mButton3;
private Button mButton4;
private Button mButton5;
private Button mButton6;
private Button mButton7;
private Button mButton8;
```

你的findView会是这样的

```java
mButton1 = (Button) findViewById(R.id.button1);
mButton2 = (Button) findViewById(R.id.button2);
mButton3 = (Button) findViewById(R.id.button3);
mButton4 = (Button) findViewById(R.id.button4);
mButton5 = (Button) findViewById(R.id.button5);
mButton6 = (Button) findViewById(R.id.button6);
mButton7 = (Button) findViewById(R.id.button7);
mButton8 = (Button) findViewById(R.id.button8);
```

你的事件监听会是这样的

```java
mButton1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton2.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton3.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton4.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton5.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton6.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton7.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mButton8.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
```

例子举得不怎么恰当，但是足以说明随着业务代码的不断扩大，这些看上去的”无脑操作“也会让我们广大coder变得愈加的不耐烦，而且会使得我们的类变得庞大而臃肿。

那么，接下来我们就通过注解的方式来搞一发~

#### 二、 注解的编码风格

我们打算使用注解的方式实现

> 1. xml的配置
> 2. View的注入
> 3. 点击事件的绑定

接下来就是具体实现逻辑

1. 首先我们先定义一个Bind注解

```java
package com.puke.annotationdemo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zijiao
 * @version 16/8/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface Bind {
    int value() default 0;
}

```

2. 然后写Bind注解对应的注解处理器

```java
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
```

好了，到了这一步，我们的注解工作算是结束了，代码相对有点多，但这个是一劳永逸的。

3. 接下来就是对注解的使用了

```java
package com.puke.annotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@Bind(R.layout.activity_main)
public class MainActivity extends Activity {

    @Bind(R.id.username)
    private EditText mUsername;
    @Bind(R.id.submit)
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BindHandler.handleBind(this);
    }

    @Bind(R.id.submit)
    public void submit() {
        String username = mUsername.getText().toString().trim();
        Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT).show();
    }

}
```

Run一下，完美运行~

我们这里可以看到，使用注解之后

> setContentView方法没了
>
> findViewById方法没了
>
> setOnClickListener方法没了
>
> MainActivity整个类减肥了

所有的所有，都让注解处理器一手承包了。而我们要做的是什么，要做的是真正应该由coder做的事情，在对应的地方加上对应的注解配置就ok了。

然后我们可以回过头看一下Bind这个注解，细心的同学可能发现注解声明value()的时候理论上来讲不应该有一个default为0的默认值。原因很简单啊，因为就目前的使用场景来看，无论注入一个layout还是一个id都不会为0，那这里干嘛还要再写一个default 0呢，直接不要default可以限制业务方的使用，强约束业务方一旦使用注解就必须要在注解里面set一个值进来。这里我要说明一下，我们写注解就是为了方便使用，快速开发，既然要懒，我们就懒到家，干脆就让我们的注解处理器能在业务方没有在Bind中注入值的时候也能生效。

就是要实现下面这种效果:

```java
package com.puke.annotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@Bind
public class MainActivity extends Activity {

    @Bind
    private EditText mUsername;
    @Bind
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BindHandler.handleBind(this);
    }

    @Bind
    public void submit() {
        String username = mUsername.getText().toString().trim();
        Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT).show();
    }

}
```

这样一来，只需要几个全裸的注解一顿狂注之后，就完事了。后面这种的实现方式我这里就不写了，大致说一下思路，注解处理器要制定类名—layout，属性名—id，方法名—id，这样一套转换标准出来，然后注解处理器的处理逻辑是先看业务方有没有手动注入，没有手动注入的情况（也就是前面提到的default 0）下，注解处理器再按照这套标准利用反射来取出对应的R类的对应资源值，只要找到对应的资源值，就和手动注入处理结果的完全一样。

#### 三、 一些问题

每当一种事物出现时，只要不是太极端，总会有人拥护，也有人异议。单单站在coder的角度，这种注解的方式给我带来的好处是显而易见的，算是治愈代码密集恐惧症的偏方了。

但是值得深思的是，这里大量使用了反射，在Java中反射的性能问题总是尴尬的不要不要的。虽然jdk每次升级时基本上都在对反射进行优化，但是毕竟是反射，纯理论上讲，它确实没有直接的方法调用高效。

当然针对这个问题，我也有见过这样一种说法，假如我们对所谓”高效“的时间容忍度是1000t(t为一个时间粒度单位)，直接方法调用耗时是1t，反射是50 - 200t。也就是说，反射是耗性能，是不效率，但是这个也只是相对与直接方法调用而言的，而还远远没达到我们对性能指标的容忍值。

我曾经也在高效开发和反射性能消耗之间纠结很久，在这里就不去过多评价，仁者见仁，智者见智了~