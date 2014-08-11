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


/**
 * A UML Model exception, caused by errors in the model which may be detected
 * during instantiation of the internal model elements, or during validation.
 * Subclasses refining this exception class can decide whether to set the
 * fatal flag, which can be used by the catch block to decide whether to
 * terminate the Autocoder.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class ModelException extends RuntimeException {
    private static final long serialVersionUID = 1690120942427881786L;

    protected boolean isFatal = false;

    /**
     * Default empty constructor, no message.
     */
    public ModelException () {
        super();
    }

    /**
     * Model exception with an error message.
     * @param message  an informative message about this model exception.
     */
    public ModelException (String message) {
        super(message);
    }

    /**
     * Model exception containing a {@link Throwable}.
     * @param cause  the Throwable cause of this model exception.
     */
    public ModelException (Throwable cause) {
        super(cause);
    }

    /**
     * Model exception with an error message _and_ caused by another
     * {@link Throwable}. 
     * @param message  an informative message about this model exception.
     * @param cause  the Throwable cause of this model exception.
     */
    public ModelException (String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns whether this exception is considered to be <i>fatal</i>, meaning
     * that it warrants terminating the Autocoder.
     * @return
     */
    public boolean isFatal () {
        return isFatal;
    }

}
