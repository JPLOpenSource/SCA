/**
 * @file Test5.c
 *
 * This file was generated by the SIM MagicDraw statechart converter,
 * which converts MagicDraw statecharts expressed in XML to Miro Samek's
 * C Quantum Framework.
 *
 * &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 */


#include <stdlib.h>
#include <string.h>
#include "qep_port.h"
#include "qassert.h"
#include "StatechartSignals.h"
#include "Test5.h"
#include "log_event.h"


Test5* Test5_Constructor(Test5* me, char* objNameNew, Test5Impl* implPtr)
{
   QActive_ctor_(&me->super_, (QState)Test5_initial);
   strcpy(me->objName, objNameNew);
   strcat(me->objName, " Test5");
   me->impl = implPtr;

   return me;
}

void Test5_initial(Test5* me,  QEvent const *e)
{
   QActive_subscribe_(& me->super_, Ev2);
   QActive_subscribe_(& me->super_, Ev1);

   Q_INIT(Test5_T5S1);
}

/**
 * Implementation of T5S1
 */
QSTATE Test5_T5S1(Test5* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T5S1");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST5_T5S1;
      strcat(stateName, " ENTRY");
      LogEvent_log(stateName);
      return 0;

     case Q_EXIT_SIG:
      strcat(stateName, " EXIT");
      LogEvent_log(stateName);
      return 0;

     case Ev1:
      strcat(stateName, " Ev1");
      LogEvent_log(stateName);
      Q_TRAN(Test5_T5S2);
      return 0;
   }
   return (QSTATE) QHsm_top;
}

/**
 * Implementation of T5S2
 */
QSTATE Test5_T5S2(Test5* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T5S2");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST5_T5S2;
      strcat(stateName, " ENTRY");
      LogEvent_log(stateName);
      return 0;

     case Q_EXIT_SIG:
      strcat(stateName, " EXIT");
      LogEvent_log(stateName);
      return 0;

     case Ev1:
      strcat(stateName, " Ev1");
      LogEvent_log(stateName);
      Q_TRAN(Test5_T5S1);
      return 0;

     case Ev2:
      strcat(stateName, " Ev2");
      LogEvent_log(stateName);
      Test5Impl_handleEv2(me->impl);
      return 0;
   }
   return (QSTATE) QHsm_top;
}

