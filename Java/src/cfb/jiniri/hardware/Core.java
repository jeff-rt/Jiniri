package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.operation.Conductor;
import cfb.jiniri.type.Nonet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Core implements Conductor {

    private final Processor processor;

    private final int memoryCapacity;

    private Entity entity;

    private Singlet[] scratchpad;

    protected Core(final Processor processor, final int memoryCapacity) {

        this.processor = processor;

        this.memoryCapacity = memoryCapacity;
    }

    protected void deploy(final Entity entity) {

        this.entity = entity;
    }

    protected void react(final Effect[] effects) {

        for (final Effect effect : effects) {

            scratchpad = new Singlet[memoryCapacity - entity.getStateSize() - effect.getDataSize()];
            entity.react(effect.getData(), scratchpad, this);
        }
    }

    @Override
    public void create(final Nonet type, final Singlet pointer, final Singlet size) {

        final Singlet[] data = new Singlet[(int)size.get().getValue()];
        for (int i = 0; i < data.length; i++) {

            data[i] = (Singlet)scratchpad[((int)pointer.get().getValue()) + i].clone();
        }

        processor.create(type, data);
    }

    @Override
    public void decay() {

        processor.destroy(entity.getId());
    }

    @Override
    public void affect(final Nonet environmentId, final Singlet delay, final Singlet duration,
                       final Singlet pointer, final Singlet size) {

        final Singlet[] data = new Singlet[(int)size.get().getValue()];
        for (int i = 0; i < data.length; i++) {

            data[i] = (Singlet)scratchpad[((int)pointer.get().getValue()) + i].clone();
        }

        processor.affect(data, entity.getId(), environmentId, delay, duration);
    }

    @Override
    public void join(final Nonet environmentId) {

        processor.include(entity.getId(), environmentId);
    }

    @Override
    public void leave(final Nonet environmentId) {

        processor.exclude(entity.getId(), environmentId);
    }

    @Override
    public void distance(final Nonet environmentId, final Singlet distance) {
    }

    @Override
    public void approach(final Nonet environmentId, final Singlet delta) {
    }

    @Override
    public void retreat(final Nonet environmentId, final Singlet delta) {
    }
}
