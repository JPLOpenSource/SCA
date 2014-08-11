/**
 * Created Jul 12, 2013.
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

import java.util.Collection;

/**
 * Interface for UMLInstanceSpecification.
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
public interface InstanceSpecification extends PackageableElement {

    /**
     * Returns the classifier or classifiers of the represented instance.
     * If multiple classifiers are specified, the instance is classified by all
     * of them.
     * 
     * @return {@link Collection} of {@link Classifier}s
     */
    public Collection<Classifier> classifier ();

    /**
     * Returns the collection of {@link Slot} for the value or values of a
     * structural feature of the instance.  An instance specification can have
     * one slot per structural feature of its classifiers, including inherited
     * features.
     * 
     * @return {@link Collection} of {@link Slot}s
     */
    public Collection<Slot> slot ();

    /**
     * Returns the specification of how to compute, derive, or construct the
     * instance.
     * 
     * @return {@link ValueSpecification} for this instance spec
     */
    public ValueSpecification specification ();

}
