/**
 * Created Sep 29, 2009, for housing traverse methods from VelocityModel.
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
package gov.nasa.jpl.statechart.model;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.visitor.Visitor;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Utility class of methods for performing breadth-first traversal of UML Model. 
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>, moved existing methods by Eddie from VelocityModel
 *
 */
public abstract class BreadthFirstWalker {

    /**
     * Traverses the UML model in a breadth-first fashion, allowing subset of
     * States (or any other {@link NamedElement}) to be collected breadth-first.
     * 
     * @param <T>  {@link Visitor} implementation class for visiting each node
     * @param element  root {@link NamedElement} to begin traversal.
     * @param v  implementation object of {@link Visitor}.
     * @return the same {@link Visitor} object supplied as <code>v</code>.
     */
    public static <T extends Visitor> T traverse (NamedElement element, T v) {
        // keep a map to path distance so we know we've changed depth
        Map<NamedElement,Integer> distMap = Util.newMap();
        // use a Queue and store the root vertex
        Queue<NamedElement> queue = new LinkedList<NamedElement>();
        queue.add(element);
        distMap.put(element, 0);
    
        NamedElement lastNE = element;
        while (!queue.isEmpty()) {
            // dequeue the head of the queue for visiting
            NamedElement ne = queue.poll();
            if (v.isVisiting(ne)
                    || !v.expandOrthogonalRegions()) continue;  // don't revisit

            // check if we're moving "up" or "down" in tree depth
            int depthDelta = distMap.get(ne) - distMap.get(lastNE);
            if (depthDelta > 0) {  // we've increased in dist, so moved down!
                v.moveDown(lastNE, ne);
            } else if (depthDelta < 0) {  // decreased in dist, so moved up.
                v.moveUp(lastNE, ne);
            }  // nothing to signal if same depth

            // visit the vertex at head of queue
            if (ne instanceof StateMachine) {
                v.visit((StateMachine) ne);
            } else if (ne instanceof Region) {
                v.visit((Region) ne);
            } else if (ne instanceof FinalState) {
                v.visit((FinalState) ne);
            } else if (ne instanceof State) {
                v.visit((State) ne);
            } else if (ne instanceof Pseudostate) {
                v.visit((Pseudostate) ne);
            }

            // find immediate neighbors for search of next depth
            for (NamedElement neighbor : neighbors(ne, v)) {
                // compute distance
                distMap.put(neighbor, distMap.get(ne) + 1);
                // queue the immediate neighbor
                queue.offer(neighbor);
            }
            lastNE = ne;
        }

        return v;
    }

    /**
     * Returns a collection of the neighboring vertices at the next depth of
     * the specified {@link NamedElement} vertex.  Note that the following are
     * also considered "neighbors": <ul>
     * <li> ConnectionPoints on StateMachines and States
     * <li> SubMachines of Substates
     * </ul>
     * @param ne  vertex NamedElement.
     * @param v   implementation object of {@link Visitor}.
     * @return  Collection of neighbor NameElements.
     */
    private static Collection<? extends NamedElement> neighbors (NamedElement ne, Visitor v) {
        Collection<NamedElement> nes = Util.newList();
        if (ne instanceof StateMachine) {
            StateMachine sm = (StateMachine) ne;
            nes.addAll(sm.getRegion());
            nes.addAll(sm.getConnectionPoint());
        }
        if (ne instanceof Region) {
            nes.addAll(((Region) ne).getSubvertex());
        }
        if (ne instanceof State) {
            State s = (State) ne;
            // visit the connection points, but do NOT visit ConnectionPointRefs,
            // as that causes submachine's entry/exitPoints to show up on main SM
            nes.addAll(s.getConnectionPoint());
            nes.addAll(s.getRegion());
            if (s.isSubmachineState() && v.expandSubmachines()) {
                StateMachine stateMachine = s.getSubmachine();
                if (stateMachine != null) {
                    nes.add(stateMachine);
                }
            }
        }
        if (ne instanceof Vertex && v.followOutTransitions()) {
            // follow all transitions to target Vertex
            Vertex vertex = (Vertex) ne;
            for (Transition t : vertex.getOutgoing()) {
                nes.add(t.getTarget());
            }
        }
        return nes;
    }

}
