// --------------------------------------------
// Rover.pml
//
// Implementation of Rover state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Rover(chan inp)
{
eventType e;

goto Normal_Init;


CollectingData_Init: Rover_CollectingEntry();
CollectingData:  do
    :: inp?e ->
        if
        :: e.sig == StowArm ->
            Rover_CollectingExit();
            goto Idle_Init;
        :: e.sig == Error ->
            Rover_CollectingExit();
            Rover_NormalExit();
            goto Safe_Init;
        :: else
        fi;
    od;


Driving_Init: Rover_DrivingEntry();
Driving:  do
    :: inp?e ->
        if
        :: e.sig == Stop ->
            Rover_DrivingExit();
            goto Idle_Init;
        :: e.sig == Error ->
            Rover_DrivingExit();
            Rover_NormalExit();
            goto Safe_Init;
        :: else
        fi;
    od;


Normal_Init: Rover_NormalEntry();
Idle_Init: Rover_IdleEntry();
Idle:  do
    :: inp?e ->
        if
        :: e.sig == Drive ->
            Rover_IdleExit();
            goto Driving_Init;
        :: e.sig == DeployArm ->
            Rover_IdleExit();
            goto CollectingData_Init;
        :: e.sig == Error ->
            Rover_IdleExit();
            Rover_NormalExit();
            goto Safe_Init;
        :: else
        fi;
    od;


Safe_Init: Rover_SafeEntry();
Safe:  do
    :: inp?e ->
        if
        :: e.sig == Reset ->
            Rover_SafeExit();
            evQ[Arm_ID]!StowArm;  // Publish(StowArm)
            goto Normal_Init;
        :: else
        fi;
    od;
}
