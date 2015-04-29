/**
 * Created Oct 8, 2009.
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
package gov.nasa.jpl.statechart.model.diagram;

import gov.nasa.jpl.statechart.Util;

import java.util.List;
import java.util.Map;

/**
 * Class to hold UML StateChart Diagram information.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class DiagramData {

    public Map<String,DiagramElement> elementMap = null;
    public Map<String,List<TextElement>> textElementMap = null;
    public Map<String,List<SeparatorElement>> separatorElementMap = null;
    
    /**
     * Main constructor.
     */
    public DiagramData () {
        elementMap = Util.newMap();
        textElementMap = Util.newMap();
        separatorElementMap = Util.newMap();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        final String NL = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();
        sb.append("Element map: [").append(NL);
        for (String id : elementMap.keySet()) {
            DiagramElement elem = elementMap.get(id);
            sb.append("  ").append(elem).append(NL);
        }
        sb.append("] Texts: [").append(NL);
        for (String id : textElementMap.keySet()) {
            sb.append("  Machine ").append(id).append(NL);
            for (TextElement elem : textElementMap.get(id)) {
                sb.append("    ").append(elem).append(NL);
            }
        }
        sb.append("] Separators: [").append(NL);
        for (String id : separatorElementMap.keySet()) {
            sb.append("  Machine ").append(id).append(NL);
            for (SeparatorElement elem : separatorElementMap.get(id)) {
                sb.append("    ").append(elem).append(NL);
            }
        }
        sb.append("]");

        return sb.toString();
    }

}
