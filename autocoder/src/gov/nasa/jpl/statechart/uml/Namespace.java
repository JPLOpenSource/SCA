/**
 * A namespace is an element in a model that contains a set of named elements
 * that can be identified by name.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.*;

public interface Namespace extends NamedElement {

    public Collection<NamedElement> getOwnedMember ();

    public Collection<Constraint> getOwnedRule ();

    public Set<String> getNamesOfMember (NamedElement element);

}
