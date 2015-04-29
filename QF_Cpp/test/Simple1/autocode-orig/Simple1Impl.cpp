/////////////////////////////////////////////////////////////////////
// Filename:
//   Simple1Impl.cpp
//
// Author:
//   scheng
//
// Description:
//   Implementation class
/////////////////////////////////////////////////////////////////////

#include <stdlib.h>
#include <stdio.h>
#include "qf_port.h"
#include "qassert.h"
#include "Simple1Impl.h"
#include "StatechartSignals.h"
#include "log_event.h"


Simple1Impl::Simple1Impl () {
	this->v = 123;
}

Simple1Impl::~Simple1Impl() {
}

void Simple1Impl::shootMe () {
	printf("Shooting... %d\n", this->v);
}
