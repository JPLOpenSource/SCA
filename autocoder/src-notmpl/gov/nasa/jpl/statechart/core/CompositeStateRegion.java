package gov.nasa.jpl.statechart.core;

/**
 * <p>
 * A composite state region is an independent orthogonal region within a
 * composite state.
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
 * CVS Identification: $Id: CompositeStateRegion.java,v 1.1.2.1 2005/11/23
 * 18:15:02 kclark Exp $
 * </p>
 */
public class CompositeStateRegion extends CompositeState
{
   private static int regionCount = 0;

   public CompositeStateRegion(String name, String id)
   {
      super();
      this.id = id;
      if (null == name)
      {
         String anonName = new String("Region" + ++regionCount);
         this.name = anonName;
      } else
         this.name = name;

      String initialChar = this.name.substring(0, 1);
      /**
       * Check if the specified name has an initial capital letter. This is
       * required because the generated C++ region class will be named the same,
       * and the corresponding region instance will be named the same except for
       * being all lowercase. So the region class must has at least one capital
       * letter in order to be different from the instance.
       */
      if (!initialChar.equals(initialChar.toUpperCase()))
      {
         String formattedName = new String(initialChar.toUpperCase()
               + this.name.substring(1));
         this.name = formattedName;
         System.out.println("Warning: Composite state region \"" + name
               + "\" changed to \"" + formattedName
               + "\".  Must have initial capital letter.");
      }
   }
}
