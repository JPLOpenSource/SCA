#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "log_event.h"


////////////////////////////////////////////////////////////////////////////////
// @fn LogEventLog()
// @brief logs Events
// @param Input string
// @return None
////////////////////////////////////////////////////////////////////////////////
void LogEvent_log(char *msg)
{
   printf("%s\n", msg);
   fflush(stdout);
}

