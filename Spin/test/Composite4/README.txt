This example verifies that a state labels for a StateMachine hierarchy of
depth greater than 2 is properly handled, so that the top-level ancestor state
does NOT appear more than once.

Original error:  state "Channel" appeared as first label in both if blocks.
