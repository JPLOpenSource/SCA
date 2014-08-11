package gov.nasa.jpl.statechart.uml;

import java.util.Map;

public interface Classifier extends Namespace {

    /** NON-UML Standard!!
     * 
     * Convenience function to obtain a cached Map of Attributes by XMI ID.
     *
     * @return Map of Property element by XMI ID string
     */
    public Map<String,Property> getAttributesMap ();

    /** NON-UML Standard!!
     *
     * Returns the {@link Property} instance for the named Classifier attribute.
     * 
     * @param name  Name of attribute to retrieve
     * @return  {@link Property} instance for the named attributes
     */
    public Property getAttribute (String name);

}
