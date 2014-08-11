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
val D1 = Map("Rover_NormalEntry"  -> "Enter(Rover, Normal)",
             "Rover_NormalExit" -> "Exit(Rover, Normal)",
             "Rover_IdleEntry"   -> "Enter(Rover, Idle)",
             "Rover_IdleExit"  -> "Exit(Rover, Idle)",
             "Rover_CollectingEntry"  -> "Enter(Rover, Collecting)",
             "Rover_CollectingExit"   -> "Exit(Rover, Collecting)",
             "Rover_DrivingEntry" -> "Enter(Rover, Driving)",
             "Rover_DrivingExit" ->  "Exit(Rover, Driving)",
             "Rover_SafeEntry" -> "Enter(Rover, Safe)",
             "Rover_SafeExit" -> "Exit(Rover, Safe)",
             "Rover_DriveWaitingEntry" -> "Enter(Rover, DriveWaiting)",
             "Rover_DriveWaitingExit" -> "Exit(Rover, DriveWaiting)",
             "Arm_DeployWaitingEntry" -> "Enter(Arm, DeployWaiting)",
             "Arm_DeployWaitingExit" -> "Exit(Arm, DeployWaiting)",
             "Arm_StowedEntry" -> "Enter(Arm, Stowed)",
             "Arm_StowedExit" -> "Exit(Arm, Stowed)",
             "Arm_DeployedEntry" -> "Enter(Arm, Deployed)",
             "Arm_DeployedExit" ->  "Exit(Arm, Deployed)")

// This initial function is expected by the LogScope spec
println("Init()")
             
for (line <- Source.stdin.getLines())
  for (key <- D1.keys)
    if (line.contains(key))
      println(D1(key))  


