package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    private final Tryte[] data;

    private final Tryte earliestTime;
    private final Tryte latestTime;

    public Effect(final Tryte[] data, final Tryte earliestTime, final Tryte latestTime) {

        this.data = new Tryte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        this.earliestTime = earliestTime;
        this.latestTime = latestTime;
    }

    public Effect(final Tryte[] data) {

        this(data, Tryte.ZERO, Tryte.ZERO);
    }

    public Tryte[] getData() {

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

        if (getEarliestTime().getValue() < effect.getEarliestTime().getValue()) {

            return -1;

        } else if (getEarliestTime().getValue() > effect.getEarliestTime().getValue()) {

            return 1;

        } else {

            return hashCode() - effect.hashCode();
        }
    }
}
