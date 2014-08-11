/**
 * Created Jul 24, 2009.
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
package gov.nasa.jpl.statechart.input.magicdraw;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.core.DeepHistoryState;
import gov.nasa.jpl.statechart.core.EntryPoint;
import gov.nasa.jpl.statechart.core.ExitPoint;
import gov.nasa.jpl.statechart.core.InitialState;
import gov.nasa.jpl.statechart.core.JunctionState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;

import java.util.List;

import org.w3c.dom.Node;



/**
 * This class defines the literals and version-specific functions in support of
 * reading MagicDraw UML 16.0.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name=MDUmlReaderHelper.EXPORTER, version="16.0")
public class MD16_0UmlReaderHelper extends MDUmlReaderHelper {

    /**
     * Default constructor overrides values defined in base reader class.
     */
    public MD16_0UmlReaderHelper () {
        super();
    }

    
    /**
     * Finds the incoming and outgoing transitions to this state, looking for
     * XMI tags specific to MagicDraw UML version 12.5.
     * 
     * @param thisState
     *            The state for which we're finding incoming and outgoing transitions.
     * @param startNode
     *            The node to search at and below.
     * @param transList
     *            The list of transitions to correlate with the state's incomings and outgoings.
     * @throws Exception
     * @see {@link MagicDrawUmlReader#findStateInsAndOuts()}
     */
    /*package*/ void findStateInsAndOuts(State thisState, Node startNode,
            List<Transition> transList) throws Exception {
        // Search through transitions for ones that specify this state as target
        for (Transition t : transList) {
            if (thisState.id().equalsIgnoreCase(t.targetId())
                    && !thisState.hasIncoming(t.id())) {
                thisState.addIncoming(t);
            }
        }
        // Search through transitions for ones that specify this state as source
        for (Transition t : transList) {
            if (thisState.id().equalsIgnoreCase(t.sourceId())
                    && !thisState.hasOutgoing(t.id())) {
                thisState.addOutgoing(t);
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper#refinePseudoState(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    /*package*/ State refinePseudoState (String kindOfPseudostate, String name, String id) {
        State newState = null;
        // In 16.0, a PseudoState is implied to be of the "initial" kind
        // when the kind attribute is unspecified, i.e., null.
        if (kindOfPseudostate == null
                || kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_INITIAL))) {
            newState = new InitialState(makeUniqueName("Initial"), id);
        } else if (kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_JUNCTION))) {
            newState = new JunctionState(makeUniqueName("Junction"), id);
        } else if (kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_DEEPHISTORY))) {
            newState = new DeepHistoryState(makeUniqueName("DeepHistory"), id);
        } else if (kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_ENTRYPOINT))) {
            // System.out.println( "Creating new EntryPoint, id = " + id);
            if (name == null) {
                newState = new EntryPoint(makeUniqueName("EntryPoint"), id);
            } else {
                newState = new EntryPoint(name, id);
            }
        } else if (kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_EXITPOINT))) {
            // System.out.println( "Creating new ExitPoint, id = " + id);
            if (name == null) {
                newState = new ExitPoint(makeUniqueName("ExitPoint"), id);
            } else {
                newState = new ExitPoint(name, id);
            }
        }
        return newState;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper#checkSafeEventOnTransition(org.w3c.dom.Node, java.lang.String)
     */
    @Override
    /*package*/ void checkSafeEventOnTransition (Node transNode, String eventName)
            throws Exception {
        /* In 16.0, transition kind is a hidden attribute by default, and its
         * value can be either "external" or "local", default "external".
         */
        String kindOfTransition = Util.getNodeAttribute(transNode, uml().lit(UMLLabel.KEY_KIND));
        if (eventName.endsWith("Pev")
                && !uml().lit(UMLLabel.VAL_LOCAL).equals(kindOfTransition)) {
            System.out.println("Violation! A safe event named \"" + eventName
                    + "\" was used on an external transition");
            System.out.println("Safe events can only be used on local(internal) transitions");
            throw new Exception("Safe event violation");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper#getTimeOutExpression(org.w3c.dom.Node)
     */
    @Override
    /*package*/ String getTimeOutExpression (Node timeoutEventNode) throws Exception {
        Node when = reader.findNode(uml().lit(UMLLabel.TAG_WHEN), timeoutEventNode, 1);
        if (when != null) {
            Node expr = reader.findNode(uml().lit(UMLLabel.TAG_EXPR), when, 1);
            if (expr != null) {
                String exprBody = Util.getNodeAttribute(expr, uml().lit(UMLLabel.KEY_BODY));
                if (exprBody != null) {
                    return exprBody;
                }
            }
        }
        return "";
        // Any other case is an exception
//        throw new Exception(
//                "Invalid XMI file format - UML:TimeEvent ID="
//                + Util.getNodeAttribute(timeoutEventNode, XMIIdentifiers.id())
//                + " found with no timeout expression.");
    }

}
