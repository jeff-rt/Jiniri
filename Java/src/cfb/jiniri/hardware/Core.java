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
    public void run(final Tryte behaviorAddress, final Tryte behaviorSize,
                    final Tryte stateAddress, final Tryte stateSize,
                    final Tryte effectAddress, final Tryte effectSize) {
    }

    @Override
    public void push(final Tryte memoryAddress,
                     final Tryte destination, final Tryte destinationAddress,
                     final Tryte numberOfTrits, final Tryte addressForStatus) {
    }

    @Override
    public void pull(final Tryte memoryAddress,
                     final Tryte source, final Tryte sourceAddress,
                     final Tryte numberOfTrits, final Tryte addressForStatus) {
    }

    @Override
    public void halt() {

        throw new RuntimeException("Halt");
    }

    @Override
    public void spawn(final Tryte entityHeight,
                      final Class entityClass, final Tryte maxDataSize,
                      final Tryte effectDataAddress, final Tryte effectDataSize) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(scratchpad, effectDataAddress.getIntValue(), data, 0, data.length);

        processor.create(entityHeight, entityClass, maxDataSize, data);
    }

    @Override
    public void decay() {

        processor.destroy(entity);
    }

    @Override
    public void affect(final Tryte environment,
                       final Tryte effectDirection, final Tryte effectDelay, final Tryte effectDuration,
                       final Tryte effectDataAddress, final Tryte effectDataSize) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(scratchpad, effectDataAddress.getIntValue(), data, 0, data.length);

        processor.affect(environment, effectDirection, effectDelay, effectDuration, data);
    }

    @Override
    public void join(final Tryte environment) {

        processor.include(entity, environment);
    }

    @Override
    public void leave(final Tryte environment) {

        processor.exclude(entity, environment);
    }
}
