package gnu.io;

import java.util.Enumeration;

class CommPortEnumerator implements Enumeration {
    private static final boolean debug = false;
    private CommPortIdentifier index;

    CommPortEnumerator() {
    }

    public Object nextElement() {
        CommPortIdentifier commPortIdentifier;
        synchronized (CommPortIdentifier.Sync) {
            if (this.index != null) {
                this.index = this.index.next;
            } else {
                this.index = CommPortIdentifier.CommPortIndex;
            }
            commPortIdentifier = this.index;
        }
        return commPortIdentifier;
    }

    public boolean hasMoreElements() {
        boolean z = false;
        synchronized (CommPortIdentifier.Sync) {
            if (this.index != null) {
                if (this.index.next != null) {
                    z = true;
                }
            } else if (CommPortIdentifier.CommPortIndex != null) {
                z = true;
            }
        }
        return z;
    }
}
