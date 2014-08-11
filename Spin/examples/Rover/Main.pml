// --------------------------------------------
// Main.pml
//
// Main Promela Driver
// This should be auto-generated
//
// --------------------------------------------

// List of State-machine events
#define DeployArm 1
#define Drive 2
#define Error 3
#define Reset 4
#define Stop 5
#define StowArm 6

// Define an Event Type
typedef eventType {byte sig};

// Include manually-generated Promela routines
#include "ManStubs.pml"

// Event Queue definitions
#define EV_Q_SIZE 2
#define Arm_ID 0
#define Rover_ID 1
#define SM_MAX 2
chan evQ[SM_MAX] = [EV_Q_SIZE] of {eventType};

// Include all the state-machine models
#include "Rover.pml"
#include "Arm.pml"

// Include the init proc - which should be the driver.
#include "Init.pml"
