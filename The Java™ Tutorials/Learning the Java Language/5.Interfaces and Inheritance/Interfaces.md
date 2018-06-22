<h1>Interfaces</h1>
<!--Creating and Using an Interface-->
<p>There are a number of situations in software engineering when it is important for disparate groups of programmers to agree to a &quot;contract&quot; that spells out how their software interacts. Each group should be able to write their code without any knowledge of how the other group&#39;s code is written. Generally speaking, <i>interfaces</i> are such contracts.</p>

<p>For example, imagine a futuristic society where computer-controlled robotic cars transport passengers through city streets without a human operator. Automobile manufacturers write software (Java, of course) that operates the automobile&#151;stop, start, accelerate, turn left, and so forth. Another industrial group, electronic guidance instrument manufacturers, make computer systems that receive GPS (Global Positioning System) position data and wireless transmission of traffic conditions and use that information to drive the car.</p>

<p>The auto manufacturers must publish an industry-standard interface that spells out in detail what methods can be invoked to make the car move (any car, from any manufacturer). The guidance manufacturers can then write software that invokes the methods described in the interface to command the car. Neither industrial group needs to know <i>how</i> the other group&#39;s software is implemented. In fact, each group considers its software highly proprietary and reserves the right to modify it at any time, as long as it continues to adhere to the published interface.</p>

<h2>Interfaces in Java</h2>

<p>In the Java programming language, an <i>interface</i> is a reference type, similar to a class, that can contain <i>only</i> constants, method signatures, default methods, static methods, and nested types. Method bodies exist only for default methods and static methods. Interfaces cannot be instantiated&#151;they can only be <i>implemented</i> by classes or <i>extended</i> by other interfaces. Extension is discussed later in this 
lesson.
</p>
<p>Defining an interface is similar to creating a new class:</p>
<div class="codeblock"><pre>
public interface OperateCar {

   // constant declarations, if any

   // method signatures
   
   // An enum with values RIGHT, LEFT
   int turn(Direction direction,
            double radius,
            double startSpeed,
            double endSpeed);
   int changeLanes(Direction direction,
                   double startSpeed,
                   double endSpeed);
   int signalTurn(Direction direction,
                  boolean signalOn);
   int getRadarFront(double distanceToCar,
                     double speedOfCar);
   int getRadarRear(double distanceToCar,
                    double speedOfCar);
         ......
   // more method signatures
}
</pre></div> 
<p>Note that the method signatures have no braces and are terminated with a semicolon.</p>
<p>To use an interface, you write a class that <i>implements</i> the interface. When an instantiable class implements an interface, it provides a method body for each of the methods declared in the interface. For example,</p>
<div class="codeblock"><pre>
public class OperateBMW760i implements OperateCar {

    // the OperateCar method signatures, with implementation --
    // for example:
    int signalTurn(Direction direction, boolean signalOn) {
       // code to turn BMW's LEFT turn indicator lights on
       // code to turn BMW's LEFT turn indicator lights off
       // code to turn BMW's RIGHT turn indicator lights on
       // code to turn BMW's RIGHT turn indicator lights off
    }

    // other members, as needed -- for example, helper classes not 
    // visible to clients of the interface
}
</pre></div> 
<p>In the robotic car example above, it is the automobile manufacturers who will implement the interface. Chevrolet&#39;s implementation will be substantially different from that of Toyota, of course, but both manufacturers will adhere to the same interface. The guidance manufacturers, who are the clients of the interface, will build systems that use GPS data on a car&#39;s location, digital street maps, and traffic data to drive the car. In so doing, the guidance systems will invoke the interface methods: turn, change lanes, brake, accelerate, and so forth.</p>
<h2>Interfaces as APIs</h2>
<p>The robotic car example shows an interface being used as an industry standard <i>Application Programming Interface (API)</i>. APIs are also common in commercial software products. Typically, a company sells a software package that contains complex methods that another company wants to use in its own software product. An example would be a package of digital image processing methods that are sold to companies making end-user graphics programs. The image processing company writes its classes to implement an interface, which it makes public to its customers. The graphics company then invokes the image processing methods using the signatures and return types defined in the interface. While the image processing company&#39;s API is made public (to its customers), its implementation of the API is kept as a closely guarded secret&#151;in fact, it may revise the implementation at a later date as long as it continues to implement the original interface that its customers have relied on.</p>
