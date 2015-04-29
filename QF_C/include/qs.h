/*****************************************************************************
* Product:  QS/C platform-independent public interface.
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
#ifndef qs_h
#define qs_h

/**
* \file
* \ingroup qep qf qk qs
* \brief QS/C platform-independent public interface.
*
* This header file must be included directly or indirectly
* in all modules (*.c files) that use QS/C.
*/

#ifndef Q_SPY
    #error "Q_SPY must be defined to include qs.h"
#endif

/****************************************************************************/
/** \brief Quantum Spy record types.
*
* This enumeration specifies the record types used in the QP components.
* You can specify your own record types starting from ::QS_USER offset.
* Currently, the maximum of all records cannot exceed 256.
* \sa QS_filterIn()/QS_FILTER_ON and QS_filterOut()/QS_FILTER_OFF
*/
enum QSpyRecords {
    /* QEP records */
    QS_QEP_STATE_EMPTY,
    QS_QEP_STATE_ENTRY,                            /**< a state was entered */
    QS_QEP_STATE_EXIT,                              /**< a state was exited */
    QS_QEP_STATE_INIT,       /**< an intial transition was taken in a state */
    QS_QEP_INIT_TRAN,        /**< the top-most initial transition was taken */
    QS_QEP_INTERN_TRAN,               /**< an internal transition was taken */
    QS_QEP_TRAN,                        /**< a regular transition was taken */
    QS_QEP_IGNORED,          /**< an event was ignored (silently discarded) */
    QS_QEP_DISPATCH,       /**< an event was dispatched (begin of RTC step) */
    QS_QEP_RESERVED0,

    /* QF records */
    QS_QF_ACTIVE_ADD,             /**< an AO has been added to QF (started) */
    QS_QF_ACTIVE_REMOVE,      /**< an AO has been removed from QF (stopped) */
    QS_QF_ACTIVE_SUBSCRIBE,               /**< an AO subscribed to an event */
    QS_QF_ACTIVE_UNSUBSCRIBE,           /**< an AO unsubscribed to an event */
    QS_QF_ACTIVE_POST_FIFO,  /**< an event was posted (FIFO) directly to AO */
    QS_QF_ACTIVE_POST_LIFO,  /**< an event was posted (LIFO) directly to AO */
    QS_QF_ACTIVE_GET, /**< AO got an event and its queue is still not empty */
    QS_QF_ACTIVE_GET_LAST,      /**< AO got an event and its queue is empty */
    QS_QF_EQUEUE_INIT,                  /**< an event queue was initialized */
    QS_QF_EQUEUE_POST_FIFO,  /**< an event was posted (FIFO) to a raw queue */
    QS_QF_EQUEUE_POST_LIFO,  /**< an event was posted (LIFO) to a raw queue */
    QS_QF_EQUEUE_GET,           /**< get an event and queue still not empty */
    QS_QF_EQUEUE_GET_LAST,           /**< get the last event from the queue */
    QS_QF_MPOOL_INIT,                    /**< a memory pool was initialized */
    QS_QF_MPOOL_GET,     /**< a memory block was removed from a memory pool */
    QS_QF_MPOOL_PUT,      /**< a memory block was returned to a memory pool */
    QS_QF_PUBLISH,                              /**< an event was published */
    QS_QF_RESERVED8,
    QS_QF_NEW,                                      /**< new event creation */
    QS_QF_GC_ATTEMPT,                       /**< garbage collection attempt */
    QS_QF_GC,                                       /**< garbage collection */
    QS_QF_TICK,                                   /**< QF_tick() was called */
    QS_QF_TIMEEVT_ARM,                          /**< a time event was armed */
    QS_QF_TIMEEVT_AUTO_DISARM,   /**< a time event expired and was disarmed */
    QS_QF_TIMEEVT_DISARM_ATTEMPT,/**< attempt to disarmed a disarmed tevent */
    QS_QF_TIMEEVT_DISARM,        /**< true disarming of an armed time event */
    QS_QF_TIMEEVT_REARM,                      /**< rearming of a time event */
    QS_QF_TIMEEVT_POST,   /**< a time event posted itself directly to an AO */
    QS_QF_TIMEEVT_CTR,              /**< a time event counter was requested */
    QS_QF_INT_LOCK,                             /**< interrupts were locked */
    QS_QF_INT_UNLOCK,                         /**< interrupts were unlocked */
    QS_QF_ISR_ENTRY,                                /**< an ISR was entered */
    QS_QF_ISR_EXIT,                                  /**< an ISR was exited */
    QS_QF_RESERVED6,
    QS_QF_RESERVED5,
    QS_QF_RESERVED4,
    QS_QF_RESERVED3,
    QS_QF_RESERVED2,
    QS_QF_RESERVED1,
    QS_QF_RESERVED0,

    /* QK records */
    QS_QK_MUTEX_LOCK,                          /**< the QK mutex was locked */
    QS_QK_MUTEX_UNLOCK,                      /**< the QK mutex was unlocked */
    QS_QK_SCHEDULE,             /**< the QK scheduled a new task to execute */
    QS_QK_RESERVED6,
    QS_QK_RESERVED5,
    QS_QK_RESERVED4,
    QS_QK_RESERVED3,
    QS_QK_RESERVED2,
    QS_QK_RESERVED1,
    QS_QK_RESERVED0,

    /* Miscellaneous QS records */
    QS_SIG_DICTIONARY,                         /**< signal dictionary entry */
    QS_OBJ_DICTIONARY,                         /**< object dictionary entry */
    QS_FUN_DICTIONARY,                       /**< function dictionary entry */
    QS_ASSERT,                             /**< assertion fired in the code */
    QS_RESERVED5,
    QS_RESERVED4,
    QS_RESERVED3,
    QS_RESERVED2,
    QS_RESERVED1,
    QS_RESERVED0,

    /* User records */
    QS_USER             /**< the first record available for user QS records */
};

/* Global and Local QS filters .............................................*/
extern uint8_t QS_glbFilter_[32];              /**< global on/off QS filter */
extern void const *QS_smObj_;       /**< state machine for QEP local filter */
extern void const *QS_aoObj_;        /**< active object for QF local filter */
extern void const *QS_mpObj_;          /**<  event pool for QF local filter */
extern void const *QS_eqObj_;           /**<  raw queue for QF local filter */
extern void const *QS_teObj_;          /**<  time event for QF local filter */
extern void const *QS_apObj_; /**<  generic object Application local filter */

/** \brief Specification of all QS records for  QS_FILTER_ON() and
* QS_FILTER_OFF()
*/
#define QS_ALL_RECORDS          ((uint8_t)0xFF)

#ifndef QS_TIME_SIZE

    /** \brief The size (in bytes) of the QS time stamp. Valid values: 1, 2,
    * or 4; default 4.
    *
    * This macro can be defined in the QS port file (qs_port.h) to
    * configure the ::QSTimeCtr type. Here the macro is not defined so the
    * default of 4 byte is chosen.
    */
    #define QS_TIME_SIZE 4
#endif

#if (QS_TIME_SIZE == 1)
    typedef uint8_t QSTimeCtr;
    #define QS_TIME_()          QS_u8_(QS_onGetTime())
#elif (QS_TIME_SIZE == 2)
    typedef uint16_t QSTimeCtr;
    #define QS_TIME_()          QS_u16_(QS_onGetTime())
#elif (QS_TIME_SIZE == 4)
    /** \brief The type of the QS time stamp
    *
    * This type determines the dynamic range of QS time stamps
    */
    typedef uint32_t QSTimeCtr;
    /** \brief Internal macro to output time stamp to a QS record
    */
    #define QS_TIME_()          QS_u32_(QS_onGetTime())
#else
    #error "QS_TIME_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

                                 /** tick counter for the QS_QF_TICK record */
extern QSTimeCtr volatile QS_tickCtr_;

#ifndef Q_ROM                   /* provide the default if Q_ROM NOT defined */
    #define Q_ROM
#endif
#ifndef Q_ROM_VAR           /* provide the default if Q_ROM_VAR NOT defined */
    #define Q_ROM_VAR
#endif
#ifndef Q_ROM_BYTE         /* provide the default if Q_ROM_BYTE NOT defined */
    #define Q_ROM_BYTE(rom_var_)   (rom_var_)
#endif

/*****************************************************************************
* QS services.
*/
/* public methods */
/** \brief Get the current version of QS
*
* \return version of the QS as a constant 6-character string of the form
* x.y.zz, where x is a 1-digit major version number, y is a 1-digit minor
* version number, and zz is a 2-digit release number.
*/
char const Q_ROM * Q_ROM_VAR QS_getVersion(void);

/** \brief Initialize the QS data buffer.
*
* This function should be called from QS_onStartup() to provide QS with
* the data buffer. The first argument \a sto[] is the address of the memory
* block, and the second argument \a stoSize is the size of this block
* in bytes. Currently the size of the QS buffer cannot exceed 64KB.
*
* QS can work with quite small data buffers, but you will start losing data
* if the buffer is too small for the bursts of logging activity.
* The right size of the buffer depends on the data production rate and
* the data output rate. QS offers flexible filtering to reduce the data
* production rate.
*
* \note If the data output rate cannot keep up with the production rate,
* QS will start overwriting the older data with newer data. This is
* consistent with the "last-is-best" QS policy. The record sequence counters
* and checksums on each record allow to easily detect data loss.
*/
void QS_initBuf(uint8_t sto[], uint32_t stoSize);

/** \brief Turn the global Filter on for a given record type \a rec.
*
* This function sets up the QS filter to enable the record type \a rec.
* The argument #QS_ALL_RECORDS specifies to filter-in all records.
* This function should be called indirectly through the macro QS_FILTER_ON.
*
* \note Filtering based on the record-type is only the first layer of
* filtering. The second layer is based on the object-type. Both filter
* layers must be enabled for the QS record to be inserted into the QS buffer.
* \sa QS_filterOff(), QS_FILTER_SM_OBJ, QS_FILTER_AO_OBJ, QS_FILTER_MP_OBJ,
* QS_FILTER_EQ_OBJ, and QS_FILTER_TE_OBJ.
*/
void QS_filterOn(uint8_t rec);

/** \brief Turn the global Filter off for a given record type \a rec.
*
* This function sets up the QS filter to disable the record type \a rec.
* The argument #QS_ALL_RECORDS specifies to suppress all records.
* This function should be called indirectly through the macro QS_FILTER_OFF.
*
* \note Filtering records based on the record-type is only the first layer of
* filtering. The second layer is based on the object-type. Both filter
* layers must be enabled for the QS record to be inserted into the QS buffer.
* \sa
*/
void QS_filterOff(uint8_t rec);

/** \brief Mark the begin of a QS record \a rec
*
* This function must be called at the beginning of each QS record.
* This function should be called indirectly through the macro #QS_BEGIN,
* or #QS_BEGIN_NOLOCK, depending if it's called in a normal code or from
* a critical section.
*/
void QS_begin(uint8_t rec);

/** \brief Mark the end of a QS record \a rec
*
* This function must be called at the end of each QS record.
* This function should be called indirectly through the macro #QS_END,
* or #QS_END_NOLOCK, depending if it's called in a normal code or from
* a critical section.
*/
void QS_end(void);

/* unformatted data elements output ........................................*/
/** \brief output uint8_t data element without format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u8_(uint8_t d);

/** \brief Output uint16_t data element without format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u16_(uint16_t d);

/** \brief Output uint32_t data element without format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u32_(uint32_t d);

/** \brief Output zero-terminated ASCII string element without format
* information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_str_(char const *s);

/** \brief Output zero-terminated ASCII string element allocated in ROM
* without format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_str_ROM_(char const Q_ROM * Q_ROM_VAR s);

/* formatted data elements output ..........................................*/
/** \brief Output uint8_t data element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u8(uint8_t format, uint8_t d);

/** \brief output uint16_t data element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u16(uint8_t format, uint16_t d);

/** \brief Output uint32_t data element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_u32(uint8_t format, uint32_t d);

/** \brief Output 32-bit floating point data element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_f32(uint8_t format, float d);

/** \brief Output 64-bit floating point data element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_f64(uint8_t format, double d);

/** \brief Output zero-terminated ASCII string element with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_str(char const *s);

/** \brief Output zero-terminated ASCII string element allocated in ROM
* with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_str_ROM(char const Q_ROM * Q_ROM_VAR s);

/** \brief Output memory block of up to 255-bytes with format information
* \note This function is only to be used through macros, never in the
* client code directly.
*/
void QS_mem(uint8_t const *blk, uint8_t size);

#if (QS_OBJ_PTR_SIZE == 8) || (QS_FUN_PTR_SIZE == 8)
    /** \brief Output uint64_t data element without format information
    * \note This function is only to be used through macros, never in the
    * client code directly.
    */
    void QS_u64_(uint64_t d);

    /** \brief Output uint64_t data element with format information
    * \note This function is only to be used through macros, never in the
    * client code directly.
    */
    void QS_u64(uint8_t format, uint64_t d);
#endif

/* QS buffer access ........................................................*/
/** \brief Byte-oriented interface to the QS data buffer.
*
* This function delivers one byte at a time from the QS data buffer.
* The function returns the byte in the least-significant 8-bits of the
* 16-bit return value if the byte is available. If no more data is
* available at the time, the function returns QS_EOD (End-Of-Data).
*
* \note QS_getByte() is NOT protected with a critical section.
*/
uint16_t QS_getByte(void);

/** \brief Constant representing End-Of-Data condition returned from the
* QS_getByte() function.
*/
#define QS_EOD ((uint16_t)0xFFFF)

/** \brief Block-oriented interface to the QS data buffer.
*
* This function delivers a contiguous block of data from the QS data buffer.
* The function returns the pointer to the beginning of the block, and writes
* the number of bytes in the block to the location pointed to by \a pNbytes.
* The argument \a pNbytes is also used as input to provide the maximum size of
* the data block that the caller can accept.
*
* If no bytes are available in the QS buffer when the function is called,
* the function returns a NULL pointer and sets the value pointed to by
* \a pNbytes to zero.
*
* \note Only the NULL return from QS_getBlock() indicates that the QS buffer
* is empty at the time of the call. The non-NULL return often means that
* the block is at the end of the buffer and you need to call QS_getBlock()
* again to obtain the rest of the data that "wrapped around" to the beginning
* of the QS data buffer.
*
* \note QS_getBlock() is NOT protected with a critical section.
*/
uint8_t const *QS_getBlock(uint16_t *pNbytes);

/* platform-specific callback functions, need to be implemented by clients  */
/** \brief Callback to startup the QS facility
*
* This is a platform-dependent "callback" function invoked through the macro
* #QS_INIT. You need to implement this function in your application.
* At a minimum, the function must configure the QS buffer by calling
* QS_initBuf(). Typically, you will also want to open/configure the QS output
* channel, such as a serial port, or a data file. The void* argument \a arg
* can be used to pass parameter(s) needed to configure the output channel.
*
* The function returns TRUE (1) if the QS initialization was successful,
* or FALSE (0) if it failed.
*
* The following example illustrates an implementation of QS_onStartup():
* \include qs_startup.c
*/
uint8_t QS_onStartup(void const *arg);

/** \brief Callback to cleanup the QS facility
*
* This is a platform-dependent "callback" function invoked through the macro
* #QS_EXIT. You need to implement this function in your application.
* The main purpose of this function is to close the QS output channel, if
* necessary.
*/
void QS_onCleanup(void);

/** \brief Callback to flush the QS trace data to the host
*
* This is a platform-dependent "callback" function to flush the QS trace
* buffer to the host. The function typically busy-waits until all the data
* in the buffer is sent to the host. This is acceptable only in the initial
* transient.
*/
void QS_onFlush(void);

/** \brief Callback to obtain a timestamp for a QS record.
*
* This is a platform-dependent "callback" function invoked from the macro
* #QS_TIME_ to add the time stamp to a QS record.
*
* \note Some of the pre-defined QS records from QP do not output the time
* stamp. However, ALL user records do output the time stamp.
* \note QS_onGetTime() is called in a critical section and should not
* unlock interrupts.
*
* The following example shows using a system call to implement QS
* time stamping:
* \include qs_onGetTime.c
*/
QSTimeCtr QS_onGetTime(void);


/* Macros for adding QS instrumentation to the client code .................*/

/** \brief Initialize the QS facility.
*
* This macro provides an indirection layer to invoke the QS initialization
* routine if #Q_SPY is defined, or do nothing if #Q_SPY is not defined.
* \sa QS_onStartup(), example of setting up a QS filter in QS_FILTER_ON
*/
#define QS_INIT(arg_)           QS_onStartup(arg_)

/** \brief Cleanup the QS facility.
*
* This macro provides an indirection layer to invoke the QS cleanup
* routine if #Q_SPY is defined, or do nothing if #Q_SPY is not defined.
* \sa QS_exit()
*/
#define QS_EXIT()               QS_onCleanup()

/** \brief Global Filter ON for a given record type \a rec.
*
* This macro provides an indirection layer to call QS_filterOn() if #Q_SPY
* is defined, or do nothing if #Q_SPY is not defined.
*
* The following example shows how to use QS filters:
* \include qs_filter.c
*/
#define QS_FILTER_ON(rec_)      QS_filterOn(rec_)

/** \brief Global filter OFF for a given record type \a rec.
*
* This macro provides an indirection layer to call QS_filterOff() if #Q_SPY
* is defined, or do nothing if #Q_SPY is not defined.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_OFF(rec_)     QS_filterOff(rec_)

/** \brief Local Filter for a given state machine object \a obj_.
*
* This macro sets up the state machine object local filter if #Q_SPY is
* defined, or does nothing if #Q_SPY is not defined. The argument \a obj_
* is the pointer to the state machine object that you want to monitor.
*
* The state machine object filter allows you to filter QS records pertaining
* only to a given state machine object. With this filter disabled, QS will
* output records from all state machines in your application. The object
* filter is disabled by setting the state machine pointer to NULL.
*
* The state machine filter affects the following QS records:
* ::QS_QEP_STATE_ENTRY, ::QS_QEP_STATE_EXIT, ::QS_QEP_STATE_INIT,
* ::QS_QEP_INIT_TRAN, ::QS_QEP_INTERN_TRAN, ::QS_QEP_TRAN,
* and ::QS_QEP_IGNORED.
*
* \note Because active objects are state machines at the same time,
* the state machine filter (QS_FILTER_SM_OBJ) pertains to active
* objects as well. However, the state machine filter is more general,
* because it can be used only for state machines that are not active objects,
* such as "Orthogonal Components".
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_SM_OBJ(obj_)  (QS_smObj_ = (obj_))

/** \brief Local Filter for a given active object \a obj_.
*
* This macro sets up the active object local filter if #Q_SPY is defined,
* or does nothing if #Q_SPY is not defined. The argument \a obj_ is the
* pointer to the active object that you want to monitor.
*
* The active object filter allows you to filter QS records pertaining
* only to a given active object. With this filter disabled, QS will
* output records from all active objects in your application. The object
* filter is disabled by setting the active object pointer \a obj_ to NULL.
*
* The active object filter affects the following QS records:
* ::QS_QF_ACTIVE_ADD, ::QS_QF_ACTIVE_REMOVE, ::QS_QF_ACTIVE_SUBSCRIBE,
* ::QS_QF_ACTIVE_UNSUBSCRIBE, ::QS_QF_ACTIVE_POST_FIFO,
* ::QS_QF_ACTIVE_POST_LIFO, ::QS_QF_ACTIVE_GET, and ::QS_QF_ACTIVE_GET_LAST.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_AO_OBJ(obj_)  (QS_aoObj_ = (obj_))

/** \brief Local Filter for a given memory pool object \a obj_.
*
* This macro sets up the memory pool local object filter if #Q_SPY is
* defined, or does nothing if #Q_SPY is not defined. The argument \a obj_
* is the pointer to the memory buffer used during the initialization of
* the event pool with QF_poolInit().
*
* The memory pool filter allows you to filter QS records pertaining
* only to a given memory pool. With this filter disabled, QS will
* output records from all memory pools in your application. The object
* filter is disabled by setting the memory pool pointer \a obj_ to NULL.
*
* The memory pool filter affects the following QS records:
* ::QS_QF_MPOOL_INIT, ::QS_QF_MPOOL_GET, and ::QS_QF_MPOOL_PUT.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_MP_OBJ(obj_)  (QS_mpObj_ = (obj_))

/** \brief Local Filter for a given event queue object \a obj_.
*
* This macro sets up the event queue object local filter if #Q_SPY is
* defined, or does nothing if #Q_SPY is not defined. The argument \a obj_
* is the pointer to the "raw" thread-safe queue object you want to monitor.
*
* The event queue filter allows you to filter QS records pertaining
* only to a given event queue. With this filter disabled, QS will
* output records from all event queues in your application. The object
* filter is disabled by setting the event queue pointer \a obj_ to NULL.
*
* The event queue filter affects the following QS records:
* ::QS_QF_EQUEUE_INIT, ::QS_QF_EQUEUE_POST_FIFO, ::QS_QF_EQUEUE_POST_LIFO,
* ::QS_QF_EQUEUE_GET, and ::QS_QF_EQUEUE_GET_LAST.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_EQ_OBJ(obj_)  (QS_eqObj_ = (obj_))

/** \brief Local Filter for a given time event object \a obj_.
*
* This macro sets up the time event object local filter if #Q_SPY is defined,
* or does nothing if #Q_SPY is not defined. The argument \a obj_ is the
* pointer to the time event object you want to monitor.
*
* The time event filter allows you to filter QS records pertaining
* only to a given time event. With this filter disabled, QS will
* output records from all time events in your application. The object
* filter is disabled by setting the time event pointer \a obj_ to NULL.
*
* The time event filter affects the following QS records:
* ::QS_QF_TIMEEVT_ARM, ::QS_QF_TIMEEVT_AUTO_DISARM,
* ::QS_QF_TIMEEVT_DISARM_ATTEMPT, ::QS_QF_TIMEEVT_DISARM,
* ::QS_QF_TIMEEVT_REARM, ::QS_QF_TIMEEVT_POST, and ::QS_QF_TIMEEVT_PUBLISH.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_TE_OBJ(obj_)  (QS_teObj_ = (obj_))

/** \brief Local Filter for a generic application object \a obj_.
*
* This macro sets up the application object local filter if #Q_SPY is
* defined, or does nothing if #Q_SPY is not defined. The argument \a obj_
* is the pointer to the application object you want to monitor.
*
* The application object filter allows you to filter QS records pertaining
* only to a given application object. With this filter disabled, QS will
* output records from all application-records enabled by the global filter.
* The local filter is disabled by setting the time event pointer \a obj_
* to NULL.
*
* \sa Example of using QS filters in #QS_FILTER_ON documentation
*/
#define QS_FILTER_AP_OBJ(obj_)  (QS_apObj_ = (obj_))

/* Macros to generate user QS records ......................................*/

/** \brief Begin a QS user record without locking interrupts.
*/
#define QS_BEGIN_NOLOCK(rec_, obj_) \
    if (((QS_glbFilter_[(uint8_t)(rec_) >> 3U] \
          & (1U << ((uint8_t)(rec_) & 7U))) != 0U) \
        && ((QS_apObj_ == (void *)0) || (QS_apObj_ == (obj_)))) \
    { \
        QS_begin((uint8_t)(rec_)); \
        QS_TIME_(); {

/** \brief End a QS user record without locking interrupts.
*/
#define QS_END_NOLOCK() } \
    QS_END_NOLOCK_()

                                 /* QS-specific interrupt locking/unlocking */
#ifndef QF_INT_KEY_TYPE
    /** \brief This is an internal macro for defining the interrupt lock key.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this QS macro provides the definition of
    * the lock key variable. Otherwise this macro is empty.
    * \sa #QF_INT_KEY_TYPE
    */
    #define QS_INT_LOCK_KEY_

    /** \brief This is an internal macro for locking interrupts.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this QS macro invokes #QF_INT_LOCK
    * passing the key variable as the parameter. Otherwise #QF_INT_LOCK
    * is invoked with a dummy parameter.
    * \sa #QF_INT_LOCK, #QK_INT_LOCK
    */
    #define QS_INT_LOCK_()      QF_INT_LOCK(dummy)

    /** \brief This is an internal macro for unlocking interrupts.
    *
    * The purpose of this macro is to enable writing the same code for the
    * case when interrupt key is defined and when it is not. If the macro
    * #QF_INT_KEY_TYPE is defined, this QS macro invokes #QF_INT_UNLOCK
    * passing the key variable as the parameter. Otherwise #QF_INT_UNLOCK
    * is invoked with a dummy parameter.
    * \sa #QF_INT_UNLOCK, #QK_INT_UNLOCK
    */
    #define QS_INT_UNLOCK_()    QF_INT_UNLOCK(dummy)
#else
    #define QS_INT_LOCK_KEY_    QF_INT_KEY_TYPE intLockKey_;
    #define QS_INT_LOCK_()      QF_INT_LOCK(intLockKey_)
    #define QS_INT_UNLOCK_()    QF_INT_UNLOCK(intLockKey_)
#endif

/** \brief Begin a user QS record with locking interrupts.
*
* The following example shows how to build a user QS record using the
* macros #QS_BEGIN, #QS_END, and the formatted output macros: #QS_U8 and
* #QS_STR.
* \include qs_user.c
* \note Must always be used in pair with #QS_END
*/
#define QS_BEGIN(rec_, obj_) \
    if (((QS_glbFilter_[(uint8_t)(rec_) >> 3U] \
          & (1U << ((uint8_t)(rec_) & 7U))) != 0U) \
        && ((QS_apObj_ == (void *)0) || (QS_apObj_ == (obj_)))) \
    { \
        QS_INT_LOCK_KEY_ \
        QS_INT_LOCK_(); \
        QS_begin((uint8_t)(rec_)); \
        QS_TIME_(); {

/** \brief End a QS record with locking interrupts.
* \sa example for #QS_BEGIN
* \note Must always be used in pair with #QS_BEGIN
*/
#define QS_END() } \
    QS_END_()


/*****************************************************************************
* Macros for use inside other macros or internally in the QP code
*/

/** \brief Internal QS macro to begin a QS record with locking the interrupts.
* \note This macro is intended to use only inside QP components and NOT
* at the application level. \sa #QS_BEGIN
*/
#define QS_BEGIN_(rec_, objFilter_, obj_) \
    if (((QS_glbFilter_[(uint8_t)(rec_) >> 3U] \
          & (1U << ((uint8_t)(rec_) & 7U))) != 0U) \
        && (((objFilter_) == (void *)0) || ((objFilter_) == (obj_)))) \
    { \
        QS_INT_LOCK_(); \
        QS_begin((uint8_t)(rec_));

/** \brief  Internal QS macro to end a QS record with locking the interrupts.
* \note This macro is intended to use only inside QP components and NOT
* at the application level. \sa #QS_END
*/
#define QS_END_() \
        QS_end(); \
        QS_INT_UNLOCK_(); \
    }

/** \brief Internal QS macro to begin a QS record without locking the
* interrupts.
* \note This macro is intended to use only inside QP components and NOT
* at the application level. \sa #QS_BEGIN_NOLOCK
*/
#define QS_BEGIN_NOLOCK_(rec_, objFilter_, obj_) \
    if (((QS_glbFilter_[(uint8_t)(rec_) >> 3U] \
          & (1U << ((uint8_t)(rec_) & 7U))) != 0U) \
        && (((objFilter_) == (void *)0) || ((objFilter_) == (obj_)))) \
    { \
        QS_begin((uint8_t)(rec_));

/** \brief Internal QS macro to end a QS record without locking
* the interrupts.
* \note This macro is intended to use only inside QP components and NOT
* at the application level. \sa #QS_END_NOLOCK
*/
#define QS_END_NOLOCK_() \
        QS_end(); \
    }

/** \brief Internal QS macro to output an unformatted uint8_t data element */
#define QS_U8_(data_)           QS_u8_(data_)

/** \brief Internal QS macro to output an unformatted uint16_t data element */
#define QS_U16_(data_)          QS_u16_(data_)

/** \brief Internal QS macro to output an unformatted uint32_t data element */
#define QS_U32_(data_)          QS_u32_(data_)


#if (QS_OBJ_PTR_SIZE == 1)
    #define QS_OBJ_(obj_)       QS_u8_((uint8_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 2)
    #define QS_OBJ_(obj_)       QS_u16_((uint16_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 4)
    #define QS_OBJ_(obj_)       QS_u32_((uint32_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 8)
    #define QS_OBJ_(obj_)       QS_u64_((uint64_t)(obj_))
#else

    /** \brief Internal QS macro to output an unformatted object pointer
    * data element
    * \note the size of the pointer depends on the macro #QS_OBJ_PTR_SIZE.
    * If the size is not defined the size of pointer is assumed 4-bytes.
    */
    #define QS_OBJ_(obj_)       QS_u32_((uint32_t)(obj_))
#endif


#if (QS_FUN_PTR_SIZE == 1)
    #define QS_FUN_(fun_)       QS_u8_((uint8_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 2)
    #define QS_FUN_(fun_)       QS_u16_((uint16_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 4)
    #define QS_FUN_(fun_)       QS_u32_((uint32_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 8)
    #define QS_FUN_(fun_)       QS_u64_((uint64_t)(fun_))
#else

    /** \brief Internal QS macro to output an unformatted function pointer
    * data element
    * \note the size of the pointer depends on the macro #QS_FUN_PTR_SIZE.
    * If the size is not defined the size of pointer is assumed 4-bytes.
    */
    #define QS_FUN_(fun_)       QS_u32_((uint32_t)(fun_))
#endif


/** \brief Internal QS macro to output a zero-terminated ASCII string
* data element
*/
#define QS_STR_(msg_)           QS_str_(msg_)

/** \brief Internal QS macro to output a zero-terminated ASCII string
* allocated in ROM data element
*/
#define QS_STR_ROM_(msg_)       QS_str_ROM_(msg_)

/*****************************************************************************
* Macros for use in the client code
*/

/** \brief Enumerates data formats recognized by QS
*
* QS uses this enumeration is used only internally for the formatted user
* data elements.
*/
enum QSType {
    QS_I8_T,                               /**< signed 8-bit integer format */
    QS_U8_T,                             /**< unsigned 8-bit integer format */
    QS_I16_T,                             /**< signed 16-bit integer format */
    QS_U16_T,                           /**< unsigned 16-bit integer format */
    QS_I32_T,                             /**< signed 32-bit integer format */
    QS_U32_T,                           /**< unsigned 32-bit integer format */
    QS_F32_T,                             /**< 32-bit floating point format */
    QS_F64_T,                             /**< 64-bit floating point format */
    QS_STR_T,                      /**< zero-terminated ASCII string format */
    QS_MEM_T,                      /**< up to 255-bytes memory block format */
    QS_SIG_T,                                      /**< event signal format */
    QS_OBJ_T,                                    /**< object pointer format */
    QS_FUN_T,                                  /**< function pointer format */
    QS_I64_T,                             /**< signed 64-bit integer format */
    QS_U64_T,                           /**< unsigned 64-bit integer format */
    QS_U32_HEX_T                 /**< unsigned 32-bit integer in hex format */
};

/** \brief Output formatted int8_t to the QS record */
#define QS_I8(width_, data_) \
    QS_u8((uint8_t)(((width_) << 4)) | QS_I8_T, (data_))

/** \brief Output formatted uint8_t to the QS record */
#define QS_U8(width_, data_) \
    QS_u8((uint8_t)(((width_) << 4)) | QS_U8_T, (data_))

/** \brief Output formatted int16_t to the QS record */
#define QS_I16(width_, data_) \
    QS_u16((uint8_t)(((width_) << 4)) | QS_I16_T, (data_))

/** \brief Output formatted uint16_t to the QS record */
#define QS_U16(width_, data_) \
    QS_u16((uint8_t)(((width_) << 4)) | QS_U16_T, (data_))

/** \brief Output formatted int32_t to the QS record */
#define QS_I32(width_, data_) \
    QS_u32((uint8_t)(((width_) << 4)) | QS_I32_T, (data_))

/** \brief Output formatted uint32_t to the QS record */
#define QS_U32(width_, data_) \
    QS_u32((uint8_t)(((width_) << 4)) | QS_U32_T, (data_))

/** \brief Output formatted 32-bit floating point number to the QS record */
#define QS_F32(width_, data_) \
    QS_f32((uint8_t)(((width_) << 4)) | QS_F32_T, (data_))

/** \brief Output formatted 64-bit floating point number to the QS record */
#define QS_F64(width_, data_) \
    QS_f64((uint8_t)(((width_) << 4)) | QS_F64_T, (data_))

/** \brief Output formatted int64_t to the QS record */
#define QS_I64(width_, data_) \
    QS_u64((uint8_t)(((width_) << 4)) | QS_I64_T, (data_))

/** \brief Output formatted uint64_t to the QS record */
#define QS_U64(width_, data_) \
    QS_u64((uint8_t)(((width_) << 4)) | QS_U64_T, (data_))

/** \brief Output formatted uint32_t to the QS record */
#define QS_U32_HEX(width_, data_) \
    QS_u32((uint8_t)(((width_) << 4)) | QS_U32_HEX_T, (data_))

/** \brief Output formatted zero-terminated ASCII string to the QS record */
#define QS_STR(str_)            QS_str(str_)

/** \brief Output formatted zero-terminated ASCII string from ROM
* to the QS record
*/
#define QS_STR_ROM(str_)        QS_str_ROM(str_)

/** \brief Output formatted memory block of up to 255 bytes to the QS
* record
*/
#define QS_MEM(mem_, size_)     QS_mem((mem_), (size_))


#if (QS_OBJ_PTR_SIZE == 1)
    #define QS_OBJ(obj_)        QS_u8(QS_OBJ_T, (uint8_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 2)
    #define QS_OBJ(obj_)        QS_u16(QS_OBJ_T, (uint16_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 4)
    #define QS_OBJ(obj_)        QS_u32(QS_OBJ_T, (uint32_t)(obj_))
#elif (QS_OBJ_PTR_SIZE == 8)
    #define QS_OBJ(obj_)        QS_u64(QS_OBJ_T, (uint64_t)(obj_))
#else
    /** \brief Output formatted object pointer to the QS record */
    #define QS_OBJ(obj_)        QS_u32(QS_OBJ_T, (uint32_t)(obj_))
#endif


#if (QS_FUN_PTR_SIZE == 1)
    #define QS_FUN(fun_)        QS_u8(QS_FUN_T, (uint8_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 2)
    #define QS_FUN(fun_)        QS_u16(QS_FUN_T, (uint16_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 4)
    #define QS_FUN(fun_)        QS_u32(QS_FUN_T, (uint32_t)(fun_))
#elif (QS_FUN_PTR_SIZE == 8)
    #define QS_FUN(fun_)        QS_u64(QS_FUN_T, (uint64_t)(fun_))
#else
    /** \brief Output formatted function pointer to the QS record */
    #define QS_FUN(fun_)        QS_u32(QS_FUN_T, (uint32_t)(fun_))
#endif


#if (Q_SIGNAL_SIZE == 1)

    /** \brief Output formatted event signal (of type ::QSignal) and
    * the state machine object to the user QS record
    */
    #define QS_SIG(sig_, obj_) \
        QS_u8(QS_SIG_T, (sig_)); \
        QS_OBJ_(obj_)
#elif (Q_SIGNAL_SIZE == 2)
    #define QS_SIG(sig_, obj_) \
        QS_u16(QS_SIG_T, (sig_)); \
        QS_OBJ_(obj_)
#elif (Q_SIGNAL_SIZE == 4)
    #define QS_SIG(sig_, obj_) \
        QS_u32(QS_SIG_T, (sig_)); \
        QS_OBJ_(obj_)
#endif

/** \brief Output signal dictionary record
*
* A signal dictionary record associates the numerical value of the signal
* and the binary address of the state machine that consumes that signal
* with the human-readable name of the signal.
*
* Providing a signal dictionary QS record can vastly improve readability of
* the QS log, because instead of dealing with cryptic machine addresses the
* QSpy host utility can display human-readable names.
*
* A signal dictionary entry is associated with both the signal value \a sig_
* and the state machine \a obj_, because signals are required to be unique
* only within a given state machine and therefore the same numerical values
* can represent different signals in different state machines.
*
* For the "global" signals that have the same meaning in all state machines
* (such as globally published signals), you can specify a signal dictionary
* entry with the \a obj_ parameter set to NULL.
*
* The following example shows the definition of signal dictionary entries
* in the initial transition of the Table active object. Please note that
* signals HUNGRY_SIG and DONE_SIG are associated with the Table state machine
* only ("me" \a obj_ pointer). The EAT_SIG signal, on the other hand,
* is global (0 \a obj_ pointer):
* \include qs_sigDic.c
*
* \note The QSpy log utility must capture the signal dictionary record
* in order to use the human-readable information. You need to connect to
* the target before the dictionary entries have been transmitted.
*
* The following QSpy log example shows the signal dictionary records
* generated from the Table initial transition and subsequent records that
* show human-readable names of the signals:
* \include qs_sigLog.txt
*
* The following QSpy log example shows the same sequence of records, but
* with dictionary records removed. The human-readable signal names are not
* available.
* \include qs_sigLog0.txt
*/
#define QS_SIG_DICTIONARY(sig_, obj_) \
    if (((QS_glbFilter_[(uint8_t)QS_SIG_DICTIONARY >> 3U] \
          & (1U << ((uint8_t)QS_SIG_DICTIONARY & 7U))) != 0U)) \
    { \
        static char const Q_ROM Q_ROM_VAR sig_name__[] = #sig_; \
        QS_INT_LOCK_KEY_ \
        QS_INT_LOCK_(); \
        QS_begin((uint8_t)QS_SIG_DICTIONARY); \
        QS_SIG_(sig_); \
        QS_OBJ_(obj_); \
        QS_STR_ROM_(sig_name__); \
        QS_end(); \
        QS_INT_UNLOCK_(); \
        QS_onFlush(); \
    } else ((void)0)

/** \brief Output object dictionary record
*
* An object dictionary record associates the binary address of an object
* in the target's memory with the human-readable name of the object.
*
* Providing an object dictionary QS record can vastly improve readability of
* the QS log, because instead of dealing with cryptic machine addresses the
* QSpy host utility can display human-readable object names.
*
* The following example shows the definition of object dictionary entry
* for the Table active object:
* \include qs_objDic.c
*/
#define QS_OBJ_DICTIONARY(obj_) \
    if (((QS_glbFilter_[(uint8_t)QS_OBJ_DICTIONARY >> 3U] \
          & (1U << ((uint8_t)QS_OBJ_DICTIONARY & 7U))) != 0U)) \
    { \
        static char const Q_ROM Q_ROM_VAR obj_name__[] = #obj_; \
        QS_INT_LOCK_KEY_ \
        QS_INT_LOCK_(); \
        QS_begin((uint8_t)QS_OBJ_DICTIONARY); \
        QS_OBJ_(obj_); \
        QS_STR_ROM_(obj_name__); \
        QS_end(); \
        QS_INT_UNLOCK_(); \
        QS_onFlush(); \
    } else ((void)0)

/** \brief Output function dictionary record
*
* A function dictionary record associates the binary address of a function
* in the target's memory with the human-readable name of the function.
*
* Providing a function dictionary QS record can vastly improve readability of
* the QS log, because instead of dealing with cryptic machine addresses the
* QSpy host utility can display human-readable function names.
*
* The example from #QS_SIG_DICTIONARY shows the definition of a function
* dictionary.
*/
#define QS_FUN_DICTIONARY(fun_) \
    if (((QS_glbFilter_[(uint8_t)QS_FUN_DICTIONARY >> 3U] \
          & (1U << ((uint8_t)QS_FUN_DICTIONARY & 7U))) != 0U)) \
    { \
        static char const Q_ROM Q_ROM_VAR fun_name__[] = #fun_; \
        QS_INT_LOCK_KEY_ \
        QS_INT_LOCK_(); \
        QS_begin((uint8_t)QS_FUN_DICTIONARY); \
        QS_FUN_(fun_); \
        QS_STR_ROM_(fun_name__); \
        QS_end(); \
        QS_INT_UNLOCK_(); \
        QS_onFlush(); \
    } else ((void)0)


/** \brief Flush the QS trace data to the host
*
* This macro invokes the QS_flush() platform-dependent callback function
* to flush the QS trace buffer to the host. The function typically
* busy-waits until all the data in the buffer is sent to the host.
* This is acceptable only in the initial transient.
*/
#define QS_FLUSH()   QS_onFlush()

/** \brief Output the interrupt lock record */
#define QF_QS_INT_LOCK() \
    QS_BEGIN_NOLOCK_(QS_QF_INT_LOCK, (void *)0, (void *)0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(++QF_intLockNest_)); \
    QS_END_NOLOCK_()

/** \brief Output the interrupt unlock record */
#define QF_QS_INT_UNLOCK() \
    QS_BEGIN_NOLOCK_(QS_QF_INT_UNLOCK, (void *)0, (void *)0); \
        QS_TIME_(); \
        QS_U8_((uint8_t)(QF_intLockNest_--)); \
    QS_END_NOLOCK_()

/** \brief Output the interrupt entry record */
#define QF_QS_ISR_ENTRY(isrnest_, prio_) \
    QS_BEGIN_NOLOCK_(QS_QF_ISR_ENTRY, (void *)0, (void *)0); \
        QS_TIME_(); \
        QS_U8_(isrnest_); \
        QS_U8_(prio_); \
    QS_END_NOLOCK_()

/** \brief Output the interrupt exit record */
#define QF_QS_ISR_EXIT(isrnest_, prio_) \
    QS_BEGIN_NOLOCK_(QS_QF_ISR_EXIT, (void *)0, (void *)0); \
        QS_TIME_(); \
        QS_U8_(isrnest_); \
        QS_U8_(prio_); \
    QS_END_NOLOCK_()

/** \brief Execute an action that is only necessary for QS output */
#define QF_QS_ACTION(act_)      (act_)

/** \brief interrupt-lock nesting level
*
* \note Not to be used by Clients directly, only in ports of QF
*/
extern uint8_t QF_intLockNest_;

#endif                                                             /* qs_h  */

