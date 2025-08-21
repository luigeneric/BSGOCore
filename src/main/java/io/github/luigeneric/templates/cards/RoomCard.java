package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.Arrays;

public class RoomCard extends Card
{
    private final RoomDoor[] doors;
    @SerializedName("NPCs")
    private final RoomNpc[] npcs;
    private final String music;

    public RoomCard(long cardGUID, CardView view, String music, RoomDoor[] doors, RoomNpc[] npcs)
    {
        super(cardGUID, view);
        this.music = music;
        this.doors = doors;
        this.npcs = npcs;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDescArray(doors);
        bw.writeDescArray(npcs);
        bw.writeString(this.music);
    }

    public static class RoomDoor implements IProtocolWrite
    {
        @SerializedName("Door")
        private final String door;
        @SerializedName("roomGUID")
        private final long roomGuid;

        public RoomDoor(final String door, final long roomGuid)
        {
            this.door = door;
            this.roomGuid = roomGuid;
        }

        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeString(door);
            bw.writeUInt32(roomGuid);
        }
    }
    public static class RoomNpc implements IProtocolWrite
    {
        @SerializedName("NPC")
        private final String npc;
        @SerializedName("NPCGUID")
        private final long npcGuid;

        public RoomNpc(final String npc, final long npcGuid)
        {
            this.npc = npc;
            this.npcGuid = npcGuid;
        }
        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeString(npc);
            bw.writeUInt32(npcGuid);
        }
    }

    @Override
    public String toString()
    {
        return "RoomCard{" +
                "doors=" + Arrays.toString(doors) +
                ", npcs=" + Arrays.toString(npcs) +
                ", music='" + music + '\'' +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }
}
