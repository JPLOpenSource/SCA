/**
 * Created Aug 26, 2009.
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
package gov.nasa.jpl.statechart.input.identifiers;

import gov.nasa.jpl.statechart.input.VersionSupport;

/**
 * Support for reading UML schema version 2.2.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name="http://schema.omg.org/spec/UML/2.2", version="2.2")
public class UML2_2 extends UML2_1_2 {

    public UML2_2 () {
        super();
    }

}
