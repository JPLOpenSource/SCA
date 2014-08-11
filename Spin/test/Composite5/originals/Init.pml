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
    run Timers(evQ[Timers_ID]);
  }

  // Driver to exercise state-machine with all possible, enabled events
  do
  :: Timers@s111 ->
    if
    :: atomic { evQ[Timers_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Timers_ID]!s111TimerEv; printf("*** -> s111TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s11TimerEv; printf("*** -> s11TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s112 ->
    if
    :: atomic { evQ[Timers_ID]!s112TimerEv; printf("*** -> s112TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s11TimerEv; printf("*** -> s11TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s113 ->
    if
    :: atomic { evQ[Timers_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Timers_ID]!s11TimerEv; printf("*** -> s11TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s114 ->
    if
    :: atomic { evQ[Timers_ID]!s11TimerEv; printf("*** -> s11TimerEv\n"); }
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s12 ->
    if
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s13 ->
    if
    :: atomic { evQ[Timers_ID]!s1TimerEv; printf("*** -> s1TimerEv\n"); }
    fi
  :: Timers@s2 ->
    if
    :: atomic { evQ[Timers_ID]!Ev1; printf("*** -> Ev1\n"); }
    :: atomic { evQ[Timers_ID]!s2TimerEv; printf("*** -> s2TimerEv\n"); }
    fi
  od
}
