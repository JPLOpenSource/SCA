#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "log_event.h"

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

////////////////////////////////////////////////////////////////////////////
// @fn private AttributeMapper::findObj()
// @brief Utility method to look-up the index of the struct array at which
// an attribute map of the the object was created.
// @param pointer to object for which to find map
// @return int index in attribute map array for the object
////////////////////////////////////////////////////////////////////////////
ObjAttrMap* AttributeMapper_findObj (void* obj)
{
	for (ObjAttrMap* p=mapHead; p != 0; p=p->next) {
		if (p->obj == obj) {
			return p;
		}
	}
	return 0;  // not found!
}
AttrMap* AttributeMapper_findAttr (void* obj, const char* attr)
{
	AttrMap* rv = 0;  // not found at first
	if (0 != attr) {
		// find the attribute map for the given object pointer
		ObjAttrMap* p = AttributeMapper_findObj(obj);
		//		  printf(">> Found objAttrMap %p for obj %p\n", p, obj);
		if (p != 0) {  // found, now seek the attribute
			for (int i=0; i < p->freeAttr; ++i) {
				//				  printf(">> Comparing keys of attr '%s' and mapped attr '%s'\n",
				//						  attr, (p->attrMap[i].attr != 0 ? p->attrMap[i].attr : "(null)"));
				if (p->attrMap[i].attr != 0
					&& 0 == strcmp(attr, p->attrMap[i].attr)) {
					rv = &(p->attrMap[i]);  // found the attr map
					break;
				}
			}
		}
	}
	return rv;
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
int AttributeMapper_hashcode (const char* str)
{
	int h = 0;
	for (const char* c = str; *c != '\0'; ++c) {
		h = 31*h + *c;
	}
	return h;
}

void AttributeMapper_delete (ObjAttrMap* ptr)
{
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

}

void AttributeMapper_init (void* obj)
{
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
}

void AttributeMapper_set (void* obj, const char* attr, bool flag)
{
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
	fflush(stdout);
}

bool AttributeMapper_get (void* obj, char* attr)
{
	bool rv = false;
	AttrMap* mapPtr = AttributeMapper_findAttr(obj, attr);
	if (0 != mapPtr) {  // found the attribute map
		rv = mapPtr->flag;
		printf("(on obj %p) %s == %s\n", obj, attr, AttributeMapper_booltostr(rv));
	} else {
		printf("Warning! expected to find attribute map for obj %p, attr '%s'!\n", obj, attr);
	}
	return rv;

}

void AttributeMapper_clean (void* obj)
{
	// reset the attributes for supplied object, then add back to free map
	ObjAttrMap* p = AttributeMapper_findObj(obj);
	if (p != 0) {  // zero out pointer value in obj attr map
		AttributeMapper_delete(p);
	}
}

bool AttributeMapper_strtobool (const char* valStr)
{
	if (strcmp(valStr, "True") == 0 || strcmp(valStr, "true") == 0) {
		return 1;
	} else {
		return 0;
	}
}

char* AttributeMapper_booltostr (bool flag)
{
	if (flag) {
		return "True";
	} else {
		return "False";
	}
}
