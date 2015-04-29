// --------------------------------------------
// Test.pml
//
// Implementation of Test state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Test(chan inp)
{
eventType e;
bool guard_return;

goto s1_Init;


s1_Init: Test_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Test_s1Exit();
            Test_myGuard(guard_return);
            if
            :: guard_return ->
                goto s2_Init;
            :: else ->
                goto s3_Init;
            fi;
        :: else
        fi;
    od;


s2_Init: Test_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            Test_s2Exit();
            goto s1_Init;
        :: else
        fi;
    od;


s3_Init: Test_s3Enter();
s3:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            goto s2_Init;
        :: else
        fi;
    od;
}
