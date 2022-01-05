package ga.epicpix.javadebugger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Debugger {

    private static Debugger activeDebugger;

    private final DataOutput output;
    private final DataInput input;

    public Debugger(DataOutput output, DataInput input) {
        this.output = output;
        this.input = input;
    }

    public static void setActiveDebugger(Debugger debugger) {
        activeDebugger = debugger;
    }

    private static void WriteBytes(byte[] b) throws IOException {
        activeDebugger.output.write(b);
    }

    private static void WriteByte(byte b) throws IOException {
        activeDebugger.output.write(b);
    }

    private static void WriteShort(short s) throws IOException {
        activeDebugger.output.writeShort(s);
    }

    private static void WriteInt(int i) throws IOException {
        activeDebugger.output.writeInt(i);
    }

    private static byte[] ReadBytes(int bufSize) throws IOException {
        byte[] bytes = new byte[bufSize];
        for(int i = 0; i<bytes.length; i++)
            bytes[i] = ReadByte();
        return bytes;
    }

    private static byte ReadByte() throws IOException {
        return activeDebugger.input.readByte();
    }

    private static short ReadShort() throws IOException {
        return activeDebugger.input.readShort();
    }

    private static int ReadInt() throws IOException {
        return activeDebugger.input.readInt();
    }


    public static boolean PerformHandshake() throws IOException {
        byte[] handshake = "JDWP-Handshake".getBytes();
        WriteBytes(handshake);
        byte[] response = ReadBytes(14);
        for(byte b = 0; b<response.length; b++)
            if(handshake[b] != response[b]) return false;
        return true;
    }

}
