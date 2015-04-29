/**
 * Created Oct 16, 2013.
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.model.visitor;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.template.AbstractVelocityModel;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.Constraint;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.PseudostateKind;
import gov.nasa.jpl.statechart.uml.SignalEvent;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLConstraint;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Visitor to collect all paths of Transition hops from a source Vertex,
 * through any number of junction or choice Pseudostates, to a target State.
 * <br/>
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
 *
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class TransitionPathVisitor extends AbstractVisitor<TransitionPathVisitor.TransitionPath> {
    private static final long serialVersionUID = 4658122447161835092L;

    private static final Set<PseudostateKind> CHOICE_KINDS =
            Util.newSet(Arrays.asList(PseudostateKind.choice, PseudostateKind.junction));

    public static class TransitionPath extends ArrayList<Transition> {
        private static final long serialVersionUID = 8109869546258299119L;

        private boolean tElsePath = false;

        public TransitionPath () {
            super();
        }

        public TransitionPath (Transition t) {
            super();
            add(t);
        }

        public void setIsElsePath (boolean newFlag) {
            tElsePath = newFlag;
        }

        public boolean isElsePath () {
            return tElsePath;
        }

        public boolean isInternal () {
            if (size() > 0) {  // has at least a Transition
                return get(0).isInternal();
            }
            return false;
        }

        public Vertex getSource () {
            return (size() > 0) ? get(0).getSource() : null;
        }

        public Vertex getTarget () {
            return (size() > 0) ? get(size()-1).getTarget() : null;
        }

        // Specify startIndex > 0 if skipping initial hops of guards.
        public Constraint[] getGuards (int startIndex) {
            if (isElsePath()) {
                return new Constraint[]{ UMLConstraint.ELSE_CONSTRAINT };
            } else {
                List<Constraint> guards = Util.newList();
                for (Transition t: this) {
                    if (t.getGuard() != null
                            && t.getGuard().getSpecification() != null
                            && t.getGuard().getSpecification().getName() != null) {
                        guards.add(t.getGuard());
                    }
                }
                return guards.subList(startIndex, guards.size()).toArray(new Constraint[0]);
            }
        }

        // allow only ONE effect per transition path
        public Behavior getEffect () {
            List<Behavior> effects = Util.newList();
            for (Transition t: this) {
                if (t.getEffect() != null) {
                    effects.add(t.getEffect());
                }
            }
            if (effects.size() > 1) {  // report error!
                Util.error("ERROR! Each Junction Transition path can have only one Effect!");
            }
            if (effects.size() > 0) {
                return effects.iterator().next();
            } else {
                return null;
            }
        }

        public List<SignalEvent> getSignalEvents () {
            List<SignalEvent> signalEvents = Util.newList();
            for (Transition t: this) {
                signalEvents.addAll(t.getSignalEvents());
            }
            return signalEvents;
        }

        public List<TimeEvent> getTimeEvents () {
            List<TimeEvent> timeEvents = Util.newList();
            for (Transition t: this) {
                timeEvents.addAll(t.getTimeEvents());
            }
            return timeEvents;
        }

        public boolean isNullEvent () {
            return (getSignalEvents().size() == 0 && getTimeEvents().size() == 0);
        }
    }


    // Query model
    private AbstractVelocityModel tModel = null;

    // Boolean indicating first-time access
    private boolean tFirstTime = true;

    // A Map of target Vertex to TransitionPath for ease of determining progress
    private Map<Vertex,TransitionPath> tVertexPathMap = new LinkedHashMap<Vertex,TransitionPathVisitor.TransitionPath>();


    public TransitionPathVisitor (AbstractVelocityModel model) {
        // no desent into SubMachine, nor need to include orthogonal regions
        super(false, OrthoRegion.STOP_AT_ORTHO);

        tModel = model;
    }

    /**
     * This visitor requires Walker to following outgoing Transitions.
     *
     * But ONLY until all target Vertices are proper States, in which case,
     * traverse no more.  This Visitor will hence work more efficiently with
     * a Breadth-first walker.  At worst, extraneous peer transition hops will
     * be performed for transition hops up to and including the longest path
     * to a proper state in the graph.
     *
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#followOutTransitions()
     */
    @Override
    public boolean followOutTransitions () {
        boolean morePseudos = false;
        for (Vertex vertex : tVertexPathMap.keySet()) {
            if (vertex instanceof Pseudostate) {
                morePseudos = true;
            }
        }
        return morePseudos;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.Pseudostate)
     */
    @Override
    public void visit (Pseudostate pseudostate) {
        buildTransitionPaths(pseudostate);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.model.visitor.AbstractVisitor#visit(gov.nasa.jpl.statechart.uml.State)
     */
    @Override
    public void visit (State state) {
        // see if state is Source or Target
        if (tVertexPathMap.containsKey(state)) {  // Target!
            // wrap up the TransitionPath by removal from map
            tVertexPathMap.remove(state);
        } else {  // Source, fan out!
            buildTransitionPaths(state);
        }
    }

    public TransitionPath primeWalk (Vertex source, Transition transOfInterest) {
        // find TransitionPath ending in this pseudostate
        TransitionPath tpath = tVertexPathMap.get(source);
        if (tpath == null) {
            if (tFirstTime) {  // fan out from Source Vertex
                tpath = new TransitionPath();
                tFirstTime = false;
            } else {  // otherwise, traverse no more!
                return null;
            }
        } else {
            // remove it from vertex-Path Map and visitor list to clone new ones
            tVertexPathMap.remove(source);
            remove(tpath);
        }

        if (transOfInterest != null) {  // establish the Vertex to walk from!
            tpath.add(transOfInterest);
            // add TransitionPath to visitor list and map from target Vertex
            add(tpath);
            tVertexPathMap.put(transOfInterest.getTarget(), tpath);
        }

        return tpath;
    }

    private void buildTransitionPaths (Vertex source) {
        // find TransitionPath ending in this Vertex
        TransitionPath tpath = primeWalk(source, null);
        if (tpath == null) {  // otherwise, traverse no more!
            return;
        }

        if (source instanceof Pseudostate && CHOICE_KINDS.contains(((Pseudostate) source).getKind())) {
            Iterator<Transition> junctionIter = tModel.getJunctionTransitions((Pseudostate) source).iterator();
            while (junctionIter.hasNext()) {
                Transition juncTrans = junctionIter.next();

                TransitionPath newTpath = (TransitionPath) tpath.clone();
                newTpath.add(juncTrans);

                if (!junctionIter.hasNext()) {  // last transition out, the "else"!
                    newTpath.setIsElsePath(true);
                }

                // add TransitionPath to visitor list and map from target Vertex
                add(newTpath);
                tVertexPathMap.put(juncTrans.getTarget(), newTpath);
            }
        } else {
            for (Transition t : source.getOutgoing()) {
                TransitionPath newTpath = (TransitionPath) tpath.clone();
                newTpath.add(t);

                // add TransitionPath to visitor list and map from target Vertex
                add(newTpath);
                tVertexPathMap.put(t.getTarget(), newTpath);
            }
        }
    }

    public boolean isInternal () {
        if (size() > 0) {  // has at least a TransitionPath
            return get(0).isInternal();
        }
        return false;
    }

    // Returns source state from which all paths emerge
    public Vertex getSource () {
        return (size() > 0) ? get(0).getSource() : null;
    }

    // Returns array of target states
    public Vertex[] getTargets () {
        List<Vertex> targets = Util.newList();
        for (TransitionPath transPath : this) {
            targets.add(transPath.getTarget());
        }
        return targets.toArray(new Vertex[0]);
    }

    // Returns a matrix of guards, each "row" being for a different Path.
    public Constraint[][] getGuardMatrix () {
        Constraint[][] matrix = new Constraint[size()][];
        for (int i=0 ; i < size() ; ++i) {
            matrix[i] = get(i).getGuards(0);
        }

        // see whether to just return NULL
        if (matrix.length > 0 && matrix[0].length > 0) {
            return matrix;
        } else {
            return null;
        }
    }

    // Returns one Effect Behavior object per path
    public Behavior[] getEffects () {
        List<Behavior> effects = Util.newList();
        for (TransitionPath transPath: this) {
            effects.add(transPath.getEffect());
        }
        return effects.toArray(new Behavior[0]);
    }

    public List<SignalEvent> getSignalEvents () {
        List<SignalEvent> signalEvents = Util.newList();
        for (TransitionPath transPath: this) {
            signalEvents.addAll(transPath.getSignalEvents());
        }
        if (signalEvents.size() > 1) {  // report error!
            Util.error("ERROR! Each Junction Transition network can contain only one SignalEvent!");
        }
        return signalEvents;
    }

    public List<TimeEvent> getTimeEvents () {
        List<TimeEvent> timeEvents = Util.newList();
        for (TransitionPath transPath: this) {
            timeEvents.addAll(transPath.getTimeEvents());
        }
        if (timeEvents.size() > 1) {  // report error!
            Util.error("ERROR! Each Junction Transition network can contain only one TimeEvent!");
        }
        return timeEvents;
    }

    public boolean isNullEvent () {
        return (getSignalEvents().size() == 0 && getTimeEvents().size() == 0);
    }

}
