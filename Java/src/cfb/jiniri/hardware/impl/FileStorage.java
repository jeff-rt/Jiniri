package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Storage;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.util.Converter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * (c) 2014 Come-from-Beyond
 */
public class FileStorage implements Storage {

    private static enum Stage {

        IDLING,
        STORING_ENTITIES,
        STORING_ENVIRONMENTS,
        STORING_EFFECTS,
        LOADING_ENTITIES,
        LOADING_ENVIRONMENTS,
        LOADING_EFFECTS
    }

    private static final Tryte[] TERMINATOR = new Tryte[0];

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
    public void beginStoring(final Tryte time) {

        if (stage != Stage.IDLING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            outputStream = new FileOutputStream(path);
            channel = outputStream.getChannel();

            storeTrytes(new Tryte[] {time});

            stage = Stage.STORING_ENTITIES;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeEntity(final Tryte[] entityTrytes) {

        if (stage != Stage.STORING_ENTITIES) {

            throw new RuntimeException("Incorrect workflow");
        }

        validateSize(entityTrytes);
        storeTrytes(entityTrytes);
    }

    @Override
    public void storeEnvironment(final Tryte[] environmentTrytes) {

        if (stage == Stage.STORING_ENTITIES) {

            storeTrytes(TERMINATOR);

            stage = Stage.STORING_ENVIRONMENTS;

        } else {

            if (stage != Stage.STORING_ENVIRONMENTS) {

                throw new RuntimeException("Incorrect workflow");
            }
        }

        validateSize(environmentTrytes);
        storeTrytes(environmentTrytes);
    }

    @Override
    public void storeEffect(final Tryte[] effectTrytes) {

        if (stage == Stage.STORING_ENVIRONMENTS) {

            storeTrytes(TERMINATOR);

            stage = Stage.STORING_EFFECTS;

        } else {

            if (stage != Stage.STORING_EFFECTS) {

                throw new RuntimeException("Incorrect workflow");
            }
        }

        validateSize(effectTrytes);
        storeTrytes(effectTrytes);
    }

    @Override
    public void endStoring() {

        if (stage != Stage.STORING_ENTITIES && stage != Stage.STORING_ENVIRONMENTS && stage != Stage.STORING_EFFECTS) {

            throw new RuntimeException("Incorrect workflow");
        }

        storeTrytes(TERMINATOR);

        try {

            channel.close();
            outputStream.close();

            stage = Stage.IDLING;

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public Tryte beginLoading() {

        if (stage != Stage.IDLING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            inputStream = new FileInputStream(path);
            channel = inputStream.getChannel();

            final Tryte[] trytes = loadTrytes();

            stage = Stage.LOADING_ENTITIES;

            return trytes[0];

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public Tryte[] loadEntity() {

        if (stage != Stage.LOADING_ENTITIES) {

            throw new RuntimeException("Incorrect workflow");
        }

        final Tryte[] trytes = loadTrytes();

        if (trytes == null) {

            stage = Stage.LOADING_ENVIRONMENTS;
        }

        return trytes;
    }

    @Override
    public Tryte[] loadEnvironment() {

        if (stage != Stage.LOADING_ENVIRONMENTS) {

            throw new RuntimeException("Incorrect workflow");
        }

        final Tryte[] trytes = loadTrytes();

        if (trytes == null) {

            stage = Stage.LOADING_EFFECTS;
        }

        return trytes;
    }

    @Override
    public Tryte[] loadEffect() {

        if (stage != Stage.LOADING_EFFECTS) {

            throw new RuntimeException("Incorrect workflow");
        }

        final Tryte[] trytes = loadTrytes();

        if (trytes == null) {

            try {

                channel.close();
                inputStream.close();

                stage = Stage.IDLING;

            } catch (final IOException e) {

                throw new RuntimeException(e);
            }
        }

        return trytes;
    }

    private void validateSize(final Tryte[] trytes) {

        if (trytes.length == TERMINATOR.length) {

            throw new IllegalArgumentException("Illegal size: " + trytes.length);
        }
    }

    private void storeTrytes(final Tryte[] trytes) {

        final byte[] bytes = Converter.getBytes(trytes);
        final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        try {

            channel.write(buffer);

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }

    private Tryte[] loadTrytes() {

        try {

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            channel.read(buffer);

            buffer.flip();
            final int size = buffer.getInt();

            if (size == TERMINATOR.length) {

                return null;
            }

            buffer = ByteBuffer.allocate(size);

            channel.read(buffer);

            buffer.flip();
            final byte[] bytes = new byte[buffer.limit()];
            for (int i = 0; i < bytes.length; i++) {

                bytes[i] = buffer.get();
            }

            return Converter.getTrytes(bytes);

        } catch (final IOException e) {

            throw new RuntimeException(e);
        }
    }
}
