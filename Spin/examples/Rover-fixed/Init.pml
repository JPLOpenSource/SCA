// --------------------------------------------
// Init.pml
//
// Init proc.
// Manually generated driver.
//
// --------------------------------------------

init
{
  atomic {
    run Arm(evQ[Arm_ID]);
    run Rover(evQ[Rover_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do

  :: Arm@Deployed ->
    if
    :: atomic { evQ[Arm_ID]!StowArm; evQ[Rover_ID]!StowArm; printf("*** -> StowArm\n"); }
    fi
  :: Arm@Stowed ->
    if
    :: atomic { evQ[Arm_ID]!DeployArm; evQ[Rover_ID]!DeployArm; printf("*** -> DeployArm\n"); }
    fi
  :: Rover@CollectingData ->
    if
    :: atomic { evQ[Rover_ID]!Error; printf("*** -> Error\n"); }
    :: atomic { evQ[Rover_ID]!StowArm; evQ[Arm_ID]!StowArm; printf("*** -> StowArm\n"); }
    fi
  :: Rover@DriveWaiting ->
    if
    :: atomic { evQ[Rover_ID]!Error; printf("*** -> Error\n"); }
    fi
  :: Rover@Driving ->
    if
    :: atomic { evQ[Rover_ID]!Error; printf("*** -> Error\n"); }
    :: atomic { evQ[Rover_ID]!Stop; printf("*** -> Stop\n"); }
    fi
  :: Rover@Idle ->
    if
    :: atomic { evQ[Rover_ID]!DeployArm; evQ[Arm_ID]!DeployArm; printf("*** -> DeployArm\n"); }
    :: atomic { evQ[Rover_ID]!Drive; printf("*** -> Drive\n"); }
    :: atomic { evQ[Rover_ID]!Error; printf("*** -> Error\n"); }
    fi
  :: Rover@Safe ->
    if
    :: atomic { evQ[Rover_ID]!Reset; printf("*** -> Reset\n"); }
    fi
  od
}
