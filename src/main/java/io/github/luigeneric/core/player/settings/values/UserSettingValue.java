package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

public abstract class UserSettingValue<T> implements IProtocolWrite
{
    protected final T value;
    protected final UserSettingValueType type;

    public UserSettingValue(final T value, final UserSettingValueType type)
    {
        this.value = value;
        this.type = type;
    }

    public UserSettingValueType getType()
    {
        return this.type;
    }

    public T getValue()
    {
        return value;
    }
}
