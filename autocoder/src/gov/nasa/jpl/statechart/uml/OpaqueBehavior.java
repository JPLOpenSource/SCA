package gov.nasa.jpl.statechart.uml;

import java.util.Map;

public interface OpaqueBehavior extends Behavior {
    public boolean isReadOnly ();

    /**
     * Returns a String Array of the specification body, one String per
     * entry in the model.
     * 
     * @return {@link String}[]
     */
    public String[] body ();

    /**
     * Returns a String Array of the specification language, one String per
     * entry in the model.  The language array _should_ correspond positionally
     * to the body array; however, the tool may or may not enforce this.
     * 
     * @return {@link String}[]
     */
    public String[] language ();

    /**
     * NON-UML STANDARD!!
     *
     * This convenience function returns a map of the behavior specification
     * body, and its corresponding language, or <code>null</code> if none
     * defined in the model.
     *
     * @return {@link Map} of body String to its corresponding language String
     */
    public Map<String,String> getSpecificationMap ();

}
