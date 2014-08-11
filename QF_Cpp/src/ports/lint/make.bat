@echo off
rem ==========================================================================
rem Product: QP/C++ buld script for PC-Lint(TM), Standard C compiler
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
rem Quantum Leaps Web site:  http://www.quantum-leaps.com
rem e-mail:                  info@quantum-leaps.com
rem ==========================================================================
setlocal

set PC_LINT_DIR=c:\tools\Lint

set QP_INCDIR=..\..\include
set QP_PRTDIR=.

if "%1"=="" (
    set LINTFLAGS=+v -zero -i%PC_LINT_DIR%\lnt std.lnt -si4 -ss2 -sp4 -i%QP_PRTDIR%;%QP_INCDIR%
)
if "%1"=="spy" (
    set LINTFLAGS=+v -zero -i%PC_LINT_DIR%\lnt std.lnt -si4 -ss2 -sp4 -i%QP_PRTDIR%;%QP_INCDIR% -DQ_SPY
)

rem QEP ----------------------------------------------------------------------
set QEP_DIR=..\..\qep
%PC_LINT_DIR%\lint-nt %LINTFLAGS% -i%QEP_DIR%\source %QEP_DIR%\lint\opt_qep.lnt -os(lint_qep.txt) %QEP_DIR%\source\*.cpp

rem QF -----------------------------------------------------------------------
set QF_DIR=..\..\qf
%PC_LINT_DIR%\lint-nt %LINTFLAGS% -i%QF_DIR%\source %QF_DIR%\lint\opt_qf.lnt -os(lint_qf.txt) %QF_DIR%\source\*.cpp

rem QK ----------------------------------------------------------------------
set QK_DIR=..\..\qk
%PC_LINT_DIR%\lint-nt %LINTFLAGS% -i%QK_DIR%\source %QK_DIR%\lint\opt_qk.lnt -os(lint_qk.txt) %QK_DIR%\source\*.cpp

rem QS ----------------------------------------------------------------------
set QS_DIR=..\..\qs
%PC_LINT_DIR%\lint-nt %LINTFLAGS% -DQ_SPY -i%QS_DIR%\source %QS_DIR%\lint\opt_qs.lnt -os(lint_qs.txt) %QS_DIR%\source\*.cpp

endlocal
