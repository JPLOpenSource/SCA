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

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.uml.Model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Document;

/**
 * Abstract base class for model validators, implementing the various validate
 * methods that throw a default "UnsupportedOperationException" for convenience,
 * and providing common validation functions.  Validator methods are not
 * expected to take any parameters nor return any result.
 * <p>
 * At construction, the class finds and stores all the validator methods with
 * the {@link Validate} Annotation.  When validating, the validator methods are
 * invoked in the order specified via the annotation.  If no order is specified,
 * then validation occurs in the order returned by Java reflection for methods.
 * If some methods specify a validation order while others do not, then the
 * ordered ones are processed before the unordered ones.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class AbstractModelValidator implements IModelValidator {

    private List<Method> unorderedMethods = null;
    private SortedMap<Integer,List<Method>> orderedMethods = null;

    /** Flag for fatal model errors to delay throwing exception until the end. */
    private boolean fatalError = false;
    /** Flag for non-fatal validation issues. */
    private boolean validationIssue = false;

    /**
     * Default constructor, acquires list of validation methods in this class.
     */
    public AbstractModelValidator () {
        unorderedMethods = Util.newList();
        orderedMethods = Util.newSortedMap();

        // get class methods
        for (Method m : getClass().getMethods()) {
            Validate vAnnote = m.getAnnotation(Validate.class);
            if (vAnnote != null) {
                if (vAnnote.order() == Validate.UNORDERED) {
                    unorderedMethods.add(m);
                } else {
                    // check if list of same ordered methods has been created
                    List<Method> mList = orderedMethods.get(vAnnote.order());
                    if (mList == null) {  // create empty list first
                        mList = Util.newList();
                        orderedMethods.put(vAnnote.order(), mList);
                    }
                    mList.add(m);  // add method to list
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.validator.IModelValidator#validate(gov.nasa.jpl.statechart.uml.Model)
     */
    public Status validate (Model model) {
        throw new UnsupportedOperationException("Don't know how to validate a UML model!");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.validator.IModelValidator#validate(org.w3c.dom.Document)
     */
    public Status validate (Document doc) {
        throw new UnsupportedOperationException("Don't know how to validate a straight DOM Document!");
    }

    private Set<String> methodSkipList = Util.newSet();
    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.validator.IModelValidator#skipValidation(java.lang.String[])
     */
    public void skipValidation (String... methodNames) {
        for (String name : methodNames) {
            methodSkipList.add(name);
        }
    }

    protected boolean shouldSkipMethod (String methodName, Object... args) {
        boolean rv = false;

        if (methodSkipList.contains(methodName)) {
            if (Util.isInfoLevel()) {
                StringBuffer sb = new StringBuffer("Skipped validation ");
                sb.append(methodName).append("(").append(Arrays.toString(args)).append(")");
                Util.info(sb.toString());
            }
            rv = true;
        }

        return rv;
    }

    private List<Method> methodFullList = null;
    /**
     * Creates an iterator over a collection composed of ordered followed by
     * unordered methods.
     * @return an Iterator over java Methods.
     */
    protected Iterator<Method> methodIterator () {
        if (methodFullList == null) {
            List<Method> methods = Util.newList();
            for (List<Method> mList : orderedMethods.values()) {
                // we have lists in order
                methods.addAll(mList);
            }
            methods.addAll(unorderedMethods);
            methodFullList = Collections.unmodifiableList(methods);
        }
        return methodFullList.iterator();
    }

    /**
     * Invokes the supplied validator method and returns whether invocation
     * completed successfully.  Validator methods should have no return value,
     * but in case of one, result is printed to System.error.
     * <br/><p>
     * Validation is skipped if the method is on the Skip List.
     * <br/></p>
     * 
     * @param m  Validator method to invoke.
     * @param args  optional arguments to supply to method.
     * @return  <code>true</code> if validator method succeeded, <code>false</code> otherwise.
     */
    protected boolean invokeValidatorMethod (Method m, Object...args) {
        boolean rv = false;

        if (shouldSkipMethod(m.getName(), args)) {
            rv = true;
        } else {
            try {
                Object rObj = m.invoke(this, args);
                if (rObj != null && Util.isWarningLevel()) {
                    Util.warn("FYI, validator method " + m.getName()
                            + " returned a result: " + rObj.toString());
                }
                rv = true;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof ModelException) {
                    throw (ModelException) e.getCause();
                } else {
                    e.getCause().printStackTrace();
                }
            }
        }
        return rv;
    }

    /**
     * Sets the flag for fatal model error.
     */
    protected void setFatalError () {
        fatalError = true;
    }

    /**
     * Returns whether fatal error flag is set.
     * @return
     */
    protected boolean fatalError () {
        return fatalError;
    }

    /**
     * Sets the flag for validation, but non-fatal, issue.
     */
    protected void setValidationIssue () {
        validationIssue = true;
    }

    /**
     * Returns whether the validate issue flag is set.
     * @return
     */
    protected boolean validationIssue () {
        return validationIssue;
    }

}
