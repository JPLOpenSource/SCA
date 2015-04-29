When a composite state defines internal transitions, which serve essentially as
catch-all responses to events that may not be handled by inner states, those
internal transition of the super states are not coded out properly when
the inner states are two levels below the composite state.

Prior to fix, internal transitions were being added to the "seen-it-already"
cache _before_ determining if it applied to a leaf state.

This unit test verifies that those internal transitions are being coded out
properly by checking for the corresponding actions in the spin simulation run.
