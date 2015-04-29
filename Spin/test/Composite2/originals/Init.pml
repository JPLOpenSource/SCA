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
    run Hsm2(evQ[Hsm2_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Hsm2@s11 ->
    if
    :: atomic { evQ[Hsm2_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev4; printf("*** -> Ev4\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev5; printf("*** -> Ev5\n"); }
    fi
  :: Hsm2@s12 ->
    if
    :: atomic { evQ[Hsm2_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev5; printf("*** -> Ev5\n"); }
    fi
  :: Hsm2@s2 ->
    if
    :: atomic { evQ[Hsm2_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  :: Hsm2@s311 ->
    if
    :: atomic { evQ[Hsm2_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Hsm2_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  od
}
