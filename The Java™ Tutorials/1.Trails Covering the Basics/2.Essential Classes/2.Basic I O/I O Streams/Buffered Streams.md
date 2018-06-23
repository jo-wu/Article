<h1>Buffered Streams</h1>
<p>Most of the examples we&#39;ve seen so far use <i>unbuffered</i> I/O. This means each read or write request is handled directly by the underlying OS. This can make a program much less efficient, since each such request often triggers disk access, network activity, or some other operation that is relatively expensive.</p>
<p>To reduce this kind of overhead, the Java platform implements <i>buffered</i> I/O streams. Buffered input streams read data from a memory area known as a <i>buffer</i>; the native input API is called only when the buffer is empty. Similarly, buffered output streams write data to a buffer, and the native output API is called only when the buffer is full.</p>
<p>A program can convert an unbuffered stream into a buffered stream using the wrapping idiom we&#39;ve used several times now, where the unbuffered stream object is passed to the constructor for a buffered stream class. Here&#39;s how you might modify the constructor invocations in the <code>CopyCharacters</code> example to use buffered I/O:</p>
<div class="codeblock"><pre>
inputStream = new BufferedReader(new FileReader("xanadu.txt"));
outputStream = new BufferedWriter(new FileWriter("characteroutput.txt"));
</pre></div>
<p>There are four buffered stream classes used to wrap unbuffered streams: 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/BufferedInputStream.html"><code>BufferedInputStream</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/BufferedOutputStream.html"><code>BufferedOutputStream</code></a> create buffered byte streams, while 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html"><code>BufferedReader</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/BufferedWriter.html"><code>BufferedWriter</code></a> create buffered character streams.</p>
<h2>Flushing Buffered Streams</h2>
<p>It often makes sense to write out a buffer at critical points, without waiting for it to fill. This is known as <i>flushing</i> the buffer.</p>
<p>Some buffered output classes support <i>autoflush</i>, specified by an optional constructor argument. When autoflush is enabled, certain key events cause the buffer to be flushed. For example, an autoflush <code>PrintWriter</code> object flushes the buffer on every invocation of <code>println</code> or <code>format</code>. See <a href="formatting.html">Formatting</a> for more on these methods.</p>
<p>To flush a stream manually, invoke its <code>flush</code> method. The <code>flush</code> method is valid on any output stream, but has no effect unless the stream is buffered.</p>
