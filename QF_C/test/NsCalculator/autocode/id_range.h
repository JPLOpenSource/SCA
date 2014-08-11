#ifndef IDRANGE_H
#define IDRANGE_H

enum Id_Range {
    ID_RANGE_SIZE  = 0x01000,
	ID_RANGE_FIRST = 0x10000,
    ID_RANGE_LAST  = ID_RANGE_FIRST + 0x10 * ID_RANGE_SIZE /* == 0x20000 */
};

#endif /* IDRANGE_H */
