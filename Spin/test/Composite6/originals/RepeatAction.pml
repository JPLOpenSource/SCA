// --------------------------------------------
// RepeatAction.pml
//
// Implementation of RepeatAction state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype RepeatAction(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: RepeatAction_s1Enter();
s11_Init: RepeatAction_s11Enter();
s11:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            RepeatAction_s11Exit();
            goto s12_Init;
        :: e.sig == Ev2 ->
            RepeatAction_s11Exit();
            RepeatAction_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s12_Init: RepeatAction_s12Enter();
s12:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            RepeatAction_s12Exit();
            goto s11_Init;
        :: e.sig == Ev2 ->
            RepeatAction_s12Exit();
            RepeatAction_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: RepeatAction_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            RepeatAction_s2Exit();
            goto s1_Init;
        :: e.sig == Ev4 ->
            RepeatAction_s2Exit();
            RepeatAction_s1Enter();
            goto s12_Init;
        :: e.sig == Ev1 ->
            RepeatAction_someAction();
        :: e.sig == Ev2 ->
            RepeatAction_someAction();
        :: else
        fi;
    od;
}
