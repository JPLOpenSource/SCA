/*****************************************************************************
* Product:  QF/C
* Last Updated for Version: 3.3.00
* Date of the Last Update:  Jan 22, 2007
*
*                    Q u a n t u m     L e a P s
*                    ---------------------------
*                    innovating embedded systems
*
* Copyright (C) 2002-2006 Quantum Leaps, LLC. All rights reserved.
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
* e-mail:                  sales@quantum-leaps.com
*****************************************************************************/
#ifndef qpset_h
#define qpset_h

/** \ingroup qf qk
* \file qpset.h
* \brief platform-independent priority sets of 8 or 64 elements.
*
* This header file must be included in those QF ports that use the
* cooperative multitasking QF scheduler or the QK.
*/

/****************************************************************************/
/** \brief a name for QPSet8Tag struct
*
* The priority set represents the set of active objects that are ready to
* run and need to be considered by scheduling processing. The set is capable
* of storing up to 8 priority levels.
*
* The priority set allows to build cooperative multitasking schedulers
* to manage up to 8 tasks.
*
* \sa ::QPSet8Tag for the description of the data members
*/
typedef struct QPSet8Tag QPSet8;
/** \brief Priority Set useful for building various schedulers.
* \sa ::QPSet8
*/
struct QPSet8Tag {

    /** \brief bimask representing elements of the set */
    uint8_t bits__;
};

/** \brief the macro evaluates to TRUE if the priority set \a me is empty
*/
#define QPSet8_isEmpty(me_) ((me_)->bits__ == (uint8_t)0)

/** \brief the macro evaluates to TRUE if the priority set \a me has elements
*/
#define QPSet8_hasElements(me_) (!QPSet8_isEmpty(me_))

/** \brief the macro evaluates to TRUE if the priority set \a me_
* has element \a n_.
*/
#define QPSet8_hasElement(me_, n_) \
    (((me_)->bits__ & Q_ROM_BYTE(QF_pwr2Lkup[n_])) != 0)

/** \brief insert element \a n_ into the set \a me_, n_= 1..8
*/
#define QPSet8_insert(me_, n_) \
    ((me_)->bits__ |= Q_ROM_BYTE(QF_pwr2Lkup[n_]))

/** \brief remove element n_ from the set \a me_, n_= 1..8
*/
#define QPSet8_remove(me_, n_) \
    ((me_)->bits__ &= Q_ROM_BYTE(QF_invPwr2Lkup[n_]))

/** \brief find the maximum element in the set, and assign it to n_,
* \note the set cannot be empty
*/
#define QPSet8_findMax(me_, n_) \
    ((n_) = Q_ROM_BYTE(QF_log2Lkup[(me_)->bits__]))


/****************************************************************************/
/** \brief a name for QPSetTag struct
*
* The priority set represents the set of active objects that are ready to
* run and need to be considered by scheduling processing. The set is capable
* of storing up to 64 priority levels.
*
* The priority set allows to build cooperative multitasking schedulers
* to manage up to 64 tasks. It is also used in the Quantum Kernel (QK)
* preemptive scheduler.
*
* \sa ::QPSetTag for the description of the data members
*/
typedef struct QPSetTag QPSet;
/** \brief Priority Set useful for building various schedulers.
* \sa ::QPSet
*/
struct QPSetTag {

    /** \brief condensed representation of the priority set
    *
    * Each bit in the bytes__ attribute represents a byte (8-bits)
    * in the bits__[] array. More specifically: \n
    * bit 0 in bytes__ is 1 when any bit in bits__[0] is 1 \n
    * bit 1 in bytes__ is 1 when any bit in bits__[1] is 1 \n
    * bit 2 in bytes__ is 1 when any bit in bits__[2] is 1 \n
    * bit 3 in bytes__ is 1 when any bit in bits__[3] is 1 \n
    * bit 4 in bytes__ is 1 when any bit in bits__[4] is 1 \n
    * bit 5 in bytes__ is 1 when any bit in bits__[5] is 1 \n
    * bit 6 in bytes__ is 1 when any bit in bits__[6] is 1 \n
    * bit 7 in bytes__ is 1 when any bit in bits__[7] is 1 \n
    */
    uint8_t bytes__;

    /** \brief Bitmasks representing elements in the set. Specifically: \n
    * bits__[0] represent elements 1..8   \n
    * bits__[1] represent elements 9..16  \n
    * bits__[2] represent elements 17..24 \n
    * bits__[3] represent elements 25..32 \n
    * bits__[4] represent elements 33..40 \n
    * bits__[5] represent elements 41..48 \n
    * bits__[6] represent elements 49..56 \n
    * bits__[7] represent elements 57..64 \n
    */
    uint8_t bits__[8];
};

/** \brief the macro evaluates to TRUE if the priority set \a me is empty
*/
#define QPSet_isEmpty(me_) ((me_)->bytes__ == (uint8_t)0)

/** \brief the macro evaluates to TRUE if the priority set \a me has elements
*/
#define QPSet_hasElements(me_) (!QPSet_isEmpty(me_))

/** \brief the macro evaluates to TRUE if the priority set \a me_
* has element \a n_.
*/
#define QPSet_hasElement(me_, n_) \
    (((me_)->bits__[Q_ROM_BYTE(QF_div8Lkup[n_])] \
      & Q_ROM_BYTE(QF_pwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n_]) + 1])) != 0)

/** \brief insert element \a n_ into the set \a me_, n_= 1..64
*/
#define QPSet_insert(me_, n_) do { \
    (me_)->bits__[Q_ROM_BYTE(QF_div8Lkup[n_])]|= Q_ROM_BYTE(QF_pwr2Lkup[n_]);\
    (me_)->bytes__ |= Q_ROM_BYTE(QF_pwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n_])+1]);\
} while(0)

/** \brief remove element n_ from the set \a me_, n_= 1..64
*/
#define QPSet_remove(me_, n_) do { \
    (me_)->bits__[Q_ROM_BYTE(QF_div8Lkup[n_])] &= \
        Q_ROM_BYTE(QF_invPwr2Lkup[n_]); \
    if ((me_)->bits__[Q_ROM_BYTE(QF_div8Lkup[n_])] == (uint8_t)0) { \
        (me_)->bytes__ &= \
            Q_ROM_BYTE(QF_invPwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n_]) + 1]); \
    } \
} while(0)

/** \brief find the maximum element in the set, and assign it to n_,
* \note the set cannot be empty
*/
#define QPSet_findMax(me_, n_) do { \
    (n_) = (uint8_t)(Q_ROM_BYTE(QF_log2Lkup[(me_)->bytes__]) - 1); \
    (n_) = (uint8_t)(((n_)<<3) + Q_ROM_BYTE(QF_log2Lkup[(me_)->bits__[n_]]));\
} while(0)

#endif                                                           /* qpset_h */

