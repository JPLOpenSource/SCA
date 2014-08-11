//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++ port to x86, uC/OS-II, Turbo C++ 1.01, Large model
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 07, 2008
//
//                    Q u a n t u m     L e a P s
//                    ---------------------------
//                    innovating embedded systems
//
// Copyright (C) 2002-2008 Quantum Leaps, LLC. All rights reserved.
//
// This software may be distributed and modified under the terms of the GNU
// General Public License version 2 (GPL) as published by the Free Software
// Foundation and appearing in the file GPL.TXT included in the packaging of
// this file. Please note that GPL Section 2[b] requires that all works based
// on this software must also be made publicly available under the terms of
// the GPL ("Copyleft").
//
// Alternatively, this software may be distributed and modified under the
// terms of Quantum Leaps commercial licenses, which expressly supersede
// the GPL and are specifically designed for licensees interested in
// retaining the proprietary status of their code.
//
// Contact information:
// Quantum Leaps Web site:  http://www.quantum-leaps.com
// e-mail:                  info@quantum-leaps.com
//////////////////////////////////////////////////////////////////////////////
#include "qf_pkg.h"
#include "qassert.h"

Q_DEFINE_THIS_MODULE(qf_port)

// Local objects -------------------------------------------------------------
static void interrupt (*l_dosSpareISR)(...);

//............................................................................
//lint -e970 -e971               ignore MISRA rules 13 and 14 in this function
const char Q_ROM * Q_ROM_VAR QF::getPortVersion(void) {
    static const char Q_ROM Q_ROM_VAR version[] =  "3.5.00";
    return version;
}
//............................................................................
void QF::init(void) {
    OSInit();                                           // initialize uC/OS-II
}
//............................................................................
void QF::run(void) {
                                     // install uC/OS-II context switch vector
    l_dosSpareISR = getvect(uCOS);
    setvect(uCOS, (void interrupt (*)(...))&OSCtxSw);

    // NOTE the QF::onStartup() callback must be invoked from the task level
    OSStart();                                  // start uC/OS-II multitasking
}
//............................................................................
void QF::stop(void) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    setvect(uCOS, l_dosSpareISR);           // restore the original DOS vector
    QF_INT_UNLOCK_();
    onCleanup();                                           // cleanup callback
}
//............................................................................
extern "C" void task_function(void *pdata) {        // uC/OS-II task signature
    ((QActive *)pdata)->m_running = (uint8_t)1;// allow the thread-loop to run
    while (((QActive *)pdata)->m_running) {
        QEvent const *e = ((QActive *)pdata)->get_();
        ((QActive *)pdata)->dispatch(e); // dispatch to the active object's SM
        QF::gc(e);      // check if the event is garbage, and collect it if so
    }

    QF::remove_((QActive *)pdata);    // remove this object from the framework
    OSTaskDel(OS_PRIO_SELF);           // make uC/OS-II forget about this task
}
//............................................................................
void QActive::start(uint8_t prio,
                    QEvent const *qSto[], uint32_t qLen,
                    void *stkSto, uint32_t stkSize,
                    QEvent const *ie)
{
    m_eQueue = OSQCreate((void **)qSto, qLen);
    Q_ASSERT(m_eQueue != (OS_EVENT *)0);             // uC/OS-II queue created
    m_prio = prio;                                      // set the QF priority
    QF::add_(this);                     // make QF aware of this active object
    init(ie);                                // execute the initial transition

    QS_FLUSH();                          // flush the trace buffer to the host

    m_thread = QF_MAX_ACTIVE - m_prio;          // map QF priority to uC/OS-II
    INT8U err = OSTaskCreateExt(&task_function,           // the task function
             this,                                    // the 'pdata' parameter
             &(((OS_STK *)stkSto)[(stkSize / sizeof(OS_STK)) - 1]),    // ptos
             m_thread,                               // uC/OS-II task priority
             m_thread,                                              // task id
             (OS_STK *)stkSto,                                         // pbos
             stkSize/sizeof(OS_STK),      // size of the stack in OS_STK units
             (void *)0,                                                // pext
             (INT16U)OS_TASK_OPT_STK_CLR);                              // opt
    Q_ASSERT(err == OS_NO_ERR);                       // uC/OS-II task created
}
//............................................................................
void QActive::stop(void) {
    m_running = (uint8_t)0;  // clear the loop variable used in QActive::run()

    INT8U err;
    OSQDel(m_eQueue, OS_DEL_ALWAYS, &err);       // cleanup the uC/OS-II queue
    Q_ASSERT(err == OS_NO_ERR);
}
//............................................................................
void QActive::postFIFO(QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    if (e->dynamic_ != (uint8_t)0) {
        ++((QEvent *)e)->dynamic_;
    }
    QF_INT_UNLOCK_();
    Q_ALLEGE(OSQPost((OS_EVENT *)m_eQueue, (void *)e) == OS_NO_ERR);
}
//............................................................................
void QActive::postLIFO(QEvent const *e) {
    QF_INT_LOCK_KEY_
    QF_INT_LOCK_();
    if (e->dynamic_ != (uint8_t)0) {
        ++((QEvent *)e)->dynamic_;
    }
    QF_INT_UNLOCK_();
    Q_ALLEGE(OSQPostFront((OS_EVENT *)m_eQueue, (void *)e) == OS_NO_ERR);
}
//............................................................................
QEvent const *QActive::get_(void) {
    INT8U err;
    QEvent const *e = (QEvent *)OSQPend((OS_EVENT *)m_eQueue, 0, &err);
    Q_ASSERT(err == OS_NO_ERR);
    return e;
}

