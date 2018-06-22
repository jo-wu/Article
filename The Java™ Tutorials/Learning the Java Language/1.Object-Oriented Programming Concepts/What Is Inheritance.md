<h1>What Is Inheritance?</h1>
<p>Different kinds of objects often have a certain amount in common with each other. Mountain bikes, road bikes, and tandem bikes, for example, all share the characteristics of bicycles (current speed, current pedal cadence, current gear). Yet each also defines additional features that make them different: tandem bicycles have two seats and two sets of handlebars; road bikes have drop handlebars; some mountain bikes have an additional chain ring, giving them a lower gear ratio.</p>
<p>Object-oriented programming allows classes to <i>inherit</i> commonly used state and behavior from other classes. In this example, <code>Bicycle</code> now becomes the <i>superclass</i> of <code>MountainBike</code>, <code>RoadBike</code>, and <code>TandemBike</code>. In the Java programming language, each class is allowed to have one direct superclass, and each superclass has the potential for an unlimited number of <i>subclasses</i>:</p>
<center><img src="../../figures/java/concepts-bikeHierarchy.gif" width="374" height="324" align="bottom" alt="A diagram of classes in a hierarchy." /></p><p class="FigureCaption">A hierarchy of bicycle classes.</p></center><p>The syntax for creating a subclass is simple. At the beginning of your class declaration, use the <code>extends</code> keyword, followed by the name of the class to inherit from:</p>
<div class="codeblock"><pre>
class MountainBike <strong>extends</strong> Bicycle {

    // new fields and methods defining 
    // a mountain bike would go here

}
</pre></div>
<p>This gives <code>MountainBike</code> all the same fields and methods as <code>Bicycle</code>, yet allows its code to focus exclusively on the features that make it unique. This makes code for your subclasses easy to read. However, you must take care to properly document the state and behavior that each superclass defines, since that code will not appear in the source file of each subclass.</p>

        </div>
        <div class="NavBit">
            <a target="_top" href="class.html">&laquo; Previous</a>
            &bull;
            <a target="_top" href="../TOC.html">Trail</a>
            &bull;
            <a target="_top" href="interface.html">Next &raquo;</a>
        </div>
    </div>
    
