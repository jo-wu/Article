<h1>Operators</h1>
<p>Now that you&#39;ve learned how to declare and initialize variables, you probably want to know how to <i>do something</i> with them. Learning the operators of the Java programming language is a good place to start. Operators are special symbols that perform specific operations on one, two, or three <i>operands</i>, and then return a result.</p>
<p>As we explore the operators of the Java programming language, it may be helpful for you to know ahead of time which operators have the highest precedence. The operators in the following table are listed according to precedence order. The closer to the top of the table an operator appears, the higher its precedence. Operators with higher precedence are evaluated before operators with relatively lower precedence. Operators on the same line have equal precedence. When operators of equal precedence appear in the same expression, a rule must govern which is evaluated first. All binary operators except for the assignment operators are evaluated from left to right; assignment operators are evaluated right to left.</p>
<table border="1" cellpadding="5" summary="This table lists operators according to precedence order">
<caption id="nutsandbolts-precedence"><strong>Operator Precedence</strong></caption>
<tr>
<th>Operators</th>
<th>Precedence</th>
</tr>
<tr>
<td>postfix</td>
<td><code><em>expr</em>++ <em>expr</em>--</code></td>
</tr>
<tr>
<td>unary</td>
<td><code>++<em>expr</em> --<em>expr</em> +<em>expr</em> -<em>expr</em> ~ !</code></td>
</tr>
<tr>
<td>multiplicative</td>
<td><code>* / %</code></td>
</tr>
<tr>
<td>additive</td>
<td><code>+ -</code></td>
</tr>
<tr>
<td>shift</td>
<td><code>&lt;&lt; &gt;&gt; &gt;&gt;&gt;</code></td>
</tr>
<tr>
<td>relational</td>
<td><code>&lt; &gt; &lt;= &gt;= instanceof</code></td>
</tr>
<tr>
<td>equality</td>
<td><code>== !=</code></td>
</tr>
<tr>
<td>bitwise AND</td>
<td><code>&amp;</code></td>
</tr>
<tr>
<td>bitwise exclusive OR</td>
<td><code>^</code></td>
</tr>
<tr>
<td>bitwise inclusive OR</td>
<td><code>|</code></td>
</tr>
<tr>
<td>logical AND</td>
<td><code>&amp;&amp;</code></td>
</tr>
<tr>
<td>logical OR</td>
<td><code>||</code></td>
</tr>
<tr>
<td>ternary</td>
<td><code>? :</code></td>
</tr>
<tr>
<td>assignment</td>
<td><code>= += -= *= /= %= &amp;= ^= |= &lt;&lt;= &gt;&gt;= &gt;&gt;&gt;=</code></td>
</tr>
</table>
<br />
<p>In general-purpose programming, certain operators tend to appear more frequently than others; for example, the assignment operator &quot;<code>=</code>&quot; is far more common than the unsigned right shift operator &quot;<code>&gt;&gt;&gt;</code>&quot;. With that in mind, the following discussion focuses first on the operators that you&#39;re most likely to use on a regular basis, and ends focusing on those that are less common. Each discussion is accompanied by sample code that you can compile and run. Studying its output will help reinforce what you&#39;ve just learned.</p>

