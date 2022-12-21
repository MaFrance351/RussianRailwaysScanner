package gnu.io;

import java.io.IOException;
import java.util.TooManyListenersException;

public abstract class SerialPort extends CommPort {
    public static final int DATABITS_5 = 5;
    public static final int DATABITS_6 = 6;
    public static final int DATABITS_7 = 7;
    public static final int DATABITS_8 = 8;
    public static final int FLOWCONTROL_NONE = 0;
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;
    public static final int FLOWCONTROL_XONXOFF_IN = 4;
    public static final int FLOWCONTROL_XONXOFF_OUT = 8;
    public static final int PARITY_EVEN = 2;
    public static final int PARITY_MARK = 3;
    public static final int PARITY_NONE = 0;
    public static final int PARITY_ODD = 1;
    public static final int PARITY_SPACE = 4;
    public static final int STOPBITS_1 = 1;
    public static final int STOPBITS_1_5 = 3;
    public static final int STOPBITS_2 = 2;

    public abstract void addEventListener(SerialPortEventListener serialPortEventListener) throws TooManyListenersException;

    public abstract int getBaudBase() throws UnsupportedCommOperationException, IOException;

    public abstract int getBaudRate();

    public abstract boolean getCallOutHangup() throws UnsupportedCommOperationException;

    public abstract int getDataBits();

    public abstract int getDivisor() throws UnsupportedCommOperationException, IOException;

    public abstract byte getEndOfInputChar() throws UnsupportedCommOperationException;

    public abstract int getFlowControlMode();

    public abstract boolean getLowLatency() throws UnsupportedCommOperationException;

    public abstract int getParity();

    public abstract byte getParityErrorChar() throws UnsupportedCommOperationException;

    public abstract int getStopBits();

    public abstract String getUARTType() throws UnsupportedCommOperationException;

    public abstract boolean isCD();

    public abstract boolean isCTS();

    public abstract boolean isDSR();

    public abstract boolean isDTR();

    public abstract boolean isRI();

    public abstract boolean isRTS();

    public abstract void notifyOnBreakInterrupt(boolean z);

    public abstract void notifyOnCTS(boolean z);

    public abstract void notifyOnCarrierDetect(boolean z);

    public abstract void notifyOnDSR(boolean z);

    public abstract void notifyOnDataAvailable(boolean z);

    public abstract void notifyOnFramingError(boolean z);

    public abstract void notifyOnOutputEmpty(boolean z);

    public abstract void notifyOnOverrunError(boolean z);

    public abstract void notifyOnParityError(boolean z);

    public abstract void notifyOnRingIndicator(boolean z);

    public abstract void removeEventListener();

    public abstract void sendBreak(int i);

    public abstract boolean setBaudBase(int i) throws UnsupportedCommOperationException, IOException;

    public abstract boolean setCallOutHangup(boolean z) throws UnsupportedCommOperationException;

    public abstract void setDTR(boolean z);

    public abstract boolean setDivisor(int i) throws UnsupportedCommOperationException, IOException;

    public abstract boolean setEndOfInputChar(byte b) throws UnsupportedCommOperationException;

    public abstract void setFlowControlMode(int i) throws UnsupportedCommOperationException;

    public abstract boolean setLowLatency() throws UnsupportedCommOperationException;

    public abstract boolean setParityErrorChar(byte b) throws UnsupportedCommOperationException;

    public abstract void setRTS(boolean z);

    public abstract void setSerialPortParams(int i, int i2, int i3, int i4) throws UnsupportedCommOperationException;

    public abstract boolean setUARTType(String str, boolean z) throws UnsupportedCommOperationException;
}
