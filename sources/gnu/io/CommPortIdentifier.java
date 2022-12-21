package gnu.io;

import java.io.FileDescriptor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class CommPortIdentifier {
    static CommPortIdentifier CommPortIndex = null;
    public static final int PORT_I2C = 3;
    public static final int PORT_PARALLEL = 2;
    public static final int PORT_RAW = 5;
    public static final int PORT_RS485 = 4;
    public static final int PORT_SERIAL = 1;
    static Object Sync = new Object();
    private static final boolean debug = false;
    private boolean Available = true;
    private boolean HideOwnerEvents;
    private String Owner;
    private String PortName;
    private int PortType;
    private CommDriver RXTXDriver;
    private CommPort commport;
    CommPortIdentifier next;
    Vector ownershipListener;

    private native String native_psmisc_report_owner(String str);

    static {
        try {
            ((CommDriver) Class.forName("gnu.io.RXTXCommDriver").newInstance()).initialize();
        } catch (Throwable e) {
            System.err.println(e + " thrown while loading " + "gnu.io.RXTXCommDriver");
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("linux") == -1) {
        }
        System.loadLibrary("rxtxSerialIO");
    }

    CommPortIdentifier(String pn, CommPort cp, int pt, CommDriver driver) {
        this.PortName = pn;
        this.commport = cp;
        this.PortType = pt;
        this.next = null;
        this.RXTXDriver = driver;
    }

    public static void addPortName(String s, int type, CommDriver c) {
        AddIdentifierToList(new CommPortIdentifier(s, (CommPort) null, type, c));
    }

    private static void AddIdentifierToList(CommPortIdentifier cpi) {
        synchronized (Sync) {
            if (CommPortIndex == null) {
                CommPortIndex = cpi;
            } else {
                CommPortIdentifier index = CommPortIndex;
                while (index.next != null) {
                    index = index.next;
                }
                index.next = cpi;
            }
        }
    }

    public void addPortOwnershipListener(CommPortOwnershipListener c) {
        if (this.ownershipListener == null) {
            this.ownershipListener = new Vector();
        }
        if (!this.ownershipListener.contains(c)) {
            this.ownershipListener.addElement(c);
        }
    }

    public String getCurrentOwner() {
        return this.Owner;
    }

    public String getName() {
        return this.PortName;
    }

    public static CommPortIdentifier getPortIdentifier(String s) throws NoSuchPortException {
        CommPortIdentifier index;
        synchronized (Sync) {
            CommPortIdentifier index2 = CommPortIndex;
            while (index != null && !index.PortName.equals(s)) {
                index2 = index.next;
            }
            if (index == null) {
                getPortIdentifiers();
                index = CommPortIndex;
                while (index != null && !index.PortName.equals(s)) {
                    index = index.next;
                }
            }
        }
        if (index != null) {
            return index;
        }
        throw new NoSuchPortException();
    }

    public static CommPortIdentifier getPortIdentifier(CommPort p) throws NoSuchPortException {
        CommPortIdentifier c;
        synchronized (Sync) {
            c = CommPortIndex;
            while (c != null && c.commport != p) {
                c = c.next;
            }
        }
        if (c != null) {
            return c;
        }
        throw new NoSuchPortException();
    }

    public static Enumeration getPortIdentifiers() {
        synchronized (Sync) {
            HashMap oldPorts = new HashMap();
            for (CommPortIdentifier p = CommPortIndex; p != null; p = p.next) {
                oldPorts.put(p.PortName, p);
            }
            CommPortIndex = null;
            try {
                ((CommDriver) Class.forName("gnu.io.RXTXCommDriver").newInstance()).initialize();
                CommPortIdentifier prevPort = null;
                for (CommPortIdentifier curPort = CommPortIndex; curPort != null; curPort = curPort.next) {
                    CommPortIdentifier matchingOldPort = (CommPortIdentifier) oldPorts.get(curPort.PortName);
                    if (matchingOldPort == null || matchingOldPort.PortType != curPort.PortType) {
                        prevPort = curPort;
                    } else {
                        matchingOldPort.RXTXDriver = curPort.RXTXDriver;
                        matchingOldPort.next = curPort.next;
                        if (prevPort == null) {
                            CommPortIndex = matchingOldPort;
                        } else {
                            prevPort.next = matchingOldPort;
                        }
                        prevPort = matchingOldPort;
                    }
                }
            } catch (Throwable e) {
                System.err.println(e + " thrown while loading " + "gnu.io.RXTXCommDriver");
                System.err.flush();
            }
        }
        return new CommPortEnumerator();
    }

    public int getPortType() {
        return this.PortType;
    }

    public synchronized boolean isCurrentlyOwned() {
        return !this.Available;
    }

    public synchronized CommPort open(FileDescriptor f) throws UnsupportedCommOperationException {
        throw new UnsupportedCommOperationException();
    }

    public CommPort open(String TheOwner, int i) throws PortInUseException {
        boolean isAvailable;
        synchronized (this) {
            isAvailable = this.Available;
            if (isAvailable) {
                this.Available = false;
                this.Owner = TheOwner;
            }
        }
        if (!isAvailable) {
            long waitTimeEnd = System.currentTimeMillis() + ((long) i);
            fireOwnershipEvent(3);
            synchronized (this) {
                while (!this.Available) {
                    long waitTimeCurr = System.currentTimeMillis();
                    if (waitTimeCurr >= waitTimeEnd) {
                        break;
                    }
                    try {
                        wait(waitTimeEnd - waitTimeCurr);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                isAvailable = this.Available;
                if (isAvailable) {
                    this.Available = false;
                    this.Owner = TheOwner;
                }
            }
        }
        if (!isAvailable) {
            throw new PortInUseException(getCurrentOwner());
        }
        try {
            if (this.commport == null) {
                this.commport = this.RXTXDriver.getCommPort(this.PortName, this.PortType);
            }
            if (this.commport != null) {
                fireOwnershipEvent(1);
                CommPort commPort = this.commport;
                if (this.commport == null) {
                    synchronized (this) {
                        this.Available = true;
                        this.Owner = null;
                    }
                }
                return commPort;
            }
            throw new PortInUseException(native_psmisc_report_owner(this.PortName));
        } catch (Throwable th) {
            if (this.commport == null) {
                synchronized (this) {
                    this.Available = true;
                    this.Owner = null;
                }
            }
            throw th;
        }
    }

    public void removePortOwnershipListener(CommPortOwnershipListener c) {
        if (this.ownershipListener != null) {
            this.ownershipListener.removeElement(c);
        }
    }

    /* access modifiers changed from: package-private */
    public void internalClosePort() {
        synchronized (this) {
            this.Owner = null;
            this.Available = true;
            this.commport = null;
            notifyAll();
        }
        fireOwnershipEvent(2);
    }

    /* access modifiers changed from: package-private */
    public void fireOwnershipEvent(int eventType) {
        if (this.ownershipListener != null) {
            Enumeration e = this.ownershipListener.elements();
            while (e.hasMoreElements()) {
                ((CommPortOwnershipListener) e.nextElement()).ownershipChange(eventType);
            }
        }
    }
}
