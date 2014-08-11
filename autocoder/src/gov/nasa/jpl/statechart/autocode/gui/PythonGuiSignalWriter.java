/**
 * Created Sep 29, 2009.
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
import gov.nasa.jpl.statechart.template.GlobalVelocityModel;

/**
 * Finds all specified signals and time events in the global UML model and
 * generates a corresponding Python GUI of buttons to send signal.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class PythonGuiSignalWriter extends UMLStateChartTemplateWriter<UMLToPythonMapper> {

    protected static final String QPGUI_CODE = "qpgui-statechartsignals.vm";
    protected static final String SIGNAL_FILENAME = "StatechartSignals";

    /**
     * @param modelGrp
     * @param mapper
     */
    public PythonGuiSignalWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPythonMapper());
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String signalFilename = Autocoder.guiDirPrefix()
                + tMapper.sanitize(SIGNAL_FILENAME) + ".py";

        // Create the proxy objects that expose the type information
        tContext.put("model", new GlobalVelocityModel(tModelGrp));

        // Write the code to a file
        writeCode(signalFilename, QPGUI_CODE);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getVelociMacroFile()
     */
    @Override
    protected String getVelociMacroFile () {
        return PythonExecutionTraceWriter.GUI_MACROS;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter#getTargetLabel()
     */
    @Override
    protected String getTargetLabel () {
        return "GUI Signals";
    }

}
