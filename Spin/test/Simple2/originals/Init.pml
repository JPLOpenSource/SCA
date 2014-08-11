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
    run Guard(evQ[Guard_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Guard@s1 ->
    if
    :: atomic { evQ[Guard_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: Guard@s2 ->
    if
    :: atomic { evQ[Guard_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Guard_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  od
}
