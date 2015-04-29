/**
 * Created Sep 28, 2009.
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
package gov.nasa.jpl.statechart.model.visitor;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.uml.ConnectionPointReference;
import gov.nasa.jpl.statechart.uml.FinalState;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.Pseudostate;
import gov.nasa.jpl.statechart.uml.Region;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.TimeEvent;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLConnectionPointReference;
import gov.nasa.jpl.statechart.uml.UMLNamedElement;

import java.util.Map;

/**
 * Assigns names to unnamed UML elements, also disambiguating them, so that
 * autocode processing can continue.  States and Pseudostates are named
 * sequentially over the whole model; regions are named by depth; and TimeEvents
 * are named by their originating state.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Ed or Lucas, adapted by Shang-Wen Cheng <scheng@jpl.nasa.gov>
 * for new template-based implementation.
 *
 */
public class AnonymousNameInjector extends AbstractVisitor<Void> {
    private static final long serialVersionUID = 3630973041675634587L;
    // Anonymous element counters. If the traversal encounters
    // an element with no name, then we generate one automatically.
    // We keep separate region counter by tree depth (rooted at state machine)
    private int depth = 0;
    private Map<Integer,Integer> regionCount = Util.newMap();
    private int pseudoCount = 0;
    private int stateCount = 0;
    private int finalStateCount = 0;

    @Override
    public void visit (StateMachine stateMachine) {
        // clear the counter maps
        regionCount.clear();
        // reset depth to root
        depth = 0;
        // assign first counter map
        regionCount.put(depth, 1);
    }

    /**
     * Injects a state name if it doesn't have one, and also inject each of
     * its outgoing time-events with name based on the state.
     */
    @Override
    public void visit (State state) {
        String name = state.getName();

        if (name == null || name.length() == 0) {
            name = "State" + stateCount;
            ((UMLNamedElement) state).setName(name);

            ++stateCount;  // increment counter
        }

        // populate timer event mapping using the name of the source state of transition
        int timerCounter = 1;
        for (Transition transition : state.getOutgoing()) {
            for (TimeEvent event : transition.getTimeEvents()) {
                /* determine name of counter: "<state>" for first counter,
                 *  "<state><cnt>" for second counter and above.
                 */
                String timerName = name;
                if (timerCounter > 1) {
                    timerName += timerCounter;
                }
                ((UMLNamedElement) event).setName(timerName);
                ++timerCounter;
            }
        }

        // populate connection pt refs with name of referenced connection pts
        int cprCounter = 1;
        for (ConnectionPointReference cpr : state.getConnection()) {
            String cprName = null;
            if (cpr.getEntry().size() > 0) {
                // get first referenced entryPoint and use its name
                cprName = cpr.getEntry().iterator().next().getName();
            } else if (cpr.getExit().size() > 0) {
                // get first referenced exitPoint and use its name
                cprName = cpr.getExit().iterator().next().getName();
            } else {  // inject based on state name
                cprName = name + "_cpr" + cprCounter;
                ++cprCounter;
            }
            ((UMLConnectionPointReference) cpr).setName(cprName);
        }
    }

    /**
     * Special treatment for FinalState, keeping a separate counter but not
     * having to worry about timer events.
     */
    @Override
    public void visit (FinalState finalState) {
        String name = finalState.getName();

        if (name == null || name.length() == 0) {
            name = "FinalState" + finalStateCount;
            ((UMLNamedElement) finalState).setName(name);

            ++finalStateCount;  // increment counter
        }
    }

    @Override
    public void visit (Region region) {
        String name = region.getName();

        if (name == null || name.length() == 0) {
            int count = regionCount.get(depth);
            ((UMLNamedElement) region).setName("Region" + count);
            regionCount.put(depth, count + 1);
        }
    }

    @Override
    public void visit (Pseudostate state) {
        String name = state.getName();

        if (name == null || name.length() == 0) {
            name = "Pseudo" + pseudoCount;
            ((UMLNamedElement) state).setName(name);
            ++pseudoCount;
        }
    }

    @Override
    public void moveDown (NamedElement from, NamedElement to) {
        super.moveDown(from, to);

        ++depth;
        if (!regionCount.containsKey(depth)) {
            regionCount.put(depth, 1);
        }
    }

    @Override
    public void moveUp (NamedElement from, NamedElement to) {
        super.moveUp(from, to);

        --depth;
    }

    /**
     * Always descend into SubMachines to inject names.
     */
    @Override
    public boolean expandSubmachines () {
        return true;
    }

    /**
     * Always descend into orthogonal regions to inject names.
     */
    @Override
    public boolean expandOrthogonalRegions () {
        return true;
    }

}