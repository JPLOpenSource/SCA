/**
 * Created Aug 24, 2009.
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
import gov.nasa.jpl.statechart.input.IReader;
import gov.nasa.jpl.statechart.model.UMLModelGroup;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for an IGenerator with common class members and methods for
 * ease of subclassing target generators.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class AbstractGenerator implements IGenerator {

    /**
     * Defines an IWriter configuration, consisting of its Class and a list of
     * the parameter classes.  Internally used within the hierarchy of this class.
     */
    protected static class WriterConfig {
        public Class<? extends IWriter> writerClass = null;
        public Class<?>[] paramList = null;
        public WriterConfig (Class<? extends IWriter> clazz, Class<?>[] params) {
            writerClass = clazz;
            paramList = params;
        }
    }

    private IReader tReader = null;
    private List<WriterConfig> tFlowOfWriters = null;

    /**
     * Default constructor, initializes writer list; subclasses should initialize
     * reader and the specific writer configurations.
     */
    public AbstractGenerator (IReader reader) {
        setReader(reader);
        // array to hold writers
        tFlowOfWriters = new ArrayList<WriterConfig>();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#generate(java.lang.String[])
     */
    public void generate (String[] sources) {
        // get readers to read all sources
        // new reader returns model group from sources
        UMLModelGroup modelGrp = (UMLModelGroup) reader().read(sources);
        if (modelGrp.hasFatalException()) {
            System.err.println("**Sorry! one or more exceptions in the input UML Model(s) prevented code generation, aborting!");
            return;
        } else {
            reader().createModels();
            if (modelGrp.hasFatalException()) {
                System.err.println("**Sorry! fatal exception while creating UML Model(s) prevented code generation, aborting!");
                return;
            }
        }
        // create old UML reader object which was used for the SIM RTC project:
//        MagicDrawUmlReader oldReader = new MagicDrawUmlReader();
//        boolean oldReaderInitd = false;

        // before we write, validate models and inject name for unnamed elements
        modelGrp.setUMLValidationSkipList(getValidationSkipList());
        modelGrp.validateModels();

        if (Autocoder.isCheckOnly()) {  // discontinue if we're just checking
            return;
        }

        // read and collect diagram information for all models
        reader().readDiagrams();
        modelGrp.injectAnonymousNames();

        // iterate through the writers in sequence
        for (WriterConfig config : writers()) {
//            if (IOldWriter.class.isAssignableFrom(config.writerClass)) {
//                if (!oldReaderInitd) {  // only init old reader when necessary
//                    try {
//                        oldReader.parseXmlFiles(sources);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                constructWriter(config, oldReader).write();
//            } else {
                // normal order-of-business with new writers
                constructWriter(config, modelGrp).write();
//            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#generate(gov.nasa.jpl.statechart.input.IReader, java.lang.String[])
     */
    public void generate (IReader reader, String[] sources) {
        if (reader != null) {  // override only if non-null
            setReader(reader);
        }
        generate(sources);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.autocode.IGenerator#reader()
     */
    public IReader reader () {
        return tReader;
    }

    /**
     * Internal common method to set the reader.
     * @param reader
     */
    private void setReader (IReader reader) {
        tReader = reader;
    }

    /**
     * Returns a list of {@link WriterConfig}s for the writers.
     * @return
     */
    protected List<WriterConfig> writers () {
        return tFlowOfWriters;
    }

    /**
     * Adds a {@link WriterConfig} of the given {@link IWriter} subclass with
     * the given, optional listing of parameters.
     * @param writerClass  Class of the IWriter to instantiate
     * @param params  optional list of Classes of the expected constructor
     *  parameters of the IWriter class 
     */
    protected void addWriter (Class<? extends IWriter> writerClass, Class<?>...params) {
        writers().add(new WriterConfig(writerClass, params));
    }

    /**
     * Constructs an IWriter instance based on the supplied WriterConfig data,
     * using the additional supplied argument as the writer construction argument.
     * This method uses a generic second argument, and uses the class of the
     * second argument to locate the constructor of the writer.
     * 
     * @param config  the WriterConfig used to instantiate the writer.
     * @param arg  the constructor argument to instantiate the writer.
     * @return  the new IWriter object.
     */
    protected <T> IWriter constructWriter (WriterConfig config, T arg) {
        // construct argument array, should be just one parameter
        Object[] args = new Object[config.paramList.length];
        for (int i=0; i < config.paramList.length; ++i) {
            if (config.paramList[i].isAssignableFrom(arg.getClass())) {
                // param class matches
                args[i] = arg;
            }
        }

        IWriter w = null;
        try {
            w = config.writerClass.getConstructor(config.paramList).newInstance(args);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return w;
    }

    /**
     * Extension point for specific back-ends to instruct the UMLValidator
     * to skip specific validation methods.
     *
     * return String[] array of method names to skip validation.
     */
    protected String[] getValidationSkipList () {
        // nothing to do, let specific back-end override
        return null;
    }

}
