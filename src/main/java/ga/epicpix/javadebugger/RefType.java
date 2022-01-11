package ga.epicpix.javadebugger;

public enum RefType {

    CLASS(1), INTERFACE(2), ARRAY(3);

    private final int id;

    RefType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RefType getReferenceType(int id) {
        for(RefType r : values()) {
            if(r.id == id) {
                return r;
            }
        }
        throw new RuntimeException("Unknown Reference Type: " + id);
    }

}
