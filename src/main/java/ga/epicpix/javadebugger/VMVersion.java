package ga.epicpix.javadebugger;

public record VMVersion(String description, int jdwpMajor, int jdwpMinor, String vmVersion, String vmName) {

    public void Print() {
        System.out.println("JVM Name: " + vmName);
        System.out.println("JVM Version: " + vmVersion);
        System.out.println("JDWP Version: " + jdwpMajor + "." + jdwpMinor);
    }

}
