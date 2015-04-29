/**
 * Created prior to Jun 30, 2009.
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A connection point reference represents a usage (as part of a submachine
 * state) of an entry/exit point defined in the statemachine reference by the
 * submachine state.
 * <p>
 * Connection point references of a submachine state can be used as
 * sources/targets of transitions. They represent entries into or exits out of
 * the submachine state machine referenced by the submachine state.
 * </p><p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 */
public class UMLConnectionPointReference extends UMLVertex implements
        ConnectionPointReference {

    List<Pseudostate> entry = null;
    List<Pseudostate> exit = null;

    public UMLConnectionPointReference (Node element, ModelScape scape) {
        super(element, scape);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ConnectionPointReference#getEntry()
     */
    public List<Pseudostate> getEntry () {
        try {
            if (entry == null) {
                entry = Util.newList();

                NodeList entries = (NodeList) xpath.evaluate(
                        UMLIdentifiers.inst().lit(UMLLabel.TAG_ENTRY),
                        domElement, XPathConstants.NODESET);

                for (int i = 0; i < entries.getLength(); i++) {
                    String idref = getAttribute(entries.item(i), XMIIdentifiers.idref());
                    entry.add((Pseudostate) xmi2uml(idref));
                }
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLConnectionPointReference.getEntry(): ");
        }

        return Collections.unmodifiableList(entry);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ConnectionPointReference#getExit()
     */
    public List<Pseudostate> getExit () {
        try {
            if (exit == null) {
                exit = Util.newList();

                NodeList exits = (NodeList) xpath.evaluate(
                        UMLIdentifiers.inst().lit(UMLLabel.TAG_EXIT),
                        domElement, XPathConstants.NODESET);

                for (int i = 0; i < exits.getLength(); i++) {
                    String idref = getAttribute(exits.item(i), XMIIdentifiers.idref());
                    exit.add((Pseudostate) xmi2uml(idref));
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return Collections.unmodifiableList(exit);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ConnectionPointReference#getState()
     */
    public State getState () {
        return (State) xmi2uml(getAttribute(domElement.getParentNode(), XMIIdentifiers.id()));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.ConnectionPointReference#getKind()
     */
    public PseudostateKind getKind () {
        if (getEntry().size() > 0) {
            return PseudostateKind.entryPoint;
        } else {
            return PseudostateKind.exitPoint;
        }
    }

}
