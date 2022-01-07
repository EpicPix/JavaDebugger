package ga.epicpix.javadebugger;

import java.util.EnumSet;

public enum AccessFlags {

    PUBLIC,
    PRIVATE,
    PROTECTED,
    STATIC,
    FINAL,
    SYNCHRONIZED,
    BRIDGE,
    VARARGS,
    NATIVE,
    ABSTRACT,
    STRICT,
    SYNTHETIC,
    ;

    public static EnumSet<AccessFlags> getMethodAccessFlags(int flag) {
        EnumSet<AccessFlags> flags = EnumSet.noneOf(AccessFlags.class);
        if((flag & 0x0001) != 0) flags.add(PUBLIC);
        if((flag & 0x0002) != 0) flags.add(PRIVATE);
        if((flag & 0x0004) != 0) flags.add(PROTECTED);
        if((flag & 0x0008) != 0) flags.add(STATIC);
        if((flag & 0x0010) != 0) flags.add(FINAL);
        if((flag & 0x0020) != 0) flags.add(SYNCHRONIZED);
        if((flag & 0x0040) != 0) flags.add(BRIDGE);
        if((flag & 0x0080) != 0) flags.add(VARARGS);
        if((flag & 0x0100) != 0) flags.add(NATIVE);
        if((flag & 0x0400) != 0) flags.add(ABSTRACT);
        if((flag & 0x0800) != 0) flags.add(STRICT);
        if((flag & 0xf0000000) == 0xf0000000 || (flag & 0x1000) == 0x1000) flags.add(SYNTHETIC);
        return flags;
    }

}
