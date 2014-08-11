package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * An event action is an action that results in the generation of a new event
 * (signal).
 * </p>
 * 
 * <p>
 * Copyright 2005, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * 
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: Action.java,v 1.1 2008/01/31 01:28:43 lscharen Exp $
 * </p>
 * 
 * @see Action
 * @see State
 * @see Transition
 */
public class EventAction extends Action {

    public EventAction(String name) {
        super(name);
    }

    // had to add equals and hash if this is put into a Set
    public boolean equals(Object obj) {
        if (obj instanceof EventAction) {
            EventAction that = (EventAction) obj;
            return name.equals(that.name);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
