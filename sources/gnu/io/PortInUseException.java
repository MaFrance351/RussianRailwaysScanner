package gnu.io;

public class PortInUseException extends Exception {
    public String currentOwner;

    PortInUseException(String str) {
        super(str);
        this.currentOwner = str;
    }

    public PortInUseException() {
    }
}
