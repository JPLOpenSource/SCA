// --------------------------------------------
// Hsm.pml
//
// Implementation of Hsm state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Hsm(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Hsm_s1Enter();
s11_Init: Hsm_s11Enter();
s11:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Hsm_s11Exit();
            goto s12_Init;
        :: e.sig == Ev2 ->
            Hsm_s11Exit();
            Hsm_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s12_Init: Hsm_s12Enter();
s12:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            Hsm_s12Exit();
            goto s11_Init;
        :: e.sig == Ev2 ->
            Hsm_s12Exit();
            Hsm_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: Hsm_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            Hsm_s2Exit();
            goto s1_Init;
        :: e.sig == Ev4 ->
            Hsm_s2Exit();
            Hsm_s1Enter();
            goto s12_Init;
        :: else
        fi;
    od;
}
