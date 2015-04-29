/**
 * Relocated Sep 29, 2009.
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
package gov.nasa.jpl.statechart;

import java.util.Map;

/**
 * Implementation class of Map.{@link Entry} to enable instantiating a map
 * entry of key-value pair in model-query classes.  It is defined based on the
 * generic pair class.
 * <p>
 * Copyright &copy; 2009-2010 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 *
 * @param <K> Object type of entry key.
 * @param <V> Object type of entry value.
 * @see Pair
 */
public class Entry<K,V> extends Pair<K,V> implements Map.Entry<K,V> {

    public Entry (K key, V value) {
        super(key, value);
    }

    public Entry (Map.Entry<? extends K, ? extends V> entry) {
        super(entry.getKey(), entry.getValue());
    }

    public K getKey () {
        return super.getFirst();
    }

    public V getValue () {
        return super.getSecond();
    }

    public V setValue (V value) {
        throw new UnsupportedOperationException();
    }

}
