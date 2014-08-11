package gov.nasa.jpl.statechart.autocode;

/**
 * Class that encapsulated a variable for template use. These objects are
 * returned by all the TemplateModel methods.
 */
public interface TemplateVariable {
    /**
     * Returns the string for the type declaration of this
     * variable.
     */
    public String getType ();

    /**
     * Returns the type of the state chart implemenation.
     */
    public String getImpl ();

    /**
     * Return the function name of the constructor function that initialized
     * the variable
     */
    public String getCtor ();

    /**
     * Return the function name of the initial state transition.  The state
     * machine enters this state immediately after construction 
     */
    public String getInit ();

    /**
     * Return the name of the variable as in appears in the UML diagram
     */
    public String getName ();

    /**
     * Return the declaration of this variable.
     */
    public String getDecl ();

    /**
     * Return the name of this variable as an constant type.  This is
     * usually just a capitalized verion of the type or declaration,
     */
    public String getCnst ();
}
