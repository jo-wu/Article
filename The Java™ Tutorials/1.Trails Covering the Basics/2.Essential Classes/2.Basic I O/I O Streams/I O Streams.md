<h1>I/O Streams</h1>
<p>An <em>I/O Stream</em> represents an input source or an output destination. A stream can represent many different kinds of sources and destinations, including disk files, devices, other programs, and memory arrays.</p>
<p>Streams support many different kinds of data, including simple bytes, primitive data types, localized characters, and objects. Some streams simply pass on data; others manipulate and transform the data in useful ways.</p>
<p>No matter how they work internally, all streams present the same simple model to programs that use them: A stream is a sequence of data. A program uses an <i>input stream</i> to read data from a source, one item at a time:</p>
<center><img src="../../figures/essential/io-ins.gif" width="488" height="155" align="bottom" alt="Reading information into a program." /></p><p class="FigureCaption">Reading information into a program.</p></center><p>A program uses an <i>output stream</i> to write data to a destination, one item at time:</p>
<center><img src="../../figures/essential/io-outs.gif" width="494" height="157" align="bottom" alt="Writing information from a program." /></p><p class="FigureCaption">Writing information from a program.</p></center><p>In this lesson, we&#39;ll see streams that can handle all kinds of data, from primitive values to advanced objects.</p>
<p>The data source and data destination pictured above can be anything that holds, generates, or consumes data. Obviously this includes disk files, but a source or destination can also be another program, a peripheral device, a network socket, or an array.</p>
<p>In the next section, we&#39;ll use the most basic kind of streams, byte streams, to demonstrate the common operations of Stream I/O. For sample input, we&#39;ll use the example file 
<a class="SourceLink" target="_blank" href="examples/xanadu.txt" onclick="showCode('../../displayCode.html', 'examples/xanadu.txt'); return false;"><code>xanadu.txt</code></a>, which contains the following verse:</p>
<div class="codeblock"><pre>
In Xanadu did Kubla Khan
A stately pleasure-dome decree:
Where Alph, the sacred river, ran
Through caverns measureless to man
Down to a sunless sea.
</pre></div>

