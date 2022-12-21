package gnu.io;

public class NoSuchPortException extends Exception {
    NoSuchPortException(String str) {
        super(str);
    }

    public NoSuchPortException() {
    }
}
