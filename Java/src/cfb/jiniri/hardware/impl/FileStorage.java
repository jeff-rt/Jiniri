package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Storage;
import cfb.jiniri.ternary.Trit;
import cfb.jiniri.util.Converter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * (c) 2014 Come-from-Beyond
 */
public class FileStorage implements Storage {

    private enum Stage {

        IDLING,
        STORING,
        LOADING
    }

    private final String path;
    private FileOutputStream outputStream;
    private FileInputStream inputStream;
    private FileChannel channel;

    private Stage stage;

    public FileStorage(final String path) {

        this.path = path;

        this.stage = Stage.IDLING;
    }

    @Override
    public void beginStoring() {

        if (stage != Stage.IDLING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            outputStream = new FileOutputStream(path);
            channel = outputStream.getChannel();

            stage = Stage.STORING;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void continueStoring(final Trit[] trits) {

        if (stage != Stage.STORING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            channel.write(ByteBuffer.wrap(Converter.getBytes(trits)));

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void endStoring() {

        if (stage != Stage.STORING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            channel.close();
            outputStream.close();

            stage = Stage.IDLING;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginLoading() {

        if (stage != Stage.IDLING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();

            stage = Stage.LOADING;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public Trit[] continueLoading() {

        if (stage != Stage.LOADING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            final List<Trit> trits = new LinkedList<>();

            final ByteBuffer buffer = ByteBuffer.allocate(1);
            while (true) {

                buffer.clear();
                channel.read(buffer);
                buffer.flip();
                final Trit[] newTrits = Converter.getTrits(new byte[] {buffer.get()});
                for (final Trit trit : newTrits) {

                    trits.add(trit);
                }

                if (newTrits.length < Byte.SIZE / Converter.NUMBER_OF_BITS) {

                    return trits.toArray(new Trit[trits.size()]);
                }
            }

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void endLoading() {

        if (stage != Stage.LOADING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            channel.close();
            inputStream.close();

            stage = Stage.IDLING;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }
}
