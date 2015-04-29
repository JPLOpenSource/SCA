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
    run Simple(evQ[Simple_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Simple@s1 ->
    if
    :: atomic { evQ[Simple_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: Simple@s2 ->
    if
    :: atomic { evQ[Simple_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Simple_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  od
}
