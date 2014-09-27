package cfb.jiniri.hardware;

import cfb.jiniri.type.Multiplet;
import cfb.jiniri.type.Singlet;

/**
 * (c) 2014 Come-from-Beyond
 */
public class Processor {

    private final Multiplet id;

    private final Core[] cores;

    private final Singlet time;

    public Processor(final Multiplet id, final int numberOfCores, final int coreMemoryCapacity, final Singlet time) {

        this.id = id.clone();

        cores = new Core[numberOfCores];
        for (int i = 0; i < cores.length; i++) {

            cores[i] = new Core(coreMemoryCapacity);
        }

        this.time = (Singlet)time.clone();
    }
}
