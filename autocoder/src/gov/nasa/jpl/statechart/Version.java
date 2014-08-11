/**
 * Created Oct 2, 2009.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    public int major = 0;
    public int minor = 0;
    public int revision = 0;
    public String build = null;

    /* Eclipse-generated hashCode() from all class members.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + revision;
        result = prime * result + ((build == null) ? 0 : build.hashCode());
        return result;
    }

    /* Eclipse-generated equals() from all class members.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Version)) {
            return false;
        }
        Version other = (Version) obj;
        if (major != other.major) {
            return false;
        }
        if (minor != other.minor) {
            return false;
        }
        if (revision != other.revision) {
            return false;
        }
        if (build == null) {
            if (other.build != null) {
                return false;
            }
        } else if (!build.equals(other.build)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo (Version v) {
        int rv = 0;

        // first, compare by major number
        if (major > v.major) {
            rv = 1;
        } else if (major < v.major) {
            rv = -1;
        } else {  // major ==; compare by minor number
            if (minor > v.minor) {
                rv = 1;
            } else if (minor < v.minor) {
                rv = -1;
            } else {  // minor ==, compare by revision number
                if (revision > v.revision) {
                    rv = 1;
                } else if (revision < v.revision) {
                    rv = -1;
                } else {  // revision ==, now compare by build (good idea??)
                    if (build == null && v.build == null) {
                        rv = 0;
                    } else if (build == null && v.build != null) {
                        rv = -1;
                    } else if (build != null && v.build == null) {
                        rv = 1;
                    } else {  // both builds NOT null
                        assert build != null;
                        rv = build.compareTo(v.build);
                        if (rv > 0) {
                            rv = 1;
                        } else if (rv < 0) {
                            rv = -1;
                        }  // otherwise, rv == 0
                    }
                }
            }
        }

        return rv;
    }

    /**
     * Attempts to parse a version string "x.y.z-W", where z and W often
     * may not exist, and the delimiter between z and W may be other than
     * a dash...
     * @param verStr  string to parse
     * @return  Version object
     */
    public static Version parseVersion (String verStr) {
        Version ver = new Version();

        Pattern p = Pattern.compile("(\\d+)([\\.](\\d+))?([\\.](\\d+))?((\\W+(.+))|(.+))?");
        Matcher m = p.matcher(verStr);
        if (m.matches()) {
            /* Get from 8 match groups, as seen in the following sample run
             * Matcher group count: 9
             *   0: 2.5.6-build!
             *   1: 2               <-- major
             *   2: .5
             *   3: 5               <-- minor, may be null
             *   4: .6
             *   5: 6               <-- revision, may be null
             *   6: -build!         <-- build info if attached to revision #
             *   7: -build!
             *   8: build!          <-- build info, may be null
             */
//            System.out.println("Matcher group count: " + m.groupCount());
//            for (int g=0; g < m.groupCount(); ++g) {
//                System.out.println("  " + g + ": " + m.group(g));
//            }
            // attempt to get major version
            ver.major = Integer.parseInt(m.group(1));

            // attempt to get minor version
            if (m.group(3) != null) {  // we have minor version!
                ver.minor = Integer.parseInt(m.group(3));
            }

            // attempt to get revision number
            if (m.group(5) != null) {  // we have revision number
                ver.revision = Integer.parseInt(m.group(5));
            }

            // attempt to parse additional build data, maybe dashed, spaced, or other...
            if (m.group(8) != null) {  // we have a build info
                ver.build = m.group(8);
            } else if (m.group(6) != null) {
                ver.build = m.group(6);
            }
        }
        return ver;
    }

    public static void main (String [] args) {
        Version.parseVersion("2.5");
        Version.parseVersion("2.5build!");
        Version.parseVersion("2.5 x");
        Version.parseVersion("2.5.6");
        Version.parseVersion("2.5.6build!");
        Version.parseVersion("2.5.6-build!");
    }

}
