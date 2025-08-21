package io.github.luigeneric.templates.utils;

public enum TargetBracketMode
{
    Default,
    AllEnemy;

    public byte getValue(){
        return (byte) this.ordinal();
    }
}