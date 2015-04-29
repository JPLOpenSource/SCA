/**
 * This class implements the namespace context from UML XMI documents.
 */
package gov.nasa.jpl.statechart.input;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.ProfileIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.XMILabel;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class ReaderNamespaceContext implements NamespaceContext {
    // Namespaces for XMI and UML.
    private static final String XMI_NS = XMIIdentifiers.inst().lit(XMILabel.XMI_NS);
    private static final String UML_NS = UMLIdentifiers.inst().lit(UMLLabel.UML_NS);

    private static final Map<String, String> uri2prefix;
    private static final Map<String, String> prefix2uri;

    static {
        uri2prefix = Util.newMap();
        prefix2uri = Util.newMap();

        uri2prefix.put(XMI_NS, XMIIdentifiers.inst().getPrefix());
        uri2prefix.put(UML_NS, UMLIdentifiers.inst().getPrefix());
        uri2prefix.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
        uri2prefix.put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                XMLConstants.XMLNS_ATTRIBUTE);
        uri2prefix.put(XMLConstants.NULL_NS_URI, XMLConstants.DEFAULT_NS_PREFIX);

        prefix2uri.put(XMIIdentifiers.inst().getPrefix(), XMI_NS);
        prefix2uri.put(UMLIdentifiers.inst().getPrefix(), UML_NS);
        prefix2uri.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        prefix2uri.put(XMLConstants.XMLNS_ATTRIBUTE,
                XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        prefix2uri.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);

        // add prefix and URI mappings for supported profiles
        for (String prefix : ProfileIdentifiers.getSupportedPrefixes()) {
            String uri = ProfileIdentifiers.getNamespaceByPrefix(prefix);
            uri2prefix.put(uri, prefix);
            prefix2uri.put(prefix, uri);
        }
    }

	public static String elementAttr() {
		return "element";
	}
	public static String definingAttr() {
		return "definingFeature";
	}
    public static String nameAttr () {
        return "name";
    }
    public static String valueAttr () {
        return "value";
    }
    public static String typeAttr () {
        return "type";
    }
    public static String hrefAttr () {
        return "href";
    }
	public static String srcAttr() {
		return "source";
	}
	public static String tgtAttr() {
		return "target";
	}


    public String getNamespaceURI (String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException();

        if (!prefix2uri.containsKey(prefix))
            return null;

        return prefix2uri.get(prefix);
    }

    public String getPrefix (String namespaceURI) {
        if (namespaceURI == null)
            throw new IllegalArgumentException();

        if (!uri2prefix.containsKey(namespaceURI))
            return null;

        return uri2prefix.get(namespaceURI);
    }

    public Iterator<String> getPrefixes (String namespaceURI) {
        String prefix = getPrefix(namespaceURI);

        if (prefix == null)
            return Collections.<String> emptyList().iterator();

        return Collections.singletonList(prefix).iterator();
    }
}
