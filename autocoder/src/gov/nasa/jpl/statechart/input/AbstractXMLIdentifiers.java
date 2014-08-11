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
package gov.nasa.jpl.statechart.input;

import gov.nasa.jpl.statechart.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * This class serves as the base class to keep track of standards-specific XML
 * document identifiers, for instance, XMI or UML.  Extends this class with
 * an enum of labels to map to string literals.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public abstract class AbstractXMLIdentifiers<E> {

    /**
     * Searches the inner classes of supplied class for ones that have the
     * VersionSupport annotation, then instantiates those, and return these in
     * the supplied maps.
     * 
     * @param clazz  the class whose inner classes to search
     * @param uriByVersionOrPrefix  map of version number (or prefix, if no
     *      version defined) to namespace URIs
     * @param instByUri  map of namespace URIs to instantiated singleton instances,
     *      or <code>null</code> if no instances should be created.
     * @return the latest version defined, or null if not creating instances.
     */
    @SuppressWarnings("unchecked")
    protected static <T> T createSupportedSingletons (Class<T> clazz,
            SortedMap<String,String> uriByVersionOrPrefix, Map<String,T> instByUri) {

        Set<Class<? extends T>> classes = Util.findSubclassesUnder(clazz, null, false);
        for (Class<?> c : clazz.getClasses()) {
            if (clazz.isAssignableFrom(c)) {
                classes.add((Class<? extends T>)c);
            }
        }
        for (Class<? extends T> c : classes) {
            VersionSupport verAnnote = c.getAnnotation(VersionSupport.class);
            if (verAnnote != null) {  // found annotation!
                // store to version mapper, or use prefix if version is empty!
                if (verAnnote.version().length() > 0) {
                    uriByVersionOrPrefix.put(verAnnote.version(), verAnnote.name());
                } else {
                    uriByVersionOrPrefix.put(verAnnote.prefix(), verAnnote.name());
                }

                if (instByUri != null) {
                    // instantiate class and store to namespace mapper
                    try {
                        T obj = c.newInstance();
                        instByUri.put(verAnnote.name(), obj);
                    } catch (InstantiationException e) {  // ignore
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {  // ignore
                        e.printStackTrace();
                    }
                }
            }
        }
        return (instByUri == null) ? null
                : instByUri.get(uriByVersionOrPrefix.get(uriByVersionOrPrefix.lastKey()));
    }

    /** Mapping of label to literals */
    protected final Map<E,String> literalMap;

    private String prefix = null;  // namespace prefix, e.g., "xmi" or "uml"

    /**
     * Default, protected constructor, to prevent outside instantiation.
     */
    protected AbstractXMLIdentifiers () {
        literalMap = new HashMap<E,String>();
    }

    public String version () {
        String ver = null;
        // store namespace string using the VersionSupport annotation, if any
        VersionSupport verAnnote = getClass().getAnnotation(VersionSupport.class);
        if (verAnnote != null) {
            ver = verAnnote.version();
        }
        return ver;
    }

    /**
     * Returns the string literal for this specific XML-based namespace (e.g.,
     * XMI or UML) version corresponding to the supplied label.
     * 
     * @param l  a Label enum
     * @return string literal mapped to <code>l</code>
     */
    public String lit (E l) {
        return literalMap.get(l);
    }

    public boolean hasIdentifier (String iden) {
        return literalMap.containsValue(iden);
    }
    
    /**
     * Returns a prefixed string literal corresponding to the label.
     * 
     * @param l  a Label enum
     * @return
     */
    public String prefixed (E l) {
        return prefixed(lit(l));
    }

    /**
     * Returns the name prefixed by the current namespace prefix.
     * 
     * @param name  the name to prefix
     * @return the namespace-prefixed string
     */
    public String prefixed (String name) {
        return Util.prefixed(prefix, name);
    }

    /**
     * Returns the namespace prefix.
     * @return the namespace prefix string
     */
    public String getPrefix () {
        return prefix;
    }

    /**
     * Sets the name prefix used in this particular namespace, e.g., ("xmi" for
     * XMI, or "uml" for UML).
     * 
     * @param pref  the string to serve as namespace prefix
     */
    public void setPrefix (String pref) {
        if (pref != null) {
            prefix = pref;
        }
    }

}
