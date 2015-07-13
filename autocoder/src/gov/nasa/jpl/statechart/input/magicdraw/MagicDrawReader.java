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

import gov.nasa.jpl.statechart.Timer;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.AbstractXmiReader;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.XMILabel;
import gov.nasa.jpl.statechart.input.validator.FatalModelException;
import gov.nasa.jpl.statechart.model.diagram.DiagramData;
import gov.nasa.jpl.statechart.model.diagram.DiagramElement;
import gov.nasa.jpl.statechart.model.diagram.MachineElement;
import gov.nasa.jpl.statechart.model.diagram.SeparatorElement;
import gov.nasa.jpl.statechart.model.diagram.TextElement;
import gov.nasa.jpl.statechart.model.diagram.TransitionElement;
import gov.nasa.jpl.statechart.model.diagram.TransitionToSelfElement;
import gov.nasa.jpl.statechart.model.diagram.VertexElement;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLModel;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides the common data and methods for all MagicDraw
 * UML reader versions to parse descriptions of UML state machines from XMI
 * files generated by the MagicDraw UML tool. The parser creates a model of the
 * state chart by building StateMachine objects containing States, Transitions,
 * etc. The state chart model can then be used to generate an implementation in
 * a language such as C/C++, Python, etc.
 * <p>
 * N.B.:  As this was abstracted from readers of two MagicDraw versions, 12.5
 * and 16.0, some shifting of functionalities between the super- and subclasses
 * may be in order.
 * </p>
 *
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 *
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>, adapted from old MagicDrawUmlReader.
 *
 */
public class MagicDrawReader extends AbstractXmiReader {

    /** The native packed MagicDraw format. */
    private static final String EXT_MDZIP = "mdzip";
    /** The MagicDraw packed XML format. */
    private static final String EXT_XMLZIP = "xml.zip";
    /** General zip file, hopefully an XML resides within! */
    private static final String EXT_ZIP = "zip";
    /** The XMI XML file format. */
    private static final String EXT_XML = "xml";
    /** The MDXML file format, which is equivalent to the XML format. */
    private static final String EXT_MDXML = "mdxml";

    /** The Namespace URI to identify a custom profile */
    private static final String CUSTOM_PROFILE_NS_URI = "http://www.magicdraw.com/schemas/";


    // Version-based identifiers
    private MagicDrawIdentifiers mdId = null;
    private XMIIdentifiers xmiId = null;
    private UMLIdentifiers umlId = null;

    /**
     * Default constructor, initializes basic data structures.
     */
    public MagicDrawReader () {
        super();

        mdId = MagicDrawIdentifiers.inst();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.IReader#getProfileNsUriPrefixes()
     */
    public String[] getProfileNsUriPrefixes () {
        // Start with an array of size ONE
        return new String[]{ CUSTOM_PROFILE_NS_URI };
    }

    
    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXmiReader#handleSpecificFormat(java.lang.String)
     */
    @Override
    protected InputStream handleSpecificFormat (String canonPath) throws IOException{
        // get file's name and find its extension
        String ext = "";
        int extIdx = canonPath.lastIndexOf(".");
        if (extIdx > -1) {
            ext = canonPath.substring(extIdx+1);
        }
        if (ext.equalsIgnoreCase(EXT_XML) ||
                ext.equalsIgnoreCase(EXT_MDXML)) {
            // perfect, just return file as input stream
            return new FileInputStream(canonPath);
        } else if (ext.equalsIgnoreCase(EXT_MDZIP)
        		|| ext.equalsIgnoreCase(EXT_ZIP)
        		|| ext.equalsIgnoreCase(EXT_XMLZIP)) {
            // OK, need to unzip first, then return contained file as input stream
            ZipFile mdZip = new ZipFile(canonPath);
            Enumeration<? extends ZipEntry> entries = mdZip.entries();
            // just to make sure, let's find the mdxml file, assuming just 1!!
            ZipEntry mdXmlEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                if (ze.getName().endsWith(EXT_MDXML)
                		|| ze.getName().endsWith(EXT_XML)) {  // found!
                    mdXmlEntry = ze;
                    break;
                } else if (ze.getName().endsWith("magicdraw.uml_model.model")) {
                	// found, but limited support!
                	Util.warn("Support for MD Packed XML format is VERY limited; expect diagram info extraction error!");
                    mdXmlEntry = ze;
                    break;
                }
            }
            if (mdXmlEntry != null) {
                return mdZip.getInputStream(mdXmlEntry);
            }
            throw new FatalModelException("FATAL: Failed to find a ." + EXT_MDXML
                    + "/" + EXT_XML + "/com.nomagic.magicdraw.uml_model.model"
            		+ " file in the supplied ." + EXT_MDZIP +"/."
            		+ EXT_ZIP + " file!");
        } else {  // we don't understand this format, should bail!
            throw new FatalModelException("FATAL: File format '" + ext
                    + "' NOT supported by the MagicDrawReader!");
        }
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXmiReader#determineReaderVersion(org.w3c.dom.Document)
     */
    @Override
    protected boolean determineReaderVersion (Document doc) {
        // Look for the xmi:Documentation tag to find exporter and exporterVerion tags
        Element top = doc.getDocumentElement();

        if (!XMIIdentifiers.isXmiDocument(doc)) {
            Util.error("This is not an XMI document");
            return false;
        }

        // otherwise, set XMI namespace version
        XMIIdentifiers.setNamespace(getNSAttribute(top, XMIIdentifiers.XMINS_PREFIX_DEFAULT));
        // get the corresponding identifier instance
        xmiId = XMIIdentifiers.inst();

        // get XMI Documentation element
        Element docNode = xmiId.getDocumentation(doc);
        if (docNode == null) {
            Util.error("No XMI Documentation tag found!");
            return false;
        }

        boolean foundMatch = false;  // did we find a matching version?
        String lastVerTried = null;
        String docExporterVer = null;
        if (xmiId.doc_getExporter(docNode).equals(mdId.lit(MagicDrawLabel.EXPORTER))) {
            docExporterVer = xmiId.doc_getExporterVersion(docNode);
            VERLOOP: for (String ver : MagicDrawIdentifiers.getSupportedVersions()) {
                switch (Util.compareVersions(docExporterVer, ver)) {
                case 0:  // matched version!
                    MagicDrawIdentifiers.setVersion(ver);
                    foundMatch = true;
                    break VERLOOP;

                case -1:  // doc requires lower version than "ver"
                    if (lastVerTried == null) {
                        // no choice but to use our lowest supported version
                        MagicDrawIdentifiers.setVersion(ver);
                    } else {
                        // we fall back to using our last found version
                        MagicDrawIdentifiers.setVersion(lastVerTried);
                    }
                    foundMatch = true;
                    break VERLOOP;

                case 1:  // doc requires higher version, remember our "ver"
                    lastVerTried = ver;
                }
            }
        }
        if (foundMatch) {  // set identifier instances for namespace version
            mdId = MagicDrawIdentifiers.inst();
            UMLIdentifiers.setNamespace(getNSAttribute(top, UMLIdentifiers.UMLNS_PREFIX_DEFAULT));
            umlId = UMLIdentifiers.inst();
            xmiId.setPrefix(doc.lookupPrefix(xmiId.lit(XMILabel.XMI_NS)));
            umlId.setPrefix(doc.lookupPrefix(umlId.lit(UMLLabel.UML_NS)));
            if (Util.isInfoLevel()) {
                Util.info("Input reader selected: " + mdId.lit(MagicDrawLabel.EXPORTER)
                        + " ver. " + mdId.version());
                Util.info("Namespace version selected for XMI is " + xmiId.version()
                        + " and UML is " + umlId.version());
            }
        } else {
            throw new FatalModelException("MagicDraw UML '" + docExporterVer
                    + "' unsupported! Autocoder reader currently supports only versions "
                    + Arrays.toString(MagicDrawIdentifiers.getSupportedVersions().toArray()));
        }

        return foundMatch;
    }

    private String getNSAttribute (Node node, String attrName) {
        return Util.getNodeAttribute(node, XMLConstants.XMLNS_ATTRIBUTE + ":" + attrName);
    }


    /**
     * In preparation for creation of our internal UML representation, extract
     * MagicDraw XMI DOM, starting with the UML:Model XMI Node contained in
     * the top-level xmi:xmi node, and profile the model first.
     * 
     * @see gov.nasa.jpl.statechart.input.AbstractXmiReader#extractModelNodeFromDOM(org.w3c.dom.Document)
     */
    @Override
    protected Node extractModelNodeFromDOM (Document document) {
        // At this point, document should already be verified to be XMI.
        Node modelNode = null;
        if (document != null) {
            Element top = document.getDocumentElement();

            // Look for any <uml:Model> tags
            NodeList umlNodes = top.getElementsByTagNameNS(umlId.lit(UMLLabel.UML_NS), umlId.lit(UMLLabel.TAG_MODEL));
            if (umlNodes.getLength() == 0) {
                Util.error("No UML Models found!");
                return null;
            }
            if (umlNodes.getLength() > 1) {
                Util.warn("Found more than one model. Ignoring the rest!");
            }

            // Construct our internal UML meta-model instance
            modelNode = umlNodes.item(0);
        }

        return modelNode;
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXmiReader#collectDiagramData(gov.nasa.jpl.statechart.uml.UMLModel)
     */
    @Override
    protected void collectDiagramData (UMLModel model) {
        Document doc = modelGroup.getDocument(model);
        if (doc == null) {
            Util.error("Fata! Null Document object (shouldn't be!), could not collect diagram data.");
            return;
        }
        Timer timer = new Timer();
        System.out.println("Collecting Diagram data from "
                + modelGroup.getModelFilename(model) + "...");

        // create and store Diagram info map.
        DiagramData data = new DiagramData();
        modelGroup.mapModelToDiagramData(model, data);

        Element top = doc.getDocumentElement();
        for (StateMachine machine : model.getStateMachines()) {
            try {
                // get root of diagram MD element
                Node diagramElement = mdId.doc_getRootDiagramElement(top, machine.id());
                if (diagramElement == null) {
                    Util.error("Diagram element NOT found for machine " + machine.getQualifiedName() + "?!");
                    continue;
                }
                //- also get ownedViews node as root of element geometry
                Node ownedViews = mdId.diagram_getOwnedViews(diagramElement);

                gatherDiagramForMachine(diagramElement, ownedViews, machine.id(), data);

                gatherDiagramForElements(ownedViews, data);

                gatherSeparators(ownedViews, machine.id(), data);

                gatherTextboxes(ownedViews, machine.id(), data);

            } catch (XPathExpressionException e) {
                Util.reportException(e, "MagicDrawReader.collectDiagramData(): ");
            }
        }

        if (Util.isDebugLevel()) Util.debug("Diagram data aquired! " + data.toString());
        if (Util.isInfoLevel()) timer.markTime();
    }

    /**
     * Finds diagram window info for the machine, looking under XPath
     * mdOwnedViews/mdElement.
     * 
     * @param diagramElement  root XMI Node to look for window bounds 
     * @param ownedViews  XMI Node for mdOwnedViews
     * @param smId  ID of StateMachine, used to storing mapping
     * @param data  DiagramData for storing gathered data 
     * @throws XPathExpressionException
     */
    private void gatherDiagramForMachine (Node diagramElement, Node ownedViews, String smId, DiagramData data)
    throws XPathExpressionException {
        String windowBoundsStr = (String) UMLElement.xpath.evaluate(
                mdId.lit(MagicDrawLabel.TAG_DIAGRAM_WINDOW_BOUNDS),
                diagramElement, XPathConstants.STRING);
        String zoomFactorStr = (String) UMLElement.xpath.evaluate(
                mdId.lit(MagicDrawLabel.TAG_ZOOM_FACTOR) + "/@"
                + XMIIdentifiers.value(),
                diagramElement, XPathConstants.STRING);
        String frameGeometryStr = (String) UMLElement.xpath.evaluate(
                mdId.lit(MagicDrawLabel.TAG_ELEMENT) + "[@"
                    + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_DIAGRAM_FRAME)
                    + "']/"
                + mdId.lit(MagicDrawLabel.TAG_GEOMETRY),
                ownedViews, XPathConstants.STRING);
        // construct machine's diagram element object
        int[] coordInts = toIntArray(frameGeometryStr);
        MachineElement mElem = new MachineElement(smId);
        for (int i=0; i < coordInts.length; i+=2) {
            Point p = new Point(coordInts[i], coordInts[i+1]);
            mElem.addPoint(p);
        }
        if (zoomFactorStr != null && zoomFactorStr.length() > 0) {
            mElem.setZoomFactor(Double.parseDouble(zoomFactorStr));
        }
        if (windowBoundsStr != null && windowBoundsStr.length() > 0) {
            coordInts = toIntArray(windowBoundsStr);
            if (coordInts.length >= 4) {
                mElem.setWindowBounds(new Point(coordInts[0], coordInts[1]),
                        new Point(coordInts[2], coordInts[3]));
            }
        }
        data.elementMap.put(smId, mElem);
    }

    /**
     * Gather diagram nodes for State, Pseudostate, Transition (incl. to self),
     * and Decision (Junction states)
     * 
     * @param ownedViews  XMI node under which to search for element diagram data
     * @param data  DiagramData for storing gathered data 
     * @throws XPathExpressionException  in case of unexpected error in XPath eval
     */
    private void gatherDiagramForElements (Node ownedViews, DiagramData data)
    throws XPathExpressionException {

        NodeList diagramNodes = (NodeList) UMLElement.xpath.evaluate(
                ".//" + mdId.lit(MagicDrawLabel.TAG_ELEMENT)
                    + "[@" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + umlId.lit(UMLLabel.TYPE_REGION)
                    + "' or @" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + umlId.lit(UMLLabel.TYPE_STATE)
                    + "' or @" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_DECISION)
                    + "' or @" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_PSEUDOSTATE)
                    + "' or @" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + umlId.lit(UMLLabel.TYPE_TRANSITION)
                    + "' or @" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_TRANSITION_TO_SELF)
                    + "']",
                ownedViews,
                XPathConstants.NODESET);
        for (int i=0; i < diagramNodes.getLength(); ++i) {
            Node dNode = diagramNodes.item(i);
            // obtain element class
            String elementClass = Util.getNodeAttribute(dNode, mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS));
            // grab element refid
            String elementID = (String) UMLElement.xpath.evaluate(
                    mdId.lit(MagicDrawLabel.TAG_ELEMENT_ID) + "/@" + XMIIdentifiers.idref(),
                    dNode, XPathConstants.STRING);
            if (elementID == null || elementID.length() == 0) {
                // attempt to look for @href="<file>#<id>"
                String hrefStr = (String) UMLElement.xpath.evaluate(
                        mdId.lit(MagicDrawLabel.TAG_ELEMENT_ID) + "/@" + mdId.lit(MagicDrawLabel.KEY_HREF),
                        dNode, XPathConstants.STRING);
                if (hrefStr == null || hrefStr.length() == 0) {
                    Util.error("Error! No idref found for elementClass " + elementClass + "!");
                    continue;
                } else {  // grab the xmi-id part of the href
                    Pattern p = Pattern.compile(".+?[#](\\w+)");
                    Matcher m = p.matcher(hrefStr);
                    if (m.matches()) {
                        elementID = m.group(1);
                    } else {
                        Util.error("Error! elementClass " + elementClass
                                + " href attribute '" + hrefStr
                                + "' has NO XMI ID!");
                        continue;
                    }
                }
            }

            // grab and parse geometry values into array of points
            String geometryStr = (String) UMLElement.xpath.evaluate(
                    mdId.lit(MagicDrawLabel.TAG_GEOMETRY),
                    dNode, XPathConstants.STRING);
            List<Point> points = getIntsAsPoints(geometryStr, "Element ID " + elementID);
            if (points == null) {
                continue;
            }

            // construct the diagram element object
            DiagramElement dElem = null;
            if (elementClass == null) {  // something wrong?
                Util.error("A diagram node found with unknown elementClass!");
                continue;
            } else if (elementClass.equals(umlId.lit(UMLLabel.TYPE_TRANSITION))) {
                dElem = new TransitionElement(elementID);
                for (Point p : points) {
                    dElem.addPoint(p);
                }
            } else if (elementClass.equals(mdId.lit(MagicDrawLabel.MD_TRANSITION_TO_SELF))) {
                /* special treatment for self-transitions:
                 * - toSelf flag is set to true
                 * - pair of points are origin of arrow and width/height of box
                 * - "edge" is needed to determine whether horizontal or vertical
                 */
                dElem = new TransitionToSelfElement(elementID);
                // fetch and store the 'edge' element
                String edgeStr = (String) UMLElement.xpath.evaluate(
                        mdId.lit(MagicDrawLabel.TAG_EDGE) + "/@" + XMIIdentifiers.value(),
                        dNode, XPathConstants.STRING);
                ((TransitionToSelfElement) dElem).setEdge(Integer.parseInt(edgeStr));
                // store points
                for (Point p : points) {
                    dElem.addPoint(p);
                }
            } else {  // remaining possibilities are State or Pseudostate!
                // assume two points
                VertexElement vElem = new VertexElement(elementID, points.get(0), points.get(1));
                if (elementClass.equals(mdId.lit(MagicDrawLabel.MD_PSEUDOSTATE))
                        || elementClass.equals(mdId.lit(MagicDrawLabel.MD_DECISION))) {
                    // set a default color
                    vElem.setOutlineColor(VertexElement.defaultPseudostateFillColor);
                } else {
                    int fillColor = VertexElement.defaultStateFillColor;

                    // grab color property
                    Node colorPropNode = (Node) UMLElement.xpath.evaluate(
                            mdId.lit(MagicDrawLabel.TAG_PROPERTIES) + "/"
                                + mdId.lit(MagicDrawLabel.TAG_ELEMENT)
                                + "[@" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                                + "='" + mdId.lit(MagicDrawLabel.MD_COLOR_PROPERTY)
                                + "']",
                            dNode, XPathConstants.NODE);
                    if (colorPropNode != null) {
                        String propID = (String) UMLElement.xpath.evaluate(
                                mdId.lit(MagicDrawLabel.TAG_PROPERTY_ID),
                                dNode, XPathConstants.STRING);
                        if (mdId.lit(MagicDrawLabel.VALUE_FILL_COLOR).equals(propID)) {
                            String colorProp = (String) UMLElement.xpath.evaluate(
                                    mdId.lit(MagicDrawLabel.TAG_VALUE)
                                        + "/@" + XMIIdentifiers.value(),
                                    dNode, XPathConstants.STRING);
                            if (colorProp != null && colorProp.length() > 0) {
                                fillColor = Integer.parseInt(colorProp);
                            }
                        }
                    }
                    vElem.setOutlineColor(fillColor);
                }
                dElem = vElem;
            }
            // store in map
            data.elementMap.put(elementID, dElem);
        }
    }

    /**
     * Finds region separator diagram info.
     * @param ownedViews  XMI node under which to search for separators
     * @param data  DiagramData for storing gathered data 
     * @throws XPathExpressionException  in case of unexpected error in XPath eval
     */
    private void gatherSeparators (Node ownedViews, String smId, DiagramData data)
    throws XPathExpressionException {

        NodeList diagramNodes = (NodeList) UMLElement.xpath.evaluate(
                ".//" + mdId.lit(MagicDrawLabel.TAG_ELEMENT)
                    + "[@" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_SPLIT) + "']",
                ownedViews, XPathConstants.NODESET);
        for (int i=0; i < diagramNodes.getLength(); ++i) {
            Node dNode = diagramNodes.item(i);
            // get geometry and construct elem
            String geometryStr = (String) UMLElement.xpath.evaluate(
                    mdId.lit(MagicDrawLabel.TAG_GEOMETRY),
                    dNode, XPathConstants.STRING);
            List<Point> points = getIntsAsPoints(geometryStr, "Separator " + dNode.getNodeName());
            if (points == null) continue;

            // got two coordinate points, now add new separator element
            List<SeparatorElement> sepElems = data.separatorElementMap.get(smId);
            if (sepElems == null) {
                sepElems = Util.newList();
                data.separatorElementMap.put(smId, sepElems);
            }
            sepElems.add(new SeparatorElement(points.get(0), points.get(1)));
        }
    }

    /**
     * Finds text box diagram info.
     * @param ownedViews  XMI node under which to search for textboxes
     * @param data  DiagramData for storing gathered data 
     * @throws XPathExpressionException  in case of unexpected error in XPath eval
     */
    private void gatherTextboxes (Node ownedViews, String smId, DiagramData data)
    throws XPathExpressionException {

        NodeList diagramNodes = (NodeList) UMLElement.xpath.evaluate(
                ".//" + mdId.lit(MagicDrawLabel.TAG_ELEMENT)
                    + "[@" + mdId.lit(MagicDrawLabel.KEY_ELEMENT_CLASS)
                    + "='" + mdId.lit(MagicDrawLabel.MD_TEXTBOX) + "']",
                ownedViews, XPathConstants.NODESET);
        for (int i=0; i < diagramNodes.getLength(); ++i) {
            Node dNode = diagramNodes.item(i);
            // get geometry and text to construct elem
            String geometryStr = (String) UMLElement.xpath.evaluate(
                    mdId.lit(MagicDrawLabel.TAG_GEOMETRY),
                    dNode, XPathConstants.STRING);
            String txtStr = (String) UMLElement.xpath.evaluate(
                    mdId.lit(MagicDrawLabel.TAG_TEXT),
                    dNode, XPathConstants.STRING);
            int[] coordInts = toIntArray(geometryStr);
            if (coordInts.length < 2) {
                Util.error("Warning: Textbox for '" + txtStr + "' specified incomplete coordinates!");
                continue;
            }

            // got pair of coordinate dims, now add new text element
            List<TextElement> txtElems = data.textElementMap.get(smId);
            if (txtElems == null) {
                txtElems = Util.newList();
                data.textElementMap.put(smId, txtElems);
            }
            txtElems.add(new TextElement(txtStr, new Point(coordInts[0], coordInts[1])));
        }
    }

    private List<Point> getIntsAsPoints (String geometryStr, String errSrc) {
        int[] coordInts = toIntArray(geometryStr);
        if (coordInts.length < 4) {  // something wrong
            Util.error("Warning: " + errSrc + " specified incomplete geometric coordinates (fewer than 4 integers)!");
            return null;
        }

        List<Point> points = Util.newList();
        for (int j=0; j < coordInts.length; j+=2) {
            points.add(new Point(coordInts[j], coordInts[j+1]));
        }
        return points;
    }

}
