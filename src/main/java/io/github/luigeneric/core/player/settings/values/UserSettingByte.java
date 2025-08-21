package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

public class UserSettingByte extends UserSettingValue<Byte>
{
    public UserSettingByte(final byte value)
    {
        super(value, UserSettingValueType.Byte);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }

}
