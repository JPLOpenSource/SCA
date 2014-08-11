/**
 * A TimeEvent specifies a point in time. At the specified time, the event
 * occurs.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class UMLTimeEvent extends UMLEvent implements TimeEvent {

    private ValueSpecification when = null;
    // UML spec 2+ designates relative to be 'false' by default.
    private boolean isRelativeFlag = false;
    private String isRelativeStr = null;  // NON-UML: storage of relative string

    public UMLTimeEvent (Node element, ModelScape scape) {
        super(element, scape);

        try {
            when = getValueSpecificationSubtype(UMLIdentifiers.inst().timeEvent_getTimingExpression(element));
            if (when == null) {
                if (Util.isDebugLevel()) {
                    Util.debug("Warning: TimeEvent does not specify a recognizable Expression subtype (Opaque, Literal, or Instance)! ID '" + id + "'");
                }
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLTimeEvent constructor: ");
        }
    }

    public Signal getSignal () {
        return (Signal) xmi2uml(getAttribute(domElement, UMLLabel.KEY_SIGNAL));
    }

    public ValueSpecification getWhen () {
        // determine what value specification to return
        if (when instanceof InstanceValue) {
            NamedElement whenElem = ((InstanceValue) when).instance();
            if (whenElem != null) {
                if (whenElem instanceof Property) {
                    // Return property default value ValueSpecification
                    return ((Property) whenElem).getDefaultValue();
                } else if (whenElem instanceof Constraint) {
                    // Find the Constraint's ValueSpecification
                    return ((Constraint) whenElem).getSpecification();
                }
            }
        }
        // otherwise, return when itself
        return when;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.TimeEvent#isRelative()
     */
    public boolean isRelative () {
        if (isRelativeStr == null) {
            isRelativeStr = getAttribute(domElement, UMLLabel.KEY_IS_RELATIVE);
            if ("true".equalsIgnoreCase(isRelativeStr) || "false".equalsIgnoreCase(isRelativeStr)) {
                // if attribute found AND valid; otherwise, default is returned
                isRelativeFlag = Boolean.parseBoolean(isRelativeStr);
            }
        }
        return isRelativeFlag;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.TimeEvent#getWhenExpr()
     */
    public String getWhenExpr () {
        if (when == null) return null;

        if (when instanceof InstanceValue) {
            return ((InstanceValue) when).getName();
        } else {
            // in all other cases, use the common getValueString() function
            return when.stringValue();
        }
    }
}
