/**
 * Element is an abstract metaclass with no superclass. It is used as the common
 * superclass for all metaclasses in the infrastructure library. Element has a
 * derived composition association to itself to support the general capability
 * for elements to own other elements.
 */
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.input.ReaderNamespaceContext;
import gov.nasa.jpl.statechart.input.identifiers.ProfileIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.identifiers.XMIIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;
import gov.nasa.jpl.statechart.model.UMLModelGroup;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class UMLElement implements Element {

    /** Create a static XPath object for finding various elements. */
    public static final XPath xpath;

    /**
     * Keep a hash of xmi:id to UML elements per Model to resolve UID clash.
     * This cannot be declared as member of UMLModel that would cause circular
     * dependencies at construction time, when UMLElement.&lt;init&gt; needs to
     * access xmi2uml in UMLModel before UMLModel is completely constructed. 
     */
    private static final Map<UMLModel,Map<String,Element>> xmi2umlByModel = Util.newMap();

    // Cache last UMLModel to know what model we're processing
    private static UMLModel lastModel = null;

    /**
     * Map of Stereotype by stereotype name across all models, populated by
     * UMLPackage as Stereotypes are instantiated.
     */
    public static final Map<String,Stereotype> stereotypeByName = Util.newMap();


    // This is a static code block that initializes the xpath object. The
    // biggest issue is that the XMI documents use XML namespaces, so we
    // have to create a NamespaceContext for the xpath object in order to
    // properly resolve queries
    static {
        // Set up the XPath context
        NamespaceContext ctx = new ReaderNamespaceContext();

        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(ctx);
    }

    // safe-access setter/getter for static class member lastModel
    private static synchronized void setLastModel (UMLModel newModel) {
        lastModel = newModel;
    }
    private static synchronized UMLModel getLastModel () {
        return lastModel;
    }

    // Autocoder enhancements
    //
    // Keep a reference to the underlying DOM element, used by subclasses
    protected Node domElement = null;
    // Keep a cache of the ModelScape information for constructing our IR.
    protected ModelScape modelScape = null;
    // Cache the UML element ID, used by subclasses
    protected String id = null;

    // Collection<UMLComment> ownedComment = Util.newList();
    private Collection<Element> ownedElement = Util.newList();
    // TODO: UMLElement.owner unused!
    //private Element owner = null;

    /**
     * Sole constructor of UMLElement, taking in the XMI Node as argument.
     * <p>
     * This constructor causes structural changes to the static xmi2umlByModel
     * member.  This is safe from concurrent access since this constructor is
     * the only location making such a modification, and the application has
     * a single-thread design.
     * </p>
     * @param element  The XMI Node element that specifies this UML element.
     */
    public UMLElement (Node element, ModelScape scape) {
        domElement = element;
        id = getAttribute(element, XMIIdentifiers.id());
        modelScape = scape;

        UMLModel model = null;
        if (this instanceof UMLModel) {  // I _am_ the model
            model = (UMLModel) this;
            // create an XMI-to-UML map for this model
            Map<String,Element> xmi2uml = Util.newMap();
            xmi2umlByModel.put(model, xmi2uml);
            setLastModel(model);  // keep as the current Model ("last-seen")
            // add a null element to the xmi2uml mapping in order to pass nulls cleanly
            xmi2uml.put(null, null);
        } else {
            model = getLastModel();
        }

        if (id.length() != 0) {
            // Create mapping of this element's XMI UID to this element
            if (!xmi2umlByModel.get(model).containsKey(id)) {
                // It is possible that the same element may be instantiated by
                // multiple UML implementation classes; we keep the first one
                xmi2umlByModel.get(model).put(id, this);
            }
        }

        // map this UMLElement (incl. UMLModel itself) to its model
        UMLModelGroup.mapElement2Model(this, model);

        if (this instanceof StateMachine) {
            // DO NOT use getName; it'll cause recursion of construction
            System.out.println("Processing State Machine: " + getAttribute(element, ReaderNamespaceContext.nameAttr()));
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "UMLElement ID " + id;
    }

    /**
     * Returns the UML element corresponding to the supplied XMI ID in the scope
     * of the current UML model, where "current" is determined by the element
     * requesting the XMI-to-UML mapping.
     * 
     * @param uid  XMI UID to look up element of
     * @return the UML element corresponding to <code>uid</code> in the model.
     */
    public Element xmi2uml (String uid) {
        // find the containing model of this element
        UMLModel model = UMLModelGroup.element2Model(this);
        // then lookup UID in the xmi-to-uml mapping applicable to this model,
        // and return the element
        return xmi2umlByModel.get(model).get(uid);
    }

    /**
     * Returns the UML element corresponding to the supplied remote reference
     * XMI ID, searching all UML models we know of.
     * @param href
     * @return
     */
    public Element href2uml (String href) {
        Element e = null;

        if (href != null) {
            // chop href string into file name and ID
            int at = href.indexOf("#");
            if (at > -1) {
                String filename = href.substring(0, at);
                String uid = href.substring(at+1);
    
                Model m = UMLModelGroup.findModelOfName(filename);
                if (m != null) {
                    // search for UML element within this model!
                    e = xmi2umlByModel.get(m).get(uid);
                }
            }
        }

        return e;
    }

    public String id () {
        return id;
    }

    /**
     * Get the underlying DOM node for this element
     */
    public Node getNode () {
        return domElement;
    }

    /**
     * The query allOwnedElements() gives all of the direct and indirect
     * owned elements of an element. 
     */
    public Set<Element> allOwnedElements () {
        Set<Element> allElements = Util.newSet();

        for (Element e : ownedElement) {
            allElements.addAll(e.allOwnedElements());
        }

        return allElements;
    }

    /**
     * The query mustBeOwned() indicates whether elements of this type must
     * have an owner. Subclasses of Element that do not require an owner
     * must override this operation.  
     */
    public boolean mustBeOwned () {
        return true;
    }

    public Node getDOMElement () {
        return domElement;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.Element#getParent()
     */
    public Element getParent () {
        return getParentAs(Element.class);
    }


    // Cache of the sorted list of Stereotypes applied
    Set<Stereotype> cachedAppliedStereotypes = null;

    public Collection<Stereotype> getAppliedStereotypes () {

        if (cachedAppliedStereotypes == null) {
            cachedAppliedStereotypes = Util.newSortedSet();

            Map<String,Collection<Node>> stereotypeMap = modelScape.getCachedAppliedStereotypes();

            if (stereotypeMap.containsKey(id)) {
                // get applicable set of stereotype names
                for (Node n : stereotypeMap.get(id)) {
                    String stereoName = ProfileIdentifiers.inst().stereotype_getName(n);
                    Stereotype stereotype = stereotypeByName.get(stereoName);
                    cachedAppliedStereotypes.add(stereotype);

                    if (Util.isDebugLevel()) {
                        Util.debug("@@Element " + toString() + " stereotyped by " + stereotype.toString());
                    }
                }
            }
        }

        return cachedAppliedStereotypes;
    }

    // Cache of tag-value map by Stereotype Name
    Map<String,Map<String,Object>> cachedStereotypeTagValueMap = null;

    public Map<String,Object> getStereotypeTagValueMap (String stereoName) {
        if (cachedStereotypeTagValueMap == null) {
            cachedStereotypeTagValueMap = Util.newMap();
        }
        if (!cachedStereotypeTagValueMap.containsKey(stereoName)) {
            Map<String,Collection<Node>> stereotypeMap = modelScape.getCachedAppliedStereotypes();

            if (stereotypeMap.containsKey(id)) {
                // get applicable set of stereotype names
                for (Node n : stereotypeMap.get(id)) {
                    String appliedStereoName = ProfileIdentifiers.inst().stereotype_getName(n);
                    if (appliedStereoName.equals(stereoName)) {
                        Stereotype stereo = stereotypeByName.get(stereoName);
                        // store the overridden tag values into cache map
                        Map<String,Object> tagValueMap = Util.newMap();
                        cachedStereotypeTagValueMap.put(stereoName, tagValueMap);
                        // iterate through the attributes, filtering out non-attrs
                        NamedNodeMap attributes = n.getAttributes();
                        for (int i=0 ; i < attributes.getLength() ; ++i) {
                            Node attr = attributes.item(i);
                            // check if attribute is an override tag, i.e.,
                            // Stereotype defines it as a field
                            for (Property p : stereo.getAttributesMap().values()) {
                                if (p.getName().equals(attr.getNodeName())) {
                                    tagValueMap.put(attr.getNodeName(), attr.getNodeValue());
                                    if (Util.isDebugLevel()) {
                                        Util.debug("++ For stereotype " + stereoName + " applied to " + id + ": "
                                                + attr.getNodeName() + " => " + attr.getNodeValue());
                                    }
                                    break;
                                }
                            }
                        }
                        // iterate through stereotype and add default attrs
                        for (Property attr : stereo.getAttributesMap().values()) {
                            if (! tagValueMap.containsKey(attr.getName())) {
                                // not in tag-value map yet, has default val?
                                if (attr.getDefaultValue() != null) {
                                    // tag attribute has default value; add it
                                    tagValueMap.put(attr.getName(), attr.getDefaultValue());
                                    if (Util.isDebugLevel()) {
                                        Util.debug("++ For stereotype " + stereoName + " applied to " + id + ", default: "
                                                + attr.getName() + " => " + attr.getDefaultValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return cachedStereotypeTagValueMap.get(stereoName);
    }


////////////////////////////////////////////
// Utility methods for the class hierarchy
////////////////////////////////////////////

    /**
     * Returns the specified attribute of the given node, "" if no such attribute.
     *
     * @param node  the node in which to find attribute
     * @param attr  the name of the attribute to get
     * @return  the attribute value
     */
    protected String getAttribute (Node node, String attr) {
        String attribute = Util.getNodeAttribute(node, attr);
        return (attribute == null) ? "" : attribute;
    }

    /**
     * Convenience-method that returns, looking at the given node, the value of
     * the attribute identified by the specified {@link UMLLabel}.
     * @param node  the node in which to find attribute
     * @param l     the {@linkplain UMLLabel} identifying the attribute name
     * @return  the attribute value
     */
    protected String getAttribute (Node node, UMLLabel l) {
        return getAttribute(node, UMLIdentifiers.inst().lit(l));
    }

    /**
     * Utility method to get the owner of an element and try casting it
     * to a particular type.  Null is returned if the element is not of
     * the correct type.  The xmi:id is used to look it up.
     */
    protected <T> T getParentAs (java.lang.Class<T> clazz) {
        T parent = null;

        try {
            // Lookup the parent node ID and check if it implements the
            // class type
            String idref = xpath.evaluate("../@" + XMIIdentifiers.id(), domElement);
            if (idref != null) {
                parent = clazz.cast(xmi2umlByModel.get(UMLModelGroup.element2Model(this)).get(idref));
            }
        } catch (XPathExpressionException e) {
            Util.reportException(e, "UMLElement.getParentAs(): ");
        } catch (ClassCastException e) {
            // Non-error, we just return null anyway...
        }

        return parent;
    }

    /**
     * Returns the UML Behavior object (subtype thereof) for the child node of
     * the given tag label, presently either an Activity or OpaqueBehavior.
     * @param l  the desired UML tag label to search among children nodes
     * @param node  the node to start from
     * @return  object of {@link Behavior} subtype
     * @throws XPathExpressionException
     */
    protected Behavior getBehaviorSubtype (UMLLabel l, Node node) throws XPathExpressionException {
        Behavior beh = null;

        // get either an Activity or OpaqueBehavior node
        Node behaviorNode = (Node) xpath.evaluate(
                UMLIdentifiers.path2NodeOfAnyOfTypes(l, UMLLabel.TYPE_ACTIVITY,
                        UMLLabel.TYPE_BEH_OPAQUE,
                        UMLLabel.TYPE_BEH_FUNCTION),
                node, XPathConstants.NODE);
        if (behaviorNode != null) {
            String behNodeType = getAttribute(behaviorNode, XMIIdentifiers.type());
            if (behNodeType != null) {
                if (behNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_BEH_OPAQUE))) {
                    // create an opaque behavior
                    beh = new UMLOpaqueBehavior(behaviorNode, modelScape);
                } else if (behNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_BEH_FUNCTION))) {
                    // create a function behavior
                    beh = new UMLFunctionBehavior(behaviorNode, modelScape);
                } else {  // create an Activity by default
                    beh = new UMLActivity(behaviorNode, modelScape);
                }
            }
        }

        return beh;
    }


    /**
     * Given the UMLLabel, return an XPath query string to find any sub-node
     * containing the XML for a ValueSpecification subtype.
     * <br/><br/>
     * N.B. The reason a node query isn't directly coded is that, in most cases,
     * a single Node is desired, but in at least one case, a NodeSet is needed,
     * so the XPath query string is a more generic re-usable chunk.
     *
     * @param l  {@link UMLLabel} of the XML tag
     * @return   an XPath query string
     */
    protected String getValueSpecificationXpath (UMLLabel l) {
        return ".//"
                + UMLIdentifiers.path2NodeOfAnyOfTypes(l,
                    UMLLabel.TYPE_ELEMENT_VALUE, UMLLabel.TYPE_INSTANCE_VALUE,
                    UMLLabel.TYPE_EXPRESSION,
                    UMLLabel.TYPE_EXPR_OPAQUE, UMLLabel.TYPE_LITERAL_STRING,
                    UMLLabel.TYPE_LITERAL_BOOLEAN, UMLLabel.TYPE_LITERAL_REAL,
                    UMLLabel.TYPE_LITERAL_INTEGER, UMLLabel.TYPE_LITERAL_UNLIMITED);
    }

    /**
     * Returns the UML ValueSpecification object from the given Node, using
     * UML Type to return one of {@link InstanceValue}, {@link Expression},
     * {@link OpaqueExpression}, {@link LiteralString},
     * {@link LiteralReal}, {@link LiteralBoolean}, or 
     * {@link LiteralInteger} (which includes the LiteralUnlimitedNatural).
     * <br/><br/>
     * 
     * A {@link ValueSpecification} provides a value whose access depends on
     * the type of the value specification.
     * <br/><br/>
     * 
     * @param node  the node to query for a value
     * @return  object of {@link ValueSpecification} type
     * @throws XPathExpressionException
     */
    protected ValueSpecification getValueSpecificationSubtype (Node node) throws XPathExpressionException {
        ValueSpecification spec = null;

        String exprNodeType = getAttribute(node, XMIIdentifiers.type());

        if (exprNodeType != null) {
            if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_ELEMENT_VALUE))
                    || exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_INSTANCE_VALUE))) {
                // create expression referencing an ElementValue or InstanceValue
                spec = new UMLInstanceValue(node, modelScape);

            } else if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_EXPRESSION))) {
                // create an Expression, which recursively calls this function
                spec = new UMLExpression(node, modelScape);

            } else if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_EXPR_OPAQUE))) {
                // create an OpaqueExpression
                spec = new UMLOpaqueExpression(node, modelScape);

            } else if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_LITERAL_REAL))) {
                // create a LiteralReal
                spec = new UMLLiteralReal(node, modelScape);

            } else if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_LITERAL_BOOLEAN))) {
                // create a LiteralBoolean
                spec = new UMLLiteralBoolean(node, modelScape);

            } else if (exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_LITERAL_INTEGER))
                    || exprNodeType.equals(UMLIdentifiers.inst().prefixed(UMLLabel.TYPE_LITERAL_UNLIMITED))) {
                // create a LiteralInteger
                spec = new UMLLiteralInteger(node, modelScape);

            } else {  // create a LiteralString by default
                spec = new UMLLiteralString(node, modelScape);
            }
        }

        return spec;
    }

    /**
     * Sets a flag on the corresponding Model that a fatal exception occurred.
     * This allows processing to continue until the autocoding step, when
     * generation will terminate.
     */
    protected void flagFatalException () {
        UMLModelGroup.element2Model(this).setModelException(true);
    }

}
