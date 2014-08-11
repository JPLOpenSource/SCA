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
    run ThreeDeep(evQ[ThreeDeep_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: ThreeDeep@S111 ->
    if
    :: atomic { evQ[ThreeDeep_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: ThreeDeep@S112 ->
    if
    :: atomic { evQ[ThreeDeep_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[ThreeDeep_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  od
}
