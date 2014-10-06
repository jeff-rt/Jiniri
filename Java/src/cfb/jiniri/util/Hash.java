package cfb.jiniri.util;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Hash {

    public static Nonet hash(final Tryte[] trytes) {

        final byte[] bytes = new byte[Nonet.WIDTH * Converter.TRYTE_SIZE];
        try {

            final byte[] hash = MessageDigest.getInstance("SHA-256").digest(Converter.getBytes(trytes));
            System.arraycopy(hash, 0, bytes, 0, hash.length);

        } catch (final NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return new Nonet(Converter.getTrytes(bytes));
    }
}
