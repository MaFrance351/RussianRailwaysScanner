package gnu.io;

import java.util.EventListener;

public interface CommPortOwnershipListener extends EventListener {
    public static final int PORT_OWNED = 1;
    public static final int PORT_OWNERSHIP_REQUESTED = 3;
    public static final int PORT_UNOWNED = 2;

    void ownershipChange(int i);
}
