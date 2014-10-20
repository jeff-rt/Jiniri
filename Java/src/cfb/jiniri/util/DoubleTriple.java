package cfb.jiniri.util;

/**
 * (c) 2014 Come-from-Beyond
 */
public class DoubleTriple {

    public static boolean validateWidth(final int width) {

        int i = 1;
        do {

            if (width == i || width == i * 2) {

                return true;
            }
            i *= 3;

        } while (width <= i);

        return false;
    }
}
