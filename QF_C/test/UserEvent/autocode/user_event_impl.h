#ifndef UserEventImpl_h
#define UserEventImpl_h

#define MAX_DATA_SIZE 32
typedef struct DataEvent {
    QEvent super;
	char data[MAX_DATA_SIZE];
} DataEvent;

#endif  /* UserEventImpl_h */
