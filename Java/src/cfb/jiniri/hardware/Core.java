package cfb.jiniri.hardware;

import cfb.jiniri.model.Effect;
import cfb.jiniri.model.Entity;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Core {

    private final int memoryCapacity;

    private Entity entity;

    public Core(final int memoryCapacity) {

        this.memoryCapacity = memoryCapacity;
    }

    public void deploy(final Entity entity) {

        this.entity = entity;
    }

    public void react(final Effect[] effects) {

        for (final Effect effect : effects) {

            entity.react(effect.getData(), memoryCapacity - entity.getStateSize() - effect.getDataSize());
        }
    }
}
