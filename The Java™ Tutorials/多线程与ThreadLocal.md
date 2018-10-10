# 多线程与ThreadLocal

变量的Global与Local

- Global 意思是在当前线程中，任何一个点都可以访问到ThreadLocal的值。
- Local 意思是该线程的ThreadLocal只能被该线程访问，一般情况下其他线程访问不到。

一个常见的ThreadLocal声明
```
public final static ThreadLocal<String> RESOURCE = new ThreadLocal<String>();

RESOURCE.set("message");

RESOURCE.get();
```


http://www.cnblogs.com/dolphin0520/p/3920407.html


> public T get() { }
> public void set(T value) { }
> public void remove() { }
> protected T initialValue() { }


