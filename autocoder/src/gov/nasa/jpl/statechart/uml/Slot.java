/**
 * Created Sep 21, 2013.
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
 * Interface for UMLSlot.
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
public interface Slot extends Element {

    /**
     * Returns the structural feature that specifies the values that may be
     * held by the slot, overly constrained as a {@link Property} since this
     * implementation does not explicitly model the UML StructuralFeature type.
     *
     * @return  {@link Property} that is the defining feature
     */
    public Property definingFeature ();

    /**
     * Returns the {@link InstanceSpecification} that owns this Slot.
     *
     * @return {@link InstanceSpecification} that owns this {@link Slot}
     */
    public InstanceSpecification owningInstance ();

    /**
     * Returns the value(s) corresponding to the defining feature for the
     * owning instance specification.
     * 
     * @return Collection of {@link ValueSpecification}s for the value(s)
     */
    public Collection<ValueSpecification> value ();

}
