# Java教程

https://docs.oracle.com/javase/tutorial/tutorialLearningPaths.html

# 基础

---

## 面向对象的编程原则

对象
消息
类
继承

## 语法

变量
数组
数据类型
操作符
流程控制

## 类与对象

使用类来创建对象

## 注解

## 数字类型与字符串类型

## 范型

## package

---

# 进阶

## 异常处理

## I/O

## 并发

## 正则表达式

---

# 高级

## [集合](https://docs.oracle.com/javase/tutorial/collections/index.html)

## [简介](https://docs.oracle.com/javase/tutorial/collections/intro/index.html)

    一个集合框架包含了三个要素，Interface、Implementations、Algorithm。

## Interface

[Collection](https://docs.oracle.com/javase/tutorial/collections/interfaces/collection.html)

[Set](https://docs.oracle.com/javase/tutorial/collections/interfaces/set.html)

[List](https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html)

#### <b>集合操作</b>
#### <b>迭代器</b>
#### <b>Range-view操作</b>
#### <b>List Algorithms</b>
Most polymorphic algorithms in the Collections class apply specifically to List. Having all these algorithms at your disposal makes it very easy to manipulate lists. Here's a summary of these algorithms, which are described in more detail in the Algorithms section.

- sort — sorts a List using a merge sort algorithm, which provides a fast, stable sort. (A stable sort is one that does not reorder equal elements.)
- shuffle — randomly permutes the elements in a List.
- reverse — reverses the order of the elements in a List.
- rotate — rotates all the elements in a List by a specified distance.
- swap — swaps the elements at specified positions in a List.
- replaceAll — replaces all occurrences of one specified value with another.
- fill — overwrites every element in a List with the specified value.
- copy — copies the source List into the destination List.
- binarySearch — searches for an element in an ordered List using the binary search algorithm.
- indexOfSubList — returns the index of the first sublist of one List that is equal to another.
- lastIndexOfSubList — returns the index of the last sublist of one List that is equal to another.

[Queue](https://docs.oracle.com/javase/tutorial/collections/interfaces/queue.html)

[Deque](https://docs.oracle.com/javase/tutorial/collections/interfaces/deque.html)

[Map](https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html)

#### <b>Map Interface Basic Operations</b>
#### <b>Collection Views</b>
The Collection view methods allow a Map to be viewed as a Collection in these three ways:

- keySet — the Set of keys contained in the Map.
- values — The Collection of values contained in the Map. This Collection is not a Set, because multiple keys can map to the same value.
- entrySet — the Set of key-value pairs contained in the Map. The Map interface provides a small nested interface called Map.Entry, the type of the elements in this Set.

#### <b>Fancy Uses of Collection Views: Map Algebra</b>
#### <b>Multimaps</b>

---
[SortedSet](https://docs.oracle.com/javase/tutorial/collections/interfaces/sorted-set.html)

[SortedMap](https://docs.oracle.com/javase/tutorial/collections/interfaces/sorted-map.html)

---

## [聚合操作](https://docs.oracle.com/javase/tutorial/collections/streams/index.html)


## [实现](https://docs.oracle.com/javase/tutorial/collections/implementations/index.html)

## [算法](https://docs.oracle.com/javase/tutorial/collections/algorithms/index.html)

## [自定义实现](https://docs.oracle.com/javase/tutorial/collections/custom-implementations/index.html)

## [互操作性](https://docs.oracle.com/javase/tutorial/collections/interoperability/index.html)


### HashMap
HashMap是一个键值对存储数据结构，基本的结构：```HashMap<key, value>```

HashMap源码相关建议阅读：https://blog.csdn.net/login_sonata/article/details/76598675

## lambda表达式

## 聚合操作

## Packaging Programs In JAR Files

## 国际化

## 反射

## 安全

## JavaBeans

## 扩展机制

## 范型
