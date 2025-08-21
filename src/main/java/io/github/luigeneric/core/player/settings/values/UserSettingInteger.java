package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

public class UserSettingInteger extends UserSettingValue<Integer>
{
    public UserSettingInteger(final int value)
    {
        super(value, UserSettingValueType.Integer);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeInt32(this.value);
    }
}
