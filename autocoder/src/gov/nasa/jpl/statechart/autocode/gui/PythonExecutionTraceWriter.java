/**
 * Created Oct 5, 2009.
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
package gov.nasa.jpl.statechart.autocode.gui;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.autocode.python.UMLToPythonMapper;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.model.diagram.DiagramData;
import gov.nasa.jpl.statechart.template.TraceGuiVelocityModel;
import gov.nasa.jpl.statechart.uml.StateMachine;

/**
 * Finds diagram info from XMI and writes Python GUI for execution trace.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class PythonExecutionTraceWriter extends UMLStateChartTemplateWriter<UMLToPythonMapper> {

    /** Filename for velocity macro */
    protected static final String GUI_MACROS = "gui-velocimacros.vm";
    /** Filename for main template */
    protected static final String QPGUI_CODE = "qpgui-code.vm";

    /**
     * @param modelGrp
     * @param mapper
     */
    public PythonExecutionTraceWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPythonMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        // create writer-specific files for each of the top-level state machines
        for (StateMachine stateMachine : tModelGrp.getStateMachines()) {
            writeStateMachine(stateMachine);
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return GUI_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return "Execution Trace GUI";
    }

    private void writeStateMachine (StateMachine stateMachine) {
        String guiFilename = Autocoder.guiDirPrefix() + tMapper.mapToFileName(stateMachine) + ".py";

        // Create the proxy objects that expose the type information
        DiagramData data = tModelGrp.getDiagramData(UMLModelGroup.element2Model(stateMachine));
        tContext.put("model", new TraceGuiVelocityModel(stateMachine, data));

        // Write the code to a file
        writeCode(guiFilename, QPGUI_CODE);
    }

}
