/*****************************************************************************
* Product:  QF/C port to Win32
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jul 13, 2011
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2011 Quantum Leaps, LLC. All rights reserved.
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
#include "qf_pkg.h"
#include "qassert.h"

#include <stdio.h>

Q_DEFINE_THIS_MODULE(qf_port)

/* Global objects ----------------------------------------------------------*/
CRITICAL_SECTION QF_win32CritSect_;

/* Local objects -----------------------------------------------------------*/
static DWORD WINAPI thread_function(LPVOID arg);
static DWORD l_tickMsec = 10;  /* clock tick in msec (argument for Sleep()) */
static uint8_t l_running;

#ifdef Q_SPY
static uint8_t l_ticker;
#endif

/*..........................................................................*/
const char Q_ROM *QF_getPortVersion(void) {
    static const char Q_ROM version[] =  "4.2.00";
    return version;
}
/*..........................................................................*/
void QF_init(void) {
    InitializeCriticalSection(&QF_win32CritSect_);
}
/*..........................................................................*/
void QF_stop(void) {
    l_running = (uint8_t)0;
}
/*..........................................................................*/
void QF_run(void) {
    l_running = (uint8_t)1;
    QF_onStartup();                                     /* startup callback */
            /* raise the priority of this (main) thread to tick more timely */
    SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_TIME_CRITICAL);

    QS_OBJ_DICTIONARY(&l_ticker);       /* the QS dictionary for the ticker */

    while (l_running) {
        QF_TICK(&l_ticker);                          /* process a time tick */
        Sleep(l_tickMsec);                    /* wait for the tick interval */
    }
    QF_onCleanup();                                     /* cleanup callback */
    QS_EXIT();                               /* cleanup the QSPY connection */
    DeleteCriticalSection(&QF_win32CritSect_);
}
/*..........................................................................*/
void QF_setTickRate(uint32_t ticksPerSec) {
    l_tickMsec = 1000UL / ticksPerSec;
}
/*..........................................................................*/
void QActive_start(QActive *me, uint8_t prio,
                  QEvent const *qSto[], uint32_t qLen,
                  void *stkSto, uint32_t stkSize,
                  QEvent const *ie)
{
    int p;
    DWORD threadId;

    Q_REQUIRE((stkSto == (void *)0)   /* Windows allocates stack internally */
              && (stkSize != 0));

    QEQueue_init(&me->eQueue, qSto, (QEQueueCtr)qLen);
    me->osObject = CreateEvent(NULL, FALSE, FALSE, NULL);
    me->prio = prio;
    QF_add_(me);                     /* make QF aware of this active object */
    QF_ACTIVE_INIT_(&me->super, ie);          /* execute initial transition */

    me->thread = CreateThread(NULL, stkSize,
                              &thread_function, me, 0, &threadId);
    Q_ASSERT(me->thread != (HANDLE)0);            /* thread must be created */

    switch (me->prio) {              /* remap QF priority to Win32 priority */
        case 1:
            p = THREAD_PRIORITY_IDLE;
            break;
        case 2:
            p = THREAD_PRIORITY_LOWEST;
            break;
        case 3:
            p = THREAD_PRIORITY_BELOW_NORMAL;
            break;
        case (QF_MAX_ACTIVE - 1):
            p = THREAD_PRIORITY_ABOVE_NORMAL;
            break;
        case QF_MAX_ACTIVE:
            p = THREAD_PRIORITY_HIGHEST;
            break;
        default:
            p = THREAD_PRIORITY_NORMAL;
            break;
    }
    SetThreadPriority(me->thread, p);
}
/*..........................................................................*/
void QActive_stop(QActive *me) {
    me->running = (uint8_t)0;                        /* stop the run() loop */
    CloseHandle(me->osObject);                      /* cleanup the OS event */
}
/*..........................................................................*/
static DWORD WINAPI thread_function(LPVOID arg) {     /* for CreateThread() */
    ((QActive *)arg)->running = (uint8_t)1; /* allow the thread loop to run */
    while (((QActive *)arg)->running) {    /* QActive_stop() stops the loop */
        QEvent const *e = QActive_get_((QActive *)arg);   /* wait for event */
        QF_ACTIVE_DISPATCH_((QHsm *)arg, e);     /* dispatch to the AO's SM */
        QF_gc(e);    /* check if the event is garbage, and collect it if so */
    }
    QF_remove_((QActive *)arg);/* remove this object from any subscriptions */
    return 0;                                             /* return success */
}
