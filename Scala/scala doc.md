# Scala document

原始链接：
https://docs.scala-lang.org/tour/tour-of-scala.html

---

### Welcome to the tour

This tour contains bite-sized introductions to the most frequently used features of Scala. It is intended for newcomers to the language.

这个指导手册包含一个对于最常用的Scala特性的简单的介绍。它是写给Scala新手的。

This is just a brief tour, not a full language tutorial. If you want that, consider obtaining a book or consulting other resources.

这只是一个简短的指导手册，不是一份Scala指南。如果你想要一个语言指南，考虑下买一些关于Scala的专业书籍或者其他的一些资源吧。

### What is Scala?

Scala is a modern multi-paradigm programming language designed to express common programming patterns in a concise, elegant, and type-safe way. It smoothly integrates features of object-oriented and functional languages.

Scala是一个现代的多范式语言，它被设计用一个简单、优雅、类型安全的方式来表达常见的编程模式。它平滑地集成了面向对象和功能语言的特性。

### Scala is object-oriented

Scala is a pure object-oriented language in the sense that every value is an object. Types and behavior of objects are described by classes and traits. Classes are extended by subclassing and a flexible mixin-based composition mechanism as a clean replacement for multiple inheritance.

Scala是一种纯粹的面向对象语言，因为每个值都是一个对象。对象的类型和行为由class和traits描述。Class拓展子类进行拓展，和一个灵活的基于mixin的组合机制来作为实现多重继承的clean的替代品。

### Scala is functional

Scala is also a functional language in the sense that every function is a value. Scala provides a lightweight syntax for defining anonymous functions, it supports higher-order functions, it allows functions to be nested, and supports currying. Scala’s case classes and its built-in support for pattern matching model algebraic types used in many functional programming languages. Singleton objects provide a convenient way to group functions that aren’t members of a class.

Scala也是一门函数式语言，因为在Scala中每一个函数都是一个值。Scala提供了一个轻量级语法来定义匿名函数，并且支持高阶函数，它允许函数嵌套，并支持柯里化。Scala的case类及其内置支持在许多函数式编程语言中使用的代数类型的模式匹配模型（pattern matching model algebraic types）。单例对象提供了一个方便的方式去组织那些不是class中的成员的函数

Furthermore, Scala’s notion of pattern matching naturally extends to the processing of XML data with the help of right-ignoring sequence patterns,by way of general extension via extractor objects. In this context, for comprehensions are useful for formulating queries. These features make Scala ideal for developing applications like web services.

此外，Scala的模式匹配概念自然延伸到XML数据的处理，借助于right-ignoring序列模式，通过对extractor对象的通用扩展。在这种情况下，对于方程式化查询的理解很有用。 这些功能使Scala成为开发Web服务等应用程序的理想选择。

### Scala is statically typed

Scala is equipped with an expressive type system that enforces statically that abstractions are used in a safe and coherent manner. In particular,the type system supports:

Scala配备了一个富有表现力的类型系统，静态地强制执行抽象以安全和连贯的方式使用。 特别是对类型系统支持：

- generic classes（泛型类）
- variance annotations（型变注释）
- upper and lower type bounds
- inner classes and abstract types as object members（内部类和抽象类型作为对象成员）
- compound types（复合类型）
- explicitly typed self references（显式类型的自引用）
- implicit parameters and conversions（隐式参数和转换）
- polymorphic methods（多态原则）

Type inference means the user is not required to annotate code with redundant type information. In combination, these features provide a powerful basis for the safe reuse of programming abstractions and for the type-safe extension of software.

类型推断意味着用户不需要使用冗余类型信息来注释代码。 结合使用，这些功能为安全重用编程抽象和软件的类型安全扩展提供了强大的基础。

### Scala is extensible

In practice, the development of domain-specific applications often requires domain-specific language extensions. Scala provides a unique combination of language mechanisms that make it easy to smoothly add new language constructs in the form of libraries.

实际上，特定于域的应用程序的开发通常需要特定于域的语言扩展。 Scala提供了独特的语言机制组合，可以轻松地以库的形式顺利添加新的语言结构。

In many cases, this can be done without using meta-programming facilities such as macros. For example,
- Implicit classes allow adding extension methods to existing types.
- String interpolation is user-extensible with custom interpolators.

在许多情况下，这可以在不使用宏等元编程工具的情况下完成。 例如，
- 隐式类允许向现有类型添加扩展方法。
- 字符串插值是用户可扩展的自定义插值器。

### Scala interoperates

Scala is designed to interoperate well with the popular Java Runtime Environment (JRE). In particular, the interaction with the mainstream object-oriented Java programming language is as smooth as possible. Newer Java features like SAMs,lambdas, annotations, and generics have direct analogues in Scala.

Scala旨在与流行的Java Runtime Environment（JRE）进行良好的互操作。 特别是，与主流面向对象Java编程语言的交互尽可能顺畅。 较新的Java功能如SAM，lambdas，注释和泛型在Scala中具有直接的类似性。

Those Scala features without Java analogues, such as default and named parameters, compile as close to Java as they can reasonably come. Scala has the same compilation model (separate compilation, dynamic class loading) like Java and allows access to thousands of existing high-quality libraries.

这些Scala特性虽然不与Java类似，例如默认和命名参数，但尽可能合理地编译为接近Java。 Scala具有与Java相同的编译模型（单独的编译，动态类加载），并允许访问数千个Java现有的高质量库。

### Enjoy the tour!

Please continue to the next page in the Contents menu to read more.

请继续阅读目录菜单中的下一页以了解更多信息。
