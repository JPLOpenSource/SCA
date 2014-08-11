package gov.nasa.jpl.statechart.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * <p>
 * An internal representation of a UML compliant state machine. A StateMachine
 * object contains lists of state and transition objects which are defined by a
 * user as being a part of the state machine.
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
 * CVS Identification: $Id: StateMachine.java,v 1.16 2005/10/11 18:08:09 kclark
 * Exp $
 * </p>
 * 
 * @see State
 * @see Transition
 */
public class StateMachine
{
   private String  name;
   private String  id;

   private List<State>      states      = new ArrayList<State>();
   private List<Transition> transitions = new ArrayList<Transition>();

   private HashSet<String>               stateNames              = new HashSet<String>();
   private List<DiagramSeparatorElement> separators              = new ArrayList<DiagramSeparatorElement>();
   private CoordinatePoint               canvasSize;
   private List<Node>                    stateMdElementList      = new ArrayList<Node>();
   private List<Node>                    transitionMdElementList = new ArrayList<Node>();
   private List<DiagramTextElement>      textElementList         = new ArrayList<DiagramTextElement>();

   // A cache of all the states excluding submachines
   private Map<String, State> stateMap = null;
   // A cache of XMI IDs to states
   public final HashMap<String, State> xmiStates = new HashMap<String, State>();

   /**
    * Constructor.
    * 
    * @param name
    *           String The name of this state machine instance.
    * @param id
    *           String The cross-reference ID of this state machine.
    * @throws Exception
    */
   public StateMachine(String name, String id) throws Exception
   {
      if (name.length() < 1)
      {
         throw new Exception("State machine (ID=" + id
                             + ") has blank name - state machine must be named!");
      }

      this.name = name;
      this.id   = id;
   }

   /**
    * Returns the list of state definition nodes found for this state machine.
    * 
    * @return A list of nodes.
    */
   public List<Node> stateMdElementList()
   {
      return stateMdElementList;
   }

   /**
    * Returns the list of transition definition nodes found for this state
    * machine.
    * 
    * @return A list of nodes.
    */
   public List<Node> transitionMdElementList()
   {
      return transitionMdElementList;
   }

   /**
    * Adds a GUI diagram text element object to this state machine.
    * 
    * @param textElement
    *           The text element to add.
    */
   public void add(DiagramTextElement textElement)
   {
      textElementList.add(textElement);
   }

   /**
    * Defines the GUI canvas size for this state machine.
    * 
    * @param maxPoint
    *           CoordinatePoint Maximum (X,Y) corrdinate point on the canvas,
    *           assuming the starting point is at (0,0).
    */
   public void setCanvasSize(CoordinatePoint maxPoint)
   {
      if (null != maxPoint)
      {
         canvasSize = maxPoint;
      }
   }

   /**
    * Returns a corrdinate point which defines the size of the GUI canvas for
    * this state machine.
    * 
    * @return CoordinatePoint Maximum (X,Y) corrdinate point on the canvas,
    *         assuming the starting point is at (0,0).
    */
   public CoordinatePoint canvasSize()
   {
      return canvasSize;
   }

   /**
    * Returns the name of the state machine.
    * 
    * @return String state machine name.
    */
   public String name()
   {
      return this.name;
   }

   /**
    * Returns the cross-reference ID string of the state machine.
    * 
    * @return String state machine ID.
    */
   public String id()
   {
      return this.id;
   }

   /**
    * Returns the list of states contained by this state machine.
    * 
    * @return List The list of state objects for this state machine.
    */
   public List<State> states()
   {
      return this.states;
   }

   public List<State> allStates()
   {
      List<State> all = new ArrayList<State>();
      for ( State state : states )
         getAllStates( state, all );
      return all;
   }

   private void getAllStates( State state, List<State> stateList )
   {
      stateList.add( state );
      if ( state instanceof CompositeState )    
         for ( State s : ((CompositeState) state).getChildren())
            getAllStates( s, stateList );
   }

   /**
    * Return a map of all the states indexed by their unique path.  This
    * method may be called with an optional prefix to prepend to the paths
    * and can be directed to expand submachine states or not.
    *
    * Also, the search can be depth limited as well.
    *
    * By default the path prefix is the state machine name and submachines
    * are not expanded.
    */
   public Map<String, State> getAllStates()
   {
      return getAllStates( name(), false, Integer.MAX_VALUE );
   }

   public Map<String, State> getAllStates( boolean decend )
   {
      return getAllStates( name(), decend, Integer.MAX_VALUE );
   }

   public Map<String, State> getAllStates( String prefix )
   {
      return getAllStates( prefix, false, Integer.MAX_VALUE );
   }

   public Map<String, State> getAllStates( int maxDepth )
   {
      return getAllStates( name(), false, maxDepth );
   }

   public Map<String, State> getAllStates( String prefix, boolean decend )
   {
      return getAllStates( prefix, decend, Integer.MAX_VALUE );
   }

   public Map<String, State> getAllStates( String prefix, boolean decend, int maxDepth )
   {
      Map<String, State> map = new HashMap<String, State>();

      for ( State state : states() )
      {
         getAllStates( state, prefix, decend, 0, maxDepth, map );
      }

      return map;
   }

   public Map<String, State> getAllStates( State state, String prefix )
   {
      return getAllStates( state, prefix, false );
   }

   public Map<String, State> getAllStates( State state, String prefix, boolean decend )
   {
      Map<String, State> map = new HashMap<String, State>();

      getAllStates( state, prefix, decend, 0, Integer.MAX_VALUE, map );

      return map;
   }

   protected void getAllStates( State state, String path, boolean decend, int depth, int maxDepth, Map<String, State> map )
   { 
      // If we've reached the maximum depth, return immediately
      if ( depth >= maxDepth )
      {
         return;
      }

      // Extend the path to include the current state.       
      String statePath = path + ":" + state.name();

      // Check to see if path is unique -- it is an error if not.
      if ( map.containsKey( statePath ) && !( state.name() == null ))
      {
         throw new RuntimeException( "Duplicate state path : " + statePath );
      }

      // Save the path/state pair is the name is non-null (why is this
      // allowed?)
      if ( state.name() != null )
         map.put( statePath, state );

      // If the state is a submachine and the option is set to decend into
      // submachines, get the underlying state machine and get all of its
      // states 
      if (( state instanceof SubmachineState ) && decend )
      {
         StateMachine submachine = ((SubmachineState) state).getStateMachine();
         if ( submachine == null )
         {
            String err = 
               "Error: Submachine state " + state.name() + 
               " found without a defined State Machine!";

            throw new RuntimeException( err );
         }

         map.putAll( submachine.getAllStates( statePath, decend, maxDepth - depth - 1 ));
      }

      // If the state is a concurrent composite state, we expand the
      // namespace to include each orthogonal region
      else if ( state instanceof ConcurrentCompositeState )
      {
         ConcurrentCompositeState composite = (ConcurrentCompositeState) state;
         
         for ( CompositeStateRegion region : composite.getAllSubRegions() )
         {
            getAllStates( region, statePath, decend, depth + 1, maxDepth, map );
         }
      }

      // For ordinary composite states, just recursively find all the
      // enclodes state
      else if ( state instanceof CompositeState )
      {
         for ( State child : ((CompositeState) state).getChildren() )
         {
            getAllStates( child, statePath, decend, depth + 1, maxDepth, map );
         }
      }
   }
   
   /**
    * Returns the list of transition objects contained by this state machine.
    * 
    * @return List The list of transition objects.
    */
   public List<Transition> transitions()
   {
      return this.transitions;
   }

   /**
    * Returns the list of GUI text diagram elements contained by this state
    * machine.
    * 
    * @return List The list of text diagram elements.
    */
   public List<DiagramTextElement> textElementList()
   {
      return this.textElementList;
   }

   /**
    * Returns a list of GUI separator diagram elements contained by this state
    * machine.
    * 
    * @return List The list of separator diagram elements.
    */
   public List<DiagramSeparatorElement> separators()
   {
      return this.separators;
   }

   /**
    * Add a state object to this state machine.
    * 
    * @param s
    *           State State object to add.
    * @throws Exception
    */
   public void add(State s) throws Exception
   {
      // Check if this state has a corresponding diagram element, and discard if
      // not...
      if (null == s.diagramElement())
         return;
      states.add(s);
      addStateName(s.name());
   }

   /**
    * Add a diagram separator element to this state machine.
    * 
    * @param separator
    *           DiagramSeparatorElement The separator to add.
    */
   public void add(DiagramSeparatorElement separator)
   {
      separators.add(separator);
   }

   /**
    * Checks if the passed string name is already defined for this state
    * machine, and if not, adds it to this state machine's list.
    * 
    * @param name
    *           String The state name to add.
    * @throws Exception
    *            If the state name is already defined for this state machine.
    */
   private void addStateName(String name) throws Exception
   {
      if (null == name)
         return;
      if (stateNames.contains(name))
      {
         throw new Exception("State name \"" + name
                             + "\" already defined for state chart \"" + this.name
                             + "\".  State names must be unique within a state chart.");
      } else
      {
         stateNames.add(name);
      }
   }

   /**
    * Add a transition object to this state machine.
    * 
    * @param t
    *           Transition Transition object to add.
    * @throws Exception
    */
   public void add(Transition t) throws Exception
   {
      // Check if this transition has a corresponding diagram element, and
      // discard if not (unless it's an internal transition, which doesn't need
      // a diagram element).
      if (!t.isInternal() && null == t.diagramElement())
         return;
      transitions.add(t);
   }

   /**
    * Returns the state into which the specified transition ID goes.
    * 
    * @param transId
    *           String The transition cross-reference ID.
    * @return State The found state, or null if not found.
    */
   public State getTargetState(String transId)
   {
      State result = null;
      Iterator i = states.iterator();
      while (i.hasNext())
      {
         State s = (State) i.next();
         // System.out.println("Looking for " + transId + ", top: " + s.name() +
         // " ");
         result = recursivelyFindTargetState(s, transId);
         if (null != result)
         {
            return result;
         }
      }
      return null;
   }

   private State recursivelyFindTargetState(State s, String transId)
   {
      //System.out.println("Recursively looking for " + transId + ", state: " +
      //s.name() + ", class: " + s.getClass());
      if ( s instanceof EntryPoint )
      {
         if (((EntryPoint) s).hasIncoming(transId))
         {
            return s;
         }
      } else if ( s instanceof ExitPoint )
      {
         if (((ExitPoint) s).hasIncoming(transId))
         {
            return s;
         }
      } else if ( s instanceof ConnectionPointReference )
      {
         if (((ConnectionPointReference) s).hasIncoming(transId))
         {
            return s;
         }      
      } else if (s instanceof CompositeState)
      {
         if (((CompositeState) s).hasIncoming(transId))
         {
            return s;
         }
         // System.out.println(" checking children of" + s.getName() + " " +
         // s.getClass());
         State result = null;
         Iterator i = ((CompositeState) s).getChildren().iterator();
         // System.out.println("Looking for " + transId + ", parent: " +
         // s.getName() + " ");
         while (i.hasNext())
         {
            State s2 = (State) i.next();
            // System.out.println(" trying "+ s2.getName() + " ");
            result = recursivelyFindTargetState(s2, transId);
            if (null != result)
            {
               return result;
            }
         }
      } else if (s instanceof SimpleState)
      {
         if (((SimpleState) s).hasIncoming(transId))
         {
            return s;
         }
      } else if (s instanceof JunctionState)
      {
         if (((JunctionState) s).hasIncoming(transId))
         {
            return s;
         }
      } else if (s instanceof FinalState)
      {
         if (((FinalState) s).hasIncoming(transId))
         {
            return s;
         }
      } else if (s instanceof DeepHistoryState)
      {
         if (((DeepHistoryState) s).hasIncoming(transId))
         {
            return s;
         }
      }
      return null;
   }

   // Test method for printing out the contents of this state machine.
   public void print(PrintStream ps)
   {
      ps.println("State Machine: " + name);
      for (int i = 0; i < states.size(); ++i)
      {
         // ps.println("Top level state " + i + ":");
         printOneState(ps, (State) states.get(i), 0);
      }
   }

   // Test method to print out info on a single state.
   private void printOneState(PrintStream ps, State s, int indent_)
   {
      indent(ps, indent_);
      ps.print("State: " + s.name);
      ps.print(" ");
      if (s instanceof SubmachineState)
      {
         ps.print("(submachine)");
      } else if (s instanceof CompositeState)
      {
         ps.print("(compound)");
      } else if (s instanceof FinalState)
      {
         ps.print("(final)");
      } else if (s instanceof InitialState)
      {
         ps.print("(initial)");
      } else if (s instanceof JunctionState)
      {
         ps.print("(junction)");
      } else if (s instanceof DeepHistoryState)
      {
         ps.print("(deep history)");
      } else if (s instanceof SimpleState)
      {
         ps.print("(simple)");
      } else if (s instanceof ConcurrentCompositeState)
      {
         ps.print("(concurrent composite)");
      } else if (s instanceof CompositeStateRegion)
      {
         ps.print("(region)");
      }
      ps.println("");
      printActions(ps, s, indent_);
      if ((s instanceof CompositeState || s instanceof SimpleState
           || s instanceof FinalState || s instanceof JunctionState || s instanceof DeepHistoryState)
          && (null != s.getIncoming() && !s.getIncoming().isEmpty()))
      {
         printTransitions(ps, s.getIncoming(), "Incoming Transitions:", indent_);
      }
      if ((s instanceof CompositeState || s instanceof SimpleState
           || s instanceof InitialState || s instanceof JunctionState)
          && (null != s.getOutgoing() && !s.getOutgoing().isEmpty()))
      {
         printTransitions(ps, s.getOutgoing(), "Outgoing Transitions:", indent_);
      }
      ps.println("");
      if (s instanceof CompositeState)
      {
         if (null != ((CompositeState) s).getChildren())
         {
            Collection children = ((CompositeState) s).getChildren();
            Iterator i = children.iterator();
            while (i.hasNext())
            {
               int subIndent = indent_ + 4;
               State st = (State) i.next();
               printOneState(ps, st, subIndent);
            }
         }
      }
   }

   // Test method to print out info on all transitions contained by this state
   // machine.
   private void printTransitions(PrintStream ps, List<Transition> transitions,
                                 String inOrOut, int indent_)
   {
      indent(ps, indent_);
      ps.println(inOrOut);
      for (int i = 0; i < transitions.size(); ++i)
      {
         indent(ps, indent_ + 2);
         Transition tran = transitions.get(i);
         ps.println("Transition ID=" + tran.id() + "");
      }
   }

   // Prints out info on all actions contained by this state machine.
   private void printActions(PrintStream ps, State s, int indent_)
   {
      Iterator iter;
      if (null != s.getEntryActions())
      {
         iter = s.getEntryActions().iterator();
         while (iter.hasNext())
         {
            indent(ps, indent_);
            Action action = (Action) iter.next();
            ps.println("Entry action: " + action);
         }
      }
      if (null != s.getDuringActions())
      {
         iter = s.getDuringActions().iterator();
         while (iter.hasNext())
         {
            indent(ps, indent_);
            Action action = (Action) iter.next();
            ps.println("During action: " + action);
         }
      }
      if (null != s.getExitActions())
      {
         iter = s.getExitActions().iterator();
         while (iter.hasNext())
         {
            indent(ps, indent_);
            Action action = (Action) iter.next();
            ps.println("Exit action: " + action);
         }
      }
   }

   // Prints the specified number of spaces in order to indent.
   private void indent(PrintStream ps, int i)
   {
      for (int j = 0; j < i; ++j)
      {
         ps.print(" ");
      }
   }

   // Checks if this state machine contains a history state.
   public boolean hasHistoryState()
   {
      for (State s : states)
      {
         if (s instanceof DeepHistoryState)
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Return a map of pathnames to region
    */
   public Map<String, CompositeStateRegion> getRegions()
   {
      return getRegions( name() );
   }

   public Map<String, CompositeStateRegion> getRegions( String path )
   {
      Map<String, CompositeStateRegion> regions = 
         new HashMap<String, CompositeStateRegion>();

      for ( State state : states )
      {
         getRegions( state, path, regions );
      }

      return regions;
   }

   protected void getRegions( State state, String path, Map<String, CompositeStateRegion> regions )
   {
      // Extend the path to include the current state.       
      String statePath = path + ":" + state.name();

      // These tests must be in order since ConcurrentCompositeState is a
      // subclass of CompositeState.  This code really needs to be
      // refactored to break all the "instanceof" antipatterns...
      if ( state instanceof SubmachineState )
      {
         SubmachineState submachine = (SubmachineState) state;
         for ( State child : submachine.getStateMachine().states())
         {
            getRegions( child, statePath, regions );
         }
      }

      if ( state instanceof ConcurrentCompositeState )
      {
         ConcurrentCompositeState composite = 
            (ConcurrentCompositeState) state;
         
         for ( CompositeStateRegion subregion : composite.getAllSubRegions() )
         {
            String regionPath = statePath + ":" + subregion.name();
            regions.put( regionPath, subregion );
            
            for ( State child : subregion.getChildren() ) 
            {
               getRegions( child, regionPath, regions );
            }
         }

         return;
      }

      if ( state instanceof CompositeState )
      {
         for ( State child : ((CompositeState) state).getChildren() )
         {
            getRegions(child, statePath, regions );
         }
      }      
   }

   
   public Map<String, State> getTerminalStates()
   {
      Map<String, State> terminalStates = new HashMap<String, State>();

      for ( State state : states )
      {
         getTerminalStates( state, name(), terminalStates );
      }

      return terminalStates;
   }

   protected List<State> getTerminalStatesForRegion( CompositeStateRegion region ) 
   {
      Map<String, State> terminalStates = new HashMap<String, State>();

      for ( State state : region.getChildren() ) 
      {
         getTerminalStates( state, "", terminalStates );
      }

      return new ArrayList<State>( terminalStates.values() );
   }
 

   /**
    * Recursively find all the states from a starting state.  A unique path
    * to each state is built up and a path->state mapping is built up.
    * This allows for each state to be uniquely identified.
    *
    * If a duplicate path name is discovered, an exception is thrown.
    * 
    * @param state the root state of the DOM
    * @param path  the root prefic of the current state
    * @param terminalState a collection of path:state pairs
    */
   protected void getTerminalStates( State state, String path, Map<String, State> terminalStates ) 
   {
      // Extend the path to include the current state.       
      String statePath = path + ":" + state.name();

      // Submachines are a special case where we write the composite
      // state itself, we also have to decend into the submachine and
      // get all of its terminal states.  This is done recursively       
      if( state instanceof SubmachineState )
      {
         if ( terminalStates.containsKey( statePath ))
         {
            throw new RuntimeException( "Duplicate state path : " + statePath );
         }

         terminalStates.put( statePath, state );

         // Get the submachine's state machine and recurse
         StateMachine submachine = ((SubmachineState) state).getStateMachine();

         for (State subState : submachine.states()) 
         {
            getTerminalStates( subState, statePath, terminalStates);
         }
      }
      
      // Composite states may be concurrent, or not.  Either way
      // we have to account for the conposite state itself and then
      // decide how to handle the states contained within.
      else if ( state instanceof CompositeState )
      {
         if ( terminalStates.containsKey( statePath ))
         {
            throw new RuntimeException( "Duplicate state path : " + statePath );
         }

         terminalStates.put( statePath, state );

         // If it is a concurrent state, place each region in its
         // own namespace, otherwise recurse on the individual child states
         if ( state instanceof ConcurrentCompositeState )
         {
            ConcurrentCompositeState composite = 
               (ConcurrentCompositeState) state;

            for ( CompositeStateRegion subregion : composite.getAllSubRegions() )
            {
               String regionPath = statePath + ":" + subregion.name();

               for ( State child : subregion.getChildren() ) 
               {
                  getTerminalStates( child, regionPath, terminalStates );
               }
            }
         }
         else
         {
            for ( State child : ((CompositeState) state).getChildren() )
            {
               getTerminalStates(child, statePath, terminalStates);
            }
         }
      }
      else if(state instanceof SimpleState)
      {
         if ( terminalStates.containsKey( statePath ))
         {
            throw new RuntimeException( "Duplicate state path : " + statePath );
         }

         terminalStates.put( statePath, state);
      }
   }

   public List<CompositeState> getHistoryContainers()
   {
      return getHistoryContainers( states() );
   }

   public List<CompositeState> getHistoryContainers( State state )
   {
      return getHistoryContainers( Collections.singletonList( state ));
   }

   public List<CompositeState> getHistoryContainers( List<State> stateList )
   {
      List<CompositeState> historyStates = new ArrayList<CompositeState>();

      for ( State s : stateList )
      {         
         if ( !(s instanceof CompositeState ))
            continue;

         CompositeState comp = (CompositeState) s;

         if ( comp.containsHistoryState() )
         {
            historyStates.add( comp );
         }

         if (!(s instanceof ConcurrentCompositeState))
         {
            historyStates.addAll( getHistoryContainers( comp.getChildren() ));
         }         
      }

      return historyStates;
   }

   protected List<State> getTimerStates()
   {
      List<State> timedStates = new ArrayList<State>();

      for( State s: states() )
      {
         getTimerStates(s, timedStates);
      }

      return timedStates;
   }

   public List<State> getTimerStates( State state )
   {
      List<State> timedStates = new ArrayList<State>();

      getTimerStates( state, new ArrayList<State>() );

      return timedStates;
   }

   protected void getTimerStates( State state, List<State> timerStates )
   {
      if(state instanceof CompositeState && 
         !(state instanceof ConcurrentCompositeState) &&
         !(state instanceof SubmachineState))
      {
         // don't write composite states themselves
         CompositeState comp = (CompositeState) state;
         if(comp.hasTimeout())
         {
            timerStates.add(comp);
         }
         for(State child : comp.getChildren()  )
         {
            getTimerStates(child, timerStates);
         }
      }
      else if(state instanceof ConcurrentCompositeState)
      {
         ConcurrentCompositeState comp = (ConcurrentCompositeState) state;
         if(comp.hasTimeout())
         {
            timerStates.add(state);
         }
      }
      else if ( state instanceof SubmachineState )
      {
         SubmachineState submachine = (SubmachineState) state;

         if (submachine.hasTimeout())
         {
            timerStates.add(state);
         }

         for ( State subState : submachine.getStateMachine().states() )
         {
            getTimerStates( subState, timerStates );
         }
      }

      else if(state instanceof SimpleState)
      {
         SimpleState simple = (SimpleState) state;
         if(simple.hasTimeout())
         {
            timerStates.add(state);
         }
      }      
   }

   /**
    * Finds the top-level initial state of the state machine.  If no
    * initial state is specified, then null is returned.  It is usually
    * an error to not specify an initial state, but there are certain cases
    * where it is allowed, such as submachines with alternate entry points
    * defined. 
    */
   public InitialState getInitialState()
   {
      return getInitialState( states() );
   }

   /**
    * By default this method searches all the children of the passed state
    */
   public InitialState getInitialState( State state )
   {
      return getInitialState( Collections.singletonList( state ), 2 );
   }

   /**
    * Does a breath-first search of the state tree looking for the first initial
    * state.
    * 
    * @param stateTree Collection of states
    * @param maxDepth Maximum depth to search.  By deafault this is set to
    *                 one
    *
    * @return State Found intial state, or <B>null</B> if not found in tree.
    */
   public InitialState getInitialState( Collection<State> stateTree )
   {
      return getInitialState( stateTree, 0, 1 );
   }

   protected InitialState getInitialState( Collection<State> stateTree, int maxDepth )
   {
      return getInitialState( stateTree, 0, maxDepth );
   }

   protected InitialState getInitialState( Collection<State> stateTree, int depth, int maxDepth )
   {
      // If we have not found an initial state by the time we hit the
      // maximum search depth, return null
      if ( depth >= maxDepth )
      {
         return null;
      }

      // If any of the passed states is an IntialState, return the first
      // one 
      for ( State state : stateTree )
         if (state instanceof InitialState)         
            return (InitialState) state;
      

      // Not found at this level, so search subtree(s):
      for ( State state : stateTree )
      {
         // Only expand Composite States
         if (!(state instanceof CompositeState))
            continue;

         Collection<State> children = ((CompositeState) state).getChildren();

         InitialState initialState = 
            getInitialState( children, depth+1, maxDepth );

         // If we found a state, return it.
         if ( initialState != null )
         {
            return initialState;
         }
      }

      // Initial state not found...
      return null;
   }

    
   /**
    * Test to see if a state is contained within an orthogonal region and,
    * if so, return that region.
    */
   public static CompositeStateRegion getEnclosingRegion( State state )
   {
      // Walk up the hierarchy and return the first CompositeStateRegion
      if ( state == null )
         return null;
 
      if ( state instanceof CompositeStateRegion )
         return CompositeStateRegion.class.cast( state );
 
      return getEnclosingRegion( state.getParent() );
   }
 
   /**
    * Returns the top-level state that encloses the passed state
    */
   public static State getEnclosingState( State state )
   {
      if ( state == null || state.getParent() == null )
         return state;

      return getEnclosingState( state.getParent() );
   }

   /**

   * Routine to find the qualified name for a state.
   * Convenient in certain cases.
   */
   public String getQualifiedPath( State state, String prefix )
   {
      if ( stateMap == null )
         stateMap = getAllStates( "" );
 
      for ( Map.Entry<String, State> entry : stateMap.entrySet() )
         if ( entry.getValue().equals( state ))
            return prefix + entry.getKey();
 
      return null;
   }
}
