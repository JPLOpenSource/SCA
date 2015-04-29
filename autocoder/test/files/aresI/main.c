/////////////////////////////////////////////////////////////////////
// Filename:
//     main.c
//
// Author:
//   Garth Watney
//   Ed Benowitz
//
// Description:
//   Test harness Statecharts - C version
/////////////////////////////////////////////////////////////////////

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <sys/types.h>
#include "qf_port.h"
#include "qassert.h"
#include "qf.h"
#include "string.h"

#include "log_event.h"
#include "application.h"
#include "StatechartSignals.h"
#include <assert.h>



Q_DEFINE_THIS_FILE;


typedef uint32_t  U32;
#define EVENT_SIZE 16

typedef struct GenEvt {
   QEvent super_;
   U32  data[EVENT_SIZE];
} GenEvt;


enum {QSIZE = 5};			/* Queue size */
enum {USER_ENTRY_SIZE = 256};		/* Number characters on inline prompt */
enum {SOCK_BUFF_SIZE = 100};

void receiveCmd(char *cmdBuf);


/*.................................................................*/
void Q_assert_handler(char const Q_ROM *file, int line) {
   fprintf(stderr, "Assertion failed in %s, line %d", file, line);
   exit(-1);
}
/*.................................................................*/

int main(int argc, char* argv[])
{
    char cmdBuf[SOCK_BUFF_SIZE];

    printf("Quantum Test\nQEP %s\nQF  %s, QF/Linux port %s\n",
           QEP_getVersion(),
           QF_getVersion(), QF_getPortVersion());

    QF_init(MAX_SIG, sizeof(GenEvt), QSIZE*QSIZE);


    applicationStart(QSIZE);

    for (;;)
    {
      // Get the incoming string command from the dmsTerminal or the GUI
      receiveCmd(cmdBuf);

      char *word;
      word = strtok(cmdBuf, " ");

      // We assume the first word contains the signal that is to be published,
      // and the remaining words are data to be used to populate the event.
      GenEvt *event;
      int signal = strtoul(word, NULL, 10);
      if (signal == DURING)
      {
        QF_tick();
      }
      else
      {
        event = Q_NEW(GenEvt, signal);
        // Loop through the remaining words and populate the event
        int i = 0;
        do
        {
           word = strtok('\0', " ");
           if (word)
           {
             Q_ASSERT(i<EVENT_SIZE);
             event->data[i] = strtoul(word, NULL, 16);
           }
           i = i + 1;
        } while (word);
        QF_publish((QEvent *)event);
      }
      QF_run();
    }
}


////////////////////////////////////////////////////////////////////////////////
// @fn receiveCmd()
// @brief Read the next command from the socket
// @param sockfd
// @return None
////////////////////////////////////////////////////////////////////////////////
void receiveCmd(char *cmdBuf)
{
  scanf("%s", cmdBuf);
}

