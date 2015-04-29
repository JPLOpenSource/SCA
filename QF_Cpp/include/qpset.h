//////////////////////////////////////////////////////////////////////////////
// Product: QF/C++
// Last Updated for Version: 4.0.00
// Date of the Last Update:  Apr 06, 2008
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
#ifndef qpset_h
#define qpset_h

/// \file
/// \ingroup qf qk
/// \brief platform-independent priority sets of 8 or 64 elements.
///
/// This header file must be included in those QF ports that use the
/// cooperative multitasking QF scheduler or the QK.

                      // external declarations of QF lookup tables used inline
extern uint8_t const Q_ROM Q_ROM_VAR QF_log2Lkup[256];
extern uint8_t const Q_ROM Q_ROM_VAR QF_pwr2Lkup[65];
extern uint8_t const Q_ROM Q_ROM_VAR QF_invPwr2Lkup[65];
extern uint8_t const Q_ROM Q_ROM_VAR QF_div8Lkup[65];

//////////////////////////////////////////////////////////////////////////////
/// \brief Priority Set of up to 8 elements for building various schedulers,
/// but also useful as a general set of up to 8 elements of any kind.
///
/// The priority set represents the set of active objects that are ready to
/// run and need to be considered by scheduling processing. The set is capable
/// of storing up to 8 priority levels.
class QPSet8 {
    //////////////////////////////////////////////////////////////////////////
    /// \brief bimask representing elements of the set
    uint8_t m_bits;

public:

    /// \brief the function evaluates to TRUE if the priority set is empty,
    /// which means that no active objects are ready to run.
    uint8_t isEmpty(void) volatile {
        return (uint8_t)(m_bits == (uint8_t)0);
    }

    /// \brief the function evaluates to TRUE if the priority set has elements,
    /// which means that some active objects are ready to run.
    uint8_t notEmpty(void) volatile {
        return (uint8_t)(m_bits != (uint8_t)0);
    }

    /// \brief the function evaluates to TRUE if the priority set has the
    /// element \a n.
    uint8_t hasElement(uint8_t n) volatile {
        return (uint8_t)((m_bits & Q_ROM_BYTE(QF_pwr2Lkup[n])) != 0);
    }

    /// \brief insert element n_ into the set, n_= 1..8
    void insert(uint8_t n) volatile {
        m_bits |= Q_ROM_BYTE(QF_pwr2Lkup[n]);
    }

    /// \brief remove element n_ from the set, n_= 1..8
    void remove(uint8_t n) volatile {
        m_bits &= Q_ROM_BYTE(QF_invPwr2Lkup[n]);
    }

    /// \brief find the maximum element in the set,
    /// \note the set cannot be empty
    uint8_t findMax(void) volatile {
        return Q_ROM_BYTE(QF_log2Lkup[m_bits]);
    }
};

//////////////////////////////////////////////////////////////////////////////
/// \brief Priority Set of up to 64 elements for building various schedulers,
/// but also useful as a general set of up to 64 elements of any kind.
///
/// The priority set represents the set of active objects that are ready to
/// run and need to be considered by scheduling processing. The set is capable
/// of storing up to 64 priority levels.
///
/// The priority set allows to build cooperative multitasking schedulers
/// to manage up to 64 tasks. It is also used in the Quantum Kernel (QK)
/// preemptive scheduler.
class QPSet64 {

    /// \brief condensed representation of the priority set
    ///
    /// Each bit in the m_bytes attribute represents a byte (8-bits)
    /// in the m_bits[] array. More specifically: \n
    /// bit 0 in m_bytes is 1 when any bit in m_bits[0] is 1 \n
    /// bit 1 in m_bytes is 1 when any bit in m_bits[1] is 1 \n
    /// bit 2 in m_bytes is 1 when any bit in m_bits[2] is 1 \n
    /// bit 3 in m_bytes is 1 when any bit in m_bits[3] is 1 \n
    /// bit 4 in m_bytes is 1 when any bit in m_bits[4] is 1 \n
    /// bit 5 in m_bytes is 1 when any bit in m_bits[5] is 1 \n
    /// bit 6 in m_bytes is 1 when any bit in m_bits[6] is 1 \n
    /// bit 7 in m_bytes is 1 when any bit in m_bits[7] is 1 \n
    uint8_t m_bytes;

    /// \brief Bitmasks representing elements in the set. Specifically: \n
    /// m_bits[0] represent elements 1..8   \n
    /// m_bits[1] represent elements 9..16  \n
    /// m_bits[2] represent elements 17..24 \n
    /// m_bits[3] represent elements 25..32 \n
    /// m_bits[4] represent elements 33..40 \n
    /// m_bits[5] represent elements 41..48 \n
    /// m_bits[6] represent elements 49..56 \n
    /// m_bits[7] represent elements 57..64 \n
    uint8_t m_bits[8];

public:

    /// \brief the function evaluates to TRUE if the priority set is empty,
    /// which means that no active objects are ready to run.
    uint8_t isEmpty(void) volatile {
        return (uint8_t)(m_bytes == (uint8_t)0);
    }

    /// \brief the function evaluates to TRUE if the priority set has elements,
    /// which means that some active objects are ready to run.
    uint8_t notEmpty(void) volatile {
        return (uint8_t)(m_bytes != (uint8_t)0);
    }

    /// \brief the function evaluates to TRUE if the priority set has the
    /// element \a n.
    uint8_t hasElement(uint8_t n) volatile {
        return (uint8_t)((m_bits[Q_ROM_BYTE(QF_div8Lkup[n])]
                 & Q_ROM_BYTE(QF_pwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n]) + 1]))
                != 0);
    }

    /// \brief insert element n_ into the set, n_= 1..64
    void insert(uint8_t n) volatile {
        m_bits[Q_ROM_BYTE(QF_div8Lkup[n])] |= Q_ROM_BYTE(QF_pwr2Lkup[n]);
        m_bytes |= Q_ROM_BYTE(QF_pwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n]) + 1]);
    }

    /// \brief remove element n_ from the set, n_= 1..64
    void remove(uint8_t n) volatile {
        m_bits[Q_ROM_BYTE(QF_div8Lkup[n])] &= Q_ROM_BYTE(QF_invPwr2Lkup[n]);
        if (m_bits[Q_ROM_BYTE(QF_div8Lkup[n])] == (uint8_t)0) {
            m_bytes &=
                Q_ROM_BYTE(QF_invPwr2Lkup[Q_ROM_BYTE(QF_div8Lkup[n]) + 1]);
        }
    }

    /// \brief find the maximum element in the set,
    /// \note the set cannot be empty
    uint8_t findMax(void) volatile {
        uint8_t n = (uint8_t)(Q_ROM_BYTE(QF_log2Lkup[m_bytes]) - 1);
        return (uint8_t)((n << 3) + Q_ROM_BYTE(QF_log2Lkup[m_bits[n]]));
    }
};

#endif                                                              // qpset_h

