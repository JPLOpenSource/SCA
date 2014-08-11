This example uses the reduced pattern for TimeEvents that does not involve an
actual counter, thereby avoiding state explosion.  The trick is to do a priori
analysis over the model to eliminate time event transitions that are,
effectively, never taken.

So, several expect output files each verifies that a particular timer event
is never seen within an "endless" spin run,
