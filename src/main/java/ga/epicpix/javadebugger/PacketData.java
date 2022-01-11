package ga.epicpix.javadebugger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketData implements IReadWrite {

    private final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    private final DataOutputStream out = new DataOutputStream(byteArray);

    public PacketData(int id, int commandSet, int command) throws IOException {
        WriteInt(id);
        WriteByte((byte) 0x00);
        WriteByte((byte) commandSet);
        WriteByte((byte) command);
    }

    public void WriteBytes(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void WriteByte(byte b) throws IOException {
        out.writeByte(b);
    }

    public void WriteShort(short s) throws IOException {
        out.writeShort(s);
    }

    public void WriteInt(int i) throws IOException {
        out.writeInt(i);
    }

    public void WriteLong(long l) throws IOException {
        out.writeLong(l);
    }

    public byte[] finish() throws IOException {
        out.close();
        byte[] arr = byteArray.toByteArray();
        byte[] resp = new byte[arr.length + 4];
        int len = resp.length;
        resp[0] = (byte) (len >> 24);
        resp[1] = (byte) (len >> 16);
        resp[2] = (byte) (len >> 8);
        resp[3] = (byte) (len);
        System.arraycopy(arr, 0, resp, 4, arr.length);
        return resp;
    }

    public byte[] ReadBytes(int bufSize) throws IOException {
        throw new IOException("Read methods not implemented");
    }

    public byte ReadByte() throws IOException {
        throw new IOException("Read methods not implemented");
    }

    public short ReadShort() throws IOException {
        throw new IOException("Read methods not implemented");
    }

    public int ReadInt() throws IOException {
        throw new IOException("Read methods not implemented");
    }

    public long ReadLong() throws IOException {
        throw new IOException("Read methods not implemented");
    }
}
