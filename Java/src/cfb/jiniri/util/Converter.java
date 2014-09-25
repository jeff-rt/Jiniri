package cfb.jiniri.util;

import cfb.jiniri.ternary.Tryte;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Converter {

    private static final int TRYTE_SIZE = Integer.BYTES + Short.BYTES;

    public static byte[] getBytes(final Tryte[] trytes) {

        final ByteBuffer buffer = ByteBuffer.allocate(trytes.length * TRYTE_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (final Tryte tryte : trytes) {

            buffer.putInt((int)tryte.getValue());
            buffer.putShort((short)(tryte.getValue() >> 32));
        }

        return buffer.array(); // TODO: Rewrite (not guaranteed to work)
    }

    public static Tryte[] getTrytes(final byte[] bytes) {

        if (bytes.length % TRYTE_SIZE != 0) {

            throw new IllegalArgumentException("Illegal number of bytes: " + bytes.length);
        }

        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        final Tryte[] trytes = new Tryte[bytes.length / TRYTE_SIZE];
        for (int i = 0; i < trytes.length; i++) {

            trytes[i] = new Tryte((buffer.getInt() & 0xFFFFFFFFL) | ((buffer.getShort() & 0xFFFFL) << 32));
        }

        return trytes;
    }
}
