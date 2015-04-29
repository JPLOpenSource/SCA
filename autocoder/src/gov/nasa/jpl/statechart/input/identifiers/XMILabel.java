package gov.nasa.jpl.statechart.input.identifiers;

/**
 * This Enum represents known XMI identifiers used in XML documents.
 * These enums are used as keys to map to possibly version-specific XMI
 * identifiers. 
 */
public enum XMILabel {
    XMI_NS { public String defaultLiteral () { return "http://schema.omg.org/spec/XMI/?.?"; } },

    TAG_XMI { public String defaultLiteral () { return "XMI"; } },
    TAG_DOCUMENTATION { public String defaultLiteral () { return "Documentation"; } },
    TAG_EXTENSION { public String defaultLiteral () { return "Extension"; } },
    // XMI 2.4.1 onward
    TAG_EXPORTER { public String defaultLiteral () { return "exporter"; } },
    // XMI 2.4.1 onward
    TAG_EXPORTER_VERSION { public String defaultLiteral () { return "exporterVersion"; } },
    // pre-XMI 2.4.1
    KEY_EXPORTER { public String defaultLiteral () { return "exporter"; } },
    // pre-XMI 2.4.1
    KEY_EXPORTER_VERSION { public String defaultLiteral () { return "exporterVersion"; } },
    KEY_EXTENSION { public String defaultLiteral () { return "extension"; } },
    KEY_ID { public String defaultLiteral () { return "id"; } },
    KEY_IDREF { public String defaultLiteral () { return "idref"; } },
    KEY_TYPE { public String defaultLiteral () { return "type"; } },
    KEY_VALUE { public String defaultLiteral () { return "value"; } },
    ;

    public abstract String defaultLiteral ();
}
