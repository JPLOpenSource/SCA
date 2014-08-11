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

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.UMLStateChartTemplateWriter;
import gov.nasa.jpl.statechart.autocode.python.UMLToPythonMapper;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.FlattenedVelocityModel;
import gov.nasa.jpl.statechart.uml.Namespace;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [Purpose]
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class PythonTimelineGuiWriter extends UMLStateChartTemplateWriter<UMLToPythonMapper> {

    protected static final String GUI_CODE = "gui-timeline.vm";
    protected static final String TIMELINE_FILENAME = "timeline";

    private static final int CHARS_PER_CELL = 10;
    private static final int MAX_CELLS = 15;

    /**
     * Main constructor.
     * 
     * @param modelGrp
     * @param mapper
     */
    public PythonTimelineGuiWriter (ModelGroup modelGrp) {
        super(modelGrp, new UMLToPythonMapper());
    }

    /**
     * Makes a first pass over entire set of StateMachines to establish a map
     * of StateMachine to leaf states, while figuring out object labels for each
     * object.  Those two maps are passed to the Velocity template to autocode
     * the asset data structure.
     * 
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        String timelineFilename = Autocoder.guiDirPrefix()
                + tMapper.sanitize(TIMELINE_FILENAME) + ".py";

        if (fileAsPath(timelineFilename).exists()) {  // do not override
            System.out.println(getTargetLabel() + " target " + timelineFilename + " exists, will not override!");
            return;
        }

        // Create the proxy objects that expose the type information
        FlattenedVelocityModel queryModel = new FlattenedVelocityModel(tModelGrp, null);
        tContext.put("model", queryModel);

        // Scan through entire set of StateMachine once, figuring out labels
        Map<StateMachine,List<State>> smStatesMap = new LinkedHashMap<StateMachine,List<State>>();
        Map<Namespace,String> labelMap = new HashMap<Namespace,String>();
        Map<Namespace,Integer> spanMap = new HashMap<Namespace,Integer>();
        int maxColumns = 0;
        for (StateMachine sm : queryModel.sort(queryModel.getStateMachines())) {
            int spanColumns = 0;
            // establish a label for SM
            labelMap.put(sm, makeLabel(sm));
            // create a list
            List<State> statesList = new ArrayList<State>();
            smStatesMap.put(sm, statesList);
            // iterate through leaf states of SM, hopefully in expected order...
            for (State state : queryModel.getFlattenedStates(sm)) {
                statesList.add(state);
                // name label
                String label = makeLabel(state);
                labelMap.put(state, label);
                // columns spanned by label
                int span = determineSpan(label);
                spanMap.put(state, span);
                spanColumns += span;
            }
            // keep track of maximum spans so far
            if (spanColumns > maxColumns) {
                maxColumns = spanColumns;
            }
        }

        // second pass to make the labels line up nicely on the far right
        if (maxColumns > MAX_CELLS) {
            Util.error("ERROR! Columns spanned exceeds max of " + MAX_CELLS
                    + "! Manual adjustment required.");
            maxColumns = MAX_CELLS;
        }
        for (List<State> states : smStatesMap.values()) {
            int spanColumns = 0;
            for (State state : states) {
                spanColumns += spanMap.get(state);
            }
            if (spanColumns < maxColumns) {  // add spans to align on right
                int filler = maxColumns - spanColumns;
                int lastStateIdx = states.size() - 1;
                State lastState = states.get(lastStateIdx);
                spanMap.put(lastState, filler+spanMap.get(lastState));
            }
        }

        // Write the timeline code to file
        tContext.put("smStatesMap", smStatesMap);
        tContext.put("labelMap", labelMap);
        tContext.put("spanMap", spanMap);
        writeCode(timelineFilename, GUI_CODE);
        tContext.remove("smStatesMap");
        tContext.remove("labelMap");
        tContext.remove("spanMap");
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
        return "Timeline GUI";
    }

    private String makeLabel (Namespace ns) {
        StringBuilder labelSB = new StringBuilder();
        String name = ns.getName();
        String[] parts = name.split("_");

        // shorten a few known words, count name length in the process
        int nameLength = parts.length - 1;  // count separators first
        for (int i=0; i < parts.length; ++i) {
            if (parts[i].startsWith("Command")) {
                parts[i] = "Cmd";
            } else if (parts[i].startsWith("Initialize")) {
                parts[i] = "Init";
            } else if (parts[i].startsWith("Manager")) {
                parts[i] = "Mgr";
            } else if (parts[i].startsWith("Navigation")) {
                parts[i] = "Nav";
            } else if (parts[i].equals("Operations")) {
                parts[i] = "Ops";
            } else if (parts[i].startsWith("Separation")) {
                parts[i] = "Sep";
            } else if (parts[i].startsWith("Vehic")) {
                parts[i] = "Veh";
            }
            nameLength += parts[i].length();
        }

        // find out if any part is all uppercase, which can't be cut
        boolean hasAcronym = false;
        for (String p : parts) {
            if (p.equals(p.toUpperCase())) {  // found an acronym!
                hasAcronym = true;
                break;
            }
        }

        if (!hasAcronym && parts.length > 2 && nameLength > 24) {  // use acronym
            for (String p : parts) {
                labelSB.append(p.substring(0, 1).toUpperCase());
            }
        } else if (!hasAcronym && nameLength > 12) {  // use camel case
            for (String p : parts) {
                labelSB.append(p.substring(0, 1).toUpperCase());
                labelSB.append(p.substring(1));  // don't change case of remainder
            }
        } else {  // separate words by space
            labelSB.append(parts[0]);
            for (int i=1; i < parts.length; ++i) {
                labelSB.append(" ");
                labelSB.append(parts[i]);
            }
        }

        return labelSB.toString();
    }

    private int determineSpan (String label) {
        // simplistic span calculation, count 10-characters groups:
        float count = (float) label.length()/CHARS_PER_CELL;
        int span = (int) Math.ceil(count);

        // check the extras against number of spaces
        int extras = label.length() % CHARS_PER_CELL;
        int spaces = label.split(" ").length;
        if (extras <= spaces) {  // extras are due to spaces, use 1 fewer column
            --span;
        }

        return Math.max(1, span);  // force minimum span of 1
    }

}
