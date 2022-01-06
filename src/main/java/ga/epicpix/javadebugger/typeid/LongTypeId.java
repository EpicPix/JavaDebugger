package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public final class LongTypeId extends TypeId {

    private long val;

    public LongTypeId() {}
    public LongTypeId(long val) {this.val = val;}

    public void write(IReadWrite out) throws IOException {
        out.WriteLong(val);
    }

    public void read(IReadWrite in) throws IOException {
        val = in.ReadLong();
    }

    public String toString() {
        String hex = Long.toHexString(val);
        return "0x" + "0".repeat(16 - hex.length()) + hex;
    }

    public int size() {
        return 8;
    }

}
