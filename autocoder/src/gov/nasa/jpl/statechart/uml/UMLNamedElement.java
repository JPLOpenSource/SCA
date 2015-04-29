/**
 * Created prior to July 13, 2009.
 * <p>
 * Copyright 2009-2010, by the California Institute of Technology. ALL RIGHTS
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

/**
 * A named element is an element in a UML model that may have a name.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p><p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLNamedElement extends UMLElement implements NamedElement, Comparable<NamedElement> {

    // UML standard: visibility specification for element
    protected VisibilityKind visibility = null;

    // Collection<Dependency> clientDependency = Util.newList();
    // private Namespace namespace = null;

    // NON-UML standard: cache of frequently access elements
    private String name = null;
    private String qualifiedName = null;
    private String type = null;


    public UMLNamedElement (Node element, ModelScape scape) {
        super(element, scape);

        if (element != null) {
            // set visibility of element, no default per se, as UML NamedElement may
            //   just not define visibility.  The child PackageableElement will
            //   enforce a PRIVATE visibility.
            visibility = VisibilityKind.valueOfWithDefault(getAttribute(element, UMLLabel.KEY_VISIBILITY), VisibilityKind.UNDEFINED);

            try {
                // Fetch type, if any
                Node typeNode = (Node) xpath.evaluate(
                        UMLIdentifiers.inst().lit(UMLLabel.TAG_TYPE),
                        element, XPathConstants.NODE);
    
                if (typeNode != null) {
                    type = UMLIdentifiers.inst().property_getTypeFromHref(typeNode);
                }
            } catch (XPathExpressionException e) {
                Util.reportException(e, "UMLNamedElement constructor: ");
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo (NamedElement ne) {
        return getName().compareTo(ne.getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return getQualifiedName();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#getName()
     */
    public String getName () {
        if (name == null) {
            name = getAttribute(domElement, ReaderNamespaceContext.nameAttr());
        }
        return name;
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#getQualifiedName()
     */
    public String getQualifiedName () {
        if (qualifiedName != null && qualifiedName.length() > 0) {
            // no need to rebuild qualified name
            return qualifiedName;
        }
        // length 0 means it may have been accessed before injection, so retry

        String name = getName();

        if (name == null || name.length() == 0) {
            name = "";
            // System.out.println( "Empty name -- returning ''" );
            // System.out.println( Util.attrsToString( domElement ));
            // return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Namespace ns : allNamespaces()) {
            String nsName = ns.getName();

            // If we do not continue then we can get double-separating tokens,
            // e.g. ::::
            if (nsName == null || nsName.length() == 0)
                ;
            // continue;

            sb.append(nsName).append(separator());
        }

        sb.append(name);
        qualifiedName = sb.toString();

        return qualifiedName;
    }

    /*
     * (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#getPackageNames()
     */
    public String[] getPackageNames () {
        String[] namespaces = modelScape.pkgChainOfNamedElement.get(this);

        if (namespaces != null) {  // found!
            return namespaces;
        }

        // Otherwise, search model
        List<String> namespaceList = Util.newList();
        boolean firstPkgFound = false;
        NamedElement parent = getParent();

        // We are looking for a chain of packages in the ancestor path, and
        //   gathering the C++Namespaces along the way, if any, terminating
        //   as soon as a non-package is encountered.
        while (parent != null) {
            if (parent instanceof Package) {
                if (!firstPkgFound) {
                    firstPkgFound = true;
                }
                String cppNs = ((Package )parent).getPackageName();
                if (cppNs != null) {  // add C++Namespace name
                    namespaceList.add(cppNs);
                }  // else, simply skip this package
            } else {  // non-package
                if (firstPkgFound) break;  // done with package chain
            }
            parent = parent.getParent();
        }

        Collections.reverse(namespaceList);  // reverse to outside-in ordering
        namespaces = namespaceList.toArray(new String[0]);
        modelScape.pkgChainOfNamedElement.put(this, namespaces);  // cache pkg chain
        return namespaces;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.UMLElement#getParent()
     */
    @Override
    public NamedElement getParent () {
        return getParentAs(NamedElement.class);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#allNamespaces()
     */
    public List<Namespace> allNamespaces () {
        if (getNamespace() == null) {
            return Collections.emptyList();
        }

        List<Namespace> ns = Util.newList();

        ns.addAll(getNamespace().allNamespaces());
        ns.add(getNamespace());

        return ns;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#getVisibility()
     */
    public VisibilityKind getVisibility () {
        return visibility;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#separator()
     */
    public String separator () {
        return "::";
    }


    /////////////////////////////
    // NON-UML Standard methods
    /////////////////////////////

    /**
     * Forces the name of this UML NamedElement to the given name, but only if
     * the name isn't already set (length > 0). This method is useful for
     * the name injection phase, so that we're not mucking with the Document
     * directly.
     * @param forcedName  new name to set element to
     */
    public void setName (String forcedName) {
        if (name == null || name.length() == 0) {
            name = forcedName;
        }
    }

    public Namespace getNamespace () {
        return getParentAs(Namespace.class);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.NamedElement#type()
     */
    public String type () {
        if (type == null) {
            String typeId = getAttribute(domElement, ReaderNamespaceContext.typeAttr());
            Element elem = xmi2uml(typeId);
            if (elem != null && elem instanceof NamedElement) {
                type = ((NamedElement) elem).getName();
            } else {
                type = typeId;
            }
        }
        return type;
    }

}
