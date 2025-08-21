package io.github.luigeneric.binaryreaderwriter;


import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.utils.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Objects;

public class BgoProtocolWriter extends CustomFastByteArrayOutputStream
{
    public BgoProtocolWriter()
    {
        super();
        this.writeUInt16(0);
    }

    public BgoProtocolWriter writeByte(final byte b)
    {
        this.write(b);
        return this;
    }

    public BgoProtocolWriter writeSingle(final float f)
    {
        return this.writeInt32(Float.floatToRawIntBits(f));
    }

    public void writeDouble(final double d)
    {
        // prob. better
        //this.writeInt64(Double.doubleToRawLongBits(d));

        final ByteBuffer bb = allocateLittleEndianByteBuffer(8);
        bb.putDouble(d);
        writeByteBuffer(bb);
    }

    public BgoProtocolWriter writeInt16(final short s)
    {
        final byte[] writeBuffer = new byte[2];
        writeBuffer[0] = (byte) (s);
        writeBuffer[1] = (byte) (s >>> 8);
        this.write(writeBuffer);
        return this;
    }

    public final BgoProtocolWriter writeUInt16(final int i) throws IllegalArgumentException
    {
        if (i < 0 || i > 65_535) throw new IllegalArgumentException("number was negative or too big: " + i);

        return this.writeInt16((short) i);
    }

    public final void writeLength(final int i) throws IllegalArgumentException
    {
        this.writeUInt16(i);
    }

    public final BgoProtocolWriter writeMsgType(final int i) throws IllegalArgumentException
    {
        return this.writeUInt16(i);
    }



    public BgoProtocolWriter writeInt32(final int i)
    {
        final byte[] writeBuffer = new byte[4];

        writeBuffer[0] = (byte) (i);
        writeBuffer[1] = (byte) (i >>> 8);
        writeBuffer[2] = (byte) (i >>> 16);
        writeBuffer[3] = (byte) (i >>> 24);

        this.write(writeBuffer);

        return this;
    }

    public BgoProtocolWriter writeGUID(final long l) throws IllegalArgumentException
    {
        return writeUInt32(l);
    }

    public BgoProtocolWriter writeUInt32(final long l) throws IllegalArgumentException
    {
        this.writeInt32((int) l);
        return this;
    }

    public void writeUint16Collection(final int ...nums)
    {
        this.writeLength(nums.length);
        for (int num : nums)
        {
            this.writeUInt16(num);
        }
    }

    public void writeUint16Collection(final Collection<Integer> lst)
    {
        final int toWrite = lst.size();
        this.ensureDeltaCapacity(toWrite * 2 + 2); // uint16(for size) + uint16 * size
        this.writeLength(toWrite);
        for (final int integer : lst)
        {
            this.writeUInt16(integer);
        }
    }

    public BgoProtocolWriter writeUInt32Collection(final Collection<Long> ids)
    {
        final int toWrite = ids.size();
        this.ensureDeltaCapacity(2 + toWrite * 4); // uint16(for size)+ uint32 * size
        this.writeLength(toWrite);
        for (final long aLong : ids)
        {
            this.writeUInt32(aLong);
        }

        return this;
    }

    public void writeInt32Collection(final Collection<Integer> integers)
    {
        final int toWrite = integers.size();
        this.ensureDeltaCapacity(2 + toWrite * 4); // uint16(for size)+ uint32 * size
        writeLength(toWrite);
        for (final int integer : integers)
        {
            writeInt32(integer);
        }
    }

    public void writeUint32Array(final long[] longs)
    {
        final int toWrite = longs.length;
        this.ensureDeltaCapacity(2 + toWrite * 4); // uint16(for size)+ uint32 * size
        this.writeLength(toWrite);
        for (final long l : longs)
        {
            this.writeUInt32(l);
        }
    }

    public BgoProtocolWriter writeInt64(final long l)
    {
        final ByteBuffer bb = allocateLittleEndianByteBuffer(8);
        bb.putLong(l);
        writeByteBuffer(bb);
        return this;
    }

    public void writeUInt64(final long l) throws IllegalArgumentException
    {
        if (Long.compareUnsigned(l, 0) < 0) throw new IllegalArgumentException("value was less 0: " + l);
        this.writeInt64(l);
    }

    public BgoProtocolWriter writeBoolean(final boolean b)
    {
        final byte b1 = (byte) (b ? 1 : 0);
        return writeByte(b1);
    }

    public BgoProtocolWriter writeString(final String s) throws NullPointerException
    {
        Objects.requireNonNull(s, "string can not be null!");
        final int length = s.length();
        this.writeUInt16(length);
        if (length > 0)
        {
            this.write(s.getBytes());
        }
        return this;
    }

    public void writeStringArray(final String[] strArr) throws NullPointerException
    {
        Objects.requireNonNull(strArr, "strArr can not be null!");

        this.writeUInt16(strArr.length);
        for (final String str : strArr)
        {
            this.writeString(str);
        }
    }

    public BgoProtocolWriter writeDesc(final IProtocolWrite desc)
    {
        desc.write(this);
        return this;
    }

    public void writeDescArray(final IProtocolWrite[] protocolWrites)
    {
        final int toWrite = protocolWrites.length;
        this.writeLength(toWrite);
        for (IProtocolWrite protocolWrite : protocolWrites)
        {
            writeDesc(protocolWrite);
        }
    }

    @SafeVarargs
    public final <T extends IProtocolWrite> void writeDescCollection(final T... writables)
    {
        this.writeUInt16(writables.length);
        for (final T writeable : writables)
        {
            this.writeDesc(writeable);
        }
    }

    public <T extends IProtocolWrite> BgoProtocolWriter writeDescCollection(final Collection<T> protocolWrites)
    {
        final int toWrite = protocolWrites.size();
        this.writeLength(toWrite);
        for (final T t : protocolWrites)
        {
            this.writeDesc(t);
        }

        return this;
    }


    private void writeByteBuffer(final ByteBuffer bb)
    {
        this.write(bb.array());
    }

    public void writeColor(final Color color)
    {
        final byte r = (byte) (255f * color.r());
        final byte g = (byte) (255f * color.g());
        final byte b = (byte) (255f * color.b());
        final byte a = (byte) (255f * color.a());
        this.write(r, g, b, a);
    }

    public void writeVector3(final Vector3 position)
    {
        ensureDeltaCapacity(3 * 4);
        this.writeSingle(position.getX());
        this.writeSingle(position.getY());
        this.writeSingle(position.getZ());
    }

    public void writeVector2(final Vector2 vector2)
    {
        ensureDeltaCapacity(8);
        writeSingle(vector2.getX());
        writeSingle(vector2.getY());
    }

    public void writeEuler3(final Euler3 euler3)
    {
        ensureDeltaCapacity(12);

        this.writeSingle(euler3.pitch());
        this.writeSingle(euler3.yaw());
        this.writeSingle(euler3.getRoll());
    }

    public void writeQuaternion(final Quaternion rotation)
    {
        ensureDeltaCapacity(16);
        this.writeSingle(rotation.x());
        this.writeSingle(rotation.y());
        this.writeSingle(rotation.z());
        this.writeSingle(rotation.w());
    }

    protected static ByteBuffer allocateLittleEndianByteBuffer(final int capacity) throws IllegalArgumentException
    {
        final ByteBuffer bb = ByteBuffer.allocate(capacity);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb;
    }


    public void writeDateTime(final LocalDateTime received)
    {
        this.writeUInt32(received.toEpochSecond(ZoneOffset.UTC));
    }

    public void writeLongDateTime(final LocalDateTime localDateTime)
    {
        this.writeUInt64(localDateTime.toEpochSecond(ZoneOffset.UTC));
    }

    public void writeLongDateTime(final long systemCurrentTimeMillis)
    {
        this.writeLongDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(systemCurrentTimeMillis), ZoneOffset.UTC));
    }
}