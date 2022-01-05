package ga.epicpix.javadebugger;

import java.io.IOException;

public interface IReadWrite {

    void WriteBytes(byte[] b) throws IOException;
    void WriteByte(byte b) throws IOException;
    void WriteShort(short s) throws IOException;
    void WriteInt(int i) throws IOException;

    byte[] ReadBytes(int bufSize) throws IOException;
    byte ReadByte() throws IOException;
    short ReadShort() throws IOException;
    int ReadInt() throws IOException;

}
