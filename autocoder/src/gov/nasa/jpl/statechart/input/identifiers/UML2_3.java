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
import gov.nasa.jpl.statechart.uml.UMLElement;
import gov.nasa.jpl.statechart.uml.UMLOpaqueBehavior;
import gov.nasa.jpl.statechart.uml.UMLValueSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

/**
 * Support for reading UML schema version 2.3.
 * <p>
 * Copyright &copy; 2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
@VersionSupport(name="http://schema.omg.org/spec/UML/2.3", version="2.3")
public class UML2_3 extends UML2_2 {

    public UML2_3 () {
        super();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#behavior_getSpec(gov.nasa.jpl.statechart.uml.UMLOpaqueBehavior)
     */
    @Override
    public Map<String,String> behavior_getSpec (UMLOpaqueBehavior beh) {
        Map<String,String> specBodyLangMap = new LinkedHashMap<String,String>();

        try {
            // UML spec lets us assume that the body and language elements are
            // paired in order..so if one or the other is missing, we'll store
            // null in map.  Exception:  There can be no more than ONE null body.
            NodeList bodyNodes = (NodeList) UMLElement.xpath.evaluate(
                    lit(UMLLabel.TAG_BODY),
                    beh.getNode(), XPathConstants.NODESET);
            NodeList langNodes = (NodeList) UMLElement.xpath.evaluate(
                    lit(UMLLabel.TAG_LANGUAGE),
                    beh.getNode(), XPathConstants.NODESET);

            for (int i=0 ; i < Math.max(bodyNodes.getLength(), langNodes.getLength()) ; ++i) {
                // assume null until found
                String body = null;
                String lang = null;
                if (i < bodyNodes.getLength()) {
                    body = bodyNodes.item(i).getTextContent();
                }
                if (i < langNodes.getLength()) {
                    lang = langNodes.item(i).getTextContent();
                }
                if (i >= bodyNodes.getLength()+1) {
                    Util.error("ERROR! UML2_3.behavior_getSpec() encountered more than ONE language ["
                            + lang + "] specified without a specification body, overriding previous Map entry ["
                            + specBodyLangMap.get(null) + "]!");
                } else if (i >= bodyNodes.getLength()) {
                    Util.warn("WARNNG! UML2_3.behavior_getSpec() encountered a language ["
                            + lang + "] specified without a specification body");
                }
                specBodyLangMap.put(body, lang);
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_3.behavior_getSpec(): ");
        }

        return specBodyLangMap;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.identifiers.UML2_0#expr_getBody(gov.nasa.jpl.statechart.uml.UMLValueSpecification)
     */
    @Override
    public String value_getExprBody (UMLValueSpecification spec) {
        String body = "";

        try {
            body = (String) UMLElement.xpath.evaluate(
                    lit(UMLLabel.TAG_BODY),
                    spec.getNode(), XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UML2_3.value_getExprBody(): ");
        }

        return body;
    }

}
