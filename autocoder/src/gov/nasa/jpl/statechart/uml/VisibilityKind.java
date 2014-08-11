package gov.nasa.jpl.statechart.uml;

public enum VisibilityKind {
    PUBLIC {
        public String getName () {
            return "public";
        }
    },
    PRIVATE {
        public String getName () {
            return "private";
        }
    },
    PROTECTED {
        public String getName () {
            return "protected";
        }
    },
    PACKAGE {
        public String getName () {
            return "package";
        }
    },

    /**
     * Value to use when visibility attribute was not specified.
     */
    UNDEFINED {
        public String getName () {
            return "";
        }
    };

    public abstract String getName ();

    public static VisibilityKind valueOfWithDefault (String str, VisibilityKind def) {
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return def;
        } catch (NullPointerException npe) {
            return def;
        }
    }
}
