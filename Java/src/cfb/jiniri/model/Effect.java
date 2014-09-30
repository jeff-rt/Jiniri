package cfb.jiniri.model;

import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect implements Comparable<Effect> {

    private final Nonet id;

    private final Singlet[] data;

    private final Nonet entityId;

    private final Nonet environmentId;

    private final Singlet time;
    private final Singlet delay;
    private final Singlet duration;

    public Effect(final Nonet id, final Singlet[] data, final Nonet entityId, final Nonet environmentId,
                  final Singlet time, final Singlet delay, final Singlet duration) {

        if (data.length < 1) {

            throw new IllegalArgumentException("No data");
        }

        this.id = (Nonet)id.clone();

        this.data = new Singlet[data.length];
        for (int i = 0; i < this.data.length; i++) {

            this.data[i] = (Singlet)data[i].clone();
        }

        this.entityId = (Nonet)entityId.clone();

        this.environmentId = (Nonet)environmentId.clone();

        this.time = (Singlet)time.clone();
        this.delay = (Singlet)delay.clone();
        this.duration = (Singlet)duration.clone();
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

        final Nonet entityId = new Nonet(trytes, i, Nonet.WIDTH);
        i += Nonet.WIDTH;

        final Nonet environmentId = new Nonet(trytes, i, Nonet.WIDTH);
        i += Nonet.WIDTH;

        final Singlet time = new Singlet(trytes, i, Singlet.WIDTH);
        i += Singlet.WIDTH;

        final Singlet delay = new Singlet(trytes, i, Singlet.WIDTH);
        i += Singlet.WIDTH;

        final Singlet duration = new Singlet(trytes, i, Singlet.WIDTH);

        return new Effect(id, data, entityId, environmentId, time, delay, duration);
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

    public Nonet getEntityId() {

        return entityId;
    }

    public Nonet getEnvironmentId() {

        return environmentId;
    }

    public Singlet getTime() {

        return time;
    }

    public Singlet getDelay() {

        return delay;
    }

    public Singlet getDuration() {

        return duration;
    }

    public Tryte[] getTrytes() {

        final Tryte[] trytes = new Tryte[getId().getWidth() + 1 + getDataSize()
                + getEntityId().getWidth() + getEnvironmentId().getWidth()
                + getTime().getWidth() + getDelay().getWidth() + getDuration().getWidth()];
        int i = 0;

        System.arraycopy(getId().getTrytes(), 0, trytes, i, getId().getWidth());
        i += getId().getWidth();

        trytes[i] = new Tryte(getDataSize());
        i += 1;
        for (int j = 0; j < getData().length; j++) {

            System.arraycopy(getData()[j].getTrytes(), 0, trytes, i, getData()[j].getWidth());
            i += getData()[j].getWidth();
        }

        System.arraycopy(getEntityId().getTrytes(), 0, trytes, i, getEntityId().getWidth());
        i += getEntityId().getWidth();

        System.arraycopy(getEnvironmentId().getTrytes(), 0, trytes, i, getEnvironmentId().getWidth());
        i += getEnvironmentId().getWidth();

        System.arraycopy(getTime().getTrytes(), 0, trytes, i, getTime().getWidth());
        i += getTime().getWidth();

        System.arraycopy(getDelay().getTrytes(), 0, trytes, i, getDelay().getWidth());
        i += getDelay().getWidth();

        System.arraycopy(getDuration().getTrytes(), 0, trytes, i, getDuration().getWidth());

        return trytes;
    }

    @Override
    public int compareTo(final Effect effect) {

        if (getTime().get().getValue() + getDelay().get().getValue() < effect.getTime().get().getValue() + effect.getDelay().get().getValue()) {

            return -1;

        } else if (getTime().get().getValue() + getDelay().get().getValue() > effect.getTime().get().getValue() + effect.getDelay().get().getValue()) {

            return 1;

        } else {

            return getId().cmp(effect.getId());
        }
    }
}
