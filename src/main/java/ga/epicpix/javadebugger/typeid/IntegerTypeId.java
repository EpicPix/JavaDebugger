package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public final class IntegerTypeId extends TypeId {

    private int val;

    public void write(IReadWrite out) throws IOException {
        out.WriteInt(val);
    }

    public void read(IReadWrite in) throws IOException {
        val = in.ReadInt();
    }

    public String toString() {
        String hex = Integer.toHexString(val);
        return "0x" + "0".repeat(8 - hex.length()) + hex;
    }

    public int size() {
        return 4;
    }

}
