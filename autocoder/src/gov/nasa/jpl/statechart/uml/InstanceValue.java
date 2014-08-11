/**
 * Created Jul 3, 2013.
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
 * An instance value is a value specification that identifies an instance.  Its
 * value thus specifies the value modeled by an instance specification.  For
 * simplicity, we will represent that instance via its XMI ID, which can then
 * be used to reference the actual instance in the model tree.
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
public interface InstanceValue extends ValueSpecification {

    /**
     * Returns the {@link NamedElement} instance that is the specified value,
     * looking up by the XMI ID of the Instance Value reference.
     *
     * The return type, as a departure from UML standard, is typed {@link NamedElement}
     * to allow for any UML element to be referenced as instance.
     *
     * @return {@link NamedElement} instance that is the specified value
     */
    public NamedElement instance ();

    /** NON-UML Standard!!
     *
     * @return The XMI ID of the instance that specifies the value.
     */
    String instanceId ();

}
