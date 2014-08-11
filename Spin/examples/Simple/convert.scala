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
val D1 = Map("Test_s1Enter"  -> "Enter(Test, s1)",
             "Test_s11Enter" -> "Enter(Test, s11)",
             "Test_s1Exit"   -> "Exit(Test, s1)",
             "Test_s11Exit"  -> "Exit(Test, s11)",
             "Test_s2Enter"  -> "Enter(Test, s2)",
             "Test_s2Exit"   -> "Exit(Test, s2)")

// This initial function is expected by the LogScope spec
println("Init()")
             
for (line <- Source.stdin.getLines())
  for (key <- D1.keys)
    if (line.contains(key))
      println(D1(key))  


