<h1>Expressions, Statements, and Blocks</h1>
<p>Now that you understand variables and operators, it&#39;s time to learn about <i>expressions</i>, <i>statements</i>, and <i>blocks</i>. Operators may be used in building expressions, which compute values; expressions are the core components of statements; statements may be grouped into blocks.</p>
<h2>Expressions</h2>
<p>An <em>expression</em> is a construct made up of variables, operators, and method invocations, which are constructed according to the syntax of the language, that evaluates to a single value. You&#39;ve already seen examples of expressions, illustrated in bold below:</p>
<div class="codeblock"><pre>
<code>int <strong>cadence = 0</strong>;</code>
<code><strong>anArray[0] = 100</strong>;</code>
<code>System.out.println(<strong>"Element 1 at index 0: " + anArray[0]</strong>);</code>

<code>int <strong>result = 1 + 2</strong>; // result is now 3</code>
<code>if (<strong>value1 == value2</strong>) 
    System.out.println(<strong>"value1 == value2"</strong>);</code>
</pre></div>
<p>The data type of the value returned by an expression depends on the elements used in the expression. The expression <code>cadence = 0</code> returns an <code>int</code> because the assignment operator returns a value of the same data type as its left-hand operand; in this case, <code>cadence</code> is an <code>int</code>. As you can see from the other expressions, an expression can return other types of values as well, such as <code>boolean</code> or <code>String</code>.</p>
<p>The Java programming language allows you to construct compound expressions from various smaller expressions as long as the data type required by one part of the expression matches the data type of the other. Here&#39;s an example of a compound expression:</p>
<div class="codeblock"><pre> 
1 * 2 * 3
</pre></div>
<p>In this particular example, the order in which the expression is evaluated is unimportant because the result of multiplication is independent of order; the outcome is always the same, no matter in which order you apply the multiplications. However, this is not true of all expressions. For example, the following expression gives different results, depending on whether you perform the addition or the division operation first:</p>
<div class="codeblock"><pre>
x + y / 100    // ambiguous
</pre></div>
<p>You can specify exactly how an expression will be evaluated using balanced parenthesis: ( and ). For example, to make the previous expression unambiguous, you could write the following:</p>
<div class="codeblock"><pre> 
(x + y) / 100  // unambiguous, recommended
</pre></div>
<p>If you don&#39;t explicitly indicate the order for the operations to be performed, the order is determined by the precedence assigned to the operators in use within the expression. Operators that have a higher precedence get evaluated first. For example, the division operator has a higher precedence than does the addition operator. Therefore, the following two statements are equivalent:</p>
<div class="codeblock"><pre>
x + y / 100 <br />

x + (y / 100) // unambiguous, recommended
</pre></div>
<p>When writing compound expressions, be explicit and indicate with parentheses which operators should be evaluated first. This practice makes code easier to read and to maintain.</p>
<h2>Statements</h2>
<p>Statements are roughly equivalent to sentences in natural languages. A <em>statement</em> forms a complete unit of execution. The following types of expressions can be made into a statement by terminating the expression with a semicolon (<code>;</code>).</p>
<ul>
<li>Assignment expressions</li>
<li>Any use of <code>++</code> or <code>--</code></li>
<li>Method invocations</li>
<li>Object creation expressions</li>
</ul>
<p>Such statements are called <em>expression statements</em>. Here are some examples of expression statements.</p>
<div class="codeblock"><pre>
// assignment statement
aValue = 8933.234;
// increment statement
aValue++;
// method invocation statement
System.out.println("Hello World!");
// object creation statement
Bicycle myBike = new Bicycle();
</pre></div>
<p>In addition to expression statements, there are two other kinds of statements: <i>declaration statements</i> and <i>control flow statements</i>. A <em>declaration statement</em> declares a variable. You&#39;ve seen many examples of declaration statements already:</p>
<div class="codeblock"><pre>
// declaration statement
double aValue = 8933.234;
</pre></div>
<p>Finally, <em>control flow statements</em> regulate the order in which statements get executed. You&#39;ll learn about control flow statements in the next section, 
<a class="TutorialLink" target="_top" href="flow.html">Control Flow Statements</a></p>
<h2>Blocks</h2>
<p>A <i>block</i> is a group of zero or more statements between balanced braces and can be used anywhere a single statement is allowed. The following example, 
<a class="SourceLink" target="_blank" href="examples/BlockDemo.java" onclick="showCode('../../displayCode.html', 'examples/BlockDemo.java'); return false;"><code>BlockDemo</code></a>, illustrates the use of blocks:</p>
<div class="codeblock"><pre>
class BlockDemo {
     public static void main(String[] args) {
          boolean condition = true;
          if (condition) { <strong>// begin block 1</strong>
               System.out.println("Condition is true.");
          } <strong>// end block one</strong>
          else { <strong>// begin block 2</strong>
               System.out.println("Condition is false.");
          } <strong>// end block 2</strong>
     }
}
</pre></div>
