/**
 * Created Aug 5, 2009.
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
import gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.XMILabel;
import gov.nasa.jpl.statechart.uml.UMLElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class provides a "database" of MagicDraw UML namespace identifiers to
 * shield code from literal changes that result from namespace version changes.
 * Access the identifier mappings via the singleton instance {@link #inst()}.
 * Set the singleton object to the namespace version supported via
 * {@link #setNamespace(String)}.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class MagicDrawIdentifiers extends AbstractXMLIdentifiers<MagicDrawLabel> {

    public static final String EXPORTER = "MagicDraw UML";


    @VersionSupport(name=EXPORTER, version="12.5")
    public static class MD12_5 extends MagicDrawIdentifiers {
        protected MD12_5 () {
            super();
        }
        @Override
        public Node doc_getRootDiagramElement (Element doc, String id)
                throws XPathExpressionException {
            // Diagram MD element root: xmi:Extension/mdOwnedDiagrams/mdElement/mdElement 
            return (Node) UMLElement.xpath.evaluate(
                    XMIIdentifiers.inst().prefixed(XMILabel.TAG_EXTENSION) + "/"
                    + lit(MagicDrawLabel.TAG_OWNED_DIAGRAMS) + "/"
                    + lit(MagicDrawLabel.TAG_ELEMENT) + "[@"
                         + lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                         + "='Diagram' and @"
                         + lit(MagicDrawLabel.KEY_DIAGRAM_OWNER)
                         + "='"
                         + id
                         +"']/"
                     + lit(MagicDrawLabel.TAG_ELEMENT),
                    doc,
                    XPathConstants.NODE);
        }
        @Override
        public Node diagram_getOwnedViews (Node diagram)
                throws XPathExpressionException {
            return (Node) UMLElement.xpath.evaluate(lit(MagicDrawLabel.TAG_OWNED_VIEWS),
                    diagram, XPathConstants.NODE);
        }
    }

    @VersionSupport(name=EXPORTER, version="16.0")
    public static class MD16_0 extends MD12_5 {
        protected MD16_0 () {
            super();
        }
    }

    @VersionSupport(name=EXPORTER, version="16.5")
    public static class MD16_5 extends MD16_0 {
        protected MD16_5 () {
            super();
        }
    }

    @VersionSupport(name=EXPORTER, version="16.6")
    public static class MD16_6 extends MD16_0 {
        protected MD16_6 () {
            super();
        }
    }

    @VersionSupport(name=EXPORTER, version="16.8")
    public static class MD16_8 extends MD16_0 {
        protected MD16_8 () {
            super();
        }
    }

    @VersionSupport(name=EXPORTER, version="17.0")
    public static class MD17_0 extends MD16_8 {
        protected MD17_0 () {
            super();
        }
    }

    @VersionSupport(name=EXPORTER, version="17.0.2")
    public static class MD17_0_2 extends MD17_0 {
        protected MD17_0_2 () {
            super();
            // override mdOwnedDiagrams by ownedDiagram
            literalMap.put(MagicDrawLabel.TAG_OWNED_DIAGRAMS, "ownedDiagram");
        }
        @Override
        public Node doc_getRootDiagramElement (Element doc, String id)
                throws XPathExpressionException {
            Node rootDiagramElement = null;
            // 17.0.2 Diagram MD element root: 2 parts
            // part 1:  xmi:Extension/modelExtension/ownedDiagram[@xmi:type="uml:Diagram"], where we match for machine ID
            String diagramID = (String) UMLElement.xpath.evaluate(".//" + 
                    XMIIdentifiers.inst().prefixed(XMILabel.TAG_EXTENSION) + "/"
                    + lit(MagicDrawLabel.TAG_MODEL_EXTENSION) + "/"
                    + lit(MagicDrawLabel.TAG_OWNED_DIAGRAMS) + "[@"
                         + XMIIdentifiers.type()
                         + "='uml:" + lit(MagicDrawLabel.MD_DIAGRAM) + "' and @"
                         + lit(MagicDrawLabel.KEY_DIAGRAM_OWNER)
                         + "='"
                         + id
                         +"']/"
                     + "@" + XMIIdentifiers.id(),
                    doc,
                    XPathConstants.STRING);
            if (diagramID != null && diagramID.length() > 0) {
                // part 2:  xmi:Extension/filePart/mdOwnedViews/mdElement/elementID/../../../
                //- so peculiarly, we're returning the 'filePart' node...
                rootDiagramElement = (Node) UMLElement.xpath.evaluate(
                        XMIIdentifiers.inst().prefixed(XMILabel.TAG_EXTENSION) + "/"
                        + lit(MagicDrawLabel.TAG_FILE_PART) + "/"
                        + lit(MagicDrawLabel.TAG_OWNED_VIEWS) + "/"
                        + lit(MagicDrawLabel.TAG_ELEMENT) + "[@"
                             + lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                             + "='" + lit(MagicDrawLabel.MD_DIAGRAM_FRAME) + "']/"
                        + lit(MagicDrawLabel.TAG_ELEMENT_ID) + "[@"
                             + XMIIdentifiers.idref()
                             + "='"
                             + diagramID
                             +"']/"
                        + "../../..",
                        doc,
                        XPathConstants.NODE);
            }
            return rootDiagramElement;
        }
    }

    @VersionSupport(name=EXPORTER, version="17.0.5")
    public static class MD17_0_5 extends MD17_0_2 {
        protected MD17_0_5 () {
            super();
        }
    }

    /**
     * {@link AbstractXMLIdentifiers} subclass for reading MagicDraw 18.0 input.
     * <ul>
     * <li> <a href="http://www.omg.org/spec/XMI/20131001/">XMI 2.5 support</a>
     * <li> <a href="http://www.omg.org/spec/UML/20131001/">UML 2.5 support</a>
     * </ul>
     * MagicDraw information page on
     * <a href="http://www.nomagic.com/news/new-noteworthy/linked-pages/supported-uml-specification-changes-from-version-2-4-2-to-2-5.html#Metamodel_Changes">
     * supported changes from UML 2.4.1 to 2.5</a>.
     */
    @VersionSupport(name=EXPORTER, version="18.0")
    public static class MD18_0 extends MD17_0_5 {
        protected MD18_0 () {
            super();
        }
    }


    /** Singleton instance of this identifier class. */
    private static MagicDrawIdentifiers singleton = null;
    /** Sorted map of MagicDraw UML version to instance of this class. */
    private static SortedMap<String,MagicDrawIdentifiers> instByVersion = null;

    /**
     * Returns the current singleton instance, corresponding to the latest
     * supported MagicDraw namespace version or the namespace set by {@link #setNamespace(String)}.
     * @return the MagicDrawIdentifiers singleton instance.
     */
    public static MagicDrawIdentifiers inst () {
        if (singleton == null) {  // lazy init
            init();
        }
        return singleton;
    }

    /**
     * Sets the MagicDraw UML version and affects the singleton instance returned.
     * Must be a supported version, which is defined by an inner class
     * extending this class and annotated with {@link VersionSupport}.
     * @param ver  the MagicDraw UML version to set to
     */
    public static void setVersion (String ver) {
        if (instByVersion == null) {  // lazy init
            init();
        }
        if (instByVersion.containsKey(ver)) {
            singleton = instByVersion.get(ver);
        }
    }

    /**
     * Returns a list of supported MagicDraw UML versions.
     * @return list of version strings
     */
    public static List<String> getSupportedVersions () {
        if (instByVersion == null) {
            init();
        }
        return new ArrayList<String>(instByVersion.keySet());
    }

    /**
     * Initializes the singleton object of this class, setting the singleton
     * to the latest version of MagicDraw UML supported.
     */
    @SuppressWarnings("unchecked")
    synchronized private static void init () {
        if (singleton != null) return;  // don't repeat init

        instByVersion = new TreeMap<String,MagicDrawIdentifiers>();
        // instantiate annotated inner class(es) and get last version as singleton
        for (Class<? extends MagicDrawIdentifiers> c
                : (Class<? extends MagicDrawIdentifiers>[]) MagicDrawIdentifiers.class.getClasses()) {
            VersionSupport verAnnote = c.getAnnotation(VersionSupport.class);
            if (verAnnote != null) {  // found annotation!
                // instantiate class and store to version mapper
                try {
                    MagicDrawIdentifiers obj = c.newInstance();
                    instByVersion.put(verAnnote.version(), obj);
                } catch (InstantiationException e) {  // ignore
                    Util.error(e.getLocalizedMessage());
                } catch (IllegalAccessException e) {  // ignore
                    Util.error(e.getLocalizedMessage());
                }
            }
        }
        singleton = instByVersion.get(instByVersion.lastKey());

        if (Util.isInfoLevel()) Util.info("Supported MagicDraw UML version(s) "
                + Arrays.toString(instByVersion.keySet().toArray()));
    }


    /**
     * Default, protected constructor, populates the mapper of identifier label
     * to string literal.
     */
    protected MagicDrawIdentifiers () {
        super();
        for (MagicDrawLabel l : MagicDrawLabel.values()) {
            literalMap.put(l, l.defaultLiteral());
        }
        // store namespace string using the VersionSupport annotation, if any
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null) {
            literalMap.put(MagicDrawLabel.EXPORTER_VERSION, verAnnote.version());
        }
    }

    public String latestVersionSupported () {
        return instByVersion.lastKey();
    }

    /**
     * Returns the root XML element containing MagicDraw Diagram extension.
     * The goal is to get geometry info.
     *
     * @param doc  the {@link Document} {@link Element} node
     * @param id   XMI ID of the machine for which to find Diagram Element
     * @return     the XML {@link Node} of the appropriate Diagram
     * @throws XPathExpressionException
     */
    public abstract Node doc_getRootDiagramElement (Element doc, String id)
            throws XPathExpressionException;

    public abstract Node diagram_getOwnedViews (Node diagram)
            throws XPathExpressionException;

}
