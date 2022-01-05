package ga.epicpix.javadebugger;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface ReplyInfo<T> {
    public T handle(int length, ErrorCodes errorCode, DataInput input) throws IOException;
}
