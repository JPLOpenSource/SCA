/**
 * Created Dec 11, 2009.
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
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.GlobalVelocityModel;

/**
 * Generates a main file for C StateMachine to be executed standalone.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class CMainWriter extends UMLStateChartTemplateWriter<UMLToCMapper> {

    protected static final String QPC_MAIN = "qpc-main.vm";
    protected static final String MAIN_FILENAME = "main";

    /**
     * Main constructor, calls super with a Model Group.
     * 
     * @param modelGrp  Group of UML models
     */
    public CMainWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Cm.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return CQuantumStateMachineWriter.QPC_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String mainFilename = tMapper.sanitize(MAIN_FILENAME) + ".c";

        // Create the proxy objects that expose the type information
        tContext.put("model", new GlobalVelocityModel(tModelGrp));

        // Write the code to a file
        writeCode(mainFilename, QPC_MAIN);
    }

}
