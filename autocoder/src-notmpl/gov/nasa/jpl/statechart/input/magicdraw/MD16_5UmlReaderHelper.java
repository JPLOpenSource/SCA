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
package gov.nasa.jpl.statechart.input.magicdraw;

import gov.nasa.jpl.statechart.input.VersionSupport;


/**
 * This class defines the literals and version-specific functions in support of
 * reading MagicDraw UML 16.5.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name=MDUmlReaderHelper.EXPORTER, version="16.5")
public class MD16_5UmlReaderHelper extends MD16_0UmlReaderHelper {

    /**
     * Default constructor overrides values defined in base reader class.
     */
    public MD16_5UmlReaderHelper () {
        super();
    }

}
