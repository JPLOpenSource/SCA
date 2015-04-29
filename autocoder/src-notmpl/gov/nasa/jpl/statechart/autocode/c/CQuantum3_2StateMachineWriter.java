package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.autocode.IOldWriter;
import gov.nasa.jpl.statechart.autocode.IWriter;
import gov.nasa.jpl.statechart.autocode.StateChartCodeWriter;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.DiagramElement;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.SubmachineState;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.StateMachineXmiReader;

import java.util.Map;

/**
 * <p>
 * This class writes out a state machine in the form of C code based on the
 * Quantum Framework model developed by Miro Samek. The code is generated
 * from an internal representation of StateMachine, State, and Transition 
 * objects. It also writes an <i>execution trace file</i> which contains
 * Python code used to create an animated GUI display of the various
 * elements of the state machine.
 * </p>
 * 
 * <p>
 * Copyright 2005, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * 
 * <p>
 * This software is subject to U.S. export control laws and regulations and 
 * has been classified as 4D993.  By accepting this software, the user agrees 
 * to comply with all applicable U.S. export laws and regulations.  User has 
 * the responsibility to obtain export licenses, or other export authority as 
 * may be required before exporting such information to foreign countries or 
 * providing access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: CSimQuantumStateMachineWriter.java,v 1.7 2005/10/11
 * 18:08:09 kclark Exp $
 * </p>
 * 
 * @see StateMachine
 * @see State
 * @see Transition
 * @see DiagramElement
 */
public class CQuantum3_2StateMachineWriter implements IOldWriter {
    // Filename extensions for submachines
    protected static final String submachineCodeExtension = "subc";
    protected static final String submachineHeaderExtension = "subh";

    // XMI Reader
    //
    // Break encapsulation for convenience. The writers need to access the
    // XmiReader to get signal names. This functionality should evenually
    // be moved into the StateMachine class.
    //
    // Note that setting this to be a static variable effectively renders
    // this class to be a singleton -- but it is not enforced!!
    public static StateMachineXmiReader reader = null;

    /**
     * Constructor.
     * 
     * @param reader
     *           StateMachineXmiReader The reader object containing the set of
     *           state machine representations.
     */
    public CQuantum3_2StateMachineWriter (StateMachineXmiReader reader) {
        this.reader = reader;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IWriter#write()
     */
    public void write () {
        try {
            writeAllStateMachines();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes out the C implementation files for the set of state machines
     * contained in this instance's reader.
     * 
     * @deprecated In favor of invocation via the {@link IWriter} interface.
     * @throws Exception
     */
    public void writeAllStateMachines () throws Exception {
        Map<String, StateMachine> machines = reader.getStateMachineMap();

        for (StateMachine stateMachine : machines.values()) {
            writeStateMachine(stateMachine);
        }
    }

    /**
     * Creates and writes out the Quantum Framework C code which implements the
     * state machine.
     * 
     * @throws Exception
     */
    void writeStateMachine (StateMachine stateMachine) throws Exception {
        String classname = stateMachine.name();
        String declarationsFilename = classname + ".h";
        String definitionsFilename = classname + ".c";

        StateChartCodeWriter headerWriter = new StateChartHeaderWriter(
                stateMachine);

        StateChartCodeWriter codeWriter = new StateChartCWriter(stateMachine);

        headerWriter.writeCode(declarationsFilename);
        codeWriter.writeCode(definitionsFilename);

        // Recursively generate the code for all the submachines contained
        // within this state machine.
        Map<String, SubmachineState> submachines = StateChartCodeWriter.filterByType(
                stateMachine.getAllStates(), SubmachineState.class);

        // Get the prefix which is needed for the namespacing of
        // orthogonal-enclosed submachines
        String prefix = codeWriter.getClassPrefix();

        for (Map.Entry<String, SubmachineState> entry : submachines.entrySet()) {
            writeSubmachine(entry.getKey(), stateMachine, entry.getValue(),
                    codeWriter);
        }
    }

    /*
    void writeSubmachine( String path, UMLStateMachine submachine, UMLState state, StateChartCodeWriter parentWriter )
    {
       // Get the prefix which is needed for the namespacing of
       // orthogonal-enclosed submachines
       String prefix = parentWriter.getClassPrefix();

       String headerFilename = 
          path.replace( ":", "_" ) + "." + submachineHeaderExtension;
       
       String codeFilename = 
          path.replace( ":", "_" ) + "." + submachineCodeExtension;

       String classname = submachine.getName();

       // If the submachine is enclosed within an orthogonal region, then 
       // the classname is going to be the region struct type
       String meType = classname;
       
       CompositeStateRegion enclosingRegion = 
          StateMachine.getEnclosingRegion( state );

       
       if ( enclosingRegion != null )
       {
          String fullPath = stateMachine.getQualifiedPath( enclosingRegion, prefix );
          meType = fullPath.replace( ":", "" ) + "Region";
       }

       StateChartCodeWriter headerWriter = 
          new SubmachineHeaderWriter( submachine, classname, path, meType );

       StateChartCodeWriter codeWriter = 
          new SubmachineCWriter( submachine, classname, path, meType );

       // Check to see if the submachine is contained within an orthogonal
       // region or the enclosing code writer 
       boolean inRegion = 
          parentWriter.isWithinRegion() ||
          ( StateMachine.getEnclosingRegion( state ) != null );

       codeWriter.setWithinRegion( inRegion );

       headerWriter.writeCode( headerFilename );
       codeWriter.writeCode( codeFilename );
             
       // Get a copy of he prefix for the writers
       String subPrefix = codeWriter.getClassPrefix();

       // Look for other submachine contained within this one
       Map<String, SubmachineState> submachines =
          StateChartCodeWriter.filterByType( submachine.getAllStates( path ), 
                                             SubmachineState.class );

       for ( Map.Entry<String, SubmachineState> entry : submachines.entrySet() )
       {
          writeSubmachine( entry.getKey(), stateMachine, entry.getValue(), codeWriter );
       }
    }
    */

    /**
     * Creates and writes out the Quantum Framework C code that implements
     * a submachine state.  The generated code files are intended to be 
     * pulled into the generated code from writeStateMachine
     *
     * @see writeStateMachine
     */
    void writeSubmachine (String path, StateMachine stateMachine,
            SubmachineState state, StateChartCodeWriter parentWriter) {
        // Get the prefix which is needed for the namespacing of
        // orthogonal-enclosed submachines
        String prefix = parentWriter.getClassPrefix();

        String headerFilename = path.replace(":", "_") + "."
                + submachineHeaderExtension;

        String codeFilename = path.replace(":", "_") + "."
                + submachineCodeExtension;

        StateMachine submachine = state.getStateMachine();
        String classname = stateMachine.name();

        // If the submachine is enclosed within an orthogonal region, then
        // the classname is going to be the region struct type
        String meType = classname;

        CompositeStateRegion enclosingRegion = StateMachine.getEnclosingRegion(state);

        if (enclosingRegion != null) {
            String fullPath = stateMachine.getQualifiedPath(enclosingRegion,
                    prefix);
            meType = fullPath.replace(":", "") + "Region";
        }

        StateChartCodeWriter headerWriter = new SubmachineHeaderWriter(
                submachine, classname, path, meType);

        StateChartCodeWriter codeWriter = new SubmachineCWriter(submachine,
                classname, path, meType);

        // Check to see if the submachine is contained within an orthogonal
        // region or the enclosing code writer
        boolean inRegion = parentWriter.isWithinRegion()
                || (StateMachine.getEnclosingRegion(state) != null);

        codeWriter.setWithinRegion(inRegion);

        headerWriter.writeCode(headerFilename);
        codeWriter.writeCode(codeFilename);

        // Get a copy of he prefix for the writers
        String subPrefix = codeWriter.getClassPrefix();

        // Look for other submachine contained within this one
        Map<String, SubmachineState> submachines = StateChartCodeWriter.filterByType(
                submachine.getAllStates(path), SubmachineState.class);

        for (Map.Entry<String, SubmachineState> entry : submachines.entrySet()) {
            writeSubmachine(entry.getKey(), stateMachine, entry.getValue(),
                    codeWriter);
        }
    }

}
