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

            set(i, Tryte.ZERO);
        }
    }

    public int getWidth() {

        return trytes.length;
    }

    public Tryte[] getTrytes() {

        final Tryte[] trytes = new Tryte[getWidth()];
        System.arraycopy(this.trytes, 0, trytes, 0, this.trytes.length);

        return trytes;
    }

    public Tryte get(final int index) {

        return trytes[index];
    }

    public void set(final int index, final Tryte tryte) {

        trytes[index] = tryte;
    }

    public Multiplet not() {

        for (int i = 0; i < getWidth(); i++) {

            set(i, get(i).not());
        }

        return this;
    }

    public Multiplet and(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            set(i, get(i).and(multiplet.get(i)));
        }

        return this;
    }

    public Multiplet or(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            set(i, get(i).or(multiplet.get(i)));
        }

        return this;
    }

    public Multiplet xor(final Multiplet multiplet) {

        validateWidth(multiplet);

        for (int i = 0; i < getWidth(); i++) {

            set(i, get(i).xor(multiplet.get(i)));
        }

        return this;
    }

    public Multiplet neg() {

        for (int i = 0; i < getWidth(); i++) {

            set(i, get(i).neg());
        }

        return this;
    }

    public Multiplet add(final Multiplet multiplet) {

        validateWidth(multiplet);

        Tryte previousOverflow = Tryte.ZERO;
        for (int i = 0; i < getWidth(); i++) {

            Tryte currentOverflow = get(i).addOverflow(previousOverflow);
            set(i, get(i).add(previousOverflow));
            currentOverflow = currentOverflow.add(get(i).addOverflow(multiplet.get(i)));
            set(i, get(i).add(multiplet.get(i)));
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

            value = value.multiply(COEFFICIENT).add(BigInteger.valueOf(get(i).getValue()));
        }

        return value.toString();
    }

    @Override
    public int hashCode() {

        int hashCode = 0;
        for (int i = 0; i < getWidth(); i++) {

            hashCode ^= get(i).hashCode() * (i + 1);
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

            if (get(i).getValue() != ((Multiplet)obj).get(i).getValue()) {

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
        System.arraycopy(trytes, 0, multiplet.trytes, 0, getWidth());

        return multiplet;
    }

    private void validateWidth(final Multiplet multiplet) {

        if (multiplet.getWidth() != getWidth()) {

            throw new IllegalArgumentException("Non-matching width: " + multiplet.getWidth());
        }
    }
}
