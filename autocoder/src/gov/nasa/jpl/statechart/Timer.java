/**
 * Created Oct 05, 2009.
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

/**
 * A simple utility, millisecond-scale timer for making elapsed time.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class Timer {
    private long initTime = 0;
    private long curTime = 0;

    /**
     * Default constructor.
     */
    public Timer () {
        initTime = System.currentTimeMillis();
        curTime = initTime;
    }

    public long markTime () {
        return markTime("");
    }

    /**
     * Marks the new time, returns the elapsed duration, and outputs the given
     * message along with the elapsed time.
     * @param msg  message string to display
     * @return  duration elapsed since the last mark time, in milliseconds
     */
    public long markTime (String msg) {
        long newTime = System.currentTimeMillis();
        long elapsed = 0L;
        if (curTime > 0) {
            elapsed = newTime - curTime;
        }
        curTime = newTime;

        if (Util.isInfoLevel()) {
            long ttlTime = newTime - initTime;
            Util.info(msg + ">>*" + elapsed + "ms (of " + ttlTime + ") elapsed*<<");
        }

        return elapsed;
    }

}
