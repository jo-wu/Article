<h1>Path Operations</h1>
<!-- Path Operations -->
<p>The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html"><code>Path</code></a> class includes various methods that can be used to obtain information about the path, access elements of the path, convert the path to other forms, or extract portions of a path. There are also methods for matching the path string and methods for removing redundancies in a path. This lesson addresses these <code>Path</code> methods, sometimes called <em>syntactic</em> operations, because they operate on the path itself and don&#39;t access the file system.</p>
<p>This section covers the following:</p>
<ul>
<li><a href="#create">Creating a Path</a></li>
<li><a href="#info">Retrieving Information About a Path</a></li>
<li><a href="#normal">Removing Redundancies from a Path</a></li>
<li><a href="#convert">Converting a Path</a></li>
<li><a href="#resolve">Joining Two Paths</a></li>
<li><a href="#relativize">Creating a Path Between Two Paths</a></li>
<li><a href="#compare">Comparing Two Paths</a></li>
</ul>
<h2><a name="create" id="create">Creating a Path</a></h2>
<p>A <code>Path</code> instance contains the information used to specify the location of a file or directory. At the time it is defined, a <code>Path</code> is provided with a series of one or more names. A root element or a file name might be included, but neither are required. A <code>Path</code> might consist of just a single directory or file name.</p>
<p>You can easily create a <code>Path</code> object by using one of the following <code>get</code> methods from the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html"><code>Paths</code></a> (note the plural) helper class:</p>
<div class="codeblock"><pre>
Path p1 = Paths.get("/tmp/foo");
Path p2 = Paths.get(args[0]);
Path p3 = Paths.get(URI.create("file:///Users/joe/FileTest.java"));
</pre></div>
<p>The <code>Paths.get</code> method is shorthand for the following code:</p>
<div class="codeblock"><pre>
Path p4 = FileSystems.getDefault().getPath("/users/sally");
</pre></div>
<p>The following example creates <code>/u/joe/logs/foo.log</code> assuming your home directory is <code>/u/joe</code>, or <code>C:\joe\logs\foo.log</code> if you are on Windows.</p>
<div class="codeblock"><pre>
Path p5 = Paths.get(System.getProperty("user.home"),"logs", "foo.log");
</pre></div>
<h2><a name="info" id="info">Retrieving Information about a Path</a></h2>
<p>You can think of the <code>Path</code> as storing these name elements as a sequence. The highest element in the directory structure would be located at index 0. The lowest element in the directory structure would be located at index <code>[n-1]</code>, where <code>n</code> is the number of name elements in the <code>Path</code>. Methods are available for retrieving individual elements or a subsequence of the <code>Path</code> using these indexes.</p>
<p>The examples in this lesson use the following directory structure.</p>
<center><img src="../../figures/essential/io-dirStructure.gif" width="228" height="308" align="bottom" alt="Sample directory structure" /></p><p class="FigureCaption"><center>Sample Directory Structure</center></p></center><p>The following code snippet defines a <code>Path</code> instance and then invokes several methods to obtain information about the path:</p>
<div class="codeblock"><pre>
// None of these methods requires that the file corresponding
// to the Path exists.
// Microsoft Windows syntax
Path path = Paths.get("C:\\home\\joe\\foo");

// Solaris syntax
Path path = Paths.get("/home/joe/foo");

System.out.format("toString: %s%n", path.toString());
System.out.format("getFileName: %s%n", path.getFileName());
System.out.format("getName(0): %s%n", path.getName(0));
System.out.format("getNameCount: %d%n", path.getNameCount());
System.out.format("subpath(0,2): %s%n", path.subpath(0,2));
System.out.format("getParent: %s%n", path.getParent());
System.out.format("getRoot: %s%n", path.getRoot());
</pre></div>
<p>Here is the output for both Windows and the Solaris OS:</p>
<table border="1" cellspacing="2" cellpadding="0" summary="This table shows the output for various Path methods for
       Microsoft Windows and Solaris and includes comments about the methods">
<tr>
<th>Method Invoked</th>
<th>Returns in the Solaris OS</th>
<th>Returns in Microsoft Windows</th>
<th>Comment</th>
</tr>
<tr>
<td><code>toString</code></td>
<td><code>/home/joe/foo</code></td>
<td><code>C:\home\joe\foo</code></td>
<td>Returns the string representation of the <code>Path</code>. If the path was created using <code>Filesystems.getDefault().getPath(String)</code> or <code>Paths.get</code> (the latter is a convenience method for <code>getPath</code>), the method performs minor syntactic cleanup. For example, in a UNIX operating system, it will correct the input string <code>//home/joe/foo</code> to <code>/home/joe/foo</code>.</td>
</tr>
<tr>
<td><code>getFileName</code></td>
<td><code>foo</code></td>
<td><code>foo</code></td>
<td>Returns the file name or the last element of the sequence of name elements.</td>
</tr>
<tr>
<td><code>getName(0)</code></td>
<td><code>home</code></td>
<td><code>home</code></td>
<td>Returns the path element corresponding to the specified index. The 0th element is the path element closest to the root.</td>
</tr>
<tr>
<td><code>getNameCount</code></td>
<td><code>3</code></td>
<td><code>3</code></td>
<td>Returns the number of elements in the path.</td>
</tr>
<tr>
<td><code>subpath(0,2)</code></td>
<td><code>home/joe</code></td>
<td><code>home\joe</code></td>
<td>Returns the subsequence of the <code>Path</code> (not including a root element) as specified by the beginning and ending indexes.</td>
</tr>
<tr>
<td><code>getParent</code></td>
<td><code>/home/joe</code></td>
<td><code>\home\joe</code></td>
<td>Returns the path of the parent directory.</td>
</tr>
<tr>
<td><code>getRoot</code></td>
<td><code>/</code></td>
<td><code>C:\</code></td>
<td>Returns the root of the path.</td>
</tr>
</table>
<p>The previous example shows the output for an absolute path. In the following example, a relative path is specified:</p>
<div class="codeblock"><pre>
// Solaris syntax
Path path = Paths.get("sally/bar");
or
// Microsoft Windows syntax
Path path = Paths.get("sally\\bar");
</pre></div>
<p>Here is the output for Windows and the Solaris OS:</p>
<table border="1" summary="This table shows the output for various Path methods for
the Solaris OS and Microsoft Windows, this time using a relative path.
cellspacing=" cellpadding="0">
<tr>
<th>Method Invoked</th>
<th>Returns in the Solaris OS</th>
<th>Returns in Microsoft Windows</th>
</tr>
<tr>
<td><code>toString</code></td>
<td><code>sally/bar</code></td>
<td><code>sally\bar</code></td>
</tr>
<tr>
<td><code>getFileName</code></td>
<td><code>bar</code></td>
<td><code>bar</code></td>
</tr>
<tr>
<td><code>getName(0)</code></td>
<td><code>sally</code></td>
<td><code>sally</code></td>
</tr>
<tr>
<td><code>getNameCount</code></td>
<td><code>2</code></td>
<td><code>2</code></td>
</tr>
<tr>
<td><code>subpath(0,1)</code></td>
<td><code>sally</code></td>
<td><code>sally</code></td>
</tr>
<tr>
<td><code>getParent</code></td>
<td><code>sally</code></td>
<td><code>sally</code></td>
</tr>
<tr>
<td><code>getRoot</code></td>
<td><code>null</code></td>
<td><code>null</code></td>
</tr>
</table>
<h2><a name="normal" id="normal">Removing Redundancies From a Path</a></h2>
<p>Many file systems use &quot;.&quot; notation to denote the current directory and &quot;..&quot; to denote the parent directory. You might have a situation where a <code>Path</code> contains redundant directory information. Perhaps a server is configured to save its log files in the &quot;<code>/dir/logs/.</code>&quot; directory, and you want to delete the trailing &quot;<code>/.</code>&quot; notation from the path.</p>
<p>The following examples both include redundancies:</p>
<div class="codeblock"><pre>
/home/./joe/foo
/home/sally/../joe/foo
</pre></div>
<p>The <code>normalize</code> method removes any redundant elements, which includes any &quot;<code>.</code>&quot; or &quot;<code><em>directory</em>/..</code>&quot; occurrences. Both of the preceding examples normalize to <code>/home/joe/foo</code>.</p>
<p>It is important to note that <code>normalize</code> doesn&#39;t check at the file system when it cleans up a path. It is a purely syntactic operation. In the second example, if <code>sally</code> were a symbolic link, removing <code>sally/..</code> might result in a <code>Path</code> that no longer locates the intended file.</p>
<p>To clean up a path while ensuring that the result locates the correct file, you can use the <code>toRealPath</code> method. This method is described in the next section, 
<a class="TutorialLink" target="_top" href="#convert">Converting a Path</a>.</p>
<h2><a name="convert" id="convert">Converting a Path</a></h2>
<p>You can use three methods to convert the <code>Path</code>. If you need to convert the path to a string that can be opened from a browser, you can use 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#toUri--"><code>toUri</code></a>. For example:</p>
<div class="codeblock"><pre>
Path p1 = Paths.get("/home/logfile");
// Result is <strong>file:///home/logfile</strong>
System.out.format("%s%n", p1.toUri());
</pre></div>
<p>The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#toAbsolutePath--"><code>toAbsolutePath</code></a> method converts a path to an absolute path. If the passed-in path is already absolute, it returns the same <code>Path</code> object. The <code>toAbsolutePath</code> method can be very helpful when processing user-entered file names. For example:</p>
<div class="codeblock"><pre>
public class FileTest {
    public static void main(String[] args) {

        if (args.length &lt; 1) {
            System.out.println("usage: FileTest file");
            System.exit(-1);
        }

        // Converts the input string to a Path object.
        Path inputPath = Paths.get(args[0]);

        <strong>// Converts the input Path
        // to an absolute path.
        // Generally, this means prepending
        // the current working
        // directory.  If this example
        // were called like this:
        //     java FileTest foo
        // the getRoot and getParent methods
        // would return null
        // on the original "inputPath"
        // instance.  Invoking getRoot and
        // getParent on the "fullPath"
        // instance returns expected values.
        Path fullPath = inputPath.toAbsolutePath();</strong>
    }
}
</pre></div>
<p>The <code>toAbsolutePath</code> method converts the user input and returns a <code>Path</code> that returns useful values when queried. The file does not need to exist for this method to work.</p>
<p>The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#toRealPath-java.nio.file.LinkOption...-"><code>toRealPath</code></a> method returns the <em>real</em> path of an existing file. This method performs several operations in one:</p>
<ul>
<li>If <code>true</code> is passed to this method and the file system supports symbolic links, this method resolves any symbolic links in the path.</li>
<li>If the <code>Path</code> is relative, it returns an absolute path.</li>
<li>If the <code>Path</code> contains any redundant elements, it returns a path with those elements removed.</li>
</ul>
<p>This method throws an exception if the file does not exist or cannot be accessed. You can catch the exception when you want to handle any of these cases. For example:</p>
<div class="codeblock"><pre>
try {
    Path fp = path.toRealPath();
} catch (NoSuchFileException x) {
    System.err.format("%s: no such" + " file or directory%n", path);
    // Logic for case when file doesn't exist.
} catch (IOException x) {
    System.err.format("%s%n", x);
    // Logic for other sort of file error.
}
</pre></div>
<h2><a name="resolve" id="resolve">Joining Two Paths</a></h2>
<p>You can combine paths by using the <code>resolve</code> method. You pass in a <em>partial path</em> , which is a path that does not include a root element, and that partial path is appended to the original path.</p>
<p>For example, consider the following code snippet:</p>
<div class="codeblock"><pre>
// Solaris
Path p1 = Paths.get("/home/joe/foo");
// Result is <strong>/home/joe/foo/bar</strong>
System.out.format("%s%n", p1.resolve("bar"));

or

// Microsoft Windows
Path p1 = Paths.get("C:\\home\\joe\\foo");
// Result is <strong>C:\home\joe\foo\bar</strong>
System.out.format("%s%n", p1.resolve("bar"));
</pre></div>
<p>Passing an absolute path to the <code>resolve</code> method returns the passed-in path:</p>
<div class="codeblock"><pre>
// Result is <strong>/home/joe</strong>
Paths.get("foo").resolve("/home/joe");
</pre></div>
<h2><a name="relativize" id="relativize">Creating a Path Between Two Paths</a></h2>
<p>A common requirement when you are writing file I/O code is the capability to construct a path from one location in the file system to another location. You can meet this using the <code>relativize</code> method. This method constructs a path originating from the original path and ending at the location specified by the passed-in path. The new path is <em>relative</em> to the original path.</p>
<p>For example, consider two relative paths defined as <code>joe</code> and <code>sally</code>:</p>
<div class="codeblock"><pre>
Path p1 = Paths.get("joe");
Path p2 = Paths.get("sally");
</pre></div>
<p>In the absence of any other information, it is assumed that <code>joe</code> and <code>sally</code> are siblings, meaning nodes that reside at the same level in the tree structure. To navigate from <code>joe</code> to <code>sally</code>, you would expect to first navigate one level up to the parent node and then down to <code>sally</code>:</p>
<div class="codeblock"><pre>
// Result is <strong>../sally</strong>
Path p1_to_p2 = p1.relativize(p2);
// Result is <strong>../joe</strong>
Path p2_to_p1 = p2.relativize(p1);
</pre></div>
<p>Consider a slightly more complicated example:</p>
<div class="codeblock"><pre>
Path p1 = Paths.get("home");
Path p3 = Paths.get("home/sally/bar");
// Result is <strong>sally/bar</strong>
Path p1_to_p3 = p1.relativize(p3);
// Result is <strong>../..</strong>
Path p3_to_p1 = p3.relativize(p1);
</pre></div>
<p>In this example, the two paths share the same node, <code>home</code>. To navigate from <code>home</code> to <code>bar</code>, you first navigate one level down to <code>sally</code> and then one more level down to <code>bar</code>. Navigating from <code>bar</code> to <code>home</code> requires moving up two levels.</p>
<p>A relative path cannot be constructed if only one of the paths includes a root element. If both paths include a root element, the capability to construct a relative path is system dependent.</p>
<p>The recursive 
<a class="SourceLink" target="_blank" href="examples/Copy.java" onclick="showCode('../../displayCode.html', 'examples/Copy.java'); return false;"><code><code>Copy</code></code></a> example uses the <code>relativize</code> and <code>resolve</code> methods.</p>
<h2><a name="compare" id="compare">Comparing Two Paths</a></h2>
<p>The <code>Path</code> class supports 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#equals-java.lang.Object-"><code>equals</code></a>, enabling you to test two paths for equality. The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#startsWith-java.nio.file.Path-"><code>startsWith</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#endsWith-java.nio.file.Path-"><code>endsWith</code></a> methods enable you to test whether a path begins or ends with a particular string. These methods are easy to use. For example:</p>
<div class="codeblock"><pre>
Path path = ...;
Path otherPath = ...;
Path beginning = Paths.get("/home");
Path ending = Paths.get("foo");

if (path.equals(otherPath)) {
    // <em>equality logic here</em>
} else if (path.startsWith(beginning)) {
    // <em>path begins with "/home"</em>
} else if (path.endsWith(ending)) {
    // <em>path ends with "foo"</em>
}
</pre></div>
<p>The <code>Path</code> class implements the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html"><code>Iterable</code></a> interface. The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html#iterator--"><code>iterator</code></a> method returns an object that enables you to iterate over the name elements in the path. The first element returned is that closest to the root in the directory tree. The following code snippet iterates over a path, printing each name element:</p>
<div class="codeblock"><pre>
Path path = ...;
for (Path name: path) {
    System.out.println(name);
}
</pre></div>
<p>The <code>Path</code> class also implements the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html"><code>Comparable</code></a> interface. You can compare <code>Path</code> objects by using <code>compareTo</code> which is useful for sorting.</p>
<p>You can also put <code>Path</code> objects into a <code>Collection</code>. See the 
<a class="TutorialLink" target="_top" href="../../collections/index.html">Collections</a> trail for more information about this powerful feature.</p>
<p>When you want to verify that two <code>Path</code> objects locate the same file, you can use the <code>isSameFile</code> method, as described in 
<a class="TutorialLink" target="_top" href="check.html#same">Checking Whether Two Paths Locate the Same File</a>.</p>
