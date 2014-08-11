package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

/**
 * Behavior is a specification of how its context classifier changes state over
 * time. This specification may be either a definition of possible behavior
 * execution or emergent behavior, or a selective illustration of an interesting
 * subset of possible executions. The latter form is typically used for
 * capturing examples, such as a trace of a particular execution.
 * 
 * A classifier behavior is always a definition of behavior and not an
 * illustration. It describes the sequence of state changes an instance of a
 * classifier may undergo in the course of its lifetime. Its precise semantics
 * depends on the kind of classifier. For example, the classifier behavior of a
 * collaboration represents emergent behavior of all the parts, whereas the
 * classifier behavior of a class is just the behavior of instances of the class
 * separated from the behaviors of any of its parts.
 * 
 * When a behavior is associated as the method of a behavioral feature, it
 * defines the implementation of that feature (i.e., the computation that
 * generates the effects of the behavioral feature). As a classifier, a behavior
 * can be specialized. Instantiating a behavior is referred to as "invoking" the
 * behavior, an instantiated behavior is also called a behavior "execution." A
 * behavior may be invoked directly or its invocation may be the result of
 * invoking the behavioral feature that specifies this behavior. A behavior can
 * also be instantiated as an object in virtue of it being a class.
 * 
 * The specification of a behavior can take a number of forms, as described in
 * the subclasses of Behavior. Behavior is an abstract metaclass factoring out
 * the commonalities of these different specification mechanisms.
 * 
 * When a behavior is invoked, its execution receives a set of input values that
 * are used to affect the course of execution, and as a result of its execution
 * it produces a set of output values that are returned, as specified by its
 * parameters. The observable effects of a behavior execution may include
 * changes of values of various objects involved in the execution, the creation
 * and destruction of objects, generation of communications between objects, as
 * well as an explicit set of output values.
 */
public class UMLBehavior extends UMLClass implements Behavior {

    private boolean isReentrant = false;

    //private UMLBehavioralFeature specification;
    private BehavioredClassifier context = null;

    //private UMLParameter ownedParameter;
    //private UMLBehavior redefinedBehavior;
    //private UMLConstraint precondition;
    //private UMLConstraint postCondition;

    /* NON UML-standard metamodel property: list of actions in name.
     * This is a quick-and-dirty way of getting a list of actions out of a
     * behavior specification, but allowing semicolon-separated list
     * of "label" (or "func()") in the name of the behavior.
     */
    protected List<String> actionList = null;


    public UMLBehavior (Node element, ModelScape scape) {
        super(element, scape);

        actionList = Util.newList();

        // split name by semicolons and store in actionList
        //- empty trailing strings are omitted
        //- allow ";;" to mean a semicolon literal, so we replace ;; with a
        //  string NOT containing ';', and then replace that back in for loop.
        String nameEscaped = getName().replaceAll(";;", "#*#SC#*#");
        for (String action : nameEscaped.split("\\s*;\\s*")) {
            actionList.add(action.replaceAll("#*#SC#*#", ";"));
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Behavior#isReentrant()
     */
    public boolean isReentrant () {
        return isReentrant;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Behavior#getContext()
     */
    public BehavioredClassifier getContext () {
        return context;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Behavior#setContext(gov.nasa.jpl.statechart.uml.BehavioredClassifier)
     */
    public void setContext (BehavioredClassifier context) {
        this.context = context;
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Behavior#actionList()
     */
    public List<String> actionList () {
        return Collections.unmodifiableList(actionList);
    }

}
