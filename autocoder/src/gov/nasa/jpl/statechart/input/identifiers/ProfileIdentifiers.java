/**
 * Created Apr 7, 2011.
 * <p>
 * Copyright 2009-2011, by the California Institute of Technology. ALL RIGHTS
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
import gov.nasa.jpl.statechart.input.IReader;
import gov.nasa.jpl.statechart.input.VersionSupport;
import gov.nasa.jpl.statechart.uml.UMLElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides a "database" of Profile-specific namespace identifiers,
 * in the spirit of XML- and UMLIdentifiers, except that this one class houses
 * the knowledge of all supported profiles.
 * <br/>
 * <p>
 * The ramification is that there is not a single default Namespace prefix
 * (like "uml" or "xmi"), as each profile will have its own namespace prefix.
 * In addition, each model may contribute additional profile names.
 * <br/>
 * </p>
 * </p>
 * These are the essential distinctions between this class and the previous two,
 * which affects how this class is constructed, what the maps store, and the
 * irrelevance of setNamespace and a number of other methods (which either has
 * not been defined for this class, or has been overridden to throw an
 * {@link UnsupportedOperationException}).
 * <br/>
 * </p>
 * <p>
 * Another caveat of this class is that the MagicDraw Schema Namespace URI is
 * embedded, which is probably NOT desirable down the road when we need to
 * support multiple UML readers.
 * <br/>
 * </p>
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class ProfileIdentifiers extends AbstractXMLIdentifiers<ProfileLabel> {

    private static final String NS_PREFIX_PLACEHOLDER = "<NS_URI_PREFIX>";


    @VersionSupport(name="<NS_URI_PREFIX>c___ANSI_profile.xmi", prefix="c___ANSI_profile")
    public class Profile_C_ANSI extends ProfileIdentifiers {
        protected Profile_C_ANSI () {
            super();
        }
    }

    @VersionSupport(name="<NS_URI_PREFIX>SCAProfile.xmi", prefix="SCAProfile")
    public class Profile_SCAProfile extends ProfileIdentifiers {
        protected Profile_SCAProfile () {
            super();
        }
    }

    /** Singleton instance of this identifier class. */
    private static ProfileIdentifiers singleton = null;
    /** Sorted map of untouched Namespace prefix to URI. */
    private static SortedMap<String,String> prestineNsUriByPrefix = null;
    /** Sorted map of Namespace prefix to the namespace URI. */
    private static SortedMap<String,String> nsUriByPrefix = null;

    /**
     * Returns the singleton instance, which contains all supported profiles.
     * 
     * @return the ProfileIdentifiers singleton instance.
     */
    public static ProfileIdentifiers inst () {
        if (singleton == null) {  // lazy init
            init();
        }
        return singleton;
    }

    /**
     * Returns a list of the supported profile Namespace URIs.
     * @return list of URI strings of the supported profiles.
     */
    public static List<String> getSupportedNamespaces () {
        if (nsUriByPrefix == null) {
            init();
        }
        return new ArrayList<String>(nsUriByPrefix.values());
    }

    /**
     * Returns a list of prefixes of supported Namespace URIs.
     * @return list of prefix strings for supported profiles.
     */
    public static List<String> getSupportedPrefixes () {
        if (nsUriByPrefix == null) {
            init();
        }
        return new ArrayList<String>(nsUriByPrefix.keySet());
    }

    /**
     * Returns the Namespace URI for the given prefix.
     * @param prefix  prefix string for which to look up URI.
     * @return  URI string.
     */
    public static String getNamespaceByPrefix (String prefix) {
        return nsUriByPrefix.get(prefix);
    }

    /**
     * Returns an XPath query using the provided profile tag, extended base UML
     * type, and the ID of the instance of that type to match.  The produced
     * XPath string will have the form:<pre>
     *     &lt;tag's prefix&gt;:&lt;tag&gt;[@base_&lt;UML-type-extended&gt;='&lt;id&gt;']
     * </pre>
     * @param tag  the XML tag of the profile element to query for
     * @param baseExtended  the {@link UMLLabel} for the UML type extended by stereotype
     * @param id   the ID string to match for
     * @return  an XPath query string that can be supplied to {@link XPath#evaluate(String, Object, javax.xml.namespace.QName)}.
     */
    public static String path2ExtendedBaseOfId (ProfileLabel tag, UMLLabel baseExtended, String id) {
        return new StringBuilder("../" + inst().prefixed(tag))
                .append("[@base_")
                .append(UMLIdentifiers.inst().lit(baseExtended))
                .append("='")
                .append(id)
                .append("']").toString();
    }

    /**
     * Initializes the singleton object of this class, setting the singleton
     * to the latest version of namespace identifiers supported.
     */
    synchronized private static void init () {
        if (singleton != null) return;  // don't repeat init

        prestineNsUriByPrefix = new TreeMap<String,String>();
        nsUriByPrefix         = new TreeMap<String,String>();

        // find, but don't instantiate annotated profile identifer subclass(es)
        AbstractXMLIdentifiers.createSupportedSingletons(ProfileIdentifiers.class, prestineNsUriByPrefix, null);

        // instantiate class singleton
        singleton = new ProfileIdentifiers();

        if (Util.isInfoLevel()) Util.info("Supported Profile namespace prefixes "
                + Arrays.toString(prestineNsUriByPrefix.keySet().toArray()));
    }


    /**
     * Default, protected constructor, populates the mapper of identifier label
     * to string literal.
     */
    protected ProfileIdentifiers () {
        super();

        for (ProfileLabel l : ProfileLabel.values()) {
            literalMap.put(l, l.defaultLiteral());
        }
        // store namespace string using the VersionSupport annotation, if any
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null && verAnnote.prefix().length() > 0) {
            literalMap.put(ProfileLabel.valueOf(verAnnote.prefix()), verAnnote.name());
        }
    }


    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers#getPrefix()
     */
    @Override
    public String getPrefix () {
        throw new UnsupportedOperationException("ProfileIdentifiers#getPrefix() is not defined, as prefix can be queried from ProfileLabel!");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers#setPrefix(java.lang.String)
     */
    @Override
    public void setPrefix (String pref) {
        throw new UnsupportedOperationException("ProfileIdentifiers#setPrefix(String) is not defined, as prefix can be queried from ProfileLabel!");
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers#prefixed(java.lang.Object)
     */
    @Override
    public String prefixed (ProfileLabel l) {
        return Util.prefixed(lit(l.prefix()), lit(l));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.input.AbstractXMLIdentifiers#prefixed(java.lang.String)
     */
    @Override
    public String prefixed (String name) {
        return prefixed(ProfileLabel.valueOf(name));
    }

    /**
     * Adds a UML XMI reader's Namespace URI prefix to the known profile set.
     *
     * @param reader  UML XMI Reader to "register".
     */
    public void addReader (IReader reader) {
        String[] nsUriPrefixes = reader.getProfileNsUriPrefixes();

        Pattern regex = Pattern.compile(NS_PREFIX_PLACEHOLDER);

        // create new set of all Profile Namespace URI with Reader-specific URIs
        for (String prefix : prestineNsUriByPrefix.keySet()) {
            String uri = prestineNsUriByPrefix.get(prefix);
            String newUri = regex.matcher(uri).replaceAll(nsUriPrefixes[0]);
            if (!newUri.equals(uri)) {
                nsUriByPrefix.put(prefix, newUri);
            }
        }

        if (Util.isInfoLevel()) Util.info("Added reader yielded Profile namespaces URIs "
                + Arrays.toString(nsUriByPrefix.values().toArray()));
    }

    /**
     * Adds profiles from the given Model Document to the known profile set.
     *
     * @param reader  The UML XMI Reader parsing the Document
     * @param doc     Document to add
     */
    public void addModel (IReader reader, Document doc) {
        // determine all custom profiles based on Namespace URI prefix
        String[] nsUriPrefixes = reader.getProfileNsUriPrefixes();

        for (Map.Entry<String,String> entry : XMIIdentifiers.inst().getProfileNamespaceMap(doc.getDocumentElement()).entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();
            for (String readerPrefix : nsUriPrefixes) {
                if (uri.startsWith(readerPrefix)) {
                    // Namespace URI Prefix is a custom profile
                    nsUriByPrefix.put(prefix, uri);
                }
            }
        }

        if (Util.isInfoLevel()) Util.info("Added model yielded Profile namespace prefixes "
                + Arrays.toString(nsUriByPrefix.keySet().toArray()));
    }

    /**
     * Profile implementation to parse/load all unknown stereotypes of
     * given profile.
     */
    public NodeList model_getAllCustomStereotypes (Node n, String profile) throws XPathExpressionException {
        // Retrieve all stereotypes for the model node
        return (NodeList) UMLElement.xpath.evaluate(
                "../" + profile + ":*",
                n, XPathConstants.NODESET);
    }

    /**
     * Profile implementation to parse/load all predefined stereotypes.
     */
    public NodeList model_getAllStereotypes (Node n, ProfileLabel tag) throws XPathExpressionException {
        // Retrieve all stereotypes for the model node
        return (NodeList) UMLElement.xpath.evaluate(
                "../" + inst().prefixed(tag),
                n, XPathConstants.NODESET);
    }

    /**
     * Profile implementation to get base_Package id.
     */
    public String stereotype_getExtensionBaseId (ProfileLabel tagLabel, Node n) throws XPathExpressionException {
        // Query base_<tagLabel> id of given [stereotype extension] node.
        return (String) UMLElement.xpath.evaluate(
                "@" + lit(tagLabel),
                n, XPathConstants.STRING);
    }

    /**
     * Profile implementation to get the stereotype name.
     * 
     * @param n  XML Node
     * @return   Name of stereotype
     */
    public String stereotype_getName (Node n) {
        // Obtain name sans namespace
        return n.getLocalName();
    }

    /**
     * Profile implementation to get friends list tag attribute.
     */
    public String scaProfile_getFriendsList (Node n) throws XPathExpressionException {
        // Query friendsList tag attribute of given stereotype node.
        return (String) UMLElement.xpath.evaluate(
                "@" + lit(ProfileLabel.TAGATTR_SCA_FRIENDSLIST),
                n, XPathConstants.STRING);
    }

}
