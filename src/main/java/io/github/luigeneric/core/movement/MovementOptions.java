package io.github.luigeneric.core.movement;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.MovementCard;
import io.github.luigeneric.utils.ICopy;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MovementOptions implements IProtocolWrite, ICopy<MovementOptions>
{
    @Getter
    private Gear gear;
    @Getter
    private Gear lastGear;
    @Getter
    private float speed;
    @Setter
    @Getter
    private float throttleSpeed;
    @Setter
    @Getter
    private float acceleration;
    @Setter
    @Getter
    private float inertiaCompensation;
    @Setter
    @Getter
    private float pitchAcceleration;
    @Setter
    @Getter
    private float pitchMaxSpeed;
    @Setter
    @Getter
    private float yawAcceleration;
    @Setter
    @Getter
    private float yawMaxSpeed;
    @Setter
    @Getter
    private float rollAcceleration;
    @Setter
    @Getter
    private float rollMaxSpeed;
    @Setter
    @Getter
    private float strafeAcceleration;
    @Setter
    @Getter
    private float strafeMaxSpeed;
    @Setter
    @Getter
    private float minYawSpeed;
    @Setter
    @Getter
    private float maxPitch;
    @Setter
    @Getter
    private float maxRoll;
    @Setter
    @Getter
    private float pitchFading;
    @Setter
    @Getter
    private float yawFading;
    @Setter
    @Getter
    private float rollFading;
    private MovementCard movementCard;
    private IMovementUpdateSubscriber updateSubscriber;
    private final Lock lock;

    public MovementOptions(final Gear gear, final float speed, final float acceleration, final float inertiaCompensation,
                           final float pitchAcceleration, final float pitchMaxSpeed, final float yawAcceleration, final float yawMaxSpeed,
                           final float rollAcceleration, final float rollMaxSpeed, final float strafeAcceleration, final float strafeMaxSpeed,
                           final MovementCard movementCard)
    {
        this.lock = new ReentrantLock();
        this.gear = gear;
        this.speed = speed;
        this.acceleration = acceleration;
        this.inertiaCompensation = inertiaCompensation;
        this.pitchAcceleration = pitchAcceleration;
        this.pitchMaxSpeed = pitchMaxSpeed;
        this.yawAcceleration = yawAcceleration;
        this.yawMaxSpeed = yawMaxSpeed;
        this.rollAcceleration = rollAcceleration;
        this.rollMaxSpeed = rollMaxSpeed;
        this.strafeAcceleration = strafeAcceleration;
        this.strafeMaxSpeed = strafeMaxSpeed;
        this.throttleSpeed = 0;
        this.lastGear = Gear.None;
        if (movementCard != null)
        {
            this.applyCard(movementCard);
        }
    }

    protected void setUpdateSubscriber(final IMovementUpdateSubscriber subscriber)
    {
        this.updateSubscriber = subscriber;
    }

    public MovementOptions()
    {
        this(Gear.Regular, 0, 0, 0, 0 ,
                0,0, 0, 0, 0,
                0, 0, null);
    }

    /**
     * Copy-constructor
     * @param movementOptions to copy
     */
    private MovementOptions(final MovementOptions movementOptions)
    {
        Objects.requireNonNull(movementOptions);
        this.gear = movementOptions.gear;
        this.lastGear = movementOptions.lastGear;
        this.speed = movementOptions.speed;
        this.throttleSpeed = movementOptions.throttleSpeed;
        this.acceleration = movementOptions.acceleration;
        this.inertiaCompensation = movementOptions.inertiaCompensation;
        this.pitchAcceleration = movementOptions.pitchAcceleration;
        this.pitchMaxSpeed = movementOptions.pitchMaxSpeed;
        this.yawAcceleration = movementOptions.yawAcceleration;
        this.yawMaxSpeed = movementOptions.yawMaxSpeed;
        this.rollAcceleration = movementOptions.rollAcceleration;
        this.rollMaxSpeed = movementOptions.rollMaxSpeed;
        this.strafeAcceleration = movementOptions.strafeAcceleration;
        this.strafeMaxSpeed = movementOptions.strafeMaxSpeed;
        this.minYawSpeed = movementOptions.minYawSpeed;
        this.maxPitch = movementOptions.maxPitch;
        this.maxRoll = movementOptions.maxRoll;
        this.pitchFading = movementOptions.pitchFading;
        this.yawFading = movementOptions.yawFading;
        this.rollFading = movementOptions.rollFading;
        this.movementCard = movementOptions.movementCard;
        this.updateSubscriber = movementOptions.updateSubscriber;
        this.lock = new ReentrantLock();
    }

    @Override
    public MovementOptions copy()
    {
        return new MovementOptions(this);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(this.gear.getValue());
        bw.writeSingle(this.speed);
        bw.writeSingle(this.acceleration);
        bw.writeSingle(this.inertiaCompensation);
        bw.writeSingle(this.pitchAcceleration);
        bw.writeSingle(this.pitchMaxSpeed);
        bw.writeSingle(this.yawAcceleration);
        bw.writeSingle(this.yawMaxSpeed);
        bw.writeSingle(this.rollAcceleration);
        bw.writeSingle(this.rollMaxSpeed);
        bw.writeSingle(this.strafeAcceleration);
        bw.writeSingle(this.strafeMaxSpeed);
    }

    public void applyCard(final MovementCard card)
    {
        Objects.requireNonNull(card, "MovementCard cannot ben null");
        this.setMinYawSpeed(card.minYawSpeed * this.getYawMaxSpeed());
        this.setMaxPitch(card.maxPitch);
        this.setMaxRoll(card.maxRoll);
        this.setPitchFading(card.pitchFading);
        this.setYawFading(card.yawFading);
        this.setRollFading(card.rollFading);
    }



    public Euler3 maxTurnAcceleration()
    {
        return new Euler3(this.getPitchAcceleration(), this.getYawAcceleration(), this.getRollAcceleration());
    }
    public Euler3 maxTurnSpeed()
    {
        return new Euler3(this.getPitchMaxSpeed(), this.getYawMaxSpeed(), this.getRollMaxSpeed());
    }


    /**
     * from all min values creates a new Euler3
     * @param euler3 from euler to linear interpolate
     * @return a new Euler3 object
     */
    public Euler3 minEulerSpeed(final Euler3 euler3)
    {
        return new Euler3(this.minPitchSpeed(euler3.pitch()), this.minYawSpeed(euler3.getRoll(),
                euler3.pitch()), this.minRollSpeed(euler3.getRoll()));
    }

    /**
     * Max linear interpolation
     * @param euler3 to interpolate
     * @return a new Euler3 object
     */
    public Euler3 maxEulerSpeed(final Euler3 euler3)
    {
        return new Euler3(
                this.maxPitchSpeed(euler3.pitch()),
                this.maxYawSpeed(euler3.getRoll(), euler3.pitch()),
                this.maxRollSpeed(euler3.getRoll()));
    }

    public float minYawSpeed(final float roll, final float pitch)
    {
        float rollInfluence = -Mathf.clamp(Mathf.normalizeAngle(roll) / this.getMaxRoll(), -1f, 1f);
        float baseYawSpeed = Mathf.lerp(-this.getYawMaxSpeed(), -this.getMinYawSpeed(), (rollInfluence + 1f) / 2f);
        final float PITCH_INFLUENCE_DIVISOR = 90f;
        return baseYawSpeed * (1f + Mathf.abs(Mathf.normalizeAngle(pitch)) / PITCH_INFLUENCE_DIVISOR);
    }

    public float maxYawSpeed(final float roll, final float pitch)
    {
        float num = -Mathf.clamp(Mathf.normalizeAngle(roll) / this.getMaxRoll(), -1f, 1f);
        float num2 = Mathf.lerp(this.getMinYawSpeed(), this.getYawMaxSpeed(), (num + 1f) / 2f);
        return num2 * (1f + Mathf.abs(Mathf.normalizeAngle(pitch)) / 90f);
    }

    public float minPitchSpeed(final float pitch)
    {
        final float num = -this.getMaxPitch() * 0.7f;
        if (pitch < num)
        {
            return Mathf.lerp(-this.getPitchMaxSpeed(), 0f, Mathf.clamp01((num - pitch) / (this.getMaxPitch() * 0.3f)));
        }
        return -this.getPitchMaxSpeed();
    }

    public float maxPitchSpeed(final float pitch)
    {
        float num = maxPitch * 0.7f;
        if (pitch > num)
        {
            return Mathf.lerp(pitchMaxSpeed, 0f, Mathf.clamp01((0f - num + pitch) / (maxPitch * 0.3f)));
        }
        return pitchMaxSpeed;
    }

    public float minRollSpeed(final float roll)
    {
        float num = -this.getMaxRoll() * 0.5f;
        return (roll >= num) ? (-this.getRollMaxSpeed()) : Mathf.lerp(-this.getRollMaxSpeed(), 0f, Mathf.clamp01((num - roll) / (this.getMaxRoll() * 0.5f)));
    }

    public float maxRollSpeed(final float roll)
    {
        float num = this.getMaxRoll() * 0.5f;
        return (roll <= num) ? this.getRollMaxSpeed() : Mathf.lerp(this.getRollMaxSpeed(), 0f, Mathf.clamp01((-num + roll) / (this.getMaxRoll() * 0.5f)));
    }


    @Override
    public String toString()
    {
        return "MovementOptions{" +
                "gear=" + getGear() +
                ", speed=" + getSpeed() +
                ", acceleration=" + getAcceleration() +
                ", inertiaCompensation=" + getInertiaCompensation() +
                ", pitchAcceleration=" + getPitchAcceleration() +
                ", pitchMaxSpeed=" + getPitchMaxSpeed() +
                ", yawAcceleration=" + getYawAcceleration() +
                ", yawMaxSpeed=" + getYawMaxSpeed() +
                ", rollAcceleration=" + getRollAcceleration() +
                ", rollMaxSpeed=" + getRollMaxSpeed() +
                ", strafeAcceleration=" + getStrafeAcceleration() +
                ", strafeMaxSpeed=" + getStrafeMaxSpeed() +
                '}';
    }

    public void setGear(final Gear gear)
    {
        this.setLastGear(this.gear);
        this.gear = gear;
    }


    /**
     * Since this is used from all over the place, it's required to lock this
     * @param speed in total value
     */
    public void setSpeed(final float speed)
    {
        lock.lock();
        try
        {
            this.speed = speed;
            this.updateSubscriber.movementUpdate();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setSpeedAndThrottle(final float speed)
    {
        this.setThrottleSpeed(speed);
        this.setSpeed(speed);
    }

    private void setLastGear(final Gear lastGear)
    {
        if (lastGear == Gear.RCS)
        {
            return;
        }
        this.lastGear = lastGear;
    }
}
