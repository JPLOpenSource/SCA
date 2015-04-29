/**
 * Created Feb 21, 2010.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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

import gov.nasa.jpl.statechart.autocode.AbstractGenerator;
import gov.nasa.jpl.statechart.autocode.GeneratorKind;
import gov.nasa.jpl.statechart.autocode.IGenerator;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.model.ModelGroup;

/**
 * Generator to generate the Python Timeline GUI.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
@GeneratorKind(IGenerator.Kind.Timeline)
public class TimelineGuiGenerator extends AbstractGenerator {

    /**
     * Default constructor, initializes reader and writers.
     */
    public TimelineGuiGenerator () {
        // Create the new Autocoder MagicDraw reader
        super(new MagicDrawReader());

        // Populate with a single writer configuration
        addWriter(PythonTimelineGuiWriter.class, ModelGroup.class);
    }

}
