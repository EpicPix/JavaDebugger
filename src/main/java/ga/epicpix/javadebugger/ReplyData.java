package ga.epicpix.javadebugger;

import java.io.DataInput;

public class ReplyData {

    public int length;
    public int id;
    public int flags;
    public ErrorCodes errorCode;
    public DataInput input;
    public byte[] bytes;

}
