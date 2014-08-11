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
#define s111TimerEv 3
#define s112TimerEv 4
#define s114TimerEv 5
#define s11TimerEv 6
#define s12TimerEv 7
#define s1TimerEv 8
#define s2TimerEv 9

// Define an Event Type
typedef eventType {byte sig};

// Include manually-generated Promela routines
#include "ManStubs.pml"

// Event Queue definitions
#define EV_Q_SIZE 1
#define Timers_ID 0
#define SM_MAX 2
chan evQ[SM_MAX] = [EV_Q_SIZE] of {eventType};

// Include all the state-machine models
#include "Timers.pml"

// Include the init proc - which should be the driver.
#include "Init.pml"
