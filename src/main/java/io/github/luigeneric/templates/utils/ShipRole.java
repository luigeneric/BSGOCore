package io.github.luigeneric.templates.utils;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public enum ShipRole implements IProtocolWrite
{
    Fighter(1),
    Bomber(2),
    Command(3),
    ElectronicWarfare(4),
    Engineer(5),
    Interceptor(6),
    Gunship(7),
    Picket(8),
    Destroyer(9),
    Artillery(10),
    Assault(11),
    Stealth(12),
    Carrier(13),
    Mothership(14);

    public final byte value;

    ShipRole(int value)
    {
        this.value = (byte) value;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }
}
