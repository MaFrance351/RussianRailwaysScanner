package gnu.io;

public interface CommDriver {
    CommPort getCommPort(String str, int i);

    void initialize();
}
