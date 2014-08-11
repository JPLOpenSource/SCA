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
package gov.nasa.jpl.statechart.input;

import gov.nasa.jpl.statechart.model.ModelGroup;

/**
 * Common interface for all readers in this Autocoder.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public interface IReader {

    /**
     * Sequentially reads the content of one or more source XMI files,
     * parses each, and returns a group of Model objects corresponding to this
     * Autocoder's intermediate representation of the UML model(s) therein.
     * 
     * @param sources  file names of the sources to read.
     * @return  group of UML model representing the processed files.
     */
    public ModelGroup read (String... sources);

    /**
     * Parse the XMI DOM models and create our internal representation of the
     * UML Model from using the profiled UML:Model XMI Node.
     */
    public void createModels ();

    /**
     * This is a separate step that collects the diagram data from the
     * underlying XMI model and populates the Autocoder's intermediate rep.
     * The rationale for this separate step is to avoid wasted processing time
     * if the parsed model fails validation.
     * Unlike the core UML model, diagram info generally doesn't induce
     * validation issues.
     * 
     * @return  the same group of UML model as from the {@link #read(String...)}
     *      step, for uniformity.
     */
    public ModelGroup readDiagrams ();

    /**
     * Returns the group of models processed so far.
     * @return  group of UML Model processed from files so far.
     */
    public ModelGroup getModelGroup ();

    /**
     * Clears cached of read sources to force loading.
     */
    public void clearCache ();


    /**
     * Returns the Namespace URI prefixes that the Reader designate for
     * identifying custom UML Profiles.
     * 
     * @return Array of URI prefix Strings, with first item being the
     * main reader Namespace
     */
    public String[] getProfileNsUriPrefixes ();

}
