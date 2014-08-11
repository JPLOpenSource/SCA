@echo off
:: ===========================================================================
:: Product: QP/C buld script for ARM Cortex-M3, QK port, IAR compiler
:: Last Updated for Version: 4.2.00
:: Date of the Last Update:  Jul 08, 2011
::
::                    Q u a n t u m     L e a P s
::                    ---------------------------
::                    innovating embedded systems
::
:: Copyright (C) 2002-2011 Quantum Leaps, LLC. All rights reserved.
::
:: This software may be distributed and modified under the terms of the GNU
:: General Public License version 2 (GPL) as published by the Free Software
:: Foundation and appearing in the file GPL.TXT included in the packaging of
:: this file. Please note that GPL Section 2[b] requires that all works based
:: on this software must also be made publicly available under the terms of
:: the GPL ("Copyleft").
::
:: Alternatively, this software may be distributed and modified under the
:: terms of Quantum Leaps commercial licenses, which expressly supersede
:: the GPL and are specifically designed for licensees interested in
:: retaining the proprietary status of their code.
::
:: Contact information:
:: Quantum Leaps Web site:  http://www.quantum-leaps.com
:: e-mail:                  info@quantum-leaps.com
:: ===========================================================================
setlocal

:: define the IAR_ARM environment variable to point to the location 
:: where you've installed the IAR toolset or adjust the following 
:: set instruction 
::if "%IAR_ARM%"=="" set IAR_ARM="C:\Program Files\IAR Systems\Embedded Workbench 6.20"
if "%IAR_ARM%"=="" set IAR_ARM="C:\tools\IAR\ARM_KS_6.20"

set PATH=%IAR_ARM%\arm\bin;%IAR_ARM%\common\bin;%PATH%

set CC=iccarm
set ASM=iasmarm
set LIB=iarchive

set QP_INCDIR=..\..\..\..\include
set QP_PRTDIR=.

set ARM_CORE=cortex-m3

if "%1"=="" (
    echo default selected
    set BINDIR=%QP_PRTDIR%\dbg
    set CCFLAGS=-D DEBUG --debug --endian little --cpu %ARM_CORE% -e --fpu None --dlib_config %IAR_ARM%\ARM\INC\c\DLib_Config_Normal.h --diag_suppress Pa050 -Ohz
    set ASMFLAGS=-s+ -w+ -r --cpu %ARM_CORE% --fpu None -I%IAR_ARM%\ARM\INC\ 
)
if "%1"=="rel" (
    echo rel selected
    set BINDIR=%QP_PRTDIR%\rel
    set CCFLAGS=-D NDEBUG --endian little --cpu %ARM_CORE% -e --fpu None --dlib_config %IAR_ARM%\ARM\INC\c\DLib_Config_Normal.h --diag_suppress Pa050 -Ohz
    set ASMFLAGS=-s+ -w+ -r --cpu %ARM_CORE% --fpu None -I%IAR_ARM%\ARM\INC\ 
)
if "%1"=="spy" (
    echo spy selected
    set BINDIR=%QP_PRTDIR%\spy
    set CCFLAGS=-D Q_SPY -D DEBUG --debug --endian little --cpu %ARM_CORE% -e --fpu None --dlib_config %IAR_ARM%\ARM\INC\c\DLib_Config_Normal.h --diag_suppress Pa050 -Ohz
    set ASMFLAGS=-s+ -w+ -r --cpu %ARM_CORE% --fpu None -I%IAR_ARM%\ARM\INC\ 
)

set LIBDIR=%BINDIR%
set LIBFLAGS=

:: QEP ----------------------------------------------------------------------
set SRCDIR=..\..\..\..\qep\source
set CCINC=-I%QP_PRTDIR% -I%QP_INCDIR% -I%SRCDIR%

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qep.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qfsm_ini.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qfsm_dis.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qhsm_ini.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qhsm_dis.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qhsm_top.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qhsm_in.c

erase %LIBDIR%\libqep_%ARM_CORE%.a
%LIB% %LIBFLAGS% -o%LIBDIR%\libqep_%ARM_CORE%.a %BINDIR%\qep.o %BINDIR%\qfsm_ini.o %BINDIR%\qfsm_dis.o %BINDIR%\qhsm_ini.o %BINDIR%\qhsm_dis.o %BINDIR%\qhsm_top.o %BINDIR%\qhsm_in.o
@echo off

:: QF -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\qf\source
set CCINC=-I%QP_PRTDIR% -I%QP_INCDIR% -I%SRCDIR%

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_defer.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_fifo.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_lifo.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_get_.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_sub.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_usub.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qa_usuba.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qeq_fifo.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qeq_get.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qeq_init.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qeq_lifo.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_act.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_gc.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_log2.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_new.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_pool.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_psini.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_pspub.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_pwr2.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qf_tick.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qmp_get.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qmp_init.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qmp_put.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qte_ctor.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qte_arm.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qte_darm.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qte_rarm.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qte_ctr.c

erase %LIBDIR%\libqf_%ARM_CORE%.a
%LIB% %LIBFLAGS% -o%LIBDIR%\libqf_%ARM_CORE%.a %BINDIR%\qa_defer.o %BINDIR%\qa_fifo.o %BINDIR%\qa_lifo.o %BINDIR%\qa_get_.o %BINDIR%\qa_sub.o %BINDIR%\qa_usub.o %BINDIR%\qa_usuba.o %BINDIR%\qeq_fifo.o %BINDIR%\qeq_get.o %BINDIR%\qeq_init.o %BINDIR%\qeq_lifo.o %BINDIR%\qf_act.o %BINDIR%\qf_gc.o %BINDIR%\qf_log2.o %BINDIR%\qf_new.o %BINDIR%\qf_pool.o %BINDIR%\qf_psini.o %BINDIR%\qf_pspub.o %BINDIR%\qf_pwr2.o %BINDIR%\qf_tick.o %BINDIR%\qmp_get.o %BINDIR%\qmp_init.o %BINDIR%\qmp_put.o %BINDIR%\qte_ctor.o %BINDIR%\qte_arm.o %BINDIR%\qte_darm.o %BINDIR%\qte_rarm.o %BINDIR%\qte_ctr.o
@echo off

:: QK -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\qk\source
set CCINC=-I%QP_PRTDIR% -I%QP_INCDIR% -I%SRCDIR%

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qk.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qk_sched.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qk_mutex.c
%ASM% %ASMFLAGS%  -o %BINDIR%\qk_port.o src\qk_port.s

erase %LIBDIR%\libqk_%ARM_CORE%.a
%LIB% %LIBFLAGS% %LIBDIR%\libqk_%ARM_CORE%.a %BINDIR%\qk.o %BINDIR%\qk_sched.o %BINDIR%\qk_mutex.o %BINDIR%\qk_port.o
@echo off

if not "%1"=="spy" goto clean

:: QS -----------------------------------------------------------------------
set SRCDIR=..\..\..\..\qs\source
set CCINC=-I%QP_PRTDIR% -I%QP_INCDIR% -I%SRCDIR%

@echo on
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_blk.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_byte.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_f32.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_f64.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_mem.c
%CC% %CCFLAGS% %CCINC% -o%BINDIR%\ %SRCDIR%\qs_str.c

erase %LIBDIR%\libqs_%ARM_CORE%.a
%LIB% %LIBFLAGS% -o%LIBDIR%\libqs_%ARM_CORE%.a %BINDIR%\qs.o %BINDIR%\qs_.o %BINDIR%\qs_blk.o %BINDIR%\qs_byte.o %BINDIR%\qs_f32.o %BINDIR%\qs_f64.o %BINDIR%\qs_mem.o %BINDIR%\qs_str.o
@echo off

:clean
@echo off
erase %BINDIR%\*.o

endlocal