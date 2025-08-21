package io.github.luigeneric.binaryreaderwriter;

import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * A specialized reader for parsing protocol-specific data from a byte array.
 * Inherits from {@link FastByteArrayInputStream} for optimized byte array input handling.
 *
 */
public class BgoProtocolReader extends FastByteArrayInputStream
{
    public BgoProtocolReader(final byte[] buf)
    {
        super(buf);
    }

    public int getSize()
    {
        return this.count;
    }

    public static int readBufferSize(final byte[] data)
    {
        int num = Byte.toUnsignedInt(data[0]);
        int num2 = Byte.toUnsignedInt(data[1]);
        return (num << 8 | num2);
    }

    public boolean canRead()
    {
        return this.available() > 0;
    }

    public float readSingle() throws IOException
    {
        return Float.intBitsToFloat(this.readInt32());
    }

    public double readDouble() throws IOException
    {
        return ByteBuffer.wrap(this.readNBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    public byte readByte() throws IOException
    {
        final int rv = this.read();
        return (byte) rv;
    }

    public long[] readUint32Array() throws IOException
    {
        final int toRead = this.readLength();
        final long[] arr = new long[toRead];
        for (int i = 0; i < toRead; i++)
        {
            arr[i] = this.readUint32();
        }
        return arr;
    }

    public boolean readBoolean() throws IOException
    {
        return this.readByte() != 0;
    }

    public short readInt16() throws IOException
    {
        final byte[] bytes = this.readNBytes(2);
        //short res1 = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort(); //seems to be the same as below

        //return (short)(((bytes[0]&0xff) << 0) + ((bytes[1]&0xff) << 8));
        return (short) (((bytes[0] & 0xff)) + ((bytes[1] & 0xff) << 8));
    }

    public int readUint16() throws IOException
    {
        return Short.toUnsignedInt(this.readInt16());
    }

    public int readLength() throws IOException
    {
        return this.readUint16();
    }

    public int readInt32() throws IOException
    {
        final byte[] bytes = this.readNBytes(4);
        //final int correct = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return ((bytes[0] & 0xff)) + ((bytes[1] & 0xff) << 8) + ((bytes[2] & 0xff) << 16) + ((bytes[3] & 0xff) << 24);
    }

    public long readUint32() throws IOException, IllegalStateException
    {
        final int erg = this.readInt32();
        return Integer.toUnsignedLong(erg);
    }

    public long readGUID() throws IOException
    {
        return this.readUint32();
    }

    public long readInt64() throws IOException
    {
        return ByteBuffer.wrap(this.readNBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public String readUint64() throws IOException
    {
        return Long.toUnsignedString(readInt64());
    }

    public String readString() throws IOException
    {
        final int numToRead = this.readLength();
        return numToRead > 0 ? new String(this.readNBytes(numToRead), StandardCharsets.UTF_8) : "";
    }

    public <T extends IProtocolRead> T readDesc(Class<T> type) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        final T result = type.getDeclaredConstructor().newInstance();
        result.read(this);

        return result;
    }


    public Euler3 readEuler3() throws IOException
    {
        return new Euler3(this.readSingle(), this.readSingle(), this.readSingle());
    }

    public Vector3 readVector3() throws IOException
    {
        return new Vector3(this.readSingle(), this.readSingle(), this.readSingle());
    }


    public Vector2 readVector2() throws IOException
    {
        return new Vector2(this.readSingle(), this.readSingle());
    }
}
