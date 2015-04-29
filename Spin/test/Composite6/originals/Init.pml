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
    run RepeatAction(evQ[RepeatAction_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: RepeatAction@s11 ->
    if
    :: atomic { evQ[RepeatAction_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[RepeatAction_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  :: RepeatAction@s12 ->
    if
    :: atomic { evQ[RepeatAction_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[RepeatAction_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  :: RepeatAction@s2 ->
    if
    :: atomic { evQ[RepeatAction_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[RepeatAction_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[RepeatAction_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[RepeatAction_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  od
}
