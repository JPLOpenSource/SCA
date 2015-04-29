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
//       File: CalculatorImpl.h
// Created on: 30-Jul-2011 10:19:58
//     Author: scheng@jpl.nasa.gov
//
// This file was stubbed by the JPL StateChart Autocoders, which converts UML
// Statecharts, in XML, to a C++ variant of Miro Samek's Quantum Framework.
//===========================================================================
#ifndef CalculatorImpl_h
#define CalculatorImpl_h

#include <qf_port.h>
#include <qassert.h>
/* Submachine impls */
#include <OperandXImpl.h>

class CalculatorImpl {

    friend class CalculatorImplSpy;

public:
    CalculatorImpl ();  // Default constructor
    virtual ~CalculatorImpl ();  // Required virtual destructor

    virtual void setQActive (QActive* active);

    ////////////////////////////////////////////
    // Action and guard implementation methods
    ////////////////////////////////////////////
    virtual void set_isKeyId (bool flag);
    virtual void set_onError (bool flag);
    virtual bool isKeyId (QEvent const* e, char arg2);
    virtual bool onError ();
    virtual void ce ();
    virtual void clearAll ();
    virtual void compute (QEvent const* e);
    virtual void negate ();
    virtual void op (QEvent const* e);
    virtual void reportResult ();
    virtual void updateOperand (QEvent const* e);

private:
    char machineName[256];
    /** Cache of pointer to the container QActive object, for ease of access */
    QActive* m_active;
    // calculator data
    char operand[32];
    char resultStr[32];
    char oper;
    double lOperand;
    double rOperand;
    double result;
    char errStat[256];

};


/**
 * Operand1_OperandXImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
class Operand1_OperandXImpl : public OperandXImpl {

    friend class Operand1_OperandXImplSpy;

public:
    Operand1_OperandXImpl ();  // Default constructor
    virtual ~Operand1_OperandXImpl ();  // Default virtual destructor

    /////////////////////////////////////////////////////////////////////////
    // Override submachine-instance action and guard implementation methods
    /////////////////////////////////////////////////////////////////////////

private:
    char machineName[256];

};

/**
 * Operand2_OperandXImpl SubMachine implementation overriding subclass.
 *
 * Override the action methods of the SubMachine individually if custom
 * behavior is desired.
 */
class Operand2_OperandXImpl : public OperandXImpl {

    friend class Operand2_OperandXImplSpy;

public:
    Operand2_OperandXImpl ();  // Default constructor
    virtual ~Operand2_OperandXImpl ();  // Default virtual destructor

    /////////////////////////////////////////////////////////////////////////
    // Override submachine-instance action and guard implementation methods
    /////////////////////////////////////////////////////////////////////////

private:
    char machineName[256];

};

#endif  /* CalculatorImpl_h */
