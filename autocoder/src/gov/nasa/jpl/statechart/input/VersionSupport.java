/**
 * Created Jul 30, 2009.
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
package gov.nasa.jpl.statechart.input;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation for reader helper classes to designate a specific version
 * that it supports, be the helper an exporter, or a particular XMI feature
 * such as XMI, UML, or Profile.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionSupport {
    /**
     * Defines the exporter or helper name supported by the annotated class.
     * @return the name string
     */
    String name ();

    /**
     * Defines the version string of the exporter/helper version supported.
     * It no version is specified, then prefix must be specified.
     * <p>
     * N.B. For an exporter, this string is used to define the EXPORTER_VERSION
     * data member in the target reader helper class.
     * </p><p>
     * N.B.2.  For a helper, this string is informational, and usually given
     * as output to inform the user.
     * </p>
     * @return the version string
     */
    String version () default "";

    /**
     * Defines the Namespace prefix, if it is necessary to specify.  For
     * XMI and UML labels, prefix is not necessary.  This is mainly useful for
     * Profiles.  If prefix is not specified, version must be specified.
     * @return the Namespace prefix string
     */
    String prefix () default "";
}
