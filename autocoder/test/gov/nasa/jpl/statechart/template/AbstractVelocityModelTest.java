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
package gov.nasa.jpl.statechart.template;

import gov.nasa.jpl.statechart.autocode.UMLSnippetTestHarness;
import gov.nasa.jpl.statechart.autocode.cm.UMLToCMapper;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.StateMachine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import junit.framework.Assert;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * JUnit test class to test that AbstractVelocityModel functions produce the
 * expected results, and that velocity template strings against corresponding
 * functions work properly.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class AbstractVelocityModelTest<T extends AbstractVelocityModel>
extends UMLSnippetTestHarness {

    private static final String LOG_TAG = VelocityModelTest.class.getName();

    protected Model model = null;
    protected VelocityContext velocontext = null;
    protected int numRegionAndStates = 0;
    protected T velomodel = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass () throws Exception {
        staticInit();

        // Override Velocity properties for log and resource loading paths
        Properties properties = new Properties();
        //- configure runtime log path
        properties.setProperty("runtime.log",
                System.getProperty("user.dir") + File.separator
                + PATH_OUTPUT + "velocity.log");
        //- enable strict runtime references
        properties.setProperty("runtime.references.strict", "true");
        // Initialize the Velocity Template Engine
        try {
            Velocity.init(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass () throws Exception {
        staticDispose();
    }


    protected abstract T instantiateVelocityModel (UMLModelGroup modelGroup, StateMachine machine);

    /**
     * Loads the specified snippet and prepares the Velocity model and context
     * for testing.
     * <p>
     * N.B.: By design, this method has side effects on protected class
     * members in {@link AbstractVelocityModelTest}.
     * </p>
     * @param snippet  name of snippet to load
     */
    protected void prepareUmlSnippet (String snippet) {
        model = loadUmlSnippet(snippet);
        UMLModelGroup modelGroup = (UMLModelGroup) reader.getModelGroup();
        modelGroup.injectAnonymousNames();

        // Create a Velocity context and set a few variables, incl. the mapper
        velocontext = new VelocityContext();
        TargetLanguageMapper mapper = new UMLToCMapper();
        velomodel = instantiateVelocityModel(modelGroup, getFirstStateMachine(model));

        // Create the proxy objects that expose the type information
        int year = Calendar.getInstance().get(Calendar.YEAR);
        velocontext.put("year", Integer.toString(year));
        velocontext.put("model", velomodel);
        velocontext.put("mapper", mapper);
    }

    protected String evalTemplate (String templateStr) {
        StringWriter sw = new StringWriter();
        boolean evalOk = false;
        try {
            evalOk = Velocity.evaluate(velocontext, sw, LOG_TAG, templateStr);
        } catch (ParseErrorException e) {
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            e.printStackTrace();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(evalOk);
        return sw.toString();
    }

    protected Set<String> arrayStringToSet (String arrayString) {
        Set<String> stringSet = new HashSet<String>();
        int bracketBegin = arrayString.indexOf('[');
        int bracketEnd = arrayString.lastIndexOf(']');
        if (bracketBegin > -1 && bracketEnd > bracketBegin) {
            StringTokenizer tokens = new StringTokenizer(
                    arrayString.substring(bracketBegin+1, bracketEnd), ",");
            while (tokens.hasMoreTokens()) {
                String v = tokens.nextToken();
                stringSet.add(v.trim());
            }
        }
        return stringSet;
    }

    protected String velocityGetState (String name) {
        return
            "#foreach( $_s in $model.getStates($model.statemachine, false).entrySet() )\n"
            + "#if( $_s.getValue().getName().equals(\"" + name + "\") )\n"
            + "#set( $_sFound = $_s )\n"
            + "#end\n#end\n";
    }

}
