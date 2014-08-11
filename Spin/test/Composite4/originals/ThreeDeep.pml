// --------------------------------------------
// ThreeDeep.pml
//
// Implementation of ThreeDeep state-machine.
// This should be auto-generated
//
// --------------------------------------------
proctype ThreeDeep(chan inp)
{
eventType e;

goto S1_Init;


S1_Init: ThreeDeep_s1Entry();
S11_Init: ThreeDeep_s11Entry();
S111_Init: ThreeDeep_s111Entry();
S111:  do
    :: inp?e ->
        if
        :: e.sig == Ev1 ->
            ThreeDeep_s111Exit();
            goto S112_Init;
        :: else
        fi;
    od;


S112_Init: ThreeDeep_s112Entry();
S112:  do
    :: inp?e ->
        if
        :: e.sig == Ev2 ->
            ThreeDeep_s112Exit();
            goto S111_Init;
        :: e.sig == Ev3 ->
            ThreeDeep_s112Exit();
            ThreeDeep_s11Exit();
            goto S1_Init;
        :: else
        fi;
    od;
}
