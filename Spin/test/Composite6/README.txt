This example verifies that a function used multiple times in the same state
is stamped out only ONCE in the ManStubs file.

Original error:  a function stamped out as many times as used in the same state.

In this case, Spin will complain with the following warning:

spin: line  54 "ManStubs.pml", Error: procedure name Test_someAction redefined

So the expect output simplify verifies that this error is never seen.
