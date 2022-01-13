package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

import java.io.IOException;

public class UntaggedValue {

    protected final Object val;

    public UntaggedValue(byte num) {val = num;}
    public UntaggedValue(short num) {val = num;}
    public UntaggedValue(int num) {val = num;}
    public UntaggedValue(long num) {val = num;}
    public UntaggedValue(TypeId type) {val = type;}

    public void write(IReadWrite write) throws IOException {
        if(val instanceof Byte b) write.WriteByte(b);
        else if(val instanceof Short s) write.WriteShort(s);
        else if(val instanceof Integer i) write.WriteInt(i);
        else if(val instanceof Long l) write.WriteLong(l);
        else if(val instanceof TypeId id) write.WriteTypeId(id);
    }

    public int size() {
        if(val instanceof Byte) return 1;
        else if(val instanceof Short) return 2;
        else if(val instanceof Integer) return 4;
        else if(val instanceof Long) return 8;
        else if(val instanceof TypeId id) return id.size();
        return 0;
    }

}
