package cfb.jiniri.hardware;

import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private final Multiplet id;

    private final Core[] cores;

    private final Singlet tick;

    public Processor(final Multiplet id, final int numberOfCores, final int coreMemoryCapacity, final Singlet tick) {

        this.id = id.clone();

        cores = new Core[numberOfCores];
        for (int i = 0; i < cores.length; i++) {

            cores[i] = new Core(coreMemoryCapacity);
        }

        this.tick = (Singlet)tick.clone();
    }
}
