/**
 * A TimeEvent specifies a point in time. At the specified time, the event
 * occurs.
 */
package gov.nasa.jpl.statechart.uml;


public interface TimeEvent extends Event {

    public Signal getSignal ();

    public ValueSpecification getWhen ();

    public boolean isRelative ();

    /**
     * Non-UML Standard! Convenience function to extract the timer string
     * expression of interest, from either an OpaqueExpression or LiteralString.
     * 
     * @return  String representing time expression.
     */
    public String getWhenExpr ();

}
