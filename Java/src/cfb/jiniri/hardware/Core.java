package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;
import cfb.jiniri.operation.Routines;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.ternary.Tryte;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Core implements Routines {

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
    public void halt() {

        throw new RuntimeException("Halt");
    }

    @Override
    public void spawn(final Class entityClass,
                      final Tryte initializationDataAddress, final Tryte initializationDataSize,
                      final Tryte priority) {

        final Trit[] initializationData = new Trit[initializationDataSize.getIntValue()];
        System.arraycopy(scratchpad, initializationDataAddress.getIntValue(), initializationData, 0, initializationData.length);

        processor.create(entityClass, initializationData, priority);
    }

    @Override
    public void decay() {

        processor.destroy(entity);
    }

    @Override
    public void affect(final Tryte environment,
                       final Tryte effectDelay, final Tryte effectDataAddress, final Tryte effectDataSize) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(scratchpad, effectDataAddress.getIntValue(), data, 0, data.length);

        processor.affect(environment, effectDelay, data);
    }

    @Override
    public void join(final Tryte environment) {

        processor.include(entity, environment);
    }

    @Override
    public void leave(final Tryte environment) {

        processor.exclude(entity, environment);
    }

    @Override
    public void broadcast(final Tryte channel, final Tryte messageAddress, final Tryte messageSize) {

        // TODO: Implement!
    }

    @Override
    public void listen(final Tryte channel) {

        // TODO: Implement!
    }

    @Override
    public void ignore(final Tryte channel) {

        // TODO: Implement!
    }
}
