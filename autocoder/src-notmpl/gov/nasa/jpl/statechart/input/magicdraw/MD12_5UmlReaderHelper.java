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
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;


/**
 * This class defines the literals in support of MagicDraw UML 12.5, but there's
 * nothing to implement as the base class literals already apply to 12.5.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name=MDUmlReaderHelper.EXPORTER, version="12.5")
public final class MD12_5UmlReaderHelper extends MDUmlReaderHelper {

    public final String KEY_INCOMING = "incoming";
    public final String KEY_OUTGOING = "outgoing";

    /**
     * Default constructor overrides values defined in base reader class.
     */
    public MD12_5UmlReaderHelper () {
        super();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper#findStateInsAndOuts(StateChart.State, org.w3c.dom.Node, java.util.List)
     */
    @Override
    /*package*/ void findStateInsAndOuts(State thisState, Node startNode,
            List<Transition> transList) throws Exception {
        List<Node> incoming = new ArrayList<Node>();
        List<Node> outgoing = new ArrayList<Node>();

        // Search child nodes of startNode for in or out transition definitions:
        reader.findNodes(incoming, uml().lit(UMLLabel.TAG_INCOMING), XMIIdentifiers.idref(), startNode, 1);
        reader.findNodes(outgoing, uml().lit(UMLLabel.TAG_OUTGOING), XMIIdentifiers.idref(), startNode, 1);
        // Check if the starting node contains an incoming attribute:
        if (null != startNode.getAttributes()
                && null != startNode.getAttributes().getNamedItem(KEY_INCOMING)) {
            thisState.addIncoming(State.findTransition(
                    transList,
                    startNode.getAttributes().getNamedItem(KEY_INCOMING).getNodeValue()));
        }
        // Now search the list of child nodes defining incoming transitions:
        for (Node thisNode : incoming) {
            String id = thisNode.getAttributes().getNamedItem(XMIIdentifiers.idref()).getNodeValue();
            Transition inTran = State.findTransition(transList, id);
            thisState.addIncoming(inTran);
        }
        
        // Check if the starting node contains an outgoing attribute:
        if (null != startNode.getAttributes()
                && null != startNode.getAttributes().getNamedItem(KEY_OUTGOING)) {
            thisState.addOutgoing(State.findTransition(
                    transList,
                    startNode.getAttributes().getNamedItem(KEY_OUTGOING).getNodeValue()));
        }
        // Now search the list of child nodes defining outgoing transitions:
        for (Node thisNode : outgoing) {
            thisState.addOutgoing(State.findTransition(
                    transList,
                    thisNode.getAttributes().getNamedItem(XMIIdentifiers.idref()).getNodeValue()));
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper#refinePseudoState(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    /*package*/ State refinePseudoState (String kindOfPseudostate, String name, String id) {
        State newState = null;
        if (kindOfPseudostate.equalsIgnoreCase(uml().lit(UMLLabel.VAL_INITIAL))) {
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
        String kindOfTransition = Util.getNodeAttribute(transNode, uml().lit(UMLLabel.KEY_KIND));
        if (eventName.endsWith("Pev")
                && !uml().lit(UMLLabel.VAL_INTERNAL).equals(kindOfTransition)) {
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
            String exprBody = Util.getNodeAttribute(when, uml().lit(UMLLabel.KEY_BODY));
            if (exprBody != null) {
                return exprBody;
            }
        }
        // Any other case is an exception
        throw new Exception(
                "Invalid XMI file format - UML:TimeEvent ID="
                + Util.getNodeAttribute(timeoutEventNode, XMIIdentifiers.id())
                + " found with no timeout expression.");
    }

}
