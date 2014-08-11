/**
 * Element is an abstract metaclass with no superclass. It is used as the common
 * superclass for all metaclasses in the infrastructure library. Element has a
 * derived composition association to itself to support the general capability
 * for elements to own other elements.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface Element {

    public Set<Element> allOwnedElements ();

    public boolean mustBeOwned ();

    /**
     * This is an addition to get an optional XMI ID (not UUID) of the element.
     * @return String literal of the XMI ID
     */
    public String id ();

    /**
     * Returns the parent element of this element, or <code>null</code> if top node.
     * @return
     */
    public Element getParent ();

    /** NON-UML Standard!!
     * 
     * Returns a set of {@link Stereotype}s applied to this element.
     *
     * @return Collection of {@link Stereotype}s 
     */
    public Collection<Stereotype> getAppliedStereotypes ();

    /** NON-UML Standard!!
     *
     * Returns a map of tag-value pairs for the applied Stereotype on this Element.
     *
     * @param stereoName  Name of stereotype applied
     * @return  Map of tag overrides
     */
    public Map<String,Object> getStereotypeTagValueMap (String stereoName);

}
