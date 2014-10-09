package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    public static final Nonet VIRTUAL_EFFECT_ID = Nonet.ZERO;

    private final Nonet id;

    private final Singlet[] data;

    private final Nonet environmentId;

    private final Singlet earliestTime;
    private final Singlet latestTime;

    public Effect(final Nonet id, final Singlet[] data, final Nonet environmentId,
                  final Singlet earliestTime, final Singlet latestTime) {

        this.id = (Nonet)id.clone();

        this.data = new Singlet[data.length];
        for (int i = 0; i < this.data.length; i++) {

            this.data[i] = (Singlet)data[i].clone();
        }

        this.environmentId = (Nonet)environmentId.clone();

        this.earliestTime = (Singlet)earliestTime.clone();
        this.latestTime = (Singlet)latestTime.clone();
    }

    public Effect(final Singlet[] data) {

        this(VIRTUAL_EFFECT_ID, data, null, Singlet.ZERO, Singlet.ZERO);
    }

    public static Effect getEffect(final Tryte[] trytes) {

        int i = 0;

        final Nonet id = new Nonet(trytes, i, Nonet.WIDTH);
        i += Nonet.WIDTH;

        final Singlet[] data = new Singlet[(int)trytes[i].getValue()];
        i += 1;
        for (int j = 0; j < data.length; j++) {

            data[j] = new Singlet(trytes, i, Singlet.WIDTH);
            i += Singlet.WIDTH;
        }

        final Nonet environmentId = new Nonet(trytes, i, Nonet.WIDTH);
        i += Nonet.WIDTH;

        final Singlet earliestTime = new Singlet(trytes, i, Singlet.WIDTH);
        i += Singlet.WIDTH;

        final Singlet latestTime = new Singlet(trytes, i, Singlet.WIDTH);

        return new Effect(id, data, environmentId, earliestTime, latestTime);
    }

    public Nonet getId() {

        return id;
    }

    public Singlet[] getData() {

        return data;
    }

    public int getDataSize() {

        return getData()[0].getWidth() * getData().length;
    }

    public Nonet getEnvironmentId() {

        return environmentId;
    }

    public Singlet getEarliestTime() {

        return earliestTime;
    }

    public Singlet getLatestTime() {

        return latestTime;
    }

    public Tryte[] getTrytes() {

        final Tryte[] trytes = new Tryte[getId().getWidth() + 1 + getDataSize()
                + getEnvironmentId().getWidth() + getEarliestTime().getWidth() + getLatestTime().getWidth()];
        int i = 0;

        System.arraycopy(getId().getTrytes(), 0, trytes, i, getId().getWidth());
        i += getId().getWidth();

        trytes[i] = new Tryte(getDataSize());
        i += 1;
        for (int j = 0; j < getData().length; j++) {

            System.arraycopy(getData()[j].getTrytes(), 0, trytes, i, getData()[j].getWidth());
            i += getData()[j].getWidth();
        }

        System.arraycopy(getEnvironmentId().getTrytes(), 0, trytes, i, getEnvironmentId().getWidth());
        i += getEnvironmentId().getWidth();

        System.arraycopy(getEarliestTime().getTrytes(), 0, trytes, i, getEarliestTime().getWidth());
        i += getEarliestTime().getWidth();

        System.arraycopy(getLatestTime().getTrytes(), 0, trytes, i, getLatestTime().getWidth());

        return trytes;
    }

    @Override
    public int compareTo(final Effect effect) {

        final int difference = getEarliestTime().cmp(effect.getEarliestTime());

        return difference == 0 ? getId().cmp(effect.getId()) : difference;
    }
}
