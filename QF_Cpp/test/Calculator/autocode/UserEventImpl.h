/**
 * Created on May 14, 2010.
 *
 * @author: Shang-Wen Cheng <Shang-Wen.Cheng@jpl.nasa.gov>
 */
#include <qf_port.h>
#include <StatechartSignals.h>

/**
 * User event for a key press.
 */
class KeyEvent : public QEvent {
public:
	char keyId;
};

/**
 * User event for computation result.
 */
class ResultEvent : public QEvent {
public:
	char result[256];
};
