package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;
import ga.epicpix.javadebugger.typeid.TypeIdTypes;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Debugger implements IReadWrite {

    private final DataOutput output;
    private final DataInput input;

    private int id;
    private final ConcurrentHashMap<Integer, ReplyData> replies = new ConcurrentHashMap<>();
    private volatile boolean handshakeDone = false;
    private VMCapabilities capabilities;
    private VMVersion version;
    private VMIdSizes idSizes;

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

    public void WriteLong(long l) throws IOException {
        output.writeLong(l);
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

    public long ReadLong() throws IOException {
        return input.readLong();
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
            public void WriteLong(long l) throws IOException {Debugger.this.WriteLong(l);}
            public byte[] ReadBytes(int bufSize) throws IOException {
                byte[] bytes = new byte[bufSize];
                for(int i = 0; i<bytes.length; i++) bytes[i] = ReadByte();
                return bytes;
            }
            public byte ReadByte() throws IOException {return data.input.readByte();}
            public short ReadShort() throws IOException {return data.input.readShort();}
            public int ReadInt() throws IOException {return data.input.readInt();}
            public long ReadLong() throws IOException {return data.input.readLong();}
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

    public VMIdSizes IdSizes() throws IOException {
        if(idSizes != null) return idSizes;
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 7);
        return idSizes = WaitForReply(id, (length, errorCode, input, bytes) -> new VMIdSizes(input.ReadInt(), input.ReadInt(), input.ReadInt(), input.ReadInt(), input.ReadInt()));
    }

    public void Exit(int exitCode) throws IOException {
        SendPacketHeader(4, GetIdAndIncrement(), 0x00, 1, 10);
        WriteInt(exitCode);
        if(output instanceof DataOutputStream out) out.close();
        if(input instanceof DataInputStream in) in.close();
    }

    public ArrayList<VMClassInfoData> AllClasses() throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 3);
        return WaitForReply(id, (length, errorCode, input, bytes) -> {
            int classes = input.ReadInt();
            ArrayList<VMClassInfoData> classList = new ArrayList<>(classes);
            for(int i = 0; i<classes; i++) {
                ReferenceType refTypeTag = ReferenceType.getReferenceType(input.ReadByte());
                TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, IdSizes());
                String signature = input.ReadString();
                ClassLoadStatus status = new ClassLoadStatus(input.ReadInt());
                classList.add(new VMClassInfoData(refTypeTag, typeId, signature, status));
            }
            return classList;
        });
    }

    public ArrayList<VMMethodInfoData> Methods(TypeId referenceId) throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(IdSizes().ReferenceTypeIdSize(), id, 0x00, 2, 5);
        WriteTypeId(referenceId);
        return WaitForReply(id, (length, errorCode, input, bytes) -> {
            int methods = input.ReadInt();
            ArrayList<VMMethodInfoData> methodList = new ArrayList<>(methods);
            for(int i = 0; i<methods; i++) {
                TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, IdSizes());
                String name = input.ReadString();
                String signature = input.ReadString();
                int modBits = input.ReadInt();
                methodList.add(new VMMethodInfoData(typeId, name, signature, modBits));
            }
            return methodList;
        });
    }

    public ArrayList<TypeId> AllThreads() throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(0, id, 0x00, 1, 4);
        return WaitForReply(id, (length, errorCode, input, bytes) -> {
            int threads = input.ReadInt();
            ArrayList<TypeId> threadIds = new ArrayList<>(threads);
            for(int i = 0; i<threads; i++) {
                TypeId typeId = input.ReadTypeId(TypeIdTypes.OBJECT_ID, IdSizes());
                threadIds.add(typeId);
            }
            return threadIds;
        });
    }

    public String ThreadName(TypeId threadId) throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(threadId.size(), id, 0x00, 11, 1);
        WriteTypeId(threadId);
        return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
    }

    public TypeId CreateString(String str) throws IOException {
        int id = GetIdAndIncrement();
        SendPacketHeader(4 + str.length(), id, 0x00, 1, 11);
        WriteString(str);
        return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadTypeId(TypeIdTypes.OBJECT_ID, IdSizes()));
    }

}
