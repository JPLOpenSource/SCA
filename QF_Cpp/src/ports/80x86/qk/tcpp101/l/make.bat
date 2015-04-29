@echo off
rem ==========================================================================
rem Product: QP/C++ buld script for QK port, Turbo C++ 1.01 compiler
rem Last Updated for Version: 4.0.00
rem Date of the Last Update:  Apr 07, 2008
rem
rem                    Q u a n t u m     L e a P s
rem                    ---------------------------
rem                    innovating embedded systems
rem
rem Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
rem
rem This software may be distributed and modified under the terms of the GNU
rem General Public License version 2 (GPL) as published by the Free Software
rem Foundation and appearing in the file GPL.TXT included in the packaging of
rem this file. Please note that GPL Section 2[b] requires that all works based
rem on this software must also be made publicly available under the terms of
rem the GPL ("Copyleft").
rem
rem Alternatively, this software may be distributed and modified under the
rem terms of Quantum Leaps commercial licenses, which expressly supersede
rem the GPL and are specifically designed for licensees interested in
rem retaining the proprietary status of their code.
rem
rem Contact information:
rem Quantum Leaps Web site:  http://www.quantum-leaps.cppom
rem e-mail:                  info@quantum-leaps.cppom
rem ==========================================================================
setlocal

rem adjust the following path to the location where you've installed
rem the Turbo C++ 1.01 toolset...
rem
set TCPP101_DIR=c:\tools\tcpp101\bin

set PATH=%TCPP101_DIR%;%PATH%
set CC=tcc.exe
set LIB=tlib.exe

rem Memory model -- large
set MM=l

set QP_INCDIR=..\..\..\..\..\include
set QP_PRTDIR=.

if "%1"=="" (
    echo default selected
    set BINDIR=%QP_PRTDIR%\dbg
    set CCFLAGS=-c -1 -f287 -v -m%MM%
)
if "%1"=="rel" (
    echo rel selected
    set BINDIR=%QP_PRTDIR%\rel
    set CCFLAGS=-c -1 -f287 -m%MM% -DNDEBUG
)
if "%1"=="spy" (
    echo spy selected
    set BINDIR=%QP_PRTDIR%\spy
    set CCFLAGS=-c -1 -f287 -v -m%MM% -DQ_SPY
)

set LIBDIR=%BINDIR%

rem QEP ----------------------------------------------------------------------
set SRCDIR=..\..\..\..\..\qep\source
set CCINC=@inc_qep.rsp

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qep.obj      %SRCDIR%\qep.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qfsm_ini.obj %SRCDIR%\qfsm_ini.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qfsm_dis.obj %SRCDIR%\qfsm_dis.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qhsm_ini.obj %SRCDIR%\qhsm_ini.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qhsm_dis.obj %SRCDIR%\qhsm_dis.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qhsm_top.obj %SRCDIR%\qhsm_top.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qhsm_in.obj  %SRCDIR%\qhsm_in.cpp

erase %LIBDIR%\qep.lib
%LIB% %LIBDIR%\qep +%BINDIR%\qep
%LIB% %LIBDIR%\qep +%BINDIR%\qfsm_ini
%LIB% %LIBDIR%\qep +%BINDIR%\qfsm_dis
%LIB% %LIBDIR%\qep +%BINDIR%\qhsm_ini
%LIB% %LIBDIR%\qep +%BINDIR%\qhsm_dis
%LIB% %LIBDIR%\qep +%BINDIR%\qhsm_top
%LIB% %LIBDIR%\qep +%BINDIR%\qhsm_in
@echo off

rem QF -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\..\qf\source
set CCINC=@inc_qf.rsp

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_defer.obj %SRCDIR%\qa_defer.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_fifo.obj  %SRCDIR%\qa_fifo.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_lifo.obj  %SRCDIR%\qa_lifo.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_get_.obj  %SRCDIR%\qa_get_.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_sub.obj   %SRCDIR%\qa_sub.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_usub.obj  %SRCDIR%\qa_usub.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qa_usuba.obj %SRCDIR%\qa_usuba.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qeq_fifo.obj %SRCDIR%\qeq_fifo.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qeq_get.obj  %SRCDIR%\qeq_get.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qeq_init.obj %SRCDIR%\qeq_init.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qeq_lifo.obj %SRCDIR%\qeq_lifo.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_act.obj   %SRCDIR%\qf_act.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_gc.obj    %SRCDIR%\qf_gc.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_log2.obj  %SRCDIR%\qf_log2.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_new.obj   %SRCDIR%\qf_new.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_pool.obj  %SRCDIR%\qf_pool.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_psini.obj %SRCDIR%\qf_psini.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_pspub.obj %SRCDIR%\qf_pspub.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_pwr2.obj  %SRCDIR%\qf_pwr2.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qf_tick.obj  %SRCDIR%\qf_tick.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qmp_get.obj  %SRCDIR%\qmp_get.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qmp_init.obj %SRCDIR%\qmp_init.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qmp_put.obj  %SRCDIR%\qmp_put.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qte_ctor.obj %SRCDIR%\qte_ctor.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qte_arm.obj  %SRCDIR%\qte_arm.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qte_darm.obj %SRCDIR%\qte_darm.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qte_rarm.obj %SRCDIR%\qte_rarm.cpp

erase %LIBDIR%\qf.lib
%LIB% %LIBDIR%\qf +%BINDIR%\qa_defer
%LIB% %LIBDIR%\qf +%BINDIR%\qa_fifo
%LIB% %LIBDIR%\qf +%BINDIR%\qa_lifo
%LIB% %LIBDIR%\qf +%BINDIR%\qa_get_
%LIB% %LIBDIR%\qf +%BINDIR%\qa_sub
%LIB% %LIBDIR%\qf +%BINDIR%\qa_usub
%LIB% %LIBDIR%\qf +%BINDIR%\qa_usuba
%LIB% %LIBDIR%\qf +%BINDIR%\qeq_fifo
%LIB% %LIBDIR%\qf +%BINDIR%\qeq_get
%LIB% %LIBDIR%\qf +%BINDIR%\qeq_init
%LIB% %LIBDIR%\qf +%BINDIR%\qeq_lifo
%LIB% %LIBDIR%\qf +%BINDIR%\qf_act
%LIB% %LIBDIR%\qf +%BINDIR%\qf_gc
%LIB% %LIBDIR%\qf +%BINDIR%\qf_log2
%LIB% %LIBDIR%\qf +%BINDIR%\qf_new
%LIB% %LIBDIR%\qf +%BINDIR%\qf_pool
%LIB% %LIBDIR%\qf +%BINDIR%\qf_psini
%LIB% %LIBDIR%\qf +%BINDIR%\qf_pspub
%LIB% %LIBDIR%\qf +%BINDIR%\qf_pwr2
%LIB% %LIBDIR%\qf +%BINDIR%\qf_tick
%LIB% %LIBDIR%\qf +%BINDIR%\qmp_get
%LIB% %LIBDIR%\qf +%BINDIR%\qmp_init
%LIB% %LIBDIR%\qf +%BINDIR%\qmp_put
%LIB% %LIBDIR%\qf +%BINDIR%\qte_ctor
%LIB% %LIBDIR%\qf +%BINDIR%\qte_arm
%LIB% %LIBDIR%\qf +%BINDIR%\qte_darm
%LIB% %LIBDIR%\qf +%BINDIR%\qte_rarm
@echo off

rem QK -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\..\qk\source
set CCINC=@inc_qk.rsp

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qk.obj       %SRCDIR%\qk.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qk_sched.obj %SRCDIR%\qk_sched.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qk_mutex.obj %SRCDIR%\qk_mutex.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qk_ext.obj   %SRCDIR%\qk_ext.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qk_port.obj  src\qk_port.cpp

erase %LIBDIR%\qk.lib
%LIB% %LIBDIR%\qk +%BINDIR%\qk
%LIB% %LIBDIR%\qk +%BINDIR%\qk_sched
%LIB% %LIBDIR%\qk +%BINDIR%\qk_mutex
%LIB% %LIBDIR%\qk +%BINDIR%\qk_ext
%LIB% %LIBDIR%\qk +%BINDIR%\qk_port
%LIB% %LIBDIR%\qk +src\fpu
@echo off

if not "%1"=="spy" goto clean

rem QS -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\..\qs\source
set CCINC=@inc_qs.rsp

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs.obj      %SRCDIR%\qs.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_.obj     %SRCDIR%\qs_.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_blk.obj  %SRCDIR%\qs_blk.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_byte.obj %SRCDIR%\qs_byte.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_f32.obj  %SRCDIR%\qs_f32.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_f64.obj  %SRCDIR%\qs_f64.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_mem.obj  %SRCDIR%\qs_mem.cpp
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\qs_str.obj  %SRCDIR%\qs_str.cpp

erase %LIBDIR%\qs.lib
%LIB% %LIBDIR%\qs +%BINDIR%\qs.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_blk.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_byte.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_f32.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_f64.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_mem.obj
%LIB% %LIBDIR%\qs +%BINDIR%\qs_str.obj
@echo off

:clean
@echo off

erase %BINDIR%\*.obj
erase %LIBDIR%\*.bak

endlocal