package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.operation.Conductor;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Core implements Conductor {

    private final Processor processor;

    private final int memoryCapacity;

    private Entity entity;

    private Trit[] scratchpad;

    protected Core(final Processor processor, final int memoryCapacity) {

        this.processor = processor;

        this.memoryCapacity = memoryCapacity;
    }

    protected void deploy(final Entity entity) {

        this.entity = entity;
    }

    protected void react(final Effect[] effects) {

        for (final Effect effect : effects) {

            scratchpad = new Trit[memoryCapacity - entity.getStateSize() - effect.getDataSize()];
            entity.react(effect.getData(), scratchpad, this);
        }

        processor.salvage(this);
    }

    @Override
    public void create(final Tryte type, final Tryte pointer, final Tryte size) {

        final Trit[] data = new Trit[size.getIntValue()];
        System.arraycopy(scratchpad, pointer.getIntValue(), data, 0, data.length);

        processor.create(type, data);
    }

    @Override
    public void decay() {

        processor.destroy(entity);
    }

    @Override
    public void get(final Tryte type) {
    }

    @Override
    public void add(final Tryte pointer, final Tryte size, final Tryte environmentId) {
    }

    @Override
    public void remove(final Tryte type) {
    }

    @Override
    public void affect(final Tryte environmentId, final Tryte pointer, final Tryte size,
                       final Tryte power, final Tryte delay, final Tryte duration) {

        final Trit[] data = new Trit[size.getIntValue()];
        System.arraycopy(scratchpad, pointer.getIntValue(), data, 0, data.length);

        processor.affect(environmentId, data, delay, duration, power);
    }

    @Override
    public void join(final Tryte environmentId) {

        processor.include(entity, environmentId);
    }

    @Override
    public void leave(final Tryte environmentId) {

        processor.exclude(entity, environmentId);
    }
}
