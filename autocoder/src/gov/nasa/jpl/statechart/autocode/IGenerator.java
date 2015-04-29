/**
 * Created Aug 6, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.autocode;

import gov.nasa.jpl.statechart.input.IReader;


/**
 * Common interface for generators in this Autocoder, which may be composed of
 * one or more writers in a generation "workflow", as reflected in the methods.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public interface IGenerator {

    /**
     * Defines the kinds of generator, essentially identifying the target language.
     * <p>
     * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
     * </p>
     * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
     *
     */
    public static enum Kind {
        C            { public String label () { return name(); }},
        Cpp          { public String label () { return "C++"; }},
        Java         { public String label () { return name(); }},
        Python       { public String label () { return name(); }},
        Promela      { public String label () { return name(); }},
        Cm           { public String label () { return "C (old)"; }},
        CNonTemplate { public String label () { return "C ad-hoc (old)"; }},
        ExPython     { public String label () { return "Ex-Python (old)"; }},
        Signals      { public String label () { return "StateChart Signals"; }},
        CSignals     { public String label () { return "C-old StateChart Signals"; }},
        CppSignals   { public String label () { return "C++ StateChart Signals"; }},
        Timeline     { public String label () { return "Python Timeline GUI"; }},
        SCL          { public String label () { return "Spacecraft Command Language"; }};

        public abstract String label ();
    }


    /**
     * Generates autocoded product from the supplied input sources through 3
     * phases:<ol>
     * <li> reading the sources
     * <li> processing the internal model
     * <li> writing output targets
     * </ol>
     * @param sources  array of source filenames
     */
    public void generate (String[] sources);

    /**
     * Designates the supplied reader object to perform autocoding. 
     * @param reader  IReader object to use
     * @param sources  array of source filenames
     */
    public void generate (IReader reader, String[] sources);

    /**
     * Returns the IReader used by this generator to autocode its targets.
     * @return  the IReader object used
     */
    public IReader reader ();

}
