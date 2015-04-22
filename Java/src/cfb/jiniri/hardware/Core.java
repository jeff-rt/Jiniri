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

    private Entity entity;

    private final Trit[] cache;

    protected Core(final Processor processor, final int cacheCapacity) {

        this.processor = processor;

        cache = new Trit[cacheCapacity];
    }

    protected void deploy(final Entity entity) {

        this.entity = entity;
    }

    protected void executeForm(final Trit[] initializationData) {

        entity.getState(cache);

        System.arraycopy(initializationData, 0, cache, entity.getStateSize(), initializationData.length);
        entity.form(cache, this);

        entity.setState(cache);

        processor.salvage(this);
    }

    protected void executeEvolve(final Tryte time) {

        entity.getState(cache);

        System.arraycopy(time.getTrits(), 0, cache, entity.getStateSize(), time.getWidth());
        entity.morph(cache, this);

        entity.setState(cache);

        processor.salvage(this);
    }

    protected void executeReact(final Effect[] effects) {

        entity.getState(cache);

        for (final Effect effect : effects) {

            System.arraycopy(effect.getData(), 0, cache, entity.getStateSize(), effect.getDataSize());
            entity.react(cache, this);
        }

        entity.setState(cache);

        processor.salvage(this);
    }

    protected void executeAnalyze(final Trit[][] messages) {

        entity.getState(cache);

        for (final Trit[] message : messages) {

            System.arraycopy(message, 0, cache, entity.getStateSize(), message.length);
            entity.analyze(cache, this);
        }

        entity.setState(cache);

        processor.salvage(this);
    }

    protected void executeDecay() {

        entity.getState(cache);

        entity.decay(cache, this);

        entity.setState(cache);

        processor.salvage(this);
    }

    @Override
    public void halt() {

        throw new RuntimeException("Halt");
    }

    @Override
    public void form(final Class entityClass,
                     final Tryte initializationDataAddress, final Tryte initializationDataSize) {

        final Trit[] initializationData = new Trit[initializationDataSize.getIntValue()];
        System.arraycopy(cache, initializationDataAddress.getIntValue(), initializationData, 0, initializationData.length);

        processor.create(entityClass, initializationData);
    }

    @Override
    public void decay() {

        processor.destroy(entity);
    }

    @Override
    public void affect(final Tryte environment,
                       final Tryte effectDataAddress, final Tryte effectDataSize, final Tryte delay) {

        final Trit[] data = new Trit[effectDataSize.getIntValue()];
        System.arraycopy(cache, effectDataAddress.getIntValue(), data, 0, data.length);

        processor.affect(environment, data, delay);
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

        final Trit[] message = new Trit[messageSize.getIntValue()];
        System.arraycopy(cache, messageAddress.getIntValue(), message, 0, message.length);

        processor.broadcast(channel, message);
    }

    @Override
    public void listen(final Tryte channel) {

        processor.listen(entity, channel);
    }

    @Override
    public void ignore(final Tryte channel) {

        processor.ignore(entity, channel);
    }
}
