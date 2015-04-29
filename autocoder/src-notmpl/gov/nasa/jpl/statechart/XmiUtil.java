package gov.nasa.jpl.statechart;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmiUtil {

    private XmiUtil () {  // no instantiation
    }

    /**
     * filterElements
     *
     * Patterned after the findNodes method, but more DOM-ish.
     *
     * @param nodes   A NodeList of elements
     * @param attrs   A map of attribute name/value pairs that must match. May
     *                be empty. In that case all the elements are 
     *                accepted.  Use the special value of "*" to accept
     *                any node with the attribute.
     */
    public static List<Element> filterElements (NodeList nodes,
            Map<String, String> attrs) {
        List<Element> elements = Util.newList();
    
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            boolean accept = true;
    
            Iterator<Map.Entry<String, String>> iter = attrs.entrySet().iterator();
    
            while (iter.hasNext() && accept) {
                Map.Entry<String, String> entry = iter.next();
    
                String attribute = entry.getKey();
                String value = entry.getValue();
    
                accept &= element.hasAttribute(attribute);
                if (accept) {
                    accept &= (value.equals("*") || element.getAttribute(
                            attribute).equals(value));
                }
            }
    
            if (accept)
                elements.add(element);
        }
    
        return elements;
    }

    public static List<Element> filterElements (NodeList nodes,
            String attribute, String value) {
        return filterElements(nodes, Collections.singletonMap(attribute, value));
    }

    public static List<Element> filterElementsNS (List<Element> nodes,
            String nsURI, String attribute, String value) {
        return filterElementsNS(nodes, Collections.singletonMap(attribute,
                nsURI), Collections.singletonMap(attribute, value));
    }

    public static List<Element> filterElementsNS (NodeList nodes, String nsURI,
            String attribute, String value) {
        List<Element> elements = Util.newList();
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add((Element) nodes.item(i));
        }
    
        return filterElementsNS(elements, Collections.singletonMap(attribute,
                nsURI), Collections.singletonMap(attribute, value));
    }

    public static List<Element> filterElementsNS (List<Element> nodes,
            Map<String, String> nsURI, Map<String, String> attrs) {
        List<Element> elements = Util.newList();
    
        for (Element element : nodes) {
            boolean accept = true;
    
            Iterator<Map.Entry<String, String>> iter = attrs.entrySet().iterator();
    
            while (iter.hasNext() && accept) {
                Map.Entry<String, String> entry = iter.next();
    
                String attribute = entry.getKey();
                String value = entry.getValue();
                String ns = nsURI.get(attribute);
    
                accept &= element.hasAttributeNS(ns, attribute);
                if (accept) {
                    accept &= (value.equals("*") || element.getAttributeNS(ns,
                            attribute).equals(value));
                }
            }
    
            if (accept)
                elements.add(element);
        }
    
        return elements;
    }

    /**
     * Only keep Nodes that have a certain tag
     */
    public static List<Element> filterElementsByTagName (NodeList nodes,
            String tagName) {
        List<Element> elements = Util.newList();
    
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (element.getTagName().equals(tagName))
                elements.add(element);
        }
    
        return elements;
    }

    /**
     * Prints out the attributes of a Node
     */
    public static String attrsToString (Node node) {
        StringBuilder sb = new StringBuilder();
    
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++)
                sb.append(" " + attrs.item(i).getNodeName() + ":"
                        + attrs.item(i).getNodeValue());
        }
    
        return sb.toString();
    }

}
