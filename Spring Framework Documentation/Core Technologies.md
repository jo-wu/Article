# Core Technologies
<small>Version 5.0.8.RELEASE</small>

---

This part of the reference documentation covers all of those technologies that are absolutely integral to the Spring Framework.

这部分参考文档涵盖了Spring Framework中所有绝对不可或缺的技术。

Foremost amongst these is the Spring Framework’s Inversion of Control (IoC) container. A thorough treatment of the Spring Framework’s IoC container is closely followed by comprehensive coverage of Spring’s Aspect-Oriented Programming (AOP) technologies. The Spring Framework has its own AOP framework, which is conceptually easy to understand, and which successfully addresses the 80% sweet spot of AOP requirements in Java enterprise programming.

其中最重要的是Spring Framework的控制反转（IoC）容器。 对Spring框架的IoC容器进行彻底的分析后，紧接着我们将全面介绍Spring的面向方面编程（AOP）技术。 Spring Framework有自己的AOP框架，概念上易于理解，并且很好地反映了Java企业编程中使用AOP技术开发的80％最佳实践。

Coverage of Spring’s integration with AspectJ (currently the richest - in terms of features - and certainly most mature AOP implementation in the Java enterprise space) is also provided.

还进一步提供了Spring与AspectJ的集成（目前最丰富的 - 在功能方面 - 当然也是Java企业领域中最成熟的AOP实现（指AspectJ））。


# 1. The IoC container

## 1.1. Introduction to the Spring IoC container and beans

This chapter covers the Spring Framework implementation of the Inversion of Control (IoC) [1] principle. IoC is also known as dependency injection (DI). It is a process whereby objects define their dependencies, that is, the other objects they work with, only through constructor arguments, arguments to a factory method, or properties that are set on the object instance after it is constructed or returned from a factory method. The container then injects those dependencies when it creates the bean. This process is fundamentally the inverse, hence the name Inversion of Control (IoC), of the bean itself controlling the instantiation or location of its dependencies by using direct construction of classes, or a mechanism such as the Service Locator pattern.

本章介绍了Spring Framework实现的控制反转（IoC）[1]原理。 IoC也称为依赖注入（DI）。 这是一个过程，通过这个过程，对象定义它们的依赖关系，即它们使用的其他对象，只能通过构造函数参数，工厂方法的参数，或者在构造或从工厂方法返回后在对象实例上设置的属性。然后,容器在创建bean时注入这些依赖项。 这个过程基本上是相反的，因此名称Inversion of Control（IoC），bean本身通过使用类的直接构造或诸如Service Locator模式之类的机制来控制其依赖关系的实例化或位置。

The org.springframework.beans and org.springframework.context packages are the basis for Spring Framework’s IoC container. The BeanFactory interface provides an advanced configuration mechanism capable of managing any type of object. ApplicationContext is a sub-interface of BeanFactory. It adds easier integration with Spring’s AOP features; message resource handling (for use in internationalization), event publication; and application-layer specific contexts such as the WebApplicationContext for use in web applications.

org.springframework.beans 和 org.springframework.context是Spring Framework的IOC容器的核心。BeanFactory接口提供了先进的。ApplicationContext是BeanFactory的一个子接口。它提供了更加简便方式集成Spring AOP特性； 消息资源处理（用于国际化），事件发布; 和特定于应用程序层的上下文，例如，WebApplicationContext，用于Web应用程序。

In short, the BeanFactory provides the configuration framework and basic functionality, and the ApplicationContext adds more enterprise-specific functionality. The ApplicationContext is a complete superset of the BeanFactory, and is used exclusively in this chapter in descriptions of Spring’s IoC container. For more information on using the BeanFactory instead of the ApplicationContext, refer to The BeanFactory.

简而言之，BeanFactory提供配置框架和基本功能，ApplicationContext添加了更多特定于企业的功能。 ApplicationContext是BeanFactory的完整超集，在本章中专门用于Spring的IoC容器的描述。 有关使用BeanFactory而不是ApplicationContext的更多信息，请参阅The BeanFactory。

In Spring, the objects that form the backbone of your application and that are managed by the Spring IoC container are called beans. A bean is an object that is instantiated, assembled, and otherwise managed by a Spring IoC container. Otherwise, a bean is simply one of many objects in your application. Beans, and the dependencies among them, are reflected in the configuration metadata used by a container.

在Spring中，构成应用程序主干并由Spring IoC容器管理的对象称为bean。 bean是一个由Spring IoC容器实例化，组装和管理的对象。 否则，bean只是应用程序中众多对象之一。 Bean及其之间的依赖关系反映在容器使用的配置元数据中。

## 1.2. Container overview

The interface org.springframework.context.ApplicationContext represents the Spring IoC container and is responsible for instantiating, configuring, and assembling the aforementioned beans. The container gets its instructions on what objects to instantiate, configure, and assemble by reading configuration metadata. The configuration metadata is represented in XML, Java annotations, or Java code. It allows you to express the objects that compose your application and the rich interdependencies between such objects.

接口org.springframework.context.ApplicationContext表示Spring IoC容器，负责实例化，配置和组装上述bean。 容器通过读取配置元数据获取有关要实例化，配置和组装的对象的指令。 配置元数据以XML，Java注释或Java代码表示。 它允许您表达组成应用程序的对象以及这些对象之间丰富的相互依赖性。

Several implementations of the ApplicationContext interface are supplied out-of-the-box with Spring. In standalone applications it is common to create an instance of ClassPathXmlApplicationContext or FileSystemXmlApplicationContext. While XML has been the traditional format for defining configuration metadata you can instruct the container to use Java annotations or code as the metadata format by providing a small amount of XML configuration to declaratively enable support for these additional metadata formats.

ApplicationContext接口的几个实现是与Spring一起提供的。 在独立应用程序中，通常会创建ClassPathXmlApplicationContext或FileSystemXmlApplicationContext的实例。 虽然XML是定义配置元数据的传统格式，但您可以通过提供少量XML配置来声明性地支持这些其他元数据格式，从而指示容器使用Java注释或代码作为元数据格式。

In most application scenarios, explicit user code is not required to instantiate one or more instances of a Spring IoC container. For example, in a web application scenario, a simple eight (or so) lines of boilerplate web descriptor XML in the web.xml file of the application will typically suffice (see Convenient ApplicationContext instantiation for web applications). If you are using the Spring Tool Suite Eclipse-powered development environment this boilerplate configuration can be easily created with few mouse clicks or keystrokes.

在大多数应用程序方案中，不需要显式用户代码来实例化Spring IoC容器的一个或多个实例。 例如，在Web应用程序场景中，应用程序的web.xml文件中的简单八行（左右）样板Web描述符XML通常就足够了（请参阅Web应用程序的便捷ApplicationContext实例化）。 如果您使用的是Spring工具套件Eclipse驱动的开发环境，只需点击几下鼠标或按键即可轻松创建此样板文件配置。

The following diagram is a high-level view of how Spring works. Your application classes are combined with configuration metadata so that after the ApplicationContext is created and initialized, you have a fully configured and executable system or application.

下图是Spring工作原理的高级视图。 您的应用程序类与配置元数据相结合，以便在创建和初始化ApplicationContext之后，您拥有一个完全配置且可执行的系统或应用程序。



## 1.3. Bean overview
## 1.4. Dependencies
## 1.5. Bean scopes
## 1.6. Customizing the nature of a bean
## 1.7. Bean definition inheritance
## 1.8. Container Extension Points
## 1.9. Annotation-based container configuration
## 1.10. Classpath scanning and managed components
## 1.11. Using JSR 330 Standard Annotations
## 1.12. Java-based container configuration

### 1.12.1. Basic concepts: @Bean and @Configuration
The central artifacts in Spring’s new Java-configuration support are @Configuration-annotated classes and @Bean-annotated methods.

The @Bean annotation is used to indicate that a method instantiates, configures and initializes a new object to be managed by the Spring IoC container. For those familiar with Spring’s \<beans/> XML configuration the @Bean annotation plays the same role as the \<bean/> element. You can use @Bean annotated methods with any Spring @Component, however, they are most often used with @Configuration beans.

Annotating a class with @Configuration indicates that its primary purpose is as a source of bean definitions. Furthermore, @Configuration classes allow inter-bean dependencies to be defined by simply calling other @Bean methods in the same class. The simplest possible @Configuration class would read as follows:

```
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyServiceImpl();
    }
}
```
The AppConfig class above would be equivalent to the following Spring \<beans/> XML:
```
<beans>
    <bean id="myService" class="com.acme.services.MyServiceImpl"/>
</beans>
```
<pre>
<center> Full @Configuration vs 'lite' @Bean mode?</center>
<i>When @Bean methods are declared within classes that are not annotated with @Configuration they are referred to as being processed in a 'lite' mode. Bean methods declared in a @Component or even in a plain old class will be considered 'lite', with a different primary purpose of the containing class and an @Bean method just being a sort of bonus there. For example, service components may expose management views to the container through an additional @Bean method on each applicable component class. In such scenarios, @Bean methods are a simple general-purpose factory method mechanism.

Unlike full @Configuration, lite @Bean methods cannot declare inter-bean dependencies. Instead, they operate on their containing component’s internal state and optionally on arguments that they may declare. Such an @Bean method should therefore not invoke other @Bean methods; each such method is literally just a factory method for a particular bean reference, without any special runtime semantics. The positive side-effect here is that no CGLIB subclassing has to be applied at runtime, so there are no limitations in terms of class design (i.e. the containing class may nevertheless be final etc).

In common scenarios, @Bean methods are to be declared within @Configuration classes, ensuring that 'full' mode is always used and that cross-method references will therefore get redirected to the container’s lifecycle management. This will prevent the same @Bean method from accidentally being invoked through a regular Java call which helps to reduce subtle bugs that can be hard to track down when operating in 'lite' mode.</i>
</pre>

The @Bean and @Configuration annotations will be discussed in depth in the sections below. First, however, we’ll cover the various ways of creating a spring container using Java-based configuration.

## 1.13. Environment abstraction
## 1.14. Registering a LoadTimeWeaver
## 1.15. Additional capabilities of the ApplicationContext
## 1.16. The BeanFactory

