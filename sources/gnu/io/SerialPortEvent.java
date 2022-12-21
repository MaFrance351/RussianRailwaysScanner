package gnu.io;

import java.util.EventObject;

public class SerialPortEvent extends EventObject {
    public static final int BI = 10;
    public static final int CD = 6;
    public static final int CTS = 3;
    public static final int DATA_AVAILABLE = 1;
    public static final int DSR = 4;
    public static final int FE = 9;
    public static final int OE = 7;
    public static final int OUTPUT_BUFFER_EMPTY = 2;
    public static final int PE = 8;
    public static final int RI = 5;
    private boolean NewValue;
    private boolean OldValue;
    private int eventType;

    public SerialPortEvent(SerialPort srcport, int eventtype, boolean oldvalue, boolean newvalue) {
        super(srcport);
        this.OldValue = oldvalue;
        this.NewValue = newvalue;
        this.eventType = eventtype;
    }

    public int getEventType() {
        return this.eventType;
    }

    public boolean getNewValue() {
        return this.NewValue;
    }

    public boolean getOldValue() {
        return this.OldValue;
    }
}
