<h1>Nested Classes</h1>
<p>The Java programming language allows you to define a class within another class. Such a class is called a <i>nested class</i> and is illustrated here:</p>
<div class="codeblock"><pre>
class OuterClass {
    ...
    class NestedClass {
        ...
    }
}
</pre></div>
<div class="note"><hr /><strong>Terminology:</strong>&nbsp;Nested classes are divided into two categories: static and non-static. Nested classes that are declared <code>static</code> are called <i>static nested classes</i>. Non-static nested classes are called <i>inner classes</i>.
<hr /></div>
<div class="codeblock"><pre>
class OuterClass {
    ...
    static class StaticNestedClass {
        ...
    }
    class InnerClass {
        ...
    }
}
</pre></div>
<p>A nested class is a member of its enclosing class. Non-static nested classes (inner classes) have access to other members of the enclosing class, even if they are declared private. Static nested classes do not have access to other members of the enclosing class. As a member of the <code>OuterClass</code>, a nested class can be declared <code>private</code>, <code>public</code>, <code>protected</code>, or <i>package private</i>. (Recall that outer classes can only be declared <code>public</code> or <i>package private</i>.)</p>

<h2>Why Use Nested Classes?</h2>

<p>Compelling reasons for using nested classes include the following:</p>
<ul>

<li><p><strong>It is a way of logically grouping classes that are only used in one place</strong>: If a class is useful to only one other class, then it is logical to embed it in that class and keep the two together. Nesting such &quot;helper classes&quot; makes their package more streamlined.</p></li>

<li><p><strong>It increases encapsulation</strong>: Consider two top-level classes, A and B, where B needs access to members of A that would otherwise be declared <code>private</code>. By hiding class B within class A, A&#39;s members can be declared private and B can access them. In addition, B itself can be hidden from the outside world.</p></li>

<li><p><strong>It can lead to more readable and maintainable code</strong>: Nesting small classes within top-level classes places the code closer to where it is used.</p></li>
</ul>


<h2>Static Nested Classes</h2>

<p>As with class methods and variables, a static nested class is associated with its outer class. And like static class methods, a static nested class cannot refer directly to instance variables or methods defined in its enclosing class: it can use them only through an object reference.</p>
<div class="note"><hr /><strong>Note:</strong>&nbsp;A static nested class interacts with the instance members of its outer class (and other classes) just like any other top-level class. In effect, a static nested class is behaviorally a top-level class that has been nested in another top-level class for packaging convenience.
<hr /></div>
<p>Static nested classes are accessed using the enclosing class name:</p>
<div class="codeblock"><pre>
OuterClass.StaticNestedClass
</pre></div>
<p>For example, to create an object for the static nested class, use this syntax:</p>
<div class="codeblock"><pre>
OuterClass.StaticNestedClass nestedObject =
     new OuterClass.StaticNestedClass();
</pre></div>
<h2>Inner Classes</h2>
<p>As with instance methods and variables, an inner class is associated with an instance of its enclosing class and has direct access to that object&#39;s methods and fields. Also, because an inner class is associated with an instance, it cannot define any static members itself.</p>
<p>Objects that are instances of an inner class exist <i>within</i> an instance of the outer class. Consider the following classes:</p>
<div class="codeblock"><pre>
class OuterClass {
    ...
    class InnerClass {
        ...
    }
}

</pre></div>
<p>An instance of <code>InnerClass</code> can exist only within an instance of <code>OuterClass</code> and has direct access to the methods and fields of its enclosing instance.</p>

<p>To instantiate an inner class, you must first instantiate the outer class. Then, create the inner object within the outer object with this syntax:</p>
<div class="codeblock"><pre>
OuterClass.InnerClass innerObject = outerObject.new InnerClass();
</pre></div>
<p>There are two special kinds of inner classes:
<a class="TutorialLink" target="_top" href="localclasses.html">local classes</a> and
<a class="TutorialLink" target="_top" href="anonymousclasses.html">anonymous classes</a>.</p>

<!--

<div class="note"><hr /><strong>Note:</strong>&nbsp;For more information about the taxonomy of the different kinds of classes in the Java programming language (which can be tricky to describe concisely, clearly, and correctly), see Joseph Darcy&#39;s blog:
<a class="OutsideLink" target="_blank" href="https://blogs.oracle.com/darcy/entry/nested_inner_member_and_top">Nested, Inner, Member, and Top-Level Classes</a>.
<hr /></div>

-->

<h2><a name="shadowing">Shadowing</a></h2>

<p>If a declaration of a type (such as a member variable or a parameter name) in a particular scope (such as an inner class or a method definition) has the same name as another declaration in the enclosing scope, then the declaration <em>shadows</em> the declaration of the enclosing scope. You cannot refer to a shadowed declaration by its name alone. The following example,
<a class="SourceLink" target="_blank" href="examples/ShadowTest.java" onclick="showCode('../../displayCode.html', 'examples/ShadowTest.java'); return false;"><code>ShadowTest</code></a>, demonstrates this:</p>

<pre class="codeblock">
 
public class ShadowTest {

    public int x = 0;

    class FirstLevel {

        public int x = 1;

        void methodInFirstLevel(int x) {
            System.out.println(&quot;x = &quot; + x);
            System.out.println(&quot;this.x = &quot; + this.x);
            System.out.println(&quot;ShadowTest.this.x = &quot; + ShadowTest.this.x);
        }
    }

    public static void main(String... args) {
        ShadowTest st = new ShadowTest();
        ShadowTest.FirstLevel fl = st.new FirstLevel();
        fl.methodInFirstLevel(23);
    }
}
</pre>

<p>The following is the output of this example:</p>

<pre class="codeblock">x = 23
this.x = 1
ShadowTest.this.x = 0</pre>

<p>This example defines three variables named <code>x</code>: the member variable of the class <code>ShadowTest</code>, the member variable of the inner class <code>FirstLevel</code>, and the parameter in the method <code>methodInFirstLevel</code>. The variable <code>x</code> defined as a parameter of the method <code>methodInFirstLevel</code> shadows the variable of the inner class <code>FirstLevel</code>. Consequently, when you use the variable <code>x</code> in the method <code>methodInFirstLevel</code>, it refers to the method parameter. To refer to the member variable of the inner class <code>FirstLevel</code>, use the keyword <code>this</code> to represent the enclosing scope:</p>

<pre class="codeblock">System.out.println("this.x = " + this.x);</pre>

<p>Refer to member variables that enclose larger scopes by the class name to which they belong. For example, the following statement accesses the member variable of the class <code>ShadowTest</code> from the method <code>methodInFirstLevel</code>:</p>

<pre class="codeblock">System.out.println("ShadowTest.this.x = " + ShadowTest.this.x);</pre>

<h2><a name="serialization">Serialization</a></h2>

<p>
<a class="TutorialLink" target="_top" href="../../jndi/objects/serial.html">Serialization</a> of inner classes, including
<a class="TutorialLink" target="_top" href="localclasses.html">local</a> and
<a class="TutorialLink" target="_top" href="anonymousclasses.html">anonymous</a> classes, is strongly discouraged. When the Java compiler compiles certain constructs, such as inner classes, it creates <em>synthetic constructs</em>; these are classes, methods, fields, and other constructs that do not have a corresponding construct in the source code. Synthetic constructs enable Java compilers to implement new Java language features without changes to the JVM. However, synthetic constructs can vary among different Java compiler implementations, which means that <code>.class</code> files can vary among different implementations as well. Consequently, you may have compatibility issues if you serialize an inner class and then deserialize it with a different JRE implementation. See the section
<a class="TutorialLink" target="_top" href="../../reflect/member/methodparameterreflection.html#implcit_and_synthetic">Implicit and Synthetic Parameters</a> in the section
<a class="TutorialLink" target="_top" href="../../reflect/member/methodparameterreflection.html">Obtaining Names of Method Parameters</a> for more information about the synthetic constructs generated when an inner class is compiled.</p>

