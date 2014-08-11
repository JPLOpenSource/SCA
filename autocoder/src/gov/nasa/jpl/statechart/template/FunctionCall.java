/**
 * Created Oct 6, 2009.
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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.uml.Constraint;


/**
 * Simple representation of a function call as two parts, the function name,
 * and the uninterpreted string of call arguments.  At this point, we presume
 * that the arguments can be passed in directly to the target language, though
 * we should revisit this issue later.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class FunctionCall implements Comparable<FunctionCall> {

    /**
     * Checks if the passed string conforms to method call syntax, e.g.,
     * "someFunction(e)".
     * @param str  the text to check.
     * @return <code>true</code> if the supplied string represents a method call;
     * <code>false</code> otherwise.
     * 
     * TODO Handle the case:  '"action(x)"', which should NOT be a function call
     */
    public static boolean isFunctionCall (String str) {
        if (str == null) {
            return false;
        }
        int openIndex = str.indexOf("(");
        int closeIndex = str.lastIndexOf(")");
        if (openIndex >= 1 && closeIndex >= 2 && openIndex < closeIndex) {
            return true;
        } else {
            return false;
        }
    }


    private String funcName = null;
    private String args = null;
    private String[] argList = null;
    /** Flag indicates whether this function is being used as a Guard. */
    private boolean isGuard = false;
    private Constraint guard = null;

    /**
     * Main constructor.
     */
    public FunctionCall (String exprStr) {
        this(exprStr, null);
    }

    /**
     * Constructor that supports specifying if this function is used as guard.
     * @param exprStr  function string
     * @param guard  boolean indicating whether this is a guard
     */
    public FunctionCall (String exprStr, Constraint guard) {
        setGuard(guard);

        int lpi = exprStr.indexOf("(");
        int rpi = exprStr.lastIndexOf(")");
        if (lpi > -1 & rpi > lpi) {  // a function call
            if (rpi > lpi + 1) {  // with params
                // extract args between parentheses
                args = exprStr.substring(lpi+1, rpi).trim();
            }
            // extract function name excluding left-parenthesis
            funcName = exprStr.substring(0, lpi).trim();
        } else {  // Function call created on a non-call, e.g., in Do-Activity
            funcName = exprStr;
            Util.info("FunctionCall created on non-call action '"
                    + exprStr + "'!");
        }

        if (args != null && args.length() > 0) {  // parse arguments into a list
            argList = Util.splitPreservingQuotes(args, ",");
        } else {
            argList = new String[0];
        }
    }

    /**
     * Returns hashCode based ONLY on the function name.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((funcName == null) ? 0 : funcName.hashCode());
        return result;
    }

    /**
     * Returns equivalence based ONLY on the function name.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FunctionCall)) {
            return false;
        }
        FunctionCall other = (FunctionCall) obj;
        if (funcName == null) {
            if (other.funcName != null) {
                return false;
            }
        } else if (!funcName.equals(other.funcName)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder(name()).append("(");
        if (argStr() != null) {
            sb.append(argStr());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Compares first by whether FunctionCall is a guard, then by function name.
     * That is, a guard function sorts "above" a non-guard (action) function.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo (FunctionCall o) {
        // Let a := this, b:= other
        if (this.isGuard() && !o.isGuard()) {
            return -1;  // a < b
        }
        if (!this.isGuard() && o.isGuard()) {
            return 1;  // a > b
        }
        // otherwise, guard flag is the same, check name
        if (this.name() == null && o.name() == null) {
            return 0;   // a == b
        }
        if (this.name() != null && o.name() == null) {
            return 1;   // a > b
        }
        if (this.name() == null && o.name() != null) {
            return -1;  // a < b
        }
        return this.name().compareTo(o.name());
    }

    public String name () {
        return funcName;
    }

    public boolean hasArgs () {
        return args != null;
    }

    public String argStr () {
        return args;
    }

    /**
     * Returns the comma-separated arguments from the args string.
     * 
     * @return array of args string split by comma.
     */
    public String[] argList () {
        return argList;
    }

    public boolean isGuard () {
        return isGuard;
    }

    public void setGuard (Constraint guardConstraint) {
        isGuard = (guardConstraint != null);
        guard = guardConstraint;
    }

    public Constraint getGuard () {
        return guard;
    }

}
