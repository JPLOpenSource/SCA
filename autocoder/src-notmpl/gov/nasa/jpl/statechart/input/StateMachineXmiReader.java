package gov.nasa.jpl.statechart.input;

import gov.nasa.jpl.statechart.Autocoder;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.StateChartCodeWriter;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;
import gov.nasa.jpl.statechart.core.SubmachineState;
import gov.nasa.jpl.statechart.core.Transition;
import gov.nasa.jpl.statechart.input.identifiers.UMLIdentifiers;
import gov.nasa.jpl.statechart.input.magicdraw.MDUmlReaderHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * <p>
 * This abstract class provides generic methods for parsing descriptions of UML
 * state machines from XMI files. A subclass must be created which implements
 * the abstract methods, and which is specialized to parse a particular XMI
 * statechart representation (such as the XMI representation produced by the
 * MagicDraw UML tool).
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
 * This software is subject to U.S. export control laws and regulations and 
 * has been classified as 4D993.  By accepting this software, the user agrees 
 * to comply with all applicable U.S. export laws and regulations.  User has 
 * the responsibility to obtain export licenses, or other export authority as 
 * may be required before exporting such information to foreign countries or 
 * providing access to foreign persons.
 * </p>
 * 
 * <p>
 * CVS Identification: $Id: StateMachineXmiReader.java,v 1.1.2.1 2005/12/14
 * 00:30:17 kclark Exp $
 * </p>
 * 
 * @see StateMachine
 */

public abstract class StateMachineXmiReader
{
   public Node[] topLevelNodes = null;
   public final static HashMap<String, Node>  xmiNodes = new HashMap<String, Node>();

   protected HashMap<String, StateMachine> stateMachineMap = new HashMap<String, StateMachine>();
   protected List<Node>                    signalList      = new ArrayList<Node>();
   protected List<Node>                    stateMachineNodeList;
   protected List<Node>                    allStateMachineNodeList;
   protected String                        currentFilename;
   protected boolean                       debugOn         = false;
   protected boolean                       signalsOnly     = false;

   private final int  defaultSearchDepth       = 200;


/////////////////////
// Abstract Members
/////////////////////
   /**
    * Determines what version of reader we will be parsing.  This method
    * should be called as early as possible, as it sets the version-appropriate
    * helper reader class, which affects all of parsing behavior.
    * <p>
    * After calling this method, the reader should know which version of XML
    * it is reading, and not need to worry about re-checking for version support.
    * </p>
    * @param doc  the DOM Document object on which to determine version
    */
   protected abstract boolean determineReaderVersion (Document doc);

   /**
    * Populates the subclass variables with top level information for all state
    * machines defined in the current XMI file.
    * 
    * @param xmiTop
    *           Node The top of the XMI document to search.
    */
   protected abstract void findOtherModelInfo(Node xmiTop) throws Exception;

   /**
    * Parses through the node tree starting at the specified top level node, and
    * finds and adds all state and transition nodes to the specified state
    * machine object.
    * 
    * @param machine
    *           StateMachine The state machine we're building.
    * @param smTopNode
    *           Node Start search at and below this node for states,
    *           transitions, etc.
    * @throws Exception
    */
   protected abstract void buildStateMachine(StateMachine machine, Node smTopNode)
         throws Exception;

   protected abstract Set<gov.nasa.jpl.statechart.uml.StateMachine> getStateMachines( Document document );
   protected abstract Set<gov.nasa.jpl.statechart.uml.StateMachine> getSubmachines( gov.nasa.jpl.statechart.uml.StateMachine stateMachine );


   /**
    * Parse an XMI DOM model into a UML Model
    */
   protected abstract gov.nasa.jpl.statechart.uml.Model createUMLFromDOM( Document document );


   /**
    * Get a copy of the State Machine Node list
    */
   public List<Node> getStateMachineNodes()
   {
      return Collections.unmodifiableList( stateMachineNodeList );
   }

   public List<Node> getAllStateMachineNodes()
   {
      return Collections.unmodifiableList( allStateMachineNodeList );
   }

   /**
    * Turn debug messages on or off.
    * 
    * @param On
    *           boolean true for debug on, false for off.
    */
   public void setDebug(boolean On)
   {
      debugOn = On;
   }

   /**
    * Sets the instance of the UmlReader to only create lists of signals and
    * state machine names, but <b>not</b> to parse and build the contents of
    * the state machines. This method can be called when only interested in the
    * signals used by a set of state machines.
    * 
    */
   public void setSignalsOnly()
   {
      this.signalsOnly = true;
   }

   /**
    * Finds and returns the UML model definition node for the specified XMI
    * document.
    * 
    * @param xmiTop
    *           Node The top of the XMI document to search.
    * @return Node The found UML model top node.
    * @throws Exception
    *            If the UML model definition node is not found.
    */
   public Node modelTop(Node xmiTop) throws Exception
   {
      Node n = findNode("uml:model", xmiTop, 1);
      if (null == n)
         throw new Exception("File " + currentFilename
               + " does not contain a UML model - unable to parse!");
      return n;
   }

   /**
    * Populates the signalList class variable with all found nodes which define
    * <i>signals</i>. Signals cause state transitions to occur.
    * 
    * @param xmiTop
    *           Node The top of the XMI document to search.
    * @throws Exception
    */
   protected void findSignals(Node xmiTop) throws Exception
   {
      // Find the starting UML model node:
      Node modelTop = modelTop(xmiTop);
      // Find all signal nodes defined for the entire UML model:
      findNodes(signalList, "", "xmi:type", "uml:signal", modelTop, defaultSearchDepth);
   }

   /**
    * Populates the stateMachineNodeList class variable with all found nodes
    * which define <i>state machines</i>. State machines contain states,
    * transitions, etc.
    * 
    * @param xmiTop
    *           Node The top of the XMI document to search.
    * @throws Exception
    */
   protected void findStateMachines(Node xmiTop) throws Exception
   {
      // Find the starting UML model node:
      // 
      // LJS: Increased the search depth to 2 for prox-1 implementation.
      // This should obviously search the entire search tree, but don't
      // want to alter the code too much.
      //
      // [SWC 2009.09.16] increased search depth to 3 for Ares-I, complex model
      Node modelTop = modelTop(xmiTop);
      findNodes(stateMachineNodeList, "", "xmi:type", "uml:stateMachine",
            modelTop, 3);
   }

   /**
    * Returns a new StateMachine instance created from information contained in
    * the passed state machine definition node(s).
    * 
    * @param stateMachTop
    *           Node The top node in the state machine definition.
    * @return StateMachine The created state machine.
    * @throws Exception
    *            If unable to create a new StateMachine instance.
    */
   protected StateMachine createStateMachine(Node stateMachTop)
         throws Exception
   {
      if (null == stateMachTop.getAttributes().getNamedItem("xmi:id"))
         throw new Exception(
               "Found state machine definition node without an XMI:ID attribute");
      // Get the string ID of this state machine:
      String smId = stateMachTop.getAttributes().getNamedItem("xmi:id")
            .getNodeValue();
      if (null == stateMachTop.getAttributes().getNamedItem("name"))
         throw new Exception("Found unnamed state machine with ID=" + smId
               + " - state machine must be named!");
      // Create a new state machine object:
      return new StateMachine(stateMachTop.getAttributes().getNamedItem("name")
            .getNodeValue(), smId);
   }

   /**
    * Returns a HashMap containing the set of signal names and associated text
    * comments, as found in the input XML files.
    * 
    * @return HashMap Contains pairs of string signal names and text comments.
    */
   public HashMap<String, String> getSignalMap()
   {
      HashMap<String, String> signalMap = new HashMap<String, String>();
      Iterator signalIter = getSignalList().iterator();
      while (signalIter.hasNext())
      {
         Node sig = (Node) signalIter.next();
         if (null != sig.getAttributes().getNamedItem("name")
               &&
               null != sig.getAttributes().getNamedItem("name").getNodeValue())
         {
            String name = (String) sig.getAttributes().getNamedItem("name")
                  .getNodeValue().trim();
            String comment = findComment(sig);
            if (!Transition.isLocalSignalName(name))
            { // Filter out local events...
               signalMap.put(name, comment);
            }
         }
      }
      return signalMap;
   }

   /**
    * Returns the list of parsed state machines.
    * 
    * @return HashMap Keys are String state machine names, and values are
    *         StateMachine instances.
    */
   public HashMap<String, StateMachine> getStateMachineMap()
   {
      return stateMachineMap;
   }

   /**
    * Returns the list of signals compiled from all the parsed state machines.
    * 
    * @return List
    */
   public List<Node> getSignalList()
   {
      return signalList;
   }

   /**
    * Searches one level below the specified starting node, looking for the
    * first comment node.
    * 
    * @param n
    *           Node Top of tree to search.
    * @return String Comment string, or <B>null</B> if not found.
    */
   public String findComment(Node n)
   {
      NodeList children = n.getChildNodes();
      if (null != children)
      {
         for (int i = 0; i < children.getLength(); i++)
         {
            if (isNodeType(children.item(i), "uml:comment"))
            {
               return children.item(i).getAttributes().getNamedItem("body")
                     .getNodeValue();
            }
         }
         return null;
      }
      return null;
   }

   /**
    * Load an XMI file into memory using the standard XML DOM API.
    */
   public gov.nasa.jpl.statechart.uml.Model loadXmiFile( String filename )
   {
      gov.nasa.jpl.statechart.uml.Model model = null;

      // Configure the document builder the be namespace aware.
      // This code was taken from the Sun tutorial at
      // http://java.sun.com/j2ee/1.4/docs/tutorial/doc/JAXPDOM8.html
      //
      // For validation we use the new Validator object that was
      // introduced in Java 1.5
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      factory.setNamespaceAware( true );

      // Since we are simply processing the XMI file, we want to ignore
      // the pieces that are not related to the content, e.g. comments
      factory.setCoalescing( true );
      factory.setExpandEntityReferences( true );
      factory.setIgnoringComments( true );
      factory.setIgnoringElementContentWhitespace( true );      

      try 
      {
         // Get a DataBuilder instance.  This may throw an exception
         // it it does not support the flags we set above
         DocumentBuilder db = factory.newDocumentBuilder();

         // Parse the XMI document into a DOM tree
         Document document = db.parse( new File( filename ));

/*         // Create a SchemaFactory that understands WXS schemas
         SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

         // Load the XMI 2.1 schema
         Source schemaFile = new StreamSource( new File( "schema/XMI_2_1.xsd" ));
         Schema schema     = schemaFactory.newSchema( schemaFile );

         // Create the validator and validate the document
         Validator validator = schema.newValidator();
         validator.validate( new DOMSource( document ));
*/
         // Determines the specific reader version first
         determineReaderVersion(document);
         // Process the DOM and create a UML Model
         model = createUMLFromDOM( document );
      }
      catch ( ParserConfigurationException e )
      {
         // Cannot recover if the parser does not support us...
         System.err.println( e );
         System.exit( 1 );
      }
      catch ( IllegalArgumentException e )
      {
         System.err.println( e );
      }
      catch ( IOException e )
      {
         System.err.println( e );
      }
      catch ( SAXException e )
      {
         System.err.println( e );
      }

      return model;
   }

   /**
    * Parses the specified array of XML files looking for state machines.
    * 
    * @param filenames
    *           String[] Array of filenames to parse.
    * @throws Exception
    */
   public void parseXmlFiles(String[] filenames) throws Exception
   {
      System.out.println(">>> Old XMI reader <<<");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // set factory to be namespace aware
      factory.setNamespaceAware( true );
      // Since we are simply processing the XMI file, we want to ignore
      // the pieces that are not related to the content, e.g. comments
      factory.setCoalescing( true );
      factory.setExpandEntityReferences( true );
      factory.setIgnoringComments( true );
      factory.setIgnoringElementContentWhitespace( true );      

      DocumentBuilder db = factory.newDocumentBuilder();
      // Each state machine may need to cross-reference another,
      // so first read in all the state machine elements into
      // a global xmi:id map
      for (int i = 0; i < filenames.length; i++)
      {         
         try 
         {
            Document doc = db.parse( new File( filenames[i] ));
            determineReaderVersion(doc);  // determine the reader version first
            prologue(findNode( "xmi:xmi", doc.getDocumentElement(), 1 ));
         }
         catch (Exception e)
         {
            System.out.flush();
            System.out.println("");
            System.out.println("*** Error parsing file " + filenames[i] 
                  + ".");
            System.out.println("");
            throw e;
         }
      }

      // Create a public array of all the top level nodes
      topLevelNodes = new Node[filenames.length];
      allStateMachineNodeList = new ArrayList<Node>();

      for (int i = 0; i < filenames.length; i++)
      {
         // For each XML file specified...
         try
         {
            currentFilename = filenames[i];
            System.out.println("Parsing XML file " + currentFilename + "...");
            Document doc = db.parse(new File(filenames[i]));
            determineReaderVersion(doc);  // determine the reader version first

            // Find the XMI node:
            Node xmiTop = findNode("xmi:xmi", doc.getDocumentElement(), 1);
            topLevelNodes[i] = xmiTop;

            if (null == xmiTop)
            {
               throw new Exception( "Cannot find the top level XMI node - unable to parse!" );
            }

            // no longer necessary, output by readers
//            if (null != xmiTop.getAttributes().getNamedItem("xmi:version"))
//            {
//               System.out.println("(XMI version "
//                                  + xmiTop.getAttributes().getNamedItem("xmi:version")
//                                  .getNodeValue() + ")");
//            }

            // Find all signals defined in this file:
            findSignals(xmiTop);

            if (signalList.isEmpty())
            {
               System.out.println("Info: No signal definitions found.");
            }

            if (!signalsOnly)
            {
               // Create (subclass specific) lists of various statechart
               // information nodes:
               findOtherModelInfo(xmiTop);
            }

            // Create a list of state machine nodes in this XML file:
            stateMachineNodeList = new ArrayList<Node>();
            findStateMachines( xmiTop );

            allStateMachineNodeList.addAll( stateMachineNodeList );

            if (stateMachineNodeList.isEmpty()) 
            {
               System.out.println("No state machine definitions found.");
            } 
            else
            {
               System.out.println("Found " + stateMachineNodeList.size() + " state machine definitions.");
            }

            // Go through the list of state machines:
            for ( Node stateMachTop : stateMachineNodeList )
            {
                if (Autocoder.getSpecificSMNames().size() > 0) {
                    // skip if SM not named in the list
                    String name = Util.getNodeAttribute(stateMachTop, "name");
                    if (name != null &&
                            !Autocoder.getSpecificSMNames().contains(name)) {
                        System.out.println("Skipping State Machine: " + name);
                        continue;
                    }
                }
               StateMachine stateMach = createStateMachine(stateMachTop);
               System.out.println( "Processing State Machine: " + stateMach.name() );

               if (!signalsOnly)
               {
                  // We've found everything we need to build this state
                  // machine:
                  buildStateMachine(stateMach, stateMachTop);
               }

               // Add this state machine to the list:
               stateMachineMap.put(stateMach.name(), stateMach);

               // Add all the states with Ids to a map
               for ( State state : stateMach.states() ) 
               {
                  cacheStates( stateMach, state );
               }
            }

            // Add the cross references for submachines
            for ( StateMachine stateMachine : stateMachineMap.values() )
            {
               // Get all the states contained within this machine
               Map<String, State> allStates = stateMachine.getAllStates();
               
               // Filter out the submachines
               Map<String, SubmachineState> submachines =
                  StateChartCodeWriter.filterByType( allStates,
                                                     SubmachineState.class );

               for ( Map.Entry<String, SubmachineState> entry : submachines.entrySet() )
               {
                  String fullPath = entry.getKey();               
                  SubmachineState state = entry.getValue();
                  String subId = state.getSubmachine();
                  String subName = getAttr( xmiNodes.get( subId ), "name" );

                  state.setStateMachine( stateMachineMap.get( subName ) );
               }
            }

            // Reparent all the submachine's children within the enclosing
            // state
            //reparentSubmachineStates();
         } 
         catch (Exception e)
         {
            System.out.flush();
            System.out.println("");
            System.out.println("*** Error parsing file " + currentFilename
                  + ".");
            System.out.println("");
            throw e;
         }
      }      
   }


   /**
    * Links the states in a submachine state diagram with the enclosing 
    * submachine state as their parents.
    */
   private void reparentSubmachineStates()
   {
      for ( StateMachine stateMachine : stateMachineMap.values() )
      {
         // Get all the submachine in this state machine diagram
         Map<String, SubmachineState> submachines =
            StateChartCodeWriter.filterByType( stateMachine.getAllStates(),
                                               SubmachineState.class );
         
         // For each submachine state, get the submachine state machine and
         // set all the top-level states' parents to the enclosing state.

         for ( SubmachineState submachine : submachines.values() )
         {
            StateMachine sm = submachine.getStateMachine();
            for ( State state : sm.getAllStates( 1 ).values() )
            {
               state.setParent( submachine );
            }
         }
      }
   }

   private String getAttr( Node node, String attr )
   {
      return getAttr( node, attr, "" );
   }
   
   private String getAttr( Node node, String attr, String def )
   {
      String value = def;
      
      if ( node != null && ( node.getAttributes() != null ))
      {
         NamedNodeMap nm = node.getAttributes();
         if ( nm.getNamedItem( attr ) != null )
            value = nm.getNamedItem( attr ).getNodeValue();
      }
      
      return value;
   }
   
   private static void cacheStates( StateMachine machine, State state )
   {
      machine.xmiStates.put( state.id(), state );
      
      if ( state instanceof CompositeState )    
         for ( State s : ((CompositeState) state).getChildren())
            cacheStates( machine, s );
   }

   public static State getCachedState (String id) {
       return null;
   }

   /**
    * Performs some reader-specific parsing of the XMI structure
    * 
    * @param xmiTop
    *           Node The top of the XMI document to search.
    */
   protected void prologue(Node xmiTop)
   {
     if ( xmiTop == null )
        return;

     // [SWC 2009.09.21] Hack: store node, checking for duplicate IDs, ONLY for a UML node we care about

     String nodeName = xmiTop.getNodeName();
     NamedNodeMap attrs = xmiTop.getAttributes();
     if ( (UMLIdentifiers.inst().hasIdentifier(nodeName)
             || MDUmlReaderHelper.inst().hasTag(nodeName))
             && attrs != null && attrs.getNamedItem( "xmi:id" ) != null )
     {
        String value = attrs.getNamedItem( "xmi:id" ).getNodeValue();
        
        if (xmiNodes.get( value ) != null && !Autocoder.ignoreDuplicateXmiIds() )
        {  // no longer should we exit the application.
           System.out.println( "*** ERROR: Duplicate xmi:id " + value );
        }

        xmiNodes.put( value, xmiTop );
     }

     NodeList children = xmiTop.getChildNodes();
     for ( int i = 0; i < children.getLength(); i++ ) {
        prologue( children.item( i ));
     }
   }

   /**
    * Checks if the passed string conforms to method call syntax.
    * 
    * @param text
    *           String The text to check.
    * @return boolean True if it looks like a method call, false otherwise.
    */
   static public boolean isMethodCallSyntax(String text)
   {
      int openIndex = text.indexOf("(");
      int closeIndex = text.indexOf(")");
      if (openIndex >= 1 && closeIndex >= 2 && openIndex < closeIndex)
         return true;
      else
         return false;
   }

   /**
    * Searches through the list of signal nodes trying to find one that matches
    * the specified attribute name and value.
    * 
    * @param attrName
    *           String The node attribute name.
    * @param attrValue
    *           String The node attribute value.
    * @return The matching node, or null if not found.
    */
   protected Node findSignalWithAttr(String attrName, String attrValue)
   {
      Iterator nextSignal = signalList.iterator();
      while (nextSignal.hasNext())
      {
         Node sig = (Node) nextSignal.next();
         if (null != sig.getAttributes().getNamedItem(attrName)
               && null != sig.getAttributes().getNamedItem(attrName)
                     .getNodeValue()
               && sig.getAttributes().getNamedItem(attrName).getNodeValue()
                     .equals(attrValue))
         {
            return sig;
         }
      }
      return null;
   }

   /**
    * Parses a string containing a set of integers.
    * 
    * @param intString
    *           String Text integer values.
    * @return int[] Array of integer values, or null if not able to parse.
    */
   protected int[] toIntArray(String intString)
   {
      String[] intStringArray = intString.split("[ ,;]");
      int[] intArray = new int[intStringArray.length];
      int j = 0;
      for (int i = 0; i < intStringArray.length; i++)
      {
         try
         {
            if (intStringArray[i].length() > 0)
            {
               intArray[j] = Integer.parseInt(intStringArray[i]);
               j++;
            }
         } catch (Exception e)
         {
            System.out.println("Error parsing int string; " + e.getMessage());
            return null;
         }
      }
      int[] result = new int[j];
      for (int i = 0; i < j; i++)
      {
         result[i] = intArray[i];
      }
      return result;
   }

   /**
    * Checks the passed node's name to see if it matches the specified <i>name</i>
    * parameter.
    * <p>
    * <b>Note:</b> The term "node name" can be ambigious. The node name which
    * is accessed via the <code>node.getNodeName()</code> method <b>is
    * different</b> from a node's attribute called "NAME". I'm using the term
    * "name" to refer to the value returned via <code>getNodeName</code>,
    * while the value of the node attribute "NAME" is what I'm calling
    * "attribute name".
    * </p>
    * 
    * @param n
    *           Node The node to check.
    * @param name
    *           String The name of node to check for.
    * @return boolean True if the name matches, false otherwise.
    */
   protected boolean isNodeName(Node n, String name)
   {
      return (null != n && null != n.getNodeName() && n.getNodeName()
            .equalsIgnoreCase(name));
   }

   /**
    * Checks the XMI:TYPE attribute of the passed node to see if it matches the
    * specified <i>type</i> parameter.
    * 
    * @param n
    *           Node The node to check.
    * @param type
    *           String The type of node to check for.
    * @return boolean True if the type matches, false otherwise.
    */
   protected boolean isNodeType(Node n, String type)
   {
      return (null != n
            && null != n.getAttributes()
            && null != n.getAttributes().getNamedItem("xmi:type")
            && null != n.getAttributes().getNamedItem("xmi:type")
                  .getNodeValue() && ((String) n.getAttributes().getNamedItem(
            "xmi:type").getNodeValue()).equalsIgnoreCase(type));
   }

   /**
    * Finds all the XMI nodes which have the specified name, and optionally the
    * specified attribute name and value.
    * 
    * @param nodeList
    *           List List to which to add the found nodes.
    * @param name
    *           String name of XMI node to search for. An empty string ("")
    *           indicates to accept all node names.
    * @param requiredAttr
    *           String Optional parameter with the name of a required attribute
    *           to match.
    * @param attrValue
    *           Optional parameter with the value of the required attribute to
    *           match.
    * @param n
    *           Node The tree node at which to begin the search.
    * @param searchDepth
    *           int The number of levels below the starting node to search the
    *           tree.
    */
   public void findNodes(List<Node> nodeList, String name,
         String requiredAttr, String attrValue, Node n, int searchDepth)
   {
      if (null == nodeList || null == name || null == n)
         return;
      if (name.length() < 1 || n.getNodeName().equalsIgnoreCase(name))
      {
         // This node is a potential match, so check for required attribute:
         if (null == requiredAttr // No required attribute specified...
               || ((null != n.getAttributes() // Does it have attributes?
               && null != n.getAttributes().getNamedItem(requiredAttr)) && (null == attrValue
               // No attribute value specified...
               || (attrValue.equalsIgnoreCase( // Does specified value match?
                     n.getAttributes().getNamedItem(requiredAttr)
                           .getNodeValue())))))
         {
            nodeList.add(n); // Found it!
         }
      }
      if (searchDepth > 0)
      {
         // Search child nodes below this node:
         NodeList children = n.getChildNodes();
         if (children != null)
         {
            int length = children.getLength();
            for (int i = 0; i < length; i++)
            {
               findNodes(nodeList, name, requiredAttr, attrValue, children
                     .item(i), searchDepth - 1);
            }
         }
      }
   }

   // Overload of findNodes to allow unspecified attribute name and value.
   protected void findNodes(List<Node> elements, String name, Node n,
         int searchDepth)
   {
      findNodes(elements, name, null, null, n, searchDepth);
   }

   // Overload of findNodes to allow unspecified attribute value.
   public void findNodes(List<Node> elements, String name,
         String requiredAttr, Node n, int searchDepth)
   {
      findNodes(elements, name, requiredAttr, null, n, searchDepth);
   }

   /**
    * Finds and returns a single XMI node which has the specified name, and
    * optionally the specified attribute name and value.
    * 
    * @param name
    *           String Name of XMI node to search for. An empty string ("")
    *           indicates to accept all node names.
    * @param requiredAttr
    *           String Optional parameter with the name of a required attribute
    *           to match.
    * @param attrValue
    *           String Optional parameter with the value of the required
    *           attribute to match.
    * @param n
    *           Node The starting node at which to begin the search of the tree.
    * @param searchDepth
    *           int The number of levels below the starting node to search the
    *           tree.
    * @return Node The matching node if found, null if not found.
    */
   protected Node findNode(String name, String requiredAttr, String attrValue,
         Node n, int searchDepth)
   {
      if (null == name || null == n)
         return null;
      if (name.length() < 1 || n.getNodeName().equalsIgnoreCase(name))
      {
         // This node is a potential match, so check for required attribute:
         if (null == requiredAttr // No required attribute specified...
               || ((null != n.getAttributes() // Does it have attributes?
               && null != n.getAttributes().getNamedItem(requiredAttr)) && (null == attrValue // No
               // attribute
               // value
               // specified...
               || (attrValue.equalsIgnoreCase( // Does specified value match?
                     n.getAttributes().getNamedItem(requiredAttr)
                           .getNodeValue())))))
         {
            return n; // Found it!
         }
      }
      // This node doesn't match, so search the specified number of levels
      // below this node:
      if (searchDepth > 0)
      {
         NodeList children = n.getChildNodes();
         if (children != null)
         {
            int length = children.getLength();
            for (int i = 0; i < length; i++)
            {
               Node result = findNode(name, requiredAttr, attrValue, children
                     .item(i), searchDepth - 1);
               if (null != result)
               {
                  return result;
               }
            }
         }
      }
      return null; // Didn't find it...
   }

   // Overload of findNode to allow unspecified attribute name and value.
   public Node findNode(String name, Node n, int searchDepth)
   {
      return findNode(name, null, null, n, searchDepth);
   }

   // Overload of findNode to allow unspecified attribute value.
   protected Node findNode(String name, String requiredAttr, Node n,
         int searchDepth)
   {
      return findNode(name, requiredAttr, null, n, searchDepth);
   }

   /**
    * Test method to print information on a node.
    * 
    * @param ps
    *           The stream to which to print.
    * @param n
    *           The node to print.
    */
   protected void printNode(PrintStream ps, Node n)
   {
      if (!debugOn)
      {
         return;
      }
      ps.print(n + " name: ");
      ps.print(n.getNodeName() + ", type: ");
      ps.print(n.getNodeType() + ", value: ");
      ps.print(n.getNodeValue() + ", ");
      if (null != n.getAttributes())
      {
         ps.print("Attrs: ");
         NamedNodeMap attrs = n.getAttributes();
         String separator = "";
         for (int i = 0; i < attrs.getLength(); ++i)
         {
            ps.print(separator + attrs.item(i).getNodeName() + ": "
                  + attrs.item(i).getNodeValue());
            separator = ", ";
         }
      }
      ps.println("");
   }
}
