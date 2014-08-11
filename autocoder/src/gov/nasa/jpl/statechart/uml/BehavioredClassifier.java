/**
 * A classifier can have behavior specifications defined in its namespace. One
 * of these may specify the behavior of the classifier itself.
 */
package gov.nasa.jpl.statechart.uml;

import java.util.*;

public interface BehavioredClassifier extends Classifier {

    public Collection<Behavior> getOwnedBehavior ();
}
