package ga.epicpix.javadebugger;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Debugger implements IReadWrite {

    private final DataOutput output;
    private final DataInput input;

    private int id;
    private final ConcurrentHashMap<Integer, ReplyData> replies = new ConcurrentHashMap<>();
    private volatile boolean handshakeDone = false;
    private VMCapabilities capabilities;
    private VMVersion version;

    public Debugger(DataOutput output, DataInput input) {
        this.output = output;
        this.input = input;
        Thread thread = new Thread(() -> {
            try {
                while(true) {
                    if(!handshakeDone)
                        continue;
                    int length = ReadInt();
                    byte[] bytes = ReadBytes(length - 4);
                    DataInput i = new DataInputStream(new ByteArrayInputStream(bytes));
                    int id = i.readInt();
                    int flags = i.readUnsignedByte();
                    if ((flags & 0x80) == 0x80) {
                        int errorCode = i.readUnsignedShort();
                        ReplyData reply = new ReplyData();
                        reply.length = length;
                        reply.id = id;
                        reply.flags = flags;
                        reply.errorCode = ErrorCodes.getErrorCode(errorCode);
                        reply.input = i;
                        reply.bytes = bytes;
                        replies.put(id, reply);
                    } else {
                        int commandSet = i.readUnsignedByte();
                        int command = i.readUnsignedByte();
                        System.err.println("Unknown command from JVM: " + commandSet + "-" + command);
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

    public void WriteBytes(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
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
        if(data.errorCode != ErrorCodes.NONE) System.err.println("Response has Error Code: " + data.errorCode.errorMessage + " (" + data.errorCode.name() + ")");
        return replyInfo.handle(data.length, data.errorCode, new IReadWrite() {
            public void WriteBytes(byte[] b, int off, int len) throws IOException {Debugger.this.WriteBytes(b, off, len);}
            public void WriteByte(byte b) throws IOException {Debugger.this.WriteByte(b);}
            public void WriteShort(short s) throws IOException {Debugger.this.WriteShort(s);}
            public void WriteInt(int i) throws IOException {Debugger.this.WriteInt(i);}
            public byte[] ReadBytes(int bufSize) throws IOException {
                byte[] bytes = new byte[bufSize];
                for(int i = 0; i<bytes.length; i++) bytes[i] = ReadByte();
                return bytes;
            }
            public byte ReadByte() throws IOException {return data.input.readByte();}
            public short ReadShort() throws IOException {return data.input.readShort();}
            public int ReadInt() throws IOException {return data.input.readInt();}
        }, data.bytes);
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
        if(capabilities != null) return capabilities;
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 17);
        return capabilities = WaitForReply(id, (length, errorCode, input, bytes) -> {
            int caps = 0;
            for(int i = 0; i<32; i++) caps |= input.ReadByte() << i;
            return new VMCapabilities(caps);
        });
    }

    public VMVersion Version() throws IOException {
        if(version != null) return version;
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 1);
        return version = WaitForReply(id, (length, errorCode, input, bytes) -> new VMVersion(input.ReadString(), input.ReadInt(), input.ReadInt(), input.ReadString(), input.ReadString()));
    }

}
