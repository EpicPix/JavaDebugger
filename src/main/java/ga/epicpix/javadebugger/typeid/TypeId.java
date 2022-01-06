package ga.epicpix.javadebugger.typeid;

import ga.epicpix.javadebugger.IReadWrite;

import java.io.IOException;

public abstract sealed class TypeId permits ByteTypeId, ShortTypeId, IntegerTypeId, LongTypeId {

    public abstract void write(IReadWrite out) throws IOException;
    public abstract void read(IReadWrite in) throws IOException;
    public abstract String toString();

}
