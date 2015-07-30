/**
 * Created Jul 28, 2015.
 * <p>
 * Copyright 2009--2015, by the California Institute of Technology. ALL RIGHTS
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
 * Support for reading XMI schema version 2.5, which did not have significant
 * changes affecting the StateChart Autocoder subset of XMI and UML metamodels.
 * <p>
 * Copyright &copy; 2009--2015 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen.Cheng@jpl.nasa.gov
 *
 */
@VersionSupport(name="http://www.omg.org/spec/XMI/20131001", version="2.5")
public class XMI2_5 extends XMI2_4_1 {

    public XMI2_5 () {
        super();
    }

}
