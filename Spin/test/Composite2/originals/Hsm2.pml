// --------------------------------------------
// Hsm2.pml
//
// Implementation of Hsm2 state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype Hsm2(chan inp)
{
eventType e;

goto s1_Init;


s1_Init: Hsm2_s1Enter();
s11_Init: Hsm2_s11Enter();
s11:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Hsm2_s11Exit();
            goto s12_Init;
        :: e.sig == Ev4 ->
            Hsm2_s11Exit();
            Hsm2_s1Exit();
            Hsm2_s3Entry();
            Hsm2_s31Entry();
            goto s311_Init;
        :: e.sig == Ev2 ->
            Hsm2_s11Exit();
            Hsm2_s1Exit();
            goto s2_Init;
        :: e.sig == Ev5 ->
            Hsm2_s11Exit();
            goto s12_Init;
        :: else
        fi;
    od;


s12_Init: Hsm2_s12Enter();
s12:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            Hsm2_s12Exit();
            goto s11_Init;
        :: e.sig == Ev2 ->
            Hsm2_s12Exit();
            Hsm2_s1Exit();
            goto s2_Init;
        :: e.sig == Ev5 ->
            Hsm2_s12Exit();
            goto s12_Init;
        :: else
        fi;
    od;


s2_Init: Hsm2_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev3 ->
            Hsm2_s2Exit();
            goto s1_Init;
        :: e.sig == Ev4 ->
            Hsm2_s2Exit();
            Hsm2_s1Enter();
            goto s12_Init;
        :: e.sig == Ev2 ->
            Hsm2_s2Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s3_Init: Hsm2_s3Entry();
s31_Init: Hsm2_s31Entry();
s311_Init: Hsm2_s311Entry();
s311:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            Hsm2_s311Exit();
            Hsm2_s31Exit();
            Hsm2_s3Exit();
            goto s2_Init;
        :: e.sig == Ev2 ->
            Hsm2_s311Exit();
            Hsm2_s31Exit();
            goto s3_Init;
        :: else
        fi;
    od;
}
