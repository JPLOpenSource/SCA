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
    run OrthoRegion(evQ[OrthoRegion_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: OrthoRegion@s1 ->
    if
    :: atomic { evQ[OrthoRegion_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: OrthoRegion@s2 ->
    if
    :: atomic { evQ[OrthoRegion_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[OrthoRegion_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[OrthoRegion_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  od
}
