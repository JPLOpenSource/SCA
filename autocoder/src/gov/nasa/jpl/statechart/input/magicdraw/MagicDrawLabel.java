package gov.nasa.jpl.statechart.input.magicdraw;

/**
 * This Enum represents known MagicDraw identifiers used in its XML documents.
 * These enums are used as keys to map to possibly version-specific MagicDraw
 * UML identifiers. 
 */
public enum MagicDrawLabel {
    EXPORTER { public String defaultLiteral () { return MagicDrawIdentifiers.EXPORTER; } },
    EXPORTER_VERSION { public String defaultLiteral () { return "UNKNOWN"; } },
    TAG_MODEL_EXTENSION { public String defaultLiteral () { return "modelExtension"; } },
    TAG_OWNED_DIAGRAMS { public String defaultLiteral () { return "mdOwnedDiagrams"; } },
    TAG_OWNED_VIEWS { public String defaultLiteral () { return "mdOwnedViews"; } },
    TAG_ELEMENT { public String defaultLiteral () { return "mdElement"; } },
    TAG_ELEMENT_ID { public String defaultLiteral () { return "elementID"; } },
    TAG_PROPERTY_ID { public String defaultLiteral () { return "propertyID"; } },
    TAG_ZOOM_FACTOR { public String defaultLiteral () { return "zoomFactor"; } },
    TAG_DIAGRAM_WINDOW_BOUNDS { public String defaultLiteral () { return "diagramWindowBounds"; } },
    TAG_GEOMETRY { public String defaultLiteral () { return "geometry"; } },
    TAG_EDGE { public String defaultLiteral () { return "edge"; } },
    TAG_PROPERTIES { public String defaultLiteral () { return "properties"; } },
    TAG_TEXT { public String defaultLiteral () { return "text"; } },
    TAG_VALUE { public String defaultLiteral () { return "value"; } },
    TAG_FILE_PART { public String defaultLiteral () { return "filePart"; } },
    MD_DIAGRAM { public String defaultLiteral () { return "Diagram"; } },
    MD_DIAGRAM_FRAME { public String defaultLiteral () { return "DiagramFrame"; } },
    MD_PSEUDOSTATE { public String defaultLiteral () { return "PseudoState"; } },
    MD_TRANSITION { public String defaultLiteral () { return "Transition"; } },
    MD_TRANSITION_TO_SELF { public String defaultLiteral () { return "TransitionToSelf"; } },
    MD_DECISION { public String defaultLiteral () { return "Decision"; } },
    MD_SPLIT { public String defaultLiteral () { return "Split"; } },
    MD_TEXTBOX { public String defaultLiteral () { return "TextBox"; } },
    MD_COLOR_PROPERTY { public String defaultLiteral () { return "ColorProperty"; } },
    KEY_ELEMENT_CLASS { public String defaultLiteral () { return "elementClass"; } },
    KEY_DIAGRAM_OWNER { public String defaultLiteral () { return "ownerOfDiagram"; } },
    KEY_HREF { public String defaultLiteral () { return "href"; } },
    VALUE_FILL_COLOR { public String defaultLiteral () { return "FILL_COLOR"; } }
    ;

    public abstract String defaultLiteral ();
}
