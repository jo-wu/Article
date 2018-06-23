<h1>File I/O (Featuring NIO.2)</h1>
<!-- File I/O (Featuring NIO.2) -->
<hr />
<p><strong>Note:</strong> This tutorial reflects the file I/O mechanism introduced in the JDK 7 release. The Java SE 6 version of the File I/O tutorial was brief, but you can download the 
<a class="OutsideLink" target="_blank" href="http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-tutorials-419421.html#tutorial-2008_03_14-oth-JPR">Java SE Tutorial 2008-03-14</a> version of the tutorial which contains the earlier File I/O content.</p>
<hr />
<p>The <code>java.nio.file</code> package and its related package, <code>java.nio.file.attribute</code>, provide comprehensive support for file I/O and for accessing the default file system. Though the API has many classes, you need to focus on only a few entry points. You will see that this API is very intuitive and easy to use.</p>
<p>The tutorial starts by asking 
<a class="TutorialLink" target="_top" href="path.html">what is a path?</a> Then, the 
<a class="TutorialLink" target="_top" href="pathClass.html">Path class</a>, the primary entry point for the package, is introduced. Methods in the <code>Path</code> class relating to 
<a class="TutorialLink" target="_top" href="pathOps.html">syntactic operations</a> are explained. The tutorial then moves on to the other primary class in the package, the <code>Files</code> class, which contains methods that deal with file operations. First, some concepts common to many 
<a class="TutorialLink" target="_top" href="fileOps.html">file operations</a> are introduced. The tutorial then covers methods for 
<a class="TutorialLink" target="_top" href="check.html">checking</a>, 
<a class="TutorialLink" target="_top" href="delete.html">deleting</a>, 
<a class="TutorialLink" target="_top" href="copy.html">copying</a>, and 
<a class="TutorialLink" target="_top" href="move.html">moving</a> files.</p>
<p>The tutorial shows how 
<a class="TutorialLink" target="_top" href="fileAttr.html">metadata</a> is managed, before moving on to 
<a class="TutorialLink" target="_top" href="file.html">file I/O</a> and 
<a class="TutorialLink" target="_top" href="dirs.html">directory I/O</a>. 
<a class="TutorialLink" target="_top" href="rafs.html">Random access files</a> are explained and issues specific to 
<a class="TutorialLink" target="_top" href="links.html">symbolic and hard links</a> are examined.</p>
<p>Next, some of the very powerful, but more advanced, topics are covered. First, the capability to 
<a class="TutorialLink" target="_top" href="walk.html">recursively walk the file tree</a> is demonstrated, followed by information about how to 
<a class="TutorialLink" target="_top" href="find.html">search for files using wild cards</a>. Next, how to 
<a class="TutorialLink" target="_top" href="notification.html">watch a directory for changes</a> is explained and demonstrated. Then, 
<a class="TutorialLink" target="_top" href="misc.html">methods that didn't fit elsewhere</a> are given some attention.</p>
<p>Finally, if you have file I/O code written prior to the Java SE 7 release, there is a 
<a class="TutorialLink" target="_top" href="legacy.html#mapping">map from the old API to the new API</a>, as well as important information about the <code>File.toPath</code> method for developers who would like to 
<a class="TutorialLink" target="_top" href="legacy.html#interop">leverage the new API without rewriting existing code</a>.</p>
