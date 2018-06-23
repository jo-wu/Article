<h1>Trail: The Reflection API</h1>
<h2>Uses of Reflection</h2>
<p>Reflection is commonly used by programs which require the ability to examine or modify the runtime behavior of applications running in the Java virtual machine. This is a relatively advanced feature and should be used only by developers who have a strong grasp of the fundamentals of the language. With that caveat in mind, reflection is a powerful technique and can enable applications to perform operations which would otherwise be impossible.</p>
<dl>
<dt style="font-weight: bold">Extensibility Features</dt>
<dd>An application may make use of external, user-defined classes by creating instances of extensibility objects using their fully-qualified names.</dd>
<dt style="font-weight: bold">Class Browsers and Visual Development Environments</dt>
<dd>A class browser needs to be able to enumerate the members of classes. Visual development environments can benefit from making use of type information available in reflection to aid the developer in writing correct code.</dd>
<dt style="font-weight: bold">Debuggers and Test Tools</dt>
<dd>Debuggers need to be able to examine private members on classes. Test harnesses can make use of reflection to systematically call a discoverable set APIs defined on a class, to insure a high level of code coverage in a test suite.</dd>
</dl>
<h2>Drawbacks of Reflection</h2>
<p>Reflection is powerful, but should not be used indiscriminately. If it is possible to perform an operation without using reflection, then it is preferable to avoid using it. The following concerns should be kept in mind when accessing code via reflection.</p>
<dl>
<dt style="font-weight: bold">Performance Overhead</dt>
<dd>Because reflection involves types that are dynamically resolved, certain Java virtual machine optimizations can not be performed. Consequently, reflective operations have slower performance than their non-reflective counterparts, and should be avoided in sections of code which are called frequently in performance-sensitive applications.</dd>
<dt style="font-weight: bold">Security Restrictions</dt>
<dd>Reflection requires a runtime permission which may not be present when running under a security manager. This is in an important consideration for code which has to run in a restricted security context, such as in an Applet.</dd>
<dt style="font-weight: bold">Exposure of Internals</dt>
<dd>Since reflection allows code to perform operations that would be illegal in non-reflective code, such as accessing <code>private</code> fields and methods, the use of reflection can result in unexpected side-effects, which may render code dysfunctional and may destroy portability. Reflective code breaks abstractions and therefore may change behavior with upgrades of the platform.</dd>
</dl>
<h2>Trail Lessons</h2>
<p>This trail covers common uses of reflection for accessing and manipulating classes, fields, methods, and constructors. Each lesson contains code examples, tips, and troubleshooting information.</p>
<dl>
<dt><a href="class/index.html"><img src="../images/reflectionsm.GIF" align="left" width="20" height="20" border="0" alt="trail icon" /> <b>Classes</b></a></dt>
<dd>This lesson shows the various ways to obtain a 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html"><code>Class</code></a> object and use it to examine properties of a class, including its declaration and contents.</dd>
<dt><a href="member/index.html"><img src="../images/reflectionsm.GIF" align="left" width="20" height="20" border="0" alt="trail icon" /> <b>Members</b></a></dt>
<dd>This lesson describes how to use the Reflection APIs to find the fields, methods, and constructors of a class. Examples are provided for setting and getting field values, invoking methods, and creating new instances of objects using specific constructors.</dd>
<dt><a href="special/index.html"><img src="../images/reflectionsm.GIF" align="left" width="20" height="20" border="0" alt="trail icon" /> <b>Arrays and Enumerated Types</b></a></dt>
<dd>This lesson introduces two special types of classes: arrays, which are generated at runtime, and <code>enum</code> types, which define unique named object instances. Sample code shows how to retrieve the component type for an array and how to set and get fields with array or <code>enum</code> types.</dd>
</dl>
<div class="note"><hr /><strong>Note:</strong>&nbsp;<p>The examples in this trail are designed for experimenting with the Reflection APIs. The handling of exceptions therefore is not the same as would be used in production code. In particular, in production code it is not recommended to dump stack traces that are visible to the user.</p>
<hr /></div>
