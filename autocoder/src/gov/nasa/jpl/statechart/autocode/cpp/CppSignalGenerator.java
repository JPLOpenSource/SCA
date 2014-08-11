/**
 * Created Apr 11, 2011, adapted from CSignalGenerator.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
import gov.nasa.jpl.statechart.autocode.gui.PythonGuiSignalWriter;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.model.ModelGroup;

/**
 * Generator to generate the C++ variant of StateChart Signals .h and GUI files.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@GeneratorKind(IGenerator.Kind.CppSignals)
public class CppSignalGenerator extends AbstractGenerator {

    /**
     * Default constructor, initializes reader and writers.
     */
    public CppSignalGenerator () {
        // Create the new Autocoder MagicDraw reader
        super(new MagicDrawReader());

        // Populate list of writer configurations
        addWriter(CppStateChartSignalWriter.class, ModelGroup.class);
        if (Autocoder.isExecutionTraceOn()) {
            addWriter(PythonGuiSignalWriter.class, ModelGroup.class);
        }
    }

}
