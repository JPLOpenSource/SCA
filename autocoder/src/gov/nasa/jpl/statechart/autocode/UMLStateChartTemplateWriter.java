/**
 * Renamed class Aug 17, 2009, for pure, template-based writing.
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
import gov.nasa.jpl.statechart.Timer;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.cm.CGenerator;
import gov.nasa.jpl.statechart.autocode.extension.IProjectCustomization;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.template.TargetLanguageMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.JarResourceLoader;

/**
 * Abstract class that serves as a basis for any code generation class. This
 * class is intended to contain the overall code generation strategy that
 * subclasses can specialize for their language of choice. No language-specific
 * construct should exists here, except for convenience methods to help create
 * or rename variables to follow certain common C-type constructs.
 * 
 * @author adapted from original by Shang-Wen Cheng <scheng@jpl.nasa.gov>
 * 
 * TODO Come back to fix path issue, make TEMPLATE_DIR default VM resource path!
 */
public abstract class UMLStateChartTemplateWriter<T extends TargetLanguageMapper> implements IWriter {
    /** Default subdirectory under which template files are stored, usually "templates". */
    protected static final String TEMPLATE_DIR = "templates";
    /** Project-override velocimacro template file. */
    protected static final String PROJECT_VELOCIMACRO_TMPL = "project-velocimacros.vm";

    /** Internal ModelGroup of UML Models from which to construct template model and write output. */
    protected ModelGroup tModelGrp = null;
    /** Velocity context for template writing. */
    protected Context tContext = null;
    /** Mapper of template output strings for target programming language. */
    protected T tMapper = null;

    // Velocity engine instance used by this writer, NOT for subclass use
    private VelocityEngine ve = null;
    // Flag indicating whether writeCodePart wrote code in an invocation
    private boolean codeWritten = false;
    // StringWriter to store code written so far
    private StringWriter stringWriter = null;
    // Map of Timers used for timing code writing duration.
    private Map<String,Timer> timers = null;
    // Map of Writers for writing to output file.
    private Map<String,Writer> writers = null;


    /**
     * Main constructor, sets the model, context, and mapper.
     * 
     * @param modelGrp  the group of UML model(s) used for writing target output.
     * @param mapper    instance of TargetLanguageMapper subclass
     */
    public UMLStateChartTemplateWriter (ModelGroup modelGrp, T mapper) {
        tModelGrp = modelGrp;
        // Create a Velocity context and set a few variables, incl. the mapper
        tContext = new VelocityContext();
        tMapper = mapper;
        if (Util.isInfoLevel()) timers = Util.newMap();
        writers = Util.newMap();

        // Create the proxy objects for template variable evaluation
        int year = new GregorianCalendar().get(Calendar.YEAR);
        tContext.put("year", Integer.toString(year));
        tContext.put("autocoder", Autocoder.inst());
        tContext.put("model", null);  // to be set by subclass
        tContext.put("mapper", tMapper);
        // Methodize the Mapper to allow convenient access to static fields
        tContext.put("runtime", new FieldMethodizer(tMapper));
        tContext.put("tpath", getTemplatePath() + File.separator);

        // Create comma-separated lists of candidate template paths
        Collection<String> jarPaths = new ArrayList<String>();
        Collection<String> filePaths = new ArrayList<String>();
        if (Autocoder.configPath() != null) {
            // add custom config path first for template overriding
            if (Autocoder.configPath().exists() && Autocoder.configPath().isDirectory()) {
                filePaths.add(Autocoder.configPath().getAbsolutePath());
            }
        }
        for (String pathStr : Util.resourcePaths(CGenerator.class, TEMPLATE_DIR)) {
            if (pathStr.startsWith("jar")) {  // a jar path
                jarPaths.add(pathStr);
            } else {  // a regular file path
                filePaths.add(pathStr);
            }
        }

        // Override Velocity properties for log and resource loading paths
        Properties properties = new Properties();
        //- configure runtime log path
        properties.setProperty("runtime.log", "velocity.log");
        properties.setProperty("runtime.log.logsystem.class", Log4JLogChute.class.getCanonicalName());
//        properties.setProperty("runtime.log.logsystem.log4j.logger", /*how to get logger?*/);
        //- enable strict runtime references, forces variables to be defined
        properties.setProperty("runtime.references.strict", "true");
        //- set the counter initial value to 0
        properties.setProperty("directive.foreach.counter.initial.value", "0");
        //- eliminates error message about macro overriding
        properties.setProperty("velocimacro.permissions.allow.inline.to.replace.global", "true");
        //- makes context scoping local for sanity
        properties.setProperty("velocimacro.context.localscope", "true");
        //- set global library of Velocity macros
        if (getVelociMacroFile() == null) {
            Util.warn("WARNING! Each writer should indicate a velocimacro library file!");
        } else {
            properties.setProperty("velocimacro.library",
                    getTemplatePath() + File.separator + getVelociMacroFile()
                    + "," + getTemplatePath() + File.separator + PROJECT_VELOCIMACRO_TMPL);
        }
        //- set file and jar template loading paths to try in succession
        //-- file first, then jar (allows template overriding)
        properties.setProperty("resource.loader", "file, jar");
        properties.setProperty("file.resource.loader.description", "Velocity File Resource Loader");
        properties.setProperty("file.resource.loader.class", FileResourceLoader.class.getCanonicalName());
        properties.setProperty("file.resource.loader.path", Util.join(filePaths, ","));
        properties.setProperty("file.resource.loader.cache", Boolean.toString(!Autocoder.isDebugOn()));
        if (jarPaths.size() > 0) {
            properties.setProperty("jar.resource.loader.description", "Velocity Jar Resource Loader");
            properties.setProperty("jar.resource.loader.class", JarResourceLoader.class.getCanonicalName());
            properties.setProperty("jar.resource.loader.path", Util.join(jarPaths, ","));
            properties.setProperty("jar.resource.loader.cache", "true");
        }

        // Initialize the Velocity Template Engine, which invokes resource loader
        try {
            ve = new VelocityEngine(properties);  // instantiate new engine
            ve.init();

            if (Util.isDebugLevel()) {
                properties.store(System.out, "## Velocity Engine configuration options ##");
                Util.debug("########################################");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up any writers and timers in case they're not properly cleaned up
     * by invoking {@link #endWriteCode(String)}. 
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable {
        if (Util.isInfoLevel()) {
            // Call markTime on timers
            for (Map.Entry<String,Timer> pair : timers.entrySet()) {
                String filename = pair.getKey();
                Timer timer = pair.getValue();
                timer.markTime("File writing for " + filename + " terminated");
            }
            timers.clear();
        }
        // Close the writers.
        for (Writer w : writers.values()) {
            try {
                w.close();
            } catch (IOException e) {  // fail silently
            }
        }
        writers.clear();

        super.finalize();
    }


    /**
     * Returns the file name for the Velocity macro library template file.
     * @return  filename string
     */
    protected abstract String getVelociMacroFile ();

    /**
     * Returns a label describing the target written by this writer.
     * @return  label string
     */
    protected abstract String getTargetLabel ();

    /**
     * Returns the template path appropriate to the template writer subclass,
     * by default acquired via Class package location, but can easily be
     * overridden by the subclass.
     * 
     * @return  a writer specific template location, usually ends with {@link #TEMPLATE_DIR}.
     */
    protected String getTemplatePath () {
        return getClass().getPackage().getName().replace(".", File.separator)
                + File.separator + TEMPLATE_DIR;
    }


    protected File fileAsPath (String filename) {
        // compose an abstract file first using output dir (or CWD) and filename
        File f = null;
        File checkFile = new File(filename);
        if (checkFile.isAbsolute()) {  // don't prepend output dir
            f = checkFile;
        } else {  // prepend output dir, if applicable
            f = new File(Autocoder.hasOutputDir()
                    ? Autocoder.inst().getOutputDir(): ".", filename);
        }
        return f;
    }

    /**
     * Writes template code from <code>template</code>, output to file <code>filename</code>.
     * @param filename  name of file to write code output
     * @param template  Velocity template to use as starting point
     */
    protected void writeCode (String filename, String template) {
        Writer writer = beginWriteCode(filename);
        writeCodePart(writer, template);
        endWriteCode(filename);
    }

    /**
     * Marks the beginning of the process to write code to the given <code>filename</code>,
     * causing an output writer to be created and returned.
     * 
     * @param filename  Name of file to which to write code.
     * @return  {@link Writer} object for writing code.
     */
    protected Writer beginWriteCode (String filename) {
        if (writers.containsKey(filename)) {
            Util.error("ERROR! beginWriteCode() already called for file '"
                    + filename + "'!");
            return null;
        }

        if (Util.isInfoLevel()) timers.put(filename, new Timer());  // create timer
        System.out.println("Writing " + getTargetLabel() + " target " + filename + "...");

        // compose an abstract file first using output dir (or CWD) and filename
        File f = fileAsPath(filename);
        try {
            // Make sure that output directory(ies) exist
            f = Util.ensureDirectoriesExist(f);
            OutputStream os = new FileOutputStream(f);
            Writer writer = new OutputStreamWriter(os);
            // store mapping of name to writer
            writers.put(filename, writer);
            return writer;
        } catch (FileNotFoundException e) {
            throw new FatalModelException("Could not create file for writing! " + filename, e);
        } catch (IOException e) {
            throw new FatalModelException("Could not mkdirs for creating file! " + f.getPath(), e);
        }
    }

    /**
     * Writes template code from <code>template</code>, output to <code>writer</code>.
     * This method is intended to be used to generate partial target output.
     * @param writer    {@link OutputStreamWriter} for an output file.
     * @param template  Velocity template to merge to writer.
     */
    protected void writeCodePart (Writer writer, String template) {
        clearCodeWritten();

        if (stringWriter != null) {  // write to String instead
            writer = stringWriter;
        }

        try {
            String enc = (String) ve.getProperty("input.encoding");
            codeWritten = ve.mergeTemplate(getTemplatePath() + File.separator + template, enc, tContext, writer);
        } catch (ResourceNotFoundException e) {
            Util.error("Could not load template. See Velocity log.");
            e.printStackTrace();
        } catch (ParseErrorException e) {
            Util.error("Could not parse template. See Velocity log.");
            e.printStackTrace();
        } catch (TemplateInitException e) {
            Util.error("Could not initialize template. See Velocity log.");
            e.printStackTrace();
        } catch (MethodInvocationException e) {
            // check for fatal model exception
            if (e.getCause() instanceof FatalModelException) {
                throw (FatalModelException) e.getCause();
            } else {
                Util.error("Exception in template evaluation. See Velocity log.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Invokes Velocity macro of the supplied name, using <code>writeStep</code>
     * to make a logTag, supplying the given parameter list, and writing to the
     * given target writer.
     * @param w          FileWriter to the code file.
     * @param name       Name of the Velocimacro to invoke.
     * @param writeStep  Name of the current writing step.
     * @param params     Array of params the macro expects.
     */
    protected void invokeVmacro (Writer w, String name, String writeStep, String[] params) {
        if (stringWriter != null) {  // write to String instead
            w = stringWriter;
        }

        try {
			codeWritten = ve.invokeVelocimacro(name,
			        getTargetLabel() + "::" + writeStep + "()",
			        params, tContext, w);
		} catch (Exception e) {
			Util.error("Exception invoking Vmacro!");
			e.printStackTrace();
		}
    }

    /**
     * Attempts to supply the provided Velocity expression, and return the
     * default value if the evaluation failed, for example, due to a
     * non-existent Velocity macro definition.<br>
     * <p>
     * The intent of this function is to allow for project-specific override of
     * certain mapper function.  The pattern would be:
     * <pre>evalExpr("#mapToTypeName($elem,\"_impl\")", "makeStateMachine", tMapper.mapToTypeName($sm))</pre>
     * 
     * @param expr  The Velocity expression to attempt to evaluate
     * @param writeStep  The identifier for this autocoder writing step
     * @param defaultVal  The default String to return if expr cannot be evaluated for any reason  
     * @return String  Either the result of the Velocity evaluation, or the default value.
     * If evaluation causes an exception because the expression cannot be
     * parsed in the project context, then this function either fails
     * silently by returning the default value supplied, or it prints an error
     * message about evaluation failure. 
     */
    protected String evalExpr (String expr, String writeStep, String defaultVal) {
        boolean evalOk = false;
        StringWriter sw = new StringWriter();
        try {
			evalOk = ve.evaluate(tContext, sw,
					getTargetLabel() + "::" + writeStep + "()",
					expr);
        } catch (ResourceNotFoundException e) {
            Util.error("Could not load template. See Velocity log.");
            e.printStackTrace();
        } catch (ParseErrorException e) {
            if (defaultVal == null) {
                Util.error("Could not parse template. See Velocity log.");
                e.printStackTrace();
            }
        } catch (MethodInvocationException e) {
            // check for fatal model exception
            if (e.getCause() instanceof FatalModelException) {
                throw (FatalModelException) e.getCause();
            } else {
                Util.error("Exception in template evaluation. See Velocity log.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (evalOk) {
        	return sw.toString();
        } else {
        	return defaultVal;
        }
    }

    /**
     * Marks the end of the process of writing code to the given <code>filename</code>,
     * causing the associated writer to be closed and, if INFO logging level,
     * timer to be marked.
     * 
     * @param filename  Name of file to mark completion of writing code.
     */
    protected void endWriteCode (String filename) {
        Writer w = writers.remove(filename);
        if (w != null) {
            try {
                w.close();
            } catch (IOException e) {  // fail silently
            }
        }

        // print time marker
        if (Util.isInfoLevel()) {
            Timer t = timers.remove(filename);
            if (t != null) {
                t.markTime();
            }
        }
    }

    /**
     * Clears the flag indicating that code was written.
     */
    protected void clearCodeWritten () {
        codeWritten = false;
    }

    /**
     * Returns whether code was written in the last invocation of
     * {@link #writeCodePart(Writer, String)}.
     * @return <code>true</code> if code was written successfully, <code>false</code> otherwise.
     */
    protected boolean wasCodeWritten () {
        return codeWritten;
    }

    /**
     * Creates a StringWriter into which a Code Unit will be coded.
     * This affects the behavior of all Velocity merge operations until the
     * end of the Code Unit is marked.
     */
    protected void markCodeUnitBegin () {
        stringWriter = new StringWriter();
    }

    protected void markCodeUnitEnd (Writer w) {
        if (stringWriter == null) {
            Util.error("Code Unit ended without a corresponding stringWriter! Likely a logic error!!");
            return;
        }

        if (Autocoder.configUtil() != null
                && Autocoder.configUtil() instanceof IProjectCustomization) {

            // invoke project customization function on the Code Unit
            IProjectCustomization projCus = (IProjectCustomization)Autocoder.configUtil();
            projCus.adjustCodeUnitOutput(stringWriter.getBuffer());
        }

        try {
            w.write(stringWriter.toString());
            w.flush();
        } catch (IOException e) {
            Util.error("Could not write completed Code Unit from StringBuffer to File Writer.");
            e.printStackTrace();
        }

        // null-out to prevent further Velocity merge into this writer
        stringWriter = null;
    }
}
