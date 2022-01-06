package ga.epicpix.javadebugger;

public record VMIdSizes(int FieldIdSize, int MethodIdSize, int ObjectIdSize, int ReferenceTypeIdSize, int FrameIdSize) {

    public void Print() {
        System.out.println("FieldIdSize: " + FieldIdSize + " bytes");
        System.out.println("MethodIdSize: " + MethodIdSize + " bytes");
        System.out.println("ObjectIdSize: " + ObjectIdSize + " bytes");
        System.out.println("ReferenceTypeIdSize: " + ReferenceTypeIdSize + " bytes");
        System.out.println("FrameIdSize: " + FrameIdSize + " bytes");
    }

}
