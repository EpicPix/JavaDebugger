package ga.epicpix.javadebugger.typeid;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
        if(reader.canRead() && reader.read() == '0') {
            if(reader.canRead() && reader.read() == 'x') {
                String str = reader.readUnquotedString();
                long i = Long.parseLong(str, 16);
                return switch(size) {
                    case 1 -> new ByteTypeId((byte) i);
                    case 2 -> new ShortTypeId((short) i);
                    case 4 -> new IntegerTypeId((int) i);
                    case 8 -> new LongTypeId(i);
                    default -> throw new CommandSyntaxException(new CommandExceptionType() {}, new LiteralMessage("Invalid Id Size"));
                };
            }
        }
        throw new CommandSyntaxException(new CommandExceptionType() {}, new LiteralMessage("Expected 0x"));
    }

}
