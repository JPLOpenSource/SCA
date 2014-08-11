/**
 * Created Apr 6, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
 * A packageable element indicates a named element that may be owned directly
 * by a package.  This interface represents a UML PackageableElement.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public interface PackageableElement extends NamedElement {

    /**
     * Returns the (non-optional) visibility of this packageable element.
     * Redefines {@link NamedElement}::visibility.  If none defined, defaults to
     * {@link VisibilityKind#PRIVATE}.
     * 
     * @return  {@link VisibilityKind} for this packageable element, one of
     *          private, package, protected, or public.
     */
    public VisibilityKind visibility ();

}
