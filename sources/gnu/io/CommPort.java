package gnu.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CommPort {
    private static final boolean debug = false;
    protected String name;

    public abstract void disableReceiveFraming();

    public abstract void disableReceiveThreshold();

    public abstract void disableReceiveTimeout();

    public abstract void enableReceiveFraming(int i) throws UnsupportedCommOperationException;

    public abstract void enableReceiveThreshold(int i) throws UnsupportedCommOperationException;

    public abstract void enableReceiveTimeout(int i) throws UnsupportedCommOperationException;

    public abstract int getInputBufferSize();

    public abstract InputStream getInputStream() throws IOException;

    public abstract int getOutputBufferSize();

    public abstract OutputStream getOutputStream() throws IOException;

    public abstract int getReceiveFramingByte();

    public abstract int getReceiveThreshold();

    public abstract int getReceiveTimeout();

    public abstract boolean isReceiveFramingEnabled();

    public abstract boolean isReceiveThresholdEnabled();

    public abstract boolean isReceiveTimeoutEnabled();

    public abstract void setInputBufferSize(int i);

    public abstract void setOutputBufferSize(int i);

    public void close() {
        try {
            if (CommPortIdentifier.getPortIdentifier(this) != null) {
                CommPortIdentifier.getPortIdentifier(this).internalClosePort();
            }
        } catch (NoSuchPortException e) {
        }
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
