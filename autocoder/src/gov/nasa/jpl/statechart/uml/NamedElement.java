/**
 * A named element is an element in a model that may have a name.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.List;

public interface NamedElement extends Element {

    public String getName ();

    /**
     * If there is no name or one of the containing namespaces has no name,
     * then there is no qualified name
     */
    public String getQualifiedName ();

    /**
     * NON-UML standard:  Looks ancestor-ward for contiguous packages with
     * namespace designations, starting from the first package, and returns the
     * outside-in ordered list of package names.
     * 
     * @return List of package names, from the outermost package inward.
     */
    public String[] getPackageNames ();

    /**
     * Returns the parent named element of this named element, or <code>null</code> if top node.
     * @return parent NamedElement instance 
     */
    public NamedElement getParent ();

    public List<Namespace> allNamespaces ();

    public VisibilityKind getVisibility ();

    public String separator ();

    /**
     * NON-UML standard:  Returns a string representing the "type" of the
     * named element, in lieu of defining full-fledged Type and TypedElement
     * UML classes.
     * 
     * @return String identifier of the Type of this element, as defined by
     * a "type" attribute, if applicable; <code>null</code> otherwise.
     */
    public String type ();

}
