# JavaScript/ES5

https://wangdoc.com/javascript/basic/grammar.html

---

## 基础知识

变量的声明和赋值

var 声明全局变量

严格相等符: 类型、值
> ===

相等运算符: 值、自动类型转换
> ==

swich case 语句中采用严格相等符

三元运算符
> (条件) ? 表达式1 : 表达式2

### 基础类型

对象（object）：各种值组成的集合。

### null 和undefined

如果 JavaScript 预期某个位置应该是布尔值，会将该位置上现有的值自动转为布尔值。转换规则是除了下面六个值被转为false，其他值都视为true。

undefined
null
false
0
NaN
""或''（空字符串）

注意，空数组（[]）和空对象（{}）对应的布尔值，都是true。

### 整数和浮点数

JavaScript 内部，所有数字都是以64位浮点数形式储存，即使整数也是如此。所以，1与1.0是相同的，是同一个数。

由于浮点数不是精确的值，所以涉及小数的比较和运算要特别小心。

```

0.1 + 0.2 === 0.3
// false

0.3 / 0.1
// 2.9999999999999996

(0.3 - 0.2) === (0.2 - 0.1)
// false
```

### 正零和负零

JavaScript 内部实际上存在2个0：一个是+0，一个是-0，区别就是64位浮点数表示法的符号位不同。它们是等价的。

```

-0 === +0 // true
0 === -0 // true
0 === +0 // true
```
几乎所有场合，正零和负零都会被当作正常的0。

```
+0 // 0
-0 // 0
(-0).toString() // '0'
(+0).toString() // '0'
```
唯一有区别的场合是，+0或-0当作分母，返回的值是不相等的。

```
(1 / +0) === (1 / -0) // false
```
上面的代码之所以出现这样结果，是因为除以正零得到+Infinity，除以负零得到-Infinity，这两者是不相等的

### Base64 转码

JavaScript 原生提供两个 Base64 相关的方法。

btoa()：任意值转为 Base64 编码
atob()：Base64 编码转为原来的值

```
var string = 'Hello World!';
btoa(string) // "SGVsbG8gV29ybGQh"
atob('SGVsbG8gV29ybGQh') // "Hello World!"
```

---

## 第一等公民函数

### 声明函数的方法

3种:

### 函数的属性和方法

获取函数的名称：.name

获取函数的传入参数数量：.length

获取一个函数的所有代码：.toString()

---

## 闭包 (closure)

return


## 立即调用的函数表达式（IIFE）

它的目的有两个：一是不必为函数命名，避免了污染全局变量；二是 IIFE 内部形成了一个单独的作用域，可以封装一些外部无法读取的私有变量。

```
// 写法一
var tmp = newData;
processData(tmp);
storeData(tmp);

// 写法二
(function () {
  var tmp = newData;
  processData(tmp);
  storeData(tmp);
}());
```

上面代码中，写法二比写法一更好，因为完全避免了污染全局变量。