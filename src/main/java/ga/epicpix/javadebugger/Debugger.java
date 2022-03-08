package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;
import ga.epicpix.javadebugger.typeid.TypeIdTypes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

    private final ConcurrentHashMap<Thread, PacketData> packetCreations = new ConcurrentHashMap<>();

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
        PacketData data = packetCreations.get(Thread.currentThread());
        if(data == null) output.write(b, off, len);
        else data.WriteBytes(b, off, len);
    }

    public void WriteByte(byte b) throws IOException {
        PacketData data = packetCreations.get(Thread.currentThread());
        if(data == null) output.write(b);
        else data.WriteByte(b);
    }

    public void WriteShort(short s) throws IOException {
        PacketData data = packetCreations.get(Thread.currentThread());
        if(data == null) output.writeShort(s);
        else data.WriteShort(s);
    }

    public void WriteInt(int i) throws IOException {
        PacketData data = packetCreations.get(Thread.currentThread());
        if(data == null) output.writeInt(i);
        else data.WriteInt(i);
    }

    public void WriteLong(long l) throws IOException {
        PacketData data = packetCreations.get(Thread.currentThread());
        if(data == null) output.writeLong(l);
        else data.WriteLong(l);
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

    public int StartRequestPacket(int commandSet, int command) throws IOException {
        int id = GetIdAndIncrement();
        PacketData data = new PacketData(id, commandSet, command);
        packetCreations.put(Thread.currentThread(), data);
        return id;
    }

    public void FinishPacket() throws IOException {
        PacketData data = packetCreations.remove(Thread.currentThread());
        WriteBytes(data.finish());
    }

    public int SendRequestPacket(int commandSet, int command) throws IOException {
        int id = StartRequestPacket(commandSet, command);
        FinishPacket();
        return id;
    }

    public final VirtualMachine VirtualMachine = new VirtualMachine();
    public final ReferenceType ReferenceType = new ReferenceType();
    public final ClassType ClassType = new ClassType();
    public final ArrayType ArrayType = new ArrayType();
    public final InterfaceType InterfaceType = new InterfaceType();
    public final Method Method = new Method();
    public final Field Field = new Field();
    public final ObjectReference ObjectReference = new ObjectReference();
    public final StringReference StringReference = new StringReference();
    public final ThreadReference ThreadReference = new ThreadReference();
    public final ThreadGroupReference ThreadGroupReference = new ThreadGroupReference();
    public final ArrayReference ArrayReference = new ArrayReference();
    public final ClassLoaderReference ClassLoaderReference = new ClassLoaderReference();
    public final EventRequest EventRequest = new EventRequest();
    public final StackFrame StackFrame = new StackFrame();
    public final ClassObjectReference ClassObjectReference = new ClassObjectReference();
    public final ModuleReference ModuleReference = new ModuleReference();


    public class VirtualMachine {

        public VMVersion Version() throws IOException {
            if(version != null) return version;
            int id = SendRequestPacket(1, 1);
            return version = WaitForReply(id, (length, errorCode, input, bytes) -> new VMVersion(input.ReadString(), input.ReadInt(), input.ReadInt(), input.ReadString(), input.ReadString()));
        }

        public ArrayList<VMClassInfoData> ClassesBySignature(String clazz) throws IOException {
            int id = StartRequestPacket(1, 2);
            WriteString(clazz);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int classes = input.ReadInt();
                ArrayList<VMClassInfoData> classList = new ArrayList<>(classes);
                for(int i = 0; i<classes; i++) {
                    RefType refTypeTag = RefType.getReferenceType(input.ReadByte());
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes());
                    ClassLoadStatus status = new ClassLoadStatus(input.ReadInt());
                    classList.add(new VMClassInfoData(refTypeTag, typeId, clazz, status));
                }
                return classList;
            });
        }

        public ArrayList<VMClassInfoData> AllClasses() throws IOException {
            int id = SendRequestPacket(1, 3);
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int classes = input.ReadInt();
                ArrayList<VMClassInfoData> classList = new ArrayList<>(classes);
                for(int i = 0; i<classes; i++) {
                    RefType refTypeTag = RefType.getReferenceType(input.ReadByte());
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes());
                    String signature = input.ReadString();
                    ClassLoadStatus status = new ClassLoadStatus(input.ReadInt());
                    classList.add(new VMClassInfoData(refTypeTag, typeId, signature, status));
                }
                return classList;
            });
        }

        public ArrayList<TypeId> AllThreads() throws IOException {
            int id = SendRequestPacket(1, 4);
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int threads = input.ReadInt();
                ArrayList<TypeId> threadIds = new ArrayList<>(threads);
                for(int i = 0; i<threads; i++) {
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes());
                    threadIds.add(typeId);
                }
                return threadIds;
            });
        }

        // TopLevelThreadGroups (5)
        // Dispose (6)

        public VMIdSizes IdSizes() throws IOException {
            if(idSizes != null) return idSizes;
            int id = SendRequestPacket(1, 7);
            return idSizes = WaitForReply(id, (length, errorCode, input, bytes) -> new VMIdSizes(input.ReadInt(), input.ReadInt(), input.ReadInt(), input.ReadInt(), input.ReadInt()));
        }

        public void Suspend() throws IOException {
            int id = SendRequestPacket(1, 8);
            WaitForReply(id, (length, errorCode, input, bytes) -> null);
        }

        public void Resume() throws IOException {
            int id = SendRequestPacket(1, 9);
            WaitForReply(id, (length, errorCode, input, bytes) -> null);
        }

        public void Exit(int exitCode) throws IOException {
            StartRequestPacket(1, 10);
            WriteInt(exitCode);
            FinishPacket();
            if(output instanceof DataOutputStream out) out.close();
            if(input instanceof DataInputStream in) in.close();
        }

        public TypeId CreateString(String str) throws IOException {
            int id = StartRequestPacket(1, 11);
            WriteString(str);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes()));
        }

        // Capabilities (12)

        public VMClasspaths ClassPaths() throws IOException {
            int id = SendRequestPacket(1, 13);
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                String baseDir = input.ReadString();

                int classpathsCount = input.ReadInt();
                ArrayList<String> classpaths = new ArrayList<>(classpathsCount);
                for(int i = 0; i<classpathsCount; i++) classpaths.add(input.ReadString());

                int bootClasspathsCount = input.ReadInt();
                ArrayList<String> bootClasspaths = new ArrayList<>(bootClasspathsCount);
                for(int i = 0; i<bootClasspathsCount; i++) bootClasspaths.add(input.ReadString());

                return new VMClasspaths(baseDir,  classpaths, bootClasspaths);
            });
        }

        // DisposeObjects (14)
        // HoldEvents (15)
        // ReleaseEvents (16)

        public VMCapabilities CapabilitiesNew() throws IOException {
            if(capabilities != null) return capabilities;
            int id = SendRequestPacket(1, 17);
            return capabilities = WaitForReply(id, (length, errorCode, input, bytes) -> {
                int caps = 0;
                for(int i = 0; i<32; i++) caps |= input.ReadByte() << i;
                return new VMCapabilities(caps);
            });
        }

        // RedefineClasses (18)
        // SetDefaultStratum (19)
        // AllClassesWithGeneric (20)
        // InstanceCounts (21)

        public ArrayList<TypeId> AllModules() throws IOException {
            int id = SendRequestPacket(1, 22);
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int threads = input.ReadInt();
                ArrayList<TypeId> threadIds = new ArrayList<>(threads);
                for(int i = 0; i<threads; i++) {
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes());
                    threadIds.add(typeId);
                }
                return threadIds;
            });
        }

    }

    public class ReferenceType {

        public String Signature(TypeId clazz) throws IOException {
            int id = StartRequestPacket(2, 1);
            WriteTypeId(clazz);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

        public TypeId ClassLoader(TypeId refType) throws IOException {
            int id = StartRequestPacket(2, 2);
            WriteTypeId(refType);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes()));
        }

        public int Modifiers(TypeId refType) throws IOException {
            int id = StartRequestPacket(2, 3);
            WriteTypeId(refType);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadInt());
        }

        public ArrayList<VMFieldInfoData> Fields(TypeId referenceId) throws IOException {
            int id = StartRequestPacket(2, 4);
            WriteTypeId(referenceId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int fields = input.ReadInt();
                ArrayList<VMFieldInfoData> fieldList = new ArrayList<>(fields);
                for(int i = 0; i<fields; i++) {
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes());
                    String name = input.ReadString();
                    String signature = input.ReadString();
                    int modBits = input.ReadInt();
                    fieldList.add(new VMFieldInfoData(typeId, name, signature, modBits));
                }
                return fieldList;
            });
        }

        public ArrayList<VMMethodInfoData> Methods(TypeId referenceId) throws IOException {
            int id = StartRequestPacket(2, 5);
            WriteTypeId(referenceId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int methods = input.ReadInt();
                ArrayList<VMMethodInfoData> methodList = new ArrayList<>(methods);
                for(int i = 0; i<methods; i++) {
                    TypeId typeId = input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes());
                    String name = input.ReadString();
                    String signature = input.ReadString();
                    int modBits = input.ReadInt();
                    methodList.add(new VMMethodInfoData(typeId, name, signature, modBits));
                }
                return methodList;
            });
        }

        // GetValues (6)

        public String SourceFile(TypeId refType) throws IOException {
            int id = StartRequestPacket(2, 7);
            WriteTypeId(refType);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

        // NestedTypes (8)
        // Status (9)

        public ArrayList<TypeId> Interfaces(TypeId clazz) throws IOException {
            int id = StartRequestPacket(2, 10);
            WriteTypeId(clazz);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                int count = input.ReadInt();
                ArrayList<TypeId> interfaces = new ArrayList<>(count);
                for(int i = 0; i<count; i++) {
                    interfaces.add(input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes()));
                }
                return interfaces;
            });
        }

        public TypeId ClassObject(TypeId refType) throws IOException {
            int id = StartRequestPacket(2, 11);
            WriteTypeId(refType);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes()));
        }

        // SourceDebugExtension (12)
        // SignatureWithGeneric (13)
        // FieldsWithGeneric (14)
        // MethodsWithGeneric (15)
        // Instances (16)

        public VMClassVersionInfo ClassFileVersion(TypeId refType) throws IOException {
            int id = StartRequestPacket(2, 17);
            WriteTypeId(refType);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> new VMClassVersionInfo(input.ReadInt(), input.ReadInt()));
        }

        // ConstantPool (18)
        // Module (19)

    }

    public class ClassType {

        public TypeId SuperClass(TypeId clazz) throws IOException {
            int id = StartRequestPacket(3, 1);
            WriteTypeId(clazz);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes()));
        }

        public void SetValues(TypeId classId, List<FieldUpdate> fieldUpdates) throws IOException {
            int id = StartRequestPacket(3, 2);
            WriteTypeId(classId);
            WriteInt(fieldUpdates.size());
            for(FieldUpdate update : fieldUpdates) {
                WriteTypeId(update.fieldId());
                update.val().write(Debugger.this);
            }
            FinishPacket();
            WaitForReply(id, (length, errorCode, input, bytes) -> null);
        }

        // InvokeMethod (3)
        // NewInstance (4)

    }

    public class ArrayType {

        public TypeId NewInstance(TypeId arrayType, int arrayLength) throws IOException {
            int id = StartRequestPacket(4, 1);
            WriteTypeId(arrayType);
            WriteInt(arrayLength);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> {
                if(input.ReadByte() != '[') System.err.println("Array tag required");
                return input.ReadTypeId(TypeIdTypes.OBJECT_ID, VirtualMachine.IdSizes());
            });
        }

    }

    public class InterfaceType {

        // InvokeMethod (1)

    }

    public class Method {

        // LineTable (1)
        // VariableTable (2)
        // Bytecodes (3)
        // IsObsolete (4)
        // VariableTableWithGeneric (5)

    }

    public class Field {

    }

    public class ObjectReference {

        // ReferenceType (1)
        // GetValues (2)
        // SetValues (3)
        // MonitorInfo (5)
        // InvokeMethod (6)
        // DisableCollection (7)
        // EnableCollection (8)
        // IsCollected (9)
        // ReferringObjects (10)

    }

    public class StringReference {

        public String Value(TypeId stringObject) throws IOException {
            int id = StartRequestPacket(10, 1);
            WriteTypeId(stringObject);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

    }

    public class ThreadReference {

        public String Name(TypeId threadId) throws IOException {
            int id = StartRequestPacket(11, 1);
            WriteTypeId(threadId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

        // Suspend (2)
        // Resume (3)
        // Status (4)
        // ThreadGroup (5)
        // Frames (6)
        // FrameCount (7)
        // OwnedMonitors (8)
        // CurrentContendedMonitor (9)
        // Step (10)
        // Interrupt (11)
        // SuspendCount (12)
        // OwnedMonitorsStackDepthInfo (13)
        // ForceEarlyReturn (14)

    }

    public class ThreadGroupReference {

        public String Name(TypeId groupId) throws IOException {
            int id = StartRequestPacket(12, 1);
            WriteTypeId(groupId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

        // Parent (2)
        // Children (3)

    }

    public class ArrayReference {

        // Length (1)
        // GetValues (2)
        // SetValues (3)

    }

    public class ClassLoaderReference {

        // VisibleClasses (1)

    }

    public class EventRequest {

        // Set (1)
        // Clear (2)
        // ClearAllBreakpoints (3)

    }

    public class StackFrame {

        // GetValues (1)
        // SetValues (2)
        // ThisObject (3)
        // PopFrames (4)

    }

    public class ClassObjectReference {

        public VMReflectedType ReflectedType(TypeId classObjectId) throws IOException {
            int id = StartRequestPacket(17, 1);
            WriteTypeId(classObjectId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> new VMReflectedType(RefType.getReferenceType(input.ReadByte()), input.ReadTypeId(TypeIdTypes.REFERENCE_TYPE_ID, VirtualMachine.IdSizes())));
        }

    }

    public class ModuleReference {

        public String Name(TypeId threadId) throws IOException {
            int id = StartRequestPacket(18, 1);
            WriteTypeId(threadId);
            FinishPacket();
            return WaitForReply(id, (length, errorCode, input, bytes) -> input.ReadString());
        }

        // ClassLoader (2)

    }

}
