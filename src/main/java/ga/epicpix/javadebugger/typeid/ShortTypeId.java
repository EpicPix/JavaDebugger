package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public final class ShortTypeId extends TypeId {

    private short val;

    public void write(IReadWrite out) throws IOException {
        out.WriteShort(val);
    }

    public void read(IReadWrite in) throws IOException {
        val = in.ReadShort();
    }

    public String toString() {
        String hex = Integer.toHexString(Short.toUnsignedInt(val));
        return "0x" + "0".repeat(4 - hex.length()) + hex;
    }

}
