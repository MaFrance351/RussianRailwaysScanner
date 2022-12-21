package gnu.io;

import java.io.RandomAccessFile;
import java.util.logging.Logger;

public class Zystem {
    public static final int FILE_MODE = 1;
    public static final int J2EE_MSG_MODE = 5;
    public static final int J2SE_LOG_MODE = 6;
    public static final int MEX_MODE = 3;
    public static final int NET_MODE = 2;
    public static final int PRINT_MODE = 4;
    public static final int SILENT_MODE = 0;
    static int mode = 0;
    private static String target;

    public Zystem(int m) throws UnSupportedLoggerException {
        mode = m;
        startLogger("asdf");
    }

    public Zystem() throws UnSupportedLoggerException {
        String s = System.getProperty("gnu.io.log.mode");
        if (s == null) {
            mode = 0;
        } else if ("SILENT_MODE".equals(s)) {
            mode = 0;
        } else if ("FILE_MODE".equals(s)) {
            mode = 1;
        } else if ("NET_MODE".equals(s)) {
            mode = 2;
        } else if ("MEX_MODE".equals(s)) {
            mode = 3;
        } else if ("PRINT_MODE".equals(s)) {
            mode = 4;
        } else if ("J2EE_MSG_MODE".equals(s)) {
            mode = 5;
        } else if ("J2SE_LOG_MODE".equals(s)) {
            mode = 6;
        } else {
            try {
                mode = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                mode = 0;
            }
        }
        startLogger("asdf");
    }

    public void startLogger() throws UnSupportedLoggerException {
        if (mode != 0 && mode != 4) {
            throw new UnSupportedLoggerException("Target Not Allowed");
        }
    }

    public void startLogger(String t) throws UnSupportedLoggerException {
        target = t;
    }

    public void finalize() {
        mode = 0;
        target = null;
    }

    public void filewrite(String s) {
        try {
            RandomAccessFile w = new RandomAccessFile(target, "rw");
            w.seek(w.length());
            w.writeBytes(s);
            w.close();
        } catch (Exception e) {
            System.out.println("Debug output file write failed");
        }
    }

    public boolean report(String s) {
        if (mode != 2) {
            if (mode == 4) {
                System.out.println(s);
                return true;
            } else if (mode != 3) {
                if (mode == 0) {
                    return true;
                }
                if (mode == 1) {
                    filewrite(s);
                } else if (mode == 5) {
                    return false;
                } else {
                    if (mode == 6) {
                        Logger.getLogger("gnu.io").fine(s);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean reportln() {
        if (mode != 2) {
            if (mode == 4) {
                System.out.println();
                return true;
            } else if (mode != 3) {
                if (mode == 0) {
                    return true;
                }
                if (mode == 1) {
                    filewrite("\n");
                } else if (mode == 5) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean reportln(String s) {
        if (mode != 2) {
            if (mode == 4) {
                System.out.println(s);
                return true;
            } else if (mode != 3) {
                if (mode == 0) {
                    return true;
                }
                if (mode == 1) {
                    filewrite(String.valueOf(s) + "\n");
                } else if (mode == 5) {
                    return false;
                } else {
                    if (mode == 6) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
