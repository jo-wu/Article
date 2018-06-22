<h1>What Is a Class?</h1>
<p>In the real world, you&#39;ll often find many individual objects all of the same kind. There may be thousands of other bicycles in existence, all of the same make and model. Each bicycle was built from the same set of blueprints and therefore contains the same components. In object-oriented terms, we say that your bicycle is an <i>instance</i> of the <i>class of objects</i> known as bicycles. A <i>class</i> is the blueprint from which individual objects are created.</p>
<p>The following 
<a class="SourceLink" target="_blank" href="examples/Bicycle.java" onclick="showCode('../../displayCode.html', 'examples/Bicycle.java'); return false;"><code>Bicycle</code></a> class is one possible implementation of a bicycle:</p>
<div class="codeblock"><pre>

class Bicycle {

    int cadence = 0;
    int speed = 0;
    int gear = 1;

    void changeCadence(int newValue) {
         cadence = newValue;
    }

    void changeGear(int newValue) {
         gear = newValue;
    }

    void speedUp(int increment) {
         speed = speed + increment;   
    }

    void applyBrakes(int decrement) {
         speed = speed - decrement;
    }

    void printStates() {
         System.out.println(&quot;cadence:&quot; +
             cadence + &quot; speed:&quot; + 
             speed + &quot; gear:&quot; + gear);
    }
}
</pre></div>
<p>The syntax of the Java programming language will look new to you, but the design of this class is based on the previous discussion of bicycle objects. The fields <code>cadence</code>, <code>speed</code>, and <code>gear</code> represent the object&#39;s state, and the methods (<code>changeCadence</code>, <code>changeGear</code>, <code>speedUp</code> etc.) define its interaction with the outside world.</p>
<p>You may have noticed that the <code>Bicycle</code> class does not contain a <code>main</code> method. That&#39;s because it&#39;s not a complete application; it&#39;s just the blueprint for bicycles that might be <i>used</i> in an application. The responsibility of creating and using new <code>Bicycle</code> objects belongs to some other class in your application.</p>
<p>Here&#39;s a 
<a class="SourceLink" target="_blank" href="examples/BicycleDemo.java" onclick="showCode('../../displayCode.html', 'examples/BicycleDemo.java'); return false;"><code>BicycleDemo</code></a> class that creates two separate <code>Bicycle</code> objects and invokes their methods:</p>
<div class="codeblock"><pre>

class BicycleDemo {
    public static void main(String[] args) {

        // Create two different 
        // Bicycle objects
        Bicycle bike1 = new Bicycle();
        Bicycle bike2 = new Bicycle();

        // Invoke methods on 
        // those objects
        bike1.changeCadence(50);
        bike1.speedUp(10);
        bike1.changeGear(2);
        bike1.printStates();

        bike2.changeCadence(50);
        bike2.speedUp(10);
        bike2.changeGear(2);
        bike2.changeCadence(40);
        bike2.speedUp(10);
        bike2.changeGear(3);
        bike2.printStates();
    }
}

</pre></div>
<p>The output of this test prints the ending pedal cadence, speed, and gear for the two bicycles:</p>



