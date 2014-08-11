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
    run Test(evQ[Test_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Test@s1 ->
    if
    :: atomic { evQ[Test_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  :: Test@s2 ->
    if
    :: atomic { evQ[Test_ID]!Ev2; printf("*** -> Ev2\n"); }
    fi
  :: Test@s3 ->
    if
    :: atomic { evQ[Test_ID]!Ev1; printf("*** -> Ev1\n"); }
    fi
  od
}
