When a transition enters a composite state, the entry actions of that state
should be executed after any transition action.  However, when the target of
that transition is a junction (or, more generally, a pseudostate) the entry
action gets omitted.  Conversely, when the source of a transition in a
composite state is a pseudostate, the exit action of that composite state will
be coded out even when the transition is between peer states within that
composite state (as opposed to transition out of the composite state)!

The reason is because pseudostates were not being handled specially in the
algorithm to flatten out the state hierarchy between source and target vertices
and determine the order of the relevant exit and entry actions. 

This unit test confirms the bugfix.
