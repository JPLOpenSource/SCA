#!/bin/sh
exec scala "$0" "$@"
!#

import scala.io.Source
// -------------------------------------------------------------
// Filename: convert.scala
// 
// Description:
// Reads from stdin.  Prints to stdout.  Any line that contains
// the 'key' in the dictionary, the corresponding string will 
// be printed out.
//
// Usage:
// more <input_log_file> | convert.scala
//
// --------------------------------------------------------------


// Create our Dictionary here for string replacement
//
val D1 = Map("Test s1 ENTRY"  -> "Enter(Test, s1)",
             "Test s1 ENTRY"  -> "Enter(Test, s11)",
             "Test s1 EXIT"   -> "Exit(Test, s1)",
             "Test s1 EXIT"   -> "Exit(Test, s11)",
             "Test s2 ENTRY"  -> "Enter(Test, s2)",
             "Test s2 EXIT"   -> "Exit(Test, s2)")

// This initial function is expected by the LogScope spec
println("Init()")
             
for (line <- Source.stdin.getLines())
  for (key <- D1.keys)
    if (line.contains(key))
      println(D1(key))  


