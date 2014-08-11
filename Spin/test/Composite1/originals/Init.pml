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
    run Hsm(evQ[Hsm_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Hsm@s11 ->
    if
    :: atomic { evQ[Hsm_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Hsm_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  :: Hsm@s12 ->
    if
    :: atomic { evQ[Hsm_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Hsm_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  :: Hsm@s2 ->
    if
    :: atomic { evQ[Hsm_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[Hsm_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  od
}
