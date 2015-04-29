// ------------------------------------------------------
// Manually coded Promela functions
// ------------------------------------------------------


inline Arm_DeployedEntry()
{
  printf("Arm_DeployedEntry()\n");
}

inline Arm_DeployedExit()
{
  printf("Arm_DeployedExit()\n");
}

inline Arm_roverStationary(retVal)
{
  if 
  :: Rover@Driving -> retVal = false
  :: else -> retVal = true
  fi
}

inline Arm_StowedEntry()
{
  printf("Arm_StowedEntry()\n");
}

inline Arm_StowedExit()
{
  printf("Arm_StowedExit()\n");
}

inline Rover_CollectingEntry()
{
  printf("Rover_CollectingEntry()\n");
}

inline Rover_CollectingExit()
{
  printf("Rover_CollectingExit()\n");
}

inline Rover_collectData()
{
  printf("Rover_collectData()\n");
}

inline Rover_DrivingEntry()
{
  printf("Rover_DrivingEntry()\n");
}

inline Rover_DrivingExit()
{
  printf("Rover_DrivingExit()\n");
}

inline Rover_IdleEntry()
{
  printf("Rover_IdleEntry()\n");
}

inline Rover_IdleExit()
{
  printf("Rover_IdleExit()\n");
}

inline Rover_NormalEntry()
{
  printf("Rover_NormalEntry()\n");
}

inline Rover_NormalExit()
{
  printf("Rover_NormalExit()\n");
}

inline Rover_SafeEntry()
{
  printf("Rover_SafeEntry()\n");
}

inline Rover_SafeExit()
{
  printf("Rover_SafeExit()\n");
}
