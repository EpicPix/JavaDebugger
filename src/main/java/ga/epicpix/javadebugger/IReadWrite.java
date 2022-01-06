package ga.epicpix.javadebugger;

import ga.epicpix.javadebugger.typeid.*;

import java.io.IOException;
import java.io.UTFDataFormatException;

public interface IReadWrite {

    void WriteBytes(byte[] b, int off, int len) throws IOException;
    default void WriteBytes(byte[] b) throws IOException {WriteBytes(b, 0, b.length);}
    void WriteByte(byte b) throws IOException;
    void WriteShort(short s) throws IOException;
    void WriteInt(int i) throws IOException;
    void WriteLong(long l) throws IOException;

    default void WriteTypeId(TypeId id) throws IOException {
        id.write(this);
    }

    default void WriteString(String str) throws IOException {
        WriteString(str, this);
    }

    byte[] ReadBytes(int bufSize) throws IOException;
    byte ReadByte() throws IOException;
    short ReadShort() throws IOException;
    int ReadInt() throws IOException;
    long ReadLong() throws IOException;

    default TypeId ReadTypeId(TypeIdTypes type, VMIdSizes sizes) throws IOException {
        int size = sizes.Size(type);
        TypeId typeId = switch (size) {
            case 1 -> new ByteTypeId();
            case 2 -> new ShortTypeId();
            case 4 -> new IntegerTypeId();
            case 8 -> new LongTypeId();
            default -> throw new RuntimeException("Unsupported TypeId size: " + size);
        };
        typeId.read(this);
        return typeId;
    }

    default String ReadString() throws IOException {
        return ReadString(this);
    }

    public static void WriteString(String str, IReadWrite readWrite) throws IOException {
        final int strlen = str.length();
        int utflen = strlen; // optimized for ASCII

        for (int i = 0; i < strlen; i++) {
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0)
                utflen += (c >= 0x800) ? 2 : 1;
        }

        if (utflen > 65535 || /* overflow */ utflen < strlen) throw new UTFDataFormatException("Too long string");

        final byte[] bytearr = new byte[utflen + 4];

        int count = 0;
        bytearr[count++] = (byte) ((utflen >>> 24) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 16) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i = 0;
        for (i = 0; i < strlen; i++) { // optimized for initial run of ASCII
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0) break;
            bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            int c = str.charAt(i);
            if (c < 0x80 && c != 0) {
                bytearr[count++] = (byte) c;
            } else if (c >= 0x800) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        readWrite.WriteBytes(bytearr, 0, utflen + 4);
    }

    public static String ReadString(IReadWrite readWrite) throws IOException {
        int utflen = readWrite.ReadInt();
        byte[] bytearr = readWrite.ReadBytes(utflen);
        char[] chararr = new char[bytearr.length];

        int c, char2, char3;
        int count = 0;
        int chararr_count=0;



        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++]=(char)c;
                }
                case 12, 13 -> {
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException("malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                }
                case 14 -> {
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = bytearr[count-2];
                    char3 = bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException("malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6)  |
                            ((char3 & 0x3F) << 0));
                }
                default ->
                        /* 10xx xxxx,  1111 xxxx */
                        throw new UTFDataFormatException("malformed input around byte " + count);
            }
        }
        return new String(chararr, 0, chararr_count);
    }

}
