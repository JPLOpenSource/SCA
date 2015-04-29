/***
 * Originally an event-logging library, it now doubles as an XML-RPC client
 * to communicate with the GUI XML-RPC server.
 *
 * Capability is enabled by defining the environment variable DEFINE_XMLRPC.
 * But the XML-RPC library must also be present to properly link the code.
 */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "log_event.h"

#ifdef DEFINE_XMLRPC
#include <xmlrpc-c/base.h>
#include <xmlrpc-c/client.h>

#define NAME "Python Trace GUI Xmlrpc-c Client"
#define VERSION "1.0"

static void die_if_fault_occurred (xmlrpc_env *env) {
    if (env->fault_occurred) {
        fprintf(stderr, "XML-RPC Fault: %s (%d)\n",
                env->fault_string, env->fault_code);
        exit(1);
    }
}

const int DEFAULT_XMLRPC_PORT = 8091;
char * const XMLRPC_SERVER_URL = "http://localhost:%d/RPC2";
char * const XMLRPC_METHOD_UPDATE = "sendUpdate";
char * const XMLRPC_METHOD_POLLGUI = "pollButtonEvent";

char _xmlrpc_server_url[128];
int _xmlrpc_server_port;
xmlrpc_env _xmlrpc_env;
xmlrpc_value *_xmlrpc_result;
#endif /* DEFINE_XMLRPC */


int LogEvent_defaultPort() {
#ifdef DEFINE_XMLRPC
	return DEFAULT_XMLRPC_PORT;
#else
	return 0;
#endif /* DEFINE_XMLRPC */
}

////////////////////////////////////////////////////////////////////////////////
// @fn LogEventInit()
// @brief Initializes the GUI connection, if any
// @param int port value
// @return None
////////////////////////////////////////////////////////////////////////////////
void LogEvent_init(int port) {
#ifdef DEFINE_XMLRPC
	char urlBuf[128];  // to store URL built from pattern

	/* Construct the server URL from pattern and port, then store it. */
	_xmlrpc_server_port = port;
    sprintf(urlBuf, XMLRPC_SERVER_URL, _xmlrpc_server_port);
    strcpy(_xmlrpc_server_url, urlBuf);

    /* Initialize our error-handling environment. */
    xmlrpc_env_init(&_xmlrpc_env);

    /* Start up our XML-RPC client library. */
    xmlrpc_client_init2(&_xmlrpc_env, XMLRPC_CLIENT_NO_FLAGS, NAME, VERSION, NULL, 0);
    die_if_fault_occurred(&_xmlrpc_env);
#endif /* DEFINE_XMLRPC */
}

////////////////////////////////////////////////////////////////////////////////
// @fn LogEventLog()
// @brief Logs Events
// @param Event message string
// @return None
////////////////////////////////////////////////////////////////////////////////
void LogEvent_log(char *msg) {
#ifdef DEFINE_XMLRPC
    const char *response;

/*    printf("Making XMLRPC call to server url '%s' method '%s' "
           "to update msg '%s'...\n",
           XMLRPC_SERVER_URL, XMLRPC_METHOD_UPDATE, msg);
*/
    /* Make the remote procedure call */
    _xmlrpc_result = xmlrpc_client_call(&_xmlrpc_env, _xmlrpc_server_url, XMLRPC_METHOD_UPDATE,
                                "(s)", msg);
    die_if_fault_occurred(&_xmlrpc_env);

    /* Get our response text and print it out. */
    xmlrpc_read_string(&_xmlrpc_env, _xmlrpc_result, &response);
    die_if_fault_occurred(&_xmlrpc_env);
    //printf("The response is '%s'\n", response);
#endif /* DEFINE_XMLRPC */

    printf("%s\n", msg);
}

////////////////////////////////////////////////////////////////////////////////
// @fn LogEventRead()
// @brief Reads any event from the other end of the logging communication pipe
// @param Storage buffer for read event
// @return None
////////////////////////////////////////////////////////////////////////////////
void LogEvent_read(char *buf) {
#ifdef DEFINE_XMLRPC
    const char *response;

/*    printf("Making XMLRPC call to server url '%s' method '%s' "
           "to request GUI events...\n",
           XMLRPC_SERVER_URL, XMLRPC_METHOD_POLLGUI);
*/
    /* Make the remote procedure call, passing 'true' to block until event! */
    _xmlrpc_result = xmlrpc_client_call(&_xmlrpc_env, _xmlrpc_server_url, XMLRPC_METHOD_POLLGUI,
                                "(b)", 1);
    die_if_fault_occurred(&_xmlrpc_env);

	/* Get our response text and return it. */
	xmlrpc_read_string(&_xmlrpc_env, _xmlrpc_result, &response);
	die_if_fault_occurred(&_xmlrpc_env);
	strcpy(buf, response);
	printf("%s\n", buf);
#endif /* DEFINE_XMLRPC */
}

////////////////////////////////////////////////////////////////////////////////
// @fn LogEventClean()
// @brief Cleans up the GUI connection, if any
// @param None
// @return None
////////////////////////////////////////////////////////////////////////////////
void LogEvent_clean() {
#ifdef DEFINE_XMLRPC
    /* Dispose of our result value. */
    xmlrpc_DECREF(_xmlrpc_result);

    /* Clean up our error-handling environment. */
    xmlrpc_env_clean(&_xmlrpc_env);

    /* Shutdown our XML-RPC client library. */
    xmlrpc_client_cleanup();
#endif /* DEFINE_XMLRPC */
}
