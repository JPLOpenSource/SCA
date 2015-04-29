// --------------------------------------------
// PseudostateEntryFix.pml
//
// Implementation of PseudostateEntryFix state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype PseudostateEntryFix(chan inp)
{
eventType e;
bool guard_return;

goto s1_Init;


s01_Init: PseudostateEntryFix_s01Enter();
s01:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            PseudostateEntryFix_s01Exit();
            PseudostateEntryFix_s0Exit();
            PseudostateEntryFix_s01Ev1Act();
            goto s3_Init;
        :: else
        fi;
    od;


s0_Init: PseudostateEntryFix_s0Enter();
s02_Init: PseudostateEntryFix_s02Enter();
s02:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            PseudostateEntryFix_s02Exit();
            PseudostateEntryFix_s0Exit();
            PseudostateEntryFix_s02Ev2Act();
            goto s2_Init;
        :: else
        fi;
    od;


s1_Init: PseudostateEntryFix_s11Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            PseudostateEntryFix_s11Exit();
            PseudostateEntryFix_s1Ev1Act();
            PseudostateEntryFix_g1True(guard_return);
            if
            :: guard_return ->
                PseudostateEntryFix_s0Exit();
                PseudostateEntryFix_g1Act();
                goto s1_Init;
            PseudostateEntryFix_g2True(guard_return);
            if
            :: guard_return ->
                PseudostateEntryFix_s0Exit();
                PseudostateEntryFix_g2Act();
                goto s01_Init;
            PseudostateEntryFix_g3True(guard_return);
            if
            :: guard_return ->
                PseudostateEntryFix_s0Exit();
                PseudostateEntryFix_g3Act();
                goto s0_Init;
            :: else
            fi
        :: e.sig == Ev3 ->
            PseudostateEntryFix_s11Exit();
            PseudostateEntryFix_s1Ev3Act();
            goto s0_Init;
        :: e.sig == Ev2 ->
            PseudostateEntryFix_s11Exit();
            PseudostateEntryFix_s1Ev2Act();
        :: else
        fi;
    od;


s2_Init: PseudostateEntryFix_s2Enter();
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev4 ->
            PseudostateEntryFix_s2Exit();
            PseudostateEntryFix_s2Ev4Act();
        :: e.sig == Ev3 ->
            PseudostateEntryFix_s2Exit();
            PseudostateEntryFix_s2Ev3Act();
        :: else
        fi;
    od;


s3_Init: PseudostateEntryFix_s12Enter();
s3:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            PseudostateEntryFix_s12Exit();
            PseudostateEntryFix_s3Ev2Act();
            PseudostateEntryFix_g4True(guard_return);
            if
            :: guard_return ->
                PseudostateEntryFix_s0Exit();
                PseudostateEntryFix_g4Act();
                goto s01_Init;
            :: else ->
                PseudostateEntryFix_s0Exit();
                PseudostateEntryFix_elseAct();
                goto s02_Init;
            fi
        :: else
        fi;
    od;
}
