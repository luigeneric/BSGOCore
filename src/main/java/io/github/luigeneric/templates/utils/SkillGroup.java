package io.github.luigeneric.templates.utils;

public enum SkillGroup
{
    Computer(1),
    Engine(2),
    Hull(3),
    Weapon(4);

    public final byte value;
    SkillGroup(int value)
    {
        this.value = (byte) value;
    }

}
