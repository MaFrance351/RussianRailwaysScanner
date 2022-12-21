package gnu.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;

public class RXTXCommDriver implements CommDriver {
    private static final boolean debug = false;
    private static final boolean devel = false;
    private static final boolean noVersionOutput = "true".equals(System.getProperty("gnu.io.rxtx.NoVersionOutput"));
    private String deviceDirectory;
    private String osName;

    private native String getDeviceDirectory();

    private native boolean isPortPrefixValid(String str);

    public static native String nativeGetVersion();

    private native boolean registerKnownPorts(int i);

    private native boolean testRead(String str, int i);

    static {
        String LibVersion;
        System.loadLibrary("rxtxSerialIO");
        String JarVersion = RXTXVersion.getVersion();
        try {
            LibVersion = RXTXVersion.nativeGetVersion();
        } catch (Error e) {
            LibVersion = nativeGetVersion();
        }
        if (!JarVersion.equals(LibVersion)) {
            System.out.println("WARNING:  RXTX Version mismatch\n\tJar version = " + JarVersion + "\n\tnative lib Version = " + LibVersion);
        }
    }

    private final String[] getValidPortPrefixes(String[] CandidatePortPrefixes) {
        String[] ValidPortPrefixes = new String[256];
        if (CandidatePortPrefixes == null) {
        }
        int i = 0;
        for (int j = 0; j < CandidatePortPrefixes.length; j++) {
            if (isPortPrefixValid(CandidatePortPrefixes[j])) {
                ValidPortPrefixes[i] = CandidatePortPrefixes[j];
                i++;
            }
        }
        String[] returnArray = new String[i];
        System.arraycopy(ValidPortPrefixes, 0, returnArray, 0, i);
        if (ValidPortPrefixes[0] == null) {
        }
        return returnArray;
    }

    private void checkSolaris(String PortName, int PortType) {
        char[] p = {'['};
        p[0] = 'a';
        while (p[0] < '{') {
            if (testRead(PortName.concat(new String(p)), PortType)) {
                CommPortIdentifier.addPortName(PortName.concat(new String(p)), PortType, this);
            }
            p[0] = (char) (p[0] + 1);
        }
        p[0] = '0';
        while (p[0] <= '9') {
            if (testRead(PortName.concat(new String(p)), PortType)) {
                CommPortIdentifier.addPortName(PortName.concat(new String(p)), PortType, this);
            }
            p[0] = (char) (p[0] + 1);
        }
    }

    private void registerValidPorts(String[] CandidateDeviceNames, String[] ValidPortPrefixes, int PortType) {
        String PortName;
        if (CandidateDeviceNames != null && ValidPortPrefixes != null) {
            for (int i = 0; i < CandidateDeviceNames.length; i++) {
                for (String V : ValidPortPrefixes) {
                    int VL = V.length();
                    String C = CandidateDeviceNames[i];
                    if (C.length() >= VL) {
                        String CU = C.substring(VL).toUpperCase();
                        String Cl = C.substring(VL).toLowerCase();
                        if (C.regionMatches(0, V, 0, VL) && CU.equals(Cl)) {
                            if (this.osName.toLowerCase().indexOf("windows") == -1) {
                                PortName = String.valueOf(this.deviceDirectory) + C;
                            } else {
                                PortName = C;
                            }
                            if (this.osName.equals("Solaris") || this.osName.equals("SunOS")) {
                                checkSolaris(PortName, PortType);
                            } else if (testRead(PortName, PortType)) {
                                CommPortIdentifier.addPortName(PortName, PortType, this);
                            }
                        }
                    }
                }
            }
        }
    }

    public void initialize() {
        this.osName = System.getProperty("os.name");
        this.deviceDirectory = getDeviceDirectory();
        for (int PortType = 1; PortType <= 2; PortType++) {
            if (!registerSpecifiedPorts(PortType) && !registerKnownPorts(PortType)) {
                registerScannedPorts(PortType);
            }
        }
    }

    private void addSpecifiedPorts(String names, int PortType) {
        StringTokenizer tok = new StringTokenizer(names, System.getProperty("path.separator", ":"));
        while (tok.hasMoreElements()) {
            String PortName = tok.nextToken();
            if (testRead(PortName, PortType)) {
                CommPortIdentifier.addPortName(PortName, PortType, this);
            }
        }
    }

    private boolean registerSpecifiedPorts(int PortType) {
        String val = null;
        Properties origp = System.getProperties();
        try {
            FileInputStream rxtx_prop = new FileInputStream(String.valueOf("/etc" + System.getProperty("file.separator")) + "gnu.io.rxtx.properties");
            Properties p = new Properties();
            p.load(rxtx_prop);
            System.setProperties(p);
            for (String key : p.keySet()) {
                System.setProperty(key, p.getProperty(key));
            }
        } catch (Exception e) {
        }
        switch (PortType) {
            case 1:
                val = System.getProperty("gnu.io.rxtx.SerialPorts");
                if (val == null) {
                    val = System.getProperty("gnu.io.SerialPorts");
                    break;
                }
                break;
            case 2:
                val = System.getProperty("gnu.io.rxtx.ParallelPorts");
                if (val == null) {
                    val = System.getProperty("gnu.io.ParallelPorts");
                    break;
                }
                break;
        }
        System.setProperties(origp);
        if (val == null) {
            return false;
        }
        addSpecifiedPorts(val, PortType);
        return true;
    }

    private void registerScannedPorts(int PortType) {
        String[] CandidateDeviceNames;
        if (this.osName.equals("Windows CE")) {
            CandidateDeviceNames = new String[]{"COM1:", "COM2:", "COM3:", "COM4:", "COM5:", "COM6:", "COM7:", "COM8:"};
        } else if (this.osName.toLowerCase().indexOf("windows") != -1) {
            String[] temp = new String[259];
            for (int i = 1; i <= 256; i++) {
                temp[i - 1] = "COM" + i;
            }
            for (int i2 = 1; i2 <= 3; i2++) {
                temp[i2 + 255] = "LPT" + i2;
            }
            CandidateDeviceNames = temp;
        } else if (this.osName.equals("Solaris") || this.osName.equals("SunOS")) {
            String[] term = new String[2];
            int l = 0;
            if (new File("/dev/term").list().length > 0) {
                term[0] = "term/";
                l = 0 + 1;
            }
            String[] temp2 = new String[l];
            for (int l2 = l - 1; l2 >= 0; l2--) {
                temp2[l2] = term[l2];
            }
            CandidateDeviceNames = temp2;
        } else {
            CandidateDeviceNames = new File(this.deviceDirectory).list();
        }
        if (CandidateDeviceNames != null) {
            String[] CandidatePortPrefixes = new String[0];
            switch (PortType) {
                case 1:
                    if (!this.osName.equals("Linux")) {
                        if (!this.osName.equals("Linux-all-ports")) {
                            if (this.osName.toLowerCase().indexOf("qnx") == -1) {
                                if (!this.osName.equals("Irix")) {
                                    if (!this.osName.equals("FreeBSD")) {
                                        if (!this.osName.equals("NetBSD")) {
                                            if (!this.osName.equals("Solaris") && !this.osName.equals("SunOS")) {
                                                if (!this.osName.equals("HP-UX")) {
                                                    if (!this.osName.equals("UnixWare") && !this.osName.equals("OpenUNIX")) {
                                                        if (!this.osName.equals("OpenServer")) {
                                                            if (!this.osName.equals("Compaq's Digital UNIX") && !this.osName.equals("OSF1")) {
                                                                if (!this.osName.equals("BeOS")) {
                                                                    if (!this.osName.equals("Mac OS X")) {
                                                                        if (this.osName.toLowerCase().indexOf("windows") != -1) {
                                                                            CandidatePortPrefixes = new String[]{"COM"};
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        CandidatePortPrefixes = new String[]{"cu.KeyUSA28X191.", "tty.KeyUSA28X191.", "cu.KeyUSA28X181.", "tty.KeyUSA28X181.", "cu.KeyUSA19181.", "tty.KeyUSA19181."};
                                                                        break;
                                                                    }
                                                                } else {
                                                                    CandidatePortPrefixes = new String[]{"serial"};
                                                                    break;
                                                                }
                                                            } else {
                                                                CandidatePortPrefixes = new String[]{"tty0"};
                                                                break;
                                                            }
                                                        } else {
                                                            CandidatePortPrefixes = new String[]{"tty1A", "tty2A", "tty3A", "tty4A", "tty5A", "tty6A", "tty7A", "tty8A", "tty9A", "tty10A", "tty11A", "tty12A", "tty13A", "tty14A", "tty15A", "tty16A", "ttyu1A", "ttyu2A", "ttyu3A", "ttyu4A", "ttyu5A", "ttyu6A", "ttyu7A", "ttyu8A", "ttyu9A", "ttyu10A", "ttyu11A", "ttyu12A", "ttyu13A", "ttyu14A", "ttyu15A", "ttyu16A"};
                                                            break;
                                                        }
                                                    } else {
                                                        CandidatePortPrefixes = new String[]{"tty00s", "tty01s", "tty02s", "tty03s"};
                                                        break;
                                                    }
                                                } else {
                                                    CandidatePortPrefixes = new String[]{"tty0p", "tty1p"};
                                                    break;
                                                }
                                            } else {
                                                CandidatePortPrefixes = new String[]{"term/", "cua/"};
                                                break;
                                            }
                                        } else {
                                            CandidatePortPrefixes = new String[]{"tty0"};
                                            break;
                                        }
                                    } else {
                                        CandidatePortPrefixes = new String[]{"ttyd", "cuaa", "ttyA", "cuaA", "ttyD", "cuaD", "ttyE", "cuaE", "ttyF", "cuaF", "ttyR", "cuaR", "stl"};
                                        break;
                                    }
                                } else {
                                    CandidatePortPrefixes = new String[]{"ttyc", "ttyd", "ttyf", "ttym", "ttyq", "tty4d", "tty4f", "midi", "us"};
                                    break;
                                }
                            } else {
                                CandidatePortPrefixes = new String[]{"ser"};
                                break;
                            }
                        } else {
                            CandidatePortPrefixes = new String[]{"comx", "holter", "modem", "rfcomm", "ttyircomm", "ttycosa0c", "ttycosa1c", "ttyACM", "ttyC", "ttyCH", "ttyD", "ttyE", "ttyF", "ttyH", "ttyI", "ttyL", "ttyM", "ttyMX", "ttyP", "ttyR", "ttyS", "ttySI", "ttySR", "ttyT", "ttyUSB", "ttyV", "ttyW", "ttyX"};
                            break;
                        }
                    } else {
                        CandidatePortPrefixes = new String[]{"ttyS", "ttySA", "ttyUSB", "rfcomm", "ttyircomm"};
                        break;
                    }
                    break;
                case 2:
                    if (!this.osName.equals("Linux")) {
                        if (!this.osName.equals("FreeBSD")) {
                            if (this.osName.toLowerCase().indexOf("windows") == -1) {
                                CandidatePortPrefixes = new String[0];
                                break;
                            } else {
                                CandidatePortPrefixes = new String[]{"LPT"};
                                break;
                            }
                        } else {
                            CandidatePortPrefixes = new String[]{"lpt"};
                            break;
                        }
                    } else {
                        CandidatePortPrefixes = new String[]{"lp"};
                        break;
                    }
            }
            registerValidPorts(CandidateDeviceNames, CandidatePortPrefixes, PortType);
        }
    }

    public CommPort getCommPort(String PortName, int PortType) {
        switch (PortType) {
            case 1:
                try {
                    if (this.osName.toLowerCase().indexOf("windows") == -1) {
                        return new RXTXPort(PortName);
                    }
                    return new RXTXPort(String.valueOf(this.deviceDirectory) + PortName);
                } catch (PortInUseException e) {
                    break;
                }
        }
        return null;
    }

    public void Report(String arg) {
        System.out.println(arg);
    }
}
