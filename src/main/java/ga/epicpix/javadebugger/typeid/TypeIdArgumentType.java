package ga.epicpix.javadebugger.typeid;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ga.epicpix.javadebugger.VMIdSizes;

public class TypeIdArgumentType implements ArgumentType<TypeId> {

    private final int size;

    private TypeIdArgumentType(VMIdSizes sizes, TypeIdTypes type) {
        size = sizes.Size(type);
    }

    public static TypeIdArgumentType typeId(VMIdSizes sizes, TypeIdTypes type) {
        return new TypeIdArgumentType(sizes, type);
    }

    public TypeId parse(StringReader reader) throws CommandSyntaxException {
        if(reader.canRead()) {
            if(reader.peek() == '0') {
                reader.read();
                if (reader.canRead() && reader.read() == 'x') {
                    String str = reader.readUnquotedString();
                    if (str.isEmpty())
                        throw new SimpleCommandExceptionType(new LiteralMessage("Expected hex number")).createWithContext(reader);
                    long i = Long.parseLong(str, 16);
                    return switch (size) {
                        case 1 -> new ByteTypeId((byte) i);
                        case 2 -> new ShortTypeId((short) i);
                        case 4 -> new IntegerTypeId((int) i);
                        case 8 -> new LongTypeId(i);
                        default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid Id Size")).createWithContext(reader);
                    };
                }
            }else if(reader.readUnquotedString().equals("null")) {
                return switch (size) {
                    case 1 -> new ByteTypeId((byte) 0);
                    case 2 -> new ShortTypeId((short) 0);
                    case 4 -> new IntegerTypeId(0);
                    case 8 -> new LongTypeId(0);
                    default -> throw new SimpleCommandExceptionType(new LiteralMessage("Invalid Id Size")).createWithContext(reader);
                };
            }
        }
        throw new SimpleCommandExceptionType(new LiteralMessage("Expected 0x or null")).createWithContext(reader);
    }

}
