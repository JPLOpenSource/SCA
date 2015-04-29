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
    run PseudostateEntryFix(evQ[PseudostateEntryFix_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: PseudostateEntryFix@s01 ->
    if
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: PseudostateEntryFix@s02 ->
    if
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  :: PseudostateEntryFix@s1 ->
    if
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  :: PseudostateEntryFix@s2 ->
    if
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev3; printf("*** -> Ev3\n"); }
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  :: PseudostateEntryFix@s3 ->
    if
    :: atomic { evQ[PseudostateEntryFix_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  od
}
