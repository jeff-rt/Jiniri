package cfb.jiniri.util;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Converter {

    public static final int NUMBER_OF_BITS = 2;
    private static final int MASK = (1 << NUMBER_OF_BITS) - 1;

    public static byte[] getBytes(final Trit[] trits) {

        final byte[] bytes = new byte[(trits.length * NUMBER_OF_BITS + NUMBER_OF_BITS + Byte.SIZE - 1) / Byte.SIZE];

        int i = 0;
        while (i < trits.length) {

            if (trits[i] == Trit.TRUE) {

                bytes[(i * NUMBER_OF_BITS + Byte.SIZE - 1) / Byte.SIZE - 1] |= (1 << ((i & MASK) * NUMBER_OF_BITS));

            } else if (trits[i] == Trit.FALSE) {

                bytes[(i * NUMBER_OF_BITS + Byte.SIZE - 1) / Byte.SIZE - 1] |= (2 << ((i & MASK) * NUMBER_OF_BITS));
            }

            i++;
        }
        bytes[(i * NUMBER_OF_BITS + Byte.SIZE - 1) / Byte.SIZE - 1] |= (3 << ((i & MASK) * NUMBER_OF_BITS));

        return bytes;
    }

    public static Trit[] getTrits(final byte[] bytes) {

        final List<Trit> trits = new LinkedList<>();

        int i = 0;
        while (true) {

            switch ((bytes[i / Byte.SIZE] >> ((i & MASK) * NUMBER_OF_BITS)) & MASK) {

                case 0: {

                    trits.add(Trit.UNKNOWN);

                } break;

                case 1: {

                    trits.add(Trit.TRUE);

                } break;

                case 2: {

                    trits.add(Trit.FALSE);

                } break;

                default: {

                    return trits.toArray(new Trit[trits.size()]);
                }
            }
        }
    }

    public static Trit[] combine(final Tryte... trytes) {

        final List<Trit> trits = new LinkedList<>();
        for (final Tryte tryte : trytes) {

            trits.addAll(Arrays.asList(tryte.getTrits()));
        }

        return trits.toArray(new Trit[trits.size()]);
    }
}
