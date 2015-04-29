/**
 * Created Oct. 27, 2009.
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
package gov.nasa.jpl.statechart.autocode.promela;

import gov.nasa.jpl.statechart.autocode.AbstractGenerator;
import gov.nasa.jpl.statechart.autocode.GeneratorKind;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.model.ModelGroup;

/**
 * This generator writes Promela output using velocity templates. The workflow is:
 * <ol>
 * <li> Generate Promela Main code
 * <li> Generate Promela Statechart code
 * <li> Generate Optional Init code
 * <li> Generate Manual Stub code
 * </ol>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author reder
 *
 */
@GeneratorKind(IGenerator.Kind.Promela)
public class PromelaGenerator extends AbstractGenerator {	
    /**
     * Default constructor, initializes reader and writers.
     */
    public PromelaGenerator() {
        super(new MagicDrawReader());

        // Populate list of writer configurations
        addWriter(PromelaStateMachineWriter.class, ModelGroup.class);
    }
	
}
