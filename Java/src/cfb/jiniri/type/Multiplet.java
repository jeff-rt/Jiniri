package cfb.jiniri.type;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

import java.math.BigInteger;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Multiplet {

    private static final BigInteger COEFFICIENT = BigInteger.valueOf(Trit.RADIX).pow(Tryte.NUMBER_OF_TRITS);

    protected final Tryte[] trytes;

    protected Multiplet(final int width) {

        trytes = new Tryte[width];
        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = Tryte.ZERO;
        }
    }

    public int getWidth() {

        return trytes.length;
    }

    public Multiplet not() {

        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = trytes[i].not();
        }

        return this;
    }

    public Multiplet and(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = trytes[i].and(multiplet.trytes[i]);
        }

        return this;
    }

    public Multiplet or(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = trytes[i].or(multiplet.trytes[i]);
        }

        return this;
    }

    public Multiplet xor(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = trytes[i].xor(multiplet.trytes[i]);
        }

        return this;
    }

    public Multiplet neg() {

        for (int i = 0; i < getWidth(); i++) {

            trytes[i] = trytes[i].neg();
        }

        return this;
    }

    public Multiplet add(final Multiplet multiplet) {

        validateWidth(multiplet);

        Tryte previousOverflow = Tryte.ZERO;
        for (int i = 0; i < getWidth(); i++) {

            Tryte currentOverflow = trytes[i].addOverflow(previousOverflow);
            trytes[i] = trytes[i].add(previousOverflow);
            currentOverflow = currentOverflow.add(trytes[i].addOverflow(multiplet.trytes[i]));
            trytes[i] = trytes[i].add(multiplet.trytes[i]);
            previousOverflow = currentOverflow;
        }

        return this;
    }

    public Multiplet sub(final Multiplet multiplet) {

        return add(multiplet.clone().neg());
    }

    public Multiplet mul(final Multiplet multiplet) {

        validateWidth(multiplet);

        // TODO: Implement after http://arxiv.org/ftp/arxiv/papers/1407/1407.3360.pdf reviewed
        throw new UnsupportedOperationException("Multiplet.mul(Multiplet) not implemented");
    }

    public Multiplet div(final Multiplet multiplet) {

        validateWidth(multiplet);

        if (multiplet.isZero()) {

            throw new IllegalArgumentException("Division by zero");
        }

        // TODO: Implement after http://arxiv.org/ftp/arxiv/papers/1407/1407.3360.pdf reviewed
        throw new UnsupportedOperationException("Multiplet.div(Multiplet) not implemented");
    }

    public Multiplet mod(final Multiplet multiplet) {

        validateWidth(multiplet);

        if (multiplet.isZero()) {

            throw new IllegalArgumentException("Modulo by zero");
        }

        // TODO: Implement after http://arxiv.org/ftp/arxiv/papers/1407/1407.3360.pdf reviewed
        throw new UnsupportedOperationException("Multiplet.mod(Multiplet) not implemented");
    }

    public boolean isZero() {

        for (final Tryte tryte : trytes) {

            if (tryte.getValue() != Tryte.ZERO_VALUE) {

                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {

        BigInteger value = BigInteger.ZERO;
        for (int i = getWidth(); i-- > 0; ) {

            value = value.multiply(COEFFICIENT).add(BigInteger.valueOf(trytes[i].getValue()));
        }

        return value.toString();
    }

    @Override
    public int hashCode() {

        int hashCode = 0;
        for (int i = 0; i < getWidth(); i++) {

            hashCode ^= trytes[i].hashCode() * (i + 1);
        }

        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof Multiplet)) {

            return false;
        }

        final int minWidth = getWidth() <= ((Multiplet)obj).getWidth() ? getWidth() : ((Multiplet)obj).getWidth();
        final int maxWidth = getWidth() >= ((Multiplet)obj).getWidth() ? getWidth() : ((Multiplet)obj).getWidth();
        final Tryte[] widerMultipletTrytes = getWidth() >= ((Multiplet)obj).getWidth() ? trytes : ((Multiplet)obj).trytes;
        for (int i = 0; i < minWidth; i++) {

            if (trytes[i].getValue() != ((Multiplet)obj).trytes[i].getValue()) {

                return false;
            }
        }
        for (int i = minWidth; i < maxWidth; i++) {

            if (widerMultipletTrytes[i].getValue() != Tryte.ZERO_VALUE) {

                return false;
            }
        }

        return true;
    }

    @Override
    public Multiplet clone() {

        final Multiplet multiplet = new Multiplet(getWidth());
        for (int i = 0; i < multiplet.getWidth(); i++) {

            multiplet.trytes[i] = trytes[i];
        }

        return multiplet;
    }

    private void validateWidth(final Multiplet multiplet) {

        if (multiplet.getWidth() != getWidth()) {

            throw new IllegalArgumentException("Non-matching width: " + multiplet.getWidth());
        }
    }
}
