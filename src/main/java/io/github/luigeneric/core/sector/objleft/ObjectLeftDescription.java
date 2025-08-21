package io.github.luigeneric.core.sector.objleft;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import lombok.Getter;

@Getter
public abstract class ObjectLeftDescription implements IProtocolWrite
{
    protected final SpaceObject removedSpaceObject;
    protected final long removedSpaceObjectObjId;
    protected final RemovingCause removingCause;

    protected ObjectLeftDescription(final SpaceObject removedSpaceObject, final RemovingCause removingCause)
    {
        this.removedSpaceObject = removedSpaceObject;
        this.removedSpaceObjectObjId = removedSpaceObject.getObjectID();
        this.removingCause = removingCause;
        removedSpaceObject.setRemovingCause(removingCause);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(this.removedSpaceObjectObjId);
        bw.writeInt32(0); //readTick but will never be used
        bw.writeByte(this.removingCause.byteValue);
    }

    @Override
    public String toString()
    {
        return "ObjectLeftDescription{" +
                "removedSpaceObject=" + removedSpaceObject +
                ", removingCause=" + removingCause +
                '}';
    }
}
