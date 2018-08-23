<h1>Spring Framework Overview</h1>
<span>version 5.0.7.RELEASE</span>
<div id="">Table of Contents</div>
<ul class="sectlevel1">
<li><a href="#overview-spring">1. What We Mean by "Spring"</a></li>
<li><a href="#overview-history">2. History of Spring and the Spring Framework</a></li>
<li><a href="#overview-philosophy">3. Design Philosophy</a></li>
<li><a href="#overview-feedback">4. Feedback and Contributions</a></li>
<li><a href="#overview-getting-started">5. Getting Started</a></li>
</ul>
<p>Spring 使得创建Java企业级应用非常简单. 它提供了企业环境下Java语言所需要的一切，除了通过在JVM上对Groovy和Kotlin这两门备选语言的支持 ， 还通过取决于不同应用的需求可以用灵活的方式来设计不同的架构。从Spring Framework 5.0开始，
Spring 要求 JDK 8+ (Java SE 8+) 并且已经提供了对 JDK 9 开箱即用的支持。</p>
<p>Spring支持广泛的应用场景。 在大型企业中，应用程序
通常需要维护很长时间，它们不得不运行在那些升级周期超出开发者控制范围的JDK和应用服务器上。 其他应用可能作为独立的jar嵌入服务器到中的运行，
更可能是在云环境中。 还有一些可能是不需要服务端的独立应用程序（例如批处理和集成工作负载）。
</p>
<p>
Spring是开源的。 它有一个庞大而活跃的，可以提供持续的反馈，基于各种各样的实际用例的社区
基于各种各样的实际用例。 这有助于Spring成功
并且保持长时间的进化。
</p>
<h2 id="overview-spring">1. What We Mean by "Spring"</h2>
<p>术语“春天”在不同的上下文中有不同的意义。 它可以用来指代
Spring Framework项目本身，它就是一切开始的地方。 随着时间的推移，其他Spring
项目已经构建在Spring Framework之上。 大多数情况下，当人们说
“Spring”，他们意味着整个Spring家族。 本参考文档侧重于
基础：Spring框架本身。
</p>
<p>
Spring框架分为几个模块。 应用程序可以选择所需的模块。
核心是核心容器的模块，包括配置模型和
依赖注入机制。 除此之外，Spring Framework为不同的应用程序体系结构提供了基本的支持，包括消息传递，事务数据和
持久性和网络。 它还包括基于Servlet的Spring MVC Web框架，以及
有并行特性的Spring WebFlux反应式Web框架。
</p>
<p>
关于模块的说明：Spring框架的Jar包允许在JDK9 ("Jigsaw")的模块化路径下进行部署。
为了在支持Jigsaw的应用程序中使用，Spring框架5的jar包（引入时）自带了模块的名字
 For use in Jigsaw-enabled applications, the Spring Framework 5 jars come with
"Automatic-Module-Name" manifest entries which define stable language-level module names
("spring.core", "spring.context" etc) independent from jar artifact names (the jars follow
the same naming pattern with "-" instead of ".", e.g. "spring-core" and "spring-context").
Of course, Spring&#8217;s framework jars keep working fine on the classpath on both JDK 8 and 9.</p>
<h2 id="overview-history">2. History of Spring and the Spring Framework</h2>
<p>Spring came into being in 2003 as a response to the complexity of the early
<a href="https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition">J2EE</a> specifications.
While some consider Java EE and Spring to be in competition, Spring is, in fact, complementary
to Java EE. The Spring programming model does not embrace the Java EE platform specification;
rather, it integrates with carefully selected individual specifications from the EE umbrella:</p>
<ul>
<li>
<p>Servlet API (<a href="https://jcp.org/en/jsr/detail?id=340">JSR 340</a>)</p>
</li>
<li>
<p>WebSocket API (<a href="https://www.jcp.org/en/jsr/detail?id=356">JSR 356</a>)</p>
</li>
<li>
<p>Concurrency Utilities (<a href="https://www.jcp.org/en/jsr/detail?id=236">JSR 236</a>)</p>
</li>
<li>
<p>JSON Binding API (<a href="https://jcp.org/en/jsr/detail?id=367">JSR 367</a>)</p>
</li>
<li>
<p>Bean Validation (<a href="https://jcp.org/en/jsr/detail?id=303">JSR 303</a>)</p>
</li>
<li>
<p>JPA (<a href="https://jcp.org/en/jsr/detail?id=338">JSR 338</a>)</p>
</li>
<li>
<p>JMS (<a href="https://jcp.org/en/jsr/detail?id=914">JSR 914</a>)</p>
</li>
<li>
<p>as well as JTA/JCA setups for transaction coordination, if necessary.</p>
</li>
</ul>
<p>The Spring Framework also supports the Dependency Injection
(<a href="https://www.jcp.org/en/jsr/detail?id=330">JSR 330</a>) and Common Annotations
(<a href="https://jcp.org/en/jsr/detail?id=250">JSR 250</a>) specifications, which application developers
may choose to use instead of the Spring-specific mechanisms provided by the Spring Framework.</p>
<p>As of Spring Framework 5.0, Spring requires the Java EE 7 level (e.g. Servlet 3.1+, JPA 2.1+)
as a minimum - while at the same time providing out-of-the-box integration with newer APIs
at the Java EE 8 level (e.g. Servlet 4.0, JSON Binding API) when encountered at runtime.
This keeps Spring fully compatible with e.g. Tomcat 8 and 9, WebSphere 9, and JBoss EAP 7.</p>
<p>Over time, the role of Java EE in application development has evolved. In the early days of
Java EE and Spring, applications were created to be deployed to an application server.
Today, with the help of Spring Boot, applications are created in a devops- and
cloud-friendly way, with the Servlet container embedded and trivial to change.
As of Spring Framework 5, a WebFlux application does not even use the Servlet API directly
and can run on servers (such as Netty) that are not Servlet containers.</p>
<p>Spring continues to innovate and to evolve. Beyond the Spring Framework, there are other
projects, such as Spring Boot, Spring Security, Spring Data, Spring Cloud, Spring Batch,
among others. It’s important to remember that each project has its own source code repository,
issue tracker, and release cadence. See <a href="https://spring.io/projects">spring.io/projects</a> for
the complete list of Spring projects.</p>
<h2 id="overview-philosophy">3. 设计哲学</h2>
<p>When you learn about a framework, it’s important to know not only what it does but what
principles it follows. Here are the guiding principles of the Spring Framework:</p>
<ul>
<li>
<p>Provide choice at every level. Spring lets you defer design decisions as late as possible.
For example, you can switch persistence providers through configuration without changing
your code. The same is true for many other infrastructure concerns and integration with
third-party APIs.</p>
</li>
<li>
<p>Accommodate diverse perspectives. Spring embraces flexibility and is not opinionated
about how things should be done. It supports a wide range of application needs with
different perspectives.</p>
</li>
<li>
<p>Maintain strong backward compatibility. Spring’s evolution has been carefully managed
to force few breaking changes between versions. Spring supports a carefully chosen range
of JDK versions and third-party libraries to facilitate maintenance of applications and
libraries that depend on Spring.</p>
</li>
<li>
<p>Care about API design. The Spring team puts a lot of thought and time into making APIs
that are intuitive and that hold up across many versions and many years.</p>
</li>
<li>
<p>Set high standards for code quality. The Spring Framework puts a strong emphasis on
meaningful, current, and accurate Javadoc. It is one of very few projects that can claim
clean code structure with no circular dependencies between packages.</p>
</li>
</ul>
<h2 id="overview-feedback">4. Feedback and Contributions</h2>
<p>For how-to questions or diagnosing or debugging issues, we suggest using StackOverflow,
and we have a <a href="https://spring.io/questions">questions page</a> that lists the suggested tags to use.
If you&#8217;re fairly certain that there is a problem in the Spring Framework or would like
to suggest a feature, please use the <a href="https://jira.spring.io/browse/spr">JIRA issue tracker</a>.</p>
<p>If you have a solution in mind or a suggested fix, you can submit a pull request on
<a href="https://github.com/spring-projects/spring-framework">Github</a>. However, please keep in mind
that, for all but the most trivial issues, we expect a ticket to be filed in the issue
tracker, where discussions take place and leave a record for future reference.</p>
<p>For more details see the guidelines at the
<a href="https://github.com/spring-projects/spring-framework/blob/master/CONTRIBUTING.adoc">CONTRIBUTING</a>,
top-level project page.</p>
<h2 id="overview-getting-started">5. Getting Started</h2>
<p>If you are just getting started with Spring, you may want to begin using the Spring
Framework by creating a <a href="https://projects.spring.io/spring-boot/">Spring Boot</a>-based
application. Spring Boot provides a quick (and opinionated) way to create a
production-ready Spring-based application. It is based on the Spring Framework, favors
convention over configuration, and is designed to get you up and running as quickly
as possible.</p>
<p>You can use <a href="https://start.spring.io/">start.spring.io</a> to generate a basic project or follow
one of the <a href="https://spring.io/guides">"Getting Started" guides</a>, such as
<a href="https://spring.io/guides/gs/rest-service/">Getting Started Building a RESTful Web Service</a>.
As well as being easier to digest, these guides are very task focused, and most of them
are based on Spring Boot. They also cover other projects from the Spring portfolio that
you might want to consider when solving a particular problem.</p>
Version 5.0.7.RELEASE<br>
Last updated 2018-06-12 14:40:48 UTC
