/**
 * Created Sep 9, 2009.
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
package gov.nasa.jpl.statechart.input.validator;

import gov.nasa.jpl.statechart.uml.Model;

import org.w3c.dom.Document;

/**
 * Interface of a validator to check a supplied model, which may be embodied as
 * a UML Model or in an XMI DOM Document.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public interface IModelValidator {

    /**
     * Model validation status, one of {@link #FATAL}, {@link #ERROR}, or {@link #OK}.
     */
    public enum Status {
        /** Fatal error from model validation. */
        FATAL,
        /** Model validation issues that are not fatal. */
        ERROR,
        /** Model validation OK. */
        OK
    }

    /**
     * Validates a UML model and returns whether there's validation error.
     * @param model  the UML model to validate.
     * @return  the model validation {@link Status}.
     */
    public Status validate (Model model);

    /**
     * Validates an XMI model embodied in the supplied DOM Document, and returns
     * whether there's validation error.
     * @param doc  the DOM document of the model to validate.
     * @return  the model validation {@link Status}.
     */
    public Status validate (Document doc);

    /**
     * Adds methods matching given name list to the Skip List to skip its
     * execution during the validation.
     * 
     * @param methodNames  Name list of methods to skip validation.
     */
    public void skipValidation (String... methodNames);

}
