/**
 * Created Aug 08, 2013.
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
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An instance specification specifies existence of an entity in a modeled
 * system and completely or partially describes the entity.
 * InstanceSpecification is a concrete class.
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
public class UMLInstanceSpecification extends UMLPackageableElement implements InstanceSpecification {

    protected Collection<Classifier> classifier = Util.newList();
    protected Collection<Slot> slot = Util.newList();
    protected ValueSpecification specification = null;

    /**
     * @param element
     * @param scape
     */
    public UMLInstanceSpecification (Node element, ModelScape scape) {
        super(element, scape);

        try {
            // fetch any Classifier
            NodeList classifierNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.inst().lit(UMLLabel.TAG_SLOT),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < classifierNodes.getLength(); i++) {
                String idref = getAttribute(classifierNodes.item(i), XMIIdentifiers.idref());
                classifier.add((Classifier) xmi2uml(idref));
            }

            // fetch optional Value Specification
            Node specNode = (Node) xpath.evaluate(
                    getValueSpecificationXpath(UMLLabel.TAG_SPECIFICATION),
                    element, XPathConstants.NODE);
            if (specNode != null) {
                specification = getValueSpecificationSubtype(specNode);
            }

            // fetch all Slots
            NodeList slotNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_SLOT, UMLLabel.TYPE_SLOT),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < slotNodes.getLength(); i++) {
                UMLSlot newSlot = new UMLSlot(slotNodes.item(i), scape);
                slot.add(newSlot);
                newSlot.setOwningInstance(this);
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLInstanceSpecification constructor: ");
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.InstanceSpecification#classifier()
     */
    public Collection<Classifier> classifier () {
        return classifier;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.InstanceSpecification#slot()
     */
    public Collection<Slot> slot () {
        return slot;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.InstanceSpecification#specification()
     */
    public ValueSpecification specification () {
        return specification;
    }

}
