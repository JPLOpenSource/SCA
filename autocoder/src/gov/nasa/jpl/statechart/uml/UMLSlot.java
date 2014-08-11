/**
 * Created Sep 21, 2013.
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A slot specifies that an entity modeled by an instance specification has a
 * value or values for a specific structural feature.
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
public class UMLSlot extends UMLElement implements Slot {

    protected Property definingFeature = null;
    protected InstanceSpecification owningInstance = null;
    protected Collection<ValueSpecification> value = Util.newList();

    /**
     * @param element
     * @param scape
     */
    public UMLSlot (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // fetch all values
            NodeList valueNodes = (NodeList) xpath.evaluate(
                    getValueSpecificationXpath(UMLLabel.TAG_VALUE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < valueNodes.getLength(); i++) {
                value.add(getValueSpecificationSubtype(valueNodes.item(i)));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLSlot constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Slot#definingFeature()
     */
    public Property definingFeature () {
        if (definingFeature == null) {  // lazy init: acquire referenced feature
            definingFeature = (Property) xmi2uml(Util.getNodeAttribute(domElement, ReaderNamespaceContext.definingAttr()));
        }
        return definingFeature;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Slot#owningInstance()
     */
    public InstanceSpecification owningInstance () {
        return owningInstance;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Slot#value()
     */
    public Collection<ValueSpecification> value () {
        return value;
    }

    public void setOwningInstance (InstanceSpecification owningInst) {
        owningInstance = owningInst;
    }

}
