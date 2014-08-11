/**
 * Created Aug 08, 2013.
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.uml;

/**
 * An operation is a behavioral feature of a classifier that specifies the name,
 * type, parameters, and constraints for invoking an associated behavior.
 * <br/>
 * <p>
 * UML specifies Property as a BehavioralFeature.  For simplicity, we've skipped
 * the BehaviorFeature and made Property a subtype of Namespace (of which
 * BehavioralFeature is subtype).  However, this simplification implies we're
 * not modeling the Parameter aspect at all, an important future improvement.
 * <br/>
 * <p>
 * Copyright 2009-2013, by the California Institute of Technology. ALL RIGHTS
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
 *
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public interface Operation extends Namespace {

    /**
     * Returns the Type of this operation, which should have its own UML class.
     * For now, it's a String.
     * 
     * @return
     */
    public String type ();

}
