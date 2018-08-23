MVVM框架：Vue.js （版本3.0.0rc）

状态管理：Vuex

前端路由：Vue Router

服务端通讯：axios、jsonp

移动端滚动库：better-scroll

构建工具：webpack 2.0

源码：es6

============GOAL 1 BEGIN==============

# 源码分析

- 常用设计模式
- spring 5
- MyBatis

---
Proxy代理模式</br>

> - 静态代理
> - 动态代理
> - cglib
> - AspectJ（使用 AspectJ 的编译时增强进行）
> - Spring AOP（使用 Spring AOP）

参考链接：
<url>https://www.ibm.com/developerworks/cn/java/j-lo-springaopcglib/index.html</url>

## cglib



Factory工厂模式</br>

<p></p>

Singleton单例模式</br>

<p></p>

Delegate委派模式</br>

<p></p>

Strategy策略模式</br>
Prototype原型模式</br>
Template模板模式</br>






============GOAL 1 END=================


============GOAL 1 BERGIN==============

d(目标金额) = 0元硬币+1元硬币+3元硬币+5元硬币;

d(0) = 0;凑齐0元需要：0元硬币0个

d(1) = d(1-1) + 1 = d(0) + 1 = 0 + 1 = 1;凑齐1元需要：0元硬币0个，1元硬币1个
d(2) = d(2-1) + 1 = d(1) + 1 = 1 + 1 = 2;凑齐2元需要：
d(3) = d(3-3) + 1 = d(0) + 1 = 0 + 1 = 1;
d(4) = d(4-3) + 1 = d(1) + 1 = 1 + 1 = 1;
d(5) = d(5-3) + 1 = d(2) + 1 = 2 + 1 = 3;

状态：对于i的值，d(i)的值。例如，d(1),d(11);

状态转移方程：d(i) = min{d(i-Vj) + 1}





============GOAL 1 END=================
