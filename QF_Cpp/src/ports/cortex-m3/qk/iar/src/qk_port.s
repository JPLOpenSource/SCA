;*****************************************************************************
; Product: QK port to Cortex-M3, IAR ARM assembler
; Last Updated for Version: 3.5.00
; Date of the Last Update:  Mar 06, 2008
;
;                    Q u a n t u m     L e a P s
;                    ---------------------------
;                    innovating embedded systems
;
; Copyright (C) 2002-2007 Quantum Leaps, LLC. All rights reserved.
;
; This software may be distributed and modified under the terms of the GNU
; General Public License version 2 (GPL) as published by the Free Software
; Foundation and appearing in the file GPL.TXT included in the packaging of
; this file. Please note that GPL Section 2[b] requires that all works based
; on this software must also be made publicly available under the terms of
; the GPL ("Copyleft").
;
; Alternatively, this software may be distributed and modified under the
; terms of Quantum Leaps commercial licenses, which expressly supersede
; the GPL and are specifically designed for licensees interested in
; retaining the proprietary status of their code.
;
; Contact information:
; Quantum Leaps Web site:  http://www.quantum-leaps.com
; e-mail:                  info@quantum-leaps.com
;*****************************************************************************

    RSEG CODE:CODE:NOROOT(2)

    PUBLIC  QK_init
    PUBLIC  QK_PendSV
    PUBLIC  QK_SVCall

    EXTERN  QK_schedule_      ; external references


;*****************************************************************************
;
; The QK_init function sets the priorities of SVCall and PendSV exceptions
; to the lowest level possible (0xFF). The function internally disables
; interrupts, but restores the original interrupt lock before exit.
;
;*****************************************************************************
QK_init
    MRS     r0,PRIMASK        ; store the state of the PRIMASK in r0
    CPSID   i                 ; disable interrupts (set PRIMASK)

    LDR     r1,=0xE000ED18    ; System Handler Priority Register
    LDR     r2,[r1,#4]        ; load the System 8-11 Priority Register
    ORR     r2,r2,#0xFF000000 ; set PRI_11 (SVCall) to 0xFF
    STR     r2,[r1,#4]        ; write the System 8-11 Priority Register
    LDR     r2,[r1,#8]        ; load the System 12-15 Priority Register
    ORR     r2,r2,#0x00FF0000 ; set PRI_14 (PendSV) to 0xFF
    STR     r2,[r1,#8]        ; write the System 12-15 Priority Register

    MSR     PRIMASK,r0        ; restore the original PRIMASK
    BX      lr                ; return to the caller


;*****************************************************************************
;
; The QK_PendSV exception hanlder is used for handling asynchronous
; preemptions in QK. The use of the PendSV exception is the recommended and
; most efficient method for performing context switches with Cortex-M3.
;
; The PendSV exception should have the lowest priority in the whole system
; (0xFF, see QK_init). All other exeptions and interrupts should have higher
; priority. For Cortex-M3 with 3 priority bits, all interrupts and exceptions
; must have numerical value of priority lower than 0xE0. The seven interrupt
; priority levels available to your applications are (in the order from
; the lowest urgency to the highest urgency):
; 0xC0, 0xA0, 0x80, 0x60, 0x40, 0x20, 0x00.
;
; Also, all ISRs in the QK-nano application *MUST* trigger the PendSV
; exception (by calling the QK_ISR_EXIT() macro).
;
; Due to tail-chaining and its lowest priority, the PendSV exception will be
; entered immediately after the exit from the last nested interrupt (or
; exception). In QK-nano, this is exactly the time when the QK scheduler
; needs to check for the asynchronous preemptions.
;
;*****************************************************************************
QK_PendSV
    MOV     r1,#0x01000000  ; make up a task xPSR that has only the T bit set
    LDR     r0,=scheduler   ; load the address of scheduler wrapper (new PC)
    PUSH    {r0-r1}         ; push xPSR,PC
    SUB     sp,sp,#(6*4)    ; don't care for lr,r12,r3,r2,r1,r0
    BX      lr              ; interrupt return to the scheduler wrapper

scheduler
    CPSID   i               ; disable interrupts at processor level
    BL      QK_schedule_    ; call the QK-nano scheduler
    CPSIE   i               ; enable interrupts to allow SVCall exception
    SVC     0               ; cause exception to return to the preempted task


;*****************************************************************************
;
; The QK_SVCall exception handler is used for returning back to the
; interrupted context (task or interrupt). The SVCall exception should have
; the lowest priority in the whole system (see QK_init). The SVCall
; exception simply removes its own interrupt stack frame from the stack and
; returns to the preempted task using the interrupt stack frame that must be
; at the top of the stack.
;
;*****************************************************************************
QK_SVCall
    ADD     sp,sp,#(8*4)    ; remove one interrupt frame from the stack
    BX      lr              ; return to the preempted task


    ALIGNROM 2,0xFF         ; make sure the END is properly aligned
    END

