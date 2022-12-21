package gnu.io;

public class RXTXVersion {
    private static String Version = "RXTX-2.2pre2";

    public static native String nativeGetVersion();

    static {
        System.loadLibrary("rxtxSerialIO");
    }

    public static String getVersion() {
        return Version;
    }
}
