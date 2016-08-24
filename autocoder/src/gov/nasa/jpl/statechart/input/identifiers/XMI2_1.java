/**
 * Created Aug 26, 2009.
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
package gov.nasa.jpl.statechart.input.identifiers;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.VersionSupport;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Support for reading XMI schema version 2.1.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name="http://schema.omg.org/spec/XMI/2.1", version="2.1")
public class XMI2_1 extends XMIIdentifiers {

    public XMI2_1 () {
        super();

        // In XMI 2.1, exporter and exporterVersion are Documentation attributes
        //- so we null out the _tag_ labels
        literalMap.put(XMILabel.TAG_EXPORTER, null);
        literalMap.put(XMILabel.TAG_EXPORTER_VERSION, null);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers#getProfileNamespaceMap(org.w3c.dom.Element)
     */
    @Override
    public Map<String,String> getProfileNamespaceMap (Element xmiElement) {
        // From the XMI top element tag, query all its attributes and
        // extract a map of xmlns:*="<URI>/*[.xmi]"
        Map<String,String> profileNsMap = Util.newSortedMap();

        Pattern regexProfileNsUri = Pattern.compile(XMLConstants.XMLNS_ATTRIBUTE + ":(\\w+)=(http://.+?/\\1([.]xmi)?)");

        // Obtain Document element attributes
        NamedNodeMap nodeMap = xmiElement.getAttributes();

        for (int i=0 ; i < nodeMap.getLength() ; ++i ) {
            Matcher matcherProfileNsUri = regexProfileNsUri.matcher(nodeMap.item(i).getNodeName()
                    + "=" + nodeMap.item(i).getNodeValue());

            if (matcherProfileNsUri.matches()) {
                // add profile->URI mapping to map
                profileNsMap.put(matcherProfileNsUri.group(1), matcherProfileNsUri.group(2));
            }
        }

        return profileNsMap;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers#getDocumentation(org.w3c.dom.Document)
     */
    @Override
    public Element getDocumentation (Document doc) {
        // Look for the xmi:Documentation element
        NodeList docNodes = doc.getElementsByTagNameNS(inst().lit(XMILabel.XMI_NS), inst().lit(XMILabel.TAG_DOCUMENTATION));

        int cnt = docNodes.getLength();
        if (cnt < 1) {  // there must be at least ONE
            return null;
        } else if (cnt > 1) {  // issue a warning
            Util.warn("Found more than one ("+cnt+") xmi:Documentation elements, picking the last one!");
        }

        // found XMI's Documentation tag, take the last one
        return (Element) docNodes.item(cnt-1);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers#doc_getExporter(org.w3c.dom.Element)
     */
    @Override
    public String doc_getExporter (Element docNode) {
        // Find the exporter string in the given Documentation element.
        return docNode.getAttribute(inst().lit(XMILabel.KEY_EXPORTER));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers#doc_getExporterVer(org.w3c.dom.Element)
     */
    @Override
    public String doc_getExporterVersion (Element docNode) {
        // Find the exporterVersion string in the given Documentation element.
        return docNode.getAttribute(inst().lit(XMILabel.KEY_EXPORTER_VERSION));
    }

}
