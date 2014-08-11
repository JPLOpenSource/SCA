// --------------------------------------------
// Guard.pml
//
// Implementation of Guard state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Guard(chan inp)
{
eventType e;
bool guard_return;

goto s1_Init;


s1_Init: Guard_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Guard_myGuard(guard_return);
            if
            :: guard_return ->
                Guard_s1Exit();
                goto s2_Init;
            :: else
            fi;
        :: else
        fi;
    od;


s2_Init: Guard_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            Guard_s2Exit();
            goto s1_Init;
        :: e.sig == Ev3 ->
            Guard_s2Process();
        :: else
        fi;
    od;
}
