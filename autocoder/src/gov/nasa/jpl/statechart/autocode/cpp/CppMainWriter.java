/**
 * Created Jan 14, 2010.
 * <p>
 * Copyright 2010, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.autocode.cpp;

import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.QuantumStateMachineWriter;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.GlobalVelocityModel;

/**
 * Generates a main file for Cpp StateMachine to be executed standalone.
 * <p>
 * Copyright &copy; 2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class CppMainWriter extends UMLStateChartTemplateWriter<UMLToCppMapper> {

    protected static final String QP_MAIN       = "qp-main-qvanilla.vm";
    protected static final String QP_MAIN_LINUX = "qp-main-linux.vm";
    protected static final String MAIN_FILENAME = "main";

    protected static final String QP_MAKEFILE   = "qp-makefile.vm";
    protected static final String MAKE_FILENAME = "Makefile";

    /**
     * Main constructor, calls super with a Model Group and mapper.
     * 
     * @param modelGrp  Group of UML models
     */
    public CppMainWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToCppMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return IGenerator.Kind.Cpp.label();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return QuantumStateMachineWriter.QP_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String mainFilename = tMapper.sanitize(MAIN_FILENAME) + ".cpp";
        String makeFilename = tMapper.sanitize(MAKE_FILENAME);

        // Create the proxy objects that expose the type information
        tContext.put("model", new GlobalVelocityModel(tModelGrp));
        tContext.put("emptyString", new String[0]);

        // Write the main code to a file
        writeCode(mainFilename, QP_MAIN);
        // Write the Makefile
        writeCode(makeFilename, QP_MAKEFILE);
    }

}
