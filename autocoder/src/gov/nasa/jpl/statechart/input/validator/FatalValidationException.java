/**
 * Created Sep 28, 2010.
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
package gov.nasa.jpl.statechart.input.validator;

/**
 * A specific exception to attribute fatal model exception to validation error.
 * This is used to prevent dumping an unnecessary stack trace at the end.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class FatalValidationException extends FatalModelException {
    private static final long serialVersionUID = -4051835548818539841L;

    /**
     * Default empty constructor, no message.
     */
    public FatalValidationException () {
        super();
    }

    /**
     * Fatal validation exception with an error message.
     * @param message  an informative message about this validation exception.
     */
    public FatalValidationException (String message) {
        super(message);
    }

    /**
     * Fatal validation exception containing a {@link Throwable}.
     * @param cause  the Throwable cause of this model exception.
     */
    public FatalValidationException (Throwable cause) {
        super(cause);
    }

    /**
     * Validation exception with an error message _and_ caused by another
     * {@link Throwable}. 
     * @param message  an informative message about this model exception.
     * @param cause  the Throwable cause of this model exception.
     */
    public FatalValidationException (String message, Throwable cause) {
        super(message, cause);
    }

}
