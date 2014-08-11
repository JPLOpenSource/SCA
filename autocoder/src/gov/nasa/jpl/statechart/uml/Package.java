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

import java.util.Collection;

/**
 * A package is used to group elements, and provides a namespace for the
 * grouped elements.  A package is a namespace for its members, and may contain
 * other packages. Only packageable elements can be owned members of a package.
 * By virtue of being a namespace, a package can import either individual
 * members of other packages, or all the members of other packages.
 * <p>
 * In addition a package can be merged with other packages.
 * </p><p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public interface Package extends Namespace, PackageableElement {

    public Collection<Package> nestedPackages ();
    public Collection<PackageableElement> packagedElements ();
//    public Collection<Type> ownedTypes ();
//    public Collection<Package> packageMerges ();
    public Package nestingPackage ();

    /**
     * NON-UML standard:  Name of the package as derived from the C++ Namespace
     * stereotype tag value, primarily used for C++ namespace.
     * @return
     */
    public String getPackageName ();

}
