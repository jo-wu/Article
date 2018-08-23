<p>This part of the documentation covers support for Servlet stack, web applications built on the
Servlet API and deployed to Servlet containers. Individual chapters include <a href="#mvc">Spring MVC</a>,
<a href="#mvc-view">View Technologies</a>, <a href="#mvc-cors">CORS Support</a>, and <a href="#websocket">WebSocket Support</a>.
For reactive stack, web applications, go to <a href="web-reactive.html#spring-web-reactive">Web on Reactive Stack</a>.</p>

<h2 id="mvc">1. Spring Web MVC</h2>
<h3 id="mvc-introduction">1.1. Introduction</h3>
<p>Spring Web MVC is the original web framework built on the Servlet API and included
in the Spring Framework from the very beginning. The formal name "Spring Web MVC"
comes from the name of its source module
<a href="https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc">spring-webmvc</a>
but it is more commonly known as "Spring MVC".</p>
<p>Parallel to Spring Web MVC, Spring Framework 5.0 introduced a reactive stack, web framework
whose name Spring WebFlux is also based on its source module
<a href="https://github.com/spring-projects/spring-framework/tree/master/spring-webflux">spring-webflux</a>.
This section covers Spring Web MVC. The <a href="web-reactive.html#spring-web-reactive">next section</a>
covers Spring WebFlux.</p>
<p>For baseline information and compatibility with Servlet container and Java EE version
ranges please visit the Spring Framework
<a href="https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions">Wiki</a>.</p>
<h3 id="mvc-servlet">1.2. DispatcherServlet</h3>
<p><span class="small"><a href="web-reactive.html#webflux-dispatcher-handler">Same in Spring WebFlux</a></span></p>
<p>Spring MVC, like many other web frameworks, is designed around the front controller
pattern where a central <code>Servlet</code>, the <code>DispatcherServlet</code>, provides a shared algorithm
for request processing while actual work is performed by configurable, delegate components.
This model is flexible and supports diverse workflows.</p>
</div>
<div class="paragraph">
<p>The <code>DispatcherServlet</code>, as any <code>Servlet</code>, needs to be declared and mapped according
to the Servlet specification using Java configuration or in <code>web.xml</code>.
In turn the <code>DispatcherServlet</code> uses Spring configuration to discover
the delegate components it needs for request mapping, view resolution, exception
handling, <a href="#mvc-servlet-special-bean-types">and more</a>.</p>
</div>
<div class="paragraph">
<p>Below is an example of the Java configuration that registers and initializes
the <code>DispatcherServlet</code>. This class is auto-detected by the Servlet container
(see <a href="#mvc-container-config">Servlet Config</a>):</p>

```

public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletCxt) {

        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        ac.register(AppConfig.class);
        ac.refresh();

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(ac);
        ServletRegistration.Dynamic registration = servletCxt.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/app/*");
    }
}

```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>In addition to using the ServletContext API directly, you can also extend
<code>AbstractAnnotationConfigDispatcherServletInitializer</code> and override specific methods
(see example under <a href="#mvc-servlet-context-hierarchy">Context Hierarchy</a>).</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>Below is an example of <code>web.xml</code> configuration to register and initialize the <code>DispatcherServlet</code>:</p>
</div>

```
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/app-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value></param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app</servlet-name>
        <url-pattern>/app/*</url-pattern>
    </servlet-mapping>

</web-app>
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring Boot follows a different initialization sequence. Rather than hooking into
the lifecycle of the Servlet container, Spring Boot uses Spring configuration to
bootstrap itself and the embedded Servlet container. <code>Filter</code> and <code>Servlet</code> declarations
are detected in Spring configuration and registered with the Servlet container.
For more details check the
<a href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-embedded-container">Spring Boot docs</a>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect3">
<h4 id="mvc-servlet-context-hierarchy"><a class="anchor" href="#mvc-servlet-context-hierarchy"></a>1.2.1. Context Hierarchy</h4>
<div class="paragraph">
<p><code>DispatcherServlet</code> expects a <code>WebApplicationContext</code>, an extension of a plain
<code>ApplicationContext</code>, for its own configuration. <code>WebApplicationContext</code> has a link to the
<code>ServletContext</code> and <code>Servlet</code> it is associated with. It is also bound to the <code>ServletContext</code>
such that applications can use static methods on <code>RequestContextUtils</code> to look up the
<code>WebApplicationContext</code> if they need access to it.</p>
</div>
<div class="paragraph">
<p>For many applications having a single <code>WebApplicationContext</code> is simple and sufficient.
It is also possible to have a context hierarchy where one root <code>WebApplicationContext</code>
is shared across multiple <code>DispatcherServlet</code> (or other <code>Servlet</code>) instances, each with
its own child <code>WebApplicationContext</code> configuration.
See <a href="core.html#context-introduction">Additional Capabilities of the ApplicationContext</a>
for more on the context hierarchy feature.</p>
</div>
<div class="paragraph">
<p>The root <code>WebApplicationContext</code> typically contains infrastructure beans such as data repositories and
business services that need to be shared across multiple <code>Servlet</code> instances. Those beans
are effectively inherited and could be overridden (i.e. re-declared) in the Servlet-specific,
child <code>WebApplicationContext</code> which typically contains beans local to the given <code>Servlet</code>:</p>
</div>
<div class="imageblock">
<div class="content">
<img src="images/mvc-context-hierarchy.png" alt="mvc context hierarchy">
</div>
</div>
<div class="paragraph">
<p>Below is example configuration with a <code>WebApplicationContext</code> hierarchy:</p>
</div>

```
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { App1Config.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/app1/*" };
    }
}
```

<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>If an application context hierarchy is not required, applications may return all
configuration via <code>getRootConfigClasses()</code> and <code>null</code> from <code>getServletConfigClasses()</code>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>And the <code>web.xml</code> equivalent:</p>
</div>

```
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/root-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app1</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/app1-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app1</servlet-name>
        <url-pattern>/app1/*</url-pattern>
    </servlet-mapping>

</web-app>
```

<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>If an application context hierarchy is not required, applications may configure a
"root" context only and leave the <code>contextConfigLocation</code> Servlet parameter empty.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-servlet-special-bean-types"><a class="anchor" href="#mvc-servlet-special-bean-types"></a>1.2.2. Special Bean Types</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-special-bean-types">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>DispatcherServlet</code> delegates to special beans to process requests and render the
appropriate responses. By "special beans" we mean Spring-managed, Object instances that
implement WebFlux framework contracts. Those usually come with built-in contracts but
you can customize their properties, extend or replace them.</p>
</div>
<div class="paragraph">
<p>The table below lists the special beans detected by the <code>DispatcherHandler</code>:</p>
</div>
<table id="mvc-webappctx-special-beans-tbl" class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Bean type</th>
<th class="tableblock halign-left valign-top">Explanation</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-handlermapping">HandlerMapping</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Map a request to a handler along with a list of
<a href="#mvc-handlermapping-interceptor">interceptors</a> for pre- and post-processing.
The mapping is based on some criteria the details of which vary by <code>HandlerMapping</code>
implementation.</p>
<p class="tableblock"> The two main <code>HandlerMapping</code> implementations are <code>RequestMappingHandlerMapping</code> which
supports <code>@RequestMapping</code> annotated methods and <code>SimpleUrlHandlerMapping</code> which
maintains explicit registrations of URI path patterns to handlers.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">HandlerAdapter</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Help the <code>DispatcherServlet</code> to invoke a handler mapped to a request regardless of
how the handler is actually invoked. For example, invoking an annotated controller
requires resolving annotations. The main purpose of a <code>HandlerAdapter</code> is
to shield the <code>DispatcherServlet</code> from such details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-exceptionhandlers">HandlerExceptionResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Strategy to resolve exceptions possibly mapping them to handlers, or to HTML error
views, or other. See <a href="#mvc-exceptionhandlers">Exceptions</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-viewresolver">ViewResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolve logical String-based view names returned from a handler to an actual <code>View</code>
to render to the response with. See <a href="#mvc-viewresolver">View Resolution</a> and <a href="#mvc-view">View Technologies</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-localeresolver">LocaleResolver</a>, <a href="#mvc-timezone">LocaleContextResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolve the <code>Locale</code> a client is using and possibly their time zone, in order to be able
to offer internationalized views. See <a href="#mvc-localeresolver">Locale</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-themeresolver">ThemeResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolve themes your web application can use, for example, to offer personalized layouts.
See <a href="#mvc-themeresolver">Themes</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-multipart">MultipartResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Abstraction for parsing a multi-part request (e.g. browser form file upload) with
the help of some multipart parsing library. See <a href="#mvc-multipart">Multipart resolver</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="#mvc-flash-attributes">FlashMapManager</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Store and retrieve the "input" and the "output" <code>FlashMap</code> that can be used to pass
attributes from one request to another, usually across a redirect.
See <a href="#mvc-flash-attributes">Flash attributes</a>.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect3">
<h4 id="mvc-servlet-config"><a class="anchor" href="#mvc-servlet-config"></a>1.2.3. Web MVC Config</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-framework-config">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Applications can declare the infrastructure beans listed in <a href="#mvc-servlet-special-bean-types">Special Bean Types</a>
that are required to process requests. The <code>DispatcherServlet</code> checks the
<code>WebApplicationContext</code> for each special bean. If there are no matching bean types,
it falls back on the default types listed in
<a href="https://github.com/spring-projects/spring-framework/blob/master/spring-webmvc/src/main/resources/org/springframework/web/servlet/DispatcherServlet.properties">DispatcherServlet.properties</a>.</p>
</div>
<div class="paragraph">
<p>In most cases the <a href="#mvc-config">MVC Config</a> is the best starting point. It declares the required
beans in either Java or XML, and provides a higher level configuration callback API to
customize it.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring Boot relies on the MVC Java config to configure Spring MVC and also
provides many extra convenient options.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-container-config"><a class="anchor" href="#mvc-container-config"></a>1.2.4. Servlet Config</h4>
<div class="paragraph">
<p>In a Servlet 3.0+ environment, you have the option of configuring the Servlet container
programmatically as an alternative or in combination with a <code>web.xml</code> file. Below is an
example of registering a <code>DispatcherServlet</code>:</p>
</div>

```
import org.springframework.web.WebApplicationInitializer;

public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {
        XmlWebApplicationContext appContext = new XmlWebApplicationContext();
        appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");

        ServletRegistration.Dynamic registration = container.addServlet("dispatcher", new DispatcherServlet(appContext));
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
```

<div class="paragraph">
<p><code>WebApplicationInitializer</code> is an interface provided by Spring MVC that ensures your
implementation is detected and automatically used to initialize any Servlet 3 container.
An abstract base class implementation of <code>WebApplicationInitializer</code> named
<code>AbstractDispatcherServletInitializer</code> makes it even easier to register the
<code>DispatcherServlet</code> by simply overriding methods to specify the servlet mapping and the
location of the <code>DispatcherServlet</code> configuration.</p>
</div>
<div class="paragraph">
<p>This is recommended for applications that use Java-based Spring configuration:</p>
</div>

```
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MyWebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```

<div class="paragraph">
<p>If using XML-based Spring configuration, you should extend directly from
<code>AbstractDispatcherServletInitializer</code>:</p>
</div>


```
public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return null;
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        XmlWebApplicationContext cxt = new XmlWebApplicationContext();
        cxt.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
        return cxt;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```
<div class="paragraph">
<p><code>AbstractDispatcherServletInitializer</code> also provides a convenient way to add <code>Filter</code>
instances and have them automatically mapped to the <code>DispatcherServlet</code>:</p>
</div>

```
public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

    // ...

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {
            new HiddenHttpMethodFilter(), new CharacterEncodingFilter() };
    }
}
```

<div class="paragraph">
<p>Each filter is added with a default name based on its concrete type and automatically
mapped to the <code>DispatcherServlet</code>.</p>
</div>
<div class="paragraph">
<p>The <code>isAsyncSupported</code> protected method of <code>AbstractDispatcherServletInitializer</code>
provides a single place to enable async support on the <code>DispatcherServlet</code> and all
filters mapped to it. By default this flag is set to <code>true</code>.</p>
</div>
<div class="paragraph">
<p>Finally, if you need to further customize the <code>DispatcherServlet</code> itself, you can
override the <code>createDispatcherServlet</code> method.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-servlet-sequence"><a class="anchor" href="#mvc-servlet-sequence"></a>1.2.5. Processing</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-dispatcher-handler-sequence">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>DispatcherServlet</code> processes requests as follows:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>The <code>WebApplicationContext</code> is searched for and bound in the request as an attribute
that the controller and other elements in the process can use. It is bound by default
under the key <code>DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE</code>.</p>
</li>
<li>
<p>The locale resolver is bound to the request to enable elements in the process to
resolve the locale to use when processing the request (rendering the view, preparing
data, and so on). If you do not need locale resolving, you do not need it.</p>
</li>
<li>
<p>The theme resolver is bound to the request to let elements such as views determine
which theme to use. If you do not use themes, you can ignore it.</p>
</li>
<li>
<p>If you specify a multipart file resolver, the request is inspected for multiparts; if
multiparts are found, the request is wrapped in a <code>MultipartHttpServletRequest</code> for
further processing by other elements in the process. See <a href="#mvc-multipart">Multipart resolver</a> for further
information about multipart handling.</p>
</li>
<li>
<p>An appropriate handler is searched for. If a handler is found, the execution chain
associated with the handler (preprocessors, postprocessors, and controllers) is
executed in order to prepare a model or rendering. Or alternatively for annotated
controllers, the response may be rendered (within the <code>HandlerAdapter</code>) instead of
returning a view.</p>
</li>
<li>
<p>If a model is returned, the view is rendered. If no model is returned, (may be due to
a preprocessor or postprocessor intercepting the request, perhaps for security
reasons), no view is rendered, because the request could already have been fulfilled.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The <code>HandlerExceptionResolver</code> beans declared in the <code>WebApplicationContext</code> are used to
resolve exceptions thrown during request processing. Those exception resolvers allow
customizing the logic to address exceptions. See <a href="#mvc-exceptionhandlers">Exceptions</a> for more details.</p>
</div>
<div class="paragraph">
<p>The Spring <code>DispatcherServlet</code> also supports the return of the
<em>last-modification-date</em>, as specified by the Servlet API. The process of determining
the last modification date for a specific request is straightforward: the
<code>DispatcherServlet</code> looks up an appropriate handler mapping and tests whether the
handler that is found implements the <em>LastModified</em> interface. If so, the value of the
<code>long getLastModified(request)</code> method of the <code>LastModified</code> interface is returned to
the client.</p>
</div>
<div class="paragraph">
<p>You can customize individual <code>DispatcherServlet</code> instances by adding Servlet
initialization parameters ( <code>init-param</code> elements) to the Servlet declaration in the
<code>web.xml</code> file. See the following table for the list of supported parameters.</p>
</div>
<table id="mvc-disp-servlet-init-params-tbl" class="tableblock frame-all grid-all spread">
<caption class="title">Table 1. DispatcherServlet initialization parameters</caption>
<colgroup>
<col style="width: 50%;">
<col style="width: 50%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Parameter</th>
<th class="tableblock halign-left valign-top">Explanation</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>contextClass</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Class that implements <code>WebApplicationContext</code>, which instantiates the context used by
this Servlet. By default, the <code>XmlWebApplicationContext</code> is used.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>contextConfigLocation</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">String that is passed to the context instance (specified by <code>contextClass</code>) to
indicate where context(s) can be found. The string consists potentially of multiple
strings (using a comma as a delimiter) to support multiple contexts. In case of
multiple context locations with beans that are defined twice, the latest location
takes precedence.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>namespace</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Namespace of the <code>WebApplicationContext</code>. Defaults to <code>[servlet-name]-servlet</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>throwExceptionIfNoHandlerFound</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Whether to throw a <code>NoHandlerFoundException</code> when no handler was found for a request.
The exception can then be caught with a <code>HandlerExceptionResolver</code>, e.g. via an
<code>@ExceptionHandler</code> controller method, and handled as any others.</p>
<p class="tableblock"> By default this is set to "false", in which case the <code>DispatcherServlet</code> sets the
response status to 404 (NOT_FOUND) without raising an exception.</p>
<p class="tableblock"> Note that if <a href="#mvc-default-servlet-handler">default servlet handling</a> is
also configured, then unresolved requests are always forwarded to the default servlet
and a 404 would never be raised.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect3">
<h4 id="mvc-handlermapping-interceptor"><a class="anchor" href="#mvc-handlermapping-interceptor"></a>1.2.6. Interception</h4>
<div class="paragraph">
<p>All <code>HandlerMapping</code> implementations supports handler interceptors that are useful when
you want to apply specific functionality to certain requests, for example, checking for
a principal. Interceptors must implement <code>HandlerInterceptor</code> from the
<code>org.springframework.web.servlet</code> package with three methods that should provide enough
flexibility to do all kinds of pre-processing and post-processing:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>preHandle(..)</code>&#8201;&#8212;&#8201;<em>before</em> the actual handler is executed</p>
</li>
<li>
<p><code>postHandle(..)</code>&#8201;&#8212;&#8201;<em>after</em> the handler is executed</p>
</li>
<li>
<p><code>afterCompletion(..)</code>&#8201;&#8212;&#8201;<em>after the complete request has finished</em></p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The <code>preHandle(..)</code> method returns a boolean value. You can use this method to break or
continue the processing of the execution chain. When this method returns <code>true</code>, the
handler execution chain will continue; when it returns false, the <code>DispatcherServlet</code>
assumes the interceptor itself has taken care of requests (and, for example, rendered an
appropriate view) and does not continue executing the other interceptors and the actual
handler in the execution chain.</p>
</div>
<div class="paragraph">
<p>See <a href="#mvc-config-interceptors">Interceptors</a> in the section on MVC configuration for examples of how to
configure interceptors. You can also register them directly via setters on individual
<code>HandlerMapping</code> implementations.</p>
</div>
<div class="paragraph">
<p>Note that <code>postHandle</code> is less useful with <code>@ResponseBody</code> and <code>ResponseEntity</code> methods for
which the response is written and committed within the <code>HandlerAdapter</code> and before
<code>postHandle</code>. That means its too late to make any changes to the response such as adding
an extra header. For such scenarios you can implement <code>ResponseBodyAdvice</code> and either
declare it as an <a href="#mvc-ann-controller-advice">Controller Advice</a> bean or configure it directly on
<code>RequestMappingHandlerAdapter</code>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-exceptionhandlers"><a class="anchor" href="#mvc-exceptionhandlers"></a>1.2.7. Exceptions</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-dispatcher-exceptions">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>If an exception occurs during request mapping or is thrown from a request handler such as
an <code>@Controller</code>, the <code>DispatcherServlet</code> delegates to a chain of <code>HandlerExceptionResolver</code>
beans to resolve the exception and provide alternative handling, which typically is an
error response.</p>
</div>
<div class="paragraph">
<p>The table below lists the available <code>HandlerExceptionResolver</code> implementations:</p>
</div>
<table class="tableblock frame-all grid-all spread">
<caption class="title">Table 2. HandlerExceptionResolver implementations</caption>
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">HandlerExceptionResolver</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>SimpleMappingExceptionResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A mapping between exception class names and error view names. Useful for rendering
error pages in a browser application.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/mvc/support/DefaultHandlerExceptionResolver.html">DefaultHandlerExceptionResolver</a></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolves exceptions raised by Spring MVC and maps them to HTTP status codes.
Also see alternative <code>ResponseEntityExceptionHandler</code> and <a href="#mvc-ann-rest-exceptions">REST API exceptions</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ResponseStatusExceptionResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolves exceptions with the <code>@ResponseStatus</code> annotation and maps them to HTTP status
codes based on the value in the annotation.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ExceptionHandlerExceptionResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Resolves exceptions by invoking an <code>@ExceptionHandler</code> method in an <code>@Controller</code> or an
<code>@ControllerAdvice</code> class. See <a href="#mvc-ann-exceptionhandler">@ExceptionHandler methods</a>.</p></td>
</tr>
</tbody>
</table>
<div class="sect4">
<h5 id="mvc-excetionhandlers-handling"><a class="anchor" href="#mvc-excetionhandlers-handling"></a>Chain of resolvers</h5>
<div class="paragraph">
<p>You can form an exception resolver chain simply by declaring multiple <code>HandlerExceptionResolver</code>
beans in your Spring configuration and setting their <code>order</code> properties as needed.
The higher the order property, the later the exception resolver is positioned.</p>
</div>
<div class="paragraph">
<p>The contract of <code>HandlerExceptionResolver</code> specifies that it can return:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>ModelAndView</code> that points to an error view.</p>
</li>
<li>
<p>Empty <code>ModelAndView</code> if the exception was handled within the resolver.</p>
</li>
<li>
<p><code>null</code> if the exception remains unresolved, for subsequent resolvers to try; and if the
exception remains at the end, it is allowed to bubble up to the Servlet container.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The <a href="#mvc-config">MVC Config</a> automatically declares built-in resolvers for default Spring MVC
exceptions, for <code>@ResponseStatus</code> annotated exceptions, and for support of
<code>@ExceptionHandler</code> methods. You can customize that list or replace it.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-customer-servlet-container-error-page"><a class="anchor" href="#mvc-ann-customer-servlet-container-error-page"></a>Container error page</h5>
<div class="paragraph">
<p>If an exception remains unresolved by any <code>HandlerExceptionResolver</code> and is therefore
left to propagate, or if the response status is set to an error status (i.e. 4xx, 5xx),
Servlet containers may render a default error page in HTML. To customize the default
error page of the container, you can declare an error page mapping in <code>web.xml</code>:</p>
</div>

```
<error-page>
    <location>/error</location>
</error-page>
```

<div class="paragraph">
<p>Given the above, when an exception bubbles up, or the response has an error status, the
Servlet container makes an ERROR dispatch within the container to the configured URL
(e.g. "/error"). This is then processed by the <code>DispatcherServlet</code>, possibly mapping it
to an <code>@Controller</code> which could be implemented to return an error view name with a model
or to render a JSON response as shown below:</p>
</div>

```
@RestController
public class ErrorController {

    @RequestMapping(path = "/error")
    public Map<String, Object> handle(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", request.getAttribute("javax.servlet.error.status_code"));
        map.put("reason", request.getAttribute("javax.servlet.error.message"));
        return map;
    }
}
```

<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The Servlet API does not provide a way to create error page mappings in Java. You can
however use both an <code>WebApplicationInitializer</code> and a minimal <code>web.xml</code>.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-viewresolver"><a class="anchor" href="#mvc-viewresolver"></a>1.2.8. View Resolution</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-viewresolution">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC defines the <code>ViewResolver</code> and <code>View</code> interfaces that enable you to render
models in a browser without tying you to a specific view technology. <code>ViewResolver</code>
provides a mapping between view names and actual views. <code>View</code> addresses the preparation
of data before handing over to a specific view technology.</p>
</div>
<div class="paragraph">
<p>The table below provides more details on the <code>ViewResolver</code> hierarchy:</p>
</div>
<table id="mvc-view-resolvers-tbl" class="tableblock frame-all grid-all spread">
<caption class="title">Table 3. ViewResolver implementations</caption>
<colgroup>
<col style="width: 50%;">
<col style="width: 50%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">ViewResolver</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>AbstractCachingViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Sub-classes of <code>AbstractCachingViewResolver</code> cache view instances that they resolve.
Caching improves performance of certain view technologies. It&#8217;s possible to turn off the
cache by setting the <code>cache</code> property to <code>false</code>. Furthermore, if you must refresh a
certain view at runtime (for example when a FreeMarker template is modified), you can use
the <code>removeFromCache(String viewName, Locale loc)</code> method.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>XmlViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Implementation of <code>ViewResolver</code> that accepts a configuration file written in XML with
the same DTD as Spring&#8217;s XML bean factories. The default configuration file is
<code>/WEB-INF/views.xml</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ResourceBundleViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Implementation of <code>ViewResolver</code> that uses bean definitions in a <code>ResourceBundle</code>,
specified by the bundle base name, and for each view it is supposed to resolve, it uses
the value of the property <code>[viewname].(class)</code> as the view class and the value of the
property <code>[viewname].url</code> as the view url. Examples can be found in the chapter on
<a href="#mvc-view">View Technologies</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>UrlBasedViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Simple implementation of the <code>ViewResolver</code> interface that effects the direct
resolution of logical view names to URLs, without an explicit mapping definition. This
is appropriate if your logical names match the names of your view resources in a
straightforward manner, without the need for arbitrary mappings.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>InternalResourceViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Convenient subclass of <code>UrlBasedViewResolver</code> that supports <code>InternalResourceView</code> (in
effect, Servlets and JSPs) and subclasses such as <code>JstlView</code> and <code>TilesView</code>. You can
specify the view class for all views generated by this resolver by using
<code>setViewClass(..)</code>. See the <code>UrlBasedViewResolver</code> javadocs for details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>FreeMarkerViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Convenient subclass of <code>UrlBasedViewResolver</code> that supports <code>FreeMarkerView</code> and
custom subclasses of them.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ContentNegotiatingViewResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Implementation of the <code>ViewResolver</code> interface that resolves a view based on the
request file name or <code>Accept</code> header. See <a href="#mvc-multiple-representations">Content negotiation</a>.</p></td>
</tr>
</tbody>
</table>
<div class="sect4">
<h5 id="mvc-viewresolver-handling"><a class="anchor" href="#mvc-viewresolver-handling"></a>Handling</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-viewresolution-handling">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You chain view resolvers by declaring more than one resolver beans and, if necessary, by
setting the <code>order</code> property to specify ordering. Remember, the higher the order property,
the later the view resolver is positioned in the chain.</p>
</div>
<div class="paragraph">
<p>The contract of a <code>ViewResolver</code> specifies that it <em>can</em> return null to indicate the
view could not be found. However in the case of JSPs, and <code>InternalResourceViewResolver</code>,
the only way to figure out if a JSP exists is to perform a dispatch through
<code>RequestDispatcher</code>. Therefore an <code>InternalResourceViewResolver</code> must always be configured
to be last in the overall order of view resolvers.</p>
</div>
<div class="paragraph">
<p>To configure view resolution is as simple as adding <code>ViewResolver</code> beans to your Spring
configuration. The <a href="#mvc-config">MVC Config</a> provides provides a dedicated configuration API for
<a href="#mvc-config-view-resolvers">View Resolvers</a> and also for adding logic-less
<a href="#mvc-config-view-controller">View Controllers</a> which are useful for HTML template
rendering without controller logic.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-redirecting-redirect-prefix"><a class="anchor" href="#mvc-redirecting-redirect-prefix"></a>Redirecting</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-redirecting-redirect-prefix">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The special <code>redirect:</code> prefix in a view name allows you to perform a redirect. The
<code>UrlBasedViewResolver</code> (and sub-classes) recognize this as an instruction that a
redirect is needed. The rest of the view name is the redirect URL.</p>
</div>
<div class="paragraph">
<p>The net effect is the same as if the controller had returned a <code>RedirectView</code>, but now
the controller itself can simply operate in terms of logical view names. A logical view
name such as <code>redirect:/myapp/some/resource</code> will redirect relative to the current
Servlet context, while a name such as <code>redirect:http://myhost.com/some/arbitrary/path</code>
will redirect to an absolute URL.</p>
</div>
<div class="paragraph">
<p>Note that if a controller method is annotated with the <code>@ResponseStatus</code>, the annotation
value takes precedence over the response status set by <code>RedirectView</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-redirecting-forward-prefix"><a class="anchor" href="#mvc-redirecting-forward-prefix"></a>Forwarding</h5>
<div class="paragraph">
<p>It is also possible to use a special <code>forward:</code> prefix for view names that are
ultimately resolved by <code>UrlBasedViewResolver</code> and subclasses. This creates an
<code>InternalResourceView</code> which does a <code>RequestDispatcher.forward()</code>.
Therefore, this prefix is not useful with <code>InternalResourceViewResolver</code> and
<code>InternalResourceView</code> (for JSPs) but it can be helpful if using another view
technology, but still want to force a forward of a resource to be handled by the
Servlet/JSP engine. Note that you may also chain multiple view resolvers, instead.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-multiple-representations"><a class="anchor" href="#mvc-multiple-representations"></a>Content negotiation</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-multiple-representations">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/view/ContentNegotiatingViewResolver.html">ContentNegotiatingViewResolver</a>
does not resolve views itself but rather delegates
to other view resolvers, and selects the view that resembles the representation requested
by the client. The representation can be determined from the <code>Accept</code> header or from a
query parameter, e.g. <code>"/path?format=pdf"</code>.</p>
</div>
<div class="paragraph">
<p>The <code>ContentNegotiatingViewResolver</code> selects an appropriate <code>View</code> to handle the request
by comparing the request media type(s) with the media type (also known as
<code>Content-Type</code>) supported by the <code>View</code> associated with each of its <code>ViewResolvers</code>. The
first <code>View</code> in the list that has a compatible <code>Content-Type</code> returns the representation
to the client. If a compatible view cannot be supplied by the <code>ViewResolver</code> chain, then
the list of views specified through the <code>DefaultViews</code> property will be consulted. This
latter option is appropriate for singleton <code>Views</code> that can render an appropriate
representation of the current resource regardless of the logical view name. The <code>Accept</code>
header may include wild cards, for example <code>text/*</code>, in which case a <code>View</code> whose
Content-Type was <code>text/xml</code> is a compatible match.</p>
</div>
<div class="paragraph">
<p>See <a href="#mvc-config-view-resolvers">View Resolvers</a> under <a href="#mvc-config">MVC Config</a> for configuration details.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-localeresolver"><a class="anchor" href="#mvc-localeresolver"></a>1.2.9. Locale</h4>
<div class="paragraph">
<p>Most parts of Spring&#8217;s architecture support internationalization, just as the Spring web
MVC framework does. <code>DispatcherServlet</code> enables you to automatically resolve messages
using the client&#8217;s locale. This is done with <code>LocaleResolver</code> objects.</p>
</div>
<div class="paragraph">
<p>When a request comes in, the <code>DispatcherServlet</code> looks for a locale resolver, and if it
finds one it tries to use it to set the locale. Using the <code>RequestContext.getLocale()</code>
method, you can always retrieve the locale that was resolved by the locale resolver.</p>
</div>
<div class="paragraph">
<p>In addition to automatic locale resolution, you can also attach an interceptor to the
handler mapping (see <a href="#mvc-handlermapping-interceptor">Interception</a> for more information on handler
mapping interceptors) to change the locale under specific circumstances, for example,
based on a parameter in the request.</p>
</div>
<div class="paragraph">
<p>Locale resolvers and interceptors are defined in the
<code>org.springframework.web.servlet.i18n</code> package and are configured in your application
context in the normal way. Here is a selection of the locale resolvers included in
Spring.</p>
</div>
<div class="sect4">
<h5 id="mvc-timezone"><a class="anchor" href="#mvc-timezone"></a>TimeZone</h5>
<div class="paragraph">
<p>In addition to obtaining the client&#8217;s locale, it is often useful to know their time zone.
The <code>LocaleContextResolver</code> interface offers an extension to <code>LocaleResolver</code> that allows
resolvers to provide a richer <code>LocaleContext</code>, which may include time zone information.</p>
</div>
<div class="paragraph">
<p>When available, the user&#8217;s <code>TimeZone</code> can be obtained using the
<code>RequestContext.getTimeZone()</code> method. Time zone information will automatically be used
by Date/Time <code>Converter</code> and <code>Formatter</code> objects registered with Spring&#8217;s
<code>ConversionService</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-localeresolver-acceptheader"><a class="anchor" href="#mvc-localeresolver-acceptheader"></a>Header resolver</h5>
<div class="paragraph">
<p>This locale resolver inspects the <code>accept-language</code> header in the request that was sent
by the client (e.g., a web browser). Usually this header field contains the locale of
the client&#8217;s operating system. <em>Note that this resolver does not support time zone
information.</em></p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-localeresolver-cookie"><a class="anchor" href="#mvc-localeresolver-cookie"></a>Cookie resolver</h5>
<div class="paragraph">
<p>This locale resolver inspects a <code>Cookie</code> that might exist on the client to see if a
<code>Locale</code> or <code>TimeZone</code> is specified. If so, it uses the specified details. Using the
properties of this locale resolver, you can specify the name of the cookie as well as the
maximum age. Find below an example of defining a <code>CookieLocaleResolver</code>.</p>
</div>

```
<bean id="localeResolver" class="org.springframework.web.servlet.i18n.CookieLocaleResolver">

    <property name="cookieName" value="clientlanguage"/>

    <!-- in seconds. If set to -1, the cookie is not persisted (deleted when browser shuts down) -->
    <property name="cookieMaxAge" value="100000"/>

</bean>
```

<table id="mvc-cookie-locale-resolver-props-tbl" class="tableblock frame-all grid-all spread">
<caption class="title">Table 4. CookieLocaleResolver properties</caption>
<colgroup>
<col style="width: 16.6666%;">
<col style="width: 16.6666%;">
<col style="width: 66.6668%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Property</th>
<th class="tableblock halign-left valign-top">Default</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">cookieName</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">classname + LOCALE</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The name of the cookie</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">cookieMaxAge</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Servlet container default</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The maximum time a cookie will stay persistent on the client. If -1 is specified, the
cookie will not be persisted; it will only be available until the client shuts down
their browser.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">cookiePath</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">/</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Limits the visibility of the cookie to a certain part of your site. When cookiePath is
specified, the cookie will only be visible to that path and the paths below it.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect4">
<h5 id="mvc-localeresolver-session"><a class="anchor" href="#mvc-localeresolver-session"></a>Session resolver</h5>
<div class="paragraph">
<p>The <code>SessionLocaleResolver</code> allows you to retrieve <code>Locale</code> and <code>TimeZone</code> from the
session that might be associated with the user&#8217;s request. In contrast to
<code>CookieLocaleResolver</code>, this strategy stores locally chosen locale settings in the
Servlet container&#8217;s <code>HttpSession</code>. As a consequence, those settings are just temporary
for each session and therefore lost when each session terminates.</p>
</div>
<div class="paragraph">
<p>Note that there is no direct relationship with external session management mechanisms
such as the Spring Session project. This <code>SessionLocaleResolver</code> will simply evaluate and
modify corresponding <code>HttpSession</code> attributes against the current <code>HttpServletRequest</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-localeresolver-interceptor"><a class="anchor" href="#mvc-localeresolver-interceptor"></a>Locale interceptor</h5>
<div class="paragraph">
<p>You can enable changing of locales by adding the <code>LocaleChangeInterceptor</code> to one of the
handler mappings (see <a href="#mvc-handlermapping">[mvc-handlermapping]</a>). It will detect a parameter in the request
and change the locale. It calls <code>setLocale()</code> on the <code>LocaleResolver</code> that also exists
in the context. The following example shows that calls to all <code>*.view</code> resources
containing a parameter named <code>siteLanguage</code> will now change the locale. So, for example,
a request for the following URL, <code><a href="https://www.sf.net/home.view?siteLanguage=nl" class="bare">http://www.sf.net/home.view?siteLanguage=nl</a></code> will
change the site language to Dutch.</p>
</div>

```
<bean id="localeChangeInterceptor"
        class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor">
    <property name="paramName" value="siteLanguage"/>
</bean>

<bean id="localeResolver"
        class="org.springframework.web.servlet.i18n.CookieLocaleResolver"/>

<bean id="urlMapping"
        class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    <property name="interceptors">
        <list>
            <ref bean="localeChangeInterceptor"/>
        </list>
    </property>
    <property name="mappings">
        <value>/**/*.view=someController</value>
    </property>
</bean>
```

<div class="sect3">
<h4 id="mvc-themeresolver"><a class="anchor" href="#mvc-themeresolver"></a>1.2.10. Themes</h4>
<div class="paragraph">
<p>You can apply Spring Web MVC framework themes to set the overall look-and-feel of your
application, thereby enhancing user experience. A theme is a collection of static
resources, typically style sheets and images, that affect the visual style of the
application.</p>
</div>
<div class="sect4">
<h5 id="mvc-themeresolver-defining"><a class="anchor" href="#mvc-themeresolver-defining"></a>Define a theme</h5>
<div class="paragraph">
<p>To use themes in your web application, you must set up an implementation of the
<code>org.springframework.ui.context.ThemeSource</code> interface. The <code>WebApplicationContext</code>
interface extends <code>ThemeSource</code> but delegates its responsibilities to a dedicated
implementation. By default the delegate will be an
<code>org.springframework.ui.context.support.ResourceBundleThemeSource</code> implementation that
loads properties files from the root of the classpath. To use a custom <code>ThemeSource</code>
implementation or to configure the base name prefix of the <code>ResourceBundleThemeSource</code>,
you can register a bean in the application context with the reserved name <code>themeSource</code>.
The web application context automatically detects a bean with that name and uses it.</p>
</div>
<div class="paragraph">
<p>When using the <code>ResourceBundleThemeSource</code>, a theme is defined in a simple properties
file. The properties file lists the resources that make up the theme. Here is an example:</p>
</div>
<div class="literalblock">
<div class="content">
<pre>styleSheet=/themes/cool/style.css
background=/themes/cool/img/coolBg.jpg</pre>
</div>
</div>
<div class="paragraph">
<p>The keys of the properties are the names that refer to the themed elements from view
code. For a JSP, you typically do this using the <code>spring:theme</code> custom tag, which is
very similar to the <code>spring:message</code> tag. The following JSP fragment uses the theme
defined in the previous example to customize the look and feel:</p>
</div>

```
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
    <head>
        <link rel="stylesheet" href="<spring:theme code='styleSheet'/>" type="text/css"/>
    </head>
    <body style="background=<spring:theme code='background'/>">
        ...
    </body>
</html>
```

<div class="paragraph">
<p>By default, the <code>ResourceBundleThemeSource</code> uses an empty base name prefix. As a result,
the properties files are loaded from the root of the classpath. Thus you would put the
<code>cool.properties</code> theme definition in a directory at the root of the classpath, for
example, in <code>/WEB-INF/classes</code>. The <code>ResourceBundleThemeSource</code> uses the standard Java
resource bundle loading mechanism, allowing for full internationalization of themes. For
example, we could have a <code>/WEB-INF/classes/cool_nl.properties</code> that references a special
background image with Dutch text on it.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-themeresolver-resolving"><a class="anchor" href="#mvc-themeresolver-resolving"></a>Resolve themes</h5>
<div class="paragraph">
<p>After you define themes, as in the preceding section, you decide which theme to use. The
<code>DispatcherServlet</code> will look for a bean named <code>themeResolver</code> to find out which
<code>ThemeResolver</code> implementation to use. A theme resolver works in much the same way as a
<code>LocaleResolver</code>. It detects the theme to use for a particular request and can also
alter the request&#8217;s theme. The following theme resolvers are provided by Spring:</p>
</div>
<table id="mvc-theme-resolver-impls-tbl" class="tableblock frame-all grid-all spread">
<caption class="title">Table 5. ThemeResolver implementations</caption>
<colgroup>
<col style="width: 20%;">
<col style="width: 80%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Class</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>FixedThemeResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Selects a fixed theme, set using the <code>defaultThemeName</code> property.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>SessionThemeResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The theme is maintained in the user&#8217;s HTTP session. It only needs to be set once for
each session, but is not persisted between sessions.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>CookieThemeResolver</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The selected theme is stored in a cookie on the client.</p></td>
</tr>
</tbody>
</table>
<div class="paragraph">
<p>Spring also provides a <code>ThemeChangeInterceptor</code> that allows theme changes on every
request with a simple request parameter.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-multipart"><a class="anchor" href="#mvc-multipart"></a>1.2.11. Multipart resolver</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-multipart">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>MultipartResolver</code> from the <code>org.springframework.web.multipart</code> package is a strategy
for parsing multipart requests including file uploads. There is one implementation
based on <a href="https://jakarta.apache.org/commons/fileupload"><em>Commons FileUpload</em></a> and another
based on Servlet 3.0 multipart request parsing.</p>
</div>
<div class="paragraph">
<p>To enable multipart handling, you need declare a <code>MultipartResolver</code> bean in your
<code>DispatcherServlet</code> Spring configuration with the name "multipartResolver".
The <code>DispatcherServlet</code> detects it and applies it to incoming request. When a POST with
content-type of "multipart/form-data" is received, the resolver parses the content and
wraps the current <code>HttpServletRequest</code> as <code>MultipartHttpServletRequest</code> in order to
provide access to resolved parts in addition to exposing them as request parameters.</p>
</div>
<div class="sect4">
<h5 id="mvc-multipart-resolver-commons"><a class="anchor" href="#mvc-multipart-resolver-commons"></a>Apache FileUpload</h5>
<div class="paragraph">
<p>To use Apache Commons FileUpload, simply configure a bean of type
<code>CommonsMultipartResolver</code> with the name <code>multipartResolver</code>. Of course you also need to
have <code>commons-fileupload</code> as a dependency on your classpath.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-multipart-resolver-standard"><a class="anchor" href="#mvc-multipart-resolver-standard"></a>Servlet 3.0</h5>
<div class="paragraph">
<p>Servlet 3.0 multipart parsing needs to be enabled through Servlet container configuration:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>in Java, set a <code>MultipartConfigElement</code> on the Servlet registration.</p>
</li>
<li>
<p>in <code>web.xml</code>, add a <code>"&lt;multipart-config&gt;"</code> section to the servlet declaration.</p>
</li>
</ul>
</div>


```
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // ...

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {

        // Optionally also set maxFileSize, maxRequestSize, fileSizeThreshold
        registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
    }

}
```

<div class="paragraph">
<p>Once the Servlet 3.0 configuration is in place, simply add a bean of type
<code>StandardServletMultipartResolver</code> with the name <code>multipartResolver</code>.</p>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="filters"><a class="anchor" href="#filters"></a>1.3. Filters</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-filters">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>spring-web</code> module provides some useful filters.</p>
</div>
<div class="sect3">
<h4 id="filters-http-put"><a class="anchor" href="#filters-http-put"></a>1.3.1. HTTP PUT Form</h4>
<div class="paragraph">
<p>Browsers can only submit form data via HTTP GET or HTTP POST but non-browser clients can also
use HTTP PUT and PATCH. The Servlet API requires <code>ServletRequest.getParameter*()</code>
methods to support form field access only for HTTP POST.</p>
</div>
<div class="paragraph">
<p>The <code>spring-web</code> module provides <code>HttpPutFormContentFilter</code> that intercepts HTTP PUT and
PATCH requests with content type <code>application/x-www-form-urlencoded</code>, reads the form data from
the body of the request, and wraps the <code>ServletRequest</code> in order to make the form data
available through the <code>ServletRequest.getParameter*()</code> family of methods.</p>
</div>
</div>
<div class="sect3">
<h4 id="webflux-filters-forwarded-headers"><a class="anchor" href="#webflux-filters-forwarded-headers"></a>1.3.2. Forwarded Headers</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-filters-forwarded-headers">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>As a request goes through proxies such as load balancers the host, port, and
scheme may change presenting a challenge for applications that need to create links
to resources since the links should reflect the host, port, and scheme of the
original request as seen from a client perspective.</p>
</div>
<div class="paragraph">
<p><a href="https://tools.ietf.org/html/rfc7239">RFC 7239</a> defines the "Forwarded" HTTP header
for proxies to use to provide information about the original request. There are also
other non-standard headers in use such as "X-Forwarded-Host", "X-Forwarded-Port",
and "X-Forwarded-Proto".</p>
</div>
<div class="paragraph">
<p><code>ForwardedHeaderFilter</code> detects, extracts, and uses information from the "Forwarded"
header, or from "X-Forwarded-Host", "X-Forwarded-Port", and "X-Forwarded-Proto".
It wraps the request in order to overlay its host, port, and scheme and also "hides"
the forwarded headers for subsequent processing.</p>
</div>
<div class="paragraph">
<p>Note that there are security considerations when using forwarded headers as explained
in Section 8 of RFC 7239. At the application level it is difficult to determine whether
forwarded headers can be trusted or not. This is why the network upstream should be
configured correctly to filter out untrusted forwarded headers from the outside.</p>
</div>
<div class="paragraph">
<p>Applications that don&#8217;t have a proxy and don&#8217;t need to use forwarded headers can
configure the <code>ForwardedHeaderFilter</code> to remove and ignore such headers.</p>
</div>
</div>
<div class="sect3">
<h4 id="filters-shallow-etag"><a class="anchor" href="#filters-shallow-etag"></a>1.3.3. Shallow ETag</h4>
<div class="paragraph">
<p>The <code>ShallowEtagHeaderFilter</code> filter creates a "shallow" ETag by caching the content
written to the response, and computing an MD5 hash from it. The next time a client sends,
it does the same, but also compares the computed value against the <code>If-None-Match</code> request
header and if the two are equal, it returns a 304 (NOT_MODIFIED).</p>
</div>
<div class="paragraph">
<p>This strategy saves network bandwidth but not CPU, as the full response must be computed
for each request. Other strategies at the controller level, described above, can avoid the
computation. See <a href="#mvc-caching">HTTP Caching</a>.</p>
</div>
<div class="paragraph">
<p>This filter has a <code>writeWeakETag</code> parameter that configures the filter to write Weak ETags,
like this: <code>W/"02a2d595e6ed9a0b24f027f2b63b134d6"</code>, as defined in
<a href="https://tools.ietf.org/html/rfc7232#section-2.3">RFC 7232 Section 2.3</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="filters-cors"><a class="anchor" href="#filters-cors"></a>1.3.4. CORS</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-filters-cors">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC provides fine-grained support for CORS configuration through annotations on
controllers. However when used with Spring Security it is advisable to rely on the built-in
<code>CorsFilter</code> that must be ordered ahead of Spring Security&#8217;s chain of filters.</p>
</div>
<div class="paragraph">
<p>See the section on <a href="#mvc-cors">CORS</a> and the <a href="#mvc-cors-filter">CORS Filter</a> for more details.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-controller"><a class="anchor" href="#mvc-controller"></a>1.4. Annotated Controllers</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-controller">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC provides an annotation-based programming model where <code>@Controller</code> and
<code>@RestController</code> components use annotations to express request mappings, request input,
exception handling, and more. Annotated controllers have flexible method signatures and
do not have to extend base classes nor implement specific interfaces.</p>
</div>

```
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String handle(Model model) {
        model.addAttribute("message", "Hello World!");
        return "index";
    }
}
```

<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Guides and tutorials on <a href="https://spring.io/guides">spring.io</a> use the annotation-based
programming model described in this section.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect3">
<h4 id="mvc-ann-controller"><a class="anchor" href="#mvc-ann-controller"></a>1.4.1. Declaration</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-controller">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can define controller beans using a standard Spring bean definition in the
Servlet&#8217;s <code>WebApplicationContext</code>. The <code>@Controller</code> stereotype allows for auto-detection,
aligned with Spring general support for detecting <code>@Component</code> classes in the classpath
and auto-registering bean definitions for them. It also acts as a stereotype for the
annotated class, indicating its role as a web component.</p>
</div>
<div class="paragraph">
<p>To enable auto-detection of such <code>@Controller</code> beans, you can add component scanning to
your Java configuration:</p>
</div>

```
@Configuration
@ComponentScan("org.example.web")
public class WebConfig {

    // ...
}
```

<div class="paragraph">
<p>The XML configuration equivalent:</p>
</div>


```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.example.web"/>

    <!-- ... -->

</beans>
```

<div class="paragraph">
<p><code>@RestController</code> is a <a href="core.html#beans-meta-annotations">composed annotation</a> that is
itself meta-annotated with <code>@Controller</code> and <code>@ResponseBody</code> indicating a controller whose
every method inherits the type-level <code>@ResponseBody</code> annotation and therefore writes
directly to the response body vs view resolution and rendering with an HTML template.</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-proxying"><a class="anchor" href="#mvc-ann-requestmapping-proxying"></a>AOP proxies</h5>
<div class="paragraph">
<p>In some cases a controller may need to be decorated with an AOP proxy at runtime.
One example is if you choose to have <code>@Transactional</code> annotations directly on the
controller. When this is the case, for controllers specifically, we recommend
using class-based proxying. This is typically the default choice with controllers.
However if a controller must implement an interface that is not a Spring Context
callback (e.g. <code>InitializingBean</code>, <code>*Aware</code>, etc), you may need to explicitly
configure class-based proxying. For example with <code>&lt;tx:annotation-driven/&gt;</code>,
change to <code>&lt;tx:annotation-driven proxy-target-class="true"/&gt;</code>.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-requestmapping"><a class="anchor" href="#mvc-ann-requestmapping"></a>1.4.2. Request Mapping</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>@RequestMapping</code> annotation is used to map requests to controllers methods. It has
various attributes to match by URL, HTTP method, request parameters, headers, and media
types. It can be used at the class-level to express shared mappings or at the method level
to narrow down to a specific endpoint mapping.</p>
</div>
<div class="paragraph">
<p>There are also HTTP method specific shortcut variants of <code>@RequestMapping</code>:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>@GetMapping</code></p>
</li>
<li>
<p><code>@PostMapping</code></p>
</li>
<li>
<p><code>@PutMapping</code></p>
</li>
<li>
<p><code>@DeleteMapping</code></p>
</li>
<li>
<p><code>@PatchMapping</code></p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The above are <a href="#mvc-ann-requestmapping-composed">Custom Annotations</a> that are provided out of the box
because arguably most controller methods should be mapped to a specific HTTP method vs
using <code>@RequestMapping</code> which by default matches to all HTTP methods. At the same an
<code>@RequestMapping</code> is still needed at the class level to express shared mappings.</p>
</div>
<div class="paragraph">
<p>Below is an example with type and method level mappings:</p>
</div>


```
@RestController
@RequestMapping("/persons")
class PersonController {

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody Person person) {
        // ...
    }
}
```

<div class="sect4">
<h5 id="mvc-ann-requestmapping-uri-templates"><a class="anchor" href="#mvc-ann-requestmapping-uri-templates"></a>URI patterns</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-uri-templates">Same in Spring
WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can map requests using glob patterns and wildcards:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>?</code> matches one character</p>
</li>
<li>
<p><code>*</code> matches zero or more characters within a path segment</p>
</li>
<li>
<p><code>**</code> match zero or more path segments</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>You can also declare URI variables and access their values with <code>@PathVariable</code>:</p>
</div>


```
@GetMapping("/owners/{ownerId}/pets/{petId}")
public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
    // ...
}
```

<div class="paragraph">
<p>URI variables can be declared at the class and method level:</p>
</div>


```
@Controller
@RequestMapping("/owners/{ownerId}")
public class OwnerController {

    @GetMapping("/pets/{petId}")
    public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
        // ...
    }
}
```

<div class="paragraph">
<p>URI variables are automatically converted to the appropriate type or`TypeMismatchException`
is raised. Simple types&#8201;&#8212;&#8201;<code>int</code>, <code>long</code>, <code>Date</code>, are supported by default and you can
register support for any other data type.
See <a href="#mvc-ann-typeconversion">Type Conversion</a> and <a href="#mvc-ann-initbinder">DataBinder</a>.</p>
</div>
<div class="paragraph">
<p>URI variables can be named explicitly&#8201;&#8212;&#8201;e.g. <code>@PathVariable("customId")</code>, but you can
leave that detail out if the names are the same and your code is compiled with debugging
information or with the <code>-parameters</code> compiler flag on Java 8.</p>
</div>
<div class="paragraph">
<p>The syntax <code>{varName:regex}</code> declares a URI variable with a regular expressions with the
syntax <code>{varName:regex}</code>&#8201;&#8212;&#8201;e.g. given URL <code>"/spring-web-3.0.5 .jar"</code>, the below method
extracts the name, version, and file extension:</p>
</div>

```
@GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
public void handle(@PathVariable String version, @PathVariable String ext) {
    // ...
}
```

<div class="paragraph">
<p>URI path patterns can also have embedded <code>${&#8230;&#8203;}</code> placeholders that are resolved on startup
via <code>PropertyPlaceHolderConfigurer</code> against local, system, environment, and other property
sources. This can be used for example to parameterize a base URL based on some external
configuration.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring MVC uses the <code>PathMatcher</code> contract and the <code>AntPathMatcher</code> implementation from
<code>spring-core</code> for URI path matching.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-pattern-comparison"><a class="anchor" href="#mvc-ann-requestmapping-pattern-comparison"></a>Pattern comparison</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-pattern-comparison">Same in Spring
WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>When multiple patterns match a URL, they must be compared to find the best match. This done
via <code>AntPathMatcher.getPatternComparator(String path)</code> which looks for patterns that more
specific.</p>
</div>
<div class="paragraph">
<p>A pattern is less specific if it has a lower count of URI variables and single wildcards
counted as 1 and double wildcards counted as 2. Given an equal score, the longer pattern is
chosen. Given the same score and length, the pattern with more URI variables than wildcards
is chosen.</p>
</div>
<div class="paragraph">
<p>The default mapping pattern <code>/**</code> is excluded from scoring and always
sorted last. Also prefix patterns such as <code>/public/**</code> are considered less
specific than other pattern that don&#8217;t have double wildcards.</p>
</div>
<div class="paragraph">
<p>For the full details see <code>AntPatternComparator</code> in <code>AntPathMatcher</code> and also keep mind that
the <code>PathMatcher</code> implementation used can be customized. See <a href="#mvc-config-path-matching">Path Matching</a>
in the configuration section.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-suffix-pattern-match"><a class="anchor" href="#mvc-ann-requestmapping-suffix-pattern-match"></a>Suffix match</h5>
<div class="paragraph">
<p>By default Spring MVC performs <code>".*"</code> suffix pattern matching so that a
controller mapped to <code>/person</code> is also implicitly mapped to <code>/person.*</code>.
The file extension is then used to interpret the requested content type to use for
the response (i.e. instead of the "Accept" header), e.g. <code>/person.pdf</code>,
<code>/person.xml</code>, etc.</p>
</div>
<div class="paragraph">
<p>Using file extensions like this was necessary when browsers used to send Accept headers
that were hard to interpret consistently. At present that is no longer a necessity and
using the "Accept" header should be the preferred choice.</p>
</div>
<div class="paragraph">
<p>Over time the use of file name extensions has proven problematic in a variety of ways.
It can cause ambiguity when overlayed with the use of URI variables, path parameters,
URI encoding, and it also makes it difficult to reason about URL-based authorization
and security (see next section for more details).</p>
</div>
<div class="paragraph">
<p>To completely disable the use of file extensions, you must set both of these:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>useSuffixPatternMatching(false)</code>, see <a href="#mvc-config-path-matching">PathMatchConfigurer</a></p>
</li>
<li>
<p><code>favorPathExtension(false)</code>, see <a href="#mvc-config-content-negotiation">ContentNeogiationConfigurer</a></p>
</li>
</ul>
</div>
<div class="paragraph">
<p>URL-based content negotiation can still be useful, for example when typing a URL in a
browser. To enable that we recommend a query parameter based strategy to avoid most of
the issues that come with file extensions. Or if you must use file extensions, consider
restricting them to a list of explicitly registered extensions through the
<code>mediaTypes</code> property of <a href="#mvc-config-content-negotiation">ContentNeogiationConfigurer</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-rfd"><a class="anchor" href="#mvc-ann-requestmapping-rfd"></a>Suffix match and RFD</h5>
<div class="paragraph">
<p>Reflected file download (RFD) attack is similar to XSS in that it relies on request input,
e.g. query parameter, URI variable, being reflected in the response. However instead of
inserting JavaScript into HTML, an RFD attack relies on the browser switching to perform a
download and treating the response as an executable script when double-clicked later.</p>
</div>
<div class="paragraph">
<p>In Spring MVC <code>@ResponseBody</code> and <code>ResponseEntity</code> methods are at risk because
they can render different content types which clients can request via URL path extensions.
Disabling suffix pattern matching and the use of path extensions for content negotiation
lower the risk but are not sufficient to prevent RFD attacks.</p>
</div>
<div class="paragraph">
<p>To prevent RFD attacks, prior to rendering the response body Spring MVC adds a
<code>Content-Disposition:inline;filename=f.txt</code> header to suggest a fixed and safe download
file. This is done only if the URL path contains a file extension that is neither whitelisted
nor explicitly registered for content negotiation purposes. However it may potentially have
side effects when URLs are typed directly into a browser.</p>
</div>
<div class="paragraph">
<p>Many common path extensions are whitelisted by default. Applications with custom
<code>HttpMessageConverter</code> implementations can explicitly register file extensions for content
negotiation to avoid having a <code>Content-Disposition</code> header added for those extensions.
See <a href="#mvc-config-content-negotiation">Content Types</a>.</p>
</div>
<div class="paragraph">
<p>Check <a href="https://pivotal.io/security/cve-2015-5211">CVE-2015-5211</a> for additional
recommendations related to RFD.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-consumes"><a class="anchor" href="#mvc-ann-requestmapping-consumes"></a>Consumable media types</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-consumes">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can narrow the request mapping based on the <code>Content-Type</code> of the request:</p>
</div>

```
@PostMapping(path = "/pets", consumes = "application/json")
public void addPet(@RequestBody Pet pet) {
    // ...
}
```

<div class="paragraph">
<p>The consumes attribute also supports negation expressions&#8201;&#8212;&#8201;e.g. <code>!text/plain</code> means any
content type other than "text/plain".</p>
</div>
<div class="paragraph">
<p>You can declare a shared consumes attribute at the class level. Unlike most other request
mapping attributes however when used at the class level, a method-level consumes attribute
will overrides rather than extend the class level declaration.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>MediaType</code> provides constants for commonly used media types&#8201;&#8212;&#8201;e.g.
<code>APPLICATION_JSON_VALUE</code>, <code>APPLICATION_XML_VALUE</code>.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-produces"><a class="anchor" href="#mvc-ann-requestmapping-produces"></a>Producible media types</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-produces">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can narrow the request mapping based on the <code>Accept</code> request header and the list of
content types that a controller method produces:</p>
</div>

```
@GetMapping(path = "/pets/{petId}", produces = "application/json;charset=UTF-8")
@ResponseBody
public Pet getPet(@PathVariable String petId) {
    // ...
}
```

<div class="paragraph">
<p>The media type can specify a character set. Negated expressions are supported&#8201;&#8212;&#8201;e.g.
<code>!text/plain</code> means any content type other than "text/plain".</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>For JSON content type, the UTF-8 charset should be specified even if
<a href="https://tools.ietf.org/html/rfc7159#section-11">RFC7159</a>
clearly states that "no charset parameter is defined for this registration" because some
browsers require it for interpreting correctly UTF-8 special characters.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>You can declare a shared produces attribute at the class level. Unlike most other request
mapping attributes however when used at the class level, a method-level produces attribute
will overrides rather than extend the class level declaration.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>MediaType</code> provides constants for commonly used media types&#8201;&#8212;&#8201;e.g.
<code>APPLICATION_JSON_UTF8_VALUE</code>, <code>APPLICATION_XML_VALUE</code>.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-params-and-headers"><a class="anchor" href="#mvc-ann-requestmapping-params-and-headers"></a>Parameters, headers</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-params-and-headers">Same in Spring
WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can narrow request mappings based on request parameter conditions. You can test for the
presence of a request parameter (<code>"myParam"</code>), for the absence (<code>"!myParam"</code>), or for a
specific value (<code>"myParam=myValue"</code>):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@GetMapping</span>(path = <span class="string"><span class="delimiter">&quot;</span><span class="content">/pets/{petId}</span><span class="delimiter">&quot;</span></span>, <strong>params = <span class="string"><span class="delimiter">&quot;</span><span class="content">myParam=myValue</span><span class="delimiter">&quot;</span></span></strong>)
<span class="directive">public</span> <span class="type">void</span> findPet(<span class="annotation">@PathVariable</span> <span class="predefined-type">String</span> petId) {
    <span class="comment">// ...</span>
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>You can also use the same with request header conditions:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@GetMapping</span>(path = <span class="string"><span class="delimiter">&quot;</span><span class="content">/pets</span><span class="delimiter">&quot;</span></span>, <strong>headers = <span class="string"><span class="delimiter">&quot;</span><span class="content">myHeader=myValue</span><span class="delimiter">&quot;</span></span></strong>)
<span class="directive">public</span> <span class="type">void</span> findPet(<span class="annotation">@PathVariable</span> <span class="predefined-type">String</span> petId) {
    <span class="comment">// ...</span>
}</code></pre>
</div>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>You can match <code>Content-Type</code> and <code>Accept</code> with the headers condition but it is better to use
<a href="#mvc-ann-requestmapping-consumes">consumes</a> and <a href="#mvc-ann-requestmapping-produces">produces</a>
instead.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-head-options"><a class="anchor" href="#mvc-ann-requestmapping-head-options"></a>HTTP HEAD, OPTIONS</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestmapping-head-options">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@GetMapping</code>&#8201;&#8212;&#8201;and also <code>@RequestMapping(method=HttpMethod.GET)</code>, support HTTP HEAD
transparently for request mapping purposes. Controller methods don&#8217;t need to change.
A response wrapper, applied in <code>javax.servlet.http.HttpServlet</code>, ensures a <code>"Content-Length"</code>
header is set to the number of bytes written and without actually writing to the response.</p>
</div>
<div class="paragraph">
<p><code>@GetMapping</code>&#8201;&#8212;&#8201;and also <code>@RequestMapping(method=HttpMethod.GET)</code>, are implicitly mapped to
and also support HTTP HEAD. An HTTP HEAD request is processed as if it were HTTP GET except
but instead of writing the body, the number of bytes are counted and the "Content-Length"
header set.</p>
</div>
<div class="paragraph">
<p>By default HTTP OPTIONS is handled by setting the "Allow" response header to the list of HTTP
methods listed in all <code>@RequestMapping</code> methods with matching URL patterns.</p>
</div>
<div class="paragraph">
<p>For a <code>@RequestMapping</code> without HTTP method declarations, the "Allow" header is set to
<code>"GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS"</code>. Controller methods should always declare the
supported HTTP methods for example by using the HTTP method specific variants&#8201;&#8212;&#8201;<code>@GetMapping</code>, <code>@PostMapping</code>, etc.</p>
</div>
<div class="paragraph">
<p><code>@RequestMapping</code> method can be explicitly mapped to HTTP HEAD and HTTP OPTIONS, but that
is not necessary in the common case.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestmapping-composed"><a class="anchor" href="#mvc-ann-requestmapping-composed"></a>Custom Annotations</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#mvc-ann-requestmapping-head-options">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC supports the use of <a href="core.html#beans-meta-annotations">composed annotations</a>
for request mapping. Those are annotations that are themselves meta-annotated with
<code>@RequestMapping</code> and composed to redeclare a subset (or all) of the <code>@RequestMapping</code>
attributes with a narrower, more specific purpose.</p>
</div>
<div class="paragraph">
<p><code>@GetMapping</code>, <code>@PostMapping</code>, <code>@PutMapping</code>, <code>@DeleteMapping</code>, and <code>@PatchMapping</code> are
examples of composed annotations. They&#8217;re provided out of the box because arguably most
controller methods should be mapped to a specific HTTP method vs using <code>@RequestMapping</code>
which by default matches to all HTTP methods. If you need an example of composed
annotations, look at how those are declared.</p>
</div>
<div class="paragraph">
<p>Spring MVC also supports custom request mapping attributes with custom request matching
logic. This is a more advanced option that requires sub-classing
<code>RequestMappingHandlerMapping</code> and overriding the <code>getCustomMethodCondition</code> method where
you can check the custom attribute and return your own <code>RequestCondition</code>.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-methods"><a class="anchor" href="#mvc-ann-methods"></a>1.4.3. Handler Methods</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-methods">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@RequestMapping</code> handler methods have a flexible signature and can choose from a range of
supported controller method arguments and return values.</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-arguments"><a class="anchor" href="#mvc-ann-arguments"></a>Method Arguments</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-arguments">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The table below shows supported controller method arguments. Reactive types are not supported
for any arguments.</p>
</div>
<div class="paragraph">
<p>JDK 8&#8217;s <code>java.util.Optional</code> is supported as a method argument in combination with
annotations that have a <code>required</code> attribute&#8201;&#8212;&#8201;e.g. <code>@RequestParam</code>, <code>@RequestHeader</code>,
etc, and is equivalent to <code>required=false</code>.</p>
</div>
<table class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Controller method argument</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>WebRequest</code>, <code>NativeWebRequest</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Generic access to request parameters, request &amp; session attributes, without direct
use of the Servlet API.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>javax.servlet.ServletRequest</code>, <code>javax.servlet.ServletResponse</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Choose any specific request or response type&#8201;&#8212;&#8201;e.g. <code>ServletRequest</code>, <code>HttpServletRequest</code>,
or Spring&#8217;s <code>MultipartRequest</code>, <code>MultipartHttpServletRequest</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>javax.servlet.http.HttpSession</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Enforces the presence of a session. As a consequence, such an argument is never <code>null</code>.<br>
<strong>Note:</strong> Session access is not thread-safe. Consider setting the
<code>RequestMappingHandlerAdapter</code>'s "synchronizeOnSession" flag to "true" if multiple
requests are allowed to access a session concurrently.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>javax.servlet.http.PushBuilder</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Servlet 4.0 push builder API for programmatic HTTP/2 resource pushes.
Note that per Servlet spec, the injected <code>PushBuilder</code> instance can be null if the client
does not support that HTTP/2 feature.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.security.Principal</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Currently authenticated user; possibly a specific <code>Principal</code> implementation class if known.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpMethod</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The HTTP method of the request.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Locale</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The current request locale, determined by the most specific <code>LocaleResolver</code> available, in
effect, the configured <code>LocaleResolver</code>/<code>LocaleContextResolver</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.TimeZone</code> + <code>java.time.ZoneId</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The time zone associated with the current request, as determined by a <code>LocaleContextResolver</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.io.InputStream</code>, <code>java.io.Reader</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the raw request body as exposed by the Servlet API.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.io.OutputStream</code>, <code>java.io.Writer</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the raw response body as exposed by the Servlet API.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@PathVariable</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to URI template variables. See <a href="#mvc-ann-requestmapping-uri-templates">URI patterns</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@MatrixVariable</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to name-value pairs in URI path segments. See <a href="#mvc-ann-matrix-variables">Matrix variables</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestParam</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to Servlet request parameters. Parameter values are converted to the declared
method argument type. See <a href="#mvc-ann-requestparam">@RequestParam</a>.</p>
<p class="tableblock"> Note that use of <code>@RequestParam</code> is optional, e.g. to set its attributes.
See "Any other argument" further below in this table.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestHeader</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to request headers. Header values are converted to the declared method argument
type. See <a href="#mvc-ann-requestheader">@RequestHeader</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@CookieValue</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to cookies. Cookies values are converted to the declared method argument
type. See <a href="#mvc-ann-cookievalue">@CookieValue</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestBody</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the HTTP request body. Body content is converted to the declared method
argument type using <code>HttpMessageConverter</code>s. See <a href="#mvc-ann-requestbody">@RequestBody</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpEntity&lt;B&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to request headers and body. The body is converted with <code>HttpMessageConverter</code>s.
See <a href="#mvc-ann-httpentity">HttpEntity</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestPart</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to a part in a "multipart/form-data" request.
See <a href="#mvc-multipart-forms">Multipart</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Map</code>, <code>org.springframework.ui.Model</code>, <code>org.springframework.ui.ModelMap</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the model that is used in HTML controllers and exposed to templates as
part of view rendering.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>RedirectAttributes</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Specify attributes to use in case of a redirect&#8201;&#8212;&#8201;i.e. to be appended to the query
string, and/or flash attributes to be stored temporarily until the request after redirect.
See <a href="#mvc-redirecting-passing-data">Redirect attributes</a> and <a href="#mvc-flash-attributes">Flash attributes</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@ModelAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to an existing attribute in the model (instantiated if not present) with
data binding and validation applied. See <a href="#mvc-ann-modelattrib-method-args">@ModelAttribute</a> as well as
<a href="#mvc-ann-modelattrib-methods">Model</a> and <a href="#mvc-ann-initbinder">DataBinder</a>.</p>
<p class="tableblock"> Note that use of <code>@ModelAttribute</code> is optional, e.g. to set its attributes.
See "Any other argument" further below in this table.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Errors</code>, <code>BindingResult</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to errors from validation and data binding for a command object
(i.e. <code>@ModelAttribute</code> argument), or errors from the validation of an <code>@RequestBody</code> or
<code>@RequestPart</code> arguments; an <code>Errors</code>, or <code>BindingResult</code> argument must be declared
immediately after the validated method argument.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>SessionStatus</code> + class-level <code>@SessionAttributes</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For marking form processing complete which triggers cleanup of session attributes
declared through a class-level <code>@SessionAttributes</code> annotation. See
<a href="#mvc-ann-sessionattributes">@SessionAttributes</a> for more details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>UriComponentsBuilder</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For preparing a URL relative to the current request&#8217;s host, port, scheme, context path, and
the literal part of the servlet mapping also taking into account <code>Forwarded</code> and
<code>X-Forwarded-*</code> headers. See <a href="#mvc-uri-building">URI Links</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@SessionAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to any session attribute; in contrast to model attributes stored in the session
as a result of a class-level <code>@SessionAttributes</code> declaration. See
<a href="#mvc-ann-sessionattribute">@SessionAttribute</a> for more details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to request attributes. See <a href="#mvc-ann-requestattrib">@RequestAttribute</a> for more details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">Any other argument</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">If a method argument is not matched to any of the above, by default it is resolved as
an <code>@RequestParam</code> if it is a simple type, as determined by
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-">BeanUtils#isSimpleProperty</a>,
or as an <code>@ModelAttribute</code> otherwise.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect4">
<h5 id="mvc-ann-return-types"><a class="anchor" href="#mvc-ann-return-types"></a>Return Values</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-return-types">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The table below shows supported controller method return values. Reactive types are
supported for all return values, see below for more details.</p>
</div>
<table class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Controller method return value</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@ResponseBody</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The return value is converted through <code>HttpMessageConverter</code>s and written to the
response. See <a href="#mvc-ann-responsebody">@ResponseBody</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpEntity&lt;B&gt;</code>, <code>ResponseEntity&lt;B&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The return value specifies the full response including HTTP headers and body be converted
through <code>HttpMessageConverter</code>s and written to the response.
See <a href="#mvc-ann-responseentity">ResponseEntity</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpHeaders</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For returning a response with headers and no body.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>String</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A view name to be resolved with <code>ViewResolver</code>'s and used together with the implicit
model&#8201;&#8212;&#8201;determined through command objects and <code>@ModelAttribute</code> methods. The handler
method may also programmatically enrich the model by declaring a <code>Model</code> argument
(see above).</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>View</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A <code>View</code> instance to use for rendering together with the implicit model&#8201;&#8212;&#8201;determined
through command objects and <code>@ModelAttribute</code> methods. The handler method may also
programmatically enrich the model by declaring a <code>Model</code> argument (see above).</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Map</code>, <code>org.springframework.ui.Model</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Attributes to be added to the implicit model with the view name implicitly determined
through a <code>RequestToViewNameTranslator</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@ModelAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">An attribute to be added to the model with the view name implicitly determined through
a <code>RequestToViewNameTranslator</code>.</p>
<p class="tableblock"> Note that <code>@ModelAttribute</code> is optional. See "Any other return value" further below in
this table.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ModelAndView</code> object</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The view and model attributes to use, and optionally a response status.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>void</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A method with a <code>void</code> return type (or <code>null</code> return value) is considered to have fully
handled the response if it also has a <code>ServletResponse</code>, or an <code>OutputStream</code> argument, or
an <code>@ResponseStatus</code> annotation. The same is true also if the controller has made a positive
ETag or lastModified timestamp check (see <a href="#mvc-caching-etag-lastmodified">Controllers</a> for details).</p>
<p class="tableblock"> If none of the above is true, a <code>void</code> return type may also indicate "no response body" for
REST controllers, or default view name selection for HTML controllers.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>DeferredResult&lt;V&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Produce any of the above return values asynchronously from any thread&#8201;&#8212;&#8201;e.g. possibly as a
result of some event or callback. See <a href="#mvc-ann-async">Async Requests</a> and
<a href="#mvc-ann-async-deferredresult"><code>DeferredResult</code></a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Callable&lt;V&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Produce any of the above return values asynchronously in a Spring MVC managed thread.
See <a href="#mvc-ann-async">Async Requests</a> and <a href="#mvc-ann-async-callable"><code>Callable</code></a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ListenableFuture&lt;V&gt;</code>,
<code>java.util.concurrent.CompletionStage&lt;V&gt;</code>,
 <code>java.util.concurrent.CompletableFuture&lt;V&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Alternative to <code>DeferredResult</code> as a convenience for example when an underlying service
returns one of those.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ResponseBodyEmitter</code>, <code>SseEmitter</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Emit a stream of objects asynchronously to be written to the response with
<code>HttpMessageConverter</code>'s; also supported as the body of a <code>ResponseEntity</code>.
See <a href="#mvc-ann-async">Async Requests</a> and <a href="#mvc-ann-async-http-streaming">HTTP Streaming</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>StreamingResponseBody</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Write to the response <code>OutputStream</code> asynchronously; also supported as the body of a
<code>ResponseEntity</code>. See <a href="#mvc-ann-async">Async Requests</a> and <a href="#mvc-ann-async-http-streaming">HTTP Streaming</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">Reactive types&#8201;&#8212;&#8201;Reactor, RxJava, or others via <code>ReactiveAdapterRegistry</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Alternative to <code>DeferredResult</code> with multi-value streams (e.g. <code>Flux</code>, <code>Observable</code>)
collected to a <code>List</code>.</p>
<p class="tableblock"> For streaming scenarios&#8201;&#8212;&#8201;e.g. <code>text/event-stream</code>, <code>application/json+stream</code>&#8201;&#8212;&#8201; <code>SseEmitter</code> and <code>ResponseBodyEmitter</code> are used instead, where <code>ServletOutputStream</code>
blocking I/O is performed on a Spring MVC managed thread and back pressure applied
against the completion of each write.</p>
<p class="tableblock"> See <a href="#mvc-ann-async">Async Requests</a> and <a href="#mvc-ann-async-reactive-types">Reactive types</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">Any other return value</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">If a return value is not matched to any of the above, by default it is treated as a view
name, if it is <code>String</code> or <code>void</code> (default view name selection via
<code>RequestToViewNameTranslator</code> applies); or as a model attribute to be added to the model,
unless it is a simple type, as determined by
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-">BeanUtils#isSimpleProperty</a>
in which case it remains unresolved.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect4">
<h5 id="mvc-ann-typeconversion"><a class="anchor" href="#mvc-ann-typeconversion"></a>Type Conversion</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-typeconversion">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Some annotated controller method arguments that represent String-based request input&#8201;&#8212;&#8201;e.g.
<code>@RequestParam</code>, <code>@RequestHeader</code>, <code>@PathVariable</code>, <code>@MatrixVariable</code>, and <code>@CookieValue</code>,
may require type conversion if the argument is declared as something other than <code>String</code>.</p>
</div>
<div class="paragraph">
<p>For such cases type conversion is automatically applied based on the configured converters.
By default simple types such as <code>int</code>, <code>long</code>, <code>Date</code>, etc. are supported. Type conversion
can be customized through a <code>WebDataBinder</code>, see <a href="#mvc-ann-initbinder">DataBinder</a>, or by registering
<code>Formatters</code> with the <code>FormattingConversionService</code>, see
<a href="core.html#format">Spring Field Formatting</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-matrix-variables"><a class="anchor" href="#mvc-ann-matrix-variables"></a>Matrix variables</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-matrix-variables">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986</a> discusses name-value pairs in
path segments. In Spring MVC we refer to those as "matrix variables" based on an
<a href="https://www.w3.org/DesignIssues/MatrixURIs.html">"old post"</a> by Tim Berners-Lee but they
can be also be referred to as URI path parameters.</p>
</div>
<div class="paragraph">
<p>Matrix variables can appear in any path segment, each variable separated by semicolon and
multiple values separated by comma, e.g. <code>"/cars;color=red,green;year=2012"</code>. Multiple
values can also be specified through repeated variable names, e.g.
<code>"color=red;color=green;color=blue"</code>.</p>
</div>
<div class="paragraph">
<p>If a URL is expected to contain matrix variables, the request mapping for a controller
method must use a URI variable to mask that variable content and ensure the request can
be matched successfully independent of matrix variable order and presence.
Below is an example:</p>
</div>

```
// GET /pets/42;q=11;r=22

@GetMapping("/pets/{petId}")
public void findPet(@PathVariable String petId, @MatrixVariable int q) {

    // petId == 42
    // q == 11
}
```

<div class="paragraph">
<p>Given that all path segments may contain matrix variables, sometimes you may need to
disambiguate which path variable the matrix variable is expected to be in.
For example:</p>
</div>

```
// GET /owners/42;q=11/pets/21;q=22

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable(name="q", pathVar="ownerId") int q1,
        @MatrixVariable(name="q", pathVar="petId") int q2) {

    // q1 == 11
    // q2 == 22
}
```

<div class="paragraph">
<p>A matrix variable may be defined as optional and a default value specified:</p>
</div>

```
// GET /pets/42

@GetMapping("/pets/{petId}")
public void findPet(@MatrixVariable(required=false, defaultValue="1") int q) {

    // q == 1
}
```

<div class="paragraph">
<p>To get all matrix variables, use a <code>MultiValueMap</code>:</p>
</div>

```
// GET /owners/42;q=11;r=12/pets/21;q=22;s=23

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable MultiValueMap<String, String> matrixVars,
        @MatrixVariable(pathVar="petId") MultiValueMap<String, String> petMatrixVars) {

    // matrixVars: ["q" : [11,22], "r" : 12, "s" : 23]
    // petMatrixVars: ["q" : 22, "s" : 23]
}
```

</div>
<div class="paragraph">
<p>Note that you need to enable the use of matrix variables. In the MVC Java config you need
to set a <code>UrlPathHelper</code> with <code>removeSemicolonContent=false</code> via
<a href="#mvc-config-path-matching">Path Matching</a>. In the MVC XML namespace, use
<code>&lt;mvc:annotation-driven enable-matrix-variables="true"/&gt;</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestparam"><a class="anchor" href="#mvc-ann-requestparam"></a>@RequestParam</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestparam">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@RequestParam</code> annotation to bind Servlet request parameters (i.e. query
parameters or form data) to a method argument in a controller.</p>
</div>
<div class="paragraph">
<p>The following code snippet shows the usage:</p>
</div>

```
@Controller
@RequestMapping("/pets")
public class EditPetForm {

    // ...

    @GetMapping
    public String setupForm(@RequestParam("petId") int petId, Model model) {
        Pet pet = this.clinic.loadPet(petId);
        model.addAttribute("pet", pet);
        return "petForm";
    }

    // ...

}
```

<div class="paragraph">
<p>Method parameters using this annotation are required by default, but you can specify that
a method parameter is optional by setting <code>@RequestParam</code>'s <code>required</code> flag to <code>false</code>
or by declaring the argument with an <code>java.util.Optional</code> wrapper.</p>
</div>
<div class="paragraph">
<p>Type conversion is applied automatically if the target method parameter type is not
<code>String</code>. See <a href="#mvc-ann-typeconversion">Type Conversion</a>.</p>
</div>
<div class="paragraph">
<p>When an <code>@RequestParam</code> annotation is declared as <code>Map&lt;String, String&gt;</code> or
<code>MultiValueMap&lt;String, String&gt;</code> argument, the map is populated with all request
parameters.</p>
</div>
<div class="paragraph">
<p>Note that use of <code>@RequestParam</code> is optional, e.g. to set its attributes.
By default any argument that is a simple value type, as determined by
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-">BeanUtils#isSimpleProperty</a>,
and is not resolved by any other argument resolver, is treated as if it was annotated
with <code>@RequestParam</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestheader"><a class="anchor" href="#mvc-ann-requestheader"></a>@RequestHeader</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestheader">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@RequestHeader</code> annotation to bind a request header to a method argument in a
controller.</p>
</div>
<div class="paragraph">
<p>Given request with headers:</p>
</div>
<div class="literalblock">
<div class="content">
<pre>Host                    localhost:8080
Accept                  text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language         fr,en-gb;q=0.7,en;q=0.3
Accept-Encoding         gzip,deflate
Accept-Charset          ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive              300</pre>
</div>
</div>
<div class="paragraph">
<p>The following gets the value of the <code>Accept-Encoding</code> and <code>Keep-Alive</code> headers:</p>
</div>

```
@GetMapping("/demo")
public void handle(
        @RequestHeader("Accept-Encoding") String encoding,
        @RequestHeader("Keep-Alive") long keepAlive) {
    //...
}
```

<div class="paragraph">
<p>Type conversion is applied automatically if the target method parameter type is not
<code>String</code>. See <a href="#mvc-ann-typeconversion">Type Conversion</a>.</p>
</div>
<div class="paragraph">
<p>When an <code>@RequestHeader</code> annotation is used on a <code>Map&lt;String, String&gt;</code>,
<code>MultiValueMap&lt;String, String&gt;</code>, or <code>HttpHeaders</code> argument, the map is populated
with all header values.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Built-in support is available for converting a comma-separated string into an
array/collection of strings or other types known to the type conversion system. For
example a method parameter annotated with <code>@RequestHeader("Accept")</code> may be of type
<code>String</code> but also <code>String[]</code> or <code>List&lt;String&gt;</code>.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-cookievalue"><a class="anchor" href="#mvc-ann-cookievalue"></a>@CookieValue</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-cookievalue">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@CookieValue</code> annotation to bind the value of an HTTP cookie to a method argument
in a controller.</p>
</div>
<div class="paragraph">
<p>Given request with the following cookie:</p>
</div>
<div class="literalblock">
<div class="content">
<pre>JSESSIONID=415A4AC178C59DACE0B2C9CA727CDD84</pre>
</div>
</div>
<div class="paragraph">
<p>The following code sample demonstrates how to get the cookie value:</p>
</div>

```
@GetMapping("/demo")
public void handle(@CookieValue("JSESSIONID") String cookie) {
    //...
}
```

<div class="paragraph">
<p>Type conversion is applied automatically if the target method parameter type is not
<code>String</code>. See <a href="#mvc-ann-typeconversion">Type Conversion</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-modelattrib-method-args"><a class="anchor" href="#mvc-ann-modelattrib-method-args"></a>@ModelAttribute</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-modelattrib-method-args">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@ModelAttribute</code> annotation on a method argument to access an attribute from the
model, or have it instantiated if not present. The model attribute is also overlaid with
values from HTTP Servlet request parameters whose names match to field names. This is
referred to as data binding and it saves you from having to deal with parsing and
converting individual query parameters and form fields. For example:</p>
</div>

```
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute Pet pet) { }
```

<div class="paragraph">
<p>The <code>Pet</code> instance above is resolved as follows:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>From the model if already added via <a href="#mvc-ann-modelattrib-methods">Model</a>.</p>
</li>
<li>
<p>From the HTTP session via <a href="#mvc-ann-sessionattributes">@SessionAttributes</a>.</p>
</li>
<li>
<p>From a URI path variable passed through a <code>Converter</code> (example below).</p>
</li>
<li>
<p>From the invocation of a default constructor.</p>
</li>
<li>
<p>From the invocation of a "primary constructor" with arguments matching to Servlet
request parameters; argument names are determined via JavaBeans
<code>@ConstructorProperties</code> or via runtime-retained parameter names in the bytecode.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>While it is common to use a <a href="#mvc-ann-modelattrib-methods">Model</a> to populate the model with
attributes, one other alternative is to rely on a <code>Converter&lt;String, T&gt;</code> in combination
with a URI path variable convention. In the example below the model attribute name
"account" matches the URI path variable "account" and the <code>Account</code> is loaded by passing
the <code>String</code> account number through a registered <code>Converter&lt;String, Account&gt;</code>:</p>
</div>

```
@PutMapping("/accounts/{account}")
public String save(@ModelAttribute("account") Account account) {
    // ...
}
```

<div class="paragraph">
<p>After the model attribute instance is obtained, data binding is applied. The
<code>WebDataBinder</code> class matches Servlet request parameter names (query parameters and form
fields) to field names on the target Object. Matching fields are populated after type
conversion is applied where necessary. For more on data binding (and validation) see
<a href="core.html#validation">Validation</a>. For more on customizing data binding see
<a href="#mvc-ann-initbinder">DataBinder</a>.</p>
</div>
<div class="paragraph">
<p>Data binding may result in errors. By default a <code>BindException</code> is raised but to check
for such errors in the controller method, add a <code>BindingResult</code> argument immediately next
to the <code>@ModelAttribute</code> as shown below:</p>
</div>

```
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute("pet") Pet pet, BindingResult result) {
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```

<div class="paragraph">
<p>In some cases you may want access to a model attribute without data binding. For such
cases you can inject the <code>Model</code> into the controller and access it directly or
alternatively set <code>@ModelAttribute(binding=false)</code> as shown below:</p>
</div>

```
@ModelAttribute
public AccountForm setUpForm() {
    return new AccountForm();
}

@ModelAttribute
public Account findAccount(@PathVariable String accountId) {
    return accountRepository.findOne(accountId);
}

@PostMapping("update")
public String update(@Valid AccountUpdateForm form, BindingResult result,
        @ModelAttribute(binding=false) Account account) {
    // ...
}
```

<div class="paragraph">
<p>Validation can be applied automatically after data binding by adding the
<code>javax.validation.Valid</code> annotation or Spring&#8217;s <code>@Validated</code> annotation (also see
<a href="core.html#validation-beanvalidation">Bean validation</a> and
<a href="core.html#validation">Spring validation</a>). For example:</p>
</div>

```
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@Valid @ModelAttribute("pet") Pet pet, BindingResult result) {
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```

<div class="paragraph">
<p>Note that use of <code>@ModelAttribute</code> is optional, e.g. to set its attributes.
By default any argument that is not a simple value type, as determined by
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-">BeanUtils#isSimpleProperty</a>,
and is not resolved by any other argument resolver, is treated as if it was annotated
with <code>@ModelAttribute</code>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-sessionattributes"><a class="anchor" href="#mvc-ann-sessionattributes"></a>@SessionAttributes</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-sessionattributes">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@SessionAttributes</code> is used to store model attributes in the HTTP Servlet session between
requests. It is a type-level annotation that declares session attributes used by a
specific controller. This will typically list the names of model attributes or types of
model attributes which should be transparently stored in the session for subsequent
requests to access.</p>
</div>
<div class="paragraph">
<p>For example:</p>
</div>

```
@Controller
@SessionAttributes("pet")
public class EditPetForm {
    // ...
}
```

<div class="paragraph">
<p>On the first request when a model attribute with the name "pet" is added to the model,
it is automatically promoted to and saved in the HTTP Servlet session. It remains there
until another controller method uses a <code>SessionStatus</code> method argument to clear the
storage:</p>
</div>

```
@Controller
@SessionAttributes("pet")
public class EditPetForm {

    // ...

    @PostMapping("/pets/{id}")
    public String handle(Pet pet, BindingResult errors, SessionStatus status) {
        if (errors.hasErrors) {
            // ...
        }
            status.setComplete();
            // ...
        }
    }
}
```

<div class="sect4">
<h5 id="mvc-ann-sessionattribute"><a class="anchor" href="#mvc-ann-sessionattribute"></a>@SessionAttribute</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-sessionattribute">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>If you need access to pre-existing session attributes that are managed globally,
i.e. outside the controller (e.g. by a filter), and may or may not be present
use the <code>@SessionAttribute</code> annotation on a method parameter:</p>
</div>

```
@RequestMapping("/")
public String handle(@SessionAttribute User user) {
    // ...
}
```

<div class="paragraph">
<p>For use cases that require adding or removing session attributes consider injecting
<code>org.springframework.web.context.request.WebRequest</code> or
<code>javax.servlet.http.HttpSession</code> into the controller method.</p>
</div>
<div class="paragraph">
<p>For temporary storage of model attributes in the session as part of a controller
workflow consider using <code>SessionAttributes</code> as described in
<a href="#mvc-ann-sessionattributes">@SessionAttributes</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-requestattrib"><a class="anchor" href="#mvc-ann-requestattrib"></a>@RequestAttribute</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestattrib">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Similar to <code>@SessionAttribute</code> the <code>@RequestAttribute</code> annotation can be used to
access pre-existing request attributes created earlier, e.g. by a Servlet <code>Filter</code>
or <code>HandlerInterceptor</code>:</p>
</div>

```
@GetMapping("/")
public String handle(@RequestAttribute Client client) {
    // ...
}
```

<div class="sect4">
<h5 id="mvc-redirecting-passing-data"><a class="anchor" href="#mvc-redirecting-passing-data"></a>Redirect attributes</h5>
<div class="paragraph">
<p>By default all model attributes are considered to be exposed as URI template variables in
the redirect URL. Of the remaining attributes those that are primitive types or
collections/arrays of primitive types are automatically appended as query parameters.</p>
</div>
<div class="paragraph">
<p>Appending primitive type attributes as query parameters may be the desired result if a
model instance was prepared specifically for the redirect. However, in annotated
controllers the model may contain additional attributes added for rendering purposes (e.g.
drop-down field values). To avoid the possibility of having such attributes appear in the
URL, an <code>@RequestMapping</code> method can declare an argument of type <code>RedirectAttributes</code> and
use it to specify the exact attributes to make available to <code>RedirectView</code>. If the method
does redirect, the content of <code>RedirectAttributes</code> is used. Otherwise the content of the
model is used.</p>
</div>
<div class="paragraph">
<p>The <code>RequestMappingHandlerAdapter</code> provides a flag called
<code>"ignoreDefaultModelOnRedirect"</code> that can be used to indicate the content of the default
<code>Model</code> should never be used if a controller method redirects. Instead the controller
method should declare an attribute of type <code>RedirectAttributes</code> or if it doesn&#8217;t do so
no attributes should be passed on to <code>RedirectView</code>. Both the MVC namespace and the MVC
Java config keep this flag set to <code>false</code> in order to maintain backwards compatibility.
However, for new applications we recommend setting it to <code>true</code></p>
</div>
<div class="paragraph">
<p>Note that URI template variables from the present request are automatically made
available when expanding a redirect URL and do not need to be added explicitly neither
through <code>Model</code> nor <code>RedirectAttributes</code>. For example:</p>
</div>

```
@PostMapping("/files/{path}")
public String upload(...) {
    // ...
    return "redirect:files/{path}";
}
```

<div class="paragraph">
<p>Another way of passing data to the redirect target is via <em>Flash Attributes</em>. Unlike
other redirect attributes, flash attributes are saved in the HTTP session (and hence do
not appear in the URL). See <a href="#mvc-flash-attributes">Flash attributes</a> for more information.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-flash-attributes"><a class="anchor" href="#mvc-flash-attributes"></a>Flash attributes</h5>
<div class="paragraph">
<p>Flash attributes provide a way for one request to store attributes intended for use in
another. This is most commonly needed when redirecting&#8201;&#8212;&#8201;for example, the
<em>Post/Redirect/Get</em> pattern. Flash attributes are saved temporarily before the
redirect (typically in the session) to be made available to the request after the
redirect and removed immediately.</p>
</div>
<div class="paragraph">
<p>Spring MVC has two main abstractions in support of flash attributes. <code>FlashMap</code> is used
to hold flash attributes while <code>FlashMapManager</code> is used to store, retrieve, and manage
<code>FlashMap</code> instances.</p>
</div>
<div class="paragraph">
<p>Flash attribute support is always "on" and does not need to enabled explicitly although
if not used, it never causes HTTP session creation. On each request there is an "input"
<code>FlashMap</code> with attributes passed from a previous request (if any) and an "output"
<code>FlashMap</code> with attributes to save for a subsequent request. Both <code>FlashMap</code> instances
are accessible from anywhere in Spring MVC through static methods in
<code>RequestContextUtils</code>.</p>
</div>
<div class="paragraph">
<p>Annotated controllers typically do not need to work with <code>FlashMap</code> directly. Instead an
<code>@RequestMapping</code> method can accept an argument of type <code>RedirectAttributes</code> and use it
to add flash attributes for a redirect scenario. Flash attributes added via
<code>RedirectAttributes</code> are automatically propagated to the "output" FlashMap. Similarly,
after the redirect, attributes from the "input" <code>FlashMap</code> are automatically added to the
<code>Model</code> of the controller serving the target URL.</p>
</div>
<div class="sidebarblock">
<div class="content">
<div class="title">Matching requests to flash attributes</div>
<div class="paragraph">
<p>The concept of flash attributes exists in many other Web frameworks and has proven to be
exposed sometimes to concurrency issues. This is because by definition flash attributes
are to be stored until the next request. However the very "next" request may not be the
intended recipient but another asynchronous request (e.g. polling or resource requests)
in which case the flash attributes are removed too early.</p>
</div>
<div class="paragraph">
<p>To reduce the possibility of such issues, <code>RedirectView</code> automatically "stamps"
<code>FlashMap</code> instances with the path and query parameters of the target redirect URL. In
turn the default <code>FlashMapManager</code> matches that information to incoming requests when
looking up the "input" <code>FlashMap</code>.</p>
</div>
<div class="paragraph">
<p>This does not eliminate the possibility of a concurrency issue entirely but nevertheless
reduces it greatly with information that is already available in the redirect URL.
Therefore the use of flash attributes is recommended mainly for redirect scenarios .</p>
</div>
</div>
</div>
</div>
<div class="sect4">
<h5 id="mvc-multipart-forms"><a class="anchor" href="#mvc-multipart-forms"></a>Multipart</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-multipart-forms">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>After a <code>MultipartResolver</code> has been <a href="#mvc-multipart">enabled</a>, the content of POST
requests with "multipart/form-data" is parsed and accessible as regular request
parameters. In the example below we access one regular form field and one uploaded
file:</p>
</div>


```
@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }

        return "redirect:uploadFailure";
    }

}
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When using Servlet 3.0 multipart parsing you can also use <code>javax.servlet.http.Part</code> as
a method argument instead of Spring&#8217;s <code>MultipartFile</code>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>Multipart content can also be used as part of data binding to a
<a href="#mvc-ann-modelattrib-method-args">command object</a>. For example the above form field
and file could have been fields on a form object:</p>
</div>


```
class MyForm {

    private String name;

    private MultipartFile file;

    // ...

}

@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(MyForm form, BindingResult errors) {

        if (!form.getFile().isEmpty()) {
            byte[] bytes = form.getFile().getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }

        return "redirect:uploadFailure";
    }

}
```

<div class="paragraph">
<p>Multipart requests can also be submitted from non-browser clients in a RESTful service
scenario. For example a file along with JSON:</p>
</div>
<div class="literalblock">
<div class="content">
<pre>POST /someUrl
Content-Type: multipart/mixed

--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="meta-data"
Content-Type: application/json; charset=UTF-8
Content-Transfer-Encoding: 8bit

{
    "name": "value"
}
--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="file-data"; filename="file.properties"
Content-Type: text/xml
Content-Transfer-Encoding: 8bit
... File Data ...</pre>
</div>
</div>
<div class="paragraph">
<p>You can access the "meta-data" part with <code>@RequestParam</code> as a <code>String</code> but you&#8217;ll
probably want it deserialized from JSON (similar to <code>@RequestBody</code>). Use the
<code>@RequestPart</code> annotation to access a multipart after converting it with an
<a href="integration.html#rest-message-conversion">HttpMessageConverter</a>:</p>
</div>

```
@PostMapping("/")
public String handle(@RequestPart("meta-data") MetaData metadata,
        @RequestPart("file-data") MultipartFile file) {
    // ...
}
```

<div class="paragraph">
<p><code>@RequestPart</code> can be used in combination with <code>javax.validation.Valid</code>, or Spring&#8217;s
<code>@Validated</code> annotation, which causes Standard Bean Validation to be applied.
By default validation errors cause a <code>MethodArgumentNotValidException</code> which is turned
into a 400 (BAD_REQUEST) response. Alternatively validation errors can be handled locally
within the controller through an <code>Errors</code> or <code>BindingResult</code> argument:</p>
</div>

```
@PostMapping("/")
public String handle(@Valid @RequestPart("meta-data") MetaData metadata,
        BindingResult result) {
    // ...
}
```

<div class="sect4">
<h5 id="mvc-ann-requestbody"><a class="anchor" href="#mvc-ann-requestbody"></a>@RequestBody</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-requestbody">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@RequestBody</code> annotation to have the request body read and deserialized into an
Object through an <a href="integration.html#rest-message-conversion">HttpMessageConverter</a>.
Below is an example with an <code>@RequestBody</code> argument:</p>
</div>

```
@PostMapping("/accounts")
public void handle(@Valid @RequestBody Account account, BindingResult result) {
    // ...
}
```

<div class="paragraph">
<p>You can use the <a href="#mvc-config-message-converters">Message Converters</a> option of the <a href="#mvc-config">MVC Config</a> to
configure or customize message conversion.</p>
</div>
<div class="paragraph">
<p><code>@RequestBody</code> can be used in combination with <code>javax.validation.Valid</code>, or Spring&#8217;s
<code>@Validated</code> annotation, which causes Standard Bean Validation to be applied.
By default validation errors cause a <code>MethodArgumentNotValidException</code> which is turned
into a 400 (BAD_REQUEST) response. Alternatively validation errors can be handled locally
within the controller through an <code>Errors</code> or <code>BindingResult</code> argument:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@PostMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/accounts</span><span class="delimiter">&quot;</span></span>)
<span class="directive">public</span> <span class="type">void</span> handle(<span class="annotation">@Valid</span> <span class="annotation">@RequestBody</span> Account account, BindingResult result) {
    <span class="comment">// ...</span>
}</code></pre>
</div>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-httpentity"><a class="anchor" href="#mvc-ann-httpentity"></a>HttpEntity</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-httpentity">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>HttpEntity</code> is more or less identical to using <a href="#mvc-ann-requestbody">@RequestBody</a> but based on a
container object that exposes request headers and body. Below is an example:</p>
</div>

```
@PostMapping("/accounts")
public void handle(HttpEntity<Account> entity) {
    // ...
}
```

<div class="sect4">
<h5 id="mvc-ann-responsebody"><a class="anchor" href="#mvc-ann-responsebody"></a>@ResponseBody</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-responsebody">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Use the <code>@ResponseBody</code> annotation on a method to have the return serialized to the
response body through an
<a href="integration.html#rest-message-conversion">HttpMessageConverter</a>. For example:</p>
</div>

```
@GetMapping("/accounts/{id}")
@ResponseBody
public Account handle() {
    // ...
}
```

<div class="paragraph">
<p><code>@ResponseBody</code> is also supported at the class level in which case it is inherited by
all controller methods. This is the effect of <code>@RestController</code> which is nothing more
than a meta-annotation marked with <code>@Controller</code> and <code>@ResponseBody</code>.</p>
</div>
<div class="paragraph">
<p><code>@ResponseBody</code> may be used with reactive types.
See <a href="#mvc-ann-async">Async Requests</a> and <a href="#mvc-ann-async-reactive-types">Reactive types</a> for more details.</p>
</div>
<div class="paragraph">
<p>You can use the <a href="#mvc-config-message-converters">Message Converters</a> option of the <a href="#mvc-config">MVC Config</a> to
configure or customize message conversion.</p>
</div>
<div class="paragraph">
<p><code>@ResponseBody</code> methods can be combined with JSON serialization views.
See <a href="#mvc-ann-jackson">Jackson JSON</a> for details.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-responseentity"><a class="anchor" href="#mvc-ann-responseentity"></a>ResponseEntity</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-responseentity">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>ResponseEntity</code> is more or less identical to using <a href="#mvc-ann-responsebody">@ResponseBody</a> but based
on a container object that specifies request headers and body. Below is an example:</p>
</div>

```
@PostMapping("/something")
public ResponseEntity<String> handle() {
    // ...
    URI location = ... ;
    return ResponseEntity.created(location).build();
}
```
<div class="sect4">
<h5 id="mvc-ann-jackson"><a class="anchor" href="#mvc-ann-jackson"></a>Jackson JSON</h5>
<div class="sect5">
<h6 id="mvc-ann-jsonview"><a class="anchor" href="#mvc-ann-jsonview"></a>Jackson serialization views</h6>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-jsonview">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC provides built-in support for
<a href="http://wiki.fasterxml.com/JacksonJsonViews">Jackson&#8217;s Serialization Views</a>
which allows rendering only a subset of all fields in an Object. To use it with
<code>@ResponseBody</code> or <code>ResponseEntity</code> controller methods, use Jackson&#8217;s
<code>@JsonView</code> annotation to activate a serialization view class:</p>
</div>


```
@RestController
public class UserController {

    @GetMapping("/user")
    @JsonView(User.WithoutPasswordView.class)
    public User getUser() {
        return new User("eric", "7!jd#h23");
    }
}

public class User {

    public interface WithoutPasswordView {};
    public interface WithPasswordView extends WithoutPasswordView {};

    private String username;
    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonView(WithoutPasswordView.class)
    public String getUsername() {
        return this.username;
    }

    @JsonView(WithPasswordView.class)
    public String getPassword() {
        return this.password;
    }
}
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>@JsonView</code> allows an array of view classes but you can only specify only one per
controller method. Use a composite interface if you need to activate multiple views.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>For controllers relying on view resolution, simply add the serialization view class
to the model:</p>
</div>

```
@Controller
public class UserController extends AbstractController {

    @GetMapping("/user")
    public String getUser(Model model) {
        model.addAttribute("user", new User("eric", "7!jd#h23"));
        model.addAttribute(JsonView.class.getName(), User.WithoutPasswordView.class);
        return "userView";
    }
}
```

<div class="sect5">
<h6 id="mvc-ann-jsonp"><a class="anchor" href="#mvc-ann-jsonp"></a>Jackson JSONP</h6>
<div class="paragraph">
<p>In order to enable <a href="https://en.wikipedia.org/wiki/JSONP">JSONP</a> support for <code>@ResponseBody</code>
and <code>ResponseEntity</code> methods, declare an <code>@ControllerAdvice</code> bean that extends
<code>AbstractJsonpResponseBodyAdvice</code> as shown below where the constructor argument indicates
the JSONP query parameter name(s):</p>
</div>

```
@ControllerAdvice
public class JsonpAdvice extends AbstractJsonpResponseBodyAdvice {

    public JsonpAdvice() {
        super("callback");
    }
}
```

<div class="paragraph">
<p>For controllers relying on view resolution, JSONP is automatically enabled when the
request has a query parameter named <code>jsonp</code> or <code>callback</code>. Those names can be
customized through <code>jsonpParameterNames</code> property.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>As of Spring Framework 5.0.7, JSONP support is deprecated and will be removed as of
Spring Framework 5.1, <a href="#mvc-cors">CORS</a> should be used instead.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-modelattrib-methods"><a class="anchor" href="#mvc-ann-modelattrib-methods"></a>1.4.4. Model</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-modelattrib-methods">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>@ModelAttribute</code> annotation can be used:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>On a <a href="#mvc-ann-modelattrib-method-args">method argument</a> in <code>@RequestMapping</code> methods
to create or access an Object from the model, and to bind it to the request through a
<code>WebDataBinder</code>.</p>
</li>
<li>
<p>As a method-level annotation in <code>@Controller</code> or <code>@ControllerAdvice</code> classes helping
to initialize the model prior to any <code>@RequestMapping</code> method invocation.</p>
</li>
<li>
<p>On a <code>@RequestMapping</code> method to mark its return value is a model attribute.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>This section discusses <code>@ModelAttribute</code> methods, or the 2nd from the list above.
A controller can have any number of <code>@ModelAttribute</code> methods. All such methods are
invoked before <code>@RequestMapping</code> methods in the same controller. A <code>@ModelAttribute</code>
method can also be shared across controllers via <code>@ControllerAdvice</code>. See the section on
<a href="#mvc-ann-controller-advice">Controller Advice</a> for more details.</p>
</div>
<div class="paragraph">
<p><code>@ModelAttribute</code> methods have flexible method signatures. They support many of the same
arguments as <code>@RequestMapping</code> methods except for <code>@ModelAttribute</code> itself nor anything
related to the request body.</p>
</div>

```
@ModelAttribute
public void populateModel(@RequestParam String number, Model model) {
    model.addAttribute(accountRepository.findAccount(number));
    // add more ...
}
```

<div class="paragraph">
<p>To add one attribute only:</p>
</div>

```
@ModelAttribute
public Account addAccount(@RequestParam String number) {
    return accountRepository.findAccount(number);
}
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When a name is not explicitly specified, a default name is chosen based on the Object
type as explained in the Javadoc for
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/core/Conventions.html">Conventions</a>.
You can always assign an explicit name by using the overloaded <code>addAttribute</code> method or
through the name attribute on <code>@ModelAttribute</code> (for a return value).</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p><code>@ModelAttribute</code> can also be used as a method-level annotation on <code>@RequestMapping</code>
methods in which case the return value of the <code>@RequestMapping</code> method is interpreted as a
model attribute. This is typically not required, as it is the default behavior in HTML
controllers, unless the return value is a <code>String</code> which would otherwise be interpreted
as a view name (also see <a href="#mvc-coc-r2vnt">[mvc-coc-r2vnt]</a>). <code>@ModelAttribute</code> can also help to customize
the model attribute name:</p>
</div>

```
@GetMapping("/accounts/{id}")
@ModelAttribute("myAccount")
public Account handle() {
    // ...
    return account;
}
```

<div class="sect3">
<h4 id="mvc-ann-initbinder"><a class="anchor" href="#mvc-ann-initbinder"></a>1.4.5. DataBinder</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-initbinder">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@Controller</code> or <code>@ControllerAdvice</code> classes can have <code>@InitBinder</code> methods in order to
initialize instances of <code>WebDataBinder</code>, and those in turn are used to:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Bind request parameters (i.e. form data or query) to a model object.</p>
</li>
<li>
<p>Convert String-based request values such as request parameters, path variables,
headers, cookies, and others, to the target type of controller method arguments.</p>
</li>
<li>
<p>Format model object values as String values when rendering HTML forms.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p><code>@InitBinder</code> methods can register controller-specific <code>java.bean.PropertyEditor</code>, or
Spring <code>Converter</code> and <code>Formatter</code> components. In addition, the
<a href="#mvc-config-conversion">MVC config</a> can be used to register <code>Converter</code> and <code>Formatter</code>
types in a globally shared <code>FormattingConversionService</code>.</p>
</div>
<div class="paragraph">
<p><code>@InitBinder</code> methods support many of the same arguments that a <code>@RequestMapping</code> methods
do, except for <code>@ModelAttribute</code> (command object) arguments. Typically they&#8217;re are declared
with a <code>WebDataBinder</code> argument, for registrations, and a <code>void</code> return value.
Below is an example:</p>
</div>

```
@Controller
public class FormController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    // ...
}
```

<div class="paragraph">
<p>Alternatively when using a <code>Formatter</code>-based setup through a shared
<code>FormattingConversionService</code>, you could re-use the same approach and register
controller-specific <code>Formatter</code>'s:</p>
</div>

```
@Controller
public class FormController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(new DateFormatter("yyyy-MM-dd"));
    }

    // ...
}
```

<div class="sect3">
<h4 id="mvc-ann-exceptionhandler"><a class="anchor" href="#mvc-ann-exceptionhandler"></a>1.4.6. Exceptions</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-controller-exceptions">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@Controller</code> and <a href="#mvc-ann-controller-advice">@ControllerAdvice</a> classes can have
<code>@ExceptionHandler</code> methods to handle exceptions from controller methods. For example:</p>
</div>

```
@Controller
public class SimpleController {

    // ...

    @ExceptionHandler
    public ResponseEntity<String> handle(IOException ex) {
        // ...
    }
}
```

<div class="paragraph">
<p>The exception may match against a top-level exception being propagated (i.e. a direct
<code>IOException</code> thrown), or against the immediate cause within a top-level wrapper exception
(e.g. an <code>IOException</code> wrapped inside an <code>IllegalStateException</code>).</p>
</div>
<div class="paragraph">
<p>For matching exception types, preferably declare the target exception as a method argument
as shown above. When multiple exception methods match, a root exception match is generally
preferred to a cause exception match. More specifically, the <code>ExceptionDepthComparator</code> is
used to sort exceptions based on their depth from the thrown exception type.</p>
</div>
<div class="paragraph">
<p>Alternatively, the annotation declaration may narrow the exception types to match:</p>
</div>

```
@ExceptionHandler({FileSystemException.class, RemoteException.class})
public ResponseEntity<String> handle(IOException ex) {
    // ...
}
```

<div class="paragraph">
<p>Or even a list of specific exception types with a very generic argument signature:</p>
</div>

```
@ExceptionHandler({FileSystemException.class, RemoteException.class})
public ResponseEntity<String> handle(Exception ex) {
    // ...
}
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The distinction between root and cause exception matching can be surprising:</p>
</div>
<div class="paragraph">
<p>In the <code>IOException</code> variant above, the method will typically be called with
the actual <code>FileSystemException</code> or <code>RemoteException</code> instance as the argument
since both of them extend from <code>IOException</code>. However, if any such matching
exception is propagated within a wrapper exception which is an <code>IOException</code>
itself, the passed-in exception instance will be that wrapper exception.</p>
</div>
<div class="paragraph">
<p>The behavior is even simpler in the <code>handle(Exception)</code> variant: This will
always be invoked with the wrapper exception in a wrapping scenario, with the
actually matching exception to be found through <code>ex.getCause()</code> in that case.
The passed-in exception will only be the actual <code>FileSystemException</code> or
<code>RemoteException</code> instance when these are thrown as top-level exceptions.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>We generally recommend to be as specific as possible in the argument signature,
reducing the potential for mismatches between root and cause exception types.
Consider breaking a multi-matching method into individual <code>@ExceptionHandler</code>
methods, each matching a single specific exception type through its signature.</p>
</div>
<div class="paragraph">
<p>In a multi-<code>@ControllerAdvice</code> arrangement, please declare your primary root exception
mappings on a <code>@ControllerAdvice</code> prioritized with a corresponding order. While a root
exception match is preferred to a cause, this is defined among the methods of a given
controller or <code>@ControllerAdvice</code> class. This means a cause match on a higher-priority
<code>@ControllerAdvice</code> bean is preferred to any match (e.g. root) on a lower-priority
<code>@ControllerAdvice</code> bean.</p>
</div>
<div class="paragraph">
<p>Last but not least, an <code>@ExceptionHandler</code> method implementation may choose to back
out of dealing with a given exception instance by rethrowing it in its original form.
This is useful in scenarios where you are only interested in root-level matches or in
matches within a specific context that cannot be statically determined. A rethrown
exception will be propagated through the remaining resolution chain, just like if
the given <code>@ExceptionHandler</code> method would not have matched in the first place.</p>
</div>
<div class="paragraph">
<p>Support for <code>@ExceptionHandler</code> methods in Spring MVC is built on the <code>DispatcherServlet</code>
level, <a href="#mvc-exceptionhandlers">HandlerExceptionResolver</a> mechanism.</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-exceptionhandler-args"><a class="anchor" href="#mvc-ann-exceptionhandler-args"></a>Method arguments</h5>
<div class="paragraph">
<p><code>@ExceptionHandler</code> methods support the following arguments:</p>
</div>
<table class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Method argument</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">Exception type</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the raised exception.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HandlerMethod</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the controller method that raised the exception.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>WebRequest</code>, <code>NativeWebRequest</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Generic access to request parameters, request &amp; session attributes, without direct
use of the Servlet API.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>javax.servlet.ServletRequest</code>, <code>javax.servlet.ServletResponse</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Choose any specific request or response type&#8201;&#8212;&#8201;e.g. <code>ServletRequest</code>, <code>HttpServletRequest</code>,
or Spring&#8217;s <code>MultipartRequest</code>, <code>MultipartHttpServletRequest</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>javax.servlet.http.HttpSession</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Enforces the presence of a session. As a consequence, such an argument is never <code>null</code>.<br>
<strong>Note:</strong> Session access is not thread-safe. Consider setting the
<code>RequestMappingHandlerAdapter</code>'s "synchronizeOnSession" flag to "true" if multiple
requests are allowed to access a session concurrently.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.security.Principal</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Currently authenticated user; possibly a specific <code>Principal</code> implementation class if known.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpMethod</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The HTTP method of the request.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Locale</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The current request locale, determined by the most specific <code>LocaleResolver</code> available, in
effect, the configured <code>LocaleResolver</code>/<code>LocaleContextResolver</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.TimeZone</code> + <code>java.time.ZoneId</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The time zone associated with the current request, as determined by a <code>LocaleContextResolver</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.io.OutputStream</code>, <code>java.io.Writer</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the raw response body as exposed by the Servlet API.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Map</code>, <code>org.springframework.ui.Model</code>, <code>org.springframework.ui.ModelMap</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the model for an error response, always empty.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>RedirectAttributes</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Specify attributes to use in case of a redirect&#8201;&#8212;&#8201;i.e. to be appended to the query
string, and/or flash attributes to be stored temporarily until the request after redirect.
See <a href="#mvc-redirecting-passing-data">Redirect attributes</a> and <a href="#mvc-flash-attributes">Flash attributes</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@SessionAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to any session attribute; in contrast to model attributes stored in the session
as a result of a class-level <code>@SessionAttributes</code> declaration. See
<a href="#mvc-ann-sessionattribute">@SessionAttribute</a> for more details.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@RequestAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to request attributes. See <a href="#mvc-ann-requestattrib">@RequestAttribute</a> for more details.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect4">
<h5 id="mvc-ann-exceptionhandler-return-values"><a class="anchor" href="#mvc-ann-exceptionhandler-return-values"></a>Return Values</h5>
<div class="paragraph">
<p><code>@ExceptionHandler</code> methods support the following return values:</p>
</div>
<table class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Return value</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@ResponseBody</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The return value is converted through <code>HttpMessageConverter</code>s and written to the
response. See <a href="#mvc-ann-responsebody">@ResponseBody</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>HttpEntity&lt;B&gt;</code>, <code>ResponseEntity&lt;B&gt;</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The return value specifies the full response including HTTP headers and body be converted
through <code>HttpMessageConverter</code>s and written to the response.
See <a href="#mvc-ann-responseentity">ResponseEntity</a>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>String</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A view name to be resolved with <code>ViewResolver</code>'s and used together with the implicit
model&#8201;&#8212;&#8201;determined through command objects and <code>@ModelAttribute</code> methods. The handler
method may also programmatically enrich the model by declaring a <code>Model</code> argument
(see above).</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>View</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A <code>View</code> instance to use for rendering together with the implicit model&#8201;&#8212;&#8201;determined
through command objects and <code>@ModelAttribute</code> methods. The handler method may also
programmatically enrich the model by declaring a <code>Model</code> argument (see above).</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.util.Map</code>, <code>org.springframework.ui.Model</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Attributes to be added to the implicit model with the view name implicitly determined
through a <code>RequestToViewNameTranslator</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@ModelAttribute</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">An attribute to be added to the model with the view name implicitly determined through
a <code>RequestToViewNameTranslator</code>.</p>
<p class="tableblock"> Note that <code>@ModelAttribute</code> is optional. See "Any other return value" further below in
this table.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>ModelAndView</code> object</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">The view and model attributes to use, and optionally a response status.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>void</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">A method with a <code>void</code> return type (or <code>null</code> return value) is considered to have fully
handled the response if it also has a <code>ServletResponse</code>, or an <code>OutputStream</code> argument, or
an <code>@ResponseStatus</code> annotation. The same is true also if the controller has made a positive
ETag or lastModified timestamp check (see <a href="#mvc-caching-etag-lastmodified">Controllers</a> for details).</p>
<p class="tableblock"> If none of the above is true, a <code>void</code> return type may also indicate "no response body" for
REST controllers, or default view name selection for HTML controllers.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock">Any other return value</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">If a return value is not matched to any of the above, by default it is treated as a
model attribute to be added to the model, unless it is a simple type, as determined by
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/beans/BeanUtils.html#isSimpleProperty-java.lang.Class-">BeanUtils#isSimpleProperty</a>
in which case it remains unresolved.</p></td>
</tr>
</tbody>
</table>
</div>
<div class="sect4">
<h5 id="mvc-ann-rest-exceptions"><a class="anchor" href="#mvc-ann-rest-exceptions"></a>REST API exceptions</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-rest-exceptions">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>A common requirement for REST services is to include error details in the body of the
response. The Spring Framework does not automatically do this because the representation
of error details in the response body is application specific. However a
<code>@RestController</code> may use <code>@ExceptionHandler</code> methods with a <code>ResponseEntity</code> return
value to set the status and the body of the response. Such methods may also be declared
in <code>@ControllerAdvice</code> classes to apply them globally.</p>
</div>
<div class="paragraph">
<p>Applications that implement global exception handling with error details in the response
body should consider extending
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseEntityExceptionHandler.html">ResponseEntityExceptionHandler</a>
which provides handling for exceptions that Spring MVC raises along with hooks to
customize the response body. To make use of this, create a subclass of
<code>ResponseEntityExceptionHandler</code>, annotate with <code>@ControllerAdvice</code>, override the
necessary methods, and declare it as a Spring bean.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-controller-advice"><a class="anchor" href="#mvc-ann-controller-advice"></a>1.4.7. Controller Advice</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-ann-controller-advice">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Typically <code>@ExceptionHandler</code>, <code>@InitBinder</code>, and <code>@ModelAttribute</code> methods apply within
the <code>@Controller</code> class (or class hierarchy) they are declared in. If you want such
methods to apply more globally, across controllers, you can declare them in a class
marked with <code>@ControllerAdvice</code> or <code>@RestControllerAdvice</code>.</p>
</div>
<div class="paragraph">
<p><code>@ControllerAdvice</code> is marked with <code>@Component</code> which means such classes can be registered
as Spring beans via <a href="core.html#beans-java-instantiating-container-scan">component scanning</a>.
<code>@RestControllerAdvice</code> is also a meta-annotation marked with both <code>@ControllerAdvice</code> and
<code>@ResponseBody</code> which essentially means <code>@ExceptionHandler</code> methods are rendered to the
response body via message conversion (vs view resolution/template rendering).</p>
</div>
<div class="paragraph">
<p>On startup, the infrastructure classes for <code>@RequestMapping</code> and <code>@ExceptionHandler</code> methods
detect Spring beans of type <code>@ControllerAdvice</code>, and then apply their methods at runtime.
Global <code>@ExceptionHandler</code> methods (from an <code>@ControllerAdvice</code>) are applied <strong>after</strong> local
ones (from the <code>@Controller</code>). By contrast global <code>@ModelAttribute</code> and <code>@InitBinder</code>
methods are applied <strong>before</strong> local ones.</p>
</div>
<div class="paragraph">
<p>By default <code>@ControllerAdvice</code> methods apply to every request, i.e. all controllers, but
you can narrow that down to a subset of controllers via attributes on the annotation:</p>
</div>

```
// Target all Controllers annotated with @RestController
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// Target all Controllers within specific packages
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// Target all Controllers assignable to specific classes
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```

<div class="paragraph">
<p>Keep in mind the above selectors are evaluated at runtime and may negatively impact
performance if used extensively. See the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html">@ControllerAdvice</a>
Javadoc for more details.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-uri-building"><a class="anchor" href="#mvc-uri-building"></a>1.5. URI Links</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#mvc-uri-building">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>This section describes various options available in the Spring Framework to work with URI&#8217;s.</p>
</div>
<div class="sect3">
<h4 id="web-uricomponents"><a class="anchor" href="#web-uricomponents"></a>1.5.1. UriComponents</h4>
<div class="paragraph">
<p><span class="small">Spring MVC and Spring WebFlux</span></p>
</div>
<div class="paragraph">
<p><code>UriComponentsBuilder</code> helps to build URI&#8217;s from URI templates with variables:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">UriComponents uriComponents = UriComponentsBuilder
        .fromUriString(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://example.com/hotels/{hotel}</span><span class="delimiter">&quot;</span></span>)  <i class="conum" data-value="1"></i><b>(1)</b>
        .queryParam(<span class="string"><span class="delimiter">&quot;</span><span class="content">q</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">{q}</span><span class="delimiter">&quot;</span></span>)  <i class="conum" data-value="2"></i><b>(2)</b>
        .encode() <i class="conum" data-value="3"></i><b>(3)</b>
        .build(); <i class="conum" data-value="4"></i><b>(4)</b>

<span class="predefined-type">URI</span> uri = uriComponents.expand(<span class="string"><span class="delimiter">&quot;</span><span class="content">Westin</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">123</span><span class="delimiter">&quot;</span></span>).toUri();  <i class="conum" data-value="5"></i><b>(5)</b></code></pre>
</div>
</div>
<div class="colist arabic">
<table>
<tr>
<td><i class="conum" data-value="1"></i><b>1</b></td>
<td>Static factory method with a URI template.</td>
</tr>
<tr>
<td><i class="conum" data-value="2"></i><b>2</b></td>
<td>Add and/or replace URI components.</td>
</tr>
<tr>
<td><i class="conum" data-value="3"></i><b>3</b></td>
<td>Request to have the URI template and URI variables encoded.</td>
</tr>
<tr>
<td><i class="conum" data-value="4"></i><b>4</b></td>
<td>Build a <code>UriComponents</code>.</td>
</tr>
<tr>
<td><i class="conum" data-value="5"></i><b>5</b></td>
<td>Expand variables, and obtain the <code>URI</code>.</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The above can be consolidated into one chain and shortened with <code>buildAndExpand</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="predefined-type">URI</span> uri = UriComponentsBuilder
        .fromUriString(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://example.com/hotels/{hotel}</span><span class="delimiter">&quot;</span></span>)
        .queryParam(<span class="string"><span class="delimiter">&quot;</span><span class="content">q</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">{q}</span><span class="delimiter">&quot;</span></span>)
        .encode()
        .buildAndExpand(<span class="string"><span class="delimiter">&quot;</span><span class="content">Westin</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">123</span><span class="delimiter">&quot;</span></span>)
        .toUri();</code></pre>
</div>
</div>
<div class="paragraph">
<p>It can be shortened further by going directly to URI (which implies encoding):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="predefined-type">URI</span> uri = UriComponentsBuilder
        .fromUriString(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://example.com/hotels/{hotel}</span><span class="delimiter">&quot;</span></span>)
        .queryParam(<span class="string"><span class="delimiter">&quot;</span><span class="content">q</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">{q}</span><span class="delimiter">&quot;</span></span>)
        .build(<span class="string"><span class="delimiter">&quot;</span><span class="content">Westin</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">123</span><span class="delimiter">&quot;</span></span>);</code></pre>
</div>
</div>
<div class="paragraph">
<p>Or shorter further yet, with a full URI template:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="predefined-type">URI</span> uri = UriComponentsBuilder
        .fromUriString(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://example.com/hotels/{hotel}?q={q}</span><span class="delimiter">&quot;</span></span>)
        .build(<span class="string"><span class="delimiter">&quot;</span><span class="content">Westin</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">123</span><span class="delimiter">&quot;</span></span>);</code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="web-uribuilder"><a class="anchor" href="#web-uribuilder"></a>1.5.2. UriBuilder</h4>
<div class="paragraph">
<p><span class="small">Spring MVC and Spring WebFlux</span></p>
</div>
<div class="paragraph">
<p><a href="#web-uricomponents">UriComponentsBuilder</a> implements <code>UriBuilder</code>. A <code>UriBuilder</code> in turn
can be created with a <code>UriBuilderFactory</code>. Together <code>UriBuilderFactory</code> and <code>UriBuilder</code>
provide a pluggable mechanism to build URIs from URI templates, based on shared
configuration such as a base url, encoding preferences, and others.</p>
</div>
<div class="paragraph">
<p>The <code>RestTemplate</code> and the <code>WebClient</code> can be configured with a <code>UriBuilderFactory</code>
to customize the preparation of URIs. <code>DefaultUriBuilderFactory</code> is a default
implementation of <code>UriBuilderFactory</code> that uses <code>UriComponentsBuilder</code> internally and
exposes shared configuration options.</p>
</div>
<div class="paragraph">
<p><code>RestTemplate</code> example:</p>
</div>

```
// import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

String baseUrl = "http://example.org";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VARIABLES);

RestTemplate restTemplate = new RestTemplate();
restTemplate.setUriTemplateHandler(factory);
```

<div class="paragraph">
<p><code>WebClient</code> example:</p>
</div>

```
// import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

String baseUrl = "http://example.org";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VARIABLES);

WebClient client = WebClient.builder().uriBuilderFactory(factory).build();
```

<div class="paragraph">
<p>In addition <code>DefaultUriBuilderFactory</code> can also be used directly. It is similar to using
<code>UriComponentsBuilder</code> but instead of static factory methods, it is an actual instance
that holds configuration and preferences:</p>
</div>

```
String baseUrl = "http://example.com";
DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);

URI uri = uriBuilderFactory.uriString("/hotels/{hotel}")
        .queryParam("q", "{q}")
        .build("Westin", "123");
```

<div class="sect3">
<h4 id="web-uri-encoding"><a class="anchor" href="#web-uri-encoding"></a>1.5.3. URI Encoding</h4>
<div class="paragraph">
<p><span class="small">Spring MVC and Spring WebFlux</span></p>
</div>
<div class="paragraph">
<p><code>UriComponentsBuilder</code> exposes encoding options at 2 levels:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/util/UriComponentsBuilder.html#encode--">UriComponentsBuilder#encode()</a> -
pre-encodes the URI template first, then strictly encodes URI variables when expanded.</p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/util/UriComponents.html#encode--">UriComponents#encode()</a> -
encodes URI components <em>after</em> URI variables are expanded.</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>Both options replace non-ASCII and illegal characters with escaped octets, however option
1 also replaces characters with reserved meaning that appear in URI variables.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Consider ";" which is legal in a path but has reserved meaning. Option 1 replaces
";" with "%3B" in URI variables but not in the URI template. By contrast, option 2 never
replaces ";" since it is a legal character in a path.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>For most cases option 1 is likely to give the expected result because it treats URI
variables as opaque data to be fully encoded, while option 2 is useful only if
URI variables intentionally contain reserved characters.</p>
</div>

```
URI uri = UriComponentsBuilder.fromPath("/hotel list/{city}")
            .queryParam("q", "{q}")
            .encode()
            .buildAndExpand("New York", "foo+bar")
            .toUri();

    // Result is "/hotel%20list/New%20York?foo%2Bbar"
```

<div class="paragraph">
<p>The above can be shortened by going directly to URI (which implies encoding):</p>
</div>

```
URI uri = UriComponentsBuilder.fromPath("/hotel list/{city}")
            .queryParam("q", "{q}")
            .build("New York", "foo+bar")
```

<div class="paragraph">
<p>Or shorter further yet, with a full URI template:</p>
</div>

```
URI uri = UriComponentsBuilder.fromPath("/hotel list/{city}?q={q}")
            .build("New York", "foo+bar")
```

<div class="paragraph">
<p>The <code>WebClient</code> and the <code>RestTemplate</code> expand and encode URI templates internally through
the <code>UriBuilderFactory</code> strategy. Both can be configured with a custom strategy:</p>
</div>

```
String baseUrl = "http://example.com";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl)
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);

// Customize the RestTemplate..
RestTemplate restTemplate = new RestTemplate();
restTemplate.setUriTemplateHandler(factory);

// Customize the WebClient..
WebClient client = WebClient.builder().uriBuilderFactory(factory).build();
```

<div class="paragraph">
<p>The <code>DefaultUriBuilderFactory</code> implementation uses <code>UriComponentsBuilder</code> internally to
expand and encode URI templates. As a factory it provides a single place to configure
the approach to encoding based on one of the below encoding modes:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>TEMPLATE_AND_VALUES</code>&#8201;&#8212;&#8201;uses <code>UriComponentsBuilder#encode()</code>, corresponding to
option 1 above, to pre-encode the URI template and strictly encode URI variables when
expanded.</p>
</li>
<li>
<p><code>VALUES_ONLY</code>&#8201;&#8212;&#8201;does not encode the URI template and instead applies strict encoding
to URI variables via <code>UriUtils#encodeUriUriVariables</code> prior to expanding them into the
template.</p>
</li>
<li>
<p><code>URI_COMPONENTS</code>&#8201;&#8212;&#8201;uses <code>UriComponents#encode()</code>, corresponding to option 2 above, to
encode URI component value <em>after</em> URI variables are expanded.</p>
</li>
<li>
<p><code>NONE</code>&#8201;&#8212;&#8201;no encoding is applied.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Out of the box the <code>RestTemplate</code> is set to <code>EncodingMode.URI_COMPONENTS</code> for historic
reasons and for backwards compatibility. The <code>WebClient</code> relies on the default value
in <code>DefaultUriBuilderFactory</code> which was changed from <code>EncodingMode.URI_COMPONENTS</code> in
5.0.x to <code>EncodingMode.TEMPLATE_AND_VALUES</code> in 5.1.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-servleturicomponentsbuilder"><a class="anchor" href="#mvc-servleturicomponentsbuilder"></a>1.5.4. Servlet request relative</h4>
<div class="paragraph">
<p>You can use <code>ServletUriComponentsBuilder</code> to create URIs relative to the current request:</p>
</div>

```
HttpServletRequest request = ...

// Re-uses host, scheme, port, path and query string...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request)
        .replaceQueryParam("accountId", "{id}").build()
        .expand("123")
        .encode();
```

<div class="paragraph">
<p>You can create URIs relative to the context path:</p>
</div>

```
// Re-uses host, port and context path...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromContextPath(request)
        .path("/accounts").build()
```

<div class="paragraph">
<p>You can create URIs relative to a Servlet (e.g. <code>/main/*</code>):</p>
</div>

```
// Re-uses host, port, context path, and Servlet prefix...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromServletMapping(request)
        .path("/accounts").build()
```

<div class="admonitionblock caution">
<table>
<tr>
<td class="icon">
<i class="fa icon-caution" title="Caution"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>ServletUriComponentsBuilder</code> detects and uses information from the "Forwarded",
"X-Forwarded-Host", "X-Forwarded-Port", and "X-Forwarded-Proto" headers, so the resulting
links reflect the original request. You need to ensure that your application is behind
a trusted proxy which filters out such headers coming from outside. Also consider using
the <a href="#filters-forwarded-headers">ForwardedHeaderFilter</a> which processes such headers once
per request, and also provides an option to remove and ignore such headers.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-links-to-controllers"><a class="anchor" href="#mvc-links-to-controllers"></a>1.5.5. Links to controllers</h4>
<div class="paragraph">
<p>Spring MVC provides a mechanism to prepare links to controller methods. For example,
the following MVC controller easily allows for link creation:</p>
</div>

```
@Controller
@RequestMapping("/hotels/{hotel}")
public class BookingController {

    @GetMapping("/bookings/{booking}")
    public ModelAndView getBooking(@PathVariable Long booking) {
        // ...
    }
}
```

<div class="paragraph">
<p>You can prepare a link by referring to the method by name:</p>
</div>

```
UriComponents uriComponents = MvcUriComponentsBuilder
    .fromMethodName(BookingController.class, "getBooking", 21).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();

```

<div class="paragraph">
<p>In the above example we provided actual method argument values, in this case the long value 21,
to be used as a path variable and inserted into the URL. Furthermore, we provided the
value 42 in order to fill in any remaining URI variables such as the "hotel" variable inherited
from the type-level request mapping. If the method had more arguments you can supply null for
arguments not needed for the URL. In general only <code>@PathVariable</code> and <code>@RequestParam</code> arguments
are relevant for constructing the URL.</p>
</div>
<div class="paragraph">
<p>There are additional ways to use <code>MvcUriComponentsBuilder</code>. For example you can use a technique
akin to mock testing through proxies to avoid referring to the controller method by name
(the example assumes static import of <code>MvcUriComponentsBuilder.on</code>):</p>
</div>

```
UriComponents uriComponents = MvcUriComponentsBuilder
    .fromMethodCall(on(BookingController.class).getBooking(21)).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();
```

<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Controller method signatures are limited in their design when supposed to be usable for
link creation with <code>fromMethodCall</code>. Aside from needing a proper parameter signature,
there is a technical limitation on the return type: namely generating a runtime proxy
for link builder invocations, so the return type must not be <code>final</code>. In particular,
the common <code>String</code> return type for view names does not work here; use <code>ModelAndView</code>
or even plain <code>Object</code> (with a <code>String</code> return value) instead.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The above examples use static methods in <code>MvcUriComponentsBuilder</code>. Internally they rely
on <code>ServletUriComponentsBuilder</code> to prepare a base URL from the scheme, host, port,
context path and servlet path of the current request. This works well in most cases,
however sometimes it may be insufficient. For example you may be outside the context of
a request (e.g. a batch process that prepares links) or perhaps you need to insert a path
prefix (e.g. a locale prefix that was removed from the request path and needs to be
re-inserted into links).</p>
</div>
<div class="paragraph">
<p>For such cases you can use the static "fromXxx" overloaded methods that accept a
<code>UriComponentsBuilder</code> to use base URL. Or you can create an instance of <code>MvcUriComponentsBuilder</code>
with a base URL and then use the instance-based "withXxx" methods. For example:</p>
</div>

```
UriComponentsBuilder base = ServletUriComponentsBuilder.fromCurrentContextPath().path("/en");
MvcUriComponentsBuilder builder = MvcUriComponentsBuilder.relativeTo(base);
builder.withMethodCall(on(BookingController.class).getBooking(21)).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();

```

<div class="admonitionblock caution">
<table>
<tr>
<td class="icon">
<i class="fa icon-caution" title="Caution"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>MvcUriComponentsBuilder</code> detects and uses information from the "Forwarded",
"X-Forwarded-Host", "X-Forwarded-Port", and "X-Forwarded-Proto" headers, so the resulting
links reflect the original request. You need to ensure that your application is behind
a trusted proxy which filters out such headers coming from outside. Also consider using
the <a href="#filters-forwarded-headers">ForwardedHeaderFilter</a> which processes such headers once
per request, and also provides an option to remove and ignore such headers.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-links-to-controllers-from-views"><a class="anchor" href="#mvc-links-to-controllers-from-views"></a>1.5.6. Links in views</h4>
<div class="paragraph">
<p>You can also build links to annotated controllers from views such as JSP, Thymeleaf,
FreeMarker. This can be done using the <code>fromMappingName</code> method in <code>MvcUriComponentsBuilder</code>
which refers to mappings by name.</p>
</div>
<div class="paragraph">
<p>Every <code>@RequestMapping</code> is assigned a default name based on the capital letters of the
class and the full method name. For example, the method <code>getFoo</code> in class <code>FooController</code>
is assigned the name "FC#getFoo". This strategy can be replaced or customized by creating
an instance of <code>HandlerMethodMappingNamingStrategy</code> and plugging it into your
<code>RequestMappingHandlerMapping</code>. The default strategy implementation also looks at the
name attribute on <code>@RequestMapping</code> and uses that if present. That means if the default
mapping name assigned conflicts with another (e.g. overloaded methods) you can assign
a name explicitly on the <code>@RequestMapping</code>.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The assigned request mapping names are logged at TRACE level on startup.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The Spring JSP tag library provides a function called <code>mvcUrl</code> that can be used to
prepare links to controller methods based on this mechanism.</p>
</div>
<div class="paragraph">
<p>For example given:</p>
</div>

```
@RequestMapping("/people/{id}/addresses")
public class PersonAddressController {

    @RequestMapping("/{country}")
    public HttpEntity getAddress(@PathVariable String country) { ... }
}
```

<div class="paragraph">
<p>You can prepare a link from a JSP as follows:</p>
</div>

```
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
...
<a href="${s:mvcUrl('PAC#getAddress').arg(0,'US').buildAndExpand('123')}">Get Address</a>
```

<div class="paragraph">
<p>The above example relies on the <code>mvcUrl</code> JSP function declared in the Spring tag library
(i.e. META-INF/spring.tld). For more advanced cases (e.g. a custom base URL as explained
in the previous section), it is easy to define your own function, or use a custom tag file,
in order to use a specific instance of <code>MvcUriComponentsBuilder</code> with a custom base URL.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-ann-async"><a class="anchor" href="#mvc-ann-async"></a>1.6. Async Requests</h3>
<div class="paragraph">
<p><span class="small"><a href="#mvc-ann-async-vs-webflux">Compared to WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC has an extensive integration with Servlet 3.0 asynchronous request
<a href="#mvc-ann-async-processing">processing</a>:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="#mvc-ann-async-deferredresult"><code>DeferredResult</code></a> and <a href="#mvc-ann-async-callable"><code>Callable</code></a> return values in
controller method provide basic support for a single asynchronous return value.</p>
</li>
<li>
<p>Controllers can <a href="#mvc-ann-async-http-streaming">stream</a> multiple values including
<a href="#mvc-ann-async-sse">SSE</a> and <a href="#mvc-ann-async-output-stream">raw data</a>.</p>
</li>
<li>
<p>Controllers can use reactive clients and return
<a href="#mvc-ann-async-reactive-types">reactive types</a> for response handling.</p>
</li>
</ul>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-deferredresult"><a class="anchor" href="#mvc-ann-async-deferredresult"></a>1.6.1. <code>DeferredResult</code></h4>
<div class="paragraph">
<p><span class="small"><a href="#mvc-ann-async-vs-webflux">Compared to WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Once the asynchronous request processing feature is
<a href="#mvc-ann-async-configuration">enabled</a> in the Servlet container, controller methods can
wrap any supported controller method return value with <code>DeferredResult</code>:</p>
</div>

```
@GetMapping("/quotes")
@ResponseBody
public DeferredResult<String> quotes() {
    DeferredResult<String> deferredResult = new DeferredResult<String>();
    // Save the deferredResult somewhere..
    return deferredResult;
}

// From some other thread...
deferredResult.setResult(data);
```

<div class="paragraph">
<p>The controller can produce the return value asynchronously, from a different thread, for
example in response to an external event (JMS message), a scheduled task, or other.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-callable"><a class="anchor" href="#mvc-ann-async-callable"></a>1.6.2. <code>Callable</code></h4>
<div class="paragraph">
<p><span class="small"><a href="#mvc-ann-async-vs-webflux">Compared to WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>A controller may also wrap any supported return value with <code>java.util.concurrent.Callable</code>:</p>
</div>

```
@PostMapping
public Callable<String> processUpload(final MultipartFile file) {

    return new Callable<String>() {
        public String call() throws Exception {
            // ...
            return "someView";
        }
    };

}
```

<div class="paragraph">
<p>The return value will then be obtained by executing the the given task through the
<a href="#mvc-ann-async-configuration-spring-mvc">configured</a> <code>TaskExecutor</code>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-processing"><a class="anchor" href="#mvc-ann-async-processing"></a>1.6.3. Processing</h4>
<div class="paragraph">
<p><span class="small"><a href="#mvc-ann-async-vs-webflux">Compared to WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Here is a very concise overview of Servlet asynchronous request processing:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>A <code>ServletRequest</code> can be put in asynchronous mode by calling <code>request.startAsync()</code>.
The main effect of doing so is that the Servlet, as well as any Filters, can exit but
the response will remain open to allow processing to complete later.</p>
</li>
<li>
<p>The call to <code>request.startAsync()</code> returns <code>AsyncContext</code> which can be used for
further control over async processing. For example it provides the method <code>dispatch</code>,
that is similar to a forward from the Servlet API except it allows an
application to resume request processing on a Servlet container thread.</p>
</li>
<li>
<p>The <code>ServletRequest</code> provides access to the current <code>DispatcherType</code> that can
be used to distinguish between processing the initial request, an async
dispatch, a forward, and other dispatcher types.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p><code>DeferredResult</code> processing:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Controller returns a <code>DeferredResult</code> and saves it in some in-memory
queue or list where it can be accessed.</p>
</li>
<li>
<p>Spring MVC calls <code>request.startAsync()</code>.</p>
</li>
<li>
<p>Meanwhile the <code>DispatcherServlet</code> and all configured Filter&#8217;s exit the request
processing thread but the response remains open.</p>
</li>
<li>
<p>The application sets the <code>DeferredResult</code> from some thread and Spring MVC
dispatches the request back to the Servlet container.</p>
</li>
<li>
<p>The <code>DispatcherServlet</code> is invoked again and processing resumes with the
asynchronously produced return value.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p><code>Callable</code> processing:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Controller returns a <code>Callable</code>.</p>
</li>
<li>
<p>Spring MVC calls <code>request.startAsync()</code> and submits the <code>Callable</code> to
a <code>TaskExecutor</code> for processing in a separate thread.</p>
</li>
<li>
<p>Meanwhile the <code>DispatcherServlet</code> and all Filter&#8217;s exit the Servlet container thread
but the response remains open.</p>
</li>
<li>
<p>Eventually the <code>Callable</code> produces a result and Spring MVC dispatches the request back
to the Servlet container to complete processing.</p>
</li>
<li>
<p>The <code>DispatcherServlet</code> is invoked again and processing resumes with the
asynchronously produced return value from the <code>Callable</code>.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>For further background and context you can also read
<a href="https://spring.io/blog/2012/05/07/spring-mvc-3-2-preview-introducing-servlet-3-async-support">the
blog posts</a> that introduced asynchronous request processing support in Spring MVC 3.2.</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-exceptions"><a class="anchor" href="#mvc-ann-async-exceptions"></a>Exception handling</h5>
<div class="paragraph">
<p>When using a <code>DeferredResult</code> you can choose whether to call <code>setResult</code> or
<code>setErrorResult</code> with an exception. In both cases Spring MVC dispatches the request back
to the Servlet container to complete processing. It is then treated either as if the
controller method returned the given value, or as if it produced the given exception.
The exception then goes through the regular exception handling mechanism, e.g. invoking
<code>@ExceptionHandler</code> methods.</p>
</div>
<div class="paragraph">
<p>When using <code>Callable</code>, similar processing logic follows. The main difference being that
the result is returned from the <code>Callable</code> or an exception is raised by it.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-interception"><a class="anchor" href="#mvc-ann-async-interception"></a>Interception</h5>
<div class="paragraph">
<p><code>HandlerInterceptor</code>'s can also be <code>AsyncHandlerInterceptor</code> in order to receive the
<code>afterConcurrentHandlingStarted</code> callback on the initial request that starts asynchronous
processing instead of <code>postHandle</code> and <code>afterCompletion</code>.</p>
</div>
<div class="paragraph">
<p><code>HandlerInterceptor</code>'s can also register a <code>CallableProcessingInterceptor</code>
or a <code>DeferredResultProcessingInterceptor</code> in order to integrate more deeply with the
lifecycle of an asynchronous request for example to handle a timeout event. See
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/AsyncHandlerInterceptor.html">AsyncHandlerInterceptor</a>
for more details.</p>
</div>
<div class="paragraph">
<p><code>DeferredResult</code> provides <code>onTimeout(Runnable)</code> and <code>onCompletion(Runnable)</code> callbacks.
See the Javadoc of <code>DeferredResult</code> for more details. <code>Callable</code> can be substituted for
<code>WebAsyncTask</code> that exposes additional methods for timeout and completion callbacks.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-vs-webflux"><a class="anchor" href="#mvc-ann-async-vs-webflux"></a>Compared to WebFlux</h5>
<div class="paragraph">
<p>The Servlet API was originally built for making a single pass through the Filter-Servlet
chain. Asynchronous request processing, added in Servlet 3.0, allows applications to exit
the Filter-Servlet chain but leave the response open for further processing. The Spring MVC
async support is built around that mechanism. When a controller returns a <code>DeferredResult</code>,
the Filter-Servlet chain is exited and the Servlet container thread is released. Later when
the <code>DeferredResult</code> is set, an ASYNC dispatch (to the same URL) is made during which the
controller is mapped again but rather than invoking it, the <code>DeferredResult</code> value is used
(as if the controller returned it) to resume processing.</p>
</div>
<div class="paragraph">
<p>By contrast Spring WebFlux is neither built on the Servlet API, nor does it need such an
asynchronous request processing feature because it is asynchronous by design. Asynchronous
handling is built into all framework contracts and is intrinsically supported through ::
stages of request processing.</p>
</div>
<div class="paragraph">
<p>From a programming model perspective, both Spring MVC and Spring WebFlux support
asynchronous and <a href="#mvc-ann-async-reactive-types">Reactive types</a> as return values in controller methods.
Spring MVC even supports streaming, including reactive back pressure. However individual
writes to the response remain blocking (and performed on a separate thread) unlike WebFlux
that relies on non-blocking I/O and does not need an extra thread for each write.</p>
</div>
<div class="paragraph">
<p>Another fundamental difference is that Spring MVC does not support asynchronous or
reactive types in controller method arguments, e.g. <code>@RequestBody</code>, <code>@RequestPart</code>, and
others, nor does it have any explicit support for asynchronous and reactive types as
model attributes. Spring WebFlux does support all that.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-http-streaming"><a class="anchor" href="#mvc-ann-async-http-streaming"></a>1.6.4. HTTP Streaming</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-codecs-streaming">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>DeferredResult</code> and <code>Callable</code> can be used for a single asynchronous return value.
What if you want to produce multiple asynchronous values and have those written to the
response?</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-objects"><a class="anchor" href="#mvc-ann-async-objects"></a>Objects</h5>
<div class="paragraph">
<p>The <code>ResponseBodyEmitter</code> return value can be used to produce a stream of Objects, where
each Object sent is serialized with an
<a href="integration.html#rest-message-conversion">HttpMessageConverter</a> and written to the
response. For example:</p>
</div>

```
@GetMapping("/events")
public ResponseBodyEmitter handle() {
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```

<div class="paragraph">
<p><code>ResponseBodyEmitter</code> can also be used as the body in a <code>ResponseEntity</code> allowing you to
customize the status and headers of the response.</p>
</div>
<div class="paragraph">
<p>When an <code>emitter</code> throws an <code>IOException</code> (e.g. if the remote client went away) applications
are not responsible for cleaning up the connection, and should not invoke <code>emitter.complete</code>
or <code>emitter.completeWithError</code>. Instead the servlet container automatically initiates an
<code>AsyncListener</code> error notification in which Spring MVC makes a <code>completeWithError</code> call,
which in turn performs one a final ASYNC dispatch to the application during which Spring MVC
invokes the configured exception resolvers and completes the request.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-sse"><a class="anchor" href="#mvc-ann-async-sse"></a>SSE</h5>
<div class="paragraph">
<p><code>SseEmitter</code> is a sub-class of <code>ResponseBodyEmitter</code> that provides support for
<a href="https://www.w3.org/TR/eventsource/">Server-Sent Events</a> where events sent from the server
are formatted according to the W3C SSE specification. In order to produce an SSE
stream from a controller simply return <code>SseEmitter</code>:</p>
</div>

```
@GetMapping(path="/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter handle() {
    SseEmitter emitter = new SseEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```

<div class="paragraph">
<p>While SSE is the main option for streaming into browsers, note that Internet Explorer
does not support Server-Sent Events. Consider using Spring&#8217;s
<a href="#websocket">WebSocket messaging</a> with
<a href="#websocket-fallback">SockJS fallback</a> transports (including SSE) that target
a wide range of browsers.</p>
</div>
<div class="paragraph">
<p>Also see <a href="#mvc-ann-async-objects">previous section</a> for notes on exception handling.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-output-stream"><a class="anchor" href="#mvc-ann-async-output-stream"></a>Raw data</h5>
<div class="paragraph">
<p>Sometimes it is useful to bypass message conversion and stream directly to the response
<code>OutputStream</code> for example for a file download. Use the of the <code>StreamingResponseBody</code>
return value type to do that:</p>
</div>

```
@GetMapping("/download")
public StreamingResponseBody handle() {
    return new StreamingResponseBody() {
        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            // write...
        }
    };
}
```

<div class="paragraph">
<p><code>StreamingResponseBody</code> can be used as the body in a <code>ResponseEntity</code> allowing you to
customize the status and headers of the response.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-reactive-types"><a class="anchor" href="#mvc-ann-async-reactive-types"></a>1.6.5. Reactive types</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-codecs-streaming">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Spring MVC supports use of reactive client libraries in a controller. This includes the
<code>WebClient</code> from <code>spring-webflux</code> and others such as Spring Data reactive data
repositories. In such scenarios it is convenient to be able to return reactive types
from the controller method .</p>
</div>
<div class="paragraph">
<p>Reactive return values are handled as follows:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>A single-value promise is adapted to, and similar to using <code>DeferredResult</code>. Examples
include <code>Mono</code> (Reactor) or <code>Single</code> (RxJava).</p>
</li>
<li>
<p>A multi-value stream, with a streaming media type such as <code>"application/stream+json"</code>
or <code>"text/event-stream"</code>, is adapted to, and similar to using <code>ResponseBodyEmitter</code> or
<code>SseEmitter</code>. Examples include <code>Flux</code> (Reactor) or <code>Observable</code> (RxJava).
Applications can also return <code>Flux&lt;ServerSentEvent&gt;</code> or <code>Observable&lt;ServerSentEvent&gt;</code>.</p>
</li>
<li>
<p>A multi-value stream, with any other media type (e.g. "application/json"), is adapted
to, and similar to using <code>DeferredResult&lt;List&lt;?&gt;&gt;</code>.</p>
</li>
</ul>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring MVC supports Reactor and RxJava through the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/core/ReactiveAdapterRegistry.html">ReactiveAdapterRegistry</a> from
<code>spring-core</code> which allows it to adapt from multiple reactive libraries.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>For streaming to the response, reactive back pressure is supported, but writes to the
response are still blocking, and are executed on a separate thread through the
<a href="#mvc-ann-async-configuration-spring-mvc">configured</a> <code>TaskExecutor</code> in order to avoid
blocking the upstream source (e.g. a <code>Flux</code> returned from the <code>WebClient</code>).
By default <code>SimpleAsyncTaskExecutor</code> is used for the blocking writes but that is not
suitable under load. If you plan to stream with a reactive type, please use the
<a href="#mvc-ann-async-configuration-spring-mvc">MVC config</a> to configure a task executor.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-disconnects"><a class="anchor" href="#mvc-ann-async-disconnects"></a>1.6.6. Disconnects</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-codecs-streaming">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The Servlet API does not provide any notification when a remote client goes away.
Therefore while streaming to the response, whether via <a href="#mvc-ann-async-sse">SseEmitter</a> or
&lt;&lt;mvc-ann-async-reactive-types,reactive types&gt;, it is important to send data periodically,
since the write would fail if the client has disconnected. The send could take the form
of an empty (comment-only) SSE event, or any other data that the other side would have to
to interpret as a heartbeat and ignore.</p>
</div>
<div class="paragraph">
<p>Alternatively consider using web messaging solutions such as
<a href="#websocket-stomp">STOMP over WebSocket</a> or WebSocket with <a href="#websocket-fallback">SockJS</a>
that have a built-in heartbeat mechanism.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-ann-async-configuration"><a class="anchor" href="#mvc-ann-async-configuration"></a>1.6.7. Configuration</h4>
<div class="paragraph">
<p><span class="small"><a href="#mvc-ann-async-vs-webflux">Compared to WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The async request processing feature must be enabled at the Servlet container level.
The MVC config also exposes several options for asynchronous requests.</p>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-configuration-servlet3"><a class="anchor" href="#mvc-ann-async-configuration-servlet3"></a>Servlet container</h5>
<div class="paragraph">
<p>Filter and Servlet declarations have an <code>asyncSupported</code> that needs to be set to true
in order enable asynchronous request processing. In addition, Filter mappings should be
declared to handle the ASYNC <code>javax.servlet.DispatchType</code>.</p>
</div>
<div class="paragraph">
<p>In Java configuration, when you use <code>AbstractAnnotationConfigDispatcherServletInitializer</code>
to initialize the Servlet container, this is done automatically.</p>
</div>
<div class="paragraph">
<p>In <code>web.xml</code> configuration, add <code>&lt;async-supported&gt;true&lt;/async-supported&gt;</code> to the
<code>DispatcherServlet</code> and to <code>Filter</code> declarations, and also add
<code>&lt;dispatcher&gt;ASYNC&lt;/dispatcher&gt;</code> to filter mappings.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-ann-async-configuration-spring-mvc"><a class="anchor" href="#mvc-ann-async-configuration-spring-mvc"></a>Spring MVC</h5>
<div class="paragraph">
<p>The MVC config exposes options related to async request processing:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Java config&#8201;&#8212;&#8201;use the <code>configureAsyncSupport</code> callback on <code>WebMvcConfigurer</code>.</p>
</li>
<li>
<p>XML namespace&#8201;&#8212;&#8201;use the <code>&lt;async-support&gt;</code> element under <code>&lt;mvc:annotation-driven&gt;</code>.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>You can configure the following:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Default timeout value for async requests, which if not set, depends
on the underlying Servlet container (e.g. 10 seconds on Tomcat).</p>
</li>
<li>
<p><code>AsyncTaskExecutor</code> to use for blocking writes when streaming with
<a href="#mvc-ann-async-reactive-types">Reactive types</a>, and also for executing <code>Callable</code>'s returned from
controller methods. It is highly recommended to configure this property if you&#8217;re
streaming with reactive types or have controller methods that return <code>Callable</code> since
by default it is a <code>SimpleAsyncTaskExecutor</code>.</p>
</li>
<li>
<p><code>DeferredResultProcessingInterceptor</code>'s and <code>CallableProcessingInterceptor</code>'s.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Note that the default timeout value can also be set on a <code>DeferredResult</code>,
<code>ResponseBodyEmitter</code> and <code>SseEmitter</code>. For a <code>Callable</code>, use <code>WebAsyncTask</code> to provide
a timeout value.</p>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-cors"><a class="anchor" href="#mvc-cors"></a>1.7. CORS</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors">Same in Spring WebFlux</a></span></p>
</div>
<div class="sect3">
<h4 id="mvc-cors-intro"><a class="anchor" href="#mvc-cors-intro"></a>1.7.1. Introduction</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-intro">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>For security reasons browsers prohibit AJAX calls to resources outside the current origin.
For example you could have your bank account in one tab and evil.com in another. Scripts
from evil.com should not be able to make AJAX requests to your bank API with your
credentials, e.g. withdrawing money from your account!</p>
</div>
<div class="paragraph">
<p>Cross-Origin Resource Sharing (CORS) is a <a href="https://www.w3.org/TR/cors/">W3C specification</a>
implemented by <a href="https://caniuse.com/#feat=cors">most browsers</a> that allows you to specify
what kind of cross domain requests are authorized rather than using less secure and less
powerful workarounds based on IFRAME or JSONP.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-cors-processing"><a class="anchor" href="#mvc-cors-processing"></a>1.7.2. Processing</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-processing">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The CORS specification distinguishes between preflight, simple, and actual requests.
To learn how CORS works, you can read
<a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">this article</a>, among
many others, or refer to the specification for more details.</p>
</div>
<div class="paragraph">
<p>Spring MVC <code>HandlerMapping</code>'s provide built-in support for CORS. After successfully
mapping a request to a handler, <code>HandlerMapping</code>'s check the CORS configuration for the
given request and handler and take further actions. Preflight requests are handled
directly while simple and actual CORS requests are intercepted, validated, and have
required CORS response headers set.</p>
</div>
<div class="paragraph">
<p>In order to enable cross-origin requests (i.e. the <code>Origin</code> header is present and
differs from the host of the request) you need to have some explicitly declared CORS
configuration. If no matching CORS configuration is found, preflight requests are
rejected. No CORS headers are added to the responses of simple and actual CORS requests
and consequently browsers reject them.</p>
</div>
<div class="paragraph">
<p>Each <code>HandlerMapping</code> can be
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/handler/AbstractHandlerMapping.html#setCorsConfigurations-java.util.Map-">configured</a>
individually with URL pattern based <code>CorsConfiguration</code> mappings. In most cases applications
will use the MVC Java config or the XML namespace to declare such mappings, which results
in a single, global map passed to all <code>HadlerMappping</code>'s.</p>
</div>
<div class="paragraph">
<p>Global CORS configuration at the <code>HandlerMapping</code> level can be combined with more
fine-grained, handler-level CORS configuration. For example annotated controllers can use
class or method-level <code>@CrossOrigin</code> annotations (other handlers can implement
<code>CorsConfigurationSource</code>).</p>
</div>
<div class="paragraph">
<p>The rules for combining global and local configuration are generally additive&#8201;&#8212;&#8201;e.g.
all global and all local origins. For those attributes where only a single value can be
accepted such as <code>allowCredentials</code> and <code>maxAge</code>, the local overrides the global value. See
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/cors/CorsConfiguration.html#combine-org.springframework.web.cors.CorsConfiguration-"><code>CorsConfiguration#combine(CorsConfiguration)</code></a>
for more details.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>To learn more from the source or make advanced customizations, check:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>CorsConfiguration</code></p>
</li>
<li>
<p><code>CorsProcessor</code>, <code>DefaultCorsProcessor</code></p>
</li>
<li>
<p><code>AbstractHandlerMapping</code></p>
</li>
</ul>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-cors-controller"><a class="anchor" href="#mvc-cors-controller"></a>1.7.3. @CrossOrigin</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-controller">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/bind/annotation/CrossOrigin.html"><code>@CrossOrigin</code></a>
annotation enables cross-origin requests on annotated controller methods:</p>
</div>

```
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

<div class="paragraph">
<p>By default <code>@CrossOrigin</code> allows:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>All origins.</p>
</li>
<li>
<p>All headers.</p>
</li>
<li>
<p>All HTTP methods to which the controller method is mapped.</p>
</li>
<li>
<p><code>allowedCredentials</code> is not enabled by default since that establishes a trust level
that exposes sensitive user-specific information such as cookies and CSRF tokens, and
should only be used where appropriate.</p>
</li>
<li>
<p><code>maxAge</code> is set to 30 minutes.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p><code>@CrossOrigin</code> is supported at the class level too and inherited by all methods:</p>
</div>

```
@CrossOrigin(origins = "http://domain2.com", maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

<div class="paragraph">
<p><code>CrossOrigin</code> can be used at both class and method-level:</p>
</div>

```
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin("http://domain2.com")
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

<div class="sect3">
<h4 id="mvc-cors-global"><a class="anchor" href="#mvc-cors-global"></a>1.7.4. Global Config</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-global">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>In addition to fine-grained, controller method level configuration you&#8217;ll probably want to
define some global CORS configuration too. You can set URL-based <code>CorsConfiguration</code>
mappings individually on any <code>HandlerMapping</code>. Most applications however will use the
MVC Java config or the MVC XNM namespace to do that.</p>
</div>
<div class="paragraph">
<p>By default global configuration enables the following:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>All origins.</p>
</li>
<li>
<p>All headers.</p>
</li>
<li>
<p><code>GET</code>, <code>HEAD</code>, and <code>POST</code> methods.</p>
</li>
<li>
<p><code>allowedCredentials</code> is not enabled by default since that establishes a trust level
that exposes sensitive user-specific information such as cookies and CSRF tokens, and
should only be used where appropriate.</p>
</li>
<li>
<p><code>maxAge</code> is set to 30 minutes.</p>
</li>
</ul>
</div>
<div class="sect4">
<h5 id="mvc-cors-global-java"><a class="anchor" href="#mvc-cors-global-java"></a>Java Config</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-global">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>To enable CORS in the MVC Java config, use the <code>CorsRegistry</code> callback:</p>
</div>

```
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
            .allowedOrigins("http://domain2.com")
            .allowedMethods("PUT", "DELETE")
            .allowedHeaders("header1", "header2", "header3")
            .exposedHeaders("header1", "header2")
            .allowCredentials(true).maxAge(3600);

        // Add more mappings...
    }
}
```

<div class="sect4">
<h5 id="mvc-cors-global-xml"><a class="anchor" href="#mvc-cors-global-xml"></a>XML Config</h5>
<div class="paragraph">
<p>To enable CORS in the XML namespace, use the <code>&lt;mvc:cors&gt;</code> element:</p>
</div>

```
<mvc:cors>

    <mvc:mapping path="/api/**"
        allowed-origins="http://domain1.com, http://domain2.com"
        allowed-methods="GET, PUT"
        allowed-headers="header1, header2, header3"
        exposed-headers="header1, header2" allow-credentials="true"
        max-age="123" />

    <mvc:mapping path="/resources/**"
        allowed-origins="http://domain1.com" />

</mvc:cors>
```

<div class="sect3">
<h4 id="mvc-cors-filter"><a class="anchor" href="#mvc-cors-filter"></a>1.7.5. CORS Filter</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-cors-webfilter">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can apply CORS support through the built-in
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/filter/CorsFilter.html"><code>CorsFilter</code></a>.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>If you&#8217;re trying to use the <code>CorsFilter</code> with Spring Security, keep in mind that Spring
Security has
<a href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#cors">built-in support</a>
for CORS.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>To configure the filter pass a
<code>CorsConfigurationSource</code> to its constructor:</p>
</div>

```
CorsConfiguration config = new CorsConfiguration();

// Possibly...
// config.applyPermitDefaultValues()

config.setAllowCredentials(true);
config.addAllowedOrigin("http://domain1.com");
config.addAllowedHeader("");
config.addAllowedMethod("");

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", config);

CorsFilter filter = new CorsFilter(source);
```

<div class="sect2">
<h3 id="mvc-web-security"><a class="anchor" href="#mvc-web-security"></a>1.8. Web Security</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-web-security">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <a href="https://projects.spring.io/spring-security/">Spring Security</a> project provides support
for protecting web applications from malicious exploits. Check out the Spring Security
reference documentation including:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="https://docs.spring.io/spring-security/site/docs/current/reference/html5/#mvc">Spring MVC Security</a></p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-security/site/docs/current/reference/html5/#test-mockmvc">Spring MVC Test Support</a></p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-security/site/docs/current/reference/html5/#csrf">CSRF protection</a></p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-security/site/docs/current/reference/html5/#headers">Security Response Headers</a></p>
</li>
</ul>
</div>
<div class="paragraph">
<p><a href="http://hdiv.org/">HDIV</a> is another web security framework that integrates with Spring MVC.</p>
</div>
</div>
<div class="sect2">
<h3 id="mvc-caching"><a class="anchor" href="#mvc-caching"></a>1.9. HTTP Caching</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-caching">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>HTTP caching can significantly improve the performance of a web application. HTTP caching
revolves around the "Cache-Control" response header and subsequently conditional request
headers such as "Last-Modified" and "ETag". "Cache-Control" advises private (e.g. browser)
and public (e.g. proxy) caches how to cache and re-use responses. An "ETag" header is used
to make a conditional request that may result in a 304 (NOT_MODIFIED) without a body,
if the content has not changed. "ETag" can be seen as a more sophisticated successor to
the <code>Last-Modified</code> header.</p>
</div>
<div class="paragraph">
<p>This section describes HTTP caching related options available in Spring Web MVC.</p>
</div>
<div class="sect3">
<h4 id="mvc-caching-cachecontrol"><a class="anchor" href="#mvc-caching-cachecontrol"></a>1.9.1. <code>CacheControl</code></h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-caching-cachecontrol">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/http/CacheControl.html"><code>CacheControl</code></a> provides support for
configuring settings related to the "Cache-Control" header and is accepted as an argument
in a number of places:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/mvc/WebContentInterceptor.html"><code>WebContentInterceptor</code></a></p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/support/WebContentGenerator.html"><code>WebContentGenerator</code></a></p>
</li>
<li>
<p><a href="#mvc-caching-etag-lastmodified">Controllers</a></p>
</li>
<li>
<p><a href="#mvc-caching-static-resources">Static resources</a></p>
</li>
</ul>
</div>
<div class="paragraph">
<p>While <a href="https://tools.ietf.org/html/rfc7234#section-5.2.2">RFC 7234</a> describes all possible
directives for the "Cache-Control" response header, the <code>CacheControl</code> type takes a
use case oriented approach focusing on the common scenarios:</p>
</div>

```
// Cache for an hour - "Cache-Control: max-age=3600"
CacheControl ccCacheOneHour = CacheControl.maxAge(1, TimeUnit.HOURS);

// Prevent caching - "Cache-Control: no-store"
CacheControl ccNoStore = CacheControl.noStore();

// Cache for ten days in public and private caches,
// public caches should not transform the response
// "Cache-Control: max-age=864000, public, no-transform"
CacheControl ccCustom = CacheControl.maxAge(10, TimeUnit.DAYS).noTransform().cachePublic();
```

<div class="paragraph">
<p><code>WebContentGenerator</code> also accept a simpler <code>cachePeriod</code> property, in seconds, that
works as follows:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>A <code>-1</code> value won&#8217;t generate a "Cache-Control" response header.</p>
</li>
<li>
<p>A <code>0</code> value will prevent caching using the <code>'Cache-Control: no-store'</code> directive.</p>
</li>
<li>
<p>An <code>n &gt; 0</code> value will cache the given response for <code>n</code> seconds using the
<code>'Cache-Control: max-age=n'</code> directive.</p>
</li>
</ul>
</div>
</div>
<div class="sect3">
<h4 id="mvc-caching-etag-lastmodified"><a class="anchor" href="#mvc-caching-etag-lastmodified"></a>1.9.2. Controllers</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-caching-etag-lastmodified">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Controllers can add explicit support for HTTP caching. This is recommended since the
lastModified or ETag value for a resource needs to be calculated before it can be compared
against conditional request headers. A controller can add an ETag and "Cache-Control"
settings to a <code>ResponseEntity</code>:</p>
</div>

```
@GetMapping("/book/{id}")
public ResponseEntity<Book> showBook(@PathVariable Long id) {

    Book book = findBook(id);
    String version = book.getVersion();

    return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
            .eTag(version) // lastModified is also available
            .body(book);
}
```

<div class="paragraph">
<p>This will send an 304 (NOT_MODIFIED) response with an empty body, if the comparison
to the conditional request headers indicates the content has not changed. Otherwise the
"ETag" and "Cache-Control" headers will be added to the response.</p>
</div>
<div class="paragraph">
<p>The check against conditional request headers can also be made in the controller:</p>
</div>

```
@RequestMapping
public String myHandleMethod(WebRequest webRequest, Model model) {

    long eTag = ... (1)

    if (request.checkNotModified(eTag)) {
        return null;(2)
    }

    model.addAttribute(...);(3)
    return "myViewName";
}
```

<div class="colist arabic">
<table>
<tr>
<td><i class="conum" data-value="1"></i><b>1</b></td>
<td>Application-specific calculation.</td>
</tr>
<tr>
<td><i class="conum" data-value="2"></i><b>2</b></td>
<td>Response has been set to 304 (NOT_MODIFIED), no further processing.</td>
</tr>
<tr>
<td><i class="conum" data-value="3"></i><b>3</b></td>
<td>Continue with request processing.</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>There are 3 variants for checking conditional requests against eTag values, lastModified
values, or both. For conditional "GET" and "HEAD" requests, the response may be set to
304 (NOT_MODIFIED). For conditional "POST", "PUT", and "DELETE", the response would be set
to 409 (PRECONDITION_FAILED) instead to prevent concurrent modification.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-caching-static-resources"><a class="anchor" href="#mvc-caching-static-resources"></a>1.9.3. Static resources</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-caching-static-resources">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Static resources should be served with a "Cache-Control" and conditional response headers
for optimal performance. See section on configuring <a href="#mvc-config-static-resources">Static Resources</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-httpcaching-shallowetag"><a class="anchor" href="#mvc-httpcaching-shallowetag"></a>1.9.4. ETag Filter</h4>
<div class="paragraph">
<p>The <code>ShallowEtagHeaderFilter</code> can be used to add "shallow" eTag values, computed from the
response content and thus saving bandwith but not CPU time. See <a href="#filters-shallow-etag">Shallow ETag</a>.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-view"><a class="anchor" href="#mvc-view"></a>1.10. View Technologies</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The use of view technologies in Spring MVC is pluggable, whether you decide to
use Thymeleaf, Groovy Markup Templates, JSPs, or other, is primarily a matter of a
configuration change. This chapter covers view technologies integrated with Spring MVC.
We assume you are already familiar with <a href="#mvc-viewresolver">View Resolution</a>.</p>
</div>
<div class="sect3">
<h4 id="mvc-view-thymeleaf"><a class="anchor" href="#mvc-view-thymeleaf"></a>1.10.1. Thymeleaf</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-thymeleaf">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Thymeleaf is modern server-side Java template engine that emphasizes natural HTML
templates that can be previewed in a browser by double-clicking, which is very
helpful for independent work on UI templates, e.g. by designer, without the need for a
running server. If you&#8217;re looking to replace JSPs, Thymeleaf offers one of the most
extensive set of features that will make such a transition easier. Thymeleaf is actively
developed and maintained. For a more complete introduction see the
<a href="http://www.thymeleaf.org/">Thymeleaf</a> project home page.</p>
</div>
<div class="paragraph">
<p>The Thymeleaf integration with Spring MVC is managed by the Thymeleaf project. The
configuration involves a few bean declarations such as
<code>ServletContextTemplateResolver</code>, <code>SpringTemplateEngine</code>, and <code>ThymeleafViewResolver</code>.
See <a href="http://www.thymeleaf.org/documentation.html">Thymeleaf+Spring</a> for more details.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-freemarker"><a class="anchor" href="#mvc-view-freemarker"></a>1.10.2. FreeMarker</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-freemarker">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><a href="http://www.freemarker.org">Apache FreeMarker</a> is a template engine for generating any
kind of text output from HTML to email, and others. The Spring Framework has a built-in
integration for using Spring MVC with FreeMarker templates.</p>
</div>
<div class="sect4">
<h5 id="mvc-view-freemarker-contextconfig"><a class="anchor" href="#mvc-view-freemarker-contextconfig"></a>View config</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-freemarker-contextconfig">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>To configure FreeMarker as a view technology:</p>
</div>

```
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freemarker();
    }

    // Configure FreeMarker...

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("/WEB-INF/freemarker");
        return configurer;
    }
}
```

<div class="paragraph">
<p>To configure the same in XML:</p>
</div>

```
<mvc:annotation-driven/>

<mvc:view-resolvers>
    <mvc:freemarker/>
</mvc:view-resolvers>

<!-- Configure FreeMarker... -->
<mvc:freemarker-configurer>
    <mvc:template-loader-path location="/WEB-INF/freemarker"/>
</mvc:freemarker-configurer>
```

<div class="paragraph">
<p>Or you can also declare the <code>FreeMarkerConfigurer</code> bean for full control over all
properties:</p>
</div>

```
<bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
    <property name="templateLoaderPath" value="/WEB-INF/freemarker/"/>
</bean>
```

<div class="paragraph">
<p>Your templates need to be stored in the directory specified by the <code>FreeMarkerConfigurer</code>
shown above. Given the above configuration if your controller returns the view name
"welcome" then the resolver will look for the <code>/WEB-INF/freemarker/welcome.ftl</code> template.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-views-freemarker"><a class="anchor" href="#mvc-views-freemarker"></a>FreeMarker config</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-views-freemarker">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>FreeMarker 'Settings' and 'SharedVariables' can be passed directly to the FreeMarker
<code>Configuration</code> object managed by Spring by setting the appropriate bean properties on
the <code>FreeMarkerConfigurer</code> bean. The <code>freemarkerSettings</code> property requires a
<code>java.util.Properties</code> object and the <code>freemarkerVariables</code> property requires a
<code>java.util.Map</code>.</p>
</div>


```
<bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
    <property name="templateLoaderPath" value="/WEB-INF/freemarker/"/>
    <property name="freemarkerVariables">
        <map>
            <entry key="xml_escape" value-ref="fmXmlEscape"/>
        </map>
    </property>
</bean>

<bean id="fmXmlEscape" class="freemarker.template.utility.XmlEscape"/>
```

<div class="paragraph">
<p>See the FreeMarker documentation for details of settings and variables as they apply to
the <code>Configuration</code> object.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-freemarker-forms"><a class="anchor" href="#mvc-view-freemarker-forms"></a>Form handling</h5>
<div class="paragraph">
<p>Spring provides a tag library for use in JSP&#8217;s that contains, amongst others, a
<code>&lt;spring:bind/&gt;</code> tag. This tag primarily enables forms to display values from form
backing objects and to show the results of failed validations from a <code>Validator</code> in the
web or business tier. Spring also has support for the same functionality in FreeMarker,
with additional convenience macros for generating form input elements themselves.</p>
</div>
<div class="sect5">
<h6 id="mvc-view-bind-macros"><a class="anchor" href="#mvc-view-bind-macros"></a>The bind macros</h6>
<div class="paragraph">
<p>A standard set of macros are maintained within the <code>spring-webmvc.jar</code> file for both
languages, so they are always available to a suitably configured application.</p>
</div>
<div class="paragraph">
<p>Some of the macros defined in the Spring libraries are considered internal (private) but
no such scoping exists in the macro definitions making all macros visible to calling
code and user templates. The following sections concentrate only on the macros you need
to be directly calling from within your templates. If you wish to view the macro code
directly, the file is called <code>spring.ftl</code> in the package
<code>org.springframework.web.servlet.view.freemarker</code>.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-simple-binding"><a class="anchor" href="#mvc-view-simple-binding"></a>Simple binding</h6>
<div class="paragraph">
<p>In your HTML forms (vm / ftl templates) which act as a form view for a Spring MVC
controller, you can use code similar to the following to bind to field values and
display error messages for each input field in similar fashion to the JSP equivalent.
Example code is shown below for the <code>personForm</code> view configured earlier:</p>
</div>

```
<!-- freemarker macros have to be imported into a namespace. We strongly
recommend sticking to 'spring' -->
<#import "/spring.ftl" as spring/>
<html>
    ...
    <form action="" method="POST">
        Name:
        <@spring.bind "myModelObject.name"/>
        <input type="text"
            name="${spring.status.expression}"
            value="${spring.status.value?html}"/><br>
        <#list spring.status.errorMessages as error> <b>${error}</b> <br> </#list>
        <br>
        ...
        <input type="submit" value="submit"/>
    </form>
    ...
</html>
```

<div class="paragraph">
<p><code>&lt;@spring.bind&gt;</code> requires a 'path' argument which consists of the name of your command
object (it will be 'command' unless you changed it in your FormController properties)
followed by a period and the name of the field on the command object you wish to bind to.
Nested fields can be used too such as "command.address.street". The <code>bind</code> macro assumes
the default HTML escaping behavior specified by the ServletContext parameter
<code>defaultHtmlEscape</code> in <code>web.xml</code>.</p>
</div>
<div class="paragraph">
<p>The optional form of the macro called <code>&lt;@spring.bindEscaped&gt;</code> takes a second argument
and explicitly specifies whether HTML escaping should be used in the status error
messages or values. Set to true or false as required. Additional form handling macros
simplify the use of HTML escaping and these macros should be used wherever possible.
They are explained in the next section.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-views-form-macros"><a class="anchor" href="#mvc-views-form-macros"></a>Input macros</h6>
<div class="paragraph">
<p>Additional convenience macros for both languages simplify both binding and form
generation (including validation error display). It is never necessary to use these
macros to generate form input fields, and they can be mixed and matched with simple HTML
or calls direct to the spring bind macros highlighted previously.</p>
</div>
<div class="paragraph">
<p>The following table of available macros show the FTL definitions and the
parameter list that each takes.</p>
</div>
<table id="views-macros-defs-tbl" class="tableblock frame-all grid-all spread">
<caption class="title">Table 6. Table of macro definitions</caption>
<colgroup>
<col style="width: 75%;">
<col style="width: 25%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">macro</th>
<th class="tableblock halign-left valign-top">FTL definition</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>message</strong> (output a string from a resource bundle based on the code parameter)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.message code/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>messageText</strong> (output a string from a resource bundle based on the code parameter,
falling back to the value of the default parameter)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.messageText code, text/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>url</strong> (prefix a relative URL with the application&#8217;s context root)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.url relativeUrl/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formInput</strong> (standard input field for gathering user input)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formInput path, attributes, fieldType/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formHiddenInput </strong>* (hidden input field for submitting non-user input)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formHiddenInput path, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formPasswordInput</strong> * (standard input field for gathering passwords. Note that no
value will ever be populated in fields of this type)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formPasswordInput path, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formTextarea</strong> (large text field for gathering long, freeform text input)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formTextarea path, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formSingleSelect</strong> (drop down box of options allowing a single required value to be
selected)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formSingleSelect path, options, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formMultiSelect</strong> (a list box of options allowing the user to select 0 or more values)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formMultiSelect path, options, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formRadioButtons</strong> (a set of radio buttons allowing a single selection to be made
from the available choices)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formRadioButtons path, options separator, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formCheckboxes</strong> (a set of checkboxes allowing 0 or more values to be selected)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formCheckboxes path, options, separator, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>formCheckbox</strong> (a single checkbox)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.formCheckbox path, attributes/&gt;</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><strong>showErrors</strong> (simplify display of validation errors for the bound field)</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">&lt;@spring.showErrors separator, classOrStyle/&gt;</p></td>
</tr>
</tbody>
</table>
<div class="ulist">
<ul>
<li>
<p>In FTL (FreeMarker), <code>formHiddenInput</code> and <code>formPasswordInput</code> are not actually required
as you can use the normal <code>formInput</code> macro, specifying <code>hidden</code> or <code>password</code> as the
value for the <code>fieldType</code> parameter.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The parameters to any of the above macros have consistent meanings:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>path: the name of the field to bind to (ie "command.name")</p>
</li>
<li>
<p>options: a Map of all the available values that can be selected from in the input
field. The keys to the map represent the values that will be POSTed back from the form
and bound to the command object. Map objects stored against the keys are the labels
displayed on the form to the user and may be different from the corresponding values
posted back by the form. Usually such a map is supplied as reference data by the
controller. Any Map implementation can be used depending on required behavior. For
strictly sorted maps, a <code>SortedMap</code> such as a <code>TreeMap</code> with a suitable Comparator may
be used and for arbitrary Maps that should return values in insertion order, use a
<code>LinkedHashMap</code> or a <code>LinkedMap</code> from commons-collections.</p>
</li>
<li>
<p>separator: where multiple options are available as discreet elements (radio buttons or
checkboxes), the sequence of characters used to separate each one in the list (ie
"&lt;br&gt;").</p>
</li>
<li>
<p>attributes: an additional string of arbitrary tags or text to be included within the
HTML tag itself. This string is echoed literally by the macro. For example, in a
textarea field you may supply attributes as 'rows="5" cols="60"' or you could pass
style information such as 'style="border:1px solid silver"'.</p>
</li>
<li>
<p>classOrStyle: for the showErrors macro, the name of the CSS class that the span tag
wrapping each error will use. If no information is supplied (or the value is empty)
then the errors will be wrapped in &lt;b&gt;&lt;/b&gt; tags.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Examples of the macros are outlined below some in FTL and some in VTL. Where usage
differences exist between the two languages, they are explained in the notes.</p>
</div>
<div class="sect6">
<h7 id="mvc-views-form-macros-input"><a class="anchor" href="#mvc-views-form-macros-input"></a>Input Fields</h7>
<div class="paragraph">
<p>The formInput macro takes the path parameter (command.name) and an additional attributes
parameter which is empty in the example above. The macro, along with all other form
generation macros, performs an implicit spring bind on the path parameter. The binding
remains valid until a new bind occurs so the showErrors macro doesn&#8217;t need to pass the
path parameter again - it simply operates on whichever field a bind was last created for.</p>
</div>
<div class="paragraph">
<p>The showErrors macro takes a separator parameter (the characters that will be used to
separate multiple errors on a given field) and also accepts a second parameter, this
time a class name or style attribute. Note that FreeMarker is able to specify default
values for the attributes parameter.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="error">&lt;</span>@spring.formInput &quot;command.name&quot;/<span class="error">&gt;</span>
<span class="error">&lt;</span>@spring.showErrors &quot;<span class="tag">&lt;br&gt;</span>&quot;/<span class="error">&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>Output is shown below of the form fragment generating the name field, and displaying a
validation error after the form was submitted with no value in the field. Validation
occurs through Spring&#8217;s Validation framework.</p>
</div>
<div class="paragraph">
<p>The generated HTML looks like this:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">Name:
&lt;input type=&quot;text&quot; name=&quot;name&quot; value=&quot;&quot;&gt;
&lt;br&gt;
    &lt;b&gt;required&lt;/b&gt;
&lt;br&gt;
&lt;br&gt;</code></pre>
</div>
</div>
<div class="paragraph">
<p>The formTextarea macro works the same way as the formInput macro and accepts the same
parameter list. Commonly, the second parameter (attributes) will be used to pass style
information or rows and cols attributes for the textarea.</p>
</div>
</div>
<div class="sect6">
<h7 id="mvc-views-form-macros-select"><a class="anchor" href="#mvc-views-form-macros-select"></a>Selection Fields</h7>
<div class="paragraph">
<p>Four selection field macros can be used to generate common UI value selection inputs in
your HTML forms.</p>
</div>
<div class="ulist">
<ul>
<li>
<p>formSingleSelect</p>
</li>
<li>
<p>formMultiSelect</p>
</li>
<li>
<p>formRadioButtons</p>
</li>
<li>
<p>formCheckboxes</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Each of the four macros accepts a Map of options containing the value for the form
field, and the label corresponding to that value. The value and the label can be the
same.</p>
</div>
<div class="paragraph">
<p>An example of radio buttons in FTL is below. The form backing object specifies a default
value of 'London' for this field and so no validation is necessary. When the form is
rendered, the entire list of cities to choose from is supplied as reference data in the
model under the name 'cityMap'.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">...
Town:
&lt;@spring.formRadioButtons &quot;command.address.town&quot;, cityMap, &quot;&quot;/&gt;&lt;br&gt;&lt;br&gt;</code></pre>
</div>
</div>
<div class="paragraph">
<p>This renders a line of radio buttons, one for each value in <code>cityMap</code> using the
separator "". No additional attributes are supplied (the last parameter to the macro is
missing). The cityMap uses the same String for each key-value pair in the map. The map&#8217;s
keys are what the form actually submits as POSTed request parameters, map values are the
labels that the user sees. In the example above, given a list of three well known cities
and a default value in the form backing object, the HTML would be</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">Town:
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;London&quot;&gt;London&lt;/input&gt;
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;Paris&quot; checked=&quot;checked&quot;&gt;Paris&lt;/input&gt;
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;New York&quot;&gt;New York&lt;/input&gt;</code></pre>
</div>
</div>
<div class="paragraph">
<p>If your application expects to handle cities by internal codes for example, the map of
codes would be created with suitable keys like the example below.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">protected</span> <span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">String</span>&gt; referenceData(HttpServletRequest request) <span class="directive">throws</span> <span class="exception">Exception</span> {
    <span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">String</span>&gt; cityMap = <span class="keyword">new</span> <span class="predefined-type">LinkedHashMap</span>&lt;&gt;();
    cityMap.put(<span class="string"><span class="delimiter">&quot;</span><span class="content">LDN</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">London</span><span class="delimiter">&quot;</span></span>);
    cityMap.put(<span class="string"><span class="delimiter">&quot;</span><span class="content">PRS</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Paris</span><span class="delimiter">&quot;</span></span>);
    cityMap.put(<span class="string"><span class="delimiter">&quot;</span><span class="content">NYC</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">New York</span><span class="delimiter">&quot;</span></span>);

    <span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">String</span>&gt; model = <span class="keyword">new</span> <span class="predefined-type">HashMap</span>&lt;&gt;();
    model.put(<span class="string"><span class="delimiter">&quot;</span><span class="content">cityMap</span><span class="delimiter">&quot;</span></span>, cityMap);
    <span class="keyword">return</span> model;
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The code would now produce output where the radio values are the relevant codes but the
user still sees the more user friendly city names.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">Town:
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;LDN&quot;&gt;London&lt;/input&gt;
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;PRS&quot; checked=&quot;checked&quot;&gt;Paris&lt;/input&gt;
&lt;input type=&quot;radio&quot; name=&quot;address.town&quot; value=&quot;NYC&quot;&gt;New York&lt;/input&gt;</code></pre>
</div>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-views-form-macros-html-escaping"><a class="anchor" href="#mvc-views-form-macros-html-escaping"></a>HTML escaping</h6>
<div class="paragraph">
<p>Default usage of the form macros above will result in HTML tags that are HTML 4.01
compliant and that use the default value for HTML escaping defined in your web.xml as
used by Spring&#8217;s bind support. In order to make the tags XHTML compliant or to override
the default HTML escaping value, you can specify two variables in your template (or in
your model where they will be visible to your templates). The advantage of specifying
them in the templates is that they can be changed to different values later in the
template processing to provide different behavior for different fields in your form.</p>
</div>
<div class="paragraph">
<p>To switch to XHTML compliance for your tags, specify a value of <code>true</code> for a
model/context variable named xhtmlCompliant:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">&lt;#-- for FreeMarker --&gt;
&lt;#assign xhtmlCompliant = true&gt;</code></pre>
</div>
</div>
<div class="paragraph">
<p>Any tags generated by the Spring macros will now be XHTML compliant after processing
this directive.</p>
</div>
<div class="paragraph">
<p>In similar fashion, HTML escaping can be specified per field:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="jsp">&lt;#-- until this point, default HTML escaping is used --&gt;

&lt;#assign htmlEscape = true&gt;
&lt;#-- next field will use HTML escaping --&gt;
&lt;@spring.formInput &quot;command.name&quot;/&gt;

&lt;#assign htmlEscape = false in spring&gt;
&lt;#-- all future fields will be bound with HTML escaping off --&gt;</code></pre>
</div>
</div>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-groovymarkup"><a class="anchor" href="#mvc-view-groovymarkup"></a>1.10.3. Groovy Markup</h4>
<div class="paragraph">
<p><a href="http://groovy-lang.org/templating.html#_the_markuptemplateengine">Groovy Markup Template Engine</a>
is primarily aimed at generating XML-like markup (XML, XHTML, HTML5, etc) but that can
be used to generate any text based content. The Spring Framework has a built-in
integration for using Spring MVC with Groovy Markup.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The Groovy Markup Tempalte engine requires Groovy 2.3.1+.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect4">
<h5 id="mvc-view-groovymarkup-configuration"><a class="anchor" href="#mvc-view-groovymarkup-configuration"></a>Configuration</h5>
<div class="paragraph">
<p>To configure the Groovy Markup Template Engine:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureViewResolvers(ViewResolverRegistry registry) {
        registry.groovy();
    }

    <span class="comment">// Configure the Groovy Markup Template Engine...</span>

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> GroovyMarkupConfigurer groovyMarkupConfigurer() {
        GroovyMarkupConfigurer configurer = <span class="keyword">new</span> GroovyMarkupConfigurer();
        configurer.setResourceLoaderPath(<span class="string"><span class="delimiter">&quot;</span><span class="content">/WEB-INF/</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">return</span> configurer;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>To configure the same in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:annotation-driven</span><span class="tag">/&gt;</span>

<span class="tag">&lt;mvc:view-resolvers&gt;</span>
    <span class="tag">&lt;mvc:groovy</span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:view-resolvers&gt;</span>

<span class="comment">&lt;!-- Configure the Groovy Markup Template Engine... --&gt;</span>
<span class="tag">&lt;mvc:groovy-configurer</span> <span class="attribute-name">resource-loader-path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/WEB-INF/</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-groovymarkup-example"><a class="anchor" href="#mvc-view-groovymarkup-example"></a>Example</h5>
<div class="paragraph">
<p>Unlike traditional template engines, Groovy Markup relies on a DSL that uses a builder
syntax. Here is a sample template for an HTML page:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="groovy">yieldUnescaped <span class="string"><span class="delimiter">'</span><span class="content">&lt;!DOCTYPE html&gt;</span><span class="delimiter">'</span></span>
html(<span class="key">lang</span>:<span class="string"><span class="delimiter">'</span><span class="content">en</span><span class="delimiter">'</span></span>) {
    head {
        meta(<span class="string"><span class="delimiter">'</span><span class="content">http-equiv</span><span class="delimiter">'</span></span>:<span class="string"><span class="delimiter">'</span><span class="content">&quot;Content-Type&quot; content=&quot;text/html; charset=utf-8&quot;</span><span class="delimiter">'</span></span>)
        title(<span class="string"><span class="delimiter">'</span><span class="content">My page</span><span class="delimiter">'</span></span>)
    }
    body {
        p(<span class="string"><span class="delimiter">'</span><span class="content">This is an example of HTML contents</span><span class="delimiter">'</span></span>)
    }
}</code></pre>
</div>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-script"><a class="anchor" href="#mvc-view-script"></a>1.10.4. Script Views</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-script">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The Spring Framework has a built-in integration for using Spring MVC with any
templating library that can run on top of the
<a href="https://www.jcp.org/en/jsr/detail?id=223">JSR-223</a> Java scripting engine. Below is a list
of templating libraries we&#8217;ve tested on different script engines:</p>
</div>
<div class="hdlist">
<table>
<tr>
<td class="hdlist1">
<a href="http://handlebarsjs.com/">Handlebars</a>
</td>
<td class="hdlist2">
<p><a href="http://openjdk.java.net/projects/nashorn/">Nashorn</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="https://mustache.github.io/">Mustache</a>
</td>
<td class="hdlist2">
<p><a href="http://openjdk.java.net/projects/nashorn/">Nashorn</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="https://facebook.github.io/react/">React</a>
</td>
<td class="hdlist2">
<p><a href="http://openjdk.java.net/projects/nashorn/">Nashorn</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="http://www.embeddedjs.com/">EJS</a>
</td>
<td class="hdlist2">
<p><a href="http://openjdk.java.net/projects/nashorn/">Nashorn</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="http://www.stuartellis.eu/articles/erb/">ERB</a>
</td>
<td class="hdlist2">
<p><a href="http://jruby.org">JRuby</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="https://docs.python.org/2/library/string.html#template-strings">String templates</a>
</td>
<td class="hdlist2">
<p><a href="http://www.jython.org/">Jython</a></p>
</td>
</tr>
<tr>
<td class="hdlist1">
<a href="https://github.com/sdeleuze/kotlin-script-templating">Kotlin Script templating</a>
</td>
<td class="hdlist2">
<p><a href="https://kotlinlang.org/">Kotlin</a></p>
</td>
</tr>
</table>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The basic rule for integrating any other script engine is that it must implement the
<code>ScriptEngine</code> and <code>Invocable</code> interfaces.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect4">
<h5 id="mvc-view-script-dependencies"><a class="anchor" href="#mvc-view-script-dependencies"></a>Requirements</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-script-dependencies">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You need to have the script engine on your classpath:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="http://openjdk.java.net/projects/nashorn/">Nashorn</a> JavaScript engine is provided with
Java 8+. Using the latest update release available is highly recommended.</p>
</li>
<li>
<p><a href="http://jruby.org">JRuby</a> should be added as a dependency for Ruby support.</p>
</li>
<li>
<p><a href="http://www.jython.org">Jython</a> should be added as a dependency for Python support.</p>
</li>
<li>
<p><code>org.jetbrains.kotlin:kotlin-script-util</code> dependency and a <code>META-INF/services/javax.script.ScriptEngineFactory</code>
file containing a <code>org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory</code>
line should be added for Kotlin script support, see
<a href="https://github.com/sdeleuze/kotlin-script-templating">this example</a> for more details.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>You need to have the script templating library. One way to do that for Javascript is
through <a href="http://www.webjars.org/">WebJars</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-script-integrate"><a class="anchor" href="#mvc-view-script-integrate"></a>Script templates</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-script-integrate">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Declare a <code>ScriptTemplateConfigurer</code> bean in order to specify the script engine to use,
the script files to load, what function to call to render templates, and so on.
Below is an example with Mustache templates and the Nashorn JavaScript engine:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureViewResolvers(ViewResolverRegistry registry) {
        registry.scriptTemplate();
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> ScriptTemplateConfigurer configurer() {
        ScriptTemplateConfigurer configurer = <span class="keyword">new</span> ScriptTemplateConfigurer();
        configurer.setEngineName(<span class="string"><span class="delimiter">&quot;</span><span class="content">nashorn</span><span class="delimiter">&quot;</span></span>);
        configurer.setScripts(<span class="string"><span class="delimiter">&quot;</span><span class="content">mustache.js</span><span class="delimiter">&quot;</span></span>);
        configurer.setRenderObject(<span class="string"><span class="delimiter">&quot;</span><span class="content">Mustache</span><span class="delimiter">&quot;</span></span>);
        configurer.setRenderFunction(<span class="string"><span class="delimiter">&quot;</span><span class="content">render</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">return</span> configurer;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The same in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:annotation-driven</span><span class="tag">/&gt;</span>

<span class="tag">&lt;mvc:view-resolvers&gt;</span>
    <span class="tag">&lt;mvc:script-template</span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:view-resolvers&gt;</span>

<span class="tag">&lt;mvc:script-template-configurer</span> <span class="attribute-name">engine-name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">nashorn</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">render-object</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Mustache</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">render-function</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">render</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;mvc:script</span> <span class="attribute-name">location</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">mustache.js</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:script-template-configurer&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The controller would look no different:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">SampleController</span> {

    <span class="annotation">@GetMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/sample</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="predefined-type">String</span> test(Model model) {
        model.addObject(<span class="string"><span class="delimiter">&quot;</span><span class="content">title</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Sample title</span><span class="delimiter">&quot;</span></span>);
        model.addObject(<span class="string"><span class="delimiter">&quot;</span><span class="content">body</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Sample body</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">return</span> <span class="string"><span class="delimiter">&quot;</span><span class="content">template</span><span class="delimiter">&quot;</span></span>;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And the Mustache template is:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="html"><span class="tag">&lt;html&gt;</span>
    <span class="tag">&lt;head&gt;</span>
        <span class="tag">&lt;title&gt;</span>{{title}}<span class="tag">&lt;/title&gt;</span>
    <span class="tag">&lt;/head&gt;</span>
    <span class="tag">&lt;body&gt;</span>
        <span class="tag">&lt;p&gt;</span>{{body}}<span class="tag">&lt;/p&gt;</span>
    <span class="tag">&lt;/body&gt;</span>
<span class="tag">&lt;/html&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The render function is called with the following parameters:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>String template</code>: the template content</p>
</li>
<li>
<p><code>Map model</code>: the view model</p>
</li>
<li>
<p><code>RenderingContext renderingContext</code>: the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/view/script/RenderingContext.html">RenderingContext</a>
that gives access to the application context, the locale, the template loader and the
url (since 5.0)</p>
</li>
</ul>
</div>
<div class="paragraph">
<p><code>Mustache.render()</code> is natively compatible with this signature, so you can call it directly.</p>
</div>
<div class="paragraph">
<p>If your templating technology requires some customization, you may provide a script that
implements a custom render function. For example, <a href="http://handlebarsjs.com">Handlerbars</a>
needs to compile templates before using them, and requires a
<a href="https://en.wikipedia.org/wiki/Polyfill">polyfill</a> in order to emulate some
browser facilities not available in the server-side script engine.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureViewResolvers(ViewResolverRegistry registry) {
        registry.scriptTemplate();
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> ScriptTemplateConfigurer configurer() {
        ScriptTemplateConfigurer configurer = <span class="keyword">new</span> ScriptTemplateConfigurer();
        configurer.setEngineName(<span class="string"><span class="delimiter">&quot;</span><span class="content">nashorn</span><span class="delimiter">&quot;</span></span>);
        configurer.setScripts(<span class="string"><span class="delimiter">&quot;</span><span class="content">polyfill.js</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">handlebars.js</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">render.js</span><span class="delimiter">&quot;</span></span>);
        configurer.setRenderFunction(<span class="string"><span class="delimiter">&quot;</span><span class="content">render</span><span class="delimiter">&quot;</span></span>);
        configurer.setSharedEngine(<span class="predefined-constant">false</span>);
        <span class="keyword">return</span> configurer;
    }
}</code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Setting the <code>sharedEngine</code> property to <code>false</code> is required when using non thread-safe
script engines with templating libraries not designed for concurrency, like Handlebars or
React running on Nashorn for example. In that case, Java 8u60 or greater is required due
to <a href="https://bugs.openjdk.java.net/browse/JDK-8076099">this bug</a>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p><code>polyfill.js</code> only defines the <code>window</code> object needed by Handlebars to run properly:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="javascript"><span class="keyword">var</span> window = {};</code></pre>
</div>
</div>
<div class="paragraph">
<p>This basic <code>render.js</code> implementation compiles the template before using it. A production
ready implementation should also store and reused cached templates / pre-compiled templates.
This can be done on the script side, as well as any customization you need (managing
template engine configuration for example).</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="javascript"><span class="keyword">function</span> <span class="function">render</span>(template, model) {
    <span class="keyword">var</span> compiledTemplate = Handlebars.compile(template);
    <span class="keyword">return</span> compiledTemplate(model);
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Check out the Spring Framework unit tests,
<a href="https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc/src/test/java/org/springframework/web/servlet/view/script">java</a>, and
<a href="https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc/src/test/resources/org/springframework/web/servlet/view/script">resources</a>,
for more configuration examples.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-jsp"><a class="anchor" href="#mvc-view-jsp"></a>1.10.5. JSP &amp; JSTL</h4>
<div class="paragraph">
<p>The Spring Framework has a built-in integration for using Spring MVC with JSP and JSTL.</p>
</div>
<div class="sect4">
<h5 id="mvc-view-jsp-resolver"><a class="anchor" href="#mvc-view-jsp-resolver"></a>View resolvers</h5>
<div class="paragraph">
<p>When developing with JSPs you can declare a <code>InternalResourceViewResolver</code> or a
<code>ResourceBundleViewResolver</code> bean.</p>
</div>
<div class="paragraph">
<p><code>ResourceBundleViewResolver</code> relies on a properties file to define the view names
mapped to a class and a URL. With a <code>ResourceBundleViewResolver</code> you
can mix different types of views using only one resolver. Here is an example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="comment">&lt;!-- the ResourceBundleViewResolver --&gt;</span>
<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewResolver</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.ResourceBundleViewResolver</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">basename</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">views</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/bean&gt;</span>

# And a sample properties file is uses (views.properties in WEB-INF/classes):
welcome.(class)=org.springframework.web.servlet.view.JstlView
welcome.url=/WEB-INF/jsp/welcome.jsp

productList.(class)=org.springframework.web.servlet.view.JstlView
productList.url=/WEB-INF/jsp/productlist.jsp</code></pre>
</div>
</div>
<div class="paragraph">
<p><code>InternalResourceBundleViewResolver</code> can also be used for JSPs. As a best practice, we
strongly encourage placing your JSP files in a directory under the <code>'WEB-INF'</code>
directory so there can be no direct access by clients.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewResolver</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.InternalResourceViewResolver</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewClass</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.JstlView</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">prefix</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/WEB-INF/jsp/</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">suffix</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">.jsp</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-jsp-jstl"><a class="anchor" href="#mvc-view-jsp-jstl"></a>JSPs versus JSTL</h5>
<div class="paragraph">
<p>When using the Java Standard Tag Library you must use a special view class, the
<code>JstlView</code>, as JSTL needs some preparation before things such as the I18N features will
work.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-jsp-tags"><a class="anchor" href="#mvc-view-jsp-tags"></a>Spring&#8217;s JSP tag library</h5>
<div class="paragraph">
<p>Spring provides data binding of request parameters to command objects as described in
earlier chapters. To facilitate the development of JSP pages in combination with those
data binding features, Spring provides a few tags that make things even easier. All
Spring tags have<em>HTML escaping</em> features to enable or disable escaping of characters.</p>
</div>
<div class="paragraph">
<p>The <code>spring.tld</code> tag library descriptor (TLD) is included in the <code>spring-webmvc.jar</code>.
For a comprehensive reference on individual tags, browse the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/tags/package-summary.html#package.description">API reference</a>
or see the tag library description.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-jsp-formtaglib"><a class="anchor" href="#mvc-view-jsp-formtaglib"></a>Spring&#8217;s form tag library</h5>
<div class="paragraph">
<p>As of version 2.0, Spring provides a comprehensive set of data binding-aware tags for
handling form elements when using JSP and Spring Web MVC. Each tag provides support for
the set of attributes of its corresponding HTML tag counterpart, making the tags
familiar and intuitive to use. The tag-generated HTML is HTML 4.01/XHTML 1.0 compliant.</p>
</div>
<div class="paragraph">
<p>Unlike other form/input tag libraries, Spring&#8217;s form tag library is integrated with
Spring Web MVC, giving the tags access to the command object and reference data your
controller deals with. As you will see in the following examples, the form tags make
JSPs easier to develop, read and maintain.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s go through the form tags and look at an example of how each tag is used. We have
included generated HTML snippets where certain tags require further commentary.</p>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-configuration"><a class="anchor" href="#mvc-view-jsp-formtaglib-configuration"></a>Configuration</h6>
<div class="paragraph">
<p>The form tag library comes bundled in <code>spring-webmvc.jar</code>. The library descriptor is
called <code>spring-form.tld</code>.</p>
</div>
<div class="paragraph">
<p>To use the tags from this library, add the following directive to the top of your JSP
page:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="error">&lt;</span>%@ taglib prefix=&quot;form&quot; uri=&quot;http://www.springframework.org/tags/form&quot; %<span class="error">&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>where <code>form</code> is the tag name prefix you want to use for the tags from this library.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-formtag"><a class="anchor" href="#mvc-view-jsp-formtaglib-formtag"></a>The form tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'form' tag and exposes a binding path to inner tags for
binding. It puts the command object in the <code>PageContext</code> so that the command object can
be accessed by inner tags. <em>All the other tags in this library are nested tags of the
<code>form</code> tag</em>.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s assume we have a domain object called <code>User</code>. It is a JavaBean with properties
such as <code>firstName</code> and <code>lastName</code>. We will use it as the form backing object of our
form controller which returns <code>form.jsp</code>. Below is an example of what <code>form.jsp</code> would
look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">2</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The <code>firstName</code> and <code>lastName</code> values are retrieved from the command object placed in
the <code>PageContext</code> by the page controller. Keep reading to see more complex examples of
how inner tags are used with the <code>form</code> tag.</p>
</div>
<div class="paragraph">
<p>The generated HTML looks like a standard form:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form</span> <span class="attribute-name">method</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">POST</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Harry</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Potter</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">2</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The preceding JSP assumes that the variable name of the form backing object is
<code>'command'</code>. If you have put the form backing object into the model under another name
(definitely a best practice), then you can bind the form to the named variable like so:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form</span> <span class="attribute-name">modelAttribute</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">user</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">2</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-inputtag"><a class="anchor" href="#mvc-view-jsp-formtaglib-inputtag"></a>The input tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'input' tag using the bound value and type='text' by default.
For an example of this tag, see <a href="#mvc-view-jsp-formtaglib-formtag">The form tag</a>. Starting with Spring
3.1 you can use other types such HTML5-specific types like 'email', 'tel', 'date', and
others.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-checkboxtag"><a class="anchor" href="#mvc-view-jsp-formtaglib-checkboxtag"></a>The checkbox tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'input' tag with type 'checkbox'.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s assume our <code>User</code> has preferences such as newsletter subscription and a list of
hobbies. Below is an example of the <code>Preferences</code> class:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">Preferences</span> {

    <span class="directive">private</span> <span class="type">boolean</span> receiveNewsletter;
    <span class="directive">private</span> <span class="predefined-type">String</span><span class="type">[]</span> interests;
    <span class="directive">private</span> <span class="predefined-type">String</span> favouriteWord;

    <span class="directive">public</span> <span class="type">boolean</span> isReceiveNewsletter() {
        <span class="keyword">return</span> receiveNewsletter;
    }

    <span class="directive">public</span> <span class="type">void</span> setReceiveNewsletter(<span class="type">boolean</span> receiveNewsletter) {
        <span class="local-variable">this</span>.receiveNewsletter = receiveNewsletter;
    }

    <span class="directive">public</span> <span class="predefined-type">String</span><span class="type">[]</span> getInterests() {
        <span class="keyword">return</span> interests;
    }

    <span class="directive">public</span> <span class="type">void</span> setInterests(<span class="predefined-type">String</span><span class="type">[]</span> interests) {
        <span class="local-variable">this</span>.interests = interests;
    }

    <span class="directive">public</span> <span class="predefined-type">String</span> getFavouriteWord() {
        <span class="keyword">return</span> favouriteWord;
    }

    <span class="directive">public</span> <span class="type">void</span> setFavouriteWord(<span class="predefined-type">String</span> favouriteWord) {
        <span class="local-variable">this</span>.favouriteWord = favouriteWord;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The <code>form.jsp</code> would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Subscribe to newsletter?:<span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Approach 1: Property is of type java.lang.Boolean --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:checkbox</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.receiveNewsletter</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>

        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Interests:<span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Approach 2: Property is of an array or of type java.util.Collection --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span>
                Quidditch: <span class="tag">&lt;form:checkbox</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Quidditch</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
                Herbology: <span class="tag">&lt;form:checkbox</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Herbology</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
                Defence Against the Dark Arts: <span class="tag">&lt;form:checkbox</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Defence Against the Dark Arts</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>

        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Favourite Word:<span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Approach 3: Property is of type java.lang.Object --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span>
                Magic: <span class="tag">&lt;form:checkbox</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.favouriteWord</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Magic</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>There are 3 approaches to the <code>checkbox</code> tag which should meet all your checkbox needs.</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Approach One - When the bound value is of type <code>java.lang.Boolean</code>, the
<code>input(checkbox)</code> is marked as 'checked' if the bound value is <code>true</code>. The <code>value</code>
attribute corresponds to the resolved value of the <code>setValue(Object)</code> value property.</p>
</li>
<li>
<p>Approach Two - When the bound value is of type <code>array</code> or <code>java.util.Collection</code>, the
<code>input(checkbox)</code> is marked as 'checked' if the configured <code>setValue(Object)</code> value is
present in the bound <code>Collection</code>.</p>
</li>
<li>
<p>Approach Three - For any other bound value type, the <code>input(checkbox)</code> is marked as
'checked' if the configured <code>setValue(Object)</code> is equal to the bound value.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Note that regardless of the approach, the same HTML structure is generated. Below is an
HTML snippet of some checkboxes:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Interests:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        Quidditch: <span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">checkbox</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Quidditch</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">hidden</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">1</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">_preferences.interests</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        Herbology: <span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">checkbox</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Herbology</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">hidden</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">1</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">_preferences.interests</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        Defence Against the Dark Arts: <span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">checkbox</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Defence Against the Dark Arts</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">hidden</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">1</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">_preferences.interests</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>What you might not expect to see is the additional hidden field after each checkbox.
When a checkbox in an HTML page is <em>not</em> checked, its value will not be sent to the
server as part of the HTTP request parameters once the form is submitted, so we need a
workaround for this quirk in HTML in order for Spring form data binding to work. The
<code>checkbox</code> tag follows the existing Spring convention of including a hidden parameter
prefixed by an underscore ("_") for each checkbox. By doing this, you are effectively
telling Spring that "<em>the checkbox was visible in the form and I want my object to
which the form data will be bound to reflect the state of the checkbox no matter what</em>".</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-checkboxestag"><a class="anchor" href="#mvc-view-jsp-formtaglib-checkboxestag"></a>The checkboxes tag</h6>
<div class="paragraph">
<p>This tag renders multiple HTML 'input' tags with type 'checkbox'.</p>
</div>
<div class="paragraph">
<p>Building on the example from the previous <code>checkbox</code> tag section. Sometimes you prefer
not to have to list all the possible hobbies in your JSP page. You would rather provide
a list at runtime of the available options and pass that in to the tag. That is the
purpose of the <code>checkboxes</code> tag. You pass in an <code>Array</code>, a <code>List</code> or a <code>Map</code> containing
the available options in the "items" property. Typically the bound property is a
collection so it can hold multiple values selected by the user. Below is an example of
the JSP using this tag:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Interests:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span>
                <span class="error">&lt;</span>%-- Property is of an array or of type java.util.Collection --%<span class="error">&gt;</span>
                <span class="tag">&lt;form:checkboxes</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preferences.interests</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">items</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">${interestList}</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>This example assumes that the "interestList" is a <code>List</code> available as a model attribute
containing strings of the values to be selected from. In the case where you use a Map,
the map entry key will be used as the value and the map entry&#8217;s value will be used as
the label to be displayed. You can also use a custom object where you can provide the
property names for the value using "itemValue" and the label using "itemLabel".</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-radiobuttontag"><a class="anchor" href="#mvc-view-jsp-formtaglib-radiobuttontag"></a>The radiobutton tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'input' tag with type 'radio'.</p>
</div>
<div class="paragraph">
<p>A typical usage pattern will involve multiple tag instances bound to the same property
but with different values.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Sex:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        Male: <span class="tag">&lt;form:radiobutton</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">sex</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">M</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span> <span class="tag">&lt;br</span><span class="tag">/&gt;</span>
        Female: <span class="tag">&lt;form:radiobutton</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">sex</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">F</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-radiobuttonstag"><a class="anchor" href="#mvc-view-jsp-formtaglib-radiobuttonstag"></a>The radiobuttons tag</h6>
<div class="paragraph">
<p>This tag renders multiple HTML 'input' tags with type 'radio'.</p>
</div>
<div class="paragraph">
<p>Just like the <code>checkboxes</code> tag above, you might want to pass in the available options as
a runtime variable. For this usage you would use the <code>radiobuttons</code> tag. You pass in an
<code>Array</code>, a <code>List</code> or a <code>Map</code> containing the available options in the "items" property.
In the case where you use a Map, the map entry key will be used as the value and the map
entry&#8217;s value will be used as the label to be displayed. You can also use a custom
object where you can provide the property names for the value using "itemValue" and the
label using "itemLabel".</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Sex:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:radiobuttons</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">sex</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">items</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">${sexOptions}</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-passwordtag"><a class="anchor" href="#mvc-view-jsp-formtaglib-passwordtag"></a>The password tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'input' tag with type 'password' using the bound value.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Password:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;form:password</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">password</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>Please note that by default, the password value is <em>not</em> shown. If you do want the
password value to be shown, then set the value of the <code>'showPassword'</code> attribute to
true, like so.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Password:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;form:password</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">password</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">^76525bvHGq</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">showPassword</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-selecttag"><a class="anchor" href="#mvc-view-jsp-formtaglib-selecttag"></a>The select tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'select' element. It supports data binding to the selected
option as well as the use of nested <code>option</code> and <code>options</code> tags.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s assume a <code>User</code> has a list of skills.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Skills:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:select</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">skills</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">items</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">${skills}</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If the <code>User&#8217;s</code> skill were in Herbology, the HTML source of the 'Skills' row would look
like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Skills:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;select</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">skills</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">multiple</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Potions</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Potions<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Herbology</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">selected</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">selected</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Herbology<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Quidditch</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Quidditch<span class="tag">&lt;/option&gt;</span>
        <span class="tag">&lt;/select&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-optiontag"><a class="anchor" href="#mvc-view-jsp-formtaglib-optiontag"></a>The option tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'option'. It sets 'selected' as appropriate based on the bound
value.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>House:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;form:select</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">house</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;form:option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Gryffindor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;form:option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Hufflepuff</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;form:option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Ravenclaw</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;form:option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Slytherin</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/form:select&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If the <code>User&#8217;s</code> house was in Gryffindor, the HTML source of the 'House' row would look
like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>House:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;select</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">house</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Gryffindor</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">selected</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">selected</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Gryffindor<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Hufflepuff</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Hufflepuff<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Ravenclaw</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Ravenclaw<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Slytherin</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Slytherin<span class="tag">&lt;/option&gt;</span>
        <span class="tag">&lt;/select&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-optionstag"><a class="anchor" href="#mvc-view-jsp-formtaglib-optionstag"></a>The options tag</h6>
<div class="paragraph">
<p>This tag renders a list of HTML 'option' tags. It sets the 'selected' attribute as
appropriate based on the bound value.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Country:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;form:select</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">country</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;form:option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">-</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">label</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">--Please Select</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;form:options</span> <span class="attribute-name">items</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">${countryList}</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">itemValue</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">code</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">itemLabel</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">name</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/form:select&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If the <code>User</code> lived in the UK, the HTML source of the 'Country' row would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Country:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span>
        <span class="tag">&lt;select</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">country</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">-</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>--Please Select<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">AT</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Austria<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">UK</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">selected</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">selected</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>United Kingdom<span class="tag">&lt;/option&gt;</span>
            <span class="tag">&lt;option</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">US</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>United States<span class="tag">&lt;/option&gt;</span>
        <span class="tag">&lt;/select&gt;</span>
    <span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>As the example shows, the combined usage of an <code>option</code> tag with the <code>options</code> tag
generates the same standard HTML, but allows you to explicitly specify a value in the
JSP that is for display only (where it belongs) such as the default string in the
example: "-- Please Select".</p>
</div>
<div class="paragraph">
<p>The <code>items</code> attribute is typically populated with a collection or array of item objects.
<code>itemValue</code> and <code>itemLabel</code> simply refer to bean properties of those item objects, if
specified; otherwise, the item objects themselves will be stringified. Alternatively,
you may specify a <code>Map</code> of items, in which case the map keys are interpreted as option
values and the map values correspond to option labels. If <code>itemValue</code> and/or <code>itemLabel</code>
happen to be specified as well, the item value property will apply to the map key and
the item label property will apply to the map value.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-textareatag"><a class="anchor" href="#mvc-view-jsp-formtaglib-textareatag"></a>The textarea tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'textarea'.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;tr&gt;</span>
    <span class="tag">&lt;td&gt;</span>Notes:<span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:textarea</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">notes</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">rows</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">cols</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">20</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
    <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">notes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
<span class="tag">&lt;/tr&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-hiddeninputtag"><a class="anchor" href="#mvc-view-jsp-formtaglib-hiddeninputtag"></a>The hidden tag</h6>
<div class="paragraph">
<p>This tag renders an HTML 'input' tag with type 'hidden' using the bound value. To submit
an unbound hidden value, use the HTML <code>input</code> tag with type 'hidden'.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:hidden</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">house</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If we choose to submit the 'house' value as a hidden one, the HTML would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">house</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">hidden</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Gryffindor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-errorstag"><a class="anchor" href="#mvc-view-jsp-formtaglib-errorstag"></a>The errors tag</h6>
<div class="paragraph">
<p>This tag renders field errors in an HTML 'span' tag. It provides access to the errors
created in your controller or those that were created by any validators associated with
your controller.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s assume we want to display all error messages for the <code>firstName</code> and <code>lastName</code>
fields once we submit the form. We have a validator for instances of the <code>User</code> class
called <code>UserValidator</code>.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">UserValidator</span> <span class="directive">implements</span> <span class="predefined-type">Validator</span> {

    <span class="directive">public</span> <span class="type">boolean</span> supports(<span class="predefined-type">Class</span> candidate) {
        <span class="keyword">return</span> User.class.isAssignableFrom(candidate);
    }

    <span class="directive">public</span> <span class="type">void</span> validate(<span class="predefined-type">Object</span> obj, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, <span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">required</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Field is required.</span><span class="delimiter">&quot;</span></span>);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, <span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">required</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Field is required.</span><span class="delimiter">&quot;</span></span>);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The <code>form.jsp</code> would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Show errors for firstName field --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>

        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Show errors for lastName field --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If we submit a form with empty values in the <code>firstName</code> and <code>lastName</code> fields, this is
what the HTML would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form</span> <span class="attribute-name">method</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">POST</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Associated errors to firstName field displayed --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;span</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName.errors</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Field is required.<span class="tag">&lt;/span&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>

        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="error">&lt;</span>%-- Associated errors to lastName field displayed --%<span class="error">&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;span</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName.errors</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Field is required.<span class="tag">&lt;/span&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>What if we want to display the entire list of errors for a given page? The example below
shows that the <code>errors</code> tag also supports some basic wildcarding functionality.</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>path="*"</code> - displays all errors</p>
</li>
<li>
<p><code>path="lastName"</code> - displays all errors associated with the <code>lastName</code> field</p>
</li>
<li>
<p>if <code>path</code> is omitted - object errors only are displayed</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The example below will display a list of errors at the top of the page, followed by
field-specific errors next to the fields:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form&gt;</span>
    <span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">*</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">cssClass</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">errorBox</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:input</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;form:errors</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The HTML would look like:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form</span> <span class="attribute-name">method</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">POST</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;span</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">*.errors</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">errorBox</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Field is required.<span class="tag">&lt;br</span><span class="tag">/&gt;</span>Field is required.<span class="tag">&lt;/span&gt;</span>
    <span class="tag">&lt;table&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>First Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;span</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">firstName.errors</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Field is required.<span class="tag">&lt;/span&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>

        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td&gt;</span>Last Name:<span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/td&gt;</span>
            <span class="tag">&lt;td&gt;</span><span class="tag">&lt;span</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">lastName.errors</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>Field is required.<span class="tag">&lt;/span&gt;</span><span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
        <span class="tag">&lt;tr&gt;</span>
            <span class="tag">&lt;td</span> <span class="attribute-name">colspan</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Save Changes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/td&gt;</span>
        <span class="tag">&lt;/tr&gt;</span>
    <span class="tag">&lt;/table&gt;</span>
<span class="tag">&lt;/form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The <code>spring-form.tld</code> tag library descriptor (TLD) is included in the <code>spring-webmvc.jar</code>.
For a comprehensive reference on individual tags, browse the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/tags/form/package-summary.html#package.description">API reference</a>
or see the tag library description.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-rest-method-conversion"><a class="anchor" href="#mvc-rest-method-conversion"></a>HTTP method conversion</h6>
<div class="paragraph">
<p>A key principle of REST is the use of the Uniform Interface. This means that all
resources (URLs) can be manipulated using the same four HTTP methods: GET, PUT, POST,
and DELETE. For each method, the HTTP specification defines the exact semantics. For
instance, a GET should always be a safe operation, meaning that is has no side effects,
and a PUT or DELETE should be idempotent, meaning that you can repeat these operations
over and over again, but the end result should be the same. While HTTP defines these
four methods, HTML only supports two: GET and POST. Fortunately, there are two possible
workarounds: you can either use JavaScript to do your PUT or DELETE, or simply do a POST
with the 'real' method as an additional parameter (modeled as a hidden input field in an
HTML form). This latter trick is what Spring&#8217;s <code>HiddenHttpMethodFilter</code> does. This
filter is a plain Servlet Filter and therefore it can be used in combination with any
web framework (not just Spring MVC). Simply add this filter to your web.xml, and a POST
with a hidden _method parameter will be converted into the corresponding HTTP method
request.</p>
</div>
<div class="paragraph">
<p>To support HTTP method conversion the Spring MVC form tag was updated to support setting
the HTTP method. For example, the following snippet taken from the updated Petclinic
sample</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;form:form</span> <span class="attribute-name">method</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">delete</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;p</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span><span class="tag">&lt;input</span> <span class="attribute-name">type</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">submit</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Delete Pet</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/p&gt;</span>
<span class="tag">&lt;/form:form&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>This will actually perform an HTTP POST, with the 'real' DELETE method hidden behind a
request parameter, to be picked up by the <code>HiddenHttpMethodFilter</code>, as defined in
web.xml:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">&lt;filter&gt;
    &lt;filter-name&gt;httpMethodFilter&lt;/filter-name&gt;
    &lt;filter-<span class="type">class</span>&gt;<span class="class">org</span>.springframework.web.filter.HiddenHttpMethodFilter&lt;/filter-<span class="type">class</span>&gt;
&lt;/<span class="class">filter</span>&gt;

&lt;filter-mapping&gt;
    &lt;filter-name&gt;httpMethodFilter&lt;/filter-name&gt;
    &lt;servlet-name&gt;petclinic&lt;/servlet-name&gt;
&lt;/filter-mapping&gt;</code></pre>
</div>
</div>
<div class="paragraph">
<p>The corresponding <code>@Controller</code> method is shown below:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@RequestMapping</span>(method = RequestMethod.DELETE)
<span class="directive">public</span> <span class="predefined-type">String</span> deletePet(<span class="annotation">@PathVariable</span> <span class="type">int</span> ownerId, <span class="annotation">@PathVariable</span> <span class="type">int</span> petId) {
    <span class="local-variable">this</span>.clinic.deletePet(petId);
    <span class="keyword">return</span> <span class="string"><span class="delimiter">&quot;</span><span class="content">redirect:/owners/</span><span class="delimiter">&quot;</span></span> + ownerId;
}</code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-jsp-formtaglib-html5"><a class="anchor" href="#mvc-view-jsp-formtaglib-html5"></a>HTML5 tags</h6>
<div class="paragraph">
<p>Starting with Spring 3, the Spring form tag library allows entering dynamic attributes,
which means you can enter any HTML5 specific attributes.</p>
</div>
<div class="paragraph">
<p>In Spring 3.1, the form input tag supports entering a type attribute other than 'text'.
This is intended to allow rendering new HTML5 specific input types such as 'email',
'date', 'range', and others. Note that entering type='text' is not required since 'text'
is the default type.</p>
</div>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-tiles"><a class="anchor" href="#mvc-view-tiles"></a>1.10.6. Tiles</h4>
<div class="paragraph">
<p>It is possible to integrate Tiles - just as any other view technology - in web
applications using Spring. The following describes in a broad way how to do this.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>This section focuses on Spring&#8217;s support for Tiles v3 in the
<code>org.springframework.web.servlet.view.tiles3</code> package.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect4">
<h5 id="mvc-view-tiles-dependencies"><a class="anchor" href="#mvc-view-tiles-dependencies"></a>Dependencies</h5>
<div class="paragraph">
<p>To be able to use Tiles, you have to add a dependency on Tiles version 3.0.1 or higher
and <a href="https://tiles.apache.org/framework/dependency-management.html">its transitive dependencies</a>
to your project.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-tiles-integrate"><a class="anchor" href="#mvc-view-tiles-integrate"></a>Configuration</h5>
<div class="paragraph">
<p>To be able to use Tiles, you have to configure it using files containing definitions
(for basic information on definitions and other Tiles concepts, please have a look at
<a href="https://tiles.apache.org" class="bare">http://tiles.apache.org</a>). In Spring this is done using the <code>TilesConfigurer</code>. Have a
look at the following piece of example ApplicationContext configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">tilesConfigurer</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.tiles3.TilesConfigurer</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">definitions</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;list&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/general.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/widgets.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/administrator.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/customer.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/templates.xml<span class="tag">&lt;/value&gt;</span>
        <span class="tag">&lt;/list&gt;</span>
    <span class="tag">&lt;/property&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>As you can see, there are five files containing definitions, which are all located in
the <code>'WEB-INF/defs'</code> directory. At initialization of the <code>WebApplicationContext</code>, the
files will be loaded and the definitions factory will be initialized. After that has
been done, the Tiles includes in the definition files can be used as views within your
Spring web application. To be able to use the views you have to have a <code>ViewResolver</code>
just as with any other view technology used with Spring. Below you can find two
possibilities, the <code>UrlBasedViewResolver</code> and the <code>ResourceBundleViewResolver</code>.</p>
</div>
<div class="paragraph">
<p>You can specify locale specific Tiles definitions by adding an underscore and then
the locale. For example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">tilesConfigurer</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.tiles3.TilesConfigurer</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">definitions</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;list&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/tiles.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/tiles_fr_FR.xml<span class="tag">&lt;/value&gt;</span>
        <span class="tag">&lt;/list&gt;</span>
    <span class="tag">&lt;/property&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>With this configuration, <code>tiles_fr_FR.xml</code> will be used for requests with the <code>fr_FR</code> locale,
and <code>tiles.xml</code> will be used by default.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Since underscores are used to indicate locales, it is recommended to avoid using
them otherwise in the file names for Tiles definitions.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="sect5">
<h6 id="mvc-view-tiles-url"><a class="anchor" href="#mvc-view-tiles-url"></a>UrlBasedViewResolver</h6>
<div class="paragraph">
<p>The <code>UrlBasedViewResolver</code> instantiates the given <code>viewClass</code> for each view it has to
resolve.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewResolver</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.UrlBasedViewResolver</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewClass</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.tiles3.TilesView</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-tiles-resource"><a class="anchor" href="#mvc-view-tiles-resource"></a>ResourceBundleViewResolver</h6>
<div class="paragraph">
<p>The <code>ResourceBundleViewResolver</code> has to be provided with a property file containing
view names and view classes the resolver can use:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">viewResolver</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.ResourceBundleViewResolver</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">basename</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">views</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">...
welcomeView.(<span class="type">class</span>)=<span class="class">org</span>.springframework.web.servlet.view.tiles3.TilesView
welcomeView.url=welcome (<span class="local-variable">this</span> is the name of a Tiles definition)

vetsView.(<span class="type">class</span>)=<span class="class">org</span>.springframework.web.servlet.view.tiles3.TilesView
vetsView.url=vetsView (again, <span class="local-variable">this</span> is the name of a Tiles definition)

findOwnersForm.(<span class="type">class</span>)=<span class="class">org</span>.springframework.web.servlet.view.JstlView
findOwnersForm.url=/WEB-INF/jsp/findOwners.jsp
...</code></pre>
</div>
</div>
<div class="paragraph">
<p>As you can see, when using the <code>ResourceBundleViewResolver</code>, you can easily mix
different view technologies.</p>
</div>
<div class="paragraph">
<p>Note that the <code>TilesView</code> class supports JSTL (the JSP Standard Tag Library) out of the
box.</p>
</div>
</div>
<div class="sect5">
<h6 id="mvc-view-tiles-preparer"><a class="anchor" href="#mvc-view-tiles-preparer"></a>SimpleSpringPreparerFactory and SpringBeanPreparerFactory</h6>
<div class="paragraph">
<p>As an advanced feature, Spring also supports two special Tiles <code>PreparerFactory</code>
implementations. Check out the Tiles documentation for details on how to use
<code>ViewPreparer</code> references in your Tiles definition files.</p>
</div>
<div class="paragraph">
<p>Specify <code>SimpleSpringPreparerFactory</code> to autowire ViewPreparer instances based on
specified preparer classes, applying Spring&#8217;s container callbacks as well as applying
configured Spring BeanPostProcessors. If Spring&#8217;s context-wide annotation-config has
been activated, annotations in ViewPreparer classes will be automatically detected and
applied. Note that this expects preparer <em>classes</em> in the Tiles definition files, just
like the default <code>PreparerFactory</code> does.</p>
</div>
<div class="paragraph">
<p>Specify <code>SpringBeanPreparerFactory</code> to operate on specified preparer <em>names</em> instead
of classes, obtaining the corresponding Spring bean from the DispatcherServlet&#8217;s
application context. The full bean creation process will be in the control of the Spring
application context in this case, allowing for the use of explicit dependency injection
configuration, scoped beans etc. Note that you need to define one Spring bean definition
per preparer name (as used in your Tiles definitions).</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">tilesConfigurer</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.tiles3.TilesConfigurer</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">definitions</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;list&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/general.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/widgets.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/administrator.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/customer.xml<span class="tag">&lt;/value&gt;</span>
            <span class="tag">&lt;value&gt;</span>/WEB-INF/defs/templates.xml<span class="tag">&lt;/value&gt;</span>
        <span class="tag">&lt;/list&gt;</span>
    <span class="tag">&lt;/property&gt;</span>

    <span class="comment">&lt;!-- resolving preparer names as Spring bean definition names --&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">preparerFactoryClass</span><span class="delimiter">&quot;</span></span>
            <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.tiles3.SpringBeanPreparerFactory</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-feeds"><a class="anchor" href="#mvc-view-feeds"></a>1.10.7. RSS, Atom</h4>
<div class="paragraph">
<p>Both <code>AbstractAtomFeedView</code> and <code>AbstractRssFeedView</code> inherit from the base class
<code>AbstractFeedView</code> and are used to provide Atom and RSS Feed views respectfully. They
are based on java.net&#8217;s <a href="https://rome.dev.java.net">ROME</a> project and are located in the
package <code>org.springframework.web.servlet.view.feed</code>.</p>
</div>
<div class="paragraph">
<p><code>AbstractAtomFeedView</code> requires you to implement the <code>buildFeedEntries()</code> method and
optionally override the <code>buildFeedMetadata()</code> method (the default implementation is
empty), as shown below.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">SampleContentAtomView</span> <span class="directive">extends</span> AbstractAtomFeedView {

    <span class="annotation">@Override</span>
    <span class="directive">protected</span> <span class="type">void</span> buildFeedMetadata(<span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">Object</span>&gt; model,
            Feed feed, HttpServletRequest request) {
        <span class="comment">// implementation omitted</span>
    }

    <span class="annotation">@Override</span>
    <span class="directive">protected</span> <span class="predefined-type">List</span>&lt;Entry&gt; buildFeedEntries(<span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">Object</span>&gt; model,
            HttpServletRequest request, HttpServletResponse response) <span class="directive">throws</span> <span class="exception">Exception</span> {
        <span class="comment">// implementation omitted</span>
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Similar requirements apply for implementing <code>AbstractRssFeedView</code>, as shown below.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">SampleContentAtomView</span> <span class="directive">extends</span> AbstractRssFeedView {

    <span class="annotation">@Override</span>
    <span class="directive">protected</span> <span class="type">void</span> buildFeedMetadata(<span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">Object</span>&gt; model,
            <span class="predefined-type">Channel</span> feed, HttpServletRequest request) {
        <span class="comment">// implementation omitted</span>
    }

    <span class="annotation">@Override</span>
    <span class="directive">protected</span> <span class="predefined-type">List</span>&lt;Item&gt; buildFeedItems(<span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">Object</span>&gt; model,
            HttpServletRequest request, HttpServletResponse response) <span class="directive">throws</span> <span class="exception">Exception</span> {
        <span class="comment">// implementation omitted</span>
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The <code>buildFeedItems()</code> and <code>buildFeedEntires()</code> methods pass in the HTTP request in case
you need to access the Locale. The HTTP response is passed in only for the setting of
cookies or other HTTP headers. The feed will automatically be written to the response
object after the method returns.</p>
</div>
<div class="paragraph">
<p>For an example of creating an Atom view please refer to Alef Arendsen&#8217;s Spring Team Blog
<a href="https://spring.io/blog/2009/03/16/adding-an-atom-view-to-an-application-using-spring-s-rest-support">entry</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-document"><a class="anchor" href="#mvc-view-document"></a>1.10.8. PDF, Excel</h4>
<div class="sect4">
<h5 id="mvc-view-document-intro"><a class="anchor" href="#mvc-view-document-intro"></a>Introduction</h5>
<div class="paragraph">
<p>Returning an HTML page isn&#8217;t always the best way for the user to view the model output,
and Spring makes it simple to generate a PDF document or an Excel spreadsheet
dynamically from the model data. The document is the view and will be streamed from the
server with the correct content type to (hopefully) enable the client PC to run their
spreadsheet or PDF viewer application in response.</p>
</div>
<div class="paragraph">
<p>In order to use Excel views, you need to add the Apache POI library to your classpath,
and for PDF generation preferably the OpenPDF library.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Use the latest versions of the underlying document generation libraries if possible.
In particular, we strongly recommend OpenPDF (e.g. OpenPDF 1.0.5) instead of the
outdated original iText 2.1.7 since it is actively maintained and fixes an important
vulnerability for untrusted PDF content.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-document-config"><a class="anchor" href="#mvc-view-document-config"></a>Configuration</h5>
<div class="paragraph">
<p>Document based views are handled in an almost identical fashion to XSLT views, and the
following sections build upon the previous one by demonstrating how the same controller
used in the XSLT example is invoked to render the same model as both a PDF document and
an Excel spreadsheet (which can also be viewed or manipulated in Open Office).</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-document-configviews"><a class="anchor" href="#mvc-view-document-configviews"></a>View definition</h5>
<div class="paragraph">
<p>First, let&#8217;s amend the views.properties file (or xml equivalent) and add a simple view
definition for both document types. The entire file now looks like this with the XSLT
view shown from earlier:</p>
</div>
<div class="literalblock">
<div class="content">
<pre>home.(class)=xslt.HomePage
home.stylesheetLocation=/WEB-INF/xsl/home.xslt
home.root=words

xl.(class)=excel.HomePage

pdf.(class)=pdf.HomePage</pre>
</div>
</div>
<div class="paragraph">
<p><em>If you want to start with a template spreadsheet or a fillable PDF form to add your
model data to, specify the location as the 'url' property in the view definition</em></p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-document-configcontroller"><a class="anchor" href="#mvc-view-document-configcontroller"></a>Controller</h5>
<div class="paragraph">
<p>The controller code we&#8217;ll use remains exactly the same from the XSLT example earlier
other than to change the name of the view to use. Of course, you could be clever and
have this selected based on a URL parameter or some other logic - proof that Spring
really is very good at decoupling the views from the controllers!</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-document-configsubclasses"><a class="anchor" href="#mvc-view-document-configsubclasses"></a>Excel views</h5>
<div class="paragraph">
<p>Exactly as we did for the XSLT example, we&#8217;ll subclass suitable abstract classes in
order to implement custom behavior in generating our output documents. For Excel, this
involves writing a subclass of
<code>org.springframework.web.servlet.view.document.AbstractExcelView</code> (for Excel files
generated by POI) or <code>org.springframework.web.servlet.view.document.AbstractJExcelView</code>
(for JExcelApi-generated Excel files) and implementing the <code>buildExcelDocument()</code> method.</p>
</div>
<div class="paragraph">
<p>Here&#8217;s the complete listing for our POI Excel view which displays the word list from the
model map in consecutive rows of the first column of a new spreadsheet:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">package</span> <span class="namespace">excel</span>;

<span class="comment">// imports omitted for brevity</span>

<span class="directive">public</span> <span class="type">class</span> <span class="class">HomePage</span> <span class="directive">extends</span> AbstractExcelView {

    <span class="directive">protected</span> <span class="type">void</span> buildExcelDocument(<span class="predefined-type">Map</span> model, HSSFWorkbook wb, HttpServletRequest req,
            HttpServletResponse resp) <span class="directive">throws</span> <span class="exception">Exception</span> {

        HSSFSheet sheet;
        HSSFRow sheetRow;
        HSSFCell cell;

        <span class="comment">// Go to the first sheet</span>
        <span class="comment">// getSheetAt: only if wb is created from an existing document</span>
        <span class="comment">// sheet = wb.getSheetAt(0);</span>
        sheet = wb.createSheet(<span class="string"><span class="delimiter">&quot;</span><span class="content">Spring</span><span class="delimiter">&quot;</span></span>);
        sheet.setDefaultColumnWidth((<span class="type">short</span>) <span class="integer">12</span>);

        <span class="comment">// write a text at A1</span>
        cell = getCell(sheet, <span class="integer">0</span>, <span class="integer">0</span>);
        setText(cell, <span class="string"><span class="delimiter">&quot;</span><span class="content">Spring-Excel test</span><span class="delimiter">&quot;</span></span>);

        <span class="predefined-type">List</span> words = (<span class="predefined-type">List</span>) model.get(<span class="string"><span class="delimiter">&quot;</span><span class="content">wordList</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">for</span> (<span class="type">int</span> i=<span class="integer">0</span>; i &lt; words.size(); i++) {
            cell = getCell(sheet, <span class="integer">2</span>+i, <span class="integer">0</span>);
            setText(cell, (<span class="predefined-type">String</span>) words.get(i));
        }
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And the following is a view generating the same Excel file, now using JExcelApi:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">package</span> <span class="namespace">excel</span>;

<span class="comment">// imports omitted for brevity</span>

<span class="directive">public</span> <span class="type">class</span> <span class="class">HomePage</span> <span class="directive">extends</span> AbstractJExcelView {

    <span class="directive">protected</span> <span class="type">void</span> buildExcelDocument(<span class="predefined-type">Map</span> model, WritableWorkbook wb,
            HttpServletRequest request, HttpServletResponse response) <span class="directive">throws</span> <span class="exception">Exception</span> {

        WritableSheet sheet = wb.createSheet(<span class="string"><span class="delimiter">&quot;</span><span class="content">Spring</span><span class="delimiter">&quot;</span></span>, <span class="integer">0</span>);

        sheet.addCell(<span class="keyword">new</span> <span class="predefined-type">Label</span>(<span class="integer">0</span>, <span class="integer">0</span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Spring-Excel test</span><span class="delimiter">&quot;</span></span>));

        <span class="predefined-type">List</span> words = (<span class="predefined-type">List</span>) model.get(<span class="string"><span class="delimiter">&quot;</span><span class="content">wordList</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">for</span> (<span class="type">int</span> i = <span class="integer">0</span>; i &lt; words.size(); i++) {
            sheet.addCell(<span class="keyword">new</span> <span class="predefined-type">Label</span>(<span class="integer">2</span>+i, <span class="integer">0</span>, (<span class="predefined-type">String</span>) words.get(i)));
        }
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Note the differences between the APIs. We&#8217;ve found that the JExcelApi is somewhat more
intuitive, and furthermore, JExcelApi has slightly better image-handling capabilities.
There have been memory problems with large Excel files when using JExcelApi however.</p>
</div>
<div class="paragraph">
<p>If you now amend the controller such that it returns <code>xl</code> as the name of the view (
<code>return new ModelAndView("xl", map);</code>) and run your application again, you should find
that the Excel spreadsheet is created and downloaded automatically when you request the
same page as before.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-document-configsubclasspdf"><a class="anchor" href="#mvc-view-document-configsubclasspdf"></a>PDF views</h5>
<div class="paragraph">
<p>The PDF version of the word list is even simpler. This time, the class extends
<code>org.springframework.web.servlet.view.document.AbstractPdfView</code> and implements the
<code>buildPdfDocument()</code> method as follows:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">package</span> <span class="namespace">pdf</span>;

<span class="comment">// imports omitted for brevity</span>

<span class="directive">public</span> <span class="type">class</span> <span class="class">PDFPage</span> <span class="directive">extends</span> AbstractPdfView {

    <span class="directive">protected</span> <span class="type">void</span> buildPdfDocument(<span class="predefined-type">Map</span> model, <span class="predefined-type">Document</span> doc, PdfWriter writer,
        HttpServletRequest req, HttpServletResponse resp) <span class="directive">throws</span> <span class="exception">Exception</span> {
        <span class="predefined-type">List</span> words = (<span class="predefined-type">List</span>) model.get(<span class="string"><span class="delimiter">&quot;</span><span class="content">wordList</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">for</span> (<span class="type">int</span> i=<span class="integer">0</span>; i&lt;words.size(); i++) {
            doc.add( <span class="keyword">new</span> Paragraph((<span class="predefined-type">String</span>) words.get(i)));
        }
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Once again, amend the controller to return the <code>pdf</code> view with <code>return new
ModelAndView("pdf", map);</code>, and reload the URL in your application. This time a PDF
document should appear listing each of the words in the model map.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-jackson"><a class="anchor" href="#mvc-view-jackson"></a>1.10.9. Jackson</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-httpmessagewriter">Same in Spring WebFlux</a></span></p>
</div>
<div class="sect4">
<h5 id="mvc-view-json-mapping"><a class="anchor" href="#mvc-view-json-mapping"></a>JSON</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-httpmessagewriter">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>MappingJackson2JsonView</code> uses the Jackson library&#8217;s <code>ObjectMapper</code> to render the response
content as JSON. By default, the entire contents of the model map (with the exception of
framework-specific classes) will be encoded as JSON. For cases where the contents of the
map need to be filtered, users may specify a specific set of model attributes to encode
via the <code>RenderedAttributes</code> property. The <code>extractValueFromSingleKeyModel</code> property may
also be used to have the value in single-key models extracted and serialized directly
rather than as a map of model attributes.</p>
</div>
<div class="paragraph">
<p>JSON mapping can be customized as needed through the use of Jackson&#8217;s provided
annotations. When further control is needed, a custom <code>ObjectMapper</code> can be injected
through the <code>ObjectMapper</code> property for cases where custom JSON
serializers/deserializers need to be provided for specific types.</p>
</div>
<div class="paragraph">
<p>As of Spring Framework 5.0.7, <a href="https://en.wikipedia.org/wiki/JSONP">JSONP</a> support is
deprecated and requires to customize the JSONP query parameter
name(s) through the <code>jsonpParameterNames</code> property. This support will be removed as of
Spring Framework 5.1, <a href="#mvc-cors">CORS</a> should be used instead.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-xml-mapping"><a class="anchor" href="#mvc-view-xml-mapping"></a>XML</h5>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-view-httpmessagewriter">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The <code>MappingJackson2XmlView</code> uses the
<a href="https://github.com/FasterXML/jackson-dataformat-xml">Jackson XML extension</a>'s <code>XmlMapper</code>
to render the response content as XML. If the model contains multiples entries, the
object to be serialized should be set explicitly using the <code>modelKey</code> bean property.
If the model contains a single entry, it will be serialized automatically.</p>
</div>
<div class="paragraph">
<p>XML mapping can be customized as needed through the use of JAXB or Jackson&#8217;s provided
annotations. When further control is needed, a custom <code>XmlMapper</code> can be injected
through the <code>ObjectMapper</code> property for cases where custom XML
serializers/deserializers need to be provided for specific types.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-xml-marshalling"><a class="anchor" href="#mvc-view-xml-marshalling"></a>1.10.10. XML</h4>
<div class="paragraph">
<p>The <code>MarshallingView</code> uses an XML <code>Marshaller</code> defined in the <code>org.springframework.oxm</code>
package to render the response content as XML. The object to be marshalled can be set
explicitly using <code>MarhsallingView</code>'s <code>modelKey</code> bean property. Alternatively, the view
will iterate over all model properties and marshal the first type that is supported
by the <code>Marshaller</code>. For more information on the functionality in the
<code>org.springframework.oxm</code> package refer to the chapter <a href="#oxm">Marshalling XML using O/X
Mappers</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-view-xslt"><a class="anchor" href="#mvc-view-xslt"></a>1.10.11. XSLT</h4>
<div class="paragraph">
<p>XSLT is a transformation language for XML and is popular as a view technology within web
applications. XSLT can be a good choice as a view technology if your application
naturally deals with XML, or if your model can easily be converted to XML. The following
section shows how to produce an XML document as model data and have it transformed with
XSLT in a Spring Web MVC application.</p>
</div>
<div class="paragraph">
<p>This example is a trivial Spring application that creates a list of words in the
<code>Controller</code> and adds them to the model map. The map is returned along with the view
name of our XSLT view. See <a href="#mvc-controller">Annotated Controllers</a> for details of Spring Web MVC&#8217;s
<code>Controller</code> interface. The XSLT Controller will turn the list of words into a simple XML
document ready for transformation.</p>
</div>
<div class="sect4">
<h5 id="mvc-view-xslt-beandefs"><a class="anchor" href="#mvc-view-xslt-beandefs"></a>Beans</h5>
<div class="paragraph">
<p>Configuration is standard for a simple Spring application.
The MVC configuration has to define a <code>XsltViewResolver</code> bean and
regular MVC annotation configuration.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@EnableWebMvc</span>
<span class="annotation">@ComponentScan</span>
<span class="annotation">@Configuration</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> XsltViewResolver xsltViewResolver() {
        XsltViewResolver viewResolver = <span class="keyword">new</span> XsltViewResolver();
        viewResolver.setPrefix(<span class="string"><span class="delimiter">&quot;</span><span class="content">/WEB-INF/xsl/</span><span class="delimiter">&quot;</span></span>);
        viewResolver.setSuffix(<span class="string"><span class="delimiter">&quot;</span><span class="content">.xslt</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">return</span> viewResolver;
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And we need a Controller that encapsulates our word generation logic.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-xslt-controllercode"><a class="anchor" href="#mvc-view-xslt-controllercode"></a>Controller</h5>
<div class="paragraph">
<p>The controller logic is encapsulated in a <code>@Controller</code> class, with the
handler method being defined like so&#8230;&#8203;</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">XsltController</span> {

    <span class="annotation">@RequestMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="predefined-type">String</span> home(Model model) <span class="directive">throws</span> <span class="exception">Exception</span> {

        <span class="predefined-type">Document</span> document = <span class="predefined-type">DocumentBuilderFactory</span>.newInstance().newDocumentBuilder().newDocument();
        <span class="predefined-type">Element</span> root = document.createElement(<span class="string"><span class="delimiter">&quot;</span><span class="content">wordList</span><span class="delimiter">&quot;</span></span>);

        <span class="predefined-type">List</span>&lt;<span class="predefined-type">String</span>&gt; words = <span class="predefined-type">Arrays</span>.asList(<span class="string"><span class="delimiter">&quot;</span><span class="content">Hello</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Spring</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">Framework</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">for</span> (<span class="predefined-type">String</span> word : words) {
            <span class="predefined-type">Element</span> wordNode = document.createElement(<span class="string"><span class="delimiter">&quot;</span><span class="content">word</span><span class="delimiter">&quot;</span></span>);
            Text textNode = document.createTextNode(word);
            wordNode.appendChild(textNode);
            root.appendChild(wordNode);
        }

        model.addAttribute(<span class="string"><span class="delimiter">&quot;</span><span class="content">wordList</span><span class="delimiter">&quot;</span></span>, root);
        <span class="keyword">return</span> <span class="string"><span class="delimiter">&quot;</span><span class="content">home</span><span class="delimiter">&quot;</span></span>;
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>So far we&#8217;ve only created a DOM document and added it to the Model map. Note that you
can also load an XML file as a <code>Resource</code> and use it instead of a custom DOM document.</p>
</div>
<div class="paragraph">
<p>Of course, there are software packages available that will automatically 'domify'
an object graph, but within Spring, you have complete flexibility to create the DOM
from your model in any way you choose. This prevents the transformation of XML playing
too great a part in the structure of your model data which is a danger when using tools
to manage the domification process.</p>
</div>
<div class="paragraph">
<p>Next, <code>XsltViewResolver</code> will resolve the "home" XSLT template file and merge the
DOM document into it to generate our view.</p>
</div>
</div>
<div class="sect4">
<h5 id="mvc-view-xslt-transforming"><a class="anchor" href="#mvc-view-xslt-transforming"></a>Transformation</h5>
<div class="paragraph">
<p>Finally, the <code>XsltViewResolver</code> will resolve the "home" XSLT template file and merge the
DOM document into it to generate our view. As shown in the <code>XsltViewResolver</code>
configuration, XSLT templates live in the war file in the <code>'WEB-INF/xsl'</code> directory
and end with a <code>"xslt"</code> file extension.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="preprocessor">&lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;</span>
<span class="tag">&lt;xsl:stylesheet</span> <span class="attribute-name">version</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">1.0</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">xmlns:xsl</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/1999/XSL/Transform</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;xsl:output</span> <span class="attribute-name">method</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">html</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">omit-xml-declaration</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">yes</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

    <span class="tag">&lt;xsl:template</span> <span class="attribute-name">match</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;html&gt;</span>
            <span class="tag">&lt;head&gt;</span><span class="tag">&lt;title&gt;</span>Hello!<span class="tag">&lt;/title&gt;</span><span class="tag">&lt;/head&gt;</span>
            <span class="tag">&lt;body&gt;</span>
                <span class="tag">&lt;h1&gt;</span>My First Words<span class="tag">&lt;/h1&gt;</span>
                <span class="tag">&lt;ul&gt;</span>
                    <span class="tag">&lt;xsl:apply-templates</span><span class="tag">/&gt;</span>
                <span class="tag">&lt;/ul&gt;</span>
            <span class="tag">&lt;/body&gt;</span>
        <span class="tag">&lt;/html&gt;</span>
    <span class="tag">&lt;/xsl:template&gt;</span>

    <span class="tag">&lt;xsl:template</span> <span class="attribute-name">match</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">word</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;li&gt;</span><span class="tag">&lt;xsl:value-of</span> <span class="attribute-name">select</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">.</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span><span class="tag">&lt;/li&gt;</span>
    <span class="tag">&lt;/xsl:template&gt;</span>

<span class="tag">&lt;/xsl:stylesheet&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>This is rendered as:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="html"><span class="tag">&lt;html&gt;</span>
    <span class="tag">&lt;head&gt;</span>
        <span class="tag">&lt;META</span> <span class="attribute-name">http-equiv</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">Content-Type</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">content</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">text/html; charset=UTF-8</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;title&gt;</span>Hello!<span class="tag">&lt;/title&gt;</span>
    <span class="tag">&lt;/head&gt;</span>
    <span class="tag">&lt;body&gt;</span>
        <span class="tag">&lt;h1&gt;</span>My First Words<span class="tag">&lt;/h1&gt;</span>
        <span class="tag">&lt;ul&gt;</span>
            <span class="tag">&lt;li&gt;</span>Hello<span class="tag">&lt;/li&gt;</span>
            <span class="tag">&lt;li&gt;</span>Spring<span class="tag">&lt;/li&gt;</span>
            <span class="tag">&lt;li&gt;</span>Framework<span class="tag">&lt;/li&gt;</span>
        <span class="tag">&lt;/ul&gt;</span>
    <span class="tag">&lt;/body&gt;</span>
<span class="tag">&lt;/html&gt;</span></code></pre>
</div>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-config"><a class="anchor" href="#mvc-config"></a>1.11. MVC Config</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The MVC Java config and the MVC XML namespace provide default configuration suitable for most
applications along with a configuration API to customize it.</p>
</div>
<div class="paragraph">
<p>For more advanced customizations, not available in the configuration API, see
<a href="#mvc-config-advanced-java">Advanced Java Config</a> and <a href="#mvc-config-advanced-xml">Advanced XML Config</a>.</p>
</div>
<div class="paragraph">
<p>You do not need to understand the underlying beans created by the MVC Java config and
the MVC namespace but if you want to learn more, see <a href="#mvc-servlet-special-bean-types">Special Bean Types</a>
and <a href="#mvc-servlet-config">Web MVC Config</a>.</p>
</div>
<div class="sect3">
<h4 id="mvc-config-enable"><a class="anchor" href="#mvc-config-enable"></a>1.11.1. Enable MVC Config</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-enable">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>In Java config use the <code>@EnableWebMvc</code> annotation:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> {
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML use the <code>&lt;mvc:annotation-driven&gt;</code> element:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="preprocessor">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;</span>
<span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:mvc</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/mvc</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/mvc</span>
        <span class="content">http://www.springframework.org/schema/mvc/spring-mvc.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;mvc:annotation-driven</span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The above registers a number of Spring MVC
<a href="#mvc-servlet-special-bean-types">infrastructure beans</a> also adapting to dependencies
available on the classpath: e.g. payload converters for JSON, XML, etc.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-customize"><a class="anchor" href="#mvc-config-customize"></a>1.11.2. MVC Config API</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-customize">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>In Java config implement <code>WebMvcConfigurer</code> interface:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="comment">// Implement configuration methods...</span>
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML check attributes and sub-elements of <code>&lt;mvc:annotation-driven/&gt;</code>. You can
view the <a href="https://schema.spring.io/mvc/spring-mvc.xsd">Spring MVC XML schema</a> or use
the code completion feature of your IDE to discover what attributes and
sub-elements are available.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-conversion"><a class="anchor" href="#mvc-config-conversion"></a>1.11.3. Type conversion</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-conversion">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>By default formatters for <code>Number</code> and <code>Date</code> types are installed, including support for
the <code>@NumberFormat</code> and <code>@DateTimeFormat</code> annotations. Full support for the Joda-Time
formatting library is also installed if Joda-Time is present on the classpath.</p>
</div>
<div class="paragraph">
<p>In Java config, register custom formatters and converters:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> addFormatters(FormatterRegistry registry) {
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="preprocessor">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;</span>
<span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:mvc</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/mvc</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/mvc</span>
        <span class="content">http://www.springframework.org/schema/mvc/spring-mvc.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;mvc:annotation-driven</span> <span class="attribute-name">conversion-service</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">conversionService</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">conversionService</span><span class="delimiter">&quot;</span></span>
            <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.format.support.FormattingConversionServiceFactoryBean</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">converters</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;set&gt;</span>
                <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.MyConverter</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/set&gt;</span>
        <span class="tag">&lt;/property&gt;</span>
        <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">formatters</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;set&gt;</span>
                <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.MyFormatter</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
                <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.MyAnnotationFormatterFactory</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/set&gt;</span>
        <span class="tag">&lt;/property&gt;</span>
        <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">formatterRegistrars</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;set&gt;</span>
                <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.MyFormatterRegistrar</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/set&gt;</span>
        <span class="tag">&lt;/property&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>See <a href="core.html#format-FormatterRegistrar-SPI">FormatterRegistrar SPI</a>
and the <code>FormattingConversionServiceFactoryBean</code> for more information on when to use FormatterRegistrars.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-validation"><a class="anchor" href="#mvc-config-validation"></a>1.11.4. Validation</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-validation">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>By default if <a href="core.html#validation-beanvalidation-overview">Bean Validation</a> is present
on the classpath&#8201;&#8212;&#8201;e.g. Hibernate Validator, the <code>LocalValidatorFactoryBean</code> is registered
as a global <a href="core.html#validator">Validator</a> for use with <code>@Valid</code> and <code>Validated</code> on
controller method arguments.</p>
</div>
<div class="paragraph">
<p>In Java config, you can customize the global <code>Validator</code> instance:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="predefined-type">Validator</span> getValidator(); {
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="preprocessor">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;</span>
<span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:mvc</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/mvc</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/mvc</span>
        <span class="content">http://www.springframework.org/schema/mvc/spring-mvc.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;mvc:annotation-driven</span> <span class="attribute-name">validator</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">globalValidator</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>Note that you can also register <code>Validator</code>'s locally:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyController</span> {

    <span class="annotation">@InitBinder</span>
    <span class="directive">protected</span> <span class="type">void</span> initBinder(WebDataBinder binder) {
        binder.addValidators(<span class="keyword">new</span> FooValidator());
    }

}</code></pre>
</div>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>If you need to have a <code>LocalValidatorFactoryBean</code> injected somewhere, create a bean and
mark it with <code>@Primary</code> in order to avoid conflict with the one declared in the MVC config.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-interceptors"><a class="anchor" href="#mvc-config-interceptors"></a>1.11.5. Interceptors</h4>
<div class="paragraph">
<p>In Java config, register interceptors to apply to incoming requests:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(<span class="keyword">new</span> LocaleChangeInterceptor());
        registry.addInterceptor(<span class="keyword">new</span> ThemeChangeInterceptor()).addPathPatterns(<span class="string"><span class="delimiter">&quot;</span><span class="content">/**</span><span class="delimiter">&quot;</span></span>).excludePathPatterns(<span class="string"><span class="delimiter">&quot;</span><span class="content">/admin/**</span><span class="delimiter">&quot;</span></span>);
        registry.addInterceptor(<span class="keyword">new</span> SecurityInterceptor()).addPathPatterns(<span class="string"><span class="delimiter">&quot;</span><span class="content">/secure/*</span><span class="delimiter">&quot;</span></span>);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:interceptors&gt;</span>
    <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.i18n.LocaleChangeInterceptor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;mvc:interceptor&gt;</span>
        <span class="tag">&lt;mvc:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/**</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;mvc:exclude-mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/admin/**</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.theme.ThemeChangeInterceptor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/mvc:interceptor&gt;</span>
    <span class="tag">&lt;mvc:interceptor&gt;</span>
        <span class="tag">&lt;mvc:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/secure/*</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.SecurityInterceptor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/mvc:interceptor&gt;</span>
<span class="tag">&lt;/mvc:interceptors&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-content-negotiation"><a class="anchor" href="#mvc-config-content-negotiation"></a>1.11.6. Content Types</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-content-negotiation">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>You can configure how Spring MVC determines the requested media types from the request&#8201;&#8212;&#8201;e.g. <code>Accept</code> header, URL path extension, query parameter, etc.</p>
</div>
<div class="paragraph">
<p>By default the URL path extension is checked first&#8201;&#8212;&#8201;with <code>json</code>, <code>xml</code>, <code>rss</code>, and <code>atom</code>
registered as known extensions depending on classpath dependencies, and the "Accept" header
is checked second.</p>
</div>
<div class="paragraph">
<p>Consider changing those defaults to <code>Accept</code> header only and if you must use URL-based
content type resolution consider the query parameter strategy over the path extensions. See
<a href="#mvc-ann-requestmapping-suffix-pattern-match">Suffix match</a> and <a href="#mvc-ann-requestmapping-rfd">Suffix match and RFD</a> for
more details.</p>
</div>
<div class="paragraph">
<p>In Java config, customize requested content type resolution:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType(<span class="string"><span class="delimiter">&quot;</span><span class="content">json</span><span class="delimiter">&quot;</span></span>, MediaType.APPLICATION_JSON);
        configurer.mediaType(<span class="string"><span class="delimiter">&quot;</span><span class="content">xml</span><span class="delimiter">&quot;</span></span>, MediaType.APPLICATION_XML);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:annotation-driven</span> <span class="attribute-name">content-negotiation-manager</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">contentNegotiationManager</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">contentNegotiationManager</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.accept.ContentNegotiationManagerFactoryBean</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">mediaTypes</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;value&gt;</span>
            json=application/json
            xml=application/xml
        <span class="tag">&lt;/value&gt;</span>
    <span class="tag">&lt;/property&gt;</span>
<span class="tag">&lt;/bean&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-message-converters"><a class="anchor" href="#mvc-config-message-converters"></a>1.11.7. Message Converters</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-message-codecs">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Customization of <code>HttpMessageConverter</code> can be achieved in Java config by overriding
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html#configureMessageConverters-java.util.List-"><code>configureMessageConverters()</code></a>
if you want to replace the default converters created by Spring MVC, or by overriding
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html#extendMessageConverters-java.util.List-"><code>extendMessageConverters()</code></a>
if you just want to customize them or add additional converters to the default ones.</p>
</div>
<div class="paragraph">
<p>Below is an example that adds Jackson JSON and XML converters with a customized
<code>ObjectMapper</code> instead of default ones:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfiguration</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageConverters(<span class="predefined-type">List</span>&lt;HttpMessageConverter&lt;?&gt;&gt; converters) {
        Jackson2ObjectMapperBuilder builder = <span class="keyword">new</span> Jackson2ObjectMapperBuilder()
                .indentOutput(<span class="predefined-constant">true</span>)
                .dateFormat(<span class="keyword">new</span> <span class="predefined-type">SimpleDateFormat</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">yyyy-MM-dd</span><span class="delimiter">&quot;</span></span>))
                .modulesToInstall(<span class="keyword">new</span> ParameterNamesModule());
        converters.add(<span class="keyword">new</span> MappingJackson2HttpMessageConverter(builder.build()));
        converters.add(<span class="keyword">new</span> MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(<span class="predefined-constant">true</span>).build()));
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In this example,
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/http/converter/json/Jackson2ObjectMapperBuilder.html">Jackson2ObjectMapperBuilder</a>
is used to create a common configuration for both <code>MappingJackson2HttpMessageConverter</code> and
<code>MappingJackson2XmlHttpMessageConverter</code> with indentation enabled, a customized date format
and the registration of
<a href="https://github.com/FasterXML/jackson-module-parameter-names">jackson-module-parameter-names</a>
that adds support for accessing parameter names (feature added in Java 8).</p>
</div>
<div class="paragraph">
<p>This builder customizes Jackson&#8217;s default properties with the following ones:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p><a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/DeserializationFeature.html#FAIL_ON_UNKNOWN_PROPERTIES"><code>DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES</code></a> is disabled.</p>
</li>
<li>
<p><a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/MapperFeature.html#DEFAULT_VIEW_INCLUSION"><code>MapperFeature.DEFAULT_VIEW_INCLUSION</code></a> is disabled.</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>It also automatically registers the following well-known modules if they are detected on the classpath:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p><a href="https://github.com/FasterXML/jackson-datatype-jdk7">jackson-datatype-jdk7</a>: support for Java 7 types like <code>java.nio.file.Path</code>.</p>
</li>
<li>
<p><a href="https://github.com/FasterXML/jackson-datatype-joda">jackson-datatype-joda</a>: support for Joda-Time types.</p>
</li>
<li>
<p><a href="https://github.com/FasterXML/jackson-datatype-jsr310">jackson-datatype-jsr310</a>: support for Java 8 Date &amp; Time API types.</p>
</li>
<li>
<p><a href="https://github.com/FasterXML/jackson-datatype-jdk8">jackson-datatype-jdk8</a>: support for other Java 8 types like <code>Optional</code>.</p>
</li>
</ol>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Enabling indentation with Jackson XML support requires
<a href="https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.codehaus.woodstox%22%20AND%20a%3A%22woodstox-core-asl%22"><code>woodstox-core-asl</code></a>
dependency in addition to <a href="https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jackson-dataformat-xml%22"><code>jackson-dataformat-xml</code></a> one.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>Other interesting Jackson modules are available:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p><a href="https://github.com/zalando/jackson-datatype-money">jackson-datatype-money</a>: support for <code>javax.money</code> types (unofficial module)</p>
</li>
<li>
<p><a href="https://github.com/FasterXML/jackson-datatype-hibernate">jackson-datatype-hibernate</a>: support for Hibernate specific types and properties (including lazy-loading aspects)</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>It is also possible to do the same in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:annotation-driven&gt;</span>
    <span class="tag">&lt;mvc:message-converters&gt;</span>
        <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.http.converter.json.MappingJackson2HttpMessageConverter</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">objectMapper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">ref</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">objectMapper</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/bean&gt;</span>
        <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">objectMapper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">ref</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">xmlMapper</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/bean&gt;</span>
    <span class="tag">&lt;/mvc:message-converters&gt;</span>
<span class="tag">&lt;/mvc:annotation-driven&gt;</span>

<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">objectMapper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean</span><span class="delimiter">&quot;</span></span>
      <span class="attribute-name">p:indentOutput</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span>
      <span class="attribute-name">p:simpleDateFormat</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">yyyy-MM-dd</span><span class="delimiter">&quot;</span></span>
      <span class="attribute-name">p:modulesToInstall</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">com.fasterxml.jackson.module.paramnames.ParameterNamesModule</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">xmlMapper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">parent</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">objectMapper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">p:createXmlMapper</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-view-controller"><a class="anchor" href="#mvc-config-view-controller"></a>1.11.8. View Controllers</h4>
<div class="paragraph">
<p>This is a shortcut for defining a <code>ParameterizableViewController</code> that immediately
forwards to a view when invoked. Use it in static cases when there is no Java controller
logic to execute before the view generates the response.</p>
</div>
<div class="paragraph">
<p>An example of forwarding a request for <code>"/"</code> to a view called <code>"home"</code> in Java:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(<span class="string"><span class="delimiter">&quot;</span><span class="content">/</span><span class="delimiter">&quot;</span></span>).setViewName(<span class="string"><span class="delimiter">&quot;</span><span class="content">home</span><span class="delimiter">&quot;</span></span>);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And the same in XML use the <code>&lt;mvc:view-controller&gt;</code> element:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:view-controller</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">view-name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">home</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-view-resolvers"><a class="anchor" href="#mvc-config-view-resolvers"></a>1.11.9. View Resolvers</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-view-resolvers">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The MVC config simplifies the registration of view resolvers.</p>
</div>
<div class="paragraph">
<p>The following is a Java config example that configures content negotiation view
resolution using JSP and Jackson as a default <code>View</code> for JSON rendering:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(<span class="keyword">new</span> MappingJackson2JsonView());
        registry.jsp();
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And the same in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:view-resolvers&gt;</span>
    <span class="tag">&lt;mvc:content-negotiation&gt;</span>
        <span class="tag">&lt;mvc:default-views&gt;</span>
            <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.json.MappingJackson2JsonView</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/mvc:default-views&gt;</span>
    <span class="tag">&lt;/mvc:content-negotiation&gt;</span>
    <span class="tag">&lt;mvc:jsp</span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:view-resolvers&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>Note however that FreeMarker, Tiles, Groovy Markup and script templates also require
configuration of the underlying view technology.</p>
</div>
<div class="paragraph">
<p>The MVC namespace provides dedicated elements. For example with FreeMarker:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:view-resolvers&gt;</span>
    <span class="tag">&lt;mvc:content-negotiation&gt;</span>
        <span class="tag">&lt;mvc:default-views&gt;</span>
            <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.servlet.view.json.MappingJackson2JsonView</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/mvc:default-views&gt;</span>
    <span class="tag">&lt;/mvc:content-negotiation&gt;</span>
    <span class="tag">&lt;mvc:freemarker</span> <span class="attribute-name">cache</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">false</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:view-resolvers&gt;</span>

<span class="tag">&lt;mvc:freemarker-configurer&gt;</span>
    <span class="tag">&lt;mvc:template-loader-path</span> <span class="attribute-name">location</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/freemarker</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:freemarker-configurer&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>In Java config simply add the respective "Configurer" bean:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(<span class="keyword">new</span> MappingJackson2JsonView());
        registry.freeMarker().cache(<span class="predefined-constant">false</span>);
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = <span class="keyword">new</span> FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath(<span class="string"><span class="delimiter">&quot;</span><span class="content">/freemarker</span><span class="delimiter">&quot;</span></span>);
        <span class="keyword">return</span> configurer;
    }
}</code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-static-resources"><a class="anchor" href="#mvc-config-static-resources"></a>1.11.10. Static Resources</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-static-resources">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>This option provides a convenient way to serve static resources from a list of
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/core/io/Resource.html">Resource</a>-based locations.</p>
</div>
<div class="paragraph">
<p>In the example below, given a request that starts with <code>"/resources"</code>, the relative path is
used to find and serve static resources relative to "/public" under the web application
root or on the classpath under <code>"/static"</code>. The resources are served with a 1-year future
expiration to ensure maximum use of the browser cache and a reduction in HTTP requests
made by the browser. The <code>Last-Modified</code> header is also evaluated and if present a <code>304</code>
status code is returned.</p>
</div>
<div class="paragraph">
<p>In Java config:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(<span class="string"><span class="delimiter">&quot;</span><span class="content">/resources/**</span><span class="delimiter">&quot;</span></span>)
            .addResourceLocations(<span class="string"><span class="delimiter">&quot;</span><span class="content">/public</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">classpath:/static/</span><span class="delimiter">&quot;</span></span>)
            .setCachePeriod(<span class="integer">31556926</span>);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:resources</span> <span class="attribute-name">mapping</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/resources/**</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">location</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/public, classpath:/static/</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">cache-period</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">31556926</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>See also
<a href="#mvc-caching-static-resources">HTTP caching support for static resources</a>.</p>
</div>
<div class="paragraph">
<p>The resource handler also supports a chain of
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/resource/ResourceResolver.html">ResourceResolver</a>s and
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/resource/ResourceTransformer.html">ResourceTransformer</a>s.
which can be used to create a toolchain for working with optimized resources.</p>
</div>
<div class="paragraph">
<p>The <code>VersionResourceResolver</code> can be used for versioned resource URLs based on an MD5 hash
computed from the content, a fixed application version, or other. A
<code>ContentVersionStrategy</code> (MD5 hash) is a good choice with some notable exceptions such as
JavaScript resources used with a module loader.</p>
</div>
<div class="paragraph">
<p>For example in Java config;</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(<span class="string"><span class="delimiter">&quot;</span><span class="content">/resources/**</span><span class="delimiter">&quot;</span></span>)
                .addResourceLocations(<span class="string"><span class="delimiter">&quot;</span><span class="content">/public/</span><span class="delimiter">&quot;</span></span>)
                .resourceChain(<span class="predefined-constant">true</span>)
                .addResolver(<span class="keyword">new</span> VersionResourceResolver().addContentVersionStrategy(<span class="string"><span class="delimiter">&quot;</span><span class="content">/**</span><span class="delimiter">&quot;</span></span>));
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:resources</span> <span class="attribute-name">mapping</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/resources/**</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">location</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/public/</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
    <span class="tag">&lt;mvc:resource-chain&gt;</span>
        <span class="tag">&lt;mvc:resource-cache</span><span class="tag">/&gt;</span>
        <span class="tag">&lt;mvc:resolvers&gt;</span>
            <span class="tag">&lt;mvc:version-resolver&gt;</span>
                <span class="tag">&lt;mvc:content-version-strategy</span> <span class="attribute-name">patterns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/**</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/mvc:version-resolver&gt;</span>
        <span class="tag">&lt;/mvc:resolvers&gt;</span>
    <span class="tag">&lt;/mvc:resource-chain&gt;</span>
<span class="tag">&lt;/mvc:resources&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>You can use <code>ResourceUrlProvider</code> to rewrite URLs and apply the full chain of resolvers and
transformers&#8201;&#8212;&#8201;e.g. to insert versions. The MVC config provides a <code>ResourceUrlProvider</code>
bean so it can be injected into others. You can also make the rewrite transparent with the
<code>ResourceUrlEncodingFilter</code> for Thymeleaf, JSPs, FreeMarker, and others with URL tags that
rely on <code>HttpServletResponse#encodeURL</code>.</p>
</div>
<div class="paragraph">
<p><a href="http://www.webjars.org/documentation">WebJars</a> is also supported via <code>WebJarsResourceResolver</code>
and automatically registered when <code>"org.webjars:webjars-locator"</code> is present on the
classpath. The resolver can re-write URLs to include the version of the jar and can also
match to incoming URLs without versions&#8201;&#8212;&#8201;e.g. <code>"/jquery/jquery.min.js"</code> to
<code>"/jquery/1.2.0/jquery.min.js"</code>.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-default-servlet-handler"><a class="anchor" href="#mvc-default-servlet-handler"></a>1.11.11. Default Servlet</h4>
<div class="paragraph">
<p>This allows for mapping the <code>DispatcherServlet</code> to "/" (thus overriding the mapping
of the container&#8217;s default Servlet), while still allowing static resource requests to be
handled by the container&#8217;s default Servlet. It configures a
<code>DefaultServletHttpRequestHandler</code> with a URL mapping of "/**" and the lowest priority
relative to other URL mappings.</p>
</div>
<div class="paragraph">
<p>This handler will forward all requests to the default Servlet. Therefore it is important
that it remains last in the order of all other URL <code>HandlerMappings</code>. That will be the
case if you use <code>&lt;mvc:annotation-driven&gt;</code> or alternatively if you are setting up your
own customized <code>HandlerMapping</code> instance be sure to set its <code>order</code> property to a value
lower than that of the <code>DefaultServletHttpRequestHandler</code>, which is <code>Integer.MAX_VALUE</code>.</p>
</div>
<div class="paragraph">
<p>To enable the feature using the default setup use:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Or in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:default-servlet-handler</span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The caveat to overriding the "/" Servlet mapping is that the <code>RequestDispatcher</code> for the
default Servlet must be retrieved by name rather than by path. The
<code>DefaultServletHttpRequestHandler</code> will attempt to auto-detect the default Servlet for
the container at startup time, using a list of known names for most of the major Servlet
containers (including Tomcat, Jetty, GlassFish, JBoss, Resin, WebLogic, and WebSphere).
If the default Servlet has been custom configured with a different name, or if a
different Servlet container is being used where the default Servlet name is unknown,
then the default Servlet&#8217;s name must be explicitly provided as in the following example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable(<span class="string"><span class="delimiter">&quot;</span><span class="content">myCustomDefaultServlet</span><span class="delimiter">&quot;</span></span>);
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Or in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:default-servlet-handler</span> <span class="attribute-name">default-servlet-name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myCustomDefaultServlet</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-path-matching"><a class="anchor" href="#mvc-config-path-matching"></a>1.11.12. Path Matching</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-path-matching">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>This allows customizing options related to URL matching and treatment of the URL.
For details on the individual options check out the
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/servlet/config/annotation/PathMatchConfigurer.html">PathMatchConfigurer</a> API.</p>
</div>
<div class="paragraph">
<p>Example in Java config:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebMvc</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">implements</span> WebMvcConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configurePathMatch(PathMatchConfigurer configurer) {
        configurer
            .setUseSuffixPatternMatch(<span class="predefined-constant">true</span>)
            .setUseTrailingSlashMatch(<span class="predefined-constant">false</span>)
            .setUseRegisteredSuffixPatternMatch(<span class="predefined-constant">true</span>)
            .setPathMatcher(antPathMatcher())
            .setUrlPathHelper(urlPathHelper());
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> UrlPathHelper urlPathHelper() {
        <span class="comment">//...</span>
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> PathMatcher antPathMatcher() {
        <span class="comment">//...</span>
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML, the same:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;mvc:annotation-driven&gt;</span>
    <span class="tag">&lt;mvc:path-matching</span>
        <span class="attribute-name">suffix-pattern</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">trailing-slash</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">false</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">registered-suffixes-only</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">path-helper</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">pathHelper</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">path-matcher</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">pathMatcher</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;/mvc:annotation-driven&gt;</span>

<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">pathHelper</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.app.MyPathHelper</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
<span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">pathMatcher</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.example.app.MyPathMatcher</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-advanced-java"><a class="anchor" href="#mvc-config-advanced-java"></a>1.11.13. Advanced Java Config</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-config-advanced-java">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p><code>@EnableWebMvc</code> imports <code>DelegatingWebMvcConfiguration</code> that (1) provides default Spring
configuration for Spring MVC applications and (2) detects and delegates to
<code>WebMvcConfigurer</code>'s to customize that configuration.</p>
</div>
<div class="paragraph">
<p>For advanced mode, remove <code>@EnableWebMvc</code> and extend directly from
<code>DelegatingWebMvcConfiguration</code> instead of implementing <code>WebMvcConfigurer</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebConfig</span> <span class="directive">extends</span> DelegatingWebMvcConfiguration {

    <span class="comment">// ...</span>

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>You can keep existing methods in <code>WebConfig</code> but you can now also override bean declarations
from the base class and you can still have any number of other <code>WebMvcConfigurer</code>'s on
the classpath.</p>
</div>
</div>
<div class="sect3">
<h4 id="mvc-config-advanced-xml"><a class="anchor" href="#mvc-config-advanced-xml"></a>1.11.14. Advanced XML Config</h4>
<div class="paragraph">
<p>The MVC namespace does not have an advanced mode. If you need to customize a property on
a bean that you can&#8217;t change otherwise, you can use the <code>BeanPostProcessor</code> lifecycle
hook of the Spring <code>ApplicationContext</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Component</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyPostProcessor</span> <span class="directive">implements</span> BeanPostProcessor {

    <span class="directive">public</span> <span class="predefined-type">Object</span> postProcessBeforeInitialization(<span class="predefined-type">Object</span> bean, <span class="predefined-type">String</span> name) <span class="directive">throws</span> BeansException {
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Note that <code>MyPostProcessor</code> needs to be declared as a bean either explicitly in XML or
detected through a <code>&lt;component-scan/&gt;</code> declaration.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="mvc-http2"><a class="anchor" href="#mvc-http2"></a>1.12. HTTP/2</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-http2">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Servlet 4 containers are required to support HTTP/2 and Spring Framework 5 is compatible
with Servlet API 4. From a programming model perspective there is nothing specific that
applications need to do. However there are considerations related to server configuration.
For more details please check out the
<a href="https://github.com/spring-projects/spring-framework/wiki/HTTP-2-support">HTTP/2 wiki page</a>.</p>
</div>
<div class="paragraph">
<p>The Servlet API does expose one construct related to HTTP/2. The
<code>javax.servlet.http.PushBuilder</code> can used to proactively push resources to clients and it
is supported as a <a href="#mvc-ann-arguments">method argument</a> to <code>@RequestMapping</code> methods.</p>
</div>
</div>
</div>
</div>
<div class="sect1">
<h2 id="webmvc-client"><a class="anchor" href="#webmvc-client"></a>2. REST Clients</h2>
<div class="sectionbody">
<div class="paragraph">
<p>This section describes options for client-side access to REST endpoints.</p>
</div>
<div class="sect2">
<h3 id="webmvc-resttemplate"><a class="anchor" href="#webmvc-resttemplate"></a>2.1. RestTemplate</h3>
<div class="paragraph">
<p><code>RestTemplate</code> is the original Spring REST client that follows a similar approach to other
template classes in the Spring Framework (e.g. <code>JdbcTemplate</code>, <code>JmsTemplate</code>, etc.) by
providing a list of parameterizable methods to perform HTTP requests.</p>
</div>
<div class="paragraph">
<p><code>RestTemplate</code> has a synchronous API and relies on blocking I/O. This is okay for
client scenarios with low concurrency. In a server environment or when orchestrating a
sequence of remote calls, prefer using the <code>WebClient</code> which provides a more efficient
execution model including seamless support for streaming.</p>
</div>
<div class="paragraph">
<p>See <a href="integration.html#rest-client-access">RestTemplate</a> for more details on using the
<code>RestTemplate</code>.</p>
</div>
</div>
<div class="sect2">
<h3 id="webmvc-webclient"><a class="anchor" href="#webmvc-webclient"></a>2.2. WebClient</h3>
<div class="paragraph">
<p><code>WebClient</code> is a reactive client that provides an alternative to the <code>RestTemplate</code>. It
exposes a functional, fluent API and relies on non-blocking I/O which allows it to support
high concurrency more efficiently (i.e. using a small number of threads) than the
<code>RestTemplate</code>. <code>WebClient</code> is a natural fit for streaming scenarios.</p>
</div>
<div class="paragraph">
<p>See <a href="web-reactive.html#webflux-client">WebClient</a> for more details on using the <code>WebClient</code>.</p>
</div>
</div>
</div>
</div>
<div class="sect1">
<h2 id="testing"><a class="anchor" href="#testing"></a>3. Testing</h2>
<div class="sectionbody">
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-test">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>This section summarizes the options available in <code>spring-test</code> for Spring MVC applications.</p>
</div>
<div class="paragraph">
<p><strong>Servlet API Mocks</strong></p>
</div>
<div class="paragraph">
<p>Mock implementations of Servlet API contracts for unit testing controllers, filters, and
other web components. See <a href="testing.html#mock-objects-servlet">Servlet API</a> mock objects
for more details.</p>
</div>
<div class="paragraph">
<p><strong>TestContext Framework</strong></p>
</div>
<div class="paragraph">
<p>Support for loading Spring configuration in JUnit and TestNG tests including efficient
caching of the loaded configuration across test methods and support for loading a
<code>WebApplicationContext</code> with a <code>MockServletContext</code>.
See <a href="testing.html#testcontext-framework">TestContext Framework</a> for more details.</p>
</div>
<div class="paragraph">
<p><strong>Spring MVC Test</strong></p>
</div>
<div class="paragraph">
<p>A framework, also known as <code>MockMvc</code>, for testing annotated controllers through the
<code>DispatcherServlet</code>, i.e. supporting annotations and complete with Spring MVC
infrastructure, but without an HTTP server. See
<a href="testing.html#spring-mvc-test-framework">Spring MVC Test</a> for more details.</p>
</div>
<div class="paragraph">
<p><strong>Client-side REST</strong></p>
</div>
<div class="paragraph">
<p><code>spring-test</code> provides a <code>MockRestServiceServer</code> that can be used as a mock server for
testing client-side code that internally uses the <code>RestTemplate</code>.
See <a href="testing.html#spring-mvc-test-client">Client REST Tests</a> for more details.</p>
</div>
<div class="paragraph">
<p><strong>WebTestClient</strong></p>
</div>
<div class="paragraph">
<p><code>WebTestClient</code> was built for testing WebFlux applications but it can also be used for
end-to-end integration testing, to any server, over an HTTP connection. It is a
non-blocking, reactive client and well suited for testing asynchronous and streaming
scenarios.</p>
</div>
</div>
</div>
<div class="sect1">
<h2 id="websocket"><a class="anchor" href="#websocket"></a>4. WebSockets</h2>
<div class="sectionbody">
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>This part of the reference documentation covers support for Servlet stack, WebSocket
messaging that includes raw WebSocket interactions, WebSocket emulation via SockJS, and
pub-sub messaging via STOMP as a sub-protocol over WebSocket.</p>
</div>
<div class="sect2">
<h3 id="websocket-intro"><a class="anchor" href="#websocket-intro"></a>4.1. Introduction</h3>
<div class="paragraph">
<p>The WebSocket protocol <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a> provides a standardized
way to establish a full-duplex, two-way communication channel between client and server
over a single TCP connection. It is a different TCP protocol from HTTP but is designed to
work over HTTP, using ports 80 and 443 and allowing re-use of existing firewall rules.</p>
</div>
<div class="paragraph">
<p>A WebSocket interaction begins with an HTTP request that uses the HTTP <code>"Upgrade"</code> header
to upgrade, or in this case to switch, to the WebSocket protocol:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>GET /spring-websocket-portfolio/portfolio HTTP/1.1
Host: localhost:8080
<strong>Upgrade: websocket</strong>
<strong>Connection: Upgrade</strong>
Sec-WebSocket-Key: Uc9l9TMkWGbHFD2qnFHltg==
Sec-WebSocket-Protocol: v10.stomp, v11.stomp
Sec-WebSocket-Version: 13
Origin: http://localhost:8080</pre>
</div>
</div>
<div class="paragraph">
<p>Instead of the usual 200 status code, a server with WebSocket support returns:</p>
</div>
<div class="listingblock">
<div class="content">
<pre><strong>HTTP/1.1 101 Switching Protocols</strong>
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: 1qVdfYHU9hPOl4JYYNXF623Gzn0=
Sec-WebSocket-Protocol: v10.stomp</pre>
</div>
</div>
<div class="paragraph">
<p>After a successful handshake the TCP socket underlying the HTTP upgrade request remains
open for both client and server to continue to send and receive messages.</p>
</div>
<div class="paragraph">
<p>A complete introduction of how WebSockets work is beyond the scope of this document.
Please read RFC 6455, the WebSocket chapter of HTML5, or one of many introductions and
tutorials on the Web.</p>
</div>
<div class="paragraph">
<p>Note that if a WebSocket server is running behind a web server (e.g. nginx) you will
likely need to configure it to pass WebSocket upgrade requests on to the WebSocket
server. Likewise if the application runs in a cloud environment, check the
instructions of the cloud provider related to WebSocket support.</p>
</div>
<div class="sect3">
<h4 id="websocket-intro-architecture"><a class="anchor" href="#websocket-intro-architecture"></a>4.1.1. HTTP vs WebSocket</h4>
<div class="paragraph">
<p>Even though WebSocket is designed to be HTTP compatible and starts with an HTTP request,
it is important to understand that the two protocols lead to very different
architectures and application programming models.</p>
</div>
<div class="paragraph">
<p>In HTTP and REST, an application is modeled as many URLs. To interact with the application
clients access those URLs, request-response style. Servers route requests to the
appropriate handler based on the HTTP URL, method, and headers.</p>
</div>
<div class="paragraph">
<p>By contrast in WebSockets there is usually just one URL for the initial connect and
subsequently all application messages flow on that same TCP connection. This points to
an entirely different asynchronous, event-driven, messaging architecture.</p>
</div>
<div class="paragraph">
<p>WebSocket is also a low-level transport protocol which unlike HTTP does not prescribe
any semantics to the content of messages. That means there is no way to route or process
a message unless client and server agree on message semantics.</p>
</div>
<div class="paragraph">
<p>WebSocket clients and servers can negotiate the use of a higher-level, messaging protocol
(e.g. STOMP), via the <code>"Sec-WebSocket-Protocol"</code> header on the HTTP handshake request,
or in the absence of that they need to come up with their own conventions.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-intro-when-to-use"><a class="anchor" href="#websocket-intro-when-to-use"></a>4.1.2. When to use it?</h4>
<div class="paragraph">
<p>WebSockets can make a web page dynamic and interactive. However in many cases
a combination of Ajax and HTTP streaming and/or long polling could provide a simple and
effective solution.</p>
</div>
<div class="paragraph">
<p>For example news, mail, and social feeds need to update dynamically but it may be
perfectly okay to do so every few minutes. Collaboration, games, and financial apps on
the other hand need to be much closer to real time.</p>
</div>
<div class="paragraph">
<p>Latency alone is not a deciding factor. If the volume of messages is relatively low (e.g.
monitoring network failures) HTTP streaming or polling may provide an effective solution.
It is the combination of low latency, high frequency and high volume that make the best
case for the use WebSocket.</p>
</div>
<div class="paragraph">
<p>Keep in mind also that over the Internet, restrictive proxies outside your control,
may preclude WebSocket interactions either because they are not configured to pass on the
<code>Upgrade</code> header or because they close long lived connections that appear idle? This
means that the use of WebSocket for internal applications within the firewall is a more
straight-forward decision than it is for public facing applications.</p>
</div>
</div>
</div>
<div class="sect2">
<h3 id="websocket-server"><a class="anchor" href="#websocket-server"></a>4.2. WebSocket API</h3>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket-server">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The Spring Framework provides a WebSocket API that can be used to write client and
server side applications that handle WebSocket messages.</p>
</div>
<div class="sect3">
<h4 id="websocket-server-handler"><a class="anchor" href="#websocket-server-handler"></a>4.2.1. WebSocketHandler</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket-server-handler">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Creating a WebSocket server is as simple as implementing <code>WebSocketHandler</code> or more
likely extending either <code>TextWebSocketHandler</code> or <code>BinaryWebSocketHandler</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">import</span> <span class="include">org.springframework.web.socket.WebSocketHandler</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.WebSocketSession</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.TextMessage</span>;

<span class="directive">public</span> <span class="type">class</span> <span class="class">MyHandler</span> <span class="directive">extends</span> TextWebSocketHandler {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> handleTextMessage(WebSocketSession session, TextMessage message) {
        <span class="comment">// ...</span>
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>There is dedicated WebSocket Java-config and XML namespace support for mapping the above
WebSocket handler to a specific URL:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.EnableWebSocket</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.WebSocketConfigurer</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry</span>;

<span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), <span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span>);
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> WebSocketHandler myHandler() {
        <span class="keyword">return</span> <span class="keyword">new</span> MyHandler();
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>XML configuration equivalent:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:handlers&gt;</span>
        <span class="tag">&lt;websocket:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">handler</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:handlers&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.samples.MyHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The above is for use in Spring MVC applications and should be included in the
configuration of a <a href="#mvc-servlet">DispatcherServlet</a>. However, Spring&#8217;s WebSocket
support does not depend on Spring MVC. It is relatively simple to integrate a <code>WebSocketHandler</code>
into other HTTP serving environments with the help of
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/socket/server/support/WebSocketHttpRequestHandler.html">WebSocketHttpRequestHandler</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-server-handshake"><a class="anchor" href="#websocket-server-handshake"></a>4.2.2. WebSocket Handshake</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket-server-handshake">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>The easiest way to customize the initial HTTP WebSocket handshake request is through
a <code>HandshakeInterceptor</code>, which exposes "before" and "after" the handshake methods.
Such an interceptor can be used to preclude the handshake or to make any attributes
available to the <code>WebSocketSession</code>. For example, there is a built-in interceptor
for passing HTTP session attributes to the WebSocket session:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(<span class="keyword">new</span> MyHandler(), <span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span>)
            .addInterceptors(<span class="keyword">new</span> HttpSessionHandshakeInterceptor());
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>And the XML configuration equivalent:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:handlers&gt;</span>
        <span class="tag">&lt;websocket:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">handler</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;websocket:handshake-interceptors&gt;</span>
            <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/websocket:handshake-interceptors&gt;</span>
    <span class="tag">&lt;/websocket:handlers&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.samples.MyHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>A more advanced option is to extend the <code>DefaultHandshakeHandler</code> that performs
the steps of the WebSocket handshake, including validating the client origin,
negotiating a sub-protocol, and others. An application may also need to use this
option if it needs to configure a custom <code>RequestUpgradeStrategy</code> in order to
adapt to a WebSocket server engine and version that is not yet supported
(also see <a href="#websocket-server-deployment">Deployment</a> for more on this subject).
Both the Java-config and XML namespace make it possible to configure a custom
<code>HandshakeHandler</code>.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring provides a <code>WebSocketHandlerDecorator</code> base class that can be used to decorate
a <code>WebSocketHandler</code> with additional behavior. Logging and exception handling
implementations are provided and added by default when using the WebSocket Java-config
or XML namespace. The <code>ExceptionWebSocketHandlerDecorator</code> catches all uncaught
exceptions arising from any WebSocketHandler method and closes the WebSocket
session with status <code>1011</code> that indicates a server error.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-server-deployment"><a class="anchor" href="#websocket-server-deployment"></a>4.2.3. Deployment</h4>
<div class="paragraph">
<p>The Spring WebSocket API is easy to integrate into a Spring MVC application where
the <code>DispatcherServlet</code> serves both HTTP WebSocket handshake as well as other
HTTP requests. It is also easy to integrate into other HTTP processing scenarios
by invoking <code>WebSocketHttpRequestHandler</code>. This is convenient and easy to
understand. However, special considerations apply with regards to JSR-356 runtimes.</p>
</div>
<div class="paragraph">
<p>The Java WebSocket API (JSR-356) provides two deployment mechanisms. The first
involves a Servlet container classpath scan (Servlet 3 feature) at startup; and
the other is a registration API to use at Servlet container initialization.
Neither of these mechanism makes it possible to use a single "front controller"
for all HTTP processing&#8201;&#8212;&#8201;including WebSocket handshake and all other HTTP
requests&#8201;&#8212;&#8201;such as Spring MVC&#8217;s <code>DispatcherServlet</code>.</p>
</div>
<div class="paragraph">
<p>This is a significant limitation of JSR-356 that Spring&#8217;s WebSocket support addresses
server-specific <code>RequestUpgradeStrategy</code>'s even when running in a JSR-356 runtime.
Such strategies currently exist for Tomcat, Jetty, GlassFish, WebLogic, WebSphere, and
Undertow (and WildFly).</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>A request to overcome the above limitation in the Java WebSocket API has been
created and can be followed at
<a href="https://github.com/eclipse-ee4j/websocket-api/issues/211">WEBSOCKET_SPEC-211</a>.
Tomcat, Undertow and WebSphere provide their own API alternatives that
makes it possible to this, and it&#8217;s also possible with Jetty. We are hopeful
that more servers will follow do the same.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>A secondary consideration is that Servlet containers with JSR-356 support are expected
to perform a <code>ServletContainerInitializer</code> (SCI) scan that can slow down application
startup, in some cases dramatically. If a significant impact is observed after an
upgrade to a Servlet container version with JSR-356 support, it should
be possible to selectively enable or disable web fragments (and SCI scanning)
through the use of the <code>&lt;absolute-ordering /&gt;</code> element in <code>web.xml</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;web-app</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://java.sun.com/xml/ns/javaee</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://java.sun.com/xml/ns/javaee</span>
        <span class="content">http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">version</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3.0</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;absolute-ordering</span><span class="tag">/&gt;</span>

<span class="tag">&lt;/web-app&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>You can then selectively enable web fragments by name, such as Spring&#8217;s own
<code>SpringServletContainerInitializer</code> that provides support for the Servlet 3
Java initialization API, if required:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;web-app</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://java.sun.com/xml/ns/javaee</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://java.sun.com/xml/ns/javaee</span>
        <span class="content">http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">version</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">3.0</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;absolute-ordering&gt;</span>
        <span class="tag">&lt;name&gt;</span>spring_web<span class="tag">&lt;/name&gt;</span>
    <span class="tag">&lt;/absolute-ordering&gt;</span>

<span class="tag">&lt;/web-app&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="websocket-server-runtime-configuration"><a class="anchor" href="#websocket-server-runtime-configuration"></a>4.2.4. Server config</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket-server-config">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>Each underlying WebSocket engine exposes configuration properties that control
runtime characteristics such as the size of message buffer sizes, idle timeout,
and others.</p>
</div>
<div class="paragraph">
<p>For Tomcat, WildFly, and GlassFish add a <code>ServletServerContainerFactoryBean</code> to your
WebSocket Java config:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = <span class="keyword">new</span> ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(<span class="integer">8192</span>);
        container.setMaxBinaryMessageBufferSize(<span class="integer">8192</span>);
        <span class="keyword">return</span> container;
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>or WebSocket XML namespace:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework...ServletServerContainerFactoryBean</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">maxTextMessageBufferSize</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">8192</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">maxBinaryMessageBufferSize</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">8192</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>For client side WebSocket configuration, you should use <code>WebSocketContainerFactoryBean</code>
(XML) or <code>ContainerProvider.getWebSocketContainer()</code> (Java config).</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>For Jetty, you&#8217;ll need to supply a pre-configured Jetty <code>WebSocketServerFactory</code> and plug
that into Spring&#8217;s <code>DefaultHandshakeHandler</code> through your WebSocket Java config:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(echoWebSocketHandler(),
            <span class="string"><span class="delimiter">&quot;</span><span class="content">/echo</span><span class="delimiter">&quot;</span></span>).setHandshakeHandler(handshakeHandler());
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> DefaultHandshakeHandler handshakeHandler() {

        WebSocketPolicy policy = <span class="keyword">new</span> WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize(<span class="integer">8192</span>);
        policy.setIdleTimeout(<span class="integer">600000</span>);

        <span class="keyword">return</span> <span class="keyword">new</span> DefaultHandshakeHandler(
                <span class="keyword">new</span> JettyRequestUpgradeStrategy(<span class="keyword">new</span> WebSocketServerFactory(policy)));
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>or WebSocket XML namespace:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:handlers&gt;</span>
        <span class="tag">&lt;websocket:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/echo</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">handler</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">echoHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;websocket:handshake-handler</span> <span class="attribute-name">ref</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">handshakeHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:handlers&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">handshakeHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework...DefaultHandshakeHandler</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;constructor-arg</span> <span class="attribute-name">ref</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">upgradeStrategy</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">upgradeStrategy</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework...JettyRequestUpgradeStrategy</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;constructor-arg</span> <span class="attribute-name">ref</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">serverFactory</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">serverFactory</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.eclipse.jetty...WebSocketServerFactory</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;constructor-arg&gt;</span>
            <span class="tag">&lt;bean</span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.eclipse.jetty...WebSocketPolicy</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
                <span class="tag">&lt;constructor-arg</span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">SERVER</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
                <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">inputBufferSize</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">8092</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
                <span class="tag">&lt;property</span> <span class="attribute-name">name</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">idleTimeout</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">600000</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
            <span class="tag">&lt;/bean&gt;</span>
        <span class="tag">&lt;/constructor-arg&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="websocket-server-allowed-origins"><a class="anchor" href="#websocket-server-allowed-origins"></a>4.2.5. Allowed origins</h4>
<div class="paragraph">
<p><span class="small"><a href="web-reactive.html#webflux-websocket-server-cors">Same in Spring WebFlux</a></span></p>
</div>
<div class="paragraph">
<p>As of Spring Framework 4.1.5, the default behavior for WebSocket and SockJS is to accept
only <em>same origin</em> requests. It is also possible to allow <em>all</em> or a specified list of origins.
This check is mostly designed for browser clients. There is nothing preventing other types
of clients from modifying the <code>Origin</code> header value (see
<a href="https://tools.ietf.org/html/rfc6454">RFC 6454: The Web Origin Concept</a> for more details).</p>
</div>
<div class="paragraph">
<p>The 3 possible behaviors are:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>Allow only same origin requests (default): in this mode, when SockJS is enabled, the
Iframe HTTP response header <code>X-Frame-Options</code> is set to <code>SAMEORIGIN</code>, and JSONP
transport is disabled since it does not allow to check the origin of a request.
As a consequence, IE6 and IE7 are not supported when this mode is enabled.</p>
</li>
<li>
<p>Allow a specified list of origins: each provided <em>allowed origin</em> must start with <code>http://</code>
or <code>https://</code>. In this mode, when SockJS is enabled, both IFrame and JSONP based
transports are disabled. As a consequence, IE6 through IE9 are not supported when this
mode is enabled.</p>
</li>
<li>
<p>Allow all origins: to enable this mode, you should provide <code>*</code> as the allowed origin
value. In this mode, all transports are available.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>WebSocket and SockJS allowed origins can be configured as shown bellow:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.EnableWebSocket</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.WebSocketConfigurer</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry</span>;

<span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), <span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span>).setAllowedOrigins(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://mydomain.com</span><span class="delimiter">&quot;</span></span>);
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> WebSocketHandler myHandler() {
        <span class="keyword">return</span> <span class="keyword">new</span> MyHandler();
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>XML configuration equivalent:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:handlers</span> <span class="attribute-name">allowed-origins</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://mydomain.com</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;websocket:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">handler</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:handlers&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.samples.MyHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="websocket-fallback"><a class="anchor" href="#websocket-fallback"></a>4.3. SockJS Fallback</h3>
<div class="paragraph">
<p>Over the public Internet, restrictive proxies outside your control may preclude WebSocket
interactions either because they are not configured to pass on the <code>Upgrade</code> header or
because they close long lived connections that appear idle.</p>
</div>
<div class="paragraph">
<p>The solution to this problem is WebSocket emulation, i.e. attempting to use WebSocket
first and then falling back on HTTP-based techniques that emulate a WebSocket
interaction and expose the same application-level API.</p>
</div>
<div class="paragraph">
<p>On the Servlet stack the Spring Framework provides both server (and also client) support
for the SockJS protocol.</p>
</div>
<div class="sect3">
<h4 id="websocket-fallback-sockjs-overview"><a class="anchor" href="#websocket-fallback-sockjs-overview"></a>4.3.1. Overview</h4>
<div class="paragraph">
<p>The goal of SockJS is to let applications use a WebSocket API but fall back to
non-WebSocket alternatives when necessary at runtime, i.e. without the need to
change application code.</p>
</div>
<div class="paragraph">
<p>SockJS consists of:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>The <a href="https://github.com/sockjs/sockjs-protocol">SockJS protocol</a>
defined in the form of executable
<a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html">narrated tests</a>.</p>
</li>
<li>
<p>The <a href="https://github.com/sockjs/sockjs-client/">SockJS JavaScript client</a> - a client library for use in browsers.</p>
</li>
<li>
<p>SockJS server implementations including one in the Spring Framework <code>spring-websocket</code> module.</p>
</li>
<li>
<p>As of 4.1 <code>spring-websocket</code> also provides a SockJS Java client.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>SockJS is designed for use in browsers. It goes to great lengths
to support a wide range of browser versions using a variety of techniques.
For the full list of SockJS transport types and browsers see the
<a href="https://github.com/sockjs/sockjs-client/">SockJS client</a> page. Transports
fall in 3 general categories: WebSocket, HTTP Streaming, and HTTP Long Polling.
For an overview of these categories see
<a href="https://spring.io/blog/2012/05/08/spring-mvc-3-2-preview-techniques-for-real-time-updates/">this blog post</a>.</p>
</div>
<div class="paragraph">
<p>The SockJS client begins by sending <code>"GET /info"</code> to
obtain basic information from the server. After that it must decide what transport
to use. If possible WebSocket is used. If not, in most browsers
there is at least one HTTP streaming option and if not then HTTP (long)
polling is used.</p>
</div>
<div class="paragraph">
<p>All transport requests have the following URL structure:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>http://host:port/myApp/myEndpoint/{server-id}/{session-id}/{transport}</pre>
</div>
</div>
<div class="ulist">
<ul>
<li>
<p><code>{server-id}</code> - useful for routing requests in a cluster but not used otherwise.</p>
</li>
<li>
<p><code>{session-id}</code> - correlates HTTP requests belonging to a SockJS session.</p>
</li>
<li>
<p><code>{transport}</code> - indicates the transport type, e.g. "websocket", "xhr-streaming", etc.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The WebSocket transport needs only a single HTTP request to do the WebSocket handshake.
All messages thereafter are exchanged on that socket.</p>
</div>
<div class="paragraph">
<p>HTTP transports require more requests. Ajax/XHR streaming for example relies on
one long-running request for server-to-client messages and additional HTTP POST
requests for client-to-server messages. Long polling is similar except it
ends the current request after each server-to-client send.</p>
</div>
<div class="paragraph">
<p>SockJS adds minimal message framing. For example the server sends the letter o
("open" frame) initially, messages are sent as a["message1","message2"]
(JSON-encoded array), the letter h ("heartbeat" frame) if no messages flow
for 25 seconds by default, and the letter c ("close" frame) to close the session.</p>
</div>
<div class="paragraph">
<p>To learn more, run an example in a browser and watch the HTTP requests.
The SockJS client allows fixing the list of transports so it is possible to
see each transport one at a time. The SockJS client also provides a debug flag
which enables helpful messages in the browser console. On the server side enable
<code>TRACE</code> logging for <code>org.springframework.web.socket</code>.
For even more detail refer to the SockJS protocol
<a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html">narrated test</a>.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-sockjs-enable"><a class="anchor" href="#websocket-fallback-sockjs-enable"></a>4.3.2. Enable SockJS</h4>
<div class="paragraph">
<p>SockJS is easy to enable through Java configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), <span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span>).withSockJS();
    }

    <span class="annotation">@Bean</span>
    <span class="directive">public</span> WebSocketHandler myHandler() {
        <span class="keyword">return</span> <span class="keyword">new</span> MyHandler();
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>and the XML configuration equivalent:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:handlers&gt;</span>
        <span class="tag">&lt;websocket:mapping</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">handler</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;websocket:sockjs</span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:handlers&gt;</span>

    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">myHandler</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.samples.MyHandler</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The above is for use in Spring MVC applications and should be included in the
configuration of a <a href="#mvc-servlet">DispatcherServlet</a>. However, Spring&#8217;s WebSocket
and SockJS support does not depend on Spring MVC. It is relatively simple to
integrate into other HTTP serving environments with the help of
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/socket/sockjs/support/SockJsHttpRequestHandler.html">SockJsHttpRequestHandler</a>.</p>
</div>
<div class="paragraph">
<p>On the browser side, applications can use the
<a href="https://github.com/sockjs/sockjs-client/">sockjs-client</a> (version 1.0.x) that
emulates the W3C WebSocket API and communicates with the server to select the best
transport option depending on the browser it&#8217;s running in. Review the
<a href="https://github.com/sockjs/sockjs-client/">sockjs-client</a> page and the list of
transport types supported by browser. The client also provides several
configuration options, for example, to specify which transports to include.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-xhr-vs-iframe"><a class="anchor" href="#websocket-fallback-xhr-vs-iframe"></a>4.3.3. IE 8, 9</h4>
<div class="paragraph">
<p>Internet Explorer 8 and 9 are and will remain common for some time. They are
a key reason for having SockJS. This section covers important
considerations about running in those browsers.</p>
</div>
<div class="paragraph">
<p>The SockJS client supports Ajax/XHR streaming in IE 8 and 9 via Microsoft&#8217;s
<a href="https://blogs.msdn.com/b/ieinternals/archive/2010/05/13/xdomainrequest-restrictions-limitations-and-workarounds.aspx">XDomainRequest</a>.
That works across domains but does not support sending cookies.
Cookies are very often essential for Java applications.
However since the SockJS client can be used with many server
types (not just Java ones), it needs to know whether cookies matter.
If so the SockJS client prefers Ajax/XHR for streaming or otherwise it
relies on a iframe-based technique.</p>
</div>
<div class="paragraph">
<p>The very first <code>"/info"</code> request from the SockJS client is a request for
information that can influence the client&#8217;s choice of transports.
One of those details is whether the server application relies on cookies,
e.g. for authentication purposes or clustering with sticky sessions.
Spring&#8217;s SockJS support includes a property called <code>sessionCookieNeeded</code>.
It is enabled by default since most Java applications rely on the <code>JSESSIONID</code>
cookie. If your application does not need it, you can turn off this option
and the SockJS client should choose <code>xdr-streaming</code> in IE 8 and 9.</p>
</div>
<div class="paragraph">
<p>If you do use an iframe-based transport, and in any case, it is good to know
that browsers can be instructed to block the use of IFrames on a given page by
setting the HTTP response header <code>X-Frame-Options</code> to <code>DENY</code>,
<code>SAMEORIGIN</code>, or <code>ALLOW-FROM &lt;origin&gt;</code>. This is used to prevent
<a href="https://www.owasp.org/index.php/Clickjacking">clickjacking</a>.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring Security 3.2+ provides support for setting <code>X-Frame-Options</code> on every
response. By default the Spring Security Java config sets it to <code>DENY</code>.
In 3.2 the Spring Security XML namespace does not set that header by default
but may be configured to do so, and in the future it may set it by default.</p>
</div>
<div class="paragraph">
<p>See <a href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#headers">Section 7.1. "Default Security Headers"</a>
of the Spring Security documentation for details on how to configure the
setting of the <code>X-Frame-Options</code> header. You may also check or watch
<a href="https://jira.spring.io/browse/SEC-2501">SEC-2501</a> for additional background.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>If your application adds the <code>X-Frame-Options</code> response header (as it should!)
and relies on an iframe-based transport, you will need to set the header value to
<code>SAMEORIGIN</code> or <code>ALLOW-FROM &lt;origin&gt;</code>. Along with that the Spring SockJS
support also needs to know the location of the SockJS client because it is loaded
from the iframe. By default the iframe is set to download the SockJS client
from a CDN location. It is a good idea to configure this option to
a URL from the same origin as the application.</p>
</div>
<div class="paragraph">
<p>In Java config this can be done as shown below. The XML namespace provides a
similar option via the <code>&lt;websocket:sockjs&gt;</code> element:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocket</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span>).withSockJS()
                .setClientLibraryUrl(<span class="string"><span class="delimiter">&quot;</span><span class="content">http://localhost:8080/myapp/js/sockjs-client.js</span><span class="delimiter">&quot;</span></span>);
    }

    <span class="comment">// ...</span>

}</code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>During initial development, do enable the SockJS client <code>devel</code> mode that prevents
the browser from caching SockJS requests (like the iframe) that would otherwise
be cached. For details on how to enable it see the
<a href="https://github.com/sockjs/sockjs-client/">SockJS client</a> page.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-sockjs-heartbeat"><a class="anchor" href="#websocket-fallback-sockjs-heartbeat"></a>4.3.4. Heartbeats</h4>
<div class="paragraph">
<p>The SockJS protocol requires servers to send heartbeat messages to preclude proxies
from concluding a connection is hung. The Spring SockJS configuration has a property
called <code>heartbeatTime</code> that can be used to customize the frequency. By default a
heartbeat is sent after 25 seconds assuming no other messages were sent on that
connection. This 25 seconds value is in line with the following
<a href="https://tools.ietf.org/html/rfc6202">IETF recommendation</a> for public Internet applications.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When using STOMP over WebSocket/SockJS, if the STOMP client and server negotiate
heartbeats to be exchanged, the SockJS heartbeats are disabled.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The Spring SockJS support also allows configuring the <code>TaskScheduler</code> to use
for scheduling heartbeats tasks. The task scheduler is backed by a thread pool
with default settings based on the number of available processors. Applications
should consider customizing the settings according to their specific needs.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-sockjs-servlet3-async"><a class="anchor" href="#websocket-fallback-sockjs-servlet3-async"></a>4.3.5. Client disconnects</h4>
<div class="paragraph">
<p>HTTP streaming and HTTP long polling SockJS transports require a connection to remain
open longer than usual. For an overview of these techniques see
<a href="https://spring.io/blog/2012/05/08/spring-mvc-3-2-preview-techniques-for-real-time-updates/">this blog post</a>.</p>
</div>
<div class="paragraph">
<p>In Servlet containers this is done through Servlet 3 async support that
allows exiting the Servlet container thread processing a request and continuing
to write to the response from another thread.</p>
</div>
<div class="paragraph">
<p>A specific issue is that the Servlet API does not provide notifications for a client
that has gone away, see <a href="https://java.net/jira/browse/SERVLET_SPEC-44">SERVLET_SPEC-44</a>.
However, Servlet containers raise an exception on subsequent attempts to write
to the response. Since Spring&#8217;s SockJS Service supports sever-sent heartbeats (every
25 seconds by default), that means a client disconnect is usually detected within that
time period or earlier if messages are sent more frequently.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>As a result network IO failures may occur simply because a client has disconnected, which
can fill the log with unnecessary stack traces. Spring makes a best effort to identify
such network failures that represent client disconnects (specific to each server) and log
a minimal message using the dedicated log category <code>DISCONNECTED_CLIENT_LOG_CATEGORY</code>
defined in <code>AbstractSockJsSession</code>. If you need to see the stack traces, set that
log category to TRACE.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-cors"><a class="anchor" href="#websocket-fallback-cors"></a>4.3.6. SockJS and CORS</h4>
<div class="paragraph">
<p>If you allow cross-origin requests (see <a href="#websocket-server-allowed-origins">Allowed origins</a>), the SockJS protocol
uses CORS for cross-domain support in the XHR streaming and polling transports. Therefore
CORS headers are added automatically unless the presence of CORS headers in the response
is detected. So if an application is already configured to provide CORS support, e.g.
through a Servlet Filter, Spring&#8217;s SockJsService will skip this part.</p>
</div>
<div class="paragraph">
<p>It is also possible to disable the addition of these CORS headers via the
<code>suppressCors</code> property in Spring&#8217;s SockJsService.</p>
</div>
<div class="paragraph">
<p>The following is the list of headers and values expected by SockJS:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>"Access-Control-Allow-Origin"</code> - initialized from the value of the "Origin" request header.</p>
</li>
<li>
<p><code>"Access-Control-Allow-Credentials"</code> - always set to <code>true</code>.</p>
</li>
<li>
<p><code>"Access-Control-Request-Headers"</code> - initialized from values from the equivalent request header.</p>
</li>
<li>
<p><code>"Access-Control-Allow-Methods"</code> - the HTTP methods a transport supports (see <code>TransportType</code> enum).</p>
</li>
<li>
<p><code>"Access-Control-Max-Age"</code> - set to 31536000 (1 year).</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>For the exact implementation see <code>addCorsHeaders</code> in <code>AbstractSockJsService</code> as well
as the <code>TransportType</code> enum in the source code.</p>
</div>
<div class="paragraph">
<p>Alternatively if the CORS configuration allows it consider excluding URLs with the
SockJS endpoint prefix thus letting Spring&#8217;s <code>SockJsService</code> handle it.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-fallback-sockjs-client"><a class="anchor" href="#websocket-fallback-sockjs-client"></a>4.3.7. SockJsClient</h4>
<div class="paragraph">
<p>A SockJS Java client is provided in order to connect to remote SockJS endpoints without
using a browser. This can be especially useful when there is a need for bidirectional
communication between 2 servers over a public network, i.e. where network proxies may
preclude the use of the WebSocket protocol. A SockJS Java client is also very useful
for testing purposes, for example to simulate a large number of concurrent users.</p>
</div>
<div class="paragraph">
<p>The SockJS Java client supports the "websocket", "xhr-streaming", and "xhr-polling"
transports. The remaining ones only make sense for use in a browser.</p>
</div>
<div class="paragraph">
<p>The <code>WebSocketTransport</code> can be configured with:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>StandardWebSocketClient</code> in a JSR-356 runtime</p>
</li>
<li>
<p><code>JettyWebSocketClient</code> using the Jetty 9+ native WebSocket API</p>
</li>
<li>
<p>Any implementation of Spring&#8217;s <code>WebSocketClient</code></p>
</li>
</ul>
</div>
<div class="paragraph">
<p>An <code>XhrTransport</code> by definition supports both "xhr-streaming" and "xhr-polling" since
from a client perspective there is no difference other than in the URL used to connect
to the server. At present there are two implementations:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>RestTemplateXhrTransport</code> uses Spring&#8217;s <code>RestTemplate</code> for HTTP requests.</p>
</li>
<li>
<p><code>JettyXhrTransport</code> uses Jetty&#8217;s <code>HttpClient</code> for HTTP requests.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The example below shows how to create a SockJS client and connect to a SockJS endpoint:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="predefined-type">List</span>&lt;Transport&gt; transports = <span class="keyword">new</span> <span class="predefined-type">ArrayList</span>&lt;&gt;(<span class="integer">2</span>);
transports.add(<span class="keyword">new</span> WebSocketTransport(<span class="keyword">new</span> StandardWebSocketClient()));
transports.add(<span class="keyword">new</span> RestTemplateXhrTransport());

SockJsClient sockJsClient = <span class="keyword">new</span> SockJsClient(transports);
sockJsClient.doHandshake(<span class="keyword">new</span> MyWebSocketHandler(), <span class="string"><span class="delimiter">&quot;</span><span class="content">ws://example.com:8080/sockjs</span><span class="delimiter">&quot;</span></span>);</code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>SockJS uses JSON formatted arrays for messages. By default Jackson 2 is used and needs
to be on the classpath. Alternatively you can configure a custom implementation of
<code>SockJsMessageCodec</code> and configure it on the <code>SockJsClient</code>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>To use the SockJsClient for simulating a large number of concurrent users you will
need to configure the underlying HTTP client (for XHR transports) to allow a sufficient
number of connections and threads. For example with Jetty:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">HttpClient jettyHttpClient = <span class="keyword">new</span> HttpClient();
jettyHttpClient.setMaxConnectionsPerDestination(<span class="integer">1000</span>);
jettyHttpClient.setExecutor(<span class="keyword">new</span> QueuedThreadPool(<span class="integer">1000</span>));</code></pre>
</div>
</div>
<div class="paragraph">
<p>Consider also customizing these server-side SockJS related properties (see Javadoc for details):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">extends</span> WebSocketMessageBrokerConfigurationSupport {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(<span class="string"><span class="delimiter">&quot;</span><span class="content">/sockjs</span><span class="delimiter">&quot;</span></span>).withSockJS()
            .setStreamBytesLimit(<span class="integer">512</span> * <span class="integer">1024</span>)
            .setHttpMessageCacheSize(<span class="integer">1000</span>)
            .setDisconnectDelay(<span class="integer">30</span> * <span class="integer">1000</span>);
    }

    <span class="comment">// ...</span>
}</code></pre>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="websocket-stomp"><a class="anchor" href="#websocket-stomp"></a>4.4. STOMP</h3>
<div class="paragraph">
<p>The WebSocket protocol defines two types of messages, text and binary, but their
content is undefined. The defines a mechanism for client and server to negotiate a
sub-protocol&#8201;&#8212;&#8201;i.e. a higher level messaging protocol, to use on top of WebSocket to
define what kind of messages each can send, what is the format and content for each
message, and so on. The use of a sub-protocol is optional but either way client and
server will need to agree on some protocol that defines message content.</p>
</div>
<div class="sect3">
<h4 id="websocket-stomp-overview"><a class="anchor" href="#websocket-stomp-overview"></a>4.4.1. Overview</h4>
<div class="paragraph">
<p><a href="https://stomp.github.io/stomp-specification-1.2.html#Abstract">STOMP</a> is a simple,
text-oriented messaging protocol that was originally created for scripting languages
such as Ruby, Python, and Perl to connect to enterprise message brokers. It is
designed to address a minimal subset of commonly used messaging patterns. STOMP can be
used over any reliable, 2-way streaming network protocol such as TCP and WebSocket.
Although STOMP is a text-oriented protocol, message payloads can be
either text or binary.</p>
</div>
<div class="paragraph">
<p>STOMP is a frame based protocol whose frames are modeled on HTTP. The structure
of a STOMP frame:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>COMMAND
header1:value1
header2:value2

Body^@</pre>
</div>
</div>
<div class="paragraph">
<p>Clients can use the SEND or SUBSCRIBE commands to send or subscribe for
messages along with a "destination" header that describes what the
message is about and who should receive it. This enables a simple
publish-subscribe mechanism that can be used to send messages through the broker
to other connected clients or to send messages to the server to request that
some work be performed.</p>
</div>
<div class="paragraph">
<p>When using Spring&#8217;s STOMP support, the Spring WebSocket application acts
as the STOMP broker to clients. Messages are routed to <code>@Controller</code> message-handling
methods or to a simple, in-memory broker that keeps track of subscriptions and
broadcasts messages to subscribed users. You can also configure Spring to work
with a dedicated STOMP broker (e.g. RabbitMQ, ActiveMQ, etc) for the actual
broadcasting of messages. In that case Spring maintains
TCP connections to the broker, relays messages to it, and also passes messages
from it down to connected WebSocket clients. Thus Spring web applications can
rely on unified HTTP-based security, common validation, and a familiar programming
model message-handling work.</p>
</div>
<div class="paragraph">
<p>Here is an example of a client subscribing to receive stock quotes which
the server may emit periodically e.g. via a scheduled task sending messages
through a <code>SimpMessagingTemplate</code> to the broker:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>SUBSCRIBE
id:sub-1
destination:/topic/price.stock.*

^@</pre>
</div>
</div>
<div class="paragraph">
<p>Here is an example of a client sending a trade request, which the server
may handle through an <code>@MessageMapping</code> method and later on, after the execution,
broadcast a trade confirmation message and details down to the client:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>SEND
destination:/queue/trade
content-type:application/json
content-length:44

{"action":"BUY","ticker":"MMM","shares",44}^@</pre>
</div>
</div>
<div class="paragraph">
<p>The meaning of a destination is intentionally left opaque in the STOMP spec. It can
be any string, and it&#8217;s entirely up to STOMP servers to define the semantics and
the syntax of the destinations that they support. It is very common, however, for
destinations to be path-like strings where <code>"/topic/.."</code> implies publish-subscribe
(<em>one-to-many</em>) and <code>"/queue/"</code> implies point-to-point (<em>one-to-one</em>) message
exchanges.</p>
</div>
<div class="paragraph">
<p>STOMP servers can use the MESSAGE command to broadcast messages to all subscribers.
Here is an example of a server sending a stock quote to a subscribed client:</p>
</div>
<div class="listingblock">
<div class="content">
<pre>MESSAGE
message-id:nxahklf6-1
subscription:sub-1
destination:/topic/price.stock.MMM

{"ticker":"MMM","price":129.45}^@</pre>
</div>
</div>
<div class="paragraph">
<p>It is important to know that a server cannot send unsolicited messages. All messages
from a server must be in response to a specific client subscription, and the
"subscription-id" header of the server message must match the "id" header of the
client subscription.</p>
</div>
<div class="paragraph">
<p>The above overview is intended to provide the most basic understanding of the
STOMP protocol. It is recommended to review the protocol
<a href="https://stomp.github.io/stomp-specification-1.2.html">specification</a> in full.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-benefits"><a class="anchor" href="#websocket-stomp-benefits"></a>4.4.2. Benefits</h4>
<div class="paragraph">
<p>Use of STOMP as a sub-protocol enables the Spring Framework and Spring Security to
provide a richer programming model vs using raw WebSockets. The same point can be
made about how HTTP vs raw TCP and how it enables Spring MVC and other web frameworks
to provide rich functionality. The following is a list of benefits:</p>
</div>
<div class="ulist">
<ul>
<li>
<p>No need to invent a custom messaging protocol and message format.</p>
</li>
<li>
<p>STOMP clients are available including a <a href="#websocket-stomp-client">Java client</a>
in the Spring Framework.</p>
</li>
<li>
<p>Message brokers such as RabbitMQ, ActiveMQ, and others can be used (optionally) to
manage subscriptions and broadcast messages.</p>
</li>
<li>
<p>Application logic can be organized in any number of <code>@Controller</code>'s and messages
routed to them based on the STOMP destination header vs handling raw WebSocket messages
with a single <code>WebSocketHandler</code> for a given connection.</p>
</li>
<li>
<p>Use Spring Security to secure messages based on STOMP destinations and message types.</p>
</li>
</ul>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-enable"><a class="anchor" href="#websocket-stomp-enable"></a>4.4.3. Enable STOMP</h4>
<div class="paragraph">
<p>STOMP over WebSocket support is available in the <code>spring-messaging</code> and the
<code>spring-websocket</code> modules. Once you have those dependencies, you can expose a STOMP
endpoints, over WebSocket with <a href="#websocket-fallback">SockJS Fallback</a>, as shown below:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker</span>;
<span class="keyword">import</span> <span class="include">org.springframework.web.socket.config.annotation.StompEndpointRegistry</span>;

<span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span>).withSockJS();  <i class="conum" data-value="1"></i><b>(1)</b>
    }

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span>); <i class="conum" data-value="2"></i><b>(2)</b>
        config.enableSimpleBroker(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">/queue</span><span class="delimiter">&quot;</span></span>); <i class="conum" data-value="3"></i><b>(3)</b>
    }
}</code></pre>
</div>
</div>
<div class="colist arabic">
<table>
<tr>
<td><i class="conum" data-value="1"></i><b>1</b></td>
<td><code>"/portfolio"</code> is the HTTP URL for the endpoint to which a WebSocket (or SockJS)
client will need to connect to for the WebSocket handshake.</td>
</tr>
<tr>
<td><i class="conum" data-value="2"></i><b>2</b></td>
<td>STOMP messages whose destination header begins with <code>"/app"</code> are routed to
<code>@MessageMapping</code> methods in <code>@Controller</code> classes.</td>
</tr>
<tr>
<td><i class="conum" data-value="3"></i><b>3</b></td>
<td>Use the built-in, message broker for subscriptions and broadcasting;
Route messages whose destination header begins with "/topic" or "/queue" to the broker.</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The same configuration in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker</span> <span class="attribute-name">application-destination-prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;websocket:stomp-endpoint</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
            <span class="tag">&lt;websocket:sockjs</span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/websocket:stomp-endpoint&gt;</span>
        <span class="tag">&lt;websocket:simple-broker</span> <span class="attribute-name">prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic, /queue</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>For the built-in, simple broker the "/topic" and "/queue" prefixes do not have any special
meaning. They&#8217;re merely a convention to differentiate between pub-sub vs point-to-point
messaging (i.e. many subscribers vs one consumer). When using an external broker, please
check the STOMP page of the broker to understand what kind of STOMP destinations and
prefixes it supports.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>To connect from a browser, for SockJS you can use the
<a href="https://github.com/sockjs/sockjs-client">sockjs-client</a>. For STOMP many applications have
used the <a href="https://github.com/jmesnil/stomp-websocket">jmesnil/stomp-websocket</a> library
(also known as stomp.js) which is feature complete and has been used in production for
years but is no longer maintained. At present the
<a href="https://github.com/JSteunou/webstomp-client">JSteunou/webstomp-client</a> is the most
actively maintained and evolving successor of that library and the example code below
is based on it:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="javascript"><span class="keyword">var</span> socket = <span class="keyword">new</span> SockJS(<span class="string"><span class="delimiter">&quot;</span><span class="content">/spring-websocket-portfolio/portfolio</span><span class="delimiter">&quot;</span></span>);
<span class="keyword">var</span> stompClient = webstomp.over(socket);

stompClient.connect({}, <span class="keyword">function</span>(frame) {
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Or if connecting via WebSocket (without SockJS):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="javascript"><span class="keyword">var</span> socket = <span class="keyword">new</span> WebSocket(<span class="string"><span class="delimiter">&quot;</span><span class="content">/spring-websocket-portfolio/portfolio</span><span class="delimiter">&quot;</span></span>);
<span class="keyword">var</span> stompClient = Stomp.over(socket);

stompClient.connect({}, <span class="keyword">function</span>(frame) {
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Note that the <code>stompClient</code> above does not need to specify <code>login</code> and <code>passcode</code> headers.
Even if it did, they would be ignored, or rather overridden, on the server side. See the
sections <a href="#websocket-stomp-handle-broker-relay-configure">Connect to Broker</a> and
<a href="#websocket-stomp-authentication">Authentication</a> for more information on authentication.</p>
</div>
<div class="paragraph">
<p>For a more example code see:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="https://spring.io/guides/gs/messaging-stomp-websocket/">Using WebSocket to build an
interactive web application</a> getting started guide.</p>
</li>
<li>
<p><a href="https://github.com/rstoyanchev/spring-websocket-portfolio">Stock Portfolio</a> sample
application.</p>
</li>
</ul>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-message-flow"><a class="anchor" href="#websocket-stomp-message-flow"></a>4.4.4. Flow of Messages</h4>
<div class="paragraph">
<p>Once a STOMP endpoint is exposed, the Spring application becomes a STOMP broker for
connected clients. This section describes the flow of messages on the server side.</p>
</div>
<div class="paragraph">
<p>The <code>spring-messaging</code> module contains foundational support for messaging applications
that originated in <a href="https://spring.io/spring-integration">Spring Integration</a> and was
later extracted and incorporated into the Spring Framework for broader use across many
<a href="https://spring.io/projects">Spring projects</a> and application scenarios.
Below is a list of a few of the available messaging abstractions:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/Message.html">Message</a>&#8201;&#8212;&#8201;simple representation for a message including headers and payload.</p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/MessageHandler.html">MessageHandler</a>&#8201;&#8212;&#8201;contract for handling a message.</p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/MessageChannel.html">MessageChannel</a>&#8201;&#8212;&#8201;contract for sending a message that enables loose coupling between producers and consumers.</p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/SubscribableChannel.html">SubscribableChannel</a>&#8201;&#8212;&#8201;<code>MessageChannel</code> with <code>MessageHandler</code> subscribers.</p>
</li>
<li>
<p><a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/support/ExecutorSubscribableChannel.html">ExecutorSubscribableChannel</a>&#8201;&#8212;&#8201;<code>SubscribableChannel</code> that uses an <code>Executor</code> for delivering messages.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>Both the Java config (i.e. <code>@EnableWebSocketMessageBroker</code>) and the XML namespace config
(i.e. <code>&lt;websocket:message-broker&gt;</code>) use the above components to assemble a message
workflow. The diagram below shows the components used when the simple, built-in message
broker is enabled:</p>
</div>
<div class="imageblock">
<div class="content">
<img src="images/message-flow-simple-broker.png" alt="message flow simple broker">
</div>
</div>
<div class="paragraph">
<p>There are 3 message channels in the above diagram:</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>"clientInboundChannel"</code>&#8201;&#8212;&#8201;for passing messages received from WebSocket clients.</p>
</li>
<li>
<p><code>"clientOutboundChannel"</code>&#8201;&#8212;&#8201;for sending server messages to WebSocket clients.</p>
</li>
<li>
<p><code>"brokerChannel"</code>&#8201;&#8212;&#8201;for sending messages to the message broker from within
server-side, application code.</p>
</li>
</ul>
</div>
<div class="paragraph">
<p>The next diagram shows the components used when an external broker (e.g. RabbitMQ)
is configured for managing subscriptions and broadcasting messages:</p>
</div>
<div class="imageblock">
<div class="content">
<img src="images/message-flow-broker-relay.png" alt="message flow broker relay">
</div>
</div>
<div class="paragraph">
<p>The main difference in the above diagram is the use of the "broker relay" for passing
messages up to the external STOMP broker over TCP, and for passing messages down from the
broker to subscribed clients.</p>
</div>
<div class="paragraph">
<p>When messages are received from a WebSocket connectin, they&#8217;re decoded to STOMP frames,
then turned into a Spring <code>Message</code> representation, and sent to the
<code>"clientInboundChannel"</code> for further processing. For example STOMP messages whose
destination header starts with <code>"/app"</code> may be routed to <code>@MessageMapping</code> methods in
annotated controllers, while <code>"/topic"</code> and <code>"/queue"</code> messages may be routed directly
to the message broker.</p>
</div>
<div class="paragraph">
<p>An annotated <code>@Controller</code> handling a STOMP message from a client may send a message to
the message broker through the <code>"brokerChannel"</code>, and the broker will broadcast the
message to matching subscribers through the <code>"clientOutboundChannel"</code>. The same
controller can also do the same in response to HTTP requests, so a client may perform an
HTTP POST and then an <code>@PostMapping</code> method can send a message to the message broker
to broadcast to subscribed clients.</p>
</div>
<div class="paragraph">
<p>Let&#8217;s trace the flow through a simple example. Given the following server setup:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span>);
    }

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span>);
        registry.enableSimpleBroker(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic</span><span class="delimiter">&quot;</span></span>);
    }

}

<span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">GreetingController</span> {

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/greeting</span><span class="delimiter">&quot;</span></span>) {
    <span class="directive">public</span> <span class="predefined-type">String</span> handle(<span class="predefined-type">String</span> greeting) {
        <span class="keyword">return</span> <span class="string"><span class="delimiter">&quot;</span><span class="content">[</span><span class="delimiter">&quot;</span></span> + getTimestamp() + <span class="string"><span class="delimiter">&quot;</span><span class="content">: </span><span class="delimiter">&quot;</span></span> + greeting;
    }

}</code></pre>
</div>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p>Client connects to <code>"http://localhost:8080/portfolio"</code> and once a WebSocket connection
is established, STOMP frames begin to flow on it.</p>
</li>
<li>
<p>Client sends SUBSCRIBE frame with destination header <code>"/topic/greeting"</code>. Once received
and decoded, the message is sent to the <code>"clientInboundChannel"</code>, then routed to the
message broker which stores the client subscription.</p>
</li>
<li>
<p>Client sends SEND frame to <code>"/app/greeting"</code>. The <code>"/app"</code> prefix helps to route it to
annotated controllers. After the <code>"/app"</code> prefix is stripped, the remaining <code>"/greeting"</code>
part of the destination is mapped to the <code>@MessageMapping</code> method in <code>GreetingController</code>.</p>
</li>
<li>
<p>The value returned from <code>GreetingController</code> is turned into a Spring <code>Message</code> with
a payload based on the return value and a default destination header of
<code>"/topic/greeting"</code> (derived from the input destination with <code>"/app"</code> replaced by
<code>"/topic"</code>). The resulting message is sent to the "brokerChannel" and handled
by the message broker.</p>
</li>
<li>
<p>The message broker finds all matching subscribers, and sends a MESSAGE frame to each
through the <code>"clientOutboundChannel"</code> from where messages are encoded as STOMP frames
and sent on the WebSocket connection.</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>The next section provides more details on annotated methods including the
kinds of arguments and return values supported.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-handle-annotations"><a class="anchor" href="#websocket-stomp-handle-annotations"></a>4.4.5. Annotated Controllers</h4>
<div class="paragraph">
<p>Applications can use annotated <code>@Controller</code> classes to handle messages from clients.
Such classes can declare <code>@MessageMapping</code>, <code>@SubscribeMapping</code>, and <code>@ExceptionHandler</code>
methods as described next.</p>
</div>
<div class="sect4">
<h5 id="websocket-stomp-message-mapping"><a class="anchor" href="#websocket-stomp-message-mapping"></a><code>@MessageMapping</code></h5>
<div class="paragraph">
<p>The <code>@MessageMapping</code> annotation can be used on methods to route messages based on their
destination. It is supported at the method level as well as at the type level. At type
level <code>@MessageMapping</code> is used to express shared mappings across all methods in a
controller.</p>
</div>
<div class="paragraph">
<p>By default destination mappings are expected to be Ant-style, path patterns, e.g. "/foo*",
"/foo/**". The patterns include support for template variables, e.g. "/foo/{id}", that can
be referenced with <code>@DestinationVariable</code> method arguments.</p>
</div>
<div class="admonitionblock tip">
<table>
<tr>
<td class="icon">
<i class="fa icon-tip" title="Tip"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Applications can choose to switch to a dot-separated destination convention.
See <a href="#websocket-stomp-destination-separator">Dot as Separator</a>.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p><code>@MessageMapping</code> methods can have flexible signatures with the following arguments:</p>
</div>
<table class="tableblock frame-all grid-all spread">
<colgroup>
<col style="width: 33.3333%;">
<col style="width: 66.6667%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Method argument</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Message</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the complete message.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>MessageHeaders</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the headers within the <code>Message</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>MessageHeaderAccessor</code>, <code>SimpMessageHeaderAccessor</code>, <code>StompHeaderAccessor</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the headers via typed accessor methods.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@Payload</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to the payload of the message, converted (e.g. from JSON) via a configured
<code>MessageConverter</code>.</p>
<p class="tableblock">The presence of this annotation is not required since it is assumed by default if no
other argument is matched.</p>
<p class="tableblock">Payload arguments may be annotated with <code>@javax.validation.Valid</code> or Spring&#8217;s <code>@Validated</code>
in order to be automatically validated.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@Header</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to a specific header value along with type conversion using an
<code>org.springframework.core.convert.converter.Converter</code> if necessary.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@Headers</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to all headers in the message. This argument must be assignable to
<code>java.util.Map</code>.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>@DestinationVariable</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">For access to template variables extracted from the message destination.
Values will be converted to the declared method argument type as necessary.</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>java.security.Principal</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">Reflects the user logged in at the time of the WebSocket HTTP handshake.</p></td>
</tr>
</tbody>
</table>
<div class="paragraph">
<p>When an <code>@MessageMapping</code> method returns a value, by default the value is serialized to
a payload through a configured <code>MessageConverter</code>, and then sent as a <code>Message</code> to the
<code>"brokerChannel"</code> from where it is broadcast to subscribers. The destination of the
outbound message is the same as that of the inbound message but prefixed with <code>"/topic"</code>.</p>
</div>
<div class="paragraph">
<p>You can use the <code>@SendTo</code> method annotation to customize the destination to send
the payload to. <code>@SendTo</code> can also be used at the class level to share a default target
destination to send messages to. <code>@SendToUser</code> is an variant for sending messages only to
the user associated with a message. See <a href="#websocket-stomp-user-destination">User Destinations</a> for details.</p>
</div>
<div class="paragraph">
<p>The return value from an <code>@MessageMapping</code> method may be wrapped with <code>ListenableFuture</code>,
<code>CompletableFuture</code>, or <code>CompletionStage</code> in order to produce the payload asynchronously.</p>
</div>
<div class="paragraph">
<p>As an alternative to returning a payload from an <code>@MessageMapping</code> method you can also
send messages using the <code>SimpMessagingTemplate</code>, which is also how return values are
handled under the covers. See <a href="#websocket-stomp-handle-send">Send Messages</a>.</p>
</div>
</div>
<div class="sect4">
<h5 id="websocket-stomp-subscribe-mapping"><a class="anchor" href="#websocket-stomp-subscribe-mapping"></a><code>@SubscribeMapping</code></h5>
<div class="paragraph">
<p><code>@SubscribeMapping</code> is similar to <code>@MessageMapping</code> but narrows the mapping to
subscription messages only. It supports the same
<a href="#websocket-stomp-message-mapping">method arguments</a> as <code>@MessageMapping</code> does. However
for the return value, by default a message is sent directly to the client via
"clientOutboundChannel" in response to the subscription, and not to the broker via
"brokerChannel" as a broadcast to matching subscriptions. Adding <code>@SendTo</code> or
<code>@SendToUser</code> overrides this behavior and sends to the broker instead.</p>
</div>
<div class="paragraph">
<p>When is this useful? Let&#8217;s assume the broker is mapped to "/topic" and "/queue" while
application controllers are mapped to "/app". In this setup, the broker <strong>stores</strong> all
subscriptions to "/topic" and "/queue" that are intended for <strong>repeated</strong> broadcasts, and
there is no need for the application to get involved. A client could also also subscribe to
some "/app" destination and a controller could return a value in response to that
subscription without involving the broker, effectively a <strong>one-off</strong>, <strong>request-reply</strong> exchange,
without storing or using the subscription again. One case for this is populating a UI
with initial data on startup.</p>
</div>
<div class="paragraph">
<p>When is this not useful? Do not try to map broker and controllers to the same destination
prefix unless you want both to process messages, including subscriptions, independently
for some reason. Inbound messages are handled in parallel. There are no guarantees whether
broker or controller will process a given message first. If the goal is to be notified
when a subscription is stored and ready for broadcasts, then a client should ask for a
receipt if the server supports it (simple broker does not). For example with the Java
<a href="#websocket-stomp-client">STOMP Client</a>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Autowired</span>
<span class="directive">private</span> TaskScheduler messageBrokerTaskScheduler;

<span class="comment">// During initialization..</span>
stompClient.setTaskScheduler(<span class="local-variable">this</span>.messageBrokerTaskScheduler);

<span class="comment">// When subscribing..</span>
StompHeaders headers = <span class="keyword">new</span> StompHeaders();
headers.setDestination(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic/...</span><span class="delimiter">&quot;</span></span>);
headers.setReceipt(<span class="string"><span class="delimiter">&quot;</span><span class="content">r1</span><span class="delimiter">&quot;</span></span>);
FrameHandler handler = ...;
stompSession.subscribe(headers, handler).addReceiptTask(() -&gt; {
    <span class="comment">// Subscription ready...</span>
});</code></pre>
</div>
</div>
<div class="paragraph">
<p>A server side option is <a href="#websocket-stomp-interceptors">to register</a> an
<code>ExecutorChannelInterceptor</code> on the <code>brokerChannel</code> and implement the <code>afterMessageHandled</code>
method that is invoked after messages, including subscriptions, have been handled.</p>
</div>
</div>
<div class="sect4">
<h5 id="websocket-stomp-exception-handler"><a class="anchor" href="#websocket-stomp-exception-handler"></a><code>@MessageExceptionHandler</code></h5>
<div class="paragraph">
<p>An application can use <code>@MessageExceptionHandler</code> methods to handle exceptions from
<code>@MessageMapping</code> methods. Exceptions of interest can be declared in the annotation
itself, or through a method argument if you want to get access to the exception instance:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyController</span> {

    <span class="comment">// ...</span>

    <span class="annotation">@MessageExceptionHandler</span>
    <span class="directive">public</span> ApplicationError handleException(MyException exception) {
        <span class="comment">// ...</span>
        <span class="keyword">return</span> appError;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p><code>@MessageExceptionHandler</code> methods support flexible method signatures and support the same
method argument types and return values as <a href="#websocket-stomp-message-mapping"><code>@MessageMapping</code></a> methods.</p>
</div>
<div class="paragraph">
<p>Typically <code>@MessageExceptionHandler</code> methods apply within the <code>@Controller</code> class (or
class hierarchy) they are declared in. If you want such methods to apply more globally,
across controllers, you can declare them in a class marked with <code>@ControllerAdvice</code>.
This is comparable to <a href="#mvc-ann-controller-advice">similar support</a> in Spring MVC.</p>
</div>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-handle-send"><a class="anchor" href="#websocket-stomp-handle-send"></a>4.4.6. Send Messages</h4>
<div class="paragraph">
<p>What if you want to send messages to connected clients from any part of the
application? Any application component can send messages to the <code>"brokerChannel"</code>.
The easiest way to do that is to have a <code>SimpMessagingTemplate</code> injected, and
use it to send messages. Typically it should be easy to have it injected by
type, for example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">GreetingController</span> {

    <span class="directive">private</span> SimpMessagingTemplate template;

    <span class="annotation">@Autowired</span>
    <span class="directive">public</span> GreetingController(SimpMessagingTemplate template) {
        <span class="local-variable">this</span>.template = template;
    }

    <span class="annotation">@RequestMapping</span>(path=<span class="string"><span class="delimiter">&quot;</span><span class="content">/greetings</span><span class="delimiter">&quot;</span></span>, method=POST)
    <span class="directive">public</span> <span class="type">void</span> greet(<span class="predefined-type">String</span> greeting) {
        <span class="predefined-type">String</span> text = <span class="string"><span class="delimiter">&quot;</span><span class="content">[</span><span class="delimiter">&quot;</span></span> + getTimestamp() + <span class="string"><span class="delimiter">&quot;</span><span class="content">]:</span><span class="delimiter">&quot;</span></span> + greeting;
        <span class="local-variable">this</span>.template.convertAndSend(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic/greetings</span><span class="delimiter">&quot;</span></span>, text);
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>But it can also be qualified by its name "brokerMessagingTemplate" if another
bean of the same type exists.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-handle-simple-broker"><a class="anchor" href="#websocket-stomp-handle-simple-broker"></a>4.4.7. Simple Broker</h4>
<div class="paragraph">
<p>The built-in, simple message broker handles subscription requests from clients,
stores them in memory, and broadcasts messages to connected clients with matching
destinations. The broker supports path-like destinations, including subscriptions
to Ant-style destination patterns.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Applications can also use dot-separated destinations (vs slash).
See <a href="#websocket-stomp-destination-separator">Dot as Separator</a>.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-handle-broker-relay"><a class="anchor" href="#websocket-stomp-handle-broker-relay"></a>4.4.8. External Broker</h4>
<div class="paragraph">
<p>The simple broker is great for getting started but supports only a subset of
STOMP commands (e.g. no acks, receipts, etc.), relies on a simple message
sending loop, and is not suitable for clustering. As an alternative, applications
can upgrade to using a full-featured message broker.</p>
</div>
<div class="paragraph">
<p>Check the STOMP documentation for your message broker of choice (e.g.
<a href="https://www.rabbitmq.com/stomp.html">RabbitMQ</a>,
<a href="http://activemq.apache.org/stomp.html">ActiveMQ</a>, etc.), install the broker,
and run it with STOMP support enabled. Then enable the STOMP broker relay in the
Spring configuration instead of the simple broker.</p>
</div>
<div class="paragraph">
<p>Below is example configuration that enables a full-featured broker:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span>).withSockJS();
    }

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">/queue</span><span class="delimiter">&quot;</span></span>);
        registry.setApplicationDestinationPrefixes(<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span>);
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>XML configuration equivalent:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker</span> <span class="attribute-name">application-destination-prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;websocket:stomp-endpoint</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/portfolio</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
            <span class="tag">&lt;websocket:sockjs</span><span class="tag">/&gt;</span>
        <span class="tag">&lt;/websocket:stomp-endpoint&gt;</span>
        <span class="tag">&lt;websocket:stomp-broker-relay</span> <span class="attribute-name">prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic,/queue</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The "STOMP broker relay" in the above configuration is a Spring
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/messaging/MessageHandler.html">MessageHandler</a>
that handles messages by forwarding them to an external message broker.
To do so it establishes TCP connections to the broker, forwards all
messages to it, and then forwards all messages received
from the broker to clients through their WebSocket sessions. Essentially
it acts as a "relay" that forwards messages in both directions.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Please add <code>io.projectreactor.ipc:reactor-netty</code> and <code>io.netty:netty-all</code>
dependencies to your project for TCP connection management.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>Furthermore, application components (e.g. HTTP request handling methods,
business services, etc.) can also send messages to the broker relay, as described
in <a href="#websocket-stomp-handle-send">Send Messages</a>, in order to broadcast messages to
subscribed WebSocket clients.</p>
</div>
<div class="paragraph">
<p>In effect, the broker relay enables robust and scalable message broadcasting.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-handle-broker-relay-configure"><a class="anchor" href="#websocket-stomp-handle-broker-relay-configure"></a>4.4.9. Connect to Broker</h4>
<div class="paragraph">
<p>A STOMP broker relay maintains a single "system" TCP connection to the broker.
This connection is used for messages originating from the server-side application
only, not for receiving messages. You can configure the STOMP credentials
for this connection, i.e. the STOMP frame <code>login</code> and <code>passcode</code> headers. This
is exposed in both the XML namespace and the Java config as the
<code>systemLogin</code>/<code>systemPasscode</code> properties with default values <code>guest</code>/<code>guest</code>.</p>
</div>
<div class="paragraph">
<p>The STOMP broker relay also creates a separate TCP connection for every connected
WebSocket client. You can configure the STOMP credentials to use for all TCP
connections created on behalf of clients. This is exposed in both the XML namespace
and the Java config as the <code>clientLogin</code>/<code>clientPasscode</code> properties with default
values <code>guest</code>/<code>guest</code>.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The STOMP broker relay always sets the <code>login</code> and <code>passcode</code> headers on every <code>CONNECT</code>
frame that it forwards to the broker on behalf of clients. Therefore WebSocket clients
need not set those headers; they will be ignored. As the <a href="#websocket-stomp-authentication">Authentication</a>
section explains, instead WebSocket clients should rely on HTTP authentication to protect
the WebSocket endpoint and establish the client identity.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The STOMP broker relay also sends and receives heartbeats to and from the message
broker over the "system" TCP connection. You can configure the intervals for sending
and receiving heartbeats (10 seconds each by default). If connectivity to the broker
is lost, the broker relay will continue to try to reconnect, every 5 seconds,
until it succeeds.</p>
</div>
<div class="paragraph">
<p>Any Spring bean can implement <code>ApplicationListener&lt;BrokerAvailabilityEvent&gt;</code> in order
to receive notifications when the "system" connection to the broker is lost and
re-established. For example a Stock Quote service broadcasting stock quotes can
stop trying to send messages when there is no active "system" connection.</p>
</div>
<div class="paragraph">
<p>By default, the STOMP broker relay always connects, and reconnects as needed if
connectivity is lost, to the same host and port. If you wish to supply multiple addresses,
on each attempt to connect, you can configure a supplier of addresses, instead of a
fixed host and port. For example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">extends</span> AbstractWebSocketMessageBrokerConfigurer {

    <span class="comment">// ...</span>

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay(<span class="string"><span class="delimiter">&quot;</span><span class="content">/queue/</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">/topic/</span><span class="delimiter">&quot;</span></span>).setTcpClient(createTcpClient());
        registry.setApplicationDestinationPrefixes(<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span>);
    }

    <span class="directive">private</span> ReactorNettyTcpClient&lt;<span class="type">byte</span><span class="type">[]</span>&gt; createTcpClient() {

        Consumer&lt;ClientOptions.Builder&lt;?&gt;&gt; builderConsumer = builder -&gt; {
            builder.connectAddress(()-&gt; {
                <span class="comment">// Select address to connect to ...</span>
            });
        };

        <span class="keyword">return</span> <span class="keyword">new</span> ReactorNettyTcpClient&lt;&gt;(builderConsumer, <span class="keyword">new</span> StompReactorNettyCodec());
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The STOMP broker relay can also be configured with a <code>virtualHost</code> property.
The value of this property will be set as the <code>host</code> header of every <code>CONNECT</code> frame
and may be useful for example in a cloud environment where the actual host to which
the TCP connection is established is different from the host providing the
cloud-based STOMP service.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-destination-separator"><a class="anchor" href="#websocket-stomp-destination-separator"></a>4.4.10. Dot as Separator</h4>
<div class="paragraph">
<p>When messages are routed to <code>@MessageMapping</code> methods, they&#8217;re matched with
<code>AntPathMatcher</code> and by default patterns are expected to use slash "/" as separator.
This is a good convention in a web applications and similar to HTTP URLs. However if
you are more used to messaging conventions, you can switch to using dot "." as separator.</p>
</div>
<div class="paragraph">
<p>In Java config:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="comment">// ...</span>

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPathMatcher(<strong><span class="keyword">new</span> AntPathMatcher(<span class="string"><span class="delimiter">&quot;</span><span class="content">.</span><span class="delimiter">&quot;</span></span>));</strong>
        registry.enableStompBrokerRelay(<span class="string"><span class="delimiter">&quot;</span><span class="content">/queue</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">/topic</span><span class="delimiter">&quot;</span></span>);
        registry.setApplicationDestinationPrefixes(<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span>);
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>In XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
        <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
                <span class="content">http://www.springframework.org/schema/beans</span>
                <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
                <span class="content">http://www.springframework.org/schema/websocket</span>
                <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker</span> <span class="attribute-name">application-destination-prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/app</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">path-matcher</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content"><strong>pathMatcher</strong></span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;websocket:stomp-endpoint</span> <span class="attribute-name">path</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/stomp</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
        <span class="tag">&lt;websocket:stomp-broker-relay</span> <span class="attribute-name">prefix</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic,/queue</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

    <strong>
    <span class="tag">&lt;bean</span> <span class="attribute-name">id</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">pathMatcher</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">class</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">org.springframework.util.AntPathMatcher</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="tag">&lt;constructor-arg</span> <span class="attribute-name">index</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">0</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">value</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">.</span><span class="delimiter">&quot;</span></span><span class="tag">/&gt;</span>
    <span class="tag">&lt;/bean&gt;</span>
    </strong>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>After that a controller may use dot "." as separator in <code>@MessageMapping</code> methods:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">foo</span><span class="delimiter">&quot;</span></span>)
<span class="directive">public</span> <span class="type">class</span> <span class="class">FooController</span> {

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">bar.{baz}</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="type">void</span> handleBaz(<span class="annotation">@DestinationVariable</span> <span class="predefined-type">String</span> baz) {
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The client can now send a message to <code>"/app/foo.bar.baz123"</code>.</p>
</div>
<div class="paragraph">
<p>In the example above we did not change the prefixes on the "broker relay" because those
depend entirely on the external message broker. Check the STOMP documentation pages of
the broker you&#8217;re using to see what conventions it supports for the destination header.</p>
</div>
<div class="paragraph">
<p>The "simple broker" on the other hand does rely on the configured <code>PathMatcher</code> so if
you switch the separator that will also apply to the broker and the way matches
destinations from a message to patterns in subscriptions.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-authentication"><a class="anchor" href="#websocket-stomp-authentication"></a>4.4.11. Authentication</h4>
<div class="paragraph">
<p>Every STOMP over WebSocket messaging session begins with an HTTP request&#8201;&#8212;&#8201;that can be a request to upgrade to WebSockets (i.e. a WebSocket handshake)
or in the case of SockJS fallbacks a series of SockJS HTTP transport requests.</p>
</div>
<div class="paragraph">
<p>Web applications already have authentication and authorization in place to
secure HTTP requests. Typically a user is authenticated via Spring Security
using some mechanism such as a login page, HTTP basic authentication, or other.
The security context for the authenticated user is saved in the HTTP session
and is associated with subsequent requests in the same cookie-based session.</p>
</div>
<div class="paragraph">
<p>Therefore for a WebSocket handshake, or for SockJS HTTP transport requests,
typically there will already be an authenticated user accessible via
<code>HttpServletRequest#getUserPrincipal()</code>. Spring automatically associates that user
with a WebSocket or SockJS session created for them and subsequently with all
STOMP messages transported over that session through a user header.</p>
</div>
<div class="paragraph">
<p>In short there is nothing special a typical web application needs to do above
and beyond what it already does for security. The user is authenticated at
the HTTP request level with a security context maintained through a cookie-based
HTTP session which is then associated with WebSocket or SockJS sessions created
for that user and results in a user header stamped on every <code>Message</code> flowing
through the application.</p>
</div>
<div class="paragraph">
<p>Note that the STOMP protocol does have a "login" and "passcode" headers
on the <code>CONNECT</code> frame. Those were originally designed for and are still needed
for example for STOMP over TCP. However for STOMP over WebSocket by default
Spring ignores authorization headers at the STOMP protocol level and assumes
the user is already authenticated at the HTTP transport level and expects that
the WebSocket or SockJS session contain the authenticated user.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring Security provides
<a href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#websocket">WebSocket sub-protocol authorization</a>
that uses a <code>ChannelInterceptor</code> to authorize messages based on the user header in them.
Also Spring Session provides a
<a href="https://docs.spring.io/spring-session/docs/current/reference/html5/#websocket">WebSocket integration</a>
that ensures the user HTTP session does not expire when the WebSocket session is still active.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-authentication-token-based"><a class="anchor" href="#websocket-stomp-authentication-token-based"></a>4.4.12. Token Authentication</h4>
<div class="paragraph">
<p><a href="https://github.com/spring-projects/spring-security-oauth">Spring Security OAuth</a>
provides support for token based security including JSON Web Token (JWT).
This can be used as the authentication mechanism in Web applications
including STOMP over WebSocket interactions just as described in the previous
section, i.e. maintaining identity through a cookie-based session.</p>
</div>
<div class="paragraph">
<p>At the same time cookie-based sessions are not always the best fit for example
in applications that don&#8217;t wish to maintain a server-side session at all or in
mobile applications where it&#8217;s common to use headers for authentication.</p>
</div>
<div class="paragraph">
<p>The <a href="https://tools.ietf.org/html/rfc6455#section-10.5">WebSocket protocol RFC 6455</a>
"doesn&#8217;t prescribe any particular way that servers can authenticate clients during
the WebSocket handshake." In practice however browser clients can only use standard
authentication headers (i.e. basic HTTP authentication) or cookies and cannot for example
provide custom headers. Likewise the SockJS JavaScript client does not provide
a way to send HTTP headers with SockJS transport requests, see
<a href="https://github.com/sockjs/sockjs-client/issues/196">sockjs-client issue 196</a>.
Instead it does allow sending query parameters that can be used to send a token
but that has its own drawbacks, for example as the token may be inadvertently
logged with the URL in server logs.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>The above limitations are for browser-based clients and do not apply to the
Spring Java-based STOMP client which does support sending headers with both
WebSocket and SockJS requests.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>Therefore applications that wish to avoid the use of cookies may not have any good
alternatives for authentication at the HTTP protocol level. Instead of using cookies
they may prefer to authenticate with headers at the STOMP messaging protocol level
There are 2 simple steps to doing that:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p>Use the STOMP client to pass authentication header(s) at connect time.</p>
</li>
<li>
<p>Process the authentication header(s) with a <code>ChannelInterceptor</code>.</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>Below is the example server-side configuration to register a custom authentication
interceptor. Note that an interceptor only needs to authenticate and set
the user header on the CONNECT <code>Message</code>. Spring will note and save the authenticated
user and associate it with subsequent STOMP messages on the same session:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(<span class="keyword">new</span> ChannelInterceptorAdapter() {
            <span class="annotation">@Override</span>
            <span class="directive">public</span> Message&lt;?&gt; preSend(Message&lt;?&gt; message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                <span class="keyword">if</span> (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Authentication user = ... ; <span class="comment">// access authentication header(s)</span>
                    accessor.setUser(user);
                }
                <span class="keyword">return</span> message;
            }
        });
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Also note that when using Spring Security&#8217;s authorization for messages, at present
you will need to ensure that the authentication <code>ChannelInterceptor</code> config is ordered
ahead of Spring Security&#8217;s. This is best done by declaring the custom interceptor in
its own implementation of <code>WebSocketMessageBrokerConfigurer</code> marked with
<code>@Order(Ordered.HIGHEST_PRECEDENCE + 99)</code>.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-user-destination"><a class="anchor" href="#websocket-stomp-user-destination"></a>4.4.13. User Destinations</h4>
<div class="paragraph">
<p>An application can send messages targeting a specific user, and Spring&#8217;s STOMP support
recognizes destinations prefixed with <code>"/user/"</code> for this purpose.
For example, a client might subscribe to the destination <code>"/user/queue/position-updates"</code>.
This destination will be handled by the <code>UserDestinationMessageHandler</code> and
transformed into a destination unique to the user session,
e.g. <code>"/queue/position-updates-user123"</code>. This provides the convenience of subscribing
to a generically named destination while at the same time ensuring no collisions
with other users subscribing to the same destination so that each user can receive
unique stock position updates.</p>
</div>
<div class="paragraph">
<p>On the sending side messages can be sent to a destination such as
<code>"/user/{username}/queue/position-updates"</code>, which in turn will be translated
by the <code>UserDestinationMessageHandler</code> into one or more destinations, one for each
session associated with the user. This allows any component within the application to
send messages targeting a specific user without necessarily knowing anything more
than their name and the generic destination. This is also supported through an
annotation as well as a messaging template.</p>
</div>
<div class="paragraph">
<p>For example, a message-handling method can send messages to the user associated with
the message being handled through the <code>@SendToUser</code> annotation (also supported on
the class-level to share a common destination):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">PortfolioController</span> {

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/trade</span><span class="delimiter">&quot;</span></span>)
    <span class="annotation">@SendToUser</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/queue/position-updates</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> TradeResult executeTrade(Trade trade, <span class="predefined-type">Principal</span> principal) {
        <span class="comment">// ...</span>
        <span class="keyword">return</span> tradeResult;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>If the user has more than one session, by default all of the sessions subscribed
to the given destination are targeted. However sometimes, it may be necessary to
target only the session that sent the message being handled. This can be done by
setting the <code>broadcast</code> attribute to false, for example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyController</span> {

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/action</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="type">void</span> handleAction() <span class="directive">throws</span> <span class="exception">Exception</span>{
        <span class="comment">// raise MyBusinessException here</span>
    }

    <span class="annotation">@MessageExceptionHandler</span>
    <span class="annotation">@SendToUser</span>(destinations=<span class="string"><span class="delimiter">&quot;</span><span class="content">/queue/errors</span><span class="delimiter">&quot;</span></span>, broadcast=<span class="predefined-constant">false</span>)
    <span class="directive">public</span> ApplicationError handleException(MyBusinessException exception) {
        <span class="comment">// ...</span>
        <span class="keyword">return</span> appError;
    }
}</code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>While user destinations generally imply an authenticated user, it isn&#8217;t required
strictly. A WebSocket session that is not associated with an authenticated user
can subscribe to a user destination. In such cases the <code>@SendToUser</code> annotation
will behave exactly the same as with <code>broadcast=false</code>, i.e. targeting only the
session that sent the message being handled.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>It is also possible to send a message to user destinations from any application
component by injecting the <code>SimpMessagingTemplate</code> created by the Java config or
XML namespace, for example (the bean name is <code>"brokerMessagingTemplate"</code> if required
for qualification with <code>@Qualifier</code>):</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Service</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">TradeServiceImpl</span> <span class="directive">implements</span> TradeService {

    <span class="directive">private</span> <span class="directive">final</span> SimpMessagingTemplate messagingTemplate;

    <span class="annotation">@Autowired</span>
    <span class="directive">public</span> TradeServiceImpl(SimpMessagingTemplate messagingTemplate) {
        <span class="local-variable">this</span>.messagingTemplate = messagingTemplate;
    }

    <span class="comment">// ...</span>

    <span class="directive">public</span> <span class="type">void</span> afterTradeExecuted(Trade trade) {
        <span class="local-variable">this</span>.messagingTemplate.convertAndSendToUser(
                trade.getUserName(), <span class="string"><span class="delimiter">&quot;</span><span class="content">/queue/position-updates</span><span class="delimiter">&quot;</span></span>, trade.getResult());
    }
}</code></pre>
</div>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When using user destinations with an external message broker, check the broker
documentation on how to manage inactive queues, so that when the user session is
over, all unique user queues are removed. For example, RabbitMQ creates auto-delete
queues when destinations like <code>/exchange/amq.direct/position-updates</code> are used.
So in that case the client could subscribe to <code>/user/exchange/amq.direct/position-updates</code>.
Similarly, ActiveMQ has
<a href="http://activemq.apache.org/delete-inactive-destinations.html">configuration options</a>
for purging inactive destinations.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>In a multi-application server scenario a user destination may remain unresolved because
the user is connected to a different server. In such cases you can configure a
destination to broadcast unresolved messages to so that other servers have a chance to try.
This can be done through the <code>userDestinationBroadcast</code> property of the
<code>MessageBrokerRegistry</code> in Java config and the <code>user-destination-broadcast</code> attribute
of the <code>message-broker</code> element in XML.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-ordered-messages"><a class="anchor" href="#websocket-stomp-ordered-messages"></a>4.4.14. Order of Messages</h4>
<div class="paragraph">
<p>Messages from the broker are published to the "clientOutboundChannel" from where they are
written to WebSocket sessions. As the channel is backed by a <code>ThreadPoolExecutor</code> messages
are processed in different threads, and the resulting sequence received by the client may
not match the exact order of publication.</p>
</div>
<div class="paragraph">
<p>If this is an issue, enable the following flag:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">protected</span> <span class="type">void</span> configureMessageBroker(MessageBrokerRegistry registry) {
        <span class="comment">// ...</span>
        registry.setPreservePublishOrder(<span class="predefined-constant">true</span>);
    }

}</code></pre>
</div>
</div>
<div class="paragraph">
<p>The same in XML:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker</span> <span class="attribute-name">preserve-publish-order</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">true</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>
        <span class="comment">&lt;!-- ... --&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>When the flag is set, messages within the same client session are published to the
"clientOutboundChannel" one at a time, so that the order of publication is guaranteed.
Note that this incurs a small performance overhead, so enable it only if required.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-appplication-context-events"><a class="anchor" href="#websocket-stomp-appplication-context-events"></a>4.4.15. Events</h4>
<div class="paragraph">
<p>Several <code>ApplicationContext</code> events (listed below) are published and can be
received by implementing Spring&#8217;s <code>ApplicationListener</code> interface.</p>
</div>
<div class="ulist">
<ul>
<li>
<p><code>BrokerAvailabilityEvent</code>&#8201;&#8212;&#8201;indicates when the broker becomes available/unavailable.
While the "simple" broker becomes available immediately on startup and remains so while
the application is running, the STOMP "broker relay" may lose its connection
to the full featured broker, for example if the broker is restarted. The broker relay
has reconnect logic and will re-establish the "system" connection to the broker
when it comes back, hence this event is published whenever the state changes from connected
to disconnected and vice versa. Components using the <code>SimpMessagingTemplate</code> should
subscribe to this event and avoid sending messages at times when the broker is not
available. In any case they should be prepared to handle <code>MessageDeliveryException</code>
when sending a message.</p>
</li>
<li>
<p><code>SessionConnectEvent</code>&#8201;&#8212;&#8201;published when a new STOMP CONNECT is received
indicating the start of a new client session. The event contains the message representing the
connect including the session id, user information (if any), and any custom headers the client
may have sent. This is useful for tracking client sessions. Components subscribed
to this event can wrap the contained message using <code>SimpMessageHeaderAccessor</code> or
<code>StompMessageHeaderAccessor</code>.</p>
</li>
<li>
<p><code>SessionConnectedEvent</code>&#8201;&#8212;&#8201;published shortly after a <code>SessionConnectEvent</code> when the
broker has sent a STOMP CONNECTED frame in response to the CONNECT. At this point the
STOMP session can be considered fully established.</p>
</li>
<li>
<p><code>SessionSubscribeEvent</code>&#8201;&#8212;&#8201;published when a new STOMP SUBSCRIBE is received.</p>
</li>
<li>
<p><code>SessionUnsubscribeEvent</code>&#8201;&#8212;&#8201;published when a new STOMP UNSUBSCRIBE is received.</p>
</li>
<li>
<p><code>SessionDisconnectEvent</code>&#8201;&#8212;&#8201;published when a STOMP session ends. The DISCONNECT may
have been sent from the client, or it may also be automatically generated when the
WebSocket session is closed. In some cases this event may be published more than once
per session. Components should be idempotent with regard to multiple disconnect events.</p>
</li>
</ul>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When using a full-featured broker, the STOMP "broker relay" automatically reconnects the
"system" connection in case the broker becomes temporarily unavailable. Client connections
however are not automatically reconnected. Assuming heartbeats are enabled, the client
will typically notice the broker is not responding within 10 seconds. Clients need to
implement their own reconnect logic.</p>
</div>
</td>
</tr>
</table>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-interceptors"><a class="anchor" href="#websocket-stomp-interceptors"></a>4.4.16. Interception</h4>
<div class="paragraph">
<p><a href="#websocket-stomp-appplication-context-events">Events</a> provide notifications for the lifecycle
of a STOMP connection and not for every client message. Applications can also register a
<code>ChannelInterceptor</code> to intercept any message, and in any part of the processing chain.
For example to intercept inbound messages from clients:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(<span class="keyword">new</span> MyChannelInterceptor());
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>A custom <code>ChannelInterceptor</code> can use <code>StompHeaderAccessor</code> or <code>SimpMessageHeaderAccessor</code>
to access information about the message.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">MyChannelInterceptor</span> <span class="directive">implements</span> ChannelInterceptor {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> Message&lt;?&gt; preSend(Message&lt;?&gt; message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getStompCommand();
        <span class="comment">// ...</span>
        <span class="keyword">return</span> message;
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Applications may also implement <code>ExecutorChannelInterceptor</code> which is a sub-interface
of <code>ChannelInterceptor</code> with callbacks in the thread in which the messages are handled.
While a <code>ChannelInterceptor</code> is invoked once for per message sent to a channel, the
<code>ExecutorChannelInterceptor</code> provides hooks in the thread of each <code>MessageHandler</code>
subscribed to messages from the channel.</p>
</div>
<div class="paragraph">
<p>Note that just like with the <code>SesionDisconnectEvent</code> above, a DISCONNECT message
may have been sent from the client, or it may also be automatically generated when
the WebSocket session is closed. In some cases an interceptor may intercept this
message more than once per session. Components should be idempotent with regard to
multiple disconnect events.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-client"><a class="anchor" href="#websocket-stomp-client"></a>4.4.17. STOMP Client</h4>
<div class="paragraph">
<p>Spring provides a STOMP over WebSocket client and a STOMP over TCP client.</p>
</div>
<div class="paragraph">
<p>To begin create and configure <code>WebSocketStompClient</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">WebSocketClient webSocketClient = <span class="keyword">new</span> StandardWebSocketClient();
WebSocketStompClient stompClient = <span class="keyword">new</span> WebSocketStompClient(webSocketClient);
stompClient.setMessageConverter(<span class="keyword">new</span> StringMessageConverter());
stompClient.setTaskScheduler(taskScheduler); <span class="comment">// for heartbeats</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>In the above example <code>StandardWebSocketClient</code> could be replaced with <code>SockJsClient</code>
since that is also an implementation of <code>WebSocketClient</code>. The <code>SockJsClient</code> can
use WebSocket or HTTP-based transport as a fallback. For more details see
<a href="#websocket-fallback-sockjs-client">SockJsClient</a>.</p>
</div>
<div class="paragraph">
<p>Next establish a connection and provide a handler for the STOMP session:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="predefined-type">String</span> url = <span class="string"><span class="delimiter">&quot;</span><span class="content">ws://127.0.0.1:8080/endpoint</span><span class="delimiter">&quot;</span></span>;
StompSessionHandler sessionHandler = <span class="keyword">new</span> MyStompSessionHandler();
stompClient.connect(url, sessionHandler);</code></pre>
</div>
</div>
<div class="paragraph">
<p>When the session is ready for use the handler is notified:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="directive">public</span> <span class="type">class</span> <span class="class">MyStompSessionHandler</span> <span class="directive">extends</span> StompSessionHandlerAdapter {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> afterConnected(StompSession session, StompHeaders connectedHeaders) {
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>Once the session is established any payload can be sent and that will be
serialized with the configured <code>MessageConverter</code>:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">session.send(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic/foo</span><span class="delimiter">&quot;</span></span>, <span class="string"><span class="delimiter">&quot;</span><span class="content">payload</span><span class="delimiter">&quot;</span></span>);</code></pre>
</div>
</div>
<div class="paragraph">
<p>You can also subscribe to destinations. The <code>subscribe</code> methods require a handler
for messages on the subscription and return a <code>Subscription</code> handle that can be
used to unsubscribe. For each received message the handler can specify the target
Object type the payload should be deserialized to:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">session.subscribe(<span class="string"><span class="delimiter">&quot;</span><span class="content">/topic/foo</span><span class="delimiter">&quot;</span></span>, <span class="keyword">new</span> StompFrameHandler() {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="predefined-type">Type</span> getPayloadType(StompHeaders headers) {
        <span class="keyword">return</span> <span class="predefined-type">String</span>.class;
    }

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> handleFrame(StompHeaders headers, <span class="predefined-type">Object</span> payload) {
        <span class="comment">// ...</span>
    }

});</code></pre>
</div>
</div>
<div class="paragraph">
<p>To enable STOMP heartbeat configure <code>WebSocketStompClient</code> with a <code>TaskScheduler</code>
and optionally customize the heartbeat intervals, 10 seconds for write inactivity
which causes a heartbeat to be sent and 10 seconds for read inactivity which
closes the connection.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>When using <code>WebSocketStompClient</code> for performance tests to simulate thousands
of clients from the same machine consider turning off heartbeats since each
connection schedules its own heartbeat tasks and that&#8217;s not optimized for a
a large number of clients running on the same machine.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The STOMP protocol also supports receipts where the client must add a "receipt"
header to which the server responds with a RECEIPT frame after the send or
subscribe are processed. To support this the <code>StompSession</code> offers
<code>setAutoReceipt(boolean)</code> that causes a "receipt" header to be
added on every subsequent send or subscribe.
Alternatively you can also manually add a "receipt" header to the <code>StompHeaders</code>.
Both send and subscribe return an instance of <code>Receiptable</code>
that can be used to register for receipt success and failure callbacks.
For this feature the client must be configured with a <code>TaskScheduler</code>
and the amount of time before a receipt expires (15 seconds by default).</p>
</div>
<div class="paragraph">
<p>Note that <code>StompSessionHandler</code> itself is a <code>StompFrameHandler</code> which allows
it to handle ERROR frames in addition to the <code>handleException</code> callback for
exceptions from the handling of messages, and <code>handleTransportError</code> for
transport-level errors including <code>ConnectionLostException</code>.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-websocket-scope"><a class="anchor" href="#websocket-stomp-websocket-scope"></a>4.4.18. WebSocket Scope</h4>
<div class="paragraph">
<p>Each WebSocket session has a map of attributes. The map is attached as a header to
inbound client messages and may be accessed from a controller method, for example:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyController</span> {

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/action</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="type">void</span> handle(SimpMessageHeaderAccessor headerAccessor) {
        <span class="predefined-type">Map</span>&lt;<span class="predefined-type">String</span>, <span class="predefined-type">Object</span>&gt; attrs = headerAccessor.getSessionAttributes();
        <span class="comment">// ...</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>It is also possible to declare a Spring-managed bean in the <code>websocket</code> scope.
WebSocket-scoped beans can be injected into controllers and any channel interceptors
registered on the "clientInboundChannel". Those are typically singletons and live
longer than any individual WebSocket session. Therefore you will need to use a
scope proxy mode for WebSocket-scoped beans:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Component</span>
<span class="annotation">@Scope</span>(scopeName = <span class="string"><span class="delimiter">&quot;</span><span class="content">websocket</span><span class="delimiter">&quot;</span></span>, proxyMode = ScopedProxyMode.TARGET_CLASS)
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyBean</span> {

    <span class="annotation">@PostConstruct</span>
    <span class="directive">public</span> <span class="type">void</span> init() {
        <span class="comment">// Invoked after dependencies injected</span>
    }

    <span class="comment">// ...</span>

    <span class="annotation">@PreDestroy</span>
    <span class="directive">public</span> <span class="type">void</span> destroy() {
        <span class="comment">// Invoked when the WebSocket session ends</span>
    }
}

<span class="annotation">@Controller</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">MyController</span> {

    <span class="directive">private</span> <span class="directive">final</span> MyBean myBean;

    <span class="annotation">@Autowired</span>
    <span class="directive">public</span> MyController(MyBean myBean) {
        <span class="local-variable">this</span>.myBean = myBean;
    }

    <span class="annotation">@MessageMapping</span>(<span class="string"><span class="delimiter">&quot;</span><span class="content">/action</span><span class="delimiter">&quot;</span></span>)
    <span class="directive">public</span> <span class="type">void</span> handle() {
        <span class="comment">// this.myBean from the current WebSocket session</span>
    }
}</code></pre>
</div>
</div>
<div class="paragraph">
<p>As with any custom scope, Spring initializes a new <code>MyBean</code> instance the first
time it is accessed from the controller and stores the instance in the WebSocket
session attributes. The same instance is returned subsequently until the session
ends. WebSocket-scoped beans will have all Spring lifecycle methods invoked as
shown in the examples above.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-configuration-performance"><a class="anchor" href="#websocket-stomp-configuration-performance"></a>4.4.19. Performance</h4>
<div class="paragraph">
<p>There is no silver bullet when it comes to performance. Many factors may
affect it including the size of messages, the volume, whether application
methods perform work that requires blocking, as well as external factors
such as network speed and others. The goal of this section is to provide
an overview of the available configuration options along with some thoughts
on how to reason about scaling.</p>
</div>
<div class="paragraph">
<p>In a messaging application messages are passed through channels for asynchronous
executions backed by thread pools. Configuring such an application requires
good knowledge of the channels and the flow of messages. Therefore it is
recommended to review <a href="#websocket-stomp-message-flow">Flow of Messages</a>.</p>
</div>
<div class="paragraph">
<p>The obvious place to start is to configure the thread pools backing the
<code>"clientInboundChannel"</code> and the <code>"clientOutboundChannel"</code>. By default both
are configured at twice the number of available processors.</p>
</div>
<div class="paragraph">
<p>If the handling of messages in annotated methods is mainly CPU bound then the
number of threads for the <code>"clientInboundChannel"</code> should remain close to the
number of processors. If the work they do is more IO bound and requires blocking
or waiting on a database or other external system then the thread pool size
will need to be increased.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p><code>ThreadPoolExecutor</code> has 3 important properties. Those are the core and
the max thread pool size as well as the capacity for the queue to store
tasks for which there are no available threads.</p>
</div>
<div class="paragraph">
<p>A common point of confusion is that configuring the core pool size (e.g. 10)
and max pool size (e.g. 20) results in a thread pool with 10 to 20 threads.
In fact if the capacity is left at its default value of Integer.MAX_VALUE
then the thread pool will never increase beyond the core pool size since
all additional tasks will be queued.</p>
</div>
<div class="paragraph">
<p>Please review the Javadoc of <code>ThreadPoolExecutor</code> to learn how these
properties work and understand the various queuing strategies.</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>On the <code>"clientOutboundChannel"</code> side it is all about sending messages to WebSocket
clients. If clients are on a fast network then the number of threads should
remain close to the number of available processors. If they are slow or on
low bandwidth they will take longer to consume messages and put a burden on the
thread pool. Therefore increasing the thread pool size will be necessary.</p>
</div>
<div class="paragraph">
<p>While the workload for the "clientInboundChannel" is possible to predict&#8201;&#8212;&#8201;after all it is based on what the application does&#8201;&#8212;&#8201;how to configure the
"clientOutboundChannel" is harder as it is based on factors beyond
the control of the application. For this reason there are two additional
properties related to the sending of messages. Those are the <code>"sendTimeLimit"</code>
and the <code>"sendBufferSizeLimit"</code>. Those are used to configure how long a
send is allowed to take and how much data can be buffered when sending
messages to a client.</p>
</div>
<div class="paragraph">
<p>The general idea is that at any given time only a single thread may be used
to send to a client. All additional messages meanwhile get buffered and you
can use these properties to decide how long sending a message is allowed to
take and how much data can be buffered in the mean time. Please review the
Javadoc and documentation of the XML schema for this configuration for
important additional details.</p>
</div>
<div class="paragraph">
<p>Here is example configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(<span class="integer">15</span> * <span class="integer">1000</span>).setSendBufferSizeLimit(<span class="integer">512</span> * <span class="integer">1024</span>);
    }

    <span class="comment">// ...</span>

}</code></pre>
</div>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker&gt;</span>
        <span class="tag">&lt;websocket:transport</span> <span class="attribute-name">send-timeout</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">15000</span><span class="delimiter">&quot;</span></span> <span class="attribute-name">send-buffer-size</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">524288</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
        <span class="comment">&lt;!-- ... --&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>The WebSocket transport configuration shown above can also be used to configure the
maximum allowed size for incoming STOMP messages. Although in theory a WebSocket
message can be almost unlimited in size, in practice WebSocket servers impose
limits&#8201;&#8212;&#8201;for example, 8K on Tomcat and 64K on Jetty. For this reason STOMP clients
such as the JavaScript <a href="https://github.com/JSteunou/webstomp-client">webstomp-client</a>
and others split larger STOMP messages at 16K boundaries and send them as multiple
WebSocket messages thus requiring the server to buffer and re-assemble.</p>
</div>
<div class="paragraph">
<p>Spring&#8217;s STOMP over WebSocket support does this so applications can configure the
maximum size for STOMP messages irrespective of WebSocket server specific message
sizes. Do keep in mind that the WebSocket message size will be automatically
adjusted if necessary to ensure they can carry 16K WebSocket messages at a
minimum.</p>
</div>
<div class="paragraph">
<p>Here is example configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java"><span class="annotation">@Configuration</span>
<span class="annotation">@EnableWebSocketMessageBroker</span>
<span class="directive">public</span> <span class="type">class</span> <span class="class">WebSocketConfig</span> <span class="directive">implements</span> WebSocketMessageBrokerConfigurer {

    <span class="annotation">@Override</span>
    <span class="directive">public</span> <span class="type">void</span> configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(<span class="integer">128</span> * <span class="integer">1024</span>);
    }

    <span class="comment">// ...</span>

}</code></pre>
</div>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;beans</span> <span class="attribute-name">xmlns</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/beans</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:xsi</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.w3.org/2001/XMLSchema-instance</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xmlns:websocket</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">http://www.springframework.org/schema/websocket</span><span class="delimiter">&quot;</span></span>
    <span class="attribute-name">xsi:schemaLocation</span>=<span class="string"><span class="delimiter">&quot;</span>
        <span class="content">http://www.springframework.org/schema/beans</span>
        <span class="content">http://www.springframework.org/schema/beans/spring-beans.xsd</span>
        <span class="content">http://www.springframework.org/schema/websocket</span>
        <span class="content">http://www.springframework.org/schema/websocket/spring-websocket.xsd</span><span class="delimiter">&quot;</span></span><span class="tag">&gt;</span>

    <span class="tag">&lt;websocket:message-broker&gt;</span>
        <span class="tag">&lt;websocket:transport</span> <span class="attribute-name">message-size</span>=<span class="string"><span class="delimiter">&quot;</span><span class="content">131072</span><span class="delimiter">&quot;</span></span> <span class="tag">/&gt;</span>
        <span class="comment">&lt;!-- ... --&gt;</span>
    <span class="tag">&lt;/websocket:message-broker&gt;</span>

<span class="tag">&lt;/beans&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>An important point about scaling is using multiple application instances.
Currently it is not possible to do that with the simple broker.
However when using a full-featured broker such as RabbitMQ, each application
instance connects to the broker and messages broadcast from one application
instance can be broadcast through the broker to WebSocket clients connected
through any other application instances.</p>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-stats"><a class="anchor" href="#websocket-stomp-stats"></a>4.4.20. Monitoring</h4>
<div class="paragraph">
<p>When using <code>@EnableWebSocketMessageBroker</code> or <code>&lt;websocket:message-broker&gt;</code> key
infrastructure components automatically gather stats and counters that provide
important insight into the internal state of the application. The configuration
also declares a bean of type <code>WebSocketMessageBrokerStats</code> that gathers all
available information in one place and by default logs it at <code>INFO</code> level once
every 30 minutes. This bean can be exported to JMX through Spring&#8217;s
<code>MBeanExporter</code> for viewing at runtime, for example through JDK&#8217;s <code>jconsole</code>.
Below is a summary of the available information.</p>
</div>
<div class="dlist">
<dl>
<dt class="hdlist1">Client WebSocket Sessions</dt>
<dd>
<div class="dlist">
<dl>
<dt class="hdlist1">Current</dt>
<dd>
<p>indicates how many client sessions there are
currently with the count further broken down by WebSocket vs HTTP
streaming and polling SockJS sessions.</p>
</dd>
<dt class="hdlist1">Total</dt>
<dd>
<p>indicates how many total sessions have been established.</p>
</dd>
<dt class="hdlist1">Abnormally Closed</dt>
<dd>
<div class="dlist">
<dl>
<dt class="hdlist1">Connect Failures</dt>
<dd>
<p>these are sessions that got established but were
closed after not having received any messages within 60 seconds. This is
usually an indication of proxy or network issues.</p>
</dd>
<dt class="hdlist1">Send Limit Exceeded</dt>
<dd>
<p>sessions closed after exceeding the configured send
timeout or the send buffer limits which can occur with slow clients
(see previous section).</p>
</dd>
<dt class="hdlist1">Transport Errors</dt>
<dd>
<p>sessions closed after a transport error such as
failure to read or write to a WebSocket connection or
HTTP request/response.</p>
</dd>
</dl>
</div>
</dd>
<dt class="hdlist1">STOMP Frames</dt>
<dd>
<p>the total number of CONNECT, CONNECTED, and DISCONNECT frames
processed indicating how many clients connected on the STOMP level. Note that
the DISCONNECT count may be lower when sessions get closed abnormally or when
clients close without sending a DISCONNECT frame.</p>
</dd>
</dl>
</div>
</dd>
<dt class="hdlist1">STOMP Broker Relay</dt>
<dd>
<div class="dlist">
<dl>
<dt class="hdlist1">TCP Connections</dt>
<dd>
<p>indicates how many TCP connections on behalf of client
WebSocket sessions are established to the broker. This should be equal to the
number of client WebSocket sessions + 1 additional shared "system" connection
for sending messages from within the application.</p>
</dd>
<dt class="hdlist1">STOMP Frames</dt>
<dd>
<p>the total number of CONNECT, CONNECTED, and DISCONNECT frames
forwarded to or received from the broker on behalf of clients. Note that a
DISCONNECT frame is sent to the broker regardless of how the client WebSocket
session was closed. Therefore a lower DISCONNECT frame count is an indication
that the broker is pro-actively closing connections, may be because of a
heartbeat that didn&#8217;t arrive in time, an invalid input frame, or other.</p>
</dd>
</dl>
</div>
</dd>
<dt class="hdlist1">Client Inbound Channel</dt>
<dd>
<p>stats from thread pool backing the "clientInboundChannel"
providing insight into the health of incoming message processing. Tasks queueing
up here is an indication the application may be too slow to handle messages.
If there I/O bound tasks (e.g. slow database query, HTTP request to 3rd party
REST API, etc) consider increasing the thread pool size.</p>
</dd>
<dt class="hdlist1">Client Outbound Channel</dt>
<dd>
<p>stats from the thread pool backing the "clientOutboundChannel"
providing insight into the health of broadcasting messages to clients. Tasks
queueing up here is an indication clients are too slow to consume messages.
One way to address this is to increase the thread pool size to accommodate the
number of concurrent slow clients expected. Another option is to reduce the
send timeout and send buffer size limits (see the previous section).</p>
</dd>
<dt class="hdlist1">SockJS Task Scheduler</dt>
<dd>
<p>stats from thread pool of the SockJS task scheduler which
is used to send heartbeats. Note that when heartbeats are negotiated on the
STOMP level the SockJS heartbeats are disabled.</p>
</dd>
</dl>
</div>
</div>
<div class="sect3">
<h4 id="websocket-stomp-testing"><a class="anchor" href="#websocket-stomp-testing"></a>4.4.21. Testing</h4>
<div class="paragraph">
<p>There are two main approaches to testing applications using Spring&#8217;s STOMP over
WebSocket support. The first is to write server-side tests verifying the functionality
of controllers and their annotated message handling methods. The second is to write
full end-to-end tests that involve running a client and a server.</p>
</div>
<div class="paragraph">
<p>The two approaches are not mutually exclusive. On the contrary each has a place
in an overall test strategy. Server-side tests are more focused and easier to write
and maintain. End-to-end integration tests on the other hand are more complete and
test much more, but they&#8217;re also more involved to write and maintain.</p>
</div>
<div class="paragraph">
<p>The simplest form of server-side tests is to write controller unit tests. However
this is not useful enough since much of what a controller does depends on its
annotations. Pure unit tests simply can&#8217;t test that.</p>
</div>
<div class="paragraph">
<p>Ideally controllers under test should be invoked as they are at runtime, much like
the approach to testing controllers handling HTTP requests using the Spring MVC Test
framework. i.e. without running a Servlet container but relying on the Spring Framework
to invoke the annotated controllers. Just like with Spring MVC Test here there are two
two possible alternatives, either using a "context-based" or "standalone" setup:</p>
</div>
<div class="olist arabic">
<ol class="arabic">
<li>
<p>Load the actual Spring configuration with the help of the
Spring TestContext framework, inject "clientInboundChannel" as a test field, and
use it to send messages to be handled by controller methods.</p>
</li>
<li>
<p>Manually set up the minimum Spring framework infrastructure required to invoke
controllers (namely the <code>SimpAnnotationMethodMessageHandler</code>) and pass messages for
controllers directly to it.</p>
</li>
</ol>
</div>
<div class="paragraph">
<p>Both of these setup scenarios are demonstrated in the
<a href="https://github.com/rstoyanchev/spring-websocket-portfolio/tree/master/src/test/java/org/springframework/samples/portfolio/web">tests for the stock portfolio</a>
sample application.</p>
</div>
<div class="paragraph">
<p>The second approach is to create end-to-end integration tests. For that you will need
to run a WebSocket server in embedded mode and connect to it as a WebSocket client
sending WebSocket messages containing STOMP frames.
The <a href="https://github.com/rstoyanchev/spring-websocket-portfolio/tree/master/src/test/java/org/springframework/samples/portfolio/web">tests for the stock portfolio</a>
sample application also demonstrates this approach using Tomcat as the embedded
WebSocket server and a simple STOMP client for test purposes.</p>
</div>
</div>
</div>
</div>
</div>
<div class="sect1">
<h2 id="web-integration"><a class="anchor" href="#web-integration"></a>5. Other Web Frameworks</h2>
<div class="sectionbody">
<div class="sect2">
<h3 id="intro"><a class="anchor" href="#intro"></a>5.1. Introduction</h3>
<div class="paragraph">
<p>This chapter details Spring&#8217;s integration with third party web frameworks.</p>
</div>
<div class="paragraph">
<p>One of the core value propositions of the Spring Framework is that of enabling
<em>choice</em>. In a general sense, Spring does not force one to use or buy into any
particular architecture, technology, or methodology (although it certainly recommends
some over others). This freedom to pick and choose the architecture, technology, or
methodology that is most relevant to a developer and their development team is
arguably most evident in the web area, where Spring provides its own web framework
(<a href="#mvc">Spring MVC</a>), while at the same time providing integration with a number of
popular third party web frameworks.</p>
</div>
</div>
<div class="sect2">
<h3 id="web-integration-common"><a class="anchor" href="#web-integration-common"></a>5.2. Common config</h3>
<div class="paragraph">
<p>Before diving into the integration specifics of each supported web framework, let us
first take a look at the Spring configuration that is <em>not</em> specific to any one web
framework. (This section is equally applicable to Spring&#8217;s own web framework, Spring
MVC.)</p>
</div>
<div class="paragraph">
<p>One of the concepts (for want of a better word) espoused by (Spring&#8217;s) lightweight
application model is that of a layered architecture. Remember that in a 'classic'
layered architecture, the web layer is but one of many layers; it serves as one of the
entry points into a server side application and it delegates to service objects
(facades) defined in a service layer to satisfy business specific (and
presentation-technology agnostic) use cases. In Spring, these service objects, any other
business-specific objects, data access objects, etc. exist in a distinct 'business
context', which contains <em>no</em> web or presentation layer objects (presentation objects
such as Spring MVC controllers are typically configured in a distinct 'presentation
context'). This section details how one configures a Spring container (a
<code>WebApplicationContext</code>) that contains all of the 'business beans' in one&#8217;s application.</p>
</div>
<div class="paragraph">
<p>On to specifics: all that one need do is to declare a
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/context/ContextLoaderListener.html"><code>ContextLoaderListener</code></a>
in the standard Java EE servlet <code>web.xml</code> file of one&#8217;s web application, and add a
<code>contextConfigLocation</code>&lt;context-param/&gt; section (in the same file) that defines which
set of Spring XML configuration files to load.</p>
</div>
<div class="paragraph">
<p>Find below the &lt;listener/&gt; configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;listener&gt;</span>
    <span class="tag">&lt;listener-class&gt;</span>org.springframework.web.context.ContextLoaderListener<span class="tag">&lt;/listener-class&gt;</span>
<span class="tag">&lt;/listener&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>Find below the &lt;context-param/&gt; configuration:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;context-param&gt;</span>
    <span class="tag">&lt;param-name&gt;</span>contextConfigLocation<span class="tag">&lt;/param-name&gt;</span>
    <span class="tag">&lt;param-value&gt;</span>/WEB-INF/applicationContext*.xml<span class="tag">&lt;/param-value&gt;</span>
<span class="tag">&lt;/context-param&gt;</span></code></pre>
</div>
</div>
<div class="paragraph">
<p>If you don&#8217;t specify the <code>contextConfigLocation</code> context parameter, the
<code>ContextLoaderListener</code> will look for a file called <code>/WEB-INF/applicationContext.xml</code> to
load. Once the context files are loaded, Spring creates a
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/context/WebApplicationContext.html"><code>WebApplicationContext</code></a>
object based on the bean definitions and stores it in the <code>ServletContext</code> of the web
application.</p>
</div>
<div class="paragraph">
<p>All Java web frameworks are built on top of the Servlet API, and so one can use the
following code snippet to get access to this 'business context' <code>ApplicationContext</code>
created by the <code>ContextLoaderListener</code>.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);</code></pre>
</div>
</div>
<div class="paragraph">
<p>The
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/context/support/WebApplicationContextUtils.html"><code>WebApplicationContextUtils</code></a>
class is for convenience, so you don&#8217;t have to remember the name of the <code>ServletContext</code>
attribute. Its <em>getWebApplicationContext()</em> method will return <code>null</code> if an object
doesn&#8217;t exist under the <code>WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE</code>
key. Rather than risk getting <code>NullPointerExceptions</code> in your application, it&#8217;s better
to use the <code>getRequiredWebApplicationContext()</code> method. This method throws an exception
when the <code>ApplicationContext</code> is missing.</p>
</div>
<div class="paragraph">
<p>Once you have a reference to the <code>WebApplicationContext</code>, you can retrieve beans by
their name or type. Most developers retrieve beans by name and then cast them to one of
their implemented interfaces.</p>
</div>
<div class="paragraph">
<p>Fortunately, most of the frameworks in this section have simpler ways of looking up
beans. Not only do they make it easy to get beans from a Spring container, but they also
allow you to use dependency injection on their controllers. Each web framework section
has more detail on its specific integration strategies.</p>
</div>
</div>
<div class="sect2">
<h3 id="jsf"><a class="anchor" href="#jsf"></a>5.3. JSF</h3>
<div class="paragraph">
<p>JavaServer Faces (JSF) is the JCP&#8217;s standard component-based, event-driven web user
interface framework. As of Java EE 5, it is an official part of the Java EE umbrella.</p>
</div>
<div class="paragraph">
<p>For a popular JSF runtime as well as for popular JSF component libraries, check out the
<a href="https://myfaces.apache.org/">Apache MyFaces project</a>. The MyFaces project also provides
common JSF extensions such as <a href="https://myfaces.apache.org/orchestra/">MyFaces Orchestra</a>:
a Spring-based JSF extension that provides rich conversation scope support.</p>
</div>
<div class="admonitionblock note">
<table>
<tr>
<td class="icon">
<i class="fa icon-note" title="Note"></i>
</td>
<td class="content">
<div class="paragraph">
<p>Spring Web Flow 2.0 provides rich JSF support through its newly established Spring Faces
module, both for JSF-centric usage (as described in this section) and for Spring-centric
usage (using JSF views within a Spring MVC dispatcher). Check out the
<a href="https://projects.spring.io/spring-webflow">Spring Web Flow website</a> for details!</p>
</div>
</td>
</tr>
</table>
</div>
<div class="paragraph">
<p>The key element in Spring&#8217;s JSF integration is the JSF <code>ELResolver</code> mechanism.</p>
</div>
<div class="sect3">
<h4 id="jsf-springbeanfaceselresolver"><a class="anchor" href="#jsf-springbeanfaceselresolver"></a>5.3.1. Spring Bean Resolver</h4>
<div class="paragraph">
<p><code>SpringBeanFacesELResolver</code> is a JSF 1.2+ compliant <code>ELResolver</code> implementation,
integrating with the standard Unified EL as used by JSF 1.2 and JSP 2.1. Like
<code>SpringBeanVariableResolver</code>, it delegates to the Spring&#8217;s 'business context'
<code>WebApplicationContext</code> <em>first</em>, then to the default resolver of the underlying JSF
implementation.</p>
</div>
<div class="paragraph">
<p>Configuration-wise, simply define <code>SpringBeanFacesELResolver</code> in your JSF
<em>faces-context.xml</em> file:</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="xml"><span class="tag">&lt;faces-config&gt;</span>
    <span class="tag">&lt;application&gt;</span>
        <span class="tag">&lt;el-resolver&gt;</span>org.springframework.web.jsf.el.SpringBeanFacesELResolver<span class="tag">&lt;/el-resolver&gt;</span>
        ...
    <span class="tag">&lt;/application&gt;</span>
<span class="tag">&lt;/faces-config&gt;</span></code></pre>
</div>
</div>
</div>
<div class="sect3">
<h4 id="jsf-facescontextutils"><a class="anchor" href="#jsf-facescontextutils"></a>5.3.2. FacesContextUtils</h4>
<div class="paragraph">
<p>A custom <code>VariableResolver</code> works well when mapping one&#8217;s properties to beans
in <em>faces-config.xml</em>, but at times one may need to grab a bean explicitly. The
<a href="https://docs.spring.io/spring-framework/docs/5.0.8.RELEASE/javadoc-api/org/springframework/web/jsf/FacesContextUtils.html"><code>FacesContextUtils</code></a>
class makes this easy. It is similar to <code>WebApplicationContextUtils</code>, except that it
takes a <code>FacesContext</code> parameter rather than a <code>ServletContext</code> parameter.</p>
</div>
<div class="listingblock">
<div class="content">
<pre class="CodeRay highlight"><code data-lang="java">ApplicationContext ctx = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());</code></pre>
</div>
</div>
</div>
</div>
<div class="sect2">
<h3 id="struts"><a class="anchor" href="#struts"></a>5.4. Apache Struts 2.x</h3>
<div class="paragraph">
<p>Invented by Craig McClanahan, <a href="https://struts.apache.org">Struts</a> is an open source project
hosted by the Apache Software Foundation. At the time, it greatly simplified the
JSP/Servlet programming paradigm and won over many developers who were using proprietary
frameworks. It simplified the programming model, it was open source (and thus free as in
beer), and it had a large community, which allowed the project to grow and become popular
among Java web developers.</p>
</div>
<div class="paragraph">
<p>Check out the Struts
<a href="https://struts.apache.org/release/2.3.x/docs/spring-plugin.html">Spring Plugin</a> for the
built-in Spring integration shipped with Struts.</p>
</div>
</div>
<div class="sect2">
<h3 id="tapestry"><a class="anchor" href="#tapestry"></a>5.5. Tapestry 5.x</h3>
<div class="paragraph">
<p>From the <a href="https://tapestry.apache.org/">Tapestry homepage</a>:</p>
</div>
<div class="paragraph">
<p>Tapestry is a "<em>Component oriented framework for creating dynamic, robust,
highly scalable web applications in Java.</em>"</p>
</div>
<div class="paragraph">
<p>While Spring has its own <a href="#mvc">powerful web layer</a>, there are a number of unique
advantages to building an enterprise Java application using a combination of Tapestry
for the web user interface and the Spring container for the lower layers.</p>
</div>
<div class="paragraph">
<p>For more information, check out Tapestry&#8217;s dedicated
<a href="https://tapestry.apache.org/integrating-with-spring-framework.html">integration module for
Spring</a>.</p>
</div>
</div>
<div class="sect2">
<h3 id="web-integration-resources"><a class="anchor" href="#web-integration-resources"></a>5.6. Further Resources</h3>
<div class="paragraph">
<p>Find below links to further resources about the various web frameworks described in this
chapter.</p>
</div>
<div class="ulist">
<ul>
<li>
<p>The <a href="http://www.oracle.com/technetwork/java/javaee/javaserverfaces-139869.html">JSF</a> homepage</p>
</li>
<li>
<p>The <a href="https://struts.apache.org/">Struts</a> homepage</p>
</li>
<li>
<p>The <a href="https://tapestry.apache.org/">Tapestry</a> homepage</p>
</li>
</ul>
</div>
</div>
</div>
</div>
</div>
<div id="footer">
<div id="footer-text">
Version 5.0.8.RELEASE<br>
Last updated 2018-07-26 07:21:49 UTC
</div>
</div>
<script src="tocbot-3.0.2/tocbot.js"></script>
<script>var oldtoc=document.getElementById('toctitle').nextElementSibling;var newtoc=document.createElement('div');newtoc.setAttribute('id','tocbot');newtoc.setAttribute('class','js-toc');oldtoc.parentNode.replaceChild(newtoc,oldtoc);tocbot.init({contentSelector:'#content',headingSelector:'h1, h2, h3, h4, h5',smoothScroll:false});var handleTocOnResize=function(){var width=window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth;if(width<768){tocbot.refresh({contentSelector:'#content',headingSelector:'h1, h2, h3, h4, h5',collapseDepth:6,activeLinkClass:'ignoreactive',throttleTimeout:1000,smoothScroll:false});}else{tocbot.refresh({contentSelector:'#content',headingSelector:'h1, h2, h3, h4, h5',smoothScroll:false});}};window.addEventListener('resize',handleTocOnResize);handleTocOnResize();var link=document.createElement("a");link.setAttribute("href","index.html");link.innerHTML="<i class=\"fa fa-chevron-left\" aria-hidden=\"true\"></i>&nbsp;&nbsp;Back to index";var p=document.createElement("p");p.appendChild(link);var toc=document.getElementById('toc')
var next=document.getElementById('toctitle').nextElementSibling;toc.insertBefore(p,next);</script>
<script>if(window.parent==window){(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create','UA-2728886-23','auto',{'siteSpeedSampleRate':100});ga('send','pageview');}</script></body>
</html>