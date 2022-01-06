package ga.epicpix.javadebugger;

public record VMIdSizes(int FieldIdSize, int MethodIdSize, int ObjectIdSize, int ReferenceTypeIdSize, int FrameIdSize) {

    public void Print() {
        System.out.println("FieldIdSize: " + FieldIdSize + " bytes");
        System.out.println("MethodIdSize: " + MethodIdSize + " bytes");
        System.out.println("ObjectIdSize: " + ObjectIdSize + " bytes");
        System.out.println("ReferenceTypeIdSize: " + ReferenceTypeIdSize + " bytes");
        System.out.println("FrameIdSize: " + FrameIdSize + " bytes");
    }

    public int Size(TypeIdTypes type) {
        return switch (type) {
            case FIELD_ID -> FieldIdSize;
            case METHOD_ID -> MethodIdSize;
            case OBJECT_ID -> ObjectIdSize;
            case REFERENCE_TYPE_ID -> ReferenceTypeIdSize;
            case FRAME_ID -> FrameIdSize;
        };
    }
}
