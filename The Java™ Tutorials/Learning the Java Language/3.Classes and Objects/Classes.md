<h1>Classes</h1>
<!---Classes=-->
<p>The introduction to object-oriented concepts in the lesson titled 
<a class="TutorialLink" target="_top" href="../../java/concepts/index.html">Object-oriented Programming Concepts </a>used a bicycle class as an example, with racing bikes, mountain bikes, and tandem bikes as subclasses. Here is sample code for a possible implementation of a <code>Bicycle</code> class, to give you an overview of a class declaration. Subsequent sections of this 
lesson 
 will back up and explain class declarations step by step. For the moment, don&#39;t concern yourself with the details.</p>
<div class="codeblock"><pre>
public class Bicycle {
        
    // <b>the Bicycle class has</b>
    // <b>three <i>fields</i></b>
    public int cadence;
    public int gear;
    public int speed;
        
    // <b>the Bicycle class has</b>
    // <b>one <i>constructor</i></b>
    public Bicycle(int startCadence, int startSpeed, int startGear) {
        gear = startGear;
        cadence = startCadence;
        speed = startSpeed;
    }
        
    // <b>the Bicycle class has</b>
    // <b>four <i>methods</i></b>
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
        
    // <b>the MountainBike subclass has</b>
    // <b>one <i>field</i></b>
    public int seatHeight;

    // <b>the MountainBike subclass has</b>
    // <b>one <i>constructor</i></b>
    public MountainBike(int startHeight, int startCadence,
                        int startSpeed, int startGear) {
        super(startCadence, startSpeed, startGear);
        seatHeight = startHeight;
    }   
        
    // <b>the MountainBike subclass has</b>
    // <b>one <i>method</i></b>
    public void setHeight(int newValue) {
        seatHeight = newValue;
    }   

}
</pre></div>
<p><code>MountainBike</code> inherits all the fields and methods of <code>Bicycle</code> and adds the field <code>seatHeight</code> and a method to set it (mountain bikes have seats that can be moved up and down as the terrain demands).</p>

