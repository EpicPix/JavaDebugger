package ga.epicpix.javadebugger;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface ReplyInfo<T> {
    public T handle(int length, ErrorCodes errorCode, IReadWrite input, byte[] bytes) throws IOException;
}
