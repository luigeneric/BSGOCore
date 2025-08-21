package io.github.luigeneric.core.protocols.room;

enum ServerMessage
{
    Talk(1),
    NpcMarks(3);

    public final short value;

    ServerMessage(int value)
    {
        this.value = (short) value;
    }
}
