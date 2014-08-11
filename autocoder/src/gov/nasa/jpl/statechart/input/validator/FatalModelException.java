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
 * Marks a fatal exception while processing a UML Model.  Currently, we do not
 * distinguish between exceptions that occur during the read phase versus the
 * processing phase.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class FatalModelException extends ModelException {
    private static final long serialVersionUID = -2338367611405307302L;

    /**
     * Default empty constructor, no message.
     */
    public FatalModelException () {
        super();
        isFatal = true;
    }

    /**
     * Fatal model exception with an error message.
     * @param message  an informative message about this model exception.
     */
    public FatalModelException (String message) {
        super(message);
        isFatal = true;
    }

    /**
     * Fatal model exception containing a {@link Throwable}.
     * @param cause  the Throwable cause of this model exception.
     */
    public FatalModelException (Throwable cause) {
        super(cause);
        isFatal = true;
    }

    /**
     * Model exception with an error message _and_ caused by another
     * {@link Throwable}. 
     * @param message  an informative message about this model exception.
     * @param cause  the Throwable cause of this model exception.
     */
    public FatalModelException (String message, Throwable cause) {
        super(message, cause);
        isFatal = true;
    }

}
