/**
 * Created Jul 24, 2009.
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
package gov.nasa.jpl.statechart.input.magicdraw;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Node;



/**
 * This abstract class defines a list of String literals and a set of utility
 * methods to be used by the MagicDraw UML reader for parsing an XMI doc.
 * Literals, as well as parsing algorithms, that can potentially change with
 * new versions of MagicDraw should be located here.
 * <p>
 * Subclasses should be created for each new version of MagicDraw to be
 * supported, and the changed literals appropriately overridden in the subclass
 * constructor.  Overriden properties cannot be declared "final."
 * </p>
 * <p>
 * N.B.: While this is not cleanly OO, it strikes the balance of extracting literals
 * without generating several dozen getter methods.  Revisit and refactor in
 * the future if deemed necessary.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 * @see MagicDrawUmlReader
 */
@SuppressWarnings("unchecked")
public abstract class MDUmlReaderHelper {

    /** MagicDraw UML is the exporter name. */
    public static final String EXPORTER = "MagicDraw UML";


    /** Singleton instance of reader helper */
    private static MDUmlReaderHelper singleton = null;
    /** Map of supported version String to a corresponding helper class,
     * determined by reading class annotations.
     */
    private static SortedMap<String,MDUmlReaderHelper> instBySupportedVersion = null;

    public static MDUmlReaderHelper inst () {
        if (singleton == null) {  // lazy init
            init();
        }
        return singleton;
    }
    /**
     * Returns the sorted list of supported versions after lazily initializing
     * the map of version to helper instances.
     * 
     * @return map of supported version strings to reader helper instances.
     */
    public static List<String> getSupportedVersions () {
        if (instBySupportedVersion == null) {
            init();
        }
        return new ArrayList(instBySupportedVersion.keySet());
    }
    public static String supportedVersionsAsString () {
        if (instBySupportedVersion == null) {
            init();
        }
        return new StringBuffer(EXPORTER).append(" model versions ")
                .append(Arrays.toString(instBySupportedVersion.keySet().toArray()))
                .toString();
    }

    public static void setVersion (String verStr) {
        if (instBySupportedVersion == null) {
            init();
        }
        if (instBySupportedVersion.containsKey(verStr)) {
            singleton = instBySupportedVersion.get(verStr);
        }
    }

    synchronized private static void init () {
        if (singleton != null) return;  // don't repeat init

        instBySupportedVersion = new TreeMap<String,MDUmlReaderHelper>();
        for (Class<? extends MDUmlReaderHelper> c
                : Util.findSubclassesUnder(MDUmlReaderHelper.class, "MD.+UmlReaderHelper")) {
            // get class and read its annotations for version info
            VersionSupport exportAnnote = c.getAnnotation(VersionSupport.class);
            if (exportAnnote != null) {
                // instantiate class and add to version map
                MDUmlReaderHelper obj;
                try {
                    obj = c.newInstance();
                    instBySupportedVersion.put(exportAnnote.version(), obj);
                } catch (InstantiationException e) {  // ignore
                } catch (IllegalAccessException e) {  // ignore
                }
            }
        }
        System.out.println("Supported " + supportedVersionsAsString());

        // set latest version to singleton reference
        singleton = instBySupportedVersion.get(instBySupportedVersion.lastKey());
    }


    /**
     * Null instance of the reader helper, useful for the initial phase before
     * having determined the appropriate version of reader helper to instantiate.
     */
    public static MDUmlReaderHelper NULL_READER = new MDUmlReaderHelper() {
        // Null implementation
        @Override
        /*package*/ void findStateInsAndOuts(State thisState, Node startNode,
                List<Transition> transList) throws Exception {
        }
        @Override
        /*package*/ State refinePseudoState (String kind, String name, String id) {
            return null;
        }
        @Override
        /*package*/ void checkSafeEventOnTransition (Node transNode, String eventName)
                throws Exception {
        }
        @Override
        /*package*/ String getTimeOutExpression (Node timeoutEventNode)
                throws Exception {
            return null;
        }
    };


    /** Exporter version number as String, format "M.m". */
    public String EXPORTER_VERSION = "UNKNOWN";

    public final String TAG_MD_OWNED_DIAGRAMS = "mdOwnedDiagrams";
    public final String TAG_MD_ELEMENT = "mdElement";
    public final String TAG_MD_ELEMENT_ID = "elementID";
    public final String TAG_MD_PROPERTY_ID = "propertyID";
    public final String TAG_MD_DIAGRAM_WINDOW_BOUDNS = "diagramWindowBounds";
    public final String TAG_MD_GEOMETRY = "geometry";
    public final String TAG_MD_TEXT = "text";
    public final String MD_PSEUDOSTATE = "PseudoState";
    public final String MD_DECISION = "Decision";
    public final String MD_DIAGRAM = "Diagram";
    public final String MD_SPLIT = "Split";
    public final String MD_TEXTBOX = "TextBox";
    public final String MD_TRANSITION = "Transition";
    public final String MD_TRANSITION_TO_SELF = "TransitionToSelf";
    public final String KEY_ELEMENT_CLASS = "elementClass";
    public final String KEY_DIAGRAM_OWNER = "ownerOfDiagram";


    private XMIIdentifiers xmiId = null;
    private UMLIdentifiers umlId = null;
    private Set<String> tags = null;

    protected MagicDrawUmlReader reader = null;

    /**
     * Default constructor, sets the EXPORTER_VERSION string.
     */
    public MDUmlReaderHelper () {
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null) {
            EXPORTER_VERSION = verAnnote.version();
        }
        // set default (latest) identifiers support class for XMI and UML
        xmiId = XMIIdentifiers.inst();
        umlId = UMLIdentifiers.inst();
    }

    public String latestVersionSupported () {
        return instBySupportedVersion.lastKey();
    }

    public void setReader (MagicDrawUmlReader reader) {
        this.reader = reader;
    }

    public void setIdentifiers (XMIIdentifiers xmi, UMLIdentifiers uml) {
        xmiId = xmi;
        umlId = uml;
    }

    public XMIIdentifiers xmi () {
        return xmiId;
    }

    public UMLIdentifiers uml () {
        return umlId;
    }

    public boolean hasTag (String t) {
        if (tags == null) {  // lazy init
            tags = Util.newSet();
            Collections.addAll(tags, findTagDataFields());
        }
        return tags.contains(t);
    }

    // finds and returns array of values of TAG_* data fields
    private String[] findTagDataFields () {
        List<String> fieldVals = Util.newList();
        for (Field f : getClass().getFields()) {
            if (f.getName().startsWith("TAG_")) {  // grab TAG field
                try {
                    fieldVals.add((String)f.get(this));
                } catch (IllegalArgumentException e) {  // ignore
                } catch (IllegalAccessException e) {  // ignore
                }
            }
        }
        return fieldVals.toArray(new String[0]);
    }

    private Map<String,Integer> nameNumberMap = new Hashtable<String,Integer>();
    /**
     * Makes and returns a uniquely numbered name, given a name stub.
     * <p>
     * This method is currently useful for state names:<ul>
     * <li> Initial
     * <li> Final
     * <li> Junction
     * <li> DeepHistory
     * <li> EntryPoint
     * <li> ExitPoint
     * <ul>
     * 
     * @param name  name stub to make unique
     * @return "&lt;name&gt;&lt;N&gt;" where N is a unique number for the name stub
     */
    public String makeUniqueName (String name) {
        StringBuffer nameBuf = new StringBuffer(name);
        if (!nameNumberMap.containsKey(name)) {
            // put initial value into map
            nameNumberMap.put(name, 0);
        }
        int num = nameNumberMap.get(name) + 1;  // increment number by 1
        nameBuf.append(num);
        nameNumberMap.put(name, num);
        return nameBuf.toString();
    }

    public int getNameNumbering (String name) {
        return nameNumberMap.get(name);
    }

    /**
     * Finds the incoming and outgoing transitions to this state, looking for
     * XMI tags specific to MagicDraw UML version 12.5.
     * 
     * @param thisState
     *            The state for which we're finding incoming and outgoing transitions.
     * @param startNode
     *            The node to search at and below.
     * @param transList
     *            The list of transitions to correlate with the state's incomings and outgoings.
     * @throws Exception
     * @see {@link MagicDrawUmlReader#findStateInsAndOuts()}
     */
    /*package*/ abstract void findStateInsAndOuts (State thisState, Node startNode,
            List<Transition> transList) throws Exception;

    /**
     * Determines the particular kind of PseudoState given the node of that
     * PseudoState, and return a new state object.
     * 
     * @param thisNode  the XMI node representing the PseudoState
     * @param name  the name of the PseudoState as defined by its "name" attribute  
     * @param id  the unique XMI identifier for the state
     * @return the new State object, one of Initial, Junction, DeepHistory, EntryPoint, or ExitPoint.
     */
    /*package*/ abstract State refinePseudoState (String kind, String name, String id);

    /**
     * Checks whether a safe event was used on an external transition.
     * @param transNode  the XMI Node representing the transition to check
     * @param eventName  the name of the event to check
     * @throws Exception
     */
    /*package*/ abstract void checkSafeEventOnTransition (Node transNode, String eventName)
            throws Exception;

    /*package*/ abstract String getTimeOutExpression (Node timeoutEventNode)
            throws Exception;

}
