/**
 * Created June 21, 2013.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.autocode.extension;

/**
 * <p>
 * This interface specifies what project customization classes must implement
 * in order for the Autocoder to fold in customization functionality.
 * Currently, this interface specifies: <ul>
 * 
 * <li>Optional adjustment of a completed unit of code for the UMLStateChartTemplateWriter. 
 * 
 * </ul>
 * <br/>
 * </p><p>
 * Copyright &copy; 2009--2013 Jet Propulsion Lab / California Institute of Technology
 * <br/></p>
 * @author Shang-Wen.Cheng@jpl.nasa.gov
 */
public interface IProjectCustomization {

    /**
     * This function is given the StringBuffer containing the code written so
     * far, allowing project-customization to make adjustments to the code
     * as deemed necessary.<br/>
     *
     * @param outputBuffer  StringBuffer of the output written for the Function
     * @return  Boolean <code>true</code> if any adjustment made; <code>false</code> otherwise.
     *     The Autocoder currently doesn't do anything different if <code>true</code> returned.
     */
    boolean adjustCodeUnitOutput (StringBuffer outputBuffer);

}
