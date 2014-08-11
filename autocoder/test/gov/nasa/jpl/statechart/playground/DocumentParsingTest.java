package gov.nasa.jpl.statechart.playground;

import java.io.File;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DocumentParsingTest {

    protected static boolean debugOn = false;

    public static void testme (Document doc) {
        debugOn = true;
        System.out.println("Doc namespace: " + doc.getNamespaceURI());
        Node n = doc.getDocumentElement();
        printNode(System.out, n);
        System.out.println("Top node prefix: " + n.getPrefix());
        n = doc.getFirstChild();
        printNode(System.out, n);
        n = n.getNextSibling();
        printNode(System.out, n);
        Element e = doc.getDocumentElement();
        n = e.getFirstChild();
        printNode(System.out, n);
        n = n.getNextSibling();
        printNode(System.out, n);
        System.out.println("Local name of child: " + n.getLocalName()
                + ", namespace: " + n.getNamespaceURI());
        NodeList l = e.getElementsByTagNameNS(
                "http://schema.omg.org/spec/XMI/2.1", "xmi:Documentation");
        for (int i = 0; i < l.getLength(); i++) {
            n = l.item(i);
            printNode(System.out, n);
        }
        l = doc.getElementsByTagName("xmi:Documentation");
        for (int i = 0; i < l.getLength(); i++) {
            n = l.item(i);
            printNode(System.out, n);
        }
        System.exit(0);
    }

    /**
     * Test method to print information on a node.
     * 
     * @param ps
     *           The stream to which to print.
     * @param n
     *           The node to print.
     */
    protected static void printNode (PrintStream ps, Node n) {
        if (!debugOn) {
            return;
        }
        ps.print(n + " name: ");
        ps.print(n.getNodeName() + ", type: ");
        ps.print(n.getNodeType() + ", value: ");
        ps.print(n.getNodeValue() + ", ");
        if (null != n.getAttributes()) {
            ps.print("Attrs: ");
            NamedNodeMap attrs = n.getAttributes();
            String separator = "";
            for (int i = 0; i < attrs.getLength(); ++i) {
                ps.print(separator + attrs.item(i).getNodeName() + ": "
                        + attrs.item(i).getNodeValue());
                separator = ", ";
            }
        }
        ps.println("");
    }

    public static void main (String[] argv) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder db = factory.newDocumentBuilder();
        System.out.println("DOM Impl is "
                + db.getDOMImplementation().toString());
        System.out.println("Namespace aware? " + db.isNamespaceAware());
        // Each state machine may need to cross-reference another,
        // so first read in all the state machine elements into
        // a global xmi:id map
        for (int i = 0; i < argv.length; i++) {
            try {
                Document doc = db.parse(new File(argv[i]));
                testme(doc);
            } catch (Exception e) {
                System.out.flush();
                System.out.println("");
                System.out.println("*** Error parsing file " + argv[i] + ".");
                System.out.println("");
            }
        }
    }

}
