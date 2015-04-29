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
#else
    (void)port;
#endif /* DEFINE_XMLRPC */
}

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
#else
    (void)buf;
#endif /* DEFINE_XMLRPC */
}

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

#ifdef DEFINE_MAIN
// only enable the attribute map functionality when test main is defined
#define MAX_OBJ_MAPS    8
#define MAX_OBJ_ATTRS  16
#define MAX_STR_LEN    32
typedef struct AttrMap {
    char attr[MAX_STR_LEN];
    bool flag;
} AttrMap;
typedef struct ObjAttrMap {
    void* obj;
    AttrMap attrMap[MAX_OBJ_ATTRS];
    int freeAttr;
    struct ObjAttrMap* next;
} ObjAttrMap;

ObjAttrMap objAttrMap[MAX_OBJ_MAPS];
ObjAttrMap* mapHead = 0;
ObjAttrMap* freeHead = 0;
int objCnt = 0;
#endif /* DEFINE_MAIN */

////////////////////////////////////////////////////////////////////////////
// @fn private AttributeMapper::findObj()
// @brief Utility method to look-up the index of the struct array at which
// an attribute map of the the object was created.
// @param pointer to object for which to find map
// @return int index in attribute map array for the object
////////////////////////////////////////////////////////////////////////////
ObjAttrMap* AttributeMapper_findObj (void* obj) {
#ifdef DEFINE_MAIN
    for (ObjAttrMap* p=mapHead; p != 0; p=p->next) {
        if (p->obj == obj) {
            return p;
        }
    }
    return 0;  // not found!
#endif /* DEFINE_MAIN */
}
AttrMap* AttributeMapper_findAttr (void* obj, const char* attr) {
#ifdef DEFINE_MAIN
    AttrMap* rv = 0;  // not found at first
    if (0 != attr) {
        // find the attribute map for the given object pointer
        ObjAttrMap* p = AttributeMapper_findObj(obj);
//        printf(">> Found objAttrMap %p for obj %p\n", p, obj);
        if (p != 0) {  // found, now seek the attribute
            for (int i=0; i < p->freeAttr; ++i) {
//                printf(">> Comparing keys of attr '%s' and mapped attr '%s'\n",
//                        attr, (p->attrMap[i].attr != 0 ? p->attrMap[i].attr : "(null)"));
                if (p->attrMap[i].attr != 0
                        && 0 == strcmp(attr, p->attrMap[i].attr)) {
                    rv = &(p->attrMap[i]);  // found the attr map
                    break;
                }
            }
        }
    }
    return rv;
#endif /* DEFINE_MAIN */
}

////////////////////////////////////////////////////////////////////////////
// @fn private AttributeMapper::hashcode()
// @brief Utility method to convert a String to a hash value.
// Computes the hash code of the string using Java's String.toString()
// algorithm:  s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1] where
// n is the length of the string s, s[0] is the first character, and ^
// is exponentiation.
// @param String to compute hash value on
// @return int hash code of the supplied string
////////////////////////////////////////////////////////////////////////////
int AttributeMapper_hashcode (const char* str) {
    int h = 0;
    for (const char* c = str; *c != '\0'; ++c) {
        h = 31*h + *c;
    }
    return h;
}

void AttributeMapper_delete (ObjAttrMap* ptr) {
#ifdef DEFINE_MAIN  // enable attribute map functionality when test main defined
    if (ptr != 0) {
        ptr->obj = 0;
        ptr->freeAttr = -1;
        ptr->next = freeHead;  // current first is the new next
        freeHead = ptr;
        // clean all attribute-value pairs
        for (int i=0; i < MAX_OBJ_ATTRS; ++i) {
            for (int j=0; j < MAX_STR_LEN; ++j) {
                freeHead->attrMap[i].attr[j] = '\0';  // no attribute stored
            }
            freeHead->attrMap[i].flag = 0;  // reset to false
        }
    }
#endif /* DEFINE_MAIN */
}

void AttributeMapper_init (void* obj) {
#ifdef DEFINE_MAIN  // enable attribute map functionality when test main defined
    // lazy initialization of the free map nodes
    if (0 == objCnt && 0 == freeHead) {  // not used, initialize it
        // let's "delete" each of the objAttrMap from the back front-ward
        for (int i=(MAX_OBJ_MAPS-1); i >= 0; --i) {
            AttributeMapper_delete(&(objAttrMap[i]));
        }
    }
    if (0 == AttributeMapper_findObj(obj)) {
        // initialize next free attribute map for supplied object
        if (0 != freeHead) {  // good, still has free nodes
            ObjAttrMap* nextFree = freeHead->next;
            freeHead->obj = obj;
            freeHead->freeAttr = 0;  // point to first one as next free
            freeHead->next = mapHead;
            mapHead = freeHead;   // update map head pointer
            freeHead = nextFree;  // update free pointer
#ifdef DEFINE_C_UNITTEST
            printf("New freeHead %p, mapHead %p with obj %p, freeAttr idx %d, and next %p\n",
                    freeHead, mapHead, mapHead->obj, mapHead->freeAttr, mapHead->next);
#endif
        } else {
            printf("FATAL! No more slots to init new ObjAttrMap! Max: %u\n", MAX_OBJ_MAPS);
        }
    }
#endif /* DEFINE_MAIN */
}

void AttributeMapper_set (void* obj, const char* attr, bool flag) {
#ifdef DEFINE_MAIN  // enable attribute map functionality when test main defined
    AttrMap* mapPtr = AttributeMapper_findAttr(obj, attr);
    if (0 == mapPtr) {
        // find the obj attr map first
        ObjAttrMap* p = AttributeMapper_findObj(obj);
        if (p != 0) {  // found, now create new attribute
            if (p->freeAttr >= MAX_OBJ_ATTRS) {
                printf("FATAL! No more attribute slots in AttrMap for obj %p! Max: %u\n", obj, MAX_OBJ_ATTRS);
            } else {
                mapPtr = &(p->attrMap[p->freeAttr++]);  // freeAttr will point to next loc!
                strcpy(mapPtr->attr, attr);  // copy attr key string
            }
        } else {
            printf("AttributeMapper for object %p not yet initialized!\n", obj);
        }
    }  // have attribute map to work with
    mapPtr->flag = flag;
    printf("(on obj %p) %s set to %s\n", obj, attr, AttributeMapper_booltostr(mapPtr->flag));
#endif /* DEFINE_MAIN */
}

bool AttributeMapper_get (void* obj, char* attr) {
#ifdef DEFINE_MAIN  // enable attribute map functionality when test main defined
    bool rv = false;
    AttrMap* mapPtr = AttributeMapper_findAttr(obj, attr);
    if (0 != mapPtr) {  // found the attribute map
        rv = mapPtr->flag;
        printf("(on obj %p) %s == %s\n", obj, attr, AttributeMapper_booltostr(rv));
    } else {
        printf("Warning! expected to find attribute map for obj %p, attr '%s'!\n", obj, attr);
    }
    return rv;
#endif /* DEFINE_MAIN */
}

void AttributeMapper_clean (void* obj) {
#ifdef DEFINE_MAIN  // enable attribute map functionality when test main defined
    // reset the attributes for supplied object, then add back to free map
    ObjAttrMap* p = AttributeMapper_findObj(obj);
    if (p != 0) {  // zero out pointer value in obj attr map
        AttributeMapper_delete(p);
    }
#endif /* DEFINE_MAIN */
}

bool AttributeMapper_strtobool (const char* valStr) {
    if (strcmp(valStr, "True") == 0 || strcmp(valStr, "true") == 0) {
        return 1;
    } else {
        return 0;
    }
}

char* AttributeMapper_booltostr (bool flag) {
    if (flag) {
        return "True";
    } else {
        return "False";
    }
}
