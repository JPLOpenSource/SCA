#ifndef log_event_h
#define log_event_h

int LogEvent_defaultPort();
void LogEvent_init(int port);
void LogEvent_log(char *msg);
void LogEvent_read(char *buf);
void LogEvent_clean();

#endif /* log_event_h */
