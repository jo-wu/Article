<h1>Object Streams</h1>
<p>Just as data streams support I/O of primitive data types, object streams support I/O of objects. Most, but not all, standard classes support serialization of their objects. Those that do implement the marker interface 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html"><code>Serializable</code></a>.</p>
<p>The object stream classes are 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/ObjectInputStream.html"><code>ObjectInputStream</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/ObjectOutputStream.html"><code>ObjectOutputStream</code></a>. These classes implement 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/ObjectInput.html"><code>ObjectInput</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/ObjectOutput.html"><code>ObjectOutput</code></a>, which are subinterfaces of <code>DataInput</code> and <code>DataOutput</code>. That means that all the primitive data I/O methods covered in <a href="datastreams.html">Data Streams</a> are also implemented in object streams. So an object stream can contain a mixture of primitive and object values. The 
<a class="SourceLink" target="_blank" href="examples/ObjectStreams.java" onclick="showCode('../../displayCode.html', 'examples/ObjectStreams.java'); return false;"><code>ObjectStreams</code></a> example illustrates this. <code>ObjectStreams</code> creates the same application as <code>DataStreams</code>, with a couple of changes. First, prices are now 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html"><code>BigDecimal</code></a>objects, to better represent fractional values. Second, a 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/util/Calendar.html"><code>Calendar</code></a> object is written to the data file, indicating an invoice date.</p>
<p>If <code>readObject()</code> doesn&#39;t return the object type expected, attempting to cast it to the correct type may throw a 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/ClassNotFoundException.html"><code>ClassNotFoundException</code></a>. In this simple example, that can&#39;t happen, so we don&#39;t try to catch the exception. Instead, we notify the compiler that we&#39;re aware of the issue by adding <code>ClassNotFoundException</code> to the <code>main</code> method&#39;s <code>throws</code> clause.</p>
<h2>Output and Input of Complex Objects</h2>
<p>The <code>writeObject</code> and <code>readObject</code> methods are simple to use, but they contain some very sophisticated object management logic. This isn&#39;t important for a class like Calendar, which just encapsulates primitive values. But many objects contain references to other objects. If <code>readObject</code> is to reconstitute an object from a stream, it has to be able to reconstitute all of the objects the original object referred to. These additional objects might have their own references, and so on. In this situation, <code>writeObject</code> traverses the entire web of object references and writes all objects in that web onto the stream. Thus a single invocation of <code>writeObject</code> can cause a large number of objects to be written to the stream.</p>
<p>This is demonstrated in the following figure, where <code>writeObject</code> is invoked to write a single object named <b>a</b>. This object contains references to objects <b>b</b> and <b>c</b>, while <b>b</b> contains references to <b>d</b> and <b>e</b>. Invoking <code>writeobject(a)</code> writes not just <b>a</b>, but all the objects necessary to reconstitute <b>a</b>, so the other four objects in this web are written also. When <b>a</b> is read back by <code>readObject</code>, the other four objects are read back as well, and all the original object references are preserved.</p>
<center><img src="../../figures/essential/io-trav.gif" width="456" height="195" align="bottom" alt="I/O of multiple referred-to objects" /></p><p class="FigureCaption">I/O of multiple referred-to objects</p></center><p>You might wonder what happens if two objects on the same stream both contain references to a single object. Will they both refer to a single object when they&#39;re read back? The answer is &quot;yes.&quot; A stream can only contain one copy of an object, though it can contain any number of references to it. Thus if you explicitly write an object to a stream twice, you&#39;re really writing only the reference twice. For example, if the following code writes an object <code>ob</code> twice to a stream:</p>
<div class="codeblock"><pre>
Object ob = new Object();
out.writeObject(ob);
out.writeObject(ob);
</pre></div>
<p>Each <code>writeObject</code> has to be matched by a <code>readObject</code>, so the code that reads the stream back will look something like this:</p>
<div class="codeblock"><pre>
Object ob1 = in.readObject();
Object ob2 = in.readObject();
</pre></div>
<p>This results in two variables, <code>ob1</code> and <code>ob2</code>, that are references to a single object.</p>
<p>However, if a single object is written to two different streams, it is effectively duplicated &#151; a single program reading both streams back will see two distinct objects.</p>
