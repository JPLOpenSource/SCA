/**
 * A trigger specifies an event that may cause the execution of an associated
 * behavior. An event is often ultimately caused by the execution of an action,
 * but need not be.
 */
package gov.nasa.jpl.statechart.uml;

public interface Trigger extends NamedElement {

    public Event getEvent ();
}
