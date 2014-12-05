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
    public void idle() {
    }

    @Override
    public void push(final Tryte memoryOffset,
                     final Tryte destination, final Tryte destinationOffset,
                     final Tryte numberOfTritsToPush, final Tryte offsetForNumberOfPushedTrits) {
    }

    @Override
    public void pull(final Tryte memoryOffset,
                     final Tryte source, final Tryte sourceOffset,
                     final Tryte numberOfTritsToPull, final Tryte offsetForNumberOfPulledTrits) {
    }

    @Override
    public void halt() {

        throw new RuntimeException("Halt");
    }

    @Override
    public void spawn(final Tryte entityDomain,
                      final Class entityClass, final Tryte maxEffectDataAndScratchpadSize,
                      final Tryte effectDataPointer, final Tryte effectDataSize) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(scratchpad, effectDataPointer.getIntValue(), data, 0, data.length);

        processor.create(entityDomain, entityClass, maxEffectDataAndScratchpadSize, data);
    }

    @Override
    public void decay() {

        processor.destroy(entity);
    }

    @Override
    public void affect(final Tryte environment,
                       final Tryte effectRange, final Tryte effectEffectiveness,
                       final Tryte effectDelay, final Tryte effectDuration,
                       final Tryte effectDataPointer, final Tryte effectDataSize) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(scratchpad, effectDataPointer.getIntValue(), data, 0, data.length);

        processor.affect(environment, effectRange, effectEffectiveness, effectDelay, effectDuration, data);
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
