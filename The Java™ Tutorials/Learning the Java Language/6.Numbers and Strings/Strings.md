<h1>Strings</h1>
<!-- Strings -->
<p>Strings, which are widely used in Java programming, are a sequence of characters. In the Java programming language, strings are objects.</p>
<p>The Java platform provides the 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/String.html"><code>String</code> </a> class to create and manipulate strings.</p>
<h2>Creating Strings</h2>
<p>The most direct way to create a string is to write:</p>
<div class="codeblock"><pre>
String greeting = "Hello world!";
</pre></div>
<p>In this case, &quot;Hello world!&quot; is a <i>string literal</i>&#151;a series of characters in your code that is enclosed in double quotes. Whenever it encounters a string literal in your code, the compiler creates a <code>String</code> object with its value&#151;in this case, <code>Hello world!</code>.</p>
<p>As with any other object, you can create <code>String</code> objects by using the <code>new</code> keyword and a constructor. The <code>String</code> class has thirteen constructors that allow you to provide the initial value of the string using different sources, such as an array of characters:</p>
<div class="codeblock"><pre>
char[] helloArray = { 'h', 'e', 'l', 'l', 'o', '.' };
String helloString = new String(helloArray);
System.out.println(helloString);
</pre></div>
<p>The last line of this code snippet displays <code>hello</code>.</p>
<div class="note"><hr /><strong>Note:</strong>&nbsp;The <code>String</code> class is immutable, so that once it is created a <code>String</code> object cannot be changed. The <code>String</code> class has a number of methods, some of which will be discussed below, that appear to modify strings. Since strings are immutable, what these methods really do is create and return a new string that contains the result of the operation.
<hr /></div>
<h2>String Length</h2>
<p>Methods used to obtain information about an object are known as <em>accessor methods</em>. One accessor method that you can use with strings is the <code>length()</code> method, which returns the number of characters contained in the string object. After the following two lines of code have been executed, <code>len</code> equals 17:</p>
<div class="codeblock"><pre>
String palindrome = "Dot saw I was Tod";
int len = palindrome.length();
</pre></div>
<p>A <i>palindrome</i> is a word or sentence that is symmetric&#151;it is spelled the same forward and backward, ignoring case and punctuation. Here is a short and inefficient program to reverse a palindrome string. It invokes the <code>String</code> method <code>charAt(i)</code>, which returns the i<sup>th</sup> character in the string, counting from 0.</p>
<div class="codeblock"><pre>

public class StringDemo {
    public static void main(String[] args) {
        String palindrome = &quot;Dot saw I was Tod&quot;;
        int len = palindrome.length();
        char[] tempCharArray = new char[len];
        char[] charArray = new char[len];
        
        // put original string in an 
        // array of chars
        for (int i = 0; i &lt; len; i++) {
            tempCharArray[i] = 
                palindrome.charAt(i);
        } 
        
        // reverse array of chars
        for (int j = 0; j &lt; len; j++) {
            charArray[j] =
                tempCharArray[len - 1 - j];
        }
        
        String reversePalindrome =
            new String(charArray);
        System.out.println(reversePalindrome);
    }
}
</pre></div>
<p>Running the program produces this output:</p>
<div class="codeblock"><pre>
doT saw I was toD
</pre></div>
<p>To accomplish the string reversal, the program had to convert the string to an array of characters (first <code>for</code> loop), reverse the array into a second array (second <code>for</code> loop), and then convert back to a string. The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/String.html"><code>String</code> </a> class includes a method, <code>getChars()</code>, to convert a string, or a portion of a string, into an array of characters so we could replace the first <code>for</code> loop in the program above with</p>
<div class="codeblock"><pre>
palindrome.getChars(0, len, tempCharArray, 0);
</pre></div>
<h2>Concatenating Strings</h2>
<p>The <code>String</code> class includes a method for concatenating two strings:</p>
<div class="codeblock"><pre>
string1.concat(string2); 
</pre></div>
<p>This returns a new string that is string1 with string2 added to it at the end.</p>
<p>You can also use the <code>concat()</code> method with string literals, as in:</p>
<div class="codeblock"><pre>
"My name is ".concat("Rumplestiltskin");
</pre></div>
<p>Strings are more commonly concatenated with the <code style="font-weight: bold">+</code> operator, as in</p>
<div class="codeblock"><pre>
"Hello," + " world" + "!"
</pre></div>
<p>which results in</p>
<div class="codeblock"><pre>
"Hello, world!"
</pre></div>
<p>The <code style="font-weight: bold">+</code> operator is widely used in <code>print</code> statements. For example:</p>
<div class="codeblock"><pre>
String string1 = "saw I was ";
System.out.println("Dot " + string1 + "Tod");
</pre></div>
<p>which prints</p>
<div class="codeblock"><pre>
Dot saw I was Tod
</pre></div>
<p>Such a concatenation can be a mixture of any objects. For each object that is not a <code>String</code>, its <code>toString()</code> method is called to convert it to a <code>String</code>.</p>
<div class="note"><hr /><strong>Note:</strong>&nbsp;The Java programming language does not permit literal strings to span lines in source files, so you must use the <code>+</code> concatenation operator at the end of each line in a multi-line string. For example:
<div class="codeblock"><pre>
String quote = 
    "Now is the time for all good " +
    "men to come to the aid of their country.";
</pre></div>
<p>Breaking strings between lines using the <code>+</code> concatenation operator is, once again, very common in <code>print</code> statements.</p>
<hr /></div>
<h2>Creating Format Strings</h2>
<p>You have seen the use of the <code>printf()</code> and <code>format()</code> methods to print output with formatted numbers. The <code>String</code> class has an equivalent class method, <code>format()</code>, that returns a <code>String</code> object rather than a <code>PrintStream</code> object.</p>
<p>Using <code>String&#39;s</code> static <code>format()</code> method allows you to create a formatted string that you can reuse, as opposed to a one-time print statement. For example, instead of</p>
<div class="codeblock"><pre>
System.out.printf("The value of the float " +
                  "variable is %f, while " +
                  "the value of the " + 
                  "integer variable is %d, " +
                  "and the string is %s", 
                  floatVar, intVar, stringVar); 
</pre></div>
<p>you can write</p>
<div class="codeblock"><pre>
String fs;
fs = String.format("The value of the float " +
                   "variable is %f, while " +
                   "the value of the " + 
                   "integer variable is %d, " +
                   " and the string is %s",
                   floatVar, intVar, stringVar);
System.out.println(fs);
</pre></div>
