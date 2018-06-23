<h1>Inheritance</h1>
<!-- Inheritance -->
<p>In the preceding 
lessons,
 you have seen <i>inheritance</i> mentioned several times. In the Java language, classes can be <i>derived</i> from other classes, thereby <i>inheriting</i> fields and methods from those classes.</p>
<div class="note"><hr /><strong>Definitions:</strong>&nbsp;A class that is derived from another class is called a <i>subclass</i> (also a <i>derived class</i>, <i>extended class</i>, or <i>child class</i>). The class from which the subclass is derived is called a <i>superclass</i> (also a <i>base class</i> or a <i>parent class</i>).<br /><br />
Excepting <code>Object</code>, which has no superclass, every class has one and only one direct superclass (single inheritance). In the absence of any other explicit superclass, every class is implicitly a subclass of <code>Object</code>.<br /><br />
Classes can be derived from classes that are derived from classes that are derived from classes, and so on, and ultimately derived from the topmost class, <code>Object</code>. Such a class is said to be <i>descended</i> from all the classes in the inheritance chain stretching back to <code>Object</code>.
<hr /></div>
<p>The idea of inheritance is simple but powerful: When you want to create a new class and there is already a class that includes some of the code that you want, you can derive your new class from the existing class. In doing this, you can reuse the fields and methods of the existing class without having to write (and debug!) them yourself.</p>
<p>A subclass inherits all the <i>members</i> (fields, methods, and nested classes) from its superclass. Constructors are not members, so they are not inherited by subclasses, but the constructor of the superclass can be invoked from the subclass.</p>
<h2>The Java Platform Class Hierarchy</h2>
<p>The 
<a class="APILink" target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html"><code>Object</code></a> class, defined in the <code>java.lang</code> package, defines and implements behavior common to all classes&#151;including the ones that you write. In the Java platform, many classes derive directly from <code>Object</code>, other classes derive from some of those classes, and so on, forming a hierarchy of classes.</p>
<center><img src="../../figures/java/classes-object.gif" width="560" height="241" align="bottom" alt="All Classes in the Java Platform are Descendants of Object" /></p><p class="FigureCaption">All Classes in the Java Platform are Descendants of Object</p></center><p>At the top of the hierarchy, <code>Object</code> is the most general of all classes. Classes near the bottom of the hierarchy provide more specialized behavior.</p>
<h2>An Example of Inheritance</h2>
<p>Here is the sample code for a possible implementation of a <code>Bicycle</code> class that was presented in 
the Classes and Objects lesson: 
</p>
<div class="codeblock"><pre>
public class Bicycle {
        
    // <b>the Bicycle class has three <i>fields</i></b>
    public int cadence;
    public int gear;
    public int speed;
        
    // <b>the Bicycle class has one <i>constructor</i></b>
    public Bicycle(int startCadence, int startSpeed, int startGear) {
        gear = startGear;
        cadence = startCadence;
        speed = startSpeed;
    }
        
    // <b>the Bicycle class has four <i>methods</i></b>
    public void setCadence(int newValue) {
        cadence = newValue;
    }
        
    public void setGear(int newValue) {
        gear = newValue;
    }
        
    public void applyBrake(int decrement) {
        speed -= decrement;
    }
        
    public void speedUp(int increment) {
        speed += increment;
    }
        
}
</pre></div>
<p>A class declaration for a <code>MountainBike</code> class that is a subclass of <code>Bicycle</code> might look like this:</p>
<div class="codeblock"><pre>
public class MountainBike extends Bicycle {
        
    // <b>the MountainBike subclass adds one <i>field</i></b>
    public int seatHeight;

    // <b>the MountainBike subclass has one <i>constructor</i></b>
    public MountainBike(int startHeight,
                        int startCadence,
                        int startSpeed,
                        int startGear) {
        super(startCadence, startSpeed, startGear);
        seatHeight = startHeight;
    }   
        
    // <b>the MountainBike subclass adds one <i>method</i></b>
    public void setHeight(int newValue) {
        seatHeight = newValue;
    }   
}
</pre></div>
<p><code>MountainBike</code> inherits all the fields and methods of <code>Bicycle</code> and adds the field <code>seatHeight</code> and a method to set it. Except for the constructor, it is as if you had written a new <code>MountainBike</code> class entirely from scratch, with four fields and five methods. However, you didn&#39;t have to do all the work. This would be especially valuable if the methods in the <code>Bicycle</code> class were complex and had taken substantial time to debug.</p>
<h2>What You Can Do in a Subclass</h2>
<p>A subclass inherits all of the <i>public</i> and <i>protected</i> members of its parent, no matter what package the subclass is in. If the subclass is in the same package as its parent, it also inherits the <i>package-private</i> members of the parent. You can use the inherited members as is, replace them, hide them, or supplement them with new members:</p>
<ul>
<li>The inherited fields can be used directly, just like any other fields.</li>
<li>You can declare a field in the subclass with the same name as the one in the superclass, thus <i>hiding</i> it (not recommended).</li>
<li>You can declare new fields in the subclass that are not in the superclass.</li>
<li>The inherited methods can be used directly as they are.</li>
<li>You can write a new <i>instance</i> method in the subclass that has the same signature as the one in the superclass, thus <i>overriding</i> it.</li>
<li>You can write a new <i>static</i> method in the subclass that has the same signature as the one in the superclass, thus <i>hiding</i> it.</li>
<li>You can declare new methods in the subclass that are not in the superclass.</li>
<li>You can write a subclass constructor that invokes the constructor of the superclass, either implicitly or by using the keyword <code>super</code>.</li>
</ul>
<p>The following sections in this 
lesson
will expand on these topics.</p>
<h2>Private Members in a Superclass</h2>
<p>A subclass does not inherit the <code>private</code> members of its parent class. However, if the superclass has public or protected methods for accessing its private fields, these can also be used by the subclass.</p>
<p>A nested class has access to all the private members of its enclosing class&#151;both fields and methods. Therefore, a public or protected nested class inherited by a subclass has indirect access to all of the private members of the superclass.</p>
<h2>Casting Objects</h2>
<p>We have seen that an object is of the data type of the class from which it was instantiated. For example, if we write</p>
<div class="codeblock"><pre>
public MountainBike myBike = new MountainBike();
</pre></div>
<p>then <code>myBike</code> is of type <code>MountainBike</code>.</p>
<p><code>MountainBike</code> is descended from <code>Bicycle</code> and <code>Object</code>. Therefore, a <code>MountainBike</code> is a <code>Bicycle</code> and is also an <code>Object</code>, and it can be used wherever <code>Bicycle</code> or <code>Object</code> objects are called for.</p>
<p>The reverse is not necessarily true: a <code>Bicycle</code> <i>may be</i> a <code>MountainBike</code>, but it isn&#39;t necessarily. Similarly, an <code>Object</code> <i>may be</i> a <code>Bicycle</code> or a <code>MountainBike</code>, but it isn&#39;t necessarily.</p>
<p><i>Casting</i> shows the use of an object of one type in place of another type, among the objects permitted by inheritance and implementations. For example, if we write</p>
<div class="codeblock"><pre>
Object obj = new MountainBike();
</pre></div>
<p>then <code>obj</code> is both an <code>Object</code> and a <code>MountainBike</code> (until such time as <code>obj</code> is assigned another object that is <i>not</i> a <code>MountainBike</code>). This is called <i>implicit casting</i>.</p>
<p>If, on the other hand, we write</p>
<div class="codeblock"><pre>
MountainBike myBike = obj;
</pre></div>
<p>we would get a compile-time error because <code>obj</code> is not known to the compiler to be a <code>MountainBike</code>. However, we can <i>tell</i> the compiler that we promise to assign a <code>MountainBike</code> to <code>obj</code> by <i>explicit casting:</i></p>
<div class="codeblock"><pre>
MountainBike myBike = (MountainBike)obj;
</pre></div>
<p>This cast inserts a runtime check that <code>obj</code> is assigned a <code>MountainBike</code> so that the compiler can safely assume that <code>obj</code> is a <code>MountainBike</code>. If <code>obj</code> is not a <code>MountainBike</code> at runtime, an exception will be thrown.</p>
<div class="note"><hr /><strong>Note:</strong>&nbsp;You can make a logical test as to the type of a particular object using the <code>instanceof</code> operator. This can save you from a runtime error owing to an improper cast. For example:
<div class="codeblock"><pre>
if (obj instanceof MountainBike) {
    MountainBike myBike = (MountainBike)obj;
}
</pre></div>
<p>Here the <code>instanceof</code> operator verifies that <code>obj</code> refers to a <code>MountainBike</code> so that we can make the cast with knowledge that there will be no runtime exception thrown.</p>
<hr /></div>


