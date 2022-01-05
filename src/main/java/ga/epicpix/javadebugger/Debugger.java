package ga.epicpix.javadebugger;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Debugger implements IReadWrite {

    private final DataOutput output;
    private final DataInput input;

    private int id;
    private final ConcurrentHashMap<Integer, ReplyData> replies = new ConcurrentHashMap<>();
    private volatile boolean handshakeDone = false;

    public Debugger(DataOutput output, DataInput input) {
        this.output = output;
        this.input = input;
        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    if(!handshakeDone)
                        continue;
                    int length = ReadInt();
                    DataInput i = new DataInputStream(new ByteArrayInputStream(ReadBytes(length - 4)));
                    int id = i.readInt();
                    int flags = i.readUnsignedByte();
                    if ((flags & 0x80) == 0x80) {
                        int errorCode = i.readUnsignedShort();
                        ReplyData reply = new ReplyData();
                        reply.length = length;
                        reply.id = id;
                        reply.flags = flags;
                        reply.errorCode = errorCode;
                        reply.input = i;
                        replies.put(id, reply);
                    } else {
                        throw new RuntimeException("CAnnot receive commands from the JVM");
                    }
                }
            }catch(IOException e) {
                System.out.println("Debugger Receiver disconnected");
            }
        }, "Debugger Receiver");
        thread.setDaemon(true);
        thread.start();
    }

    private int GetIdAndIncrement() {
        return id++;
    }

    public void WriteBytes(byte[] b) throws IOException {
        output.write(b);
    }

    public void WriteByte(byte b) throws IOException {
        output.write(b);
    }

    public void WriteShort(short s) throws IOException {
        output.writeShort(s);
    }

    public void WriteInt(int i) throws IOException {
        output.writeInt(i);
    }

    public byte[] ReadBytes(int bufSize) throws IOException {
        byte[] bytes = new byte[bufSize];
        for(int i = 0; i<bytes.length; i++)
            bytes[i] = ReadByte();
        return bytes;
    }

    public byte ReadByte() throws IOException {
        return input.readByte();
    }

    public short ReadShort() throws IOException {
        return input.readShort();
    }

    public int ReadInt() throws IOException {
        return input.readInt();
    }

    private <T> T WaitForReply(int id, ReplyInfo<T> replyInfo) throws IOException {
        while(replies.get(id) == null) {}
        ReplyData data = replies.get(id);
        return replyInfo.handle(data.length, data.errorCode, data.input);
    }

    public boolean PerformHandshake() throws IOException {
        byte[] handshake = "JDWP-Handshake".getBytes();
        WriteBytes(handshake);
        byte[] response = ReadBytes(14);
        for(byte b = 0; b<response.length; b++)
            if(handshake[b] != response[b]) return false;
        handshakeDone = true;
        return true;
    }

    public void SendPacketHeader(int length, int id, int flags, int commandSet, int command) throws IOException {
        WriteInt(length + 11);
        WriteInt(id);
        WriteByte((byte) flags);
        WriteByte((byte) commandSet);
        WriteByte((byte) command);
    }

    public VMCapabilities Capabilities() throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 17);
        return WaitForReply(id, (length, errorCode, input) -> new VMCapabilities(input.readInt()));
    }

}
