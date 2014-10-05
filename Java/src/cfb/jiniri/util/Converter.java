package cfb.jiniri.util;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Converter {

    public static final int TRYTE_SIZE = 6;

    public static byte[] getBytes(final Tryte[] trytes) {

        if (trytes == null) {

            return null;
        }

        final byte[] bytes = new byte[trytes.length * TRYTE_SIZE];
        int i = 0;
        for (final Tryte tryte : trytes) {

            long value = tryte.getValue() - Tryte.MIN_VALUE;
            bytes[i++] = (byte)value;
            value >>= 8;
            bytes[i++] = (byte)value;
            value >>= 8;
            bytes[i++] = (byte)value;
            value >>= 8;
            bytes[i++] = (byte)value;
            value >>= 8;
            bytes[i++] = (byte)value;
            value >>= 8;
            bytes[i++] = (byte)value;
        }

        return bytes;
    }

    public static Tryte[] getTrytes(final byte[] bytes) {

        if (bytes == null) {

            return null;
        }

        if (bytes.length % TRYTE_SIZE != 0) {

            throw new IllegalArgumentException("Illegal number of bytes: " + bytes.length);
        }

        final Tryte[] trytes = new Tryte[bytes.length / TRYTE_SIZE];
        for (int i = 0; i < trytes.length; i++) {

            final long value = (bytes[i * TRYTE_SIZE] & 0xFFL)
                    | (((bytes[i * TRYTE_SIZE + 1] & 0xFFL)) << 8)
                    | (((bytes[i * TRYTE_SIZE + 2] & 0xFFL)) << 16)
                    | (((bytes[i * TRYTE_SIZE + 3] & 0xFFL)) << 24)
                    | (((bytes[i * TRYTE_SIZE + 4] & 0xFFL)) << 32)
                    | (((bytes[i * TRYTE_SIZE + 5] & 0xFFL)) << 40);
            trytes[i] = new Tryte(value + Tryte.MIN_VALUE);
        }

        return trytes;
    }

    public static Singlet[] combine(final Multiplet... multiplets) {

        int size = 0;
        for (final Multiplet multiplet : multiplets) {

            size += multiplet.getWidth();
        }

        final Singlet[] singlets = new Singlet[size];
        int i = 0;
        for (final Multiplet multiplet : multiplets) {

            final Tryte[] trytes = multiplet.getTrytes();
            for (int j = 0; j < trytes.length; j++) {

                singlets[i++] = new Singlet(trytes[j]);
            }
        }

        return singlets;
    }
}
