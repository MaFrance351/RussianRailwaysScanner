package gnu.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

public final class RXTXPort extends SerialPort {
    protected static final boolean debug = false;
    protected static final boolean debug_events = false;
    protected static final boolean debug_read = false;
    protected static final boolean debug_read_results = false;
    protected static final boolean debug_verbose = false;
    protected static final boolean debug_write = false;
    static boolean dsrFlag = false;
    /* access modifiers changed from: private */
    public static Zystem z;
    int IOLocked = 0;
    Object IOLockedMutex = new Object();
    private int InputBuffer = 0;
    boolean MonitorThreadAlive = false;
    boolean MonitorThreadLock = true;
    private int OutputBuffer = 0;
    private SerialPortEventListener SPEventListener;
    boolean closeLock = false;
    private int dataBits = 8;
    long eis = 0;
    /* access modifiers changed from: private */
    public int fd = 0;
    private int flowmode = 0;
    private final SerialInputStream in = new SerialInputStream();
    private MonitorThread monThread;
    boolean monThreadisInterrupted = true;
    private final SerialOutputStream out = new SerialOutputStream();
    private int parity = 0;
    int pid = 0;
    /* access modifiers changed from: private */
    public int speed = 9600;
    private int stopBits = 1;
    /* access modifiers changed from: private */
    public int threshold = 0;
    private int timeout;

    private static native void Initialize();

    private native void NativeEnableReceiveTimeoutThreshold(int i, int i2, int i3);

    private native boolean NativeisReceiveTimeoutEnabled();

    private native void interruptEventLoop();

    private native boolean nativeClearCommInput() throws UnsupportedCommOperationException;

    private native void nativeClose(String str);

    private native int nativeGetBaudBase() throws UnsupportedCommOperationException;

    private native boolean nativeGetCallOutHangup() throws UnsupportedCommOperationException;

    private native int nativeGetDivisor() throws UnsupportedCommOperationException;

    private native byte nativeGetEndOfInputChar() throws UnsupportedCommOperationException;

    private native int nativeGetFlowControlMode(int i);

    private native boolean nativeGetLowLatency() throws UnsupportedCommOperationException;

    private native int nativeGetParity(int i);

    private native byte nativeGetParityErrorChar() throws UnsupportedCommOperationException;

    private native boolean nativeSetBaudBase(int i) throws UnsupportedCommOperationException;

    private native boolean nativeSetCallOutHangup(boolean z2) throws UnsupportedCommOperationException;

    private native boolean nativeSetDivisor(int i) throws UnsupportedCommOperationException;

    private native boolean nativeSetEndOfInputChar(byte b) throws UnsupportedCommOperationException;

    private native void nativeSetEventFlag(int i, int i2, boolean z2);

    private native boolean nativeSetLowLatency() throws UnsupportedCommOperationException;

    private native boolean nativeSetParityErrorChar(byte b) throws UnsupportedCommOperationException;

    private native boolean nativeSetSerialPortParams(int i, int i2, int i3, int i4) throws UnsupportedCommOperationException;

    private native boolean nativeSetUartType(String str, boolean z2) throws UnsupportedCommOperationException;

    private static native int nativeStaticGetBaudRate(String str) throws UnsupportedCommOperationException;

    private static native int nativeStaticGetDataBits(String str) throws UnsupportedCommOperationException;

    private static native int nativeStaticGetParity(String str) throws UnsupportedCommOperationException;

    private static native int nativeStaticGetStopBits(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsCD(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsCTS(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsDSR(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsDTR(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsRI(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticIsRTS(String str) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticSetDSR(String str, boolean z2) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticSetDTR(String str, boolean z2) throws UnsupportedCommOperationException;

    private static native boolean nativeStaticSetRTS(String str, boolean z2) throws UnsupportedCommOperationException;

    private static native void nativeStaticSetSerialPortParams(String str, int i, int i2, int i3, int i4) throws UnsupportedCommOperationException;

    private native synchronized int open(String str) throws PortInUseException;

    private native void setDSR(boolean z2);

    public native int NativegetReceiveTimeout();

    /* access modifiers changed from: package-private */
    public native void eventLoop();

    public native boolean isCD();

    public native boolean isCTS();

    public native boolean isDSR();

    public native boolean isDTR();

    public native boolean isRI();

    public native boolean isRTS();

    /* access modifiers changed from: protected */
    public native boolean nativeDrain(boolean z2) throws IOException;

    /* access modifiers changed from: package-private */
    public native String nativeGetUartType() throws UnsupportedCommOperationException;

    /* access modifiers changed from: protected */
    public native int nativeavailable() throws IOException;

    /* access modifiers changed from: protected */
    public native int readArray(byte[] bArr, int i, int i2) throws IOException;

    /* access modifiers changed from: protected */
    public native int readByte() throws IOException;

    /* access modifiers changed from: protected */
    public native int readTerminatedArray(byte[] bArr, int i, int i2, byte[] bArr2) throws IOException;

    public native void sendBreak(int i);

    public native void setDTR(boolean z2);

    public native void setRTS(boolean z2);

    /* access modifiers changed from: package-private */
    public native void setflowcontrol(int i) throws IOException;

    /* access modifiers changed from: protected */
    public native void writeArray(byte[] bArr, int i, int i2, boolean z2) throws IOException;

    /* access modifiers changed from: protected */
    public native void writeByte(int i, boolean z2) throws IOException;

    static {
        try {
            z = new Zystem();
        } catch (Exception e) {
        }
        System.loadLibrary("rxtxSerialIO");
        Initialize();
    }

    public RXTXPort(String name) throws PortInUseException {
        this.fd = open(name);
        this.name = name;
        this.MonitorThreadLock = true;
        this.monThread = new MonitorThread();
        this.monThread.start();
        waitForTheNativeCodeSilly();
        this.MonitorThreadAlive = true;
        this.timeout = -1;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public synchronized void setSerialPortParams(int b, int d, int s, int p) throws UnsupportedCommOperationException {
        if (nativeSetSerialPortParams(b, d, s, p)) {
            throw new UnsupportedCommOperationException("Invalid Parameter");
        }
        this.speed = b;
        if (s == 3) {
            this.dataBits = 5;
        } else {
            this.dataBits = d;
        }
        this.stopBits = s;
        this.parity = p;
        z.reportln("RXTXPort:setSerialPortParams(" + b + " " + d + " " + s + " " + p + ") returning");
    }

    public int getBaudRate() {
        return this.speed;
    }

    public int getDataBits() {
        return this.dataBits;
    }

    public int getStopBits() {
        return this.stopBits;
    }

    public int getParity() {
        return this.parity;
    }

    public void setFlowControlMode(int flowcontrol) {
        if (!this.monThreadisInterrupted) {
            try {
                setflowcontrol(flowcontrol);
                this.flowmode = flowcontrol;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getFlowControlMode() {
        return this.flowmode;
    }

    public void enableReceiveFraming(int f) throws UnsupportedCommOperationException {
        throw new UnsupportedCommOperationException("Not supported");
    }

    public void disableReceiveFraming() {
    }

    public boolean isReceiveFramingEnabled() {
        return false;
    }

    public int getReceiveFramingByte() {
        return 0;
    }

    public void disableReceiveTimeout() {
        this.timeout = -1;
        NativeEnableReceiveTimeoutThreshold(this.timeout, this.threshold, this.InputBuffer);
    }

    public void enableReceiveTimeout(int time) {
        if (time >= 0) {
            this.timeout = time;
            NativeEnableReceiveTimeoutThreshold(time, this.threshold, this.InputBuffer);
            return;
        }
        throw new IllegalArgumentException("Unexpected negative timeout value");
    }

    public boolean isReceiveTimeoutEnabled() {
        return NativeisReceiveTimeoutEnabled();
    }

    public int getReceiveTimeout() {
        return NativegetReceiveTimeout();
    }

    public void enableReceiveThreshold(int thresh) {
        if (thresh >= 0) {
            this.threshold = thresh;
            NativeEnableReceiveTimeoutThreshold(this.timeout, this.threshold, this.InputBuffer);
            return;
        }
        throw new IllegalArgumentException("Unexpected negative threshold value");
    }

    public void disableReceiveThreshold() {
        enableReceiveThreshold(0);
    }

    public int getReceiveThreshold() {
        return this.threshold;
    }

    public boolean isReceiveThresholdEnabled() {
        return this.threshold > 0;
    }

    public void setInputBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Unexpected negative buffer size value");
        }
        this.InputBuffer = size;
    }

    public int getInputBufferSize() {
        return this.InputBuffer;
    }

    public void setOutputBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Unexpected negative buffer size value");
        }
        this.OutputBuffer = size;
    }

    public int getOutputBufferSize() {
        return this.OutputBuffer;
    }

    public boolean checkMonitorThread() {
        if (this.monThread != null) {
            return this.monThreadisInterrupted;
        }
        return true;
    }

    public boolean sendEvent(int event, boolean state) {
        boolean z2;
        if (this.fd == 0 || this.SPEventListener == null || this.monThread == null) {
            return true;
        }
        switch (event) {
        }
        switch (event) {
            case 1:
                if (!this.monThread.Data) {
                    return false;
                }
                break;
            case 2:
                if (!this.monThread.Output) {
                    return false;
                }
                break;
            case 3:
                if (!this.monThread.CTS) {
                    return false;
                }
                break;
            case 4:
                if (!this.monThread.DSR) {
                    return false;
                }
                break;
            case 5:
                if (!this.monThread.RI) {
                    return false;
                }
                break;
            case 6:
                if (!this.monThread.CD) {
                    return false;
                }
                break;
            case 7:
                if (!this.monThread.OE) {
                    return false;
                }
                break;
            case 8:
                if (!this.monThread.PE) {
                    return false;
                }
                break;
            case SerialPortEvent.FE:
                if (!this.monThread.FE) {
                    return false;
                }
                break;
            case SerialPortEvent.BI:
                if (!this.monThread.BI) {
                    return false;
                }
                break;
            default:
                System.err.println("unknown event: " + event);
                return false;
        }
        if (state) {
            z2 = false;
        } else {
            z2 = true;
        }
        SerialPortEvent e = new SerialPortEvent(this, event, z2, state);
        if (this.monThreadisInterrupted) {
            return true;
        }
        if (this.SPEventListener != null) {
            this.SPEventListener.serialEvent(e);
        }
        if (this.fd == 0 || this.SPEventListener == null || this.monThread == null) {
            return true;
        }
        return false;
    }

    public void addEventListener(SerialPortEventListener lsnr) throws TooManyListenersException {
        if (this.SPEventListener != null) {
            throw new TooManyListenersException();
        }
        this.SPEventListener = lsnr;
        if (!this.MonitorThreadAlive) {
            this.MonitorThreadLock = true;
            this.monThread = new MonitorThread();
            this.monThread.start();
            waitForTheNativeCodeSilly();
            this.MonitorThreadAlive = true;
        }
    }

    public void removeEventListener() {
        waitForTheNativeCodeSilly();
        if (this.monThreadisInterrupted) {
            z.reportln("\tRXTXPort:removeEventListener() already interrupted");
            this.monThread = null;
            this.SPEventListener = null;
            return;
        }
        if (this.monThread != null && this.monThread.isAlive()) {
            this.monThreadisInterrupted = true;
            interruptEventLoop();
            try {
                this.monThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        this.monThread = null;
        this.SPEventListener = null;
        this.MonitorThreadLock = false;
        this.MonitorThreadAlive = false;
        this.monThreadisInterrupted = true;
        z.reportln("RXTXPort:removeEventListener() returning");
    }

    /* access modifiers changed from: protected */
    public void waitForTheNativeCodeSilly() {
        while (this.MonitorThreadLock) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
            }
        }
    }

    public void notifyOnDataAvailable(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 1, enable);
        this.monThread.Data = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnOutputEmpty(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 2, enable);
        this.monThread.Output = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnCTS(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 3, enable);
        this.monThread.CTS = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnDSR(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 4, enable);
        this.monThread.DSR = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnRingIndicator(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 5, enable);
        this.monThread.RI = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnCarrierDetect(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 6, enable);
        this.monThread.CD = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnOverrunError(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 7, enable);
        this.monThread.OE = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnParityError(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 8, enable);
        this.monThread.PE = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnFramingError(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 9, enable);
        this.monThread.FE = enable;
        this.MonitorThreadLock = false;
    }

    public void notifyOnBreakInterrupt(boolean enable) {
        waitForTheNativeCodeSilly();
        this.MonitorThreadLock = true;
        nativeSetEventFlag(this.fd, 10, enable);
        this.monThread.BI = enable;
        this.MonitorThreadLock = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0025, code lost:
        if (r4.fd > 0) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0027, code lost:
        z.reportln("RXTXPort:close detected bad File Descriptor");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x002f, code lost:
        setDTR(false);
        setDSR(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0037, code lost:
        if (r4.monThreadisInterrupted != false) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0039, code lost:
        removeEventListener();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x003c, code lost:
        nativeClose(r4.name);
        super.close();
        r4.fd = 0;
        r4.closeLock = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() {
        /*
            r4 = this;
            r3 = 0
            monitor-enter(r4)
        L_0x0002:
            int r1 = r4.IOLocked     // Catch:{ all -> 0x001c }
            if (r1 > 0) goto L_0x000c
            boolean r1 = r4.closeLock     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x001f
            monitor-exit(r4)     // Catch:{ all -> 0x001c }
        L_0x000b:
            return
        L_0x000c:
            r1 = 500(0x1f4, double:2.47E-321)
            r4.wait(r1)     // Catch:{ InterruptedException -> 0x0012 }
            goto L_0x0002
        L_0x0012:
            r0 = move-exception
            java.lang.Thread r1 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x001c }
            r1.interrupt()     // Catch:{ all -> 0x001c }
            monitor-exit(r4)     // Catch:{ all -> 0x001c }
            goto L_0x000b
        L_0x001c:
            r1 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x001c }
            throw r1
        L_0x001f:
            r1 = 1
            r4.closeLock = r1     // Catch:{ all -> 0x001c }
            monitor-exit(r4)     // Catch:{ all -> 0x001c }
            int r1 = r4.fd
            if (r1 > 0) goto L_0x002f
            gnu.io.Zystem r1 = z
            java.lang.String r2 = "RXTXPort:close detected bad File Descriptor"
            r1.reportln(r2)
            goto L_0x000b
        L_0x002f:
            r4.setDTR(r3)
            r4.setDSR(r3)
            boolean r1 = r4.monThreadisInterrupted
            if (r1 != 0) goto L_0x003c
            r4.removeEventListener()
        L_0x003c:
            java.lang.String r1 = r4.name
            r4.nativeClose(r1)
            super.close()
            r4.fd = r3
            r4.closeLock = r3
            goto L_0x000b
        */
        throw new UnsupportedOperationException("Method not decompiled: gnu.io.RXTXPort.close():void");
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (this.fd > 0) {
            close();
        }
        z.finalize();
    }

    class SerialOutputStream extends OutputStream {
        SerialOutputStream() {
        }

        public void write(int b) throws IOException {
            if (RXTXPort.this.speed != 0 && !RXTXPort.this.monThreadisInterrupted) {
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort.this.IOLocked++;
                }
                try {
                    RXTXPort.this.waitForTheNativeCodeSilly();
                    if (RXTXPort.this.fd == 0) {
                        throw new IOException();
                    }
                    RXTXPort.this.writeByte(b, RXTXPort.this.monThreadisInterrupted);
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort rXTXPort = RXTXPort.this;
                        rXTXPort.IOLocked--;
                    }
                } catch (Throwable th) {
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort rXTXPort2 = RXTXPort.this;
                        rXTXPort2.IOLocked--;
                        throw th;
                    }
                }
            }
        }

        public void write(byte[] b) throws IOException {
            if (RXTXPort.this.speed == 0 || RXTXPort.this.monThreadisInterrupted) {
                return;
            }
            if (RXTXPort.this.fd == 0) {
                throw new IOException();
            }
            synchronized (RXTXPort.this.IOLockedMutex) {
                RXTXPort.this.IOLocked++;
            }
            try {
                RXTXPort.this.waitForTheNativeCodeSilly();
                RXTXPort.this.writeArray(b, 0, b.length, RXTXPort.this.monThreadisInterrupted);
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort rXTXPort = RXTXPort.this;
                    rXTXPort.IOLocked--;
                }
            } catch (Throwable th) {
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort rXTXPort2 = RXTXPort.this;
                    rXTXPort2.IOLocked--;
                    throw th;
                }
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (RXTXPort.this.speed != 0) {
                if (off + len > b.length) {
                    throw new IndexOutOfBoundsException("Invalid offset/length passed to read");
                }
                byte[] send = new byte[len];
                System.arraycopy(b, off, send, 0, len);
                if (RXTXPort.this.fd == 0) {
                    throw new IOException();
                } else if (!RXTXPort.this.monThreadisInterrupted) {
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort.this.IOLocked++;
                    }
                    try {
                        RXTXPort.this.waitForTheNativeCodeSilly();
                        RXTXPort.this.writeArray(send, 0, len, RXTXPort.this.monThreadisInterrupted);
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort = RXTXPort.this;
                            rXTXPort.IOLocked--;
                        }
                    } catch (Throwable th) {
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort2 = RXTXPort.this;
                            rXTXPort2.IOLocked--;
                            throw th;
                        }
                    }
                }
            }
        }

        public void flush() throws IOException {
            if (RXTXPort.this.speed != 0) {
                if (RXTXPort.this.fd == 0) {
                    throw new IOException();
                } else if (!RXTXPort.this.monThreadisInterrupted) {
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort.this.IOLocked++;
                    }
                    try {
                        RXTXPort.this.waitForTheNativeCodeSilly();
                        if (RXTXPort.this.nativeDrain(RXTXPort.this.monThreadisInterrupted)) {
                            RXTXPort.this.sendEvent(2, true);
                        }
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort = RXTXPort.this;
                            rXTXPort.IOLocked--;
                        }
                    } catch (Throwable th) {
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort2 = RXTXPort.this;
                            rXTXPort2.IOLocked--;
                            throw th;
                        }
                    }
                }
            }
        }
    }

    class SerialInputStream extends InputStream {
        SerialInputStream() {
        }

        public synchronized int read() throws IOException {
            int result;
            if (RXTXPort.this.fd == 0) {
                throw new IOException();
            }
            if (RXTXPort.this.monThreadisInterrupted) {
                RXTXPort.z.reportln("+++++++++ read() monThreadisInterrupted");
            }
            synchronized (RXTXPort.this.IOLockedMutex) {
                RXTXPort.this.IOLocked++;
            }
            try {
                RXTXPort.this.waitForTheNativeCodeSilly();
                result = RXTXPort.this.readByte();
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort rXTXPort = RXTXPort.this;
                    rXTXPort.IOLocked--;
                }
            } catch (Throwable th) {
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort rXTXPort2 = RXTXPort.this;
                    rXTXPort2.IOLocked--;
                    throw th;
                }
            }
            return result;
        }

        public synchronized int read(byte[] b) throws IOException {
            int i = 0;
            synchronized (this) {
                if (!RXTXPort.this.monThreadisInterrupted) {
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort.this.IOLocked++;
                    }
                    try {
                        RXTXPort.this.waitForTheNativeCodeSilly();
                        i = read(b, 0, b.length);
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort = RXTXPort.this;
                            rXTXPort.IOLocked--;
                        }
                    } catch (Throwable th) {
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort rXTXPort2 = RXTXPort.this;
                            rXTXPort2.IOLocked--;
                            throw th;
                        }
                    }
                }
            }
            return i;
        }

        public synchronized int read(byte[] b, int off, int len) throws IOException {
            int Minimum;
            int i = 0;
            synchronized (this) {
                if (RXTXPort.this.fd == 0) {
                    RXTXPort.z.reportln("+++++++ IOException()\n");
                    throw new IOException();
                } else if (b == null) {
                    RXTXPort.z.reportln("+++++++ NullPointerException()\n");
                    throw new NullPointerException();
                } else if (off < 0 || len < 0 || off + len > b.length) {
                    RXTXPort.z.reportln("+++++++ IndexOutOfBoundsException()\n");
                    throw new IndexOutOfBoundsException();
                } else if (len != 0) {
                    int Minimum2 = len;
                    if (RXTXPort.this.threshold == 0) {
                        int a = RXTXPort.this.nativeavailable();
                        if (a == 0) {
                            Minimum = 1;
                        } else {
                            Minimum = Math.min(Minimum2, a);
                        }
                    } else {
                        Minimum = Math.min(Minimum2, RXTXPort.this.threshold);
                    }
                    if (!RXTXPort.this.monThreadisInterrupted) {
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort.this.IOLocked++;
                        }
                        try {
                            RXTXPort.this.waitForTheNativeCodeSilly();
                            i = RXTXPort.this.readArray(b, off, Minimum);
                            synchronized (RXTXPort.this.IOLockedMutex) {
                                RXTXPort rXTXPort = RXTXPort.this;
                                rXTXPort.IOLocked--;
                            }
                        } catch (Throwable th) {
                            synchronized (RXTXPort.this.IOLockedMutex) {
                                RXTXPort rXTXPort2 = RXTXPort.this;
                                rXTXPort2.IOLocked--;
                                throw th;
                            }
                        }
                    }
                }
            }
            return i;
        }

        public synchronized int read(byte[] b, int off, int len, byte[] t) throws IOException {
            int Minimum;
            int i = 0;
            synchronized (this) {
                if (RXTXPort.this.fd == 0) {
                    RXTXPort.z.reportln("+++++++ IOException()\n");
                    throw new IOException();
                } else if (b == null) {
                    RXTXPort.z.reportln("+++++++ NullPointerException()\n");
                    throw new NullPointerException();
                } else if (off < 0 || len < 0 || off + len > b.length) {
                    RXTXPort.z.reportln("+++++++ IndexOutOfBoundsException()\n");
                    throw new IndexOutOfBoundsException();
                } else if (len != 0) {
                    int Minimum2 = len;
                    if (RXTXPort.this.threshold == 0) {
                        int a = RXTXPort.this.nativeavailable();
                        if (a == 0) {
                            Minimum = 1;
                        } else {
                            Minimum = Math.min(Minimum2, a);
                        }
                    } else {
                        Minimum = Math.min(Minimum2, RXTXPort.this.threshold);
                    }
                    if (!RXTXPort.this.monThreadisInterrupted) {
                        synchronized (RXTXPort.this.IOLockedMutex) {
                            RXTXPort.this.IOLocked++;
                        }
                        try {
                            RXTXPort.this.waitForTheNativeCodeSilly();
                            i = RXTXPort.this.readTerminatedArray(b, off, Minimum, t);
                            synchronized (RXTXPort.this.IOLockedMutex) {
                                RXTXPort rXTXPort = RXTXPort.this;
                                rXTXPort.IOLocked--;
                            }
                        } catch (Throwable th) {
                            synchronized (RXTXPort.this.IOLockedMutex) {
                                RXTXPort rXTXPort2 = RXTXPort.this;
                                rXTXPort2.IOLocked--;
                                throw th;
                            }
                        }
                    }
                }
            }
            return i;
        }

        public synchronized int available() throws IOException {
            int nativeavailable;
            if (RXTXPort.this.monThreadisInterrupted) {
                nativeavailable = 0;
            } else {
                synchronized (RXTXPort.this.IOLockedMutex) {
                    RXTXPort.this.IOLocked++;
                }
                try {
                    nativeavailable = RXTXPort.this.nativeavailable();
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort rXTXPort = RXTXPort.this;
                        rXTXPort.IOLocked--;
                    }
                } catch (Throwable th) {
                    synchronized (RXTXPort.this.IOLockedMutex) {
                        RXTXPort rXTXPort2 = RXTXPort.this;
                        rXTXPort2.IOLocked--;
                        throw th;
                    }
                }
            }
            return nativeavailable;
        }
    }

    class MonitorThread extends Thread {
        /* access modifiers changed from: private */
        public volatile boolean BI = false;
        /* access modifiers changed from: private */
        public volatile boolean CD = false;
        /* access modifiers changed from: private */
        public volatile boolean CTS = false;
        /* access modifiers changed from: private */
        public volatile boolean DSR = false;
        /* access modifiers changed from: private */
        public volatile boolean Data = false;
        /* access modifiers changed from: private */
        public volatile boolean FE = false;
        /* access modifiers changed from: private */
        public volatile boolean OE = false;
        /* access modifiers changed from: private */
        public volatile boolean Output = false;
        /* access modifiers changed from: private */
        public volatile boolean PE = false;
        /* access modifiers changed from: private */
        public volatile boolean RI = false;

        MonitorThread() {
        }

        public void run() {
            RXTXPort.this.monThreadisInterrupted = false;
            RXTXPort.this.eventLoop();
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
        }
    }

    @Deprecated
    public void setRcvFifoTrigger(int trigger) {
    }

    public static int staticGetBaudRate(String port) throws UnsupportedCommOperationException {
        return nativeStaticGetBaudRate(port);
    }

    public static int staticGetDataBits(String port) throws UnsupportedCommOperationException {
        return nativeStaticGetDataBits(port);
    }

    public static int staticGetParity(String port) throws UnsupportedCommOperationException {
        return nativeStaticGetParity(port);
    }

    public static int staticGetStopBits(String port) throws UnsupportedCommOperationException {
        return nativeStaticGetStopBits(port);
    }

    public static void staticSetSerialPortParams(String f, int b, int d, int s, int p) throws UnsupportedCommOperationException {
        nativeStaticSetSerialPortParams(f, b, d, s, p);
    }

    public static boolean staticSetDSR(String port, boolean flag) throws UnsupportedCommOperationException {
        return nativeStaticSetDSR(port, flag);
    }

    public static boolean staticSetDTR(String port, boolean flag) throws UnsupportedCommOperationException {
        return nativeStaticSetDTR(port, flag);
    }

    public static boolean staticSetRTS(String port, boolean flag) throws UnsupportedCommOperationException {
        return nativeStaticSetRTS(port, flag);
    }

    public static boolean staticIsRTS(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsRTS(port);
    }

    public static boolean staticIsCD(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsCD(port);
    }

    public static boolean staticIsCTS(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsCTS(port);
    }

    public static boolean staticIsDSR(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsDSR(port);
    }

    public static boolean staticIsDTR(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsDTR(port);
    }

    public static boolean staticIsRI(String port) throws UnsupportedCommOperationException {
        return nativeStaticIsRI(port);
    }

    public byte getParityErrorChar() throws UnsupportedCommOperationException {
        return nativeGetParityErrorChar();
    }

    public boolean setParityErrorChar(byte b) throws UnsupportedCommOperationException {
        return nativeSetParityErrorChar(b);
    }

    public byte getEndOfInputChar() throws UnsupportedCommOperationException {
        return nativeGetEndOfInputChar();
    }

    public boolean setEndOfInputChar(byte b) throws UnsupportedCommOperationException {
        return nativeSetEndOfInputChar(b);
    }

    public boolean setUARTType(String type, boolean test) throws UnsupportedCommOperationException {
        return nativeSetUartType(type, test);
    }

    public String getUARTType() throws UnsupportedCommOperationException {
        return nativeGetUartType();
    }

    public boolean setBaudBase(int BaudBase) throws UnsupportedCommOperationException, IOException {
        return nativeSetBaudBase(BaudBase);
    }

    public int getBaudBase() throws UnsupportedCommOperationException, IOException {
        return nativeGetBaudBase();
    }

    public boolean setDivisor(int Divisor) throws UnsupportedCommOperationException, IOException {
        return nativeSetDivisor(Divisor);
    }

    public int getDivisor() throws UnsupportedCommOperationException, IOException {
        return nativeGetDivisor();
    }

    public boolean setLowLatency() throws UnsupportedCommOperationException {
        return nativeSetLowLatency();
    }

    public boolean getLowLatency() throws UnsupportedCommOperationException {
        return nativeGetLowLatency();
    }

    public boolean setCallOutHangup(boolean NoHup) throws UnsupportedCommOperationException {
        return nativeSetCallOutHangup(NoHup);
    }

    public boolean getCallOutHangup() throws UnsupportedCommOperationException {
        return nativeGetCallOutHangup();
    }

    public boolean clearCommInput() throws UnsupportedCommOperationException {
        return nativeClearCommInput();
    }
}
