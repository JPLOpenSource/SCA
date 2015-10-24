/**
 * @file Test2.c
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
#include "Test2.h"
#include "log_event.h"


Test2* Test2_Constructor(Test2* me, char* objNameNew, Test2Impl* implPtr)
{
   QActive_ctor_(&me->super_, (QState)Test2_initial);
   strcpy(me->objName, objNameNew);
   strcat(me->objName, " Test2");
   me->impl = implPtr;

   return me;
}

void Test2_initial(Test2* me,  QEvent const *e)
{
   QActive_subscribe_(& me->super_, Ev2);
   QActive_subscribe_(& me->super_, Ev1);

   Q_INIT(Test2_T2S1);
}

/**
 * Implementation of T2S1
 */
QSTATE Test2_T2S1(Test2* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T2S1");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST2_T2S1;
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
      Q_TRAN(Test2_T2S2);
      return 0;
   }
   return (QSTATE) QHsm_top;
}

/**
 * Implementation of T2S2
 */
QSTATE Test2_T2S2(Test2* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T2S2");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST2_T2S2;
      strcat(stateName, " ENTRY");
      LogEvent_log(stateName);
      return 0;

     case Q_EXIT_SIG:
      strcat(stateName, " EXIT");
      LogEvent_log(stateName);
      return 0;

     case Q_INIT_SIG:
      Q_INIT(Test2_T2S2_T2S21);
      return 0;

     case Ev1:
      strcat(stateName, " Ev1");
      LogEvent_log(stateName);
      Q_TRAN(Test2_T2S1);
      return 0;
   }
   return (QSTATE) QHsm_top;
}

/**
 * Implementation of T2S21
 */
QSTATE Test2_T2S2_T2S21(Test2* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T2S21");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST2_T2S2_T2S21;
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
      Q_TRAN(Test2_T2S2_T2S22);
      return 0;
   }
   return (QSTATE) Test2_T2S2;
}

/**
 * Implementation of T2S22
 */
QSTATE Test2_T2S2_T2S22(Test2* me, QEvent const *e)
{
   char stateName[256];

   strcpy(stateName, me->objName);
   strcat(stateName, "T2S22");

   switch (e->sig)
   {
     case Q_ENTRY_SIG:
      me->mystate = TEST2_T2S2_T2S22;
      strcat(stateName, " ENTRY");
      LogEvent_log(stateName);
      return 0;

     case Q_EXIT_SIG:
      strcat(stateName, " EXIT");
      LogEvent_log(stateName);
      return 0;

     case Ev2:
      strcat(stateName, " Ev2");
      LogEvent_log(stateName);
      Q_TRAN(Test2_T2S2_T2S21);
      return 0;
   }
   return (QSTATE) Test2_T2S2;
}
