// --------------------------------------------
// InternalTransFix.pml
//
// Implementation of InternalTransFix state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype InternalTransFix(chan inp)
{
eventType e;

InternalTransFix_s0Enter();
InternalTransFix_s1Enter();
goto s11_Init;


s11_Init: InternalTransFix_s11Enter();
s11:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            InternalTransFix_s11Exit();
            goto s12_Init;
        :: e.sig == Ev2 ->
            InternalTransFix_ev2NotHandled();
        :: e.sig == Ev3 ->
            InternalTransFix_ev3NotHandled();
        :: e.sig == Ev4 ->
            InternalTransFix_ev4NotHandled();
        :: else
        fi;
    od;


s12_Init: InternalTransFix_s12Enter();
s12:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            InternalTransFix_s12Exit();
            goto s11_Init;
        :: e.sig == Ev4 ->
            InternalTransFix_s12Exit();
            goto s13_Init;
        :: e.sig == Ev1 ->
            InternalTransFix_ev1NotHandled();
        :: e.sig == Ev3 ->
            InternalTransFix_ev3NotHandled();
        :: else
        fi;
    od;


s13_Init: InternalTransFix_s13Enter();
s13:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            InternalTransFix_s13Exit();
            goto s12_Init;
        :: e.sig == Ev1 ->
            InternalTransFix_ev1NotHandled();
        :: e.sig == Ev2 ->
            InternalTransFix_ev2NotHandled();
        :: e.sig == Ev4 ->
            InternalTransFix_ev4NotHandled();
        :: else
        fi;
    od;
}
