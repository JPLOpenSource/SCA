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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.model.PrefixOrderedWalker;
import gov.nasa.jpl.statechart.model.diagram.DiagramData;
import gov.nasa.jpl.statechart.model.diagram.DiagramElement;
import gov.nasa.jpl.statechart.model.diagram.MachineElement;
import gov.nasa.jpl.statechart.model.diagram.SeparatorElement;
import gov.nasa.jpl.statechart.model.diagram.TextElement;
import gov.nasa.jpl.statechart.model.diagram.TransitionElement;
import gov.nasa.jpl.statechart.model.diagram.VertexElement;
import gov.nasa.jpl.statechart.model.visitor.TransitionVisitor;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.Vertex;

import java.util.Collection;

/**
 * Velocity Model that provides query methods to facilitate autocode tasks
 * for execution trace GUI, which require accessing the diagram elements.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 */
public class TraceGuiVelocityModel extends AbstractVelocityModel {

    /** Diagram data for trace GUI */
    protected DiagramData data = null;

    private Collection<Vertex> cachedStates = null;

    /**
     * Main constructor.
     * 
     * @param myModelGrp  the UML Model group from which to autocode
     * @param myData      diagram data for GUI code
     */
    public TraceGuiVelocityModel (StateMachine myStateMachine, DiagramData myData) {
        super();

        // Initialize the variables
        setStateMachine(myStateMachine);
        data = myData;
    }

    /**
     * Collects and caches all the UML States and Pseudostates defined in this
     * State Machine, prefix-ordered.
     * <p>
     * Note: Do NOT descend into submachines, or else submachine elements will
     * clutter the main machine diagram.
     * </p>
     * @return  Collection of Vertex.
     */
    public Collection<Vertex> getAllStates () {
        if (cachedStates == null) {
            cachedStates = Util.newList();
            for (Vertex v : getVertices(getStateMachine(), false)) {
                if (getVertexDiagramElement(v) != null) {
                    // only care about those with diagram elements
                    cachedStates.add(v);
                }
            }
        }

        return cachedStates;
    }

    /**
     * Collects all the UML Transitions in this State-Machine, prefix ordered.
     * <p>
     * Note: Do NOT descend into submachines, or else submachine elements will
     * clutter the main machine diagram.
     * </p>
     * @return
     */
    public Collection<Transition> getAllTransitions () {
        return PrefixOrderedWalker.traverse(getStateMachine(), new TransitionVisitor(false));
    }

    /**
     * Returns the State Machine diagram element.
     * @return
     */
    public MachineElement getMachineDiagramElement () {
        return getDiagramElement(MachineElement.class, getStateMachine());
    }

    /**
     * Returns the diagram element for the supplied Vertex (Pseudostate or State).
     * @param v  UML Vertex whose diagram element to retrieve
     * @return
     */
    public VertexElement getVertexDiagramElement (Vertex v) {
        return getDiagramElement(VertexElement.class, v);
    }

    /**
     * Returns the diagram element for the supplied Transition.
     * @param t  UML Transition whose diagram element to retrieve
     * @return
     */
    public TransitionElement getTransitionDiagramElement (Transition t) {
        return getDiagramElement(TransitionElement.class, t);
    }

    /**
     * Returns all the diagram separator elements for this state machine.
     * @return
     */
    public Collection<SeparatorElement> getSeparatorElements () {
        return data.separatorElementMap.get(getStateMachine().id());
    }

    /**
     * Returns all the diagram textbox elements for this state machine.
     * @return
     */
    public Collection<TextElement> getTextElements () {
        return data.textElementMap.get(getStateMachine().id());
    }

    /**
     * Using generics, returns the specific implementation of DiagramElement,
     * looked-up using the XMI ID of the supplied UML NamedElement.
     * @param <T>  Type of the desired subclass of {@link DiagramElement} to retrieve
     * @param clazz  desired subclass of {@link DiagramElement} to retrieve
     * @param ne  UML NamedElement for which to retrieve a diagram element 
     * @return
     */
    private <T extends DiagramElement> T getDiagramElement (Class<T> clazz, NamedElement ne) {
        DiagramElement de = data.elementMap.get(ne.id());
        if (de == null) {
            Util.debug(ne.getQualifiedName() + " (ID " + ne.id()
                    + ") has no corresponding Diagram element!");
            return null;
        }

        // see if retrieved DiagramElement is the proper subtype
        if (clazz.isAssignableFrom(de.getClass())) {
            return clazz.cast(de);
        } else {
            Util.warn(ne.getQualifiedName() + " (ID " + ne.id()
                    + ") did NOT retrieve the expected diagram "
                    + clazz.getSimpleName() + "!");
            return null;
        }
    }

}
