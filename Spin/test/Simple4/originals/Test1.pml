// --------------------------------------------
// Test1.pml
//
// Implementation of Test1 state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Test1(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Test1_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Test1_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: evQ[Test2_ID]!Ev3;  // Publish(Ev3)
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            evQ[Test1_ID]!Ev4; evQ[Test2_ID]!Ev4;  // Publish(Ev4)
        :: e.sig == Ev4 ->
            Test1_s2Exit();
            goto s1_Init;
        :: else
        fi;
    od;
}
