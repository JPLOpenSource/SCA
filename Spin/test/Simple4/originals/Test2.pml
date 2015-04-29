// --------------------------------------------
// Test2.pml
//
// Implementation of Test2 state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Test2(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Test2_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            Test2_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: Test2_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev4 ->
            Test2_s2Exit();
            goto s1_Init;
        :: else
        fi;
    od;
}
