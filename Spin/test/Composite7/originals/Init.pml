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
    run InternalTransFix(evQ[InternalTransFix_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: InternalTransFix@s11 ->
    if
    :: atomic { evQ[InternalTransFix_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  :: InternalTransFix@s12 ->
    if
    :: atomic { evQ[InternalTransFix_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  :: InternalTransFix@s13 ->
    if
    :: atomic { evQ[InternalTransFix_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[InternalTransFix_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  od
}
