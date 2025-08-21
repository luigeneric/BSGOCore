package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class MovementCard extends Card
{
    public final float minYawSpeed;
    public final float maxPitch;
    public final float maxRoll;
    public final float pitchFading;
    public final float yawFading;
    public final float rollFading;

    /**
     * Viper Standard value..
     * @param cardGuid the cardGuid connected to the Ship (the same as for the ship)
     */
    public MovementCard(long cardGuid)
    {
        this(cardGuid,0.1f, 360f, 80f, 2f, 2f, 400f);
    }

    public MovementCard(long cardGuid, float minYawSpeed, float maxPitch, float maxRoll,
                        float pitchFading, float yawFading, float rollFading)
    {
        super(cardGuid, CardView.Movement);
        this.minYawSpeed = minYawSpeed;
        this.maxPitch = maxPitch;
        this.maxRoll = maxRoll;
        this.pitchFading = pitchFading;
        this.yawFading = yawFading;
        this.rollFading = rollFading;
    }
    public static MovementCard defaultTestCard()
    {
        return new MovementCard(
                0,
                3,
                55,
                50,
                0.3f,
                0.3f,
                0.6f
        );
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(minYawSpeed);
        bw.writeSingle(maxPitch);
        bw.writeSingle(maxRoll);
        bw.writeSingle(pitchFading);
        bw.writeSingle(yawFading);
        bw.writeSingle(rollFading);
    }

    @Override
    public String toString()
    {
        return "MovementCard{" +
                "minYawSpeed=" + minYawSpeed +
                ", maxPitch=" + maxPitch +
                ", maxRoll=" + maxRoll +
                ", pitchFading=" + pitchFading +
                ", yawFading=" + yawFading +
                ", rollFading=" + rollFading +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }
}
