<h1>File Operations</h1>
<!-- File Operations -->
<p>The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html"><code>Files</code></a> class is the other primary entrypoint of the <code>java.nio.file</code> package. This class offers a rich set of static methods for reading, writing, and manipulating files and directories. The <code>Files</code> methods work on instances of <code>Path</code> objects. Before proceeding to the remaining sections, you should familiarize yourself with the following common concepts:</p>
<ul>
<li><a href="#resources">Releasing System Resources</a></li>
<li><a href="#exception">Catching Exceptions</a></li>
<li><a href="#varargs">Varargs</a></li>
<li><a href="#atomic">Atomic Operations</a></li>
<li><a href="#chaining">Method Chaining</a></li>
<li><a href="#glob">What <em>Is</em> a Glob?</a></li>
<li><a href="#linkaware">Link Awareness</a></li>
</ul>
<h2><a name="resources" id="resources">Releasing System Resources</a></h2>
<p>Many of the resources that are used in this API, such as streams or channels, implement or extend the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html"><code>java.io.Closeable</code></a> interface. A requirement of a <code>Closeable</code> resource is that the <code>close</code> method must be invoked to release the resource when no longer required. Neglecting to close a resource can have a negative implication on an application&#39;s performance. The <code>try-</code>with-resources statement, described in the next section, handles this step for you.</p>
<h2><a name="exception" id="exception">Catching Exceptions</a></h2>
<p>With file I/O, unexpected conditions are a fact of life: a file exists (or doesn&#39;t exist) when expected, the program doesn&#39;t have access to the file system, the default file system implementation does not support a particular function, and so on. Numerous errors can be encountered.</p>
<p>All methods that access the file system can throw an <code>IOException</code>. It is best practice to catch these exceptions by embedding these methods into a <code>try-</code>with-resources statement, introduced in the Java SE 7 release. The <code>try-</code>with-resources statement has the advantage that the compiler automatically generates the code to close the resource(s) when no longer required. The following code shows how this might look:</p>
<div class="codeblock"><pre>
Charset charset = Charset.forName("US-ASCII");
String s = ...;
try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
    writer.write(s, 0, s.length());
} catch (IOException x) {
    System.err.format("IOException: %s%n", x);
}
</pre></div>
<p>For more information, see 
<a class="TutorialLink" target="_top" href="../../essential/exceptions/tryResourceClose.html">The try-with-resources Statement</a>.</p>
<p>Alternatively, you can embed the file I/O methods in a <code>try</code> block and then catch any exceptions in a <code>catch</code> block. If your code has opened any streams or channels, you should close them in a <code>finally</code> block. The previous example would look something like the following using the try-catch-finally approach:</p>
<div class="codeblock"><pre>
Charset charset = Charset.forName("US-ASCII");
String s = ...;
BufferedWriter writer = null;
try {
    writer = Files.newBufferedWriter(file, charset);
    writer.write(s, 0, s.length());
} catch (IOException x) {
    System.err.format("IOException: %s%n", x);
} finally {
    if (writer != null) writer.close();
}
</pre></div>
<p>For more information, see 
<a class="TutorialLink" target="_top" href="../../essential/exceptions/handling.html">Catching and Handling Exceptions</a>.</p>
<p>In addition to <code>IOException</code>, many specific exceptions extend 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystemException.html"><code>FileSystemException</code></a>. This class has some useful methods that return the file involved 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystemException.html#getFile--">(<code>getFile</code>)</a>, the detailed message string 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystemException.html#getMessage--">(<code>getMessage</code>)</a>, the reason why the file system operation failed 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystemException.html#getReason--">(<code>getReason</code>)</a>, and the &quot;other&quot; file involved, if any 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystemException.html#getOtherFile--">(<code>getOtherFile</code>)</a>.</p>
<p>The following code snippet shows how the <code>getFile</code> method might be used:</p>
<div class="codeblock"><pre>
try (...) {
    ...    
} catch (NoSuchFileException x) {
    System.err.format("%s does not exist\n", x.getFile());
}
</pre></div>
<p>For purposes of clarity, the file I/O examples in this lesson may not show exception handling, but your code should always include it.</p>
<h2><a name="varargs" id="varargs">Varargs</a></h2>
<p>Several <code>Files</code> methods accept an arbitrary number of arguments when flags are specified. For example, in the following method signature, the ellipses notation after the <code>CopyOption</code> argument indicates that the method accepts a variable number of arguments, or <em>varargs</em>, as they are typically called:</p>
<div class="codeblock"><pre>
Path Files.move(Path, Path, <b>CopyOption...</b>)
</pre></div>
<p>When a method accepts a varargs argument, you can pass it a comma-separated list of values or an array (<code>CopyOption[]</code>) of values.</p>
<p>In the <code>move</code> example, the method can be invoked as follows:</p>
<div class="codeblock"><pre>
import static java.nio.file.StandardCopyOption.*;

Path source = ...;
Path target = ...;
Files.move(source,
           target,
           REPLACE_EXISTING,
           ATOMIC_MOVE);
</pre></div>
<p>For more information about varargs syntax, see 
<a class="TutorialLink" target="_top" href="../../java/javaOO/arguments.html#varargs">Arbitrary Number of Arguments</a>.</p>
<h2><a name="atomic" id="atomic">Atomic Operations</a></h2>
<p>Several <code>Files</code> methods, such as <code>move</code>, can perform certain operations atomically in some file systems.</p>
<p>An <em>atomic file operation</em> is an operation that cannot be interrupted or &quot;partially&quot; performed. Either the entire operation is performed or the operation fails. This is important when you have multiple processes operating on the same area of the file system, and you need to guarantee that each process accesses a complete file.</p>
<h2><a name="chaining" id="chaining">Method Chaining</a></h2>
<p>Many of the file I/O methods support the concept of <em>method chaining</em>.</p>
<p>You first invoke a method that returns an object. You then immediately invoke a method on <em>that</em> object, which returns yet another object, and so on. Many of the I/O examples use the following technique:</p>
<div class="codeblock"><pre>
String value = Charset.defaultCharset().decode(buf).toString();
UserPrincipal group =
    file.getFileSystem().getUserPrincipalLookupService().
         lookupPrincipalByName("me");
</pre></div>
<p>This technique produces compact code and enables you to avoid declaring temporary variables that you don&#39;t need.</p>
<h2><a name="glob" id="glob">What <em>Is</em> a Glob?</a></h2>
<p>Two methods in the <code>Files</code> class accept a glob argument, but what is a <em>glob</em>?</p>
<p>You can use glob syntax to specify pattern-matching behavior.</p>
<p>A glob pattern is specified as a string and is matched against other strings, such as directory or file names. Glob syntax follows several simple rules:</p>
<ul>
<li>An asterisk, <code>*</code>, matches any number of characters (including none).</li>
<li>Two asterisks, <code>**</code>, works like <code>*</code> but crosses directory boundaries. This syntax is generally used for matching complete paths.</li>
<li>A question mark, <code>?</code>, matches exactly one character.</li>
<li>Braces specify a collection of subpatterns. For example:
<ul>
<li><code>{sun,moon,stars}</code> matches &quot;sun&quot;, &quot;moon&quot;, or &quot;stars&quot;.</li>
<li><code>{temp*,tmp*}</code> matches all strings beginning with &quot;temp&quot; or &quot;tmp&quot;.</li>
</ul>
</li>
<li>Square brackets convey a set of single characters or, when the hyphen character (<code>-</code>) is used, a range of characters. For example:
<ul>
<li><code>[aeiou]</code> matches any lowercase vowel.</li>
<li><code>[0-9]</code> matches any digit.</li>
<li><code>[A-Z]</code> matches any uppercase letter.</li>
<li><code>[a-z,A-Z]</code> matches any uppercase or lowercase letter.</li>
</ul>
Within the square brackets, <code>*</code>, <code>?</code>, and <code>\</code> match themselves.</li>
<li>All other characters match themselves.</li>
<li>To match <code>*</code>, <code>?</code>, or the other special characters, you can escape them by using the backslash character, <code>\</code>. For example: <code>\\</code> matches a single backslash, and <code>\?</code> matches the question mark.</li>
</ul>
<p>Here are some examples of glob syntax:</p>
<ul>
<li><code>*.html</code> &ndash; Matches all strings that end in <em>.html</em></li>
<li><code>???</code> &ndash; Matches all strings with exactly three letters or digits</li>
<li><code>*[0-9]*</code> &ndash; Matches all strings containing a numeric value</li>
<li><code>*.{htm,html,pdf}</code> &ndash; Matches any string ending with <em>.htm</em>, <em>.html</em> or <em>.pdf</em></li>
<li><code>a?*.java</code> &ndash; Matches any string beginning with <code>a</code>, followed by at least one letter or digit, and ending with <em>.java</em></li>
<li><code>{foo*,*[0-9]*}</code> &ndash; Matches any string beginning with <em>foo</em> or any string containing a numeric value</li>
</ul>
<div class="note"><hr /><strong>Note:</strong>&nbsp;If you are typing the glob pattern at the keyboard and it contains one of the special characters, you must put the pattern in quotes (<code>&quot;*&quot;</code>), use the backslash (<code>\*</code>), or use whatever escape mechanism is supported at the command line.
<hr /></div>
<p>The glob syntax is powerful and easy to use. However, if it is not sufficient for your needs, you can also use a regular expression. For more information, see the 
<a class="TutorialLink" target="_top" href="../../essential/regex/index.html">Regular Expressions</a> lesson.</p>
<p>For more information about the glob sytnax, see the API specification for the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-"><code>getPathMatcher</code></a> method in the <code>FileSystem</code> class.</p>
<h2><a name="linkaware" id="linkaware">Link Awareness</a></h2>
<p>The <code>Files</code> class is &quot;link aware.&quot; Every <code>Files</code> method either detects what to do when a symbolic link is encountered, or it provides an option enabling you to configure the behavior when a symbolic link is encountered.</p>
