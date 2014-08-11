/**
 * Created Aug 4, 2009.
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
import gov.nasa.jpl.statechart.uml.OpaqueBehavior;
import gov.nasa.jpl.statechart.uml.Transition;
import gov.nasa.jpl.statechart.uml.UMLOpaqueBehavior;
import gov.nasa.jpl.statechart.uml.UMLValueSpecification;
import gov.nasa.jpl.statechart.uml.UMLVertex;
import gov.nasa.jpl.statechart.uml.ValueSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

/**
 * This class provides a "database" of UML namespace identifiers to shield
 * code from literal changes that result from namespace version changes.
 * Access the identifier mappings via the singleton instance {@link #inst()}.
 * Set the singleton object to the namespace version supported via
 * {@link #setNamespace(String)}.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class UMLIdentifiers extends AbstractXMLIdentifiers<UMLLabel> {
    /** Default namespace prefix for UML. */
    public static final String UMLNS_PREFIX_DEFAULT = "uml";

    /** Singleton instance of this identifier class. */
    private static UMLIdentifiers singleton = null;
    /** Sorted map of UML Namespace version to the namespace URI. */
    private static SortedMap<String,String> nsUriByVersion = null;
    /** Map of namespace URI to instance of this class. */
    private static Map<String,UMLIdentifiers> instByNsUri = null;

    /**
     * Returns the current singleton instance, corresponding to the latest
     * supported UML namespace version or the namespace set by {@link #setNamespace(String)}.
     * @return the UMLIdentifiers singleton instance.
     */
    public static UMLIdentifiers inst () {
        if (singleton == null) {  // lazy init
            init();
        }
        return singleton;
    }

    /**
     * Sets the UML namespace URI and affects the singleton instance returned.
     * Must be a supported namespace, which is defined by an inner class
     * extending this class and annotated with {@link VersionSupport}.
     * @param ns the UML namespace URI to set
     */
    public static void setNamespace (String ns) {
        if (instByNsUri == null) {  // lazy init
            init();
        }
        if (instByNsUri.containsKey(ns)) {
            singleton = instByNsUri.get(ns);
        } else {
            Util.error("** UML Namespace '" + ns + "' not supported!");
        }
    }

    /**
     * Returns a list of the UML Namespace URIs of the supported namespace versions.
     * @return list of URI strings
     */
    public static List<String> getSupportedNamespaces () {
        if (nsUriByVersion == null) {
            init();
        }
        return new ArrayList<String>(nsUriByVersion.values());
    }

    public static String path2NodeOfType (UMLLabel tag, UMLLabel type) {
        return new StringBuilder(inst().lit(tag))
                .append("[@")
                .append(XMIIdentifiers.type())
                .append("='")
                .append(inst().prefixed(type))
                .append("']").toString();
    }

    public static String path2NodeOfTypeAndId (UMLLabel tag, UMLLabel type, String id) {
        return new StringBuilder(inst().lit(tag))
                .append("[@")
                .append(XMIIdentifiers.type())
                .append("='")
                .append(inst().prefixed(type))
                .append("' and @")
                .append(XMIIdentifiers.id())
                .append("='")
                .append(id)
                .append("']")
                .toString();
    }

    public static String path2NodeOfAnyOfTypes (UMLLabel tag, UMLLabel... type) {
        StringBuilder sb = new StringBuilder(inst().lit(tag)).append("[");
        boolean firstType = true;
        for (UMLLabel l : type) {
            if (firstType) {
                firstType = false;
            } else {  // append attribute operator
                sb.append(" or ");
            }
            sb.append("@")
                .append(XMIIdentifiers.type())
                .append("='")
                .append(inst().prefixed(l))
                .append("'");
        }
        return sb.append("]").toString();
    }

    /**
     * Initializes the singleton object of this class, setting the singleton
     * to the latest version of namespace identifiers supported.
     */
    synchronized private static void init () {
        if (singleton != null) return;  // don't repeat init

        nsUriByVersion = new TreeMap<String,String>();
        instByNsUri = new HashMap<String,UMLIdentifiers>();
        // instantiate annotated inner class(es) and get last version as singleton
        singleton = AbstractXMLIdentifiers.createSupportedSingletons(UMLIdentifiers.class, nsUriByVersion, instByNsUri);

        if (Util.isInfoLevel()) Util.info("Supported UML namespace versions "
                + Arrays.toString(nsUriByVersion.keySet().toArray()));
    }


    /**
     * Default, protected constructor, populates the mapper of identifier label
     * to string literal.
     */
    protected UMLIdentifiers () {
        super();
        setPrefix(UMLNS_PREFIX_DEFAULT);
        for (UMLLabel l : UMLLabel.values()) {
            literalMap.put(l, l.defaultLiteral());
        }
        // store namespace string using the VersionSupport annotation, if any
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null) {
            literalMap.put(UMLLabel.UML_NS, verAnnote.name());
        }
    }


    /**
     * UML version-specific implementation to parse/load transition guard.
     */
    public abstract Node transition_getGuard (Node n)
    throws XPathExpressionException;

    /**
     * UML version-specific implementation to parse/load timing expression.
     */
    public abstract Node timeEvent_getTimingExpression (Node n)
    throws XPathExpressionException;

    /**
     * UML version-specific implementation to parse/load a referenced signal ID.
     */
    public abstract String signalEvent_getReferencedSignalId (Node n);

    /**
     * UML version-specific implementation to obtain the specification body,
     * and corresponding language, of an {@link OpaqueBehavior}.
     */
    public abstract Map<String,String> behavior_getSpec (UMLOpaqueBehavior beh);

    /**
     * UML version-specific implementation to obtain the expression body of
     * a {@link ValueSpecification}.
     */
    public abstract String value_getExprBody (UMLValueSpecification spec);

    /**
     * UML version-specific implementation to find incoming transitions.
     */
    public abstract Collection<Transition> vertex_getIncomingTransitions (UMLVertex v);

    /**
     * UML version-specific implementation to find outgoing transitions.
     */
    public abstract Collection<Transition> vertex_getOutgoingTransitions (UMLVertex v);

    /**
     * UML version-specific implementation to determine Primitive Type.
     * @param n
     * @return
     */
    public abstract String property_getTypeFromHref (Node n);

}
