/**
 * Created Aug 05, 2009.
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
package gov.nasa.jpl.statechart.input;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.IGenerator.Kind;
import gov.nasa.jpl.statechart.input.identifiers.ProfileIdentifiers;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.model.ModelGroup;
import gov.nasa.jpl.statechart.model.ModelScape;
import gov.nasa.jpl.statechart.model.UMLModelGroup;
import gov.nasa.jpl.statechart.uml.Model;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * This abstract class provides generic methods for parsing descriptions of UML
 * state machines from XMI files. A subclass must be created which implements
 * the abstract methods, and which is specialized to parse a particular XMI
 * statechart representation (such as the XMI representation produced by the
 * MagicDraw UML tool).
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * 
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>, adapted from old StateMachineXmiReader.
 * 
 * @see StateMachine
 */

public abstract class AbstractXmiReader implements IReader {
    // Keep a couple sets for rapid cross referencing
    protected final Set<StateMachine> submachines = Util.newSet();

    protected UMLModelGroup modelGroup = null;

    private Set<String> filesLoaded = null;
    private Map<String,ModelScape> files2Modelscape = null;
    private Map<String,Document> files2Document = null;


    /**
     * Default constructor.
     */
    public AbstractXmiReader () {
        modelGroup = new UMLModelGroup();
        filesLoaded = Util.newSet();
        files2Modelscape = Util.newMap();
        files2Document = Util.newMap();

        // make this reader known to ProfileIdentifiers
        ProfileIdentifiers.inst().addReader(this);
    }


/////////////////////
// Abstract Members
/////////////////////

    /**
     * Allows reader subclass to handle specific format, returning the I/O
     * input stream.
     * @param canonPath  the canonical path of file to process
     * @return  the resulting InputStream from the input file
     */
    protected abstract InputStream handleSpecificFormat (String canonPath)
    throws IOException;

    /**
     * Determines what version of reader we will be parsing.  This method
     * should be called as early as possible, as it sets the version-appropriate
     * helper reader class, which affects all of parsing behavior.
     * <p>
     * After calling this method, the reader should know which version of XML
     * it is reading, and not need to worry about re-checking for version support.
     * </p>
     * @param doc  the DOM Document object on which to determine version
     */
    protected abstract boolean determineReaderVersion (Document doc);

    /**
     * Extract model node from XMI DOM model for model profiling and creation.
     */
    protected abstract Node extractModelNodeFromDOM (Document document);

    /**
     * Collect diagrammatic data from XMI DOM model into model diagram data holder.
     * Stores result in the abstract class member <code>modelGroup</code>.
     */
    protected abstract void collectDiagramData (UMLModel model);


////////////////////////
// Implemented Members
////////////////////////

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#read(java.lang.String[])
     */
    public ModelGroup read (String... sources) {
        for (String filename : sources) {
            loadXmiFile(filename);
        }
        return modelGroup;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#readDiagrams()
     */
    public ModelGroup readDiagrams () {
        // don't waste time collecting diagram data if signals OR not tracing
        if (Autocoder.isExecutionTraceOn()
                && Autocoder.autocodingTarget() != Kind.Signals) {
            for (UMLModel model : modelGroup.models()) {
                collectDiagramData(model);
            }
        }
        return modelGroup;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#getModelGroup()
     */
    public ModelGroup getModelGroup () {
        return modelGroup;
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#clearCache()
     */
    public void clearCache () {
        filesLoaded.clear();
    }


    /**
     * Load an XMI file into memory using the standard XML DOM API.
     */
    public Model loadXmiFile (String filename) {
        // first check if parsed, and get the canonical path of the file
        String canonicalPath = getCanonicalPath(filename);
        if (isAlreadyLoaded(canonicalPath)) {
            return modelGroup.getModel(canonicalPath);
        }

        System.out.println("Loading XMI doc from " + canonicalPath + "...");
        UMLModel model = null;

        // Configure the document builder the be namespace aware.
        // This code was taken from the Sun tutorial at
        // http://java.sun.com/j2ee/1.4/docs/tutorial/doc/JAXPDOM8.html
        //
        // For validation we use the new Validator object that was
        // introduced in Java 1.5
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        // Since we are simply processing the XMI file, we want to ignore
        // the pieces that are not related to the content, e.g. comments
        factory.setCoalescing(true);
        factory.setExpandEntityReferences(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        try {
            // Get a DataBuilder instance. This may throw an exception
            // it it does not support the flags we set above
            DocumentBuilder db = factory.newDocumentBuilder();

            // Parse the XMI document into a DOM tree
            Document document = db.parse(handleSpecificFormat(canonicalPath));

            /*
            // Create a SchemaFactory that understands WXS schemas
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XMI 2.1 schema
            Source schemaFile = new StreamSource( new File( "schema/XMI_2_1.xsd" ));
            Schema schema     = schemaFactory.newSchema( schemaFile );

            // Create the validator and validate the document
            Validator validator = schema.newValidator();
            validator.validate( new DOMSource( document ));
             */            

            // Determines the specific reader version first
            determineReaderVersion(document);

            // Make document known to ProfileIdentifiers
            ProfileIdentifiers.inst().addModel(this, document);

            // Create a model profile the UML model in preparation for creation
            ModelScape scape = ModelScape.profileModel(canonicalPath, extractModelNodeFromDOM(document), modelGroup);
            // Store for later model creation
            files2Modelscape.put(canonicalPath, scape);
            files2Document.put(canonicalPath, document);
        } catch (ParserConfigurationException e) {
            // Cannot recover if the parser does not support us...
            throw new FatalModelException("Sorry! The parser does NOT support the input file you gave!", e);
        } catch (IllegalArgumentException e) {
            modelGroup.setFatalException(true);
            if (Util.isDebugLevel()) {
                e.printStackTrace();
            } else {
                Util.error(e.getLocalizedMessage());
            }
        } catch (IOException e) {
            modelGroup.setFatalException(true);
            if (Util.isDebugLevel()) {
                e.printStackTrace();
            } else {
                Util.error(e.getLocalizedMessage());
            }
        } catch (SAXException e) {
            modelGroup.setFatalException(true);
            if (Util.isDebugLevel()) {
                e.printStackTrace();
            } else {
                Util.error(e.getLocalizedMessage()
                        + " (This probably means you didn't supply a proper XMI document!)");
            }
        }

        return model;
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#createModels()
     */
    public void createModels () {
        // Use ModelScape, which contains the DOM element, to prune all models
        for (ModelScape scape : files2Modelscape.values()) {
            scape.pruneModelTree();
        }
        for (String filepath : files2Document.keySet()) {
            if (!isAlreadyLoaded(filepath)) {
                Document document = files2Document.get(filepath);
                // Construct our internal UML meta-model instance
                UMLModel model = new UMLModel(modelGroup, files2Modelscape.get(filepath));
                modelGroup.mapFilenameToModel(filepath, model);
                modelGroup.mapModelToDocument(model, document);
                filesLoaded.add(filepath);
            }
        }
    }

    /**
     * Parses and returns a string containing an array of integers.
     * 
     * @param intString  String Text integer values.
     * @return int[]  Array of integer values, or null if not able to parse.
     */
    protected int[] toIntArray (String intString) {
        String[] intStringArray = intString.split("[ ,;]");
        int[] intArray = new int[intStringArray.length];
        int j = 0;
        for (int i = 0; i < intStringArray.length; ++i) {
            try {
                if (intStringArray[i].length() > 0) {
                    intArray[j] = Integer.parseInt(intStringArray[i]);
                    ++j;
                }
            } catch (Exception e) {
                Util.error("Error parsing int string; " + e.getLocalizedMessage());
                return null;
            }
        }
        // transfer results to a new array, in case of skipped elements
        int[] result = new int[j];
        for (int i = 0; i < j; ++i) {
            result[i] = intArray[i];
        }
        return result;
    }

    private boolean isAlreadyLoaded (String filename) {
        boolean rv = false;
        String canonicalPath = getCanonicalPath(filename);
        if (filesLoaded.contains(canonicalPath)
                && modelGroup.existsModel(canonicalPath)) {
            // don't load again
            return rv = true;
        }
        return rv;
    }

    private String getCanonicalPath (String filename) {
        String canonicalPath = null;
        try {
            canonicalPath = new File(filename).getCanonicalPath();
        } catch (IOException e) {
            // complain! will also return null on canonical path
            Util.reportException(e, "AbstractXmiReader.getCanonicalPath(): ");
        }
        return canonicalPath;
    }

}
