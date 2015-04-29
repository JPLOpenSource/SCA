#!/bin/sh
#
# Purpose: Verifies autocoded Promela code against originals.
#   Any deviation is reported.
#
# Usage:  ./diffTest.sh
#
# Author:  Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
#################################################################

# First, build all of the test cases
cd `dirname $0`
echo "Invoking spinsuite.py to build all test cases...\c"
../../QF_Py/bin/spinsuite.py -b >/dev/null 2>&1
echo "done"
echo

diffOutFile="diff.out.$$"

# Next, iterate through and diff with originals
testPass=1
for t in `find . -depth 1 -type d -print`; do
  echo "**** Comparing autocodes and originals in '$t' ****";
  cntDiff=`diff -bBwr $t/originals $t/autocode | tee /tmp/$diffOutFile |wc -l`;
  # account for velocity.log, one difference
  if [ $cntDiff -gt 1 ] ; then
    testPass=0
    echo "  ERROR! autocodes diverge from originals! Output below..."
    echo "  -----"
    cat /tmp/$diffOutFile
    echo "  -=-=-"
  fi
done

# Finally, clean up autocoded files
echo
echo "Cleaning up!"
../../QF_Py/bin/spinsuite.py -c >/dev/null 2>&1

echo
if [ $testPass -eq 1 ] ; then
  echo "PASSED"
else
  echo "FAILED"
fi
