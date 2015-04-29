// --------------------------------------------
// Simple.pml
//
// Implementation of Simple state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Simple(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Simple_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Simple_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: Simple_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            Simple_s2Exit();
            goto s1_Init;
        :: e.sig == Ev3 ->
            Simple_s2Process();
        :: else
        fi;
    od;
}
