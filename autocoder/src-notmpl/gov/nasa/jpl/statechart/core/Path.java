package gov.nasa.jpl.statechart.core;

import java.util.*;

/**
 * This class represents the abstract path from the root of a state machine
 * to a given state.  This is really just a type-safe wrapper around
 * TreePath
 */
public class Path<T>
{
   protected String sep = ":";
   protected String prefix = "";
   private final List<T> path;

   public Path()
   {
      path = new ArrayList<T>();
   }

   public Path( T singlePath )
   {
      this();
      path.add( singlePath );
   }

   public Path( List<T> path )
   {
      this.path = new ArrayList<T>( path );
   }

   public Path<T> getParentPath()
   {
      return new Path<T>( path.subList( 0, path.size() - 1 ));
   }

   public T getLastPathComponent()
   {
      return path.get( path.size() - 1 );
   }

   public T getPathComponent( int index )
   {
      return path.get( index );
   }

   public int length()
   {
      return path.size();
   }

   public void setPathSeparator( String sep )
   {
      this.sep = sep;
   }

   public void setPathPrefix( String prefix )
   {
      this.prefix = prefix;
   }

   public Path<T> add( T child )
   {
      Path<T> newPath = new Path<T>( path );
      newPath.path.add( child );

      return newPath;
   }

   public String toString()
   {
      String str = prefix;

      if ( path.isEmpty() )
         return "";
      
      str += path.get( 0 );
      for ( T item : path.subList( 1, path.size() ))
         str += sep + item;
      
      return str;
   }
}
