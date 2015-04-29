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

import java.lang.reflect.Method;
import java.util.Iterator;

import org.w3c.dom.Document;


/**
 * This class supports the validation of the XMI content surround the UML,
 * such as model extensions for graphic layout data.  Only critical validation
 * errors causes autocoding to terminate; all other validation problems report
 * a warning message to the user.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class XMIValidator extends AbstractModelValidator {

    // README! change this and referenced code once XMI validation is added!
    public static boolean hasXmiValidation = false;

    // TODO: XMIValidator.doc unused!
    //private Document doc = null;

    /**
     * Default constructor.
     */
    public XMIValidator () {
        super();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.validator.AbstractModelValidator#validate(org.w3c.dom.Document)
     */
    @Override
    public Status validate (Document docIn) {
//        doc = docIn;
        Status validStat = Status.OK;

        for (Iterator<Method> iter = methodIterator(); iter.hasNext();) {
            try {
                if (!invokeValidatorMethod(iter.next())) {
                    validStat = Status.ERROR;
                }
            } catch (ModelException e) {
                if (e.isFatal()) {  // propagate up to let Autocoder terminate
                    validStat = Status.FATAL;
                } else {
                    validStat = Status.ERROR;
                }
            }
        }

        return validStat;
    }

//    @Validate
//    public void checkForDiagramGeometry () {
//        
//    }

}
