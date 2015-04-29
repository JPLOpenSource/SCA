/* A simple synchronous XML-RPC client written in C, as an example of
   an Xmlrpc-c client.  This invokes the sample.add procedure that the
   Xmlrpc-c example server.c server provides.  I.e. it adds to numbers
   together, the hard way.
*/

#include <stdlib.h>
#include <stdio.h>

#include <xmlrpc-c/base.h>
#include <xmlrpc-c/client.h>

#define NAME "Xmlrpc-c Test Client"
#define VERSION "1.0"

static void die_if_fault_occurred (xmlrpc_env *env) {
    if (env->fault_occurred) {
        fprintf(stderr, "XML-RPC Fault: %s (%d)\n",
                env->fault_string, env->fault_code);
        exit(1);
    }
}

int
main(int           const argc,
     const char ** const argv) {

    xmlrpc_env env;
    xmlrpc_value *result;
    const char * response;
    char * const serverUrl = "http://localhost:8091/RPC2";
    char * const methodName = "sendUpdate";

    if (argc != 2) {
        fprintf(stderr, "This program takes exactly one argument, the test message!\n");
        exit(1);
    }

    /* Initialize our error-handling environment. */
    xmlrpc_env_init(&env);

    /* Start up our XML-RPC client library. */
    xmlrpc_client_init2(&env, XMLRPC_CLIENT_NO_FLAGS, NAME, VERSION, NULL, 0);
    die_if_fault_occurred(&env);

    printf("Making XMLRPC call to server url '%s' method '%s' "
           "to request sendUpdate of msg '%s'...\n",
           serverUrl, methodName, argv[1]);

    /* Make the remote procedure call */
    result = xmlrpc_client_call(&env, serverUrl, methodName,
                                "(s)", argv[1]);
    die_if_fault_occurred(&env);

    /* Get our response text and print it out. */
    xmlrpc_read_string(&env, result, &response);
    die_if_fault_occurred(&env);
    printf("The response is '%s'\n", response);

    /* Dispose of our result value. */
    xmlrpc_DECREF(result);

    /* Clean up our error-handling environment. */
    xmlrpc_env_clean(&env);

    /* Shutdown our XML-RPC client library. */
    xmlrpc_client_cleanup();

    return 0;
}
