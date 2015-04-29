/**
 * Created Apr 6, 2011.
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.identifiers.ProfileIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A package is used to group elements, and provides a namespace for the
 * grouped elements.
 * <p>
 * Copyright &copy; 2009-2011 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 */
public class UMLPackage extends UMLNamespace implements Package {

    private Collection<PackageableElement> packagedElements = new LinkedHashSet<PackageableElement>();
    private Collection<Package> nestedPackages = new LinkedHashSet<Package>();
    private Collection<Stereotype> ownedStereotype = new LinkedHashSet<Stereotype>();

    private Package nestingPackage = null;

//  private Collection<Type> ownedTypes;  // currently unsupported
//  private Collection<Package> packageMerges;

    // UML extension: C++ ANSI profile, package name from stereotype tag value
    private String pkgName = null;


    /**
     * Main constructor to instantiate a UMLPackage instance using the
     * XMI Node element contained in the supplied {@link ModelScape}.
     * 
     * @param element
     */
    public UMLPackage (Node element, ModelScape scape) {
        super(element, scape);

        try {
            NodeList packages = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_PACKAGE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < packages.getLength(); i++) {
                Node pkgNode = packages.item(i);
                if (modelScape.shouldLoadPath(pkgNode)) {
                    UMLPackage pkg = new UMLPackage(pkgNode, scape);
                    ownedMember.add(pkg);
                    packagedElements.add(pkg);
                    nestedPackages.add(pkg);

                    // make this the parent Package of the just-instantiated nested Package
                    pkg.setNestingPackage(this);
                }
            }

            // fetch any classes, if they contain statemachines (shouldLoadPath)
            NodeList classes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_CLASS),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < classes.getLength(); i++) {
                Node clsNode = classes.item(i);
                if (modelScape.shouldLoadPath(clsNode)) {
                    Class c = new UMLClass(clsNode, scape);
                    ownedMember.add(c);
                }
            }

            // fetch any owned enumerations
            NodeList enums = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_ENUMERATION),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < enums.getLength(); i++) {
                Enumeration enumeration = new UMLEnumeration(enums.item(i), scape);
                ownedMember.add(enumeration);
            }

            // fetch any owned primitive type, which hasn't its own UML Class
            NodeList primTypes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_PRIMITIVE_TYPE),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < primTypes.getLength(); i++) {
                DataType primType = new UMLDataType(primTypes.item(i), scape);
                ownedMember.add(primType);
            }

            // fetch any instance specifications
            NodeList instSpecNodes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_INSTANCE_SPECIFICATION),
                    element, XPathConstants.NODESET);
            for (int i = 0; i < instSpecNodes.getLength(); i++) {
                InstanceSpecification instSpec = new UMLInstanceSpecification(instSpecNodes.item(i), scape);
                packagedElements.add(instSpec);
            }

            // fetch any owned stereotype
            NodeList stereotypes = (NodeList) xpath.evaluate(
                    UMLIdentifiers.path2NodeOfType(UMLLabel.TAG_MEMBER_ELEMENT, UMLLabel.TYPE_STEREOTYPE),
                    element, XPathConstants.NODESET);
            Profile containingProfile = null;
            if (stereotypes.getLength() > 0) {
                // determine owning profile for stereotypes to be loaded
                if (this instanceof Profile) {
                    containingProfile = (Profile) this;
                } else {  // attempt to find owning profile in Model
                    // transform non-alphanumeric char to underscore for Profile names
                    String profName = getName().replaceAll("\\W", "_");
                    if (ProfileIdentifiers.getSupportedPrefixes().contains(profName)) {
                        containingProfile = scape.getCachedProfiles().get(profName);
                    }
                }
            }
            for (int i = 0; i < stereotypes.getLength(); i++) {
                UMLStereotype stereotype = new UMLStereotype(stereotypes.item(i), scape);
                ownedStereotype.add(stereotype);
                // set containing profile of the stereotype
                stereotype.setContainingProfile(containingProfile);
                // add to map of known stereotypes
                Util.debug("Stereotype instantiated: " + stereotype.getName());
                UMLElement.stereotypeByName.put(stereotype.getName(), stereotype);
            }

            // Get all "C++Namespace" stereotypes for this package
            if (modelScape.getCachedCppNsStereotypes().get(id) != null) {
                pkgName = getName();
                if (Util.isDebugLevel()) Util.debug("- Got namespace: " + pkgName);
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLPackage constructor: ");
        }

        // Visibility is required for PackageableElement, so default to PRIVATE.
        if (visibility == null) {
            visibility = VisibilityKind.PRIVATE;
        }

        // Fetch the owned members that are PackageableElements
        packagedElements.addAll(Util.filter(ownedMember, PackageableElement.class));
        // Fetch packaged elements that are Packages
        nestedPackages.addAll(Util.filter(packagedElements, Package.class));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.PackageableElement#visibility()
     */
    public VisibilityKind visibility () {
        return visibility;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Package#packagedElements()
     */
    public Collection<PackageableElement> packagedElements () {
        return packagedElements;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Package#nestedPackages()
     */
    public Collection<Package> nestedPackages () {
        return nestedPackages;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Package#nestingPackage()
     */
    public Package nestingPackage () {
        return nestingPackage;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Package#getCppNamespace()
     */
    public String getPackageName () {
        return pkgName;
    }

    public void setNestingPackage (Package myNestingPkg) {
        nestingPackage = myNestingPkg;
    }

}
