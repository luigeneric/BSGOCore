package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

public class UserSettingFloat extends UserSettingValue<Float>
{
    public UserSettingFloat(final float value)
    {
        super(value, UserSettingValueType.Float);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeSingle(this.value);
    }
}
