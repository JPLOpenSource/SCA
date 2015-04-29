/**
 * This class encapsulates the task of writing a header file for a given
 * state chart.  This is the generic framework, other specializations
 * can inherit from this class to override or veto certain operations.
 */
package gov.nasa.jpl.statechart.autocode.cm;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.StateChartCodeWriter;
import gov.nasa.jpl.statechart.core.CompositeState;
import gov.nasa.jpl.statechart.core.CompositeStateRegion;
import gov.nasa.jpl.statechart.core.SimpleState;
import gov.nasa.jpl.statechart.core.State;
import gov.nasa.jpl.statechart.core.StateMachine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class StateChartHeaderWriter extends StateChartCodeWriter
{
   protected final String classname;
   protected final String meType;

   public StateChartHeaderWriter( StateMachine stateMachine, String classname )
   {
      this( stateMachine, classname, classname );
   }

   public StateChartHeaderWriter( StateMachine stateMachine, String classname, String meType )
   {
      super( stateMachine );
      this.classname = classname;
      this.meType = meType;

      System.out.println( "Header Writer " + stateMachine.name() + " meType = " + meType );
   }

   public StateChartHeaderWriter( StateMachine stateMachine )
   {
      this( stateMachine, stateMachine.name() );
   }

   @Override
   public void writeCode( String filename )
   {
      this.currentFilename = filename;
        
      try
      {
         out = new PrintStream( new FileOutputStream( filename ) );

         // Start off without an indentation
         setIndent( 0 );

         writeCommentBlock();
         writePrologue();
         writeDataTypes();
         writeFunctionPrototypes();
         writeEpilogue();
                 
         out.flush();
         out.close();
         out = null;
      }
      catch ( IOException e )
      {
         System.err.println( e );
      }

      this.currentFilename = "";
   }

   /**
    * Write all the statements the preceed an actual data structure or
    * function declarations.  Things like other #includes and conditional
    * compilation directives
    */
   protected void writePrologue()
   {
      String mnemonic  = getMnemonic();

      out.println("#ifndef " + mnemonic);
      out.println("#define " + mnemonic);
      out.println();
      out.println("#include \"qf_port.h\"");
      out.println("#include \"qassert.h\"");
      out.println();
         
      // Forward declaration of implementation class:
      out.println("#include \"" + classname + "Impl.h\"");
      out.println();
   }

   /**
    * Write out the struct and enum definitions.  These methods tend
    * to be recursive and automatically descend into submachines in
    * order to place all the signals/regions/timers/etc. into a single
    * namespace that's accessible from the main struct (the "me" variable)
    */
   protected void writeDataTypes()
   {
      // These define all the states within the state machine
      writeStateEnumDeclarations();

      // We need separate declarations for region types
      writeStateRegionDeclarations();

      // And, finally output the class struct
      writeQActiveDeclaration();
   }

   protected void writeStateEnumDeclarations()
   {
      // Get the current path prefix to this state machine
         
      String prefix = getClassPrefix();

      // Get a list of all the terminal (simple) states in the state
      // chart.  There will be a unique indentifier for each one. Note
      // that the set of terminal states will include all the states
      // contained within the various submachines and compound states

      Map<String, SimpleState> states = 
         filterByType( stateMachine.getAllStates( prefix, true ), 
                       SimpleState.class );

      // In order to make the output prettier, run through all the
      // declarations and compute the maximum width in order to align the
      // columns.
      int    maxDeclWidth = getLongestString( states.keySet() );
      String padding      = Util.strrep( ' ', maxDeclWidth );

      // Print out each state name
      out.println(indent + "enum StateEnum" + classname );
      out.println(indent + "{");

      // Increase the indentation
      incIndent();

      // Sort the keys to make it pretty
      SortedSet<String> sortedKeys = 
         new TreeSet<String>( states.keySet() );

      int count = 0;
      for ( String s : sortedKeys )
      {
         String pad = padding.substring( 0, maxDeclWidth - s.length() );
        
         out.print( indent + s.replace(':', '_').toUpperCase());
         out.print( (count == states.size() - 1 ) ? " " : "," ); 
         out.println( pad + " /* State = "+ count + " */" );

         count++;
      }
         
      // Decrease the indentation
      decIndent();

      out.println(indent + "};");
      out.println();
   }

   protected void writeStateRegionDeclarations()
   {
      boolean first = false;

      // Get the initial path prefix

      String prefix = getClassPrefix();

      // There are two items that need to be dealt with here.  First, we
      // have to search through all the submachines and find every
      // orthogonal region that is not part of the current state machine
      // and create a forward declaration for it.  Second, for all of the
      // orthogonal regions that are defined in this state machine, we
      // have to define their structs.
      //
      // Filter out the Composite State regions and only keep the local
      // regions in their own set for fast comparisons

      Map<String, CompositeStateRegion> allRegions = 
         filterByCompositeStateRegion( stateMachine.getAllStates( prefix, true ));
         
      Map<String, CompositeStateRegion> localRegions = 
         filterByCompositeStateRegion( stateMachine.getAllStates( prefix ));

      // Sort the region names
      SortedSet<String> localPaths = new TreeSet<String>( localRegions.keySet() );

      // Create a set of path names for non-local regions
      SortedSet<String> nonLocalPaths = new TreeSet<String>( allRegions.keySet() );
      nonLocalPaths.removeAll( localPaths );

      // First, generate forward references for the regions contained in
      // submachines     
      if ( !nonLocalPaths.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Forward Declaration of Concurrent Region Types" );
         out.println( indent + " *" );
         out.println( indent + " * We cannot use forward declaration of typedefed" );
         out.println( indent + " * structs, so the struct keyword remains" );
         out.println( indent + " */" );
      }
         
      for ( String regionPath : nonLocalPaths )
      {
         // Remove the path separators
         String regionType = regionPath.replace( ":", "" ) + "Region";
            
         out.println( indent + "struct " + regionType + ";" );
      }
                   
      // Second, generate the struct definitions for regions that are
      // part of the current state machine
      if ( !localPaths.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Declaration of Local Concurrent Regions" );
         out.println( indent + " *" );
         out.println( indent + " * These regions are present in this" );
         out.println( indent + " * state machine" );
         out.println( indent + " */" );
      }

      for ( String regionPath : localPaths )
      {
         CompositeStateRegion region = allRegions.get( regionPath );

         // Write out the struct that defines the subregion
         writeOneRegionDeclaration( region, regionPath );
      }
   }

   protected void writeQActiveDeclaration()
   {
      // Get the initial path prefix

      String prefix = getClassPrefix();

      // Generate the implementation struct

      String  implName = classname + "Impl";

      // Get a copy of all the states, including the states contained
      // within submachines and orthogonal regions

      Map<String, State> allStates = 
         stateMachine.getAllStates( prefix, true );

      // Begin writing out the struct definition

      out.println();
      out.println(indent + "typedef struct " + classname );
      out.println(indent + "{");
         
      incIndent();
      out.println( indent + "QActive super_;" );      
         
      if ( executionTraceOn )
      {
         out.println( indent + "char objName[256];" );
      }
         
      out.println( indent + implName + "* impl;" );

      // For each composite state with a history, create a history
      // variable
      Map<String, CompositeState> historyStates = 
         filterByHistoryContainers( allStates );

      if ( !historyStates.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Histories */" );
      }

      for ( Map.Entry<String, CompositeState> entry : historyStates.entrySet() )
      {
         String historyName = "my" + entry.getValue().name() + "history";
         out.println( indent + "QState " + historyName + ";" );
      }

      // Write the timers. Each timer is given a unique name by
      // appending a "_timer" string to the identifying path to the
      // timed state.
      //
      // We only record timers that are not contained within orthogonal
      // regions      

      Map<String, SimpleState> timedStates = 
         sortStatesByEnclosingRegion( filterByTimers( allStates )).get( null );

      if ( !timedStates.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Timers */" );
      }

      // Only keep the states in the "null" bucket      
      for ( Map.Entry<String, SimpleState> entry : timedStates.entrySet() )
      {
         String timerName = entry.getValue().name() + "_timer";
         out.println( indent + "QTimeEvt " + timerName + ";" );
      }      

      // For each concurrent composite state, create region structure to
      // keep track of the concurrency         
      Map<String, CompositeStateRegion> regions = 
         filterByCompositeStateRegion( allStates );
        
      if ( !regions.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Concurrent State Regions */" );
      }

      for ( String regionPath : regions.keySet() )
      {
         CompositeStateRegion region = regions.get( regionPath );

         // Remove the path separators
         String regionType = regionPath.replace( ":", "" ) + "Region *";
         String regionDecl = region.name().toLowerCase();

         out.println( indent + "struct " + regionType + regionDecl + ";" );
      }      

      // Finally output the "mystate" variable that keeps track of the
      // current state of execution the state machine is in.
      out.println();
      out.println( indent + "enum StateEnum" + classname + " mystate;" );
      
      decIndent();

      out.println( indent + "} " + classname + ";" );
      out.println();
      out.println();
   }      

   /**
    * Write out all the function declarations
    */
   protected void writeFunctionPrototypes()
   {
      writeInitializationPrototypes();
      writeStateRegionPrototypes();
      writeStatePrototypes();
   }

   @Override
   public String getClassPrefix()
   {
      return classname;
   }

   protected void writeInitializationPrototypes()
   {
      String returnType = meType + "* ";
      String prefix     = getClassPrefix();         
      String implName   = classname + "Impl";

      out.println( indent + "/**" );
      out.println( indent + " * Function prototypes for state machine creation and" );
      out.println( indent + " * initialization." );
      out.println( indent + " */" ); 
      out.println( ); 

      // Declare the constructor
      String functionName = prefix + "_Constructor";

      out.println( returnType + functionName + "(" 
                   + meType + "* me, char* objNameNew, "
                   + implName + "* implPtr);");


      // Declare the initialization function
      returnType   = "void ";
      functionName = prefix + "_initial";

      out.println( indent + returnType + functionName + "(" + 
                   meType + "* me, QEvent const* e);" );
   }

   protected void writeStatePrototypes()
   {
      String prefix = getClassPrefix();

      // Get all the top-level simple states defined in this state machine

      Map<String, SimpleState> topStates = 
         filterByType( stateMachine.getAllStates( prefix, false, 1 ), SimpleState.class );

      // For each top-level state, get all of its child states, but do
      // not decend into any submachines
         
      for ( Map.Entry<String, SimpleState> entry : topStates.entrySet() )
      {
         String path = entry.getKey();
         SimpleState state = entry.getValue();

         // Remove the state name itself from the path
         int last = path.lastIndexOf( ":" );
         if ( last >= 0 )
            path = path.substring( 0, last );

         Map<String, SimpleState> childStates =
            filterByType( stateMachine.getAllStates( state, path ), SimpleState.class );

         // Write out the prototypes for this subtree
         writePrototypeComment( childStates );
         writeMethodPrototypes( childStates );
      }      
   }

   /**
    * Writes out prototypes for orthogonal subregions that are defined
    * locally to this state machine
    */
   protected void writeStateRegionPrototypes()
   {
      // Create the initial path prefix
      String prefix = getClassPrefix();

      // Find all of the local regions along with their qualified paths
      Map<String, CompositeStateRegion> localRegions = 
         filterByCompositeStateRegion( stateMachine.getAllStates( prefix ));

      // Sort the region names
      SortedSet<String> regionPaths = new TreeSet<String>( localRegions.keySet() );

      for ( String regionPath : regionPaths )
      {
         CompositeStateRegion region = localRegions.get( regionPath );
         String regionName = localRegions.get( regionPath ).name();

         out.println();
         out.println( indent + "/***" );
         out.println( indent + " * Subregion prototypes for " + regionName );
         out.println( indent + " */" );               
            
         // Write out the function prototypes
         writeOneRegionPrototypes( region, regionPath );
      }
   }

   /** 
    * The region declaration must tie into the same name space as the 
    * enclosing state machine diagram
    */
   protected void writeOneRegionDeclaration( final CompositeStateRegion region, final String path )
   {
      String  implName   = classname + "Impl";
      String  regionDecl = region.name().toLowerCase();
      String  regionType = path.replace( ":", "" ) + "Region";

      // Get all of the states contained within this region
      int last = path.lastIndexOf( ":" );
      if ( last < 0 )
         last = path.length();

      Map<String, State> allStates = 
         stateMachine.getAllStates( region, path.substring( 0, last ));
          
      out.println( indent + "typedef struct " + regionType );
      out.println( indent + "{");

      incIndent();

      out.println( indent + "QHsm super_;" );
      out.println( indent + "QActive * parent;" );
      
      if ( executionTraceOn )
      {
         out.println( indent + "char objName[256];" );
      }

      out.println( indent + implName + "* impl;" );
         
      // For each composite state with a history, write a history
      // variable
      Map<String, CompositeState> historyStates = 
         filterByHistoryContainers( allStates );

      if ( !historyStates.isEmpty() )
      {
         out.println();
         out.println( indent + "/* Histories */" );
      }
         
      for ( Map.Entry<String, CompositeState> entry : historyStates.entrySet() )
      {
         String historyName = "my" + entry.getValue().name() + "history";
         out.println( indent + "QState " + historyName + ";" );
      }

      // Write the timers. Each timer is given a unique name by
      // appending a "_timer" string to the identifying path to the
      // timed state.  Only emit the timers that are part of this
      // region
      //
      // We have to dig through the submachine here, so we cannot use the
      // allStates map we already computed.
      Map<CompositeStateRegion, Map<String, SimpleState>> statesByRegion =
         sortStatesByEnclosingRegion( filterByTimers( stateMachine.getAllStates( region, path.substring( 0, last ), true )));

      if ( statesByRegion.containsKey( region ))
      {
         out.println();
         out.println( indent + "/* Timers */" );

         Map<String, SimpleState> timers = statesByRegion.get( region );
         for ( Map.Entry<String, SimpleState> entry : timers.entrySet() )
         {
            String timerName = entry.getValue().name() + "_timer";
            out.println( indent + "QTimeEvt " + timerName + ";" );
         }      
      }
      // We do not current support recursing into orthogonal
      // states within an orthogonal state
      
      out.println();
      out.println( indent + "enum StateEnum" + classname + " mystate;" );

      decIndent();

      out.println( indent + "} " + regionType + ";" );
      out.println();
      out.println();
   }


   protected void writeOneRegionPrototypes( CompositeStateRegion region, String path )
   {        
      String implName   = classname + "Impl";
      String regionType = path.replace( ":", "" ) + "Region";

      // Initial state
      out.println( indent + regionType + " *" );
      out.println( indent + regionType + "_Constructor(" + 
                   regionType + "* me, char* objNameNew, " + 
                   implName + "* implPtr, QActive * parent);");
      out.println();
         
      out.println( indent + "void " );
      out.println( indent + regionType + "_initial(" + 
                   regionType + "* me, QEvent const* e);");
      out.println();
   }

   protected void writeMethodPrototypes( Map<String, SimpleState> states )
   {
      // Class to test for CompositeStateRegions
      Class<CompositeStateRegion> csr = CompositeStateRegion.class;

      // Sort the names
      for ( String path : new TreeSet<String>( states.keySet() ))
      {
         State  state = states.get( path );

         // No prototypes for the orthogonal regions themselves
         if ( csr.isAssignableFrom( state.getClass() ))
            continue;

         String functionName = path.replace( ":", "_" );
         String implType     = meType;

         CompositeStateRegion region = stateMachine.getEnclosingRegion( state );
         if ( region != null )
         {
            implType = stateMachine.getQualifiedPath( region, getClassPrefix() ).replace( ":", "" ) + "Region";
         }

         out.println( indent + "QSTATE " + functionName + 
                      "(" + implType + "* me, QEvent const* e);" );
      }
   }

   protected void writePrototypeComment( Map<String, SimpleState> states )
   {
      // Class to test for CompositeStateRegions
      Class<CompositeStateRegion> csr = CompositeStateRegion.class;

      out.println();
      out.println( indent + "/**" );
      out.println( indent + " * Function prototypes for state handlers" );
      out.println( indent + " *" );

      // Count the indentation of the class prefix
      int classDepth = getPathDepth( getClassPrefix() );

      // Sort the states 
      SortedSet<String> sortedKeys = new TreeSet<String>( states.keySet() );

      for ( String path : sortedKeys )
      {
         // Count the number of path separators to get the depth
         int depth = getPathDepth( path );            
         depth = Math.max( 0, depth - classDepth - 1 );

         out.print( indent + " * " );
         for ( int i = 0; i < depth; i++ )
            out.print( INDENT_STEP );

         out.print( states.get( path ).name() );

         if ( csr.isAssignableFrom( states.get( path ).getClass() ))
            out.print( " [Orthogonal Region]" );

         out.println();
      }
         
      out.println( indent + " */" );            
      out.println();         
   }

   protected int getPathDepth( String path )
   {
      int depth = 0;
      int pos   = -1;

      while (( pos = path.indexOf( ":", pos + 1 )) >= 0 )
         depth++;

      return depth;
   }

   protected String getMnemonic()
   {
      return "_" + classname.toLowerCase() + "_h";
   }

   /**
    * Close off statements in the prologue
    */
   protected void writeEpilogue()
   {
      String mnemonic = getMnemonic();

      out.println();
      out.println( indent + "#endif /* " + mnemonic + " */" );
   }

   protected int getLongestString( Collection<String> stateList )
   {
      int max = 0;
      for ( String s : stateList )
         max = Math.max( max, s.length() );

      return max;
   }
}
