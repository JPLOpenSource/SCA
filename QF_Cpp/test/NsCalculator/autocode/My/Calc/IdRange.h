#ifndef MyCalcIdRange_h
#define MyCalcIdRange_h

#include <My/IdRange.h>

namespace My {
    namespace Calc {

enum IdRange {
	ID_RANGE_FIRST = My::ID_RANGE_FIRST + 0x100 /* == 0x11100 */
};

    }
}

#endif /* MyCalcIdRange_h */
