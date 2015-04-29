#ifndef MyIdRange_h
#define MyIdRange_h

#include <IdRange.h>

namespace My {

enum IdRange {
	ID_RANGE_FIRST = ::ID_RANGE_FIRST + 0x1000 /* == 0x11000 */
};

}

#endif /* MyIdRange_h */
