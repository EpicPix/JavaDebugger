package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public final class ShortTypeId extends TypeId {

    private short val;

    public ShortTypeId() {}
    public ShortTypeId(short val) {this.val = val;}

    public void write(IReadWrite out) throws IOException {
        out.WriteShort(val);
    }

    public void read(IReadWrite in) throws IOException {
        val = in.ReadShort();
    }

    public String toString() {
        if(val == 0x0) return "null";
        String hex = Integer.toHexString(Short.toUnsignedInt(val));
        return "0x" + "0".repeat(4 - hex.length()) + hex;
    }

    public int size() {
        return 2;
    }

}
