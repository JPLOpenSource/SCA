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
    run Test1(evQ[Test1_ID]);
    run Test2(evQ[Test2_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Test1@s1 ->
    if
    :: atomic { evQ[Test1_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: Test1@s2 ->
    if
    :: atomic { evQ[Test1_ID]!Ev2; printf("*** -> Ev2\n"); }
    :: atomic { evQ[Test1_ID]!Ev4; evQ[Test2_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  :: Test2@s1 ->
    if
    :: atomic { evQ[Test2_ID]!Ev3; printf("*** -> Ev3\n"); }
    fi
  :: Test2@s2 ->
    if
    :: atomic { evQ[Test2_ID]!Ev4; evQ[Test1_ID]!Ev4; printf("*** -> Ev4\n"); }
    fi
  od
}
