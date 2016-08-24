

#include "qep_port.h"
#include "application.h"
#include "qassert.h"

#include <termios.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>


#define MYQUEUESIZE 10

  Simple1Impl mySmImpl;
  Simple1 mySm;
  const QEvent * mySm_queuestorage[MYQUEUESIZE];
  
void applicationStart(int qsize)
{

  Simple1Impl_Constructor(&mySmImpl);
  Simple1_Constructor(&mySm, "mySm", &mySmImpl, 0);
  QActive_start((QActive *) & mySm, 1, mySm_queuestorage,  MYQUEUESIZE, NULL, 0, NULL);

}

////////////////////////////////////////////////////////////////////////////////
//@fn setGuardAttribute()
//@brief 
//@param 
//@return 
////////////////////////////////////////////////////////////////////////////////
void setGuardAttribute (const char *sm, const char *attr, const char *val) {
	printf("Got sm '%s', attr name '%s', and value '%s'\n", sm, attr, val);
	
	if (strcasecmp(sm, "mySm") == 0) {
	   AttributeMapper_set(&(mySmImpl), attr, AttributeMapper_strtobool(val));
	}
}
