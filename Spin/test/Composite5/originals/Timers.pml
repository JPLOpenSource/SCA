// --------------------------------------------
// Timers.pml
//
// Implementation of Timers state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Timers(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Timers_s1Entry();
s11_Init: Timers_s11Entry();
s111_Init: Timers_s111Entry();
s111:  do
    :: inp?e ->
        if
        :: e.sig == s111TimerEv ->
            Timers_s111Exit();
            goto s112_Init;
        :: e.sig == Ev1 ->
            Timers_s111Exit();
            goto s113_Init;
        :: e.sig == s11TimerEv ->
            Timers_s111Exit();
            Timers_s11Exit();
            goto s12_Init;
        :: e.sig == s1TimerEv ->
            Timers_s111Exit();
            Timers_s11Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s112_Init: Timers_s112Entry();
s112:  do
    :: inp?e ->
        if
        :: e.sig == s112TimerEv ->
            Timers_s112Exit();
            goto s114_Init;
        :: e.sig == s11TimerEv ->
            Timers_s112Exit();
            Timers_s11Exit();
            goto s12_Init;
        :: e.sig == s1TimerEv ->
            Timers_s112Exit();
            Timers_s11Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s113_Init: Timers_s113Entry();
s113:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Timers_s113Exit();
            goto s111_Init;
        :: e.sig == s11TimerEv ->
            Timers_s113Exit();
            Timers_s11Exit();
            goto s12_Init;
        :: e.sig == s1TimerEv ->
            Timers_s113Exit();
            Timers_s11Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s114_Init: Timers_s114Entry();
s114:  do
    :: inp?e ->
        if
        :: e.sig == s11TimerEv ->
            Timers_s114Exit();
            Timers_s11Exit();
            goto s12_Init;
        :: e.sig == s1TimerEv ->
            Timers_s114Exit();
            Timers_s11Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s12_Init: Timers_s12Entry();
s12:  do
    :: inp?e ->
        if
        :: e.sig == s1TimerEv ->
            Timers_s12Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s13_Init: Timers_s13Entry();
s13:  do
    :: inp?e ->
        if
        :: e.sig == s1TimerEv ->
            Timers_s13Exit();
            Timers_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: Timers_s2Entry();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Timers_s2Exit();
            goto s1_Init;
        :: e.sig == s2TimerEv ->
            Timers_s2Exit();
            goto s2_Init;
        :: else
        fi;
    od;
}
