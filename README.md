# Select language: English | <a href="README_zh_CN.md">Chinese</a>

# What's BabyFish
BabyFish is a Java Framework that can let developers create smart data structure only with declartion code.
It have many functionalities, please view the following picture to know its general architecture.

# Architecture
<img src="babyfish.png"/>
<p>
    In the picture, 5 functionalities are marked by red star:
    <ul>
        <li><a href="#unstableCollectionElements">Unstable Collection Elements</a></li>
        <li><a href="#bubbleEvent">Bubble Event</a></li>
        <li><a href="#objectModel4Java">ObjectModel4Java</a></li>
        <li><a href="#objectModel4JPA">ObjectModel4JPA</a></li>
        <li><a href="#queryPath">Query Path</a></li>
    </ul>
    , they are the most most important 5 functionalities of BabyFish.
</p>

# Java environment requirement
BabyFish1.1 requires Java8.

# Offline documents
<table>
    <thead>
        <tr>
            <th>Document</th>
            <th>Description</th>
        <tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="tutorial.html" target="_blank">tutorial.html</a>(English)</td>
            <td rowspan="2">
                Describes some important functions in detail, with both characters and illustrations
                (Please use high version IE or other browsers to open it).
            </td>
        </tr>
        <tr>
            <td><a href="tutorial.html" target="_blank">tutorial_zh_CN.html</a>(Chinese)</td>
        </tr>
        <tr>
            <td><a id="demo-guide" name="demo-guide" href="demo-guide.html" target="_blank">demo-guide.html</a>(English)</td>
            <td rowspan="2">
                Introduces each demo one by one, and gives the recommended reading order
                (Please use high version IE or other browsers to open it).
            </td>
        </tr>
        <tr>
            <td><a href="demo-guide_zh_CN.html" target="_blank">demo-guide_zh_CN.html</a>(Chinese)</td>
        </tr>
    </tbody>
</table>

# Functionalities
<ul>
    <li>
        <h2> 1. Java Functionalities</h2>
        <ul>
            <li>
                <h3>1.1. Basic Functionalities</h3>
                <ul>
                    <li>
                        <h4>1.1.1. Typed I18N</h4>
                        Better way to support I18N. Be different with "java.util.ResourceBundle" that reports the
                        errors at runtime, all the errors of "Typed18N" will be reported at compilation-time
                        (This functionality requires the compilation-time byte-code instrument).
                    </li>
                    <li>
                        <h4>1.1.2. Delegate</h4> 
                        Better way to support event nodification, like the delegate of .NET Framework.
                        It's more simple, More economical and more powerful than the suggestion of Java Bean Standard
                        (This functionality requires the compilation-time byte-code instrument).
                    </li>
                    <li>
                        <h4>1.1.3. Equality</h4>
                        The developers often override the method "public boolean equals(Object o)",
                        they often have two choices to check whether type of argument is same with the current object(this):
                        <ul>
                            <li>Too strict way: if (this.getClass() != o.getClass()) return false; </li>
                            <li>Too lax way: if (!(o instanceof ${ThisClass})) return false;</li>
                        </ul>
                        Unfortunately, Both of them are wrong, this functionality gives a perfect solution,
                        its corresponding demos indicate their problems and give the correct solution.
                    </li>
                    <li>
                        <h4>1.1.4. Graph Traveler</h4>
                        This functionality is used to visit a object graph by depth first way or breadth first way,
                        during the visiting, it supports many context variables.
                    </li>
                </ul>
            </li>
            <li>
                <h3>1.2. BabyFish Collection Framework</h3>
                <ul>
                    <li>
                        <h4>1.2.1. X Collection Framework</h4>
                        <p>
                            "X" means "eXtensible", X Collection Framework extends the interfaces of Java Collection Framework and gives a new implementation to support some incredible functionalities.
                        </p>
                        <p>
                            Here we only disuccss two most important functionalities of X Collection Framework
                            (Please see <a href="#demo-guide">demo-guide</a> to know all functionalities).
                            <ul>
                                <li>
                                    <h5>1.2.1.1. Bidi Collection</h5>
                                    Like "org.apache.commons.collections4.BidiMap", 
                                    X Collection Framework support BidiMap to let "java.util.Map" supports unique constraint of value(not key); 
                                    In a similar way, BidiList is supported too.
                                </li>
                                <li>
                                    <h5>1.2.1.2. <a name="unstableCollectionElements">Unstable Collection Elements</a></h5>
                                    <p>
                                        Java Framework supports hash structure and red black tree via HashSet, HashMap, TreeSet and TreeMap,
                                        their benefit is high peformance but defect is an object cannot be changed since 
                                        it has been added into collections as Set element or Map key.  
                                    </p>
                                    <p>
                                        X Collection Framework supports "Unstable Collection Elements", an object can still be changed 
                                        even if it has been added into Set as element added into Map as key, because all the sets and maps will be adjusted automatically when this object is changed.
                                        <table>
                                            <thead>
                                                <tr>
                                                    <th>Collection</th>
                                                    <th>Materialize of "Unstable Collection Elements"</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td>Set</td>
                                                    <td>Unstable elements</td>
                                                </tr>
                                                <tr>
                                                    <td>Map</td>
                                                    <td>Unstable keys</td>
                                                </tr>
                                                <tr>
                                                    <td>BidiList</td>
                                                    <td>Unstable elements</td>
                                                </tr>
                                                <tr>
                                                    <td rowspan="2">BidiMap</td>
                                                    <td>Unstable keys</td>
                                                </tr>
                                                <tr>
                                                    <td>Unstable values</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </p>
                                </li>
                            </ul>
                        </p>
                    </li>
                    <li>
                        <h4>1.2.2. MA Collection Framework</h4>
                        <p>
                            "MA" means "Modification Aware", MA Collection Framework extends X Collection Framework to support the modification event nodification.
                            For each modified element or key-value pair, a couple of events will be triggered,
                            one is triggered before the modification and the other one is triggered after the modification.
                            This functionality looks like the row level trigger of RDMBS very much.
                        </p>
                        <p>
                            Here we only disuccss two most important functionalities of MA Collection Framework
                            (Please see <a href="#demo-guide">demo-guide</a> to know all functionalities).
                            <ul>
                                <li>
                                    <h5>1.2.2.1 Implicit Event</h5>
                                    For BabyFish Collection Framework, not only the explicit collection modification API invocation can change the data of collection object, 
                                    but also the collection object can be modified by the system automatically. 
                                    The most typical representative is automatic collection adjusting caused by "Unstable Collection Elements". 
                                    Events can still be triggered by the modification that isn't caused by developer and is automatic, implicit.
                                </li>
                                <li>
                                    <h5>1.2.2.2 <a name="bubbleEvent">Bubble Event</a></h5>
                                    <p>
                                        Java Colletion Framework support collection views, for example:
                                        <ul>
                                            <li>"java.util.NavigableMap" supports methods headMap, tailMap, subMap and descendingMap.</li>
                                            <li>"java.util.NavigableSet" supports methods headSet, tailSet, subSet, descendingSet and descendingIterator.</li>
                                            <li>"java.util.List" supports methods subList and listIterator.</li>
                                            <li>"java.util.Map" supports keySet, methods values and entrySet.</li>
                                            <li>"java.util.Collection" supports method iterator.</li>
                                        </ul>
                                        that means the developer can create new collection view from original collection or collection view, 
                                        all the collection views share the same data with the root collection, 
                                        the root collection will be affected when any one view is changed.
                                    </p>
                                    <p>
                                        MA Collection Framework supports "Bubble Event", when the collection view is changed by the developer, the event will be triggered by the view itself; then the event will be bubbled to its parent view, then a new event will be triggered by the parent view. By parity of reasoning, finally, the event will bubbled to root and new event will be triggered by original collection.
                                    </p>
                                </li>
                            </ul>
                        </p>
                    </li>
                    <li>
                        <h4>1.2.3. Lazy Collection Framework</h4>
                        <p>
                            As we know, ORM frameworks often support a technology called "Lazy Loading". Lazy collection is fake collection without data, it can become the real collection by IO reading when it's used by developer at first time. 
                            This functionality is very powerful, it's very wasteful if it can only be used by ORM frameworks.
                        </p>
                        <p>
                            In order to let this powerful functionality can be used anywhere(Not only by ORM implementation), babyfish support Lazy Collection Framework which is abstract and universal. 
                        </p>
                        <p>
                            No demo for it, it's not API of babyfish, it's SPI of babyfish.
                        </p>
                    </li>
                    <li>
                        <h4>1.2.4. Collection Utils</h4>
                        <p>
                            Java Collection Framework has very powerful tool class "java.util.Collections", this class supports many static methods to create magical collection proxies.
                        </p>
                        <p>
                            BabyFish Collection Framework extends the interfaces of Java Collection Framework, similarly, it must support its own tool class too: "org.babyfish.collection.MACollections".
                        </p>
                    </li>
                </ul>
            </li>
            <li>
                <h3><a name="objectModel4Java">1.3. ObjectModel4Java</h3>
                <p>
                    ObjectModel is the core functionality of BabyFish, it the reason why I created this framework. 
                </p>
                <p>
                    ObjectModel is used to support "Smart Data Structcure", it can be splitted to two parts
                    <ul>
                        <li>
                            ObjectModel4Java: Smart data structure for java language, this is being discussed by this chapter. 
                        </li>
                        <li>    
                            ObjectModel4JPA: Smart data structure for JPA entity classes, this will be discussed by <a href="#objectModel4JPA">next chapter</a>. 
                        </li>
                    </ul>
                </p>
                <p>
                    ObjectModel4Java has many functionalities, this document only introduces two of them: 1, Consistency of bidirectional association; 2, Unstable objects.
                    (Please see <a href="#demo-guide">demo-guide</a> to know all functionalities). 
                </p>
                <h4>1.3.1. Consistency of bidirectional association</h4>
                <p>
                    Java developers often declare the data classes without any logic except getters and setters
                    (Bluntly, they're C-structures), the data structure described by these simple classes 
                    does not have enough conveniences and intelligent.
                    For a simple example, there is a bidirectional association between 
                    two objects, when one side of that association is modified by the developer,
                    the other side won't be changed, so the data structure become inconsistent,
                    we can navigate to object B from object A, but cannot navigate to object A from object B. 
                </p>
                <p>
                    ObjectModel4Java adds the intelligent into data structure, 
                    but it does not increase the code complexity, after all, 
                    the classes without any logic except getters and setters is so easy to be developed that
                    they are accepted by every one. ObjectModel4Java only requires the developers to add a little
                    annotations to those simple classes, after the compilation-time bytecode instrument,
                    they will become powerful data classes. 
                </p>
                <p>
                    Let's see an example, declare the bidirectional association between department and employees. 
                    <ul>
                        <li>
                            One department has many employees. 
                            <pre><a href="src/babyfish-model/src/main/java/org/babyfish/model/Model.java">@Model</a> //Uses ObjectModel4Java, requires compilation-time bytecode instrument.
public class <a id="department" name="department">Department</a> {
    
    //One side of bidirectional association, 
    //this field is the mirroring of "Employee.department".
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Association.java">@Association</a>(opposite = <a href="#employee_department">"department"</a>)  
    private List&lt;<a href="#employee">Employee</a>&gt; <a id="department_employees" name="department_employees"> employees; //List has order.
    
    The getters and setters are omitted
}</pre>
                        </li>
                        <li>
                            Each employee belongs to a department, and it also know its position in the list of its department. 
                            <pre><a href="src/babyfish-model/src/main/java/org/babyfish/model/Model.java">@Model</a> //Uses ObjectModel4Java, requires compilation-time bytecode instrument.
public class <a id="employee" name="employee">Employee</a> {

    //Other side of bidirectional association, 
    //this field is the mirroring of "Department.employees".
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Association.java">@Association</a>(opposite = <a href="#department_employees">"employees</a>")
    private <a href="#department">Department</a> <a id="employee_department" name="employee_department">department</a>; //Employee belongs to a department, if not, it's null

    // This field is attached to "Employee.department", it's the index of current object in list of 
    // parent department object. If this object does not belong to any department object, it's -1.
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Association.java">@IndexOf</a>(<a href="#employee_department">"department"</a>)
    private int index; 

    The getters and setters are omitted
}</pre>
                        </li>
                    </ul>
                </p>
                <p>
                    Now, there are 6 objects, their variable names are department1, department2, employee1, employe2, employee3 and employee4, this is the data stucure:
                </p>
                <table>
                    <thead>
                        <tr>
                            <th colspan="2">Department object</th>
                            <th colspan="3">Employee object</th>
                        </tr>
                        <tr>
                            <td>Variable name</td>
                            <td>employees</td>
                            <td>Variable name</td>
                            <td>department</td>
                            <td>index</td>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td rowspan="2">department1</td>
                            <td rowspan="2">[ employee1, employee2 ]</td>
                            <td>employee1</td>
                            <td>department1</td>
                            <td>0</td>
                        </tr>
                        <tr>
                            <td>employee2</td>
                            <td>department1</td>
                            <td>1</td>
                        </tr>
                        <tr>
                            <td rowspan="2">department2</td>
                            <td rowspan="2">[ employee3, employee4 ]</td>
                            <td>employee3</td>
                            <td>department2</td>
                            <td>0</td>
                        </tr>
                        <tr>
                            <td>employee4</td>
                            <td>department2</td>
                            <td>1</td>
                        </tr>
                    </tbody>
                </table>
                <p>
                    Then the developer modifies the "employees" collection of "department1", like this
                    <pre>department1.getEmployees().add(0, employee3);</pre>
                    This code inserts the "employee3" into the head of "department1.employees", 
                    In order to preserve the consistency of data strucure, ObjectModel4Java will do these works: 
                </p>
                <ul>
                    <li>Remove "employee3" from the collection "department2.employees" automatically.</li>
                    <li>Assign the "employee3.department" to be "department1" automatically.</li>
                    <li>Increase "employee1.index" automatically, from 0 to 1. </li>
                    <li>Increase "employee2.index" automatically, from 1 to 2. </li>
                    <li>Decrease "employee4.index" automatically, from 1 to 0. </li>
                </ul>
                <p>Finally, the data structure become:</p>
                <table>
                    <thead>
                        <tr>
                            <th colspan="2">Department Object</th>
                            <th colspan="3">Employee Object</th>
                        </tr>
                        <tr>
                            <td>Variable name</td>
                            <td>employees</td>
                            <td>Variable name</td>
                            <td>department</td>
                            <td>index</td>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td rowspan="3">department1</td>
                            <td rowspan="3">[ employee3, employee1, employee2 ]</td>
                            <td>employee1</td>
                            <td>department1</td>
                            <td>1</td>
                        </tr>
                        <tr>
                            <td>employee2</td>
                            <td>department1</td>
                            <td>2</td>
                        </tr>
                        <tr>
                            <td>employee3</td>
                            <td>department1</td>
                            <td>0</td>
                        </tr>
                        <tr>
                            <td>department2</td>
                            <td>[ employee4 ]</td>
                            <td>employee4</td>
                            <td>department2</td>
                            <td>0</td>
                        </tr>
                    </tbody>
                </table>
            </li>
            <li>
                <h4>1.3.2. Unstable Objects. </h4>
                <p>
                    In previous demo, the collection field uses "java.util.List", of course, "java.util.Set" and "java.util.Map"
                    can be used too. In this demo, we use "java.util.Set" to decribe the bidirectional many-to-many
                    association between companies and investors. 
                </p>
                <ul>
                    <li>
                        A company can be jointly funded by multiple investors. 
                        <pre><a href="src/babyfish-model/src/main/java/org/babyfish/model/Model.java">@Model</a> //Uses ObjectModel4Java, requires compilation-time bytecode instrument.
public class <a id="company" name="company">Company</a> {
    
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Scalar.java">@Scalar</a> //Scalar, not association. 
    private String shortName;

    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Association.java">@Association</a>(opposite = <a href="#investor_companies">"companies"</a>) 
    // The annotation "@ComparatorRule" means this set collection uses the field "Investor.name"
    // to calculate the hashCode of investor and check whether two investors are equal.
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/ComparatorRule.java">@ComparatorRule</a>(properties = <a href="src/babyfish-model/src/main/java/org/babyfish/model/ComparatorProperty.java">@ComparatorProperty</a>(<a href="#investor_name">"name"</a>))
    private Set&lt;<a href="#investor">Investor</a>&gt; <a id="company_investors" name="company_investors"> investors; 
    
    The getters and setters are omitted
}</pre>
                    </li>
                    <li>
                        An investor can invest in multiple companies.
                        <pre><a href="src/babyfish-model/src/main/java/org/babyfish/model/Model.java">@Model</a> //Uses ObjectModel4Java, requires compilation-time bytecode instrument.
public class <a id="investor" name="investor">Investor</a> {
    
    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Scalar.java">@Scalar</a> //Scalar, not association. 
    private String <a id="investor_name" name="investor_name">name</a>;

    <a href="src/babyfish-model/src/main/java/org/babyfish/model/Association.java">@Association</a>(opposite = <a href="#company_investors">"investors"</a>)
    private Set&lt;<a href="#company">Company</a>&gt; <a id="investor_companies" name="investor_companies"> companies; 
    
    The getters and setters are omitted
}</pre>
                    </li>
                </ul>
                <p>
                    Now there are 3 object; one of them is an "Company" object whose variable name is "apple",
                    the other two are "Investor" objects whose variable name are "steve" and "sulley".
                    The data structure is:
                </p>
                <table>
                    <thead>
                        <tr>
                            <th colspan="3">Company object</th>
                            <th colspan="3">Investor object</th>    
                        </tr>
                        <tr>
                            <th>Variable name</th>
                            <th>shortName</th>
                            <th>investors</th>
                            <th>Variable name</th>
                            <th>name</th>
                            <th>companies</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td rowspan="2">apple</td>
                            <td rowspan="2">Apple</td>
                            <td rowspan="2">[ steve, sculley ]</td>
                            <td>steve</td>
                            <td>Steve</td>
                            <td>[ apple ]</td>
                        </tr>
                        <tr>
                            <td>sculley</td>
                            <td>Sculley</td>
                            <td>[ apple ]</td>
                        </tr>
                    </tbody>
                </table>
                <p>Now, the develop execute this code: </p>
                <pre>sculley.setName("Steve");</pre>
                <p>
                    After execute this code, The value of "sculley.name" will be same with the value of "steve.name", both of them are "Steve"; 
                    But the collection field <a href="#company_investors">investors</a>" of "<a href="#company">Company</a>"
                    uses the annotation<a href="src/babyfish-model/src/main/java/org/babyfish/model/ComparatorRule.java">@ComparatorRule</a>
                    to let this collection cannot contains tow <a href="#investor">Investor</a> objects with the same <a href="#investor_name">name</a>. 
                    This is a paradoxical situation. 
                </p>
                <p>
                    Fortunately, the field <a href="#company_investors">investors</a>" of "<a href="#company">Company</a>"
                    supports <a href="#unstableCollectionElements">"Unstable Collection Elements"</a>; that will cause the following effects. 
                </p>
                <ul>
                    <li>
                        In order to preserve the unique contraint of the set field, 
                        the object "steve" will be crowded out from the collection
                        <a href="#company_investors">investors</a> of object "apple"
                        automatically because of 
                        <a href="#unstableCollectionElements">"Unstable Collection Elements"</a>.
                    </li>
                    <li>
                        the object "steve" is longer the investor of "apple", 
                        in order to preserve the consistency of bidirectional assocation.
                        the objet "apple" will be removed from the collection 
                        <a href="#investor_companies">companies</a> of object "steve" auotmatically. 
                    </li>
                </ul>
                <p>Finally, the data structure become:</p>
                <table>
                    <thead>
                        <tr>
                            <th colspan="3">Company object</th>
                            <th colspan="3">Investor object</th>    
                        </tr>
                        <tr>
                            <th>Variable name</th>
                            <th>shortName</th>
                            <th>investors</th>
                            <th>Variable name</th>
                            <th>name</th>
                            <th>companies</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>apple</td>
                            <td>Apple</td>
                            <td>[ sculley ]</td>
                            <td>steve</td>
                            <td>Steve</td>
                            <td>[]</td>
                        </tr>
                        <tr>
                            <td colspan="3"></td>
                            <td>sculley</td>
                            <td>Steve</td>
                            <td>[ apple ]</td>
                        </tr>
                    </tbody>
                </table>
            </li>
        </ul>
    </li>
    <li>
        <h2>2. JPA Functionalities</h2>
        <ul>
            <li>
                <h3>2.1. <a id="objectModel4JPA" name="objectModel4JPA">ObjectModel4JPA</h3>
                <p>
                    ObjectModel4JPA is an enhancement of <a href="#objectModel4Java">ObjectModel4Java</a>,
                    it has the same functionalities with <a href="#objectModel4Java">ObjectModel4Java</a>,
                    but their objectives are not same, it allows the JPA entity classes to use the ObjectMode,
                    not for the ordinary java classes; their declaration modes are different too,
                    please see <a href="#demo-guide">demo-guide</a> to know more details.
                </p>
                <p>
                    Be same with <a href="#objectModel4Java">ObjectModel4Java</a>，
                    ObjectModel4JPA requires the compilation-time bytecode instrument of Maven plugin too.
                </p>
            </li>
            <li>
                <h3>2.2. BabyFish Hibernate Collection Framework</h3>
                <ul>
                    <li>
                        In <a href="#objectModel4Java">ObjectModel4Java</a>，
                        The collections used by the association field can preserve the consistency of bidirectional assocation,
                        of course, they has the all the functionalities of X Collection Framework and MA Collection Framework too.
                        <a href="#objectModel4JPA">ObjectModel4JPA</a> has the same functionalities with <a href="#objectModel4Java">ObjectModel4Java</a>，
                        The its collections must have those functionalities too.
                    </li>
                    <li>
                        <a href="#objectModel4JPA">ObjectModel4JPA</a> is designed for JPA/Hibernate,
                        so its collections must support the the functionality "Lazy Loadding", 
                        like the protogenous JPA/Hibernate.
                    </li>
                </ul>
                <p>
                    In order to let the collections of ObjectModel support all of those functionalities,
                    BabyFish support its own Hibernate Collection Framework by extending Lazy Collection Framework.
                </p>
                <p>
                    No demo for it because it's an internal module of babyfish-hibernate-extension.
                    The developer only need to know that the collections of <a href="#objectModel4JPA">ObjectModel4JPA</a> have both
                    the functionalities of ObjectModel4Java's collections and
                    the functionaltities of Hibernate's collections.
                </p>
            </li>
            <li>
                <h3>2.3. JPA & Hibernate Extension</h3>
                <p>
                    In order to add some new functionalities, BabyFish extends the API of JPA and Hibernate.
                    This deocument only introduces two of them: QueryPath and Oracle Optimization
                    (Please see <a href="#demo-guide">demo-guide</a> to know all functionalities).
                </p>
                <ul>
                    <li>
                        <h4>2.3.1. <a name="queryPath">Query Path</a></h4>
                        <p>
                            In order to support "dynamic eager fetching"(
                            Association fetches is specified by the arguments of UI or business logic layer, not by the hard code in the data accessing layer), 
                            BabyFish supports "Query Path", it looks like these technologies
                            <ul>
                                <li><a href="http://api.rubyonrails.org/classes/ActiveRecord/QueryMethods.html#method-i-includes">Including functionality of Active Record</a></li>
                                <li><a href="https://msdn.microsoft.com/en-us/library/bb738708(v=vs.110).aspx">Including functionality of ADO.NET Entity Framework</a></li>
                            </ul> very much.
                            This functionality is more simpler, more beautiful and more powerful than 
                            <a href="https://docs.oracle.com/javaee/7/api/javax/persistence/EntityGraph.html">
                                javax.persistence.EntityGraph
                            </a>
                            which is provided since JPA2.1, please forget it and use Query Path in practical projects.
                        </p>
                        <p>
                            Comparing with 
                            <a href="http://api.rubyonrails.org/classes/ActiveRecord/QueryMethods.html#method-i-includes">Including functionality of Active Record</a>
                            or 
                            <a href="https://msdn.microsoft.com/en-us/library/bb738708(v=vs.110).aspx">Including functionality of ADO.NET Entity Framework</a>,
                            The Query Path has 3 advantages.
                        </p>
                        <ul>
                            <li>
                                In Active Record or ADO.NET Entity Framework, Include Paths are string which isn't type-safe, 
                                the errors of Include Paths won't reported until run or test the application;
                                In BabyFish, Query Paths are created by the "Query Path meta-model" whose source code is automatically generated at compilation-time by maven plugin,
                                the errors of Query Paths will be reported as compilation error.
                            </li>
                            <li>
                                Not only the object associations graph with unlimited depth and breadth can be fetched dynamically, but also the lazy scalar field(Not not always but often is lob field) can be fetched dynamically.
                            </li>
                            <li>
                                It can also be used to sort the objects of query result and sort associations collections.
                            </li>
                        </ul>
                    </li>
                    <li>
                        <h4>2.3.2. Oracle Optimization</a></h4>
                        <p>
                            Hibernate has a performance issue: When the query has both collection fetches and paging filter,
                            Hibernate cannot generate paging restriction in SQL statement, 
                            all the matched rows will be selected and the paging filter will be applied in memory.
                            This is a big problem so Hibernate writes this line in log file: 
                            <div>"firstResult/maxResults specified with collection fetch; applying in memory!"</div>
                        </p>
                        <p>
                        BabyFish can resolve this problem when the database is Oracle, please uses the dialect provided by BabyFish, such as:
                            <ul>
                                <li><a href="./src/babyfish-hibernate-extension/src/main/java/org/babyfish/hibernate/dialect/Oracle8iDialect.java">org.babyfish.hibernate.dialect.Oracle8iDialect</a></li>
                                <li><a href="./src/babyfish-hibernate-extension/src/main/java/org/babyfish/hibernate/dialect/Oracle9iDialect.java">org.babyfish.hibernate.dialect.Oracle9iDialect</a></li>
                                <li><a href="./src/babyfish-hibernate-extension/src/main/java/org/babyfish/hibernate/dialect/Oracle10gDialect.java">org.babyfish.hibernate.dialect.Oracle10gDialect</a></li>
                            </ul>
                        </p>
                    </li>
                </ul>
            </li>
        </ul>
    </li>
</ul>

# license: LPGL3.0
BabyFish uses the LGPL-3.0 license so that it can be used in commercial projects, 
please see [http://opensource.org/licenses/LGPL-3.0](http://opensource.org/licenses/LGPL-3.0) to know more.

# Thanks
Thank two great frameworks: [ASM](http://asm.ow2.org) and [ANTLR](http://www.antlr.org)

# History
>* Aug 2008: I have some ideas, I decided to create an open source framework by using all of my spare time.
>* Oct 2015: This first version 1.0.0.Alpha is finished, and it's published on github.
>* Jun 2016: The version 1.1.0 is finished.

# Contact me, give suggestions and expectations
Tao Chen, [babyfish-ct@163.com](mailto:babyfish-ct@163.com)

2016-06-25, ChengDu, China
