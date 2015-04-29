#########################################################################
#
# Set up the PYTHONPATH variables to include bin, src, and lib.
#
# Source this file in the <root>/QF_Py/bin directory or
# if used in .bashrc, then set up QFROOT to point to <root>/QF_Py
# and source this file.
#
#########################################################################

if [ ${QFROOT} ] ; then 
    QF_PY_PWD=$QFROOT
else
    SETDIR=`dirname $BASH_ARGV`
    if [ "$SETDIR" != "." ] ; then  # not executed from <QFROOT>!
        pushd $SETDIR
    fi
    cd ..
    QF_PY_PWD=`pwd`
    cd -
    if [ "$SETDIR" != "." ] ; then
        popd
    fi
fi

QF_PY_SRC=${QF_PY_PWD}/src
QF_PY_BIN=${QF_PY_PWD}/bin
QF_PY_LIB=${QF_PY_PWD}/lib

# make sure we don't add redundant paths
PYTHONPATH=${PYTHONPATH/${QF_PY_BIN}/}
PYTHONPATH=${PYTHONPATH/::/:}
PYTHONPATH=${PYTHONPATH/${QF_PY_SRC}/}
PYTHONPATH=${PYTHONPATH/::/:}
PYTHONPATH=${PYTHONPATH/${QF_PY_LIB}/}
PYTHONPATH=${PYTHONPATH/::/:}
PYTHONPATH=${PYTHONPATH/#:/}
PYTHONPATH=${PYTHONPATH/%:/}
if [ ${PYTHONPATH} ] ; then
    export PYTHONPATH=${PYTHONPATH}:${QF_PY_BIN}:${QF_PY_SRC}:${QF_PY_LIB}
else
    export PYTHONPATH=${QF_PY_BIN}:${QF_PY_SRC}:${QF_PY_LIB}
fi
