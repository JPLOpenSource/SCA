/**
 * Created Aug 11, 2009.
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
package gov.nasa.jpl.statechart.autocode;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Test harness for snippets of MagicDraw input files.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class UMLSnippetTestHarness {

    public static final String PATH_OUTPUT = "test/files/output/";
    public static final String PATH_SNIPPET = "test/files/uml-snippets/";
    public static final String SUFFIX_SNIPPET = ".xmi";
    public static final String NAME_HEADER = "0header" + SUFFIX_SNIPPET;
    public static final String NAME_FOOTER = "1footer" + SUFFIX_SNIPPET;
    public static final String SNIPPET_SIMPLE_SM = "statemachine-simple" + SUFFIX_SNIPPET;
    public static final String SNIPPET_HIERARCHICAL_SM = "statemachine-hierarchical" + SUFFIX_SNIPPET;
    public static final String SNIPPET_COMPLEX_SM = "statemachine-complex" + SUFFIX_SNIPPET;
    public static final String SNIPPET_TOPLEVEL_SIGNAL = "statemachine-toplevelSignal" + SUFFIX_SNIPPET;
    public static final String SNIPPET_SMAPMODES_SM = "statemachine-smapmodes" + SUFFIX_SNIPPET;
    public static final String SNIPPET_PENULTIMATE_SM = "statemachine-penultimate" + SUFFIX_SNIPPET;

    protected static MagicDrawReader reader = null;

    private static File sharedInputFile = null;

    protected static void staticInit () throws Exception {
        Autocoder.inst().setNoTrace(true);
        reader = new MagicDrawReader();
        // create one temp file for generating test input files
        sharedInputFile = File.createTempFile(UMLSnippetTestHarness.class.getName(), ".xml");
        System.out.println("Shared input file created at '" + sharedInputFile.getCanonicalPath() + "'");
    }

    protected static void staticDispose () throws Exception {
        // delete temp input file
        System.out.println("Deleting shared input file on exit '" + sharedInputFile.getName() + "'");
        sharedInputFile.deleteOnExit();
    }

    protected static File createInputFromSnippet (String name) {
        File input = sharedInputFile;
        try {
            // prepare shared input file for write of file parts
            PrintWriter pw = new PrintWriter(input);
            // open header, snippet, and footer files for read
            for (String part : new String[]{ NAME_HEADER, name, NAME_FOOTER }) {
                String headerPath = PATH_SNIPPET + part;
                BufferedReader br = new BufferedReader(new FileReader(headerPath));
                // output each file to the shared file
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    pw.println(line);
                }
                br.close();
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            input = null;
        }
        return input;
    }

    protected static Model loadUmlSnippet (String name) {
        Model rv = null;
        File input = createInputFromSnippet(name);
        if (input != null) {
            reader.clearCache();
            rv = reader.loadXmiFile(input.getAbsolutePath());
        }
        return rv;
    }

    protected static StateMachine getFirstStateMachine (Model m) {
        List<StateMachine> machines = Util.filter(m.getOwnedMember(), StateMachine.class);
        return machines.get(0);
    }

}
