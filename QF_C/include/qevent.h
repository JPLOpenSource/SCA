/*****************************************************************************
* Product:  QP/C
* Last Updated for Version: 4.2.00
* Date of the Last Update:  Jun 29, 2011
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
#ifndef qevent_h
#define qevent_h

/**
* \file
* \ingroup qep qf qk
* \brief QEvent class and basic macros used by all QP components.
*
* This header file must be included, perhaps indirectly, in all modules
* (*.c files) that use any component of QP/C (such as QEP, QF, or QK).
*/

/****************************************************************************/
/** \brief The current QP version number
*
* \return version of the QP as a hex constant constant 0xXYZZ, where X is
* a 1-digit major version number, Y is a 1-digit minor version number, and
* ZZ is a 2-digit release number.
*/
#define QP_VERSION      0x4200U

#ifndef Q_ROM
    /** \brief Macro to specify compiler-specific directive for placing a
    * constant object in ROM.
    *
    * Many compilers for 8-bit Harvard-architecture MCUs provide non-stanard
    * extensions to support placement of objects in different memories.
    * In order to conserve the precious RAM, QP uses the Q_ROM macro for
    * all constant objects that can be allocated in ROM.
    *
    * To override the following empty definition, you need to define the
    * Q_ROM macro in the qep_port.h header file. Some examples of valid
    * Q_ROM macro definitions are: __code (IAR 8051 compiler), code (Keil
    * Cx51 compiler), PROGMEM (gcc for AVR), __flash (IAR for AVR).
    */
    #define Q_ROM
#endif
#ifndef Q_ROM_VAR         /* if NOT defined, provide the default definition */

    /** \brief Macro to specify compiler-specific directive for accessing a
    * constant object in ROM.
    *
    * Many compilers for 8-bit MCUs provide different size pointers for
    * accessing objects in various memories. Constant objects allocated
    * in ROM (see #Q_ROM macro) often mandate the use of specific-size
    * pointers (e.g., far pointers) to get access to ROM objects. The
    * macro Q_ROM_VAR specifies the kind of the pointer to be used to access
    * the ROM objects.
    *
    * To override the following empty definition, you need to define the
    * Q_ROM_VAR macro in the qep_port.h header file. An example of valid
    * Q_ROM_VAR macro definition is: __far (Freescale HC(S)08 compiler).
    */
    #define Q_ROM_VAR
#endif
#ifndef Q_ROM_BYTE
    /** \brief Macro to access a byte allocated in ROM
    *
    * Some compilers for Harvard-architecture MCUs, such as gcc for AVR, do
    * not generate correct code for accessing data allocated in the program
    * space (ROM). The workaround for such compilers is to explictly add
    * assembly code to access each data element allocated in the program
    * space. The macro Q_ROM_BYTE() retrieves a byte from the given ROM
    * address.
    *
    * The Q_ROM_BYTE() macro should be defined for the compilers that
    * cannot handle correctly data allocated in ROM (such as the gcc).
    * If the macro is left undefined, the default definition simply returns
    * the argument and lets the compiler generate the correct code.
    */
    #define Q_ROM_BYTE(rom_var_)   (rom_var_)
#endif

/****************************************************************************/
#ifndef Q_SIGNAL_SIZE

    /** \brief The size (in bytes) of the signal of an event. Valid values:
    * 1, 2, or 4; default 1
    *
    * This macro can be defined in the QEP port file (qep_port.h) to
    * configure the ::QSignal type. When the macro is not defined, the
    * default of 2 bytes is chosen.
    */
    #define Q_SIGNAL_SIZE 2
#endif
#if (Q_SIGNAL_SIZE == 1)
    typedef uint8_t QSignal;
#elif (Q_SIGNAL_SIZE == 2)
    /** \brief QSignal represents the signal of an event.
    *
    * The relationship between an event and a signal is as follows. A signal
    * in UML is the specification of an asynchronous stimulus that triggers
    * reactions [<A HREF="http://www.omg.org/docs/ptc/03-08-02.pdf">UML
    * document ptc/03-08-02</A>], and as such is an essential part of an
    * event. (The signal conveys the type of the occurrence-what happened?)
    * However, an event can also contain additional quantitative information
    * about the occurrence in form of event parameters.
    */
    typedef uint16_t QSignal;
#elif (Q_SIGNAL_SIZE == 4)
    typedef uint32_t QSignal;
#else
    #error "Q_SIGNAL_SIZE defined incorrectly, expected 1, 2, or 4"
#endif

/****************************************************************************/
/** \brief Event structure.
*
* QEvent represents events without parameters and serves as the base structure
* for derivation of events with parameters.
*
* The following example illustrates how to add an event parameter by
* derivation of the QEvent structure. Please note that the QEvent member
* super_ is defined as the FIRST member of the derived struct.
* \include qep_qevent.c
*
* \sa \ref derivation
*/
typedef struct QEventTag {
    QSignal sig;                          /**< signal of the event instance */
    uint8_t poolId_;                      /**< pool ID (0 for static event) */
    uint8_t refCtr_;                                 /**< reference counter */
} QEvent;

/****************************************************************************/
/** helper macro to calculate static dimension of a 1-dim array \a array_ */
#define Q_DIM(array_) (sizeof(array_) / sizeof(array_[0]))

#endif                                                          /* qevent_h */
