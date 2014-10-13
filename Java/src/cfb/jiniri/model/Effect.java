package cfb.jiniri.model;

import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    private final Singlet[] data;

    private final Singlet earliestTime;
    private final Singlet latestTime;

    public Effect(final Singlet[] data, final Singlet earliestTime, final Singlet latestTime) {

        this.data = new Singlet[data.length];
        for (int i = 0; i < this.data.length; i++) {

            this.data[i] = (Singlet)data[i].clone();
        }

        this.earliestTime = (Singlet)earliestTime.clone();
        this.latestTime = (Singlet)latestTime.clone();
    }

    public Effect(final Singlet[] data) {

        this(data, Singlet.ZERO, Singlet.ZERO);
    }

    public Singlet[] getData() {

        return data;
    }

    public int getDataSize() {

        return getData()[0].getWidth() * getData().length;
    }

    public Singlet getEarliestTime() {

        return earliestTime;
    }

    public Singlet getLatestTime() {

        return latestTime;
    }

    @Override
    public int compareTo(final Effect effect) {

        final int difference = getEarliestTime().cmp(effect.getEarliestTime());

        return difference == 0 ? hashCode() - effect.hashCode() : difference;
    }
}
