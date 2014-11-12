package cfb.jiniri.model;

import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    private final Tryte earliestTime;
    private final Tryte latestTime;

    private final Trit[] data;

    public Effect(final Tryte earliestTime, final Tryte latestTime, final Trit[] data) {

        this.earliestTime = earliestTime;
        this.latestTime = latestTime;

        this.data = new Trit[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public Effect(final Trit[] data) {

        this(Tryte.ZERO, Tryte.ZERO, data);
    }

    public Tryte getEarliestTime() {

        return earliestTime;
    }

    public Tryte getLatestTime() {

        return latestTime;
    }

    public Trit[] getData() {

        return data;
    }

    public int getDataSize() {

        return getData().length;
    }

    @Override
    public int compareTo(final Effect effect) {

        if (getEarliestTime().cmp(effect.getEarliestTime()).getIntValue() < 0) {

            return -1;

        } else if (getEarliestTime().cmp(effect.getEarliestTime()).getIntValue() > 0) {

            return 1;

        } else {

            return hashCode() - effect.hashCode();
        }
    }
}
