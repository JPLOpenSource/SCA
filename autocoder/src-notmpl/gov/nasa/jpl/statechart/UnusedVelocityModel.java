/**
 * Created Sep 29, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart;

import gov.nasa.jpl.statechart.Entry;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;
import gov.nasa.jpl.statechart.template.VelocityModel;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * [Purpose]
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UnusedVelocityModel extends VelocityModel {

    /**
     * Main constructor to initialize the Velocity Model.
     * Mappings of qualified names to namespaces is created at the start.
     * 
     * @param myStateMachine  the state machine with which to construct this model
     * @param myMapper  the target language mapper of names
     */
    public UnusedVelocityModel (StateMachine myStateMachine, TargetLanguageMapper myMapper) {
        super(myStateMachine, myMapper);
    }


///////////////////////////////////////
 // Methods to handle transition nodes
 ///////////////////////////////////////

     /**
      * This is a core-method that is fundamental to the code generation
      * process.  Every transition has the possibility of moving through an
      * arbitrary number of pseudo states via forks and junction point.
      * Technically, there are an arbitrary number of UML State targets 
      * for a transition.  The transition may also descend into submachines
      * via connection point references. 
      *
      * This method returns the possible targets in a depth-first order.
      * This order is important, because split nodes can have conditions which
      * must be expanded before taking an alternate path.
      *
      * The intermediate states are wrapped in an auxiliary class that
      * provides information about their depth and other meta-data
      * that is useful for code generation in the templates.
      *
      * Actions are Transition Effects (Behaviors) that are Activities (use
      * the name)
      */
     public class TransitionNode<T extends NamedElement> {
         private final int depth;
         private final boolean direction; // true = down, false = up
         private final int index;
         private final int count;
         private final Entry<String, T> source;
         private final Transition transition;

         public TransitionNode (Map.Entry<String, ? extends T> source,
                 Transition transition, boolean direction, int depth, int index,
                 int count) {
             this.source = new Entry<String, T>(source);
             this.transition = transition;
             this.direction = direction;
             this.depth = depth;
             this.index = index;
             this.count = count;
         }

         public int depth () {
             return depth;
         }

         /* direction of traversal */
         public boolean down () {
             return direction;
         }

         public boolean up () {
             return !direction;
         }

         /* which one of the signling transitions is this */
         public boolean isFirst () {
             return index == 0;
         }

         public boolean isLast () {
             return index == (count - 1);
         }

         public int index () {
             return index;
         }

         public int count () {
             return count;
         }

         public Entry<String, T> source () {
             return source;
         }

         public Transition transition () {
             return transition;
         }

         @Override
         public String toString () {
             return "TransitionNode\n" + "  source: " + source.getKey() + "\n"
                     + "  transtion: " + transition + "\n" + "  direction: "
                     + direction + "\n" + "  depth: " + depth + "\n"
                     + "  index: " + index + "\n" + "  count: " + count + "\n";
         }
     }

     public List<TransitionNode<NamedElement>> getTransitionTree (
             Map.Entry<String, ? extends Namespace> entry, Transition transition) {
         List<TransitionNode<NamedElement>> transitions = Util.newList();

         // Transitioning down
         transitions.add(new TransitionNode<NamedElement>(entry, transition,
                 true, 0, 0, 1));

         // Recurse down the transition tree
         transitions.addAll(transitionHelper(entry, transition, 0));

         // Transitioning up
         transitions.add(new TransitionNode<NamedElement>(entry, transition,
                 false, 0, 0, 1));

         return transitions;
     }

     private List<TransitionNode<NamedElement>> transitionHelper (
             Map.Entry<String, ? extends NamedElement> source,
             Transition transition, int depth) {
         List<TransitionNode<NamedElement>> transitions = Util.newList();

         // Get the target and determine what kind of state it is. If it's
         // a normal state, then we are done with the transition tree.
         //
         // If it's a special Vertex type (Pseudostate or
         // ConnectionPointReference), then we may need to continue
         // expanding the tree.
         //
         // Each helper routine is responsible for performing state-specific
         // processing and then recursing to this method to finish the traversal.
         Vertex target = transition.getTarget();

         // Handle all the pseudo states
         if (target instanceof Pseudostate) {
             Pseudostate state = (Pseudostate) target;
             PseudostateKind kind = state.getKind();

             Map.Entry<String, Pseudostate> targetEntry = new Entry<String, Pseudostate>(
                     source.getKey(), (Pseudostate) target);

             if (kind.equals(PseudostateKind.initial)) {
             } else if (kind.equals(PseudostateKind.deepHistory)) {
                 transitions.addAll(doDeepHistory(targetEntry, depth + 1));
             } else if (kind.equals(PseudostateKind.shallowHistory)) {
             } else if (kind.equals(PseudostateKind.join)) {
                 throw new UnsupportedOperationException();
             } else if (kind.equals(PseudostateKind.fork)) {
                 throw new UnsupportedOperationException();
             } else if (kind.equals(PseudostateKind.junction)) {
                 transitions.addAll(doJunctionState(targetEntry, depth + 1));
             } else if (kind.equals(PseudostateKind.choice)) {
             } else if (kind.equals(PseudostateKind.entryPoint)) {
                 transitions.addAll(doEntryPoint(targetEntry, depth + 1));
             } else if (kind.equals(PseudostateKind.exitPoint)) {
                 transitions.addAll(doExitPoint(targetEntry, depth + 1));
             } else if (kind.equals(PseudostateKind.terminate)) {

             } else {
                 throw new RuntimeException("Unknown pseudostate kind");
             }
         }

         // It may be a connection point reference into a submachine
         if (target instanceof ConnectionPointReference) {
             // [SWC 2009.09.24] what do we do here???
         }

         // Or, it may just be a transition into a state. In this case do
         // nothing and just return an empty list
         if (target instanceof State) {  // do nothing
         }

         return transitions;
     }

     /**
      * Generic handler for recursing on any Vertex
      */
     private List<TransitionNode<NamedElement>> doVertex (
             Map.Entry<String, ? extends Vertex> entry, int depth) {
         Vertex vertex = entry.getValue();

         List<TransitionNode<NamedElement>> transitions = Util.newList();
         Collection<Transition> outgoing = vertex.getOutgoing();
         int n = outgoing.size();

         int i = 0;
         for (Transition tran : outgoing) {
             // Recurse
             transitions.add(new TransitionNode<NamedElement>(entry, tran, true,
                     depth, i, n));
             transitions.addAll(transitionHelper(entry, tran, depth));
             transitions.add(new TransitionNode<NamedElement>(entry, tran,
                     false, depth, i, n));

             i++;
         }

         return transitions;
     }

     /**
      * An exit point must find its corresponding connection point reference,
      * we then defer to the connection point reference handler    
      */
     private List<TransitionNode<NamedElement>> doExitPoint (
             Map.Entry<String, Pseudostate> entry, int depth) {
         Pseudostate exit = entry.getValue();

         // Search all the connection point references for the one that has
         // an exit reference to this exitPoint state
         List<ConnectionPointReference> conn = Util.filter(ns2qname.keySet(),
                 ConnectionPointReference.class);

         ConnectionPointReference connOut = null;
         for (ConnectionPointReference ref : conn) {
             for (Pseudostate pseudo : ref.getExit())
                 if (pseudo.equals(exit))
                     connOut = ref;

             if (connOut != null)
                 break;
         }

         if (connOut == null)
             throw new RuntimeException(
                     "exit point has no corresponding connection point");

         Entry<String, ConnectionPointReference> next = new Entry<String, ConnectionPointReference>(
                 entry.getKey(), connOut);

         return doConnectionPointReference(next, depth);
     }

     private List<TransitionNode<NamedElement>> doEntryPoint (
             Map.Entry<String, ? extends Pseudostate> entry, int depth) {
         return doVertex(entry, depth);
     }

     private List<TransitionNode<NamedElement>> doDeepHistory (
             Map.Entry<String, ? extends Pseudostate> entry, int depth) {
         return doVertex(entry, depth);
     }

     private List<TransitionNode<NamedElement>> doConnectionPointReference (
             Map.Entry<String, ? extends ConnectionPointReference> entry,
             int depth) {
         List<TransitionNode<NamedElement>> transitions = Util.newList();
         ConnectionPointReference ref = entry.getValue();

         // If the entry is not empty, then we're going into a submachine, so
         // just defer to the entryPoint pseudostate
         if (!ref.getEntry().isEmpty()) {
             Pseudostate entryPoint = ref.getEntry().get(0);
             Entry<String, Pseudostate> next = new Entry<String, Pseudostate>(
                     entry.getKey(), entryPoint);

             transitions.addAll(doEntryPoint(next, depth));
         }

         // If the exit is not empty, then we're going out of a submachine. In
         // this case, we process the outgoing transitions of the connection
         // point itself
         else if (!ref.getExit().isEmpty()) {
             transitions.addAll(doVertex(entry, depth));
         }

         // Otherwise there are not transitions links
         else {
             throw new RuntimeException("ConnectionPointReference has no links");
         }

         return transitions;
     }

     /**
      * Handle the depth-first transition through a junction state.
      * The main criteria for a junction state is that there *must*
      * be guards on all but one of the outgoing transitions since
      * these are implemented as if/else statements
      */
     private List<TransitionNode<NamedElement>> doJunctionState (
             Map.Entry<String, Pseudostate> entry, int depth) {
         Pseudostate state = entry.getValue();

         // Run through the outgoing transitions and ensure that one and only
         // one of the transitions does not have a guard condition
         Transition elseTran = null;

         List<TransitionNode<NamedElement>> transitions = Util.newList();
         Collection<Transition> outgoing = state.getOutgoing();

         int n = outgoing.size();
         int i = 0;
         for (Transition tran : outgoing) {
             // If it has a guard, adda it to the front of the list
             if (tran.getGuard() != null) {
                 transitions.add(new TransitionNode<NamedElement>(entry, tran,
                         true, depth, i, n));
                 transitions.addAll(transitionHelper(entry, tran, depth));
                 transitions.add(new TransitionNode<NamedElement>(entry, tran,
                         false, depth, i, n));
             } else {
                 // Can't have two transitions without guards
                 if (elseTran != null)
                     throw new RuntimeException("Only one transition from a "
                             + "junction state can not have " + "a guard");

                 elseTran = tran;
             }

             i += 1;
         }

         // Add the last transition
         transitions.add(new TransitionNode<NamedElement>(entry, elseTran, true,
                 depth, i, n));
         transitions.addAll(transitionHelper(entry, elseTran, depth));
         transitions.add(new TransitionNode<NamedElement>(entry, elseTran,
                 false, depth, i, n));

         return transitions;
     }

}
