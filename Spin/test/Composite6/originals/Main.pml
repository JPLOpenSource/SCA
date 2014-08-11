// --------------------------------------------
// Main.pml
//
// Main Promela Driver
// This should be auto-generated
//
// --------------------------------------------

// List of State-machine events
#define Ev1 1
#define Ev2 2
#define Ev3 3
#define Ev4 4

// Define an Event Type
typedef eventType {byte sig};

// Include manually-generated Promela routines
#include "ManStubs.pml"

// Event Queue definitions
#define EV_Q_SIZE 1
#define RepeatAction_ID 0
#define SM_MAX 2
chan evQ[SM_MAX] = [EV_Q_SIZE] of {eventType};

// Include all the state-machine models
#include "RepeatAction.pml"

// Include the init proc - which should be the driver.
#include "Init.pml"
