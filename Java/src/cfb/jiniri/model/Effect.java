package cfb.jiniri.model;

import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Effect {

    private final Multiplet id;

    private final Singlet[] data;

    private final Multiplet entityId;

    private final Multiplet environmentId;

    private final Singlet time;
    private final Singlet delay;
    private final Singlet duration;

    public Effect(final Multiplet id, final Singlet[] data, final Multiplet entityId, final Multiplet environmentId,
                  final Singlet time, final Singlet delay, final Singlet duration) {

        this.id = id.clone();

        this.data = new Singlet[data.length];
        for (int i = 0; i < this.data.length; i++) {

            this.data[i] = (Singlet)data[i].clone();
        }

        this.entityId = entityId.clone();

        this.environmentId = environmentId.clone();

        this.time = (Singlet)time.clone();
        this.delay = (Singlet)delay.clone();
        this.duration = (Singlet)duration.clone();
    }

    public Multiplet getId() {

        return id;
    }

    public Singlet[] getData() {

        return data;
    }

    public Multiplet getEntityId() {

        return entityId;
    }

    public Multiplet getEnvironmentId() {

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
}
