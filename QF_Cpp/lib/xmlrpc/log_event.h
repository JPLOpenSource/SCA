#ifndef log_event_h
#define log_event_h

/**
 * Class defining static methods for logging events.
 */
class LogEvent {
public:
	////////////////////////////////////////////////////////////////////////////
	// @fn LogEvent::defaultPort()
	// @brief Returns the default XML-RPC server port to connect to.
	// @param None
	// @return int port value
	////////////////////////////////////////////////////////////////////////////
	static int defaultPort ();
	////////////////////////////////////////////////////////////////////////////
	// @fn LogEvent::init()
	// @brief Initializes the GUI connection, if any
	// @param int port value
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void init (int port);
	////////////////////////////////////////////////////////////////////////////
	// @fn LogEvent::log()
	// @brief Logs Events
	// @param Event message string
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void log (const char* msg);
	////////////////////////////////////////////////////////////////////////////
	// @fn LogEvent::read()
	// @brief Reads event from the other end of the logging communication pipe
	// @param Storage buffer for read event
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void read (char* buf);
	////////////////////////////////////////////////////////////////////////////
	// @fn LogEvent::clean()
	// @brief Cleans up the GUI connection, if any
	// @param None
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void clean ();
};

/**
 * Class defining static methods for handling Impl guard flags.
 */
class AttributeMapper {
public:
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::init()
	// @brief Initializes a map for the given SM Impl object
	// @param Impl object for which to initialize an attribute mapper
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void init (void* obj);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::set()
	// @brief Sets an attribute of given object to true or false
	// @param Impl object for which to set the attribute
	// @param Name string of attribute to set
	// @param New boolean value to set attribute to
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void set (void* obj, const char* attr, bool flag);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::get()
	// @brief Gets the value of the given attribute for the given object
	// @param Impl object for which to get attribute value
	// @param Name string of attribute to get
	// @return boolean value of attribute
	////////////////////////////////////////////////////////////////////////////
	static bool get (void* obj, char* attr);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::clean()
	// @brief Cleans up the attribute map of the given SM Impl object
	// @param Impl object for which to destroy the attribute mapper
	// @return None
	////////////////////////////////////////////////////////////////////////////
	static void clean (void* obj);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::strtobool()
	// @brief Utility method to convert a String to a boolean value
	// @param String representation of boolean: "True" for 1, "False" for 0
	// @return boolean value for string
	////////////////////////////////////////////////////////////////////////////
	static bool strtobool (const char* valStr);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::booltostr()
	// @brief Utility method to convert a boolean value to a String
	// @param boolean value to convert to String: 1=>"True", 0=>"False"
	// @return string representation of boolean value
	////////////////////////////////////////////////////////////////////////////
	static char* booltostr (bool flag);
	////////////////////////////////////////////////////////////////////////////
	// @fn AttributeMapper::hashcode()
	// @brief Utility method to convert a String to a hash value.
	// Computes the hash code of the string using Java's String.toString()
	// algorithm:  s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1] where
	// n is the length of the string s, s[0] is the first character, and ^
	// is exponentiation.
	// @param String to compute hash value on
	// @return int hash code of the supplied string
	////////////////////////////////////////////////////////////////////////////
	static int hashcode (const char* str);
};

#endif /* log_event_h */
