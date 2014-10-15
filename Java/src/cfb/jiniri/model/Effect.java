package cfb.jiniri.model;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    private final Trit[] data;

    private final Tryte earliestTime;
    private final Tryte latestTime;

    public Effect(final Trit[] data, final Tryte earliestTime, final Tryte latestTime) {

        this.data = new Trit[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        this.earliestTime = earliestTime;
        this.latestTime = latestTime;
    }

    public Effect(final Trit[] data) {

        this(data, Tryte.ZERO, Tryte.ZERO);
    }

    public Trit[] getData() {

        return data;
    }

    public int getDataSize() {

        return getData().length;
    }

    public Tryte getEarliestTime() {

        return earliestTime;
    }

    public Tryte getLatestTime() {

        return latestTime;
    }

    @Override
    public int compareTo(final Effect effect) {

        if (getEarliestTime().cmp(effect.getEarliestTime()) == Tryte.LESS) {

            return -1;

        } else if (getEarliestTime().cmp(effect.getEarliestTime()) == Tryte.GREATER) {

            return 1;

        } else {

            return hashCode() - effect.hashCode();
        }
    }
}
