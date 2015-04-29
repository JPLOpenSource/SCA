/**
 * Created Jul 3, 2013.
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

import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import org.w3c.dom.Node;

/**
 * An instance value is a value specification that identifies an instance.  Its
 * value thus specifies the value modeled by an instance specification.  In
 * addition, a model (e.g., in MagicDraw) may reference any named element via
 * XMI model extension, and we've chosen to use this class as the closest
 * approximate to represent such a reference.
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
public class UMLInstanceValue extends UMLValueSpecification implements InstanceValue {

    /**
     * Instead of {@link InstanceSpecification}, the more generic {@link NamedElement}
     * type is used to allow for model that references any named element as
     * the element value.
     */
    protected NamedElement instance = null;

    private String instanceId = null;
    private Element instanceElement = null;

    /**
     * @param element
     * @param scape
     */
    public UMLInstanceValue (Node element, ModelScape scape) {
        super(element, scape);

        // Read either the 'element' or 'instance' attribute to get the instance ID
        instanceId = getAttribute(element, ReaderNamespaceContext.elementAttr());
        if (instanceId == null || instanceId.length() == 0) {
            instanceId = getAttribute(element, UMLLabel.KEY_INSTANCE);
            if (instanceId != null && instanceId.length() == 0) {
                instanceId = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.InstanceValue#instance()
     */
    public NamedElement instance () {
        if (instance == null && instanceElement == null) {
            if (instanceElement() != null && instanceElement() instanceof NamedElement) {
                instance = (NamedElement) instanceElement();
            }
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.InstanceValue#getInstanceId()
     */
    public String instanceId () {
        return instanceId;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLNamedElement#getName()
     */
    @Override
    public String getName () {
        if (instance() != null) {
            return instance().getName();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isNull()
     */
    @Override
    public boolean isNull () {
        return (instanceId == null || instanceElement == null);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#isSupported()
     */
    @Override
    public boolean isSupported () {
        return true;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#expression()
     */
    @Override
    public Expression expression () {
        Expression expr = null;

        if (instance() != null && instance() instanceof UMLConstraint) {
            // access the expression object of the constraint
            ValueSpecification spec = ((UMLConstraint) instance()).getSpecification();
            if (spec != null) {
                expr = spec.expression();
            }
        }

        return expr;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLValueSpecification#stringValue()
     */
    @Override
    public String stringValue () {
        String stringVal = null;

        if (instance() != null) {
            // Find the property instance and return the referenced property name
            if (instance() instanceof Property) {
                Property prop = (Property) instance();
                if (prop.getName() == null || prop.getName().length() == 0) {
                    // opt for the value string since no Property Name!
                    stringVal = prop.getValueString();
                }
            } else if (instance() instanceof Constraint) {
                // Find the Constraint's specification and return its string value
                ValueSpecification spec = ((Constraint) instance()).getSpecification();
                if (spec != null) {
                    stringVal = spec.stringValue();
                }
            }

            // Default to Name for Behavior, Property, ValueSpec without spec, etc.
            if (stringVal == null) {
                stringVal = instance().getName();
            }
        }

        return stringVal;
    }

    private Element instanceElement () {
        if (instanceElement == null) {
            instanceElement = xmi2uml(instanceId());
        }
        return instanceElement;
    }

}
