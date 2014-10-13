package cfb.jiniri.hardware.impl;

import cfb.jiniri.hardware.Storage;
import cfb.jiniri.ternary.Tryte;
import cfb.jiniri.util.Converter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
    public void continueStoring(final Tryte... trytes) {

        if (stage != Stage.STORING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            channel.write(ByteBuffer.wrap(Converter.getBytes(trytes)));

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
    public void continueLoading(final Tryte[] bufferForTrytes) {

        if (stage != Stage.LOADING) {

            throw new RuntimeException("Incorrect workflow");
        }

        try {

            final ByteBuffer buffer = ByteBuffer.allocate(bufferForTrytes.length * Converter.TRYTE_SIZE);
            channel.read(buffer);
            buffer.flip();

            final byte[] fragment = new byte[Converter.TRYTE_SIZE];
            for (int i = 0; i < bufferForTrytes.length; i++) {

                for (int j = 0; j < fragment.length; j++) {

                    fragment[j] = buffer.get();
                }
                bufferForTrytes[i] = Converter.getTrytes(fragment)[0];
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
