/**
 * Created Jun 02, 2013.
 * <p>
 * Copyright 2009--2013, by the California Institute of Technology. ALL RIGHTS
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

import gov.nasa.jpl.statechart.input.VersionSupport;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Support for reading XMI schema version 2.4.1.
 * <p>
 * Copyright &copy; 2009--2013 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen.Cheng@jpl.nasa.gov
 *
 */
@VersionSupport(name="http://www.omg.org/spec/XMI/20110701", version="2.4.1")
public class XMI2_4_1 extends XMI2_1 {

    public XMI2_4_1 () {
        super();

        // In XMI 2.4.1 forward, exporter and exporterVersion are node elements
        //- _tag_ labels replace the _attribute_ labels
        literalMap.put(XMILabel.TAG_EXPORTER, literalMap.get(XMILabel.KEY_EXPORTER));
        literalMap.put(XMILabel.TAG_EXPORTER_VERSION, literalMap.get(XMILabel.KEY_EXPORTER_VERSION));
        literalMap.put(XMILabel.KEY_EXPORTER, null);
        literalMap.put(XMILabel.KEY_EXPORTER_VERSION, null);
    }

    /**
     * In XMI 2.4.1, the exporter information resides as an inner element of
     * the Documentation element.
     * 
     * @see gov.nasa.jpl.statechart.input.identifiers.XMI2_1#doc_getExporter(org.w3c.dom.Element)
     */
    @Override
    public String doc_getExporter (Element docNode) {
        NodeList nodes = docNode.getElementsByTagName(inst().prefixed(XMILabel.TAG_EXPORTER));

        // get the first exporter element and fetch its body
        Element exporterElement = (Element) nodes.item(0);
        return exporterElement.getTextContent();
    }

    /**
     * In XMI 2.4.1, the exporterVersion information resides as an inner element
     * of the Documentation element.
     *
     * @see gov.nasa.jpl.statechart.input.identifiers.XMI2_1#doc_getExporterVersion(org.w3c.dom.Element)
     */
    @Override
    public String doc_getExporterVersion (Element docNode) {
        NodeList nodes = docNode.getElementsByTagName(inst().prefixed(XMILabel.TAG_EXPORTER_VERSION));

        // get the first exporter element and fetch its body
        Element exporterElement = (Element) nodes.item(0);
        return exporterElement.getTextContent();
    }

}
