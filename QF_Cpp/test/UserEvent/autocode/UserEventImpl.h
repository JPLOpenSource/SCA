#ifndef UserEventImpl_h
#define UserEventImpl_h

#define MAX_DATA_SIZE 32
struct DataEvent : public QEvent {
	char data[MAX_DATA_SIZE];
};

#endif  /* UserEventImpl_h */
