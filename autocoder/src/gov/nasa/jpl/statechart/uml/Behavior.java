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
package gov.nasa.jpl.statechart.uml;

import java.util.List;

public interface Behavior extends Class {

    public boolean isReentrant ();

    public BehavioredClassifier getContext ();

    public void setContext (BehavioredClassifier context);


    /**
     * NON-UML Standard:  list of Behavior actions contained in the name.
     * @return List of strings containing the actions.
     */
    public List<String> actionList ();

}
