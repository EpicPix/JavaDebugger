package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public final class ByteTypeId extends TypeId {

    private byte val;

    public ByteTypeId() {}
    public ByteTypeId(byte val) {this.val = val;}

    public void write(IReadWrite out) throws IOException {
        out.WriteByte(val);
    }

    public void read(IReadWrite in) throws IOException {
        val = in.ReadByte();
    }

    public String toString() {
        if(val == 0x0) return "null";
        String hex = Integer.toHexString(Byte.toUnsignedInt(val));
        return "0x" + "0".repeat(2 - hex.length()) + hex;
    }

    public int size() {
        return 1;
    }

}
