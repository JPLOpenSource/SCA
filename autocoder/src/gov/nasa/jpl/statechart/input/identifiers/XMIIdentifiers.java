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
package gov.nasa.jpl.statechart.input.identifiers;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers;
import gov.nasa.jpl.statechart.input.VersionSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class provides a "database" of XMI namespace identifiers to shield
 * code from literal changes that may result from namespace version changes,
 * although such change is unlikely.  This at least puts XMI-related literals
 * in a single location in this codebase.
 * <p>
 * Access the identifier mappings via the singleton instance {@link #inst()}.
 * Set the singleton object to the namespace version supported via
 * {@link #setNamespace(String)}.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class XMIIdentifiers extends AbstractXMLIdentifiers<XMILabel> {
    /** Default namespace prefix for XMI. */
    public static final String XMINS_PREFIX_DEFAULT = "xmi";

    /** Singleton instance of this identifier class. */
    private static XMIIdentifiers singleton = null;
    /** Sorted map of XMI Namespace version to the namespace URI. */
    private static SortedMap<String,String> nsUriByVersion = null;
    /** Map of namespace URI to instance of this class. */
    private static Map<String,XMIIdentifiers> instByNsUri = null;

    /**
     * Returns the current singleton instance, corresponding to the latest
     * supported XMI namespace version or the namespace set by {@link #setNamespace(String)}.
     * @return the XMIIdentifiers singleton instance.
     */
    public static XMIIdentifiers inst () {
        if (singleton == null) {  // lazy init
            init();
        }
        return singleton;
    }

    /**
     * Sets the XMI namespace URI and affects the singleton instance returned.
     * Must be a supported namespace, which is defined by an inner class
     * extending this class and annotated with {@link VersionSupport}.
     * @param ns the XMI namespace URI to set
     */
    public static void setNamespace (String ns) {
        if (instByNsUri == null) {  // lazy init
            init();
        }
        if (instByNsUri.containsKey(ns)) {
            singleton = instByNsUri.get(ns);
        } else {
            Util.error("** XMI Namespace '" + ns + "' not supported!");
        }
    }

    /**
     * Returns a list of the XMI Namespace URIs of the supported namespace versions.
     * @return list of URI strings
     */
    public static List<String> getSupportedNamespaces () {
        if (nsUriByVersion == null) {
            init();
        }
        return new ArrayList<String>(nsUriByVersion.values());
    }

    /**
     * A convenience method that returns the XMI Namespace-prefixed literal for
     * an XMI ID for the currently set namespace version.
     * @return the XMI ID literal, commonly, "xmi:id"
     */
    public static String id () {
        return inst().prefixed(XMILabel.KEY_ID);
    }

    /**
     * A convenience method that returns the XMI Namespace-prefixed literal for
     * an XMI ID-ref for the currently set namespace version.
     * @return the XMI ID-ref literal, commonly, "xmi:idref"
     */
    public static String idref () {
        return inst().prefixed(XMILabel.KEY_IDREF);
    }

    /**
     * A convenience method that returns the XMI Namespace-prefixed literal for
     * an XMI type for the currently set namespace version.
     * @return the XMI type literal, commonly, "xmi:type"
     */
    public static String type () {
        return inst().prefixed(XMILabel.KEY_TYPE);
    }

    /**
     * A convenience method that returns the XMI Namespace-prefixed literal for
     * an XMI value for the currently set namespace version.
     * @return the XMI value iteral, commonly, "xmi:value"
     */
    public static String value () {
        return inst().prefixed(XMILabel.KEY_VALUE);
    }

    /**
     * Returns whether the supplied W3C Document is an XMI document.
     * 
     * @param doc       DOM {@link Document} object
     * @return boolean  <code>true</code> if the given Document object begins
     *   with an XMI element.
     */
    public static boolean isXmiDocument (Document doc) {
        return doc.getDocumentElement().getLocalName().equals(XMILabel.TAG_XMI.defaultLiteral());
    }

    /**
     * Initializes the singleton object of this class, setting the singleton
     * to the latest version of namespace identifiers supported.
     */
    synchronized private static void init () {
        if (singleton != null) return;  // don't repeat init

        nsUriByVersion = new TreeMap<String,String>();
        instByNsUri = new HashMap<String,XMIIdentifiers>();
        // instantiate annotated inner class(es) and get last version as singleton
        singleton = AbstractXMLIdentifiers.createSupportedSingletons(XMIIdentifiers.class, nsUriByVersion, instByNsUri);

        if (Util.isInfoLevel()) Util.info("Supported XMI namespace version(s) "
                + Arrays.toString(nsUriByVersion.keySet().toArray()));
    }


    /**
     * Default, protected constructor, populates the mapper of identifier label
     * to string literal.
     */
    protected XMIIdentifiers () {
        super();
        setPrefix(XMINS_PREFIX_DEFAULT);
        for (XMILabel l : XMILabel.values()) {
            literalMap.put(l, l.defaultLiteral());
        }
        // store namespace string using the VersionSupport annotation, if any
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null) {
            literalMap.put(XMILabel.XMI_NS, verAnnote.name());
        }
    }


    /**
     * XMI version-specific implementation to obtain a mapping of Profile
     * names to Profile Namespace URIs.
     *
     * @param xmiElement  The XMI element node.
     * @return  {@link Map} of Profile name string to URI string.
     */
    public abstract Map<String,String> getProfileNamespaceMap (Element xmiElement);

    /**
     * XMI version-specific implementation to obtain the Documentation element.
     *
     * @param doc  The Document to process
     * @return XMI Documentation Element node
     */
    public abstract Element getDocumentation (Document doc);

    /**
     * XMI version-specific implementation to obtain the exporter string from
     * the supplied Documentation element.
     *
     * @param docNode  The XMI Documentation Element from which to obtain the Exporter
     * @return Exporter string
     */
    public abstract String doc_getExporter (Element docNode);

    /**
     * XMI version-specific implementation to obtain the exporter version from
     * the supplied Documentation element.
     *
     * @param docNode  The XMI Documentation Element from which to obtain the Exporter Version
     * @return Exporter Version string
     */
    public abstract String doc_getExporterVersion (Element docNode);

}
