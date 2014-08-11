/*****************************************************************************
* Product: QK port to ARM Cortex-M0/M3, GNU ARM assembler, CMSIS-compliant
* Last Updated for Version: 4.1.03
* Date of the Last Update:  Feb 18, 2010
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2010 Quantum Leaps, LLC. All rights reserved.
*
* This software may be distributed and modified under the terms of the GNU
* General Public License version 2 (GPL) as published by the Free Software
* Foundation and appearing in the file GPL.TXT included in the packaging of
* this file. Please note that GPL Section 2[b] requires that all works based
* on this software must also be made publicly available under the terms of
* the GPL ("Copyleft").
*
* Alternatively, this software may be distributed and modified under the
* terms of Quantum Leaps commercial licenses, which expressly supersede
* the GPL and are specifically designed for licensees interested in
* retaining the proprietary status of their code.
*
* Contact information:
* Quantum Leaps Web site:  http://www.quantum-leaps.com
* e-mail:                  info@quantum-leaps.com
*****************************************************************************/

    .syntax unified
    .thumb

/*****************************************************************************
*
* The QK_init function sets the priorities of SVCall and PendSV exceptions
* to the lowest level possible (0xFF). The function internally disables
* interrupts, but restores the original interrupt lock before exit.
*
*****************************************************************************/
    .section .text.QK_init
    .global QK_init
    .type   QK_init, %function
QK_init:
    MRS     r0,PRIMASK        /* store the state of the PRIMASK in r0       */
    CPSID   i                 /* disable interrupts (set PRIMASK)           */

    LDR     r1,=0xE000ED18    /* System Handler Priority Register           */
    LDR     r2,[r1,#8]        /* load the System 12-15 Priority Register    */
    MOVS    r3,#0xFF
    LSLS    r3,r3,#16
    ORRS    r2,r3             /* set PRI_14 (PendSV) to 0xFF                */
    STR     r2,[r1,#8]        /* write the System 12-15 Priority Register   */
    LDR     r2,[r1,#4]        /* load the System 8-11 Priority Register     */
    LSLS    r3,r3,#8
    ORRS    r2,r3             /* set PRI_11 (SVCall) to 0xFF                */
    STR     r2,[r1,#4]        /* write the System 8-11 Priority Register    */

    MSR     PRIMASK,r0        /* restore the original PRIMASK               */
    BX      lr                /* return to the caller                       */
    .size   QK_init, . - QK_init


/*****************************************************************************
*
* The PendSV_Handler exception hanlder is used for handling asynchronous
* preemptions in QK. The use of the PendSV exception is the recommended
* and most efficient method for performing context switches with ARM Cortex.
*
* The PendSV exception should have the lowest priority in the whole system
* (0xFF, see QK_init). All other exeptions and interrupts should have higher
* priority. For example, for NVIC with 2 priority bits all interrupts and
* exceptions must have numerical value of priority lower than 0xC0. In this
* case the interrupt priority levels available to your applications are (in
* the order from the lowest urgency to the highest urgency): 0x80, 0x40, 0x00.
*
* Also, *all* ISRs in the QK application must trigger the PendSV exception
* by calling the QK_ISR_EXIT() macro.
*
* Due to tail-chaining and its lowest priority, the PendSV exception will be
* entered immediately after the exit from the *last* nested interrupt (or
* exception). In QK, this is exactly the time when the QK scheduler needs to
* check for the asynchronous preemptions.
*
*****************************************************************************/
    .section .text.PendSV_Handler
    .global PendSV_Handler    /* CMSIS-compliant exception name             */
    .type   PendSV_Handler, %function
PendSV_Handler:
    CPSID   i                 /* disable interrupts at processor level      */
    LDR     r0,=QK_readySet_  /* load the address of QK_readySet_           */
    LDRB    r0,[r0]           /* load the first byte of QK_readySet_        */
    CMP     r0,#0             /* is QK_readySet_ == 0 ?                     */
    BEQ.N   iret              /* if QK_readySet_ == 0, branch to iret       */

    MOVS    r1,#0x01
    LSLS    r1,r1,#24         /* make up a task xPSR with only the T bit set*/
    LDR     r0,=schedule      /* load the address of sched wrapper (new PC) */
    PUSH    {r0-r1}           /* push xPSR,PC                               */
    SUB     sp,sp,#(6*4)      /* don't care for lr,r12,r3,r2,r1,r0          */
    BX      lr                /* interrupt return to the scheduler          */

iret:
    CPSIE   i                 /* enable interrupts at processor level       */
    BX      lr                /* interrupt return to the task               */

schedule:
    BL      QK_schedule_      /* call the QK scheduler                      */
    CPSIE   i                 /* enable interrupts to allow SVCall exception*/
    SVC     0                 /* SV exception returns to the preempted task */
    .size   PendSV_Handler, . - PendSV_Handler


/*****************************************************************************
*
* The SVC_Handler exception handler is used for returning back to the
* interrupted context (task or interrupt). The SVC exception should have
* the lowest priority in the whole system (see QK_init). The SVCall
* exception simply removes its own interrupt stack frame from the stack and
* returns to the preempted task using the interrupt stack frame that must be
* at the top of the stack.
*
*****************************************************************************/
    .section .text.SVC_Handler
    .global SVC_Handler       /* CMSIS-compliant exception name             */
    .type   SVC_Handler, %function
SVC_Handler:
    ADD     sp,sp,#(8*4)      /* remove one interrupt frame from the stack  */
    BX      lr                /* return to the preempted task               */
    .size   SVC_Handler, . - SVC_Handler

    .end
