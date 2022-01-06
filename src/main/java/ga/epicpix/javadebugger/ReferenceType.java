package ga.epicpix.javadebugger;

public enum ReferenceType {

    CLASS(1), INTERFACE(2), ARRAY(3);

    private final int id;

    ReferenceType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ReferenceType getReferenceType(int id) {
        for(ReferenceType r : values()) {
            if(r.id == id) {
                return r;
            }
        }
        throw new RuntimeException("Unknown Reference Type: " + id);
    }

}
