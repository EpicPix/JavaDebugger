package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.TypeId;

import java.io.IOException;

public class TaggedValue extends UntaggedValue {

    public TaggedValue(byte num) {super(num);}
    public TaggedValue(short num) {super(num);}
    public TaggedValue(int num) {super(num);}
    public TaggedValue(long num) {super(num);}
    public TaggedValue(TypeId type) {super(type);}

    public void write(IReadWrite write) throws IOException {
        if(val instanceof Byte) write.WriteByte((byte) 'B');
        else if(val instanceof Short) write.WriteByte((byte) 'S');
        else if(val instanceof Integer) write.WriteByte((byte) 'I');
        else if(val instanceof Long) write.WriteByte((byte) 'J');
        else if(val instanceof TypeId) write.WriteByte((byte) 'L');
        super.write(write);
    }

    public int size() {
        return super.size() + 1;
    }
}
