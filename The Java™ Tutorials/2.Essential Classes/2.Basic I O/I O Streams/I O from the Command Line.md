<h1>I/O from the Command Line</h1>
<p>A program is often run from the command line and interacts with the user in the command line environment. The Java platform supports this kind of interaction in two ways: through the Standard Streams and through the Console.</p>
<h2>Standard Streams</h2>
<p>Standard Streams are a feature of many operating systems. By default, they read input from the keyboard and write output to the display. They also support I/O on files and between programs, but that feature is controlled by the command line interpreter, not the program.</p>
<p>The Java platform supports three Standard Streams: <em>Standard Input</em>, accessed through <code>System.in</code>; <em>Standard Output</em>, accessed through <code>System.out</code>; and <em>Standard Error</em>, accessed through <code>System.err</code>. These objects are defined automatically and do not need to be opened. Standard Output and Standard Error are both for output; having error output separately allows the user to divert regular output to a file and still be able to read error messages. For more information, refer to the documentation for your command line interpreter.</p>
<p>You might expect the Standard Streams to be character streams, but, for historical reasons, they are byte streams. <code>System.out</code> and <code>System.err</code> are defined as 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/PrintStream.html"><code>PrintStream</code></a> objects. Although it is technically a byte stream, <code>PrintStream</code> utilizes an internal character stream object to emulate many of the features of character streams.</p>
<p>By contrast, <code>System.in</code> is a byte stream with no character stream features. To use Standard Input as a character stream, wrap <code>System.in</code> in <code>InputStreamReader</code>.</p>
<div class="codeblock"><pre>
InputStreamReader cin = new InputStreamReader(System.in);
</pre></div>
<h2>The Console</h2>
<p>A more advanced alternative to the Standard Streams is the Console. This is a single, predefined object of type 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/io/Console.html"><code>Console</code></a> that has most of the features provided by the Standard Streams, and others besides. The Console is particularly useful for secure password entry. The Console object also provides input and output streams that are true character streams, through its <code>reader</code> and <code>writer</code> methods.</p>
<p>Before a program can use the Console, it must attempt to retrieve the Console object by invoking <code>System.console()</code>. If the Console object is available, this method returns it. If <code>System.console</code> returns <code>NULL</code>, then Console operations are not permitted, either because the OS doesn&#39;t support them or because the program was launched in a noninteractive environment.</p>
<p>The Console object supports secure password entry through its <code>readPassword</code> method. This method helps secure password entry in two ways. First, it suppresses echoing, so the password is not visible on the user&#39;s screen. Second, <code>readPassword</code> returns a character array, not a <code>String</code>, so the password can be overwritten, removing it from memory as soon as it is no longer needed.</p>
<p>The 
<a class="SourceLink" target="_blank" href="examples/Password.java" onclick="showCode('../../displayCode.html', 'examples/Password.java'); return false;"><code>Password</code></a> example is a prototype program for changing a user&#39;s password. It demonstrates several <code>Console</code> methods.</p>
<div class="codeblock"><pre>
import java.io.Console;
import java.util.Arrays;
import java.io.IOException;

public class Password {
    
    public static void main (String args[]) throws IOException {

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }

        String login = c.readLine("Enter your login: ");
        char [] oldPassword = c.readPassword("Enter your old password: ");

        if (verify(login, oldPassword)) {
            boolean noMatch;
            do {
                char [] newPassword1 = c.readPassword("Enter your new password: ");
                char [] newPassword2 = c.readPassword("Enter new password again: ");
                noMatch = ! Arrays.equals(newPassword1, newPassword2);
                if (noMatch) {
                    c.format("Passwords don't match. Try again.%n");
                } else {
                    change(login, newPassword1);
                    c.format("Password for %s changed.%n", login);
                }
                Arrays.fill(newPassword1, ' ');
                Arrays.fill(newPassword2, ' ');
            } while (noMatch);
        }

        Arrays.fill(oldPassword, ' ');
    }
    
    // Dummy change method.
    static boolean verify(String login, char[] password) {
        // This method always returns
        // true in this example.
        // Modify this method to verify
        // password according to your rules.
        return true;
    }

    // Dummy change method.
    static void change(String login, char[] password) {
        // Modify this method to change
        // password according to your rules.
    }
}
</pre></div>
<p>The <code>Password</code> class follows these steps:</p>
<ol>
<li>Attempt to retrieve the Console object. If the object is not available, abort.</li>
<li>Invoke <code>Console.readLine</code> to prompt for and read the user&#39;s login name.</li>
<li>Invoke <code>Console.readPassword</code> to prompt for and read the user&#39;s existing password.</li>
<li>Invoke <code>verify</code> to confirm that the user is authorized to change the password. (In this example, <code>verify</code> is a dummy method that always returns <code>true</code>.)</li>
<li>Repeat the following steps until the user enters the same password twice:
<ol style="list-style-type: lower-alpha">
<li>Invoke <code>Console.readPassword</code> twice to prompt for and read a new password.</li>
<li>If the user entered the same password both times, invoke <code>change</code> to change it. (Again, <code>change</code> is a dummy method.)</li>
<li>Overwrite both passwords with blanks.</li>
</ol>
</li>
<li>Overwrite the old password with blanks.</li>
</ol>
