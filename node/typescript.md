# TypeScript -- ES6 finally going to

## 一、TypeScript语言特性

---

### 1)基本类型

特点：可选的静态类型声明

指定类型：number，boolean，string，array，enum

**number:**
```
//十进制
let decLiteral: number = 6;

//十六进制
let hexLiteral: number = 0xf00d;

//二进制
let binaryLiteral: number = 0b1010;

//八进制
let octalLiteral: number = 0o744; 
```

**boolean:**
```
let isDone: boolean = false;
let isDone: boolean = true;
```

**string:**
```
let name: string = "bob";
```
<small><i>模版字符串</i></small>

**array:**
```
let list: number[] = [1, 2, 3];
```
<small><i>数组泛型</i></small>

<small><i>元组 Tuple</i></small>

**enum:**
```
enum Color {Red, Green, Blue}
let c: Color = Color.Green;
```

未知类型：any

**any:**
```
let notSure: any = 4;
notSure = "maybe a string instead";
notSure = false; // okay, definitely a boolean
```

### 2)变量声明和运算符

let 声明

const 声明


### 3)流程控制语句






### 4)函数
### 5)类
### 6)接口
### 7)命名空间

## 二、使用函数

---

<small>1、在Typescript中使用函数</small>
### 1)函数声明和函数表达式
### 2)函数类型
### 3)有可选参数的函数
### 4)有默认参数的函数
### 5)有剩余参数的函数
### 6)函数重载
### 7)特定重载的签名
### 8)函数作用域
### 9)立即调用函数
### 10)范型
### 11)Tag函数和标签模板

<small>2、Typescript中的异步编程</small>
### 1)回调和高阶函数
### 2)箭头函数
### 3)回调函数
### 4)Promise
### 5)生成器
### 6)异步函数--async和await

## 三、Typescript中的面向对象编程

---


### 1)SOLID原则

<small>编写易于维护的、复用率高的、易于测试的面向对象代码。</small>



单一职责原则（SRP）：表明软件组件（函数、类、模块）必须专注于单一的任务（只有单一的职责）。

开/闭原则（OCP）：表明软件设计时必须时刻考虑到（代码）可能的发展（具体扩展性），但是程序的发展必须最少地修改已有的代码（对已有的修改封闭）。

里氏替换原则（LSP）：表明只要继承的是同一个接口，程序里任意一个类都可以被其他的类替换。在替换完成后，不需要其他额外的工作程序就能像原来一样运行。

接口隔离原则（ISP）：表明我们应该将那些非常大的接口（大而全的接口）拆分成一些小的更具体的接口（特定客户端接口），这样客户端就只需关心它们需要用到接口。

依赖反转原则（DIP）：表明一个方法应该遵守于抽象（接口）而不是一个实例（类）的概念。


### 2)类

类之间的关系：关联、聚合和组合

### 3)接口
### 4)关联、聚合和组合
### 5)继承
### 6)范型类
### 7)范型约束
### 8)遵循SOLID原则
### 9)命名空间

## 四、装饰器

---

### 1)注解和装饰器
### 2)类装饰器
### 3)方法装饰器
### 4)属性装饰器
### 5)参数装饰器
### 6)装饰器工厂
### 7)带有参数的装饰器
### 8)反射元数据API




参考链接：
https://www.tslang.cn/docs/handbook/basic-types.html