package io.github.luigeneric.core.movement;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public abstract class Maneuver implements IProtocolWrite, Comparable<Maneuver>
{
    protected Tick startTick;
    protected final ManeuverType maneuverType;
    private MovementOptions movementOptions;


    public Maneuver(final ManeuverType maneuverType)
    {
        this.maneuverType = maneuverType;
        this.movementOptions = new MovementOptions();
    }

    /**
     * Writes the ManeuverType and StartTick into ProtocolBuffer
     * @param bw
     */
    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.ensureDeltaCapacity(5);
        bw.writeByte(this.maneuverType.getValue()); //every maneuver has first type
        bw.writeInt32(this.startTick.getValue()); //every maneuver has first startTick
    }

    public abstract MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt);

    @Override
    public int compareTo(final Maneuver other)
    {
        int num = this.startTick.compareTo(other.startTick);
        if (num == 0)
        {
            if (this.maneuverType.getValue() < other.maneuverType.getValue())
            {
                num = -1;
            }
            else if (this.maneuverType.getValue() > other.maneuverType.getValue())
            {
                num = 1;
            }
        }
        return num;
    }

    /**
     * Creates first new MovementFrame based on the old one and the current MovementOptions to direct the object to the given targetEuler3
     * @param prevFrame the previous MovementFrame, won't change the values inside the old MovementFrame!
     * @param direction the target to direct to in euler angles
     * @param dt the time passed, not used as of now
     * @return first new MovementFrame containing the new movement information
     */
    protected MovementFrame moveToDirection(final MovementFrame prevFrame, final Euler3 direction, final float dt)
    {
        if (!prevFrame.isValid())
        {
            return MovementFrame.invalid();
        }
        //var moveToDirectionTestFrame = Simulation.moveToDirection2(prevFrame, direction, this.movementOptions, dt);
        var moveToDirectionFrameOld = MovementSimulation.moveToDirection(prevFrame, direction, this.movementOptions, dt);

        /*
        if (!moveToDirectionFrameOld.equals(moveToDirectionTestFrame))
        {
            log.error("Frames are not equal");
        }
         */

        return moveToDirectionFrameOld;

        //return Simulation.moveToDirection(prevFrame, direction, this.movementOptions, dt);
    }

    /**
     * Advance drifting by the current Movement-stats
     * @param prevFrame won't be affected
     * @return first new MovementFrame
     */
    protected MovementFrame drift(final MovementFrame prevFrame)
    {
        if (!prevFrame.isValid())
        {
            return MovementFrame.invalid();
        }
        return MovementSimulation.wasd(prevFrame, 0, 0, movementOptions);
    }

    public Tick getStartTick()
    {
        return startTick;
    }

    public ManeuverType getManeuverType()
    {
        return maneuverType;
    }

    public MovementOptions getMovementOptions()
    {
        return movementOptions;
    }

    public void setStartTick(final Tick startTick) throws NullPointerException
    {
        this.startTick = Objects.requireNonNull(startTick);
    }

    public void setMovementOptions(final MovementOptions movementOptions)
    {
        this.movementOptions = movementOptions.copy();
    }
}
