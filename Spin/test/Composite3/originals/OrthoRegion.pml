// --------------------------------------------
// OrthoRegion.pml
//
// Implementation of OrthoRegion state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype OrthoRegion(chan inp)
{
eventType e;
// Rendezvous point for orthogonal regions in state s2
chan s2RegionChan[2] = [0] of {eventType};

// Initiate Orthogonal region OrthoRegions2Region1; blocks until ACTIVATE signal.
run OrthoRegions2Region1(s2RegionChan[0]);
// Initiate Orthogonal region OrthoRegions2Region2; blocks until ACTIVATE signal.
run OrthoRegions2Region2(s2RegionChan[1]);

goto s1_Init;


s1_Init: OrthoRegion_s1Enter();
s1:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            OrthoRegion_s1Exit();
            goto s2_Init;
        :: else
        fi;
    od;


s2_Init: OrthoRegion_s2Enter();
    s2RegionChan[0]!ACTIVATE;
    s2RegionChan[1]!ACTIVATE;
s2:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            s2RegionChan[0]!EXIT;
            s2RegionChan[1]!EXIT;
            OrthoRegion_s2Exit();
            goto s1_Init;
        :: e.sig == Ev2 ->
            s2RegionChan[0]!e;  // dispatch to Region1
        :: e.sig == Ev3 ->
            s2RegionChan[1]!e;  // dispatch to Region2
        :: else
        fi;
    od;
}

// -------------------------------------------------------
//
// Orthogonal Region1 for state s2
//
// -------------------------------------------------------

proctype OrthoRegions2Region1(chan inp)
{
eventType e;

goto nullState;

  // Orthogonal Region state-machines are atomic because they don't run
  // as separate execution threads.
  atomic
  {
    nullState: 
        do
        :: inp?e ->
            if
            :: e.sig == ACTIVATE ->
                goto s211_Init;
            :: else
            fi;
        od;


    s211_Init: OrthoRegion_s211Enter();
    s211:  do
        :: inp?e ->
            if
            :: e.sig == Ev2 ->
                OrthoRegion_s211Exit();
                goto s212_Init;
            :: e.sig == EXIT ->
                OrthoRegion_s211Exit();
                goto nullState;
            :: else
            fi;
        od;


    s212_Init: OrthoRegion_s212Enter();
    s212:  do
        :: inp?e ->
            if
            :: e.sig == Ev2 ->
                OrthoRegion_s212Exit();
                goto s211_Init;
            :: e.sig == EXIT ->
                OrthoRegion_s212Exit();
                goto nullState;
            :: else
            fi;
        od;
  }
}

// -------------------------------------------------------
//
// Orthogonal Region2 for state s2
//
// -------------------------------------------------------

proctype OrthoRegions2Region2(chan inp)
{
eventType e;

goto nullState;

  // Orthogonal Region state-machines are atomic because they don't run
  // as separate execution threads.
  atomic
  {
    nullState: 
        do
        :: inp?e ->
            if
            :: e.sig == ACTIVATE ->
                goto s221_Init;
            :: else
            fi;
        od;


    s221_Init: OrthoRegion_s221Enter();
    s221:  do
        :: inp?e ->
            if
            :: e.sig == Ev3 ->
                OrthoRegion_s221Exit();
                goto s222_Init;
            :: e.sig == EXIT ->
                OrthoRegion_s221Exit();
                goto nullState;
            :: else
            fi;
        od;


    s222_Init: OrthoRegion_s222Enter();
    s222:  do
        :: inp?e ->
            if
            :: e.sig == Ev3 ->
                OrthoRegion_s222Exit();
                goto s221_Init;
            :: e.sig == EXIT ->
                OrthoRegion_s222Exit();
                goto nullState;
            :: else
            fi;
        od;
  }
}
