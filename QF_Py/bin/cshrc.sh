####################################################################
#
# Set up the PYTHONPATH variables to include bin, src, and lib.
#
# Source this file from directory <SCAutocoder>/QF_Py/bin; or,
# if used in .cshrc, set up QFROOT to point to <SCAutocoder>/QF_Py
# and source this file.
#
####################################################################

if ( $?QFROOT ) then 
    set QF_PY_PWD=$QFROOT
else
    if (-e ../../QF_Py) then
        # Good, we're most likely in "bin"
        cd ../../QF_Py
    else if (-e ../QF_Py/bin) then
        # Good, already in ST_PWD root, but just to be sure
        cd ../QF_Py
    else  # OK, don't know where we are, can't continue!
        echo "ERROR! Either define QFROOT, or cd to QF_Py/bin to source this file."
        exit -1
    endif
    set QF_PY_PWD=`pwd`
    cd -
endif

set QF_PY_SRC=${QF_PY_PWD}/src
set QF_PY_BIN=${QF_PY_PWD}/bin
set QF_PY_LIB=${QF_PY_PWD}/lib

# make sure we don't add redundant paths
setenv PYTHONPATH `echo $PYTHONPATH | sed s=${QF_PY_BIN}==g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s/::/:/g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s=${QF_PY_SRC}==g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s/::/:/g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s=${QF_PY_LIB}==g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s/::/:/g`
setenv PYTHONPATH `echo $PYTHONPATH | sed s/^://`
setenv PYTHONPATH `echo $PYTHONPATH | sed s/:\$//`
if ( $?PYTHONPATH && "$PYTHONPATH" != "" ) then
    setenv PYTHONPATH ${PYTHONPATH}:${QF_PY_BIN}:${QF_PY_SRC}:${QF_PY_LIB}
else
    setenv PYTHONPATH ${QF_PY_BIN}:${QF_PY_SRC}:${QF_PY_LIB}
endif
