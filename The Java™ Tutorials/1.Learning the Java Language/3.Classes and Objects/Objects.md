<h1>Objects</h1>
<!-- Objects -->
<p>A typical Java program creates many objects, which as you know, interact by invoking methods. Through these object interactions, a program can carry out various tasks, such as implementing a GUI, running an animation, or sending and receiving information over a network. Once an object has completed the work for which it was created, its resources are recycled for use by other objects.</p>
<p>Here&#39;s a small program, called 
<a class="SourceLink" target="_blank" href="examples/CreateObjectDemo.java" onclick="showCode('../../displayCode.html', 'examples/CreateObjectDemo.java'); return false;"><code>CreateObjectDemo</code></a>, that creates three objects: one 
<a class="SourceLink" target="_blank" href="examples/Point.java" onclick="showCode('../../displayCode.html', 'examples/Point.java'); return false;"><code>Point</code></a> object and two 
<a class="SourceLink" target="_blank" href="examples/Rectangle.java" onclick="showCode('../../displayCode.html', 'examples/Rectangle.java'); return false;"><code>Rectangle</code></a> objects. You will need all three source files to compile this program.</p>
<div class="codeblock"><pre>

public class CreateObjectDemo {

    public static void main(String[] args) {
		
        // Declare and create a point object and two rectangle objects.
        Point originOne = new Point(23, 94);
        Rectangle rectOne = new Rectangle(originOne, 100, 200);
        Rectangle rectTwo = new Rectangle(50, 100);
		
        // display rectOne's width, height, and area
        System.out.println(&quot;Width of rectOne: &quot; + rectOne.width);
        System.out.println(&quot;Height of rectOne: &quot; + rectOne.height);
        System.out.println(&quot;Area of rectOne: &quot; + rectOne.getArea());
		
        // set rectTwo's position
        rectTwo.origin = originOne;
		
        // display rectTwo's position
        System.out.println(&quot;X Position of rectTwo: &quot; + rectTwo.origin.x);
        System.out.println(&quot;Y Position of rectTwo: &quot; + rectTwo.origin.y);
		
        // move rectTwo and display its new position
        rectTwo.move(40, 72);
        System.out.println(&quot;X Position of rectTwo: &quot; + rectTwo.origin.x);
        System.out.println(&quot;Y Position of rectTwo: &quot; + rectTwo.origin.y);
    }
}
</pre></div>
<p>This program creates, manipulates, and displays information about various objects. Here&#39;s the output:</p>
<div class="codeblock"><pre>
Width of rectOne: 100
Height of rectOne: 200
Area of rectOne: 20000
X Position of rectTwo: 23
Y Position of rectTwo: 94
X Position of rectTwo: 40
Y Position of rectTwo: 72
</pre></div>
<p>The following three sections use the above example to describe the life cycle of an object within a program. From them, you will learn how to write code that creates and uses objects in your own programs. You will also learn how the system cleans up after an object when its life has ended.</p>

