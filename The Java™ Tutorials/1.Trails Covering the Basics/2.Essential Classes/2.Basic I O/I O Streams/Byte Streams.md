<h1>Byte Streams</h1>
<p>Programs use <em>byte streams</em> to perform input and output of 8-bit bytes. All byte stream classes are descended from 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html"><code>InputStream</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html"><code>OutputStream</code></a>.</p>
<p>There are many byte stream classes. To demonstrate how byte streams work, we&#39;ll focus on the file I/O byte streams, 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/FileInputStream.html"><code>FileInputStream</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/FileOutputStream.html"><code>FileOutputStream</code></a>. Other kinds of byte streams are used in much the same way; they differ mainly in the way they are constructed.</p>
<h2>Using Byte Streams</h2>
<p>We&#39;ll explore <code>FileInputStream</code> and <code>FileOutputStream</code> by examining an example program named 
<a class="SourceLink" target="_blank" href="examples/CopyBytes.java" onclick="showCode('../../displayCode.html', 'examples/CopyBytes.java'); return false;"><code>CopyBytes</code></a>, which uses byte streams to copy <code>xanadu.txt</code>, one byte at a time.</p>
<div class="codeblock"><pre>
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyBytes {
    public static void main(String[] args) throws IOException {

        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream("xanadu.txt");
            out = new FileOutputStream("outagain.txt");
            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
</pre></div>
<p><code>CopyBytes</code> spends most of its time in a simple loop that reads the input stream and writes the output stream, one byte at a time, as shown in 
<span id="figure:byteStream.gif">the following figure</span>.</p>
<center><img src="../../figures/essential/byteStream.gif" width="365" height="299" align="bottom" alt="Simple byte stream input and output." /></p><p class="FigureCaption">Simple byte stream input and output.</p></center>
<h2>Always Close Streams</h2>
<p>Closing a stream when it&#39;s no longer needed is very important &#151; so important that <code>CopyBytes</code> uses a <code>finally</code> block to guarantee that both streams will be closed even if an error occurs. This practice helps avoid serious resource leaks.</p>
<p>One possible error is that <code>CopyBytes</code> was unable to open one or both files. When that happens, the stream variable corresponding to the file never changes from its initial <code>null</code> value. That&#39;s why <code>CopyBytes</code> makes sure that each stream variable contains an object reference before invoking <code>close</code>.</p>
<h2>When Not to Use Byte Streams</h2>
<p><code>CopyBytes</code> seems like a normal program, but it actually represents a kind of low-level I/O that you should avoid. Since <code>xanadu.txt</code> contains character data, the best approach is to use <a href="charstreams.html">character streams</a>, as discussed in the next section. There are also streams for more complicated data types. Byte streams should only be used for the most primitive I/O.</p>
<p>So why talk about byte streams? Because all other stream types are built on byte streams.</p>
> 
