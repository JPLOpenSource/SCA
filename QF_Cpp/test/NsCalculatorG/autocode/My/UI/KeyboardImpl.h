//-*- Mode: C++; -*-
//===========================================================================
// This software contains Caltech/JPL confidential information.
//
// Copyright 2009-2011, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship Acknowledged.
// Any commercial use must be negotiated with the Office of Technology
// Transfer at the California Institute of Technology.
//
// This software may be subject to US export control laws and
// regulations. By accepting this document, the user agrees to comply
// with all applicable U.S. export laws and regulations, including the
// International Traffic and Arms Regulations, 22 C.F.R. 120-130 and the
// Export Administration Regulations, 15 C.F.R. 730-744. User has the
// responsibility to obtain export licenses, or other export authority as
// may be required before exporting such information to foreign countries
// or providing access to foreign persons.
//===========================================================================
//
//       File: My/UI/KeyboardImpl.h
// Created on: 30-Jul-2011 10:20:31
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef MyUIKeyboardImpl_h
#define MyUIKeyboardImpl_h

#include <qf_port.h>
#include <qassert.h>

namespace My {
    namespace UI {

class KeyboardImpl {

    friend class KeyboardImplSpy;

public:
    KeyboardImpl ();  // Default constructor
    virtual ~KeyboardImpl ();  // Required virtual destructor

    virtual void setQActive (QActive* active);

    ////////////////////////////////////////////
    // Action and guard implementation methods
    ////////////////////////////////////////////
    virtual void clearAll ();
    virtual void clearEntry ();
    virtual void powerOff ();
    virtual void sendEquals ();
    virtual void sendKey (char arg1);
    virtual void sendOperator (char arg1);
    virtual void sendPoint ();

private:
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* m_active;

};

    }
}

#endif  /* MyUIKeyboardImpl_h */
