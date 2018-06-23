<h1>Character Streams</h1>
<p>The Java platform stores character values using Unicode conventions. Character stream I/O automatically translates this internal format to and from the local character set. In Western locales, the local character set is usually an 8-bit superset of ASCII.</p>
<p>For most applications, I/O with character streams is no more complicated than I/O with byte streams. Input and output done with stream classes automatically translates to and from the local character set. A program that uses character streams in place of byte streams automatically adapts to the local character set and is ready for internationalization &#151; all without extra effort by the programmer.</p>
<p>If internationalization isn&#39;t a priority, you can simply use the character stream classes without paying much attention to character set issues. Later, if internationalization becomes a priority, your program can be adapted without extensive recoding. See the 
<a class="TutorialLink" target="_top" href="../../i18n/index.html">Internationalization</a> trail for more information.</p>
<h2>Using Character Streams</h2>
<p>All character stream classes are descended from 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html"><code>Reader</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/Writer.html"><code>Writer</code></a>. As with byte streams, there are character stream classes that specialize in file I/O: 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/FileReader.html"><code>FileReader</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/FileWriter.html"><code>FileWriter</code></a>. The 
<a class="SourceLink" target="_blank" href="examples/CopyCharacters.java" onclick="showCode('../../displayCode.html', 'examples/CopyCharacters.java'); return false;"><code>CopyCharacters</code></a> example illustrates these classes.</p>
<div class="codeblock"><pre>
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyCharacters {
    public static void main(String[] args) throws IOException {

        FileReader inputStream = null;
        FileWriter outputStream = null;

        try {
            inputStream = new FileReader("xanadu.txt");
            outputStream = new FileWriter("characteroutput.txt");

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
</pre></div>
<p><code>CopyCharacters</code> is very similar to <code>CopyBytes</code>. The most important difference is that <code>CopyCharacters</code> uses <code>FileReader</code> and <code>FileWriter</code> for input and output in place of <code>FileInputStream</code> and <code>FileOutputStream</code>. Notice that both <code>CopyBytes</code> and <code>CopyCharacters</code> use an <code>int</code> variable to read to and write from. However, in <code>CopyCharacters</code>, the <code>int</code> variable holds a character value in its last 16 bits; in <code>CopyBytes</code>, the <code>int</code> variable holds a <code>byte</code> value in its last 8 bits.</p>
<h3>Character Streams that Use Byte Streams</h3>
<p>Character streams are often &quot;wrappers&quot; for byte streams. The character stream uses the byte stream to perform the physical I/O, while the character stream handles translation between characters and bytes. <code>FileReader</code>, for example, uses <code>FileInputStream</code>, while <code>FileWriter</code> uses <code>FileOutputStream</code>.</p>
<p>There are two general-purpose byte-to-character &quot;bridge&quot; streams: 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/InputStreamReader.html"><code>InputStreamReader</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/OutputStreamWriter.html"><code>OutputStreamWriter</code></a>. Use them to create character streams when there are no prepackaged character stream classes that meet your needs. The 
<a class="TutorialLink" target="_top" href="../../networking/sockets/readingWriting.html">sockets lesson</a> in the 
<a class="TutorialLink" target="_top" href="../../networking/index.html">networking trail</a> shows how to create character streams from the byte streams provided by socket classes.</p>
<h2>Line-Oriented I/O</h2>
<p>Character I/O usually occurs in bigger units than single characters. One common unit is the line: a string of characters with a line terminator at the end. A line terminator can be a carriage-return/line-feed sequence (<code>&quot;\r\n&quot;</code>), a single carriage-return (<code>&quot;\r&quot;</code>), or a single line-feed (<code>&quot;\n&quot;</code>). Supporting all possible line terminators allows programs to read text files created on any of the widely used operating systems.</p>
<p>Let&#39;s modify the <code>CopyCharacters</code> example to use line-oriented I/O. To do this, we have to use two classes we haven&#39;t seen before, 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html"><code>BufferedReader</code></a> and 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/PrintWriter.html"><code>PrintWriter</code></a>. We&#39;ll explore these classes in greater depth in <a href="buffers.html">Buffered I/O</a> and <a href="formatting.html">Formatting</a>. Right now, we&#39;re just interested in their support for line-oriented I/O.</p>
<p>The 
<a class="SourceLink" target="_blank" href="examples/CopyLines.java" onclick="showCode('../../displayCode.html', 'examples/CopyLines.java'); return false;"><code>CopyLines</code></a> example invokes <code>BufferedReader.readLine</code> and <code>PrintWriter.println</code> to do input and output one line at a time.</p>
<div class="codeblock"><pre>
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

public class CopyLines {
    public static void main(String[] args) throws IOException {

        BufferedReader inputStream = null;
        PrintWriter outputStream = null;

        try {
            inputStream = new BufferedReader(new FileReader("xanadu.txt"));
            outputStream = new PrintWriter(new FileWriter("characteroutput.txt"));

            String l;
            while ((l = inputStream.readLine()) != null) {
                outputStream.println(l);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
</pre></div>
<p>Invoking <code>readLine</code> returns a line of text with the line. <code>CopyLines</code> outputs each line using <code>println</code>, which appends the line terminator for the current operating system. This might not be the same line terminator that was used in the input file.</p>
<p>There are many ways to structure text input and output beyond characters and lines. For more information, see <a href="scanfor.html">Scanning and Formatting</a>.</p>
