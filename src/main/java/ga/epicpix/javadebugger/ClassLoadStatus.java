package ga.epicpix.javadebugger;

import java.util.ArrayList;

public class ClassLoadStatus {

    private final int status;

    public ClassLoadStatus(int status) {
        this.status = status;
    }

    public int getRawStatus() {
        return status;
    }

    public String getStatus() {
        ArrayList<String> statuses = new ArrayList<>();
        if(((status >> 0) & 1) == 1) statuses.add("VERIFIED");
        if(((status >> 1) & 1) == 1) statuses.add("PREPARED");
        if(((status >> 2) & 1) == 1) statuses.add("INITIALIZED");
        if(((status >> 3) & 1) == 1) statuses.add("ERROR");
        return String.join(", ", statuses.toArray(new String[0]));
    }
}
