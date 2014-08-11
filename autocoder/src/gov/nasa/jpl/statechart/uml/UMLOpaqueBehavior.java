package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.model.ModelScape;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Node;

public class UMLOpaqueBehavior extends UMLBehavior implements OpaqueBehavior {

    private Map<String,String> specBodyLangMap = new LinkedHashMap<String,String>();

    public UMLOpaqueBehavior (Node element, ModelScape scape) {
        super(element, scape);

        specBodyLangMap.putAll(UMLIdentifiers.inst().behavior_getSpec(this));
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.OpaqueBehavior#isReadOnly()
     */
    public boolean isReadOnly () {
        return false;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.OpaqueBehavior#body()
     */
    public String[] body () {
        return specBodyLangMap.keySet().toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.OpaqueBehavior#language()
     */
    public String[] language () {
        return specBodyLangMap.values().toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.statechart.uml.OpaqueBehavior#getSpecificationMap()
     */
    public Map<String,String> getSpecificationMap () {
        return specBodyLangMap;
    }

}
