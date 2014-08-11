/**
 * Created Oct 26, 2009.
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
package gov.nasa.jpl.statechart.autocode.cpp;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.autocode.AbstractGenerator;
import gov.nasa.jpl.statechart.autocode.GeneratorKind;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.autocode.gui.PythonExecutionTraceWriter;
import gov.nasa.jpl.statechart.autocode.gui.PythonGuiSignalWriter;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.model.ModelGroup;

/**
 * This generator writes C++ output using velocity templates. The workflow is:
 * <ol>
 * <li> Generate C++ header
 * <li> Generate C++ code
 * <li> Generate the python trace if designated
 * </ol>
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@GeneratorKind(IGenerator.Kind.Cpp)
public class CppGenerator extends AbstractGenerator {

    /**
     * Default constructor, initializes reader and writers.
     */
    public CppGenerator () {
        // Create the new Autocoder MagicDraw reader
        super(new MagicDrawReader());

        // Populate list of writer configurations
        addWriter(CppQuantumStateMachineWriter.class, ModelGroup.class);

        // Check if we should generate an application main file
        if (Autocoder.ifDefineMain() && !Autocoder.hasOutputDir()) {
            addWriter(CppMainWriter.class, ModelGroup.class);
        }

        // Check to avoid duplicate signal generation
        if (!Autocoder.signalGenRequested()) {
            // invoke our localized C++ signal writer
            addWriter(CppStateChartSignalWriter.class, ModelGroup.class);
        }

        // Check a system property to see if we should generate the execution trace files:
        if (Autocoder.isExecutionTraceOn()) {
            addWriter(PythonExecutionTraceWriter.class, ModelGroup.class);
            if (!Autocoder.signalGenRequested()) {
                addWriter(PythonGuiSignalWriter.class, ModelGroup.class);
            }
        }
    }

}
