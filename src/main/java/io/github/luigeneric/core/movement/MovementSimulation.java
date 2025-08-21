package io.github.luigeneric.core.movement;


import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import lombok.extern.slf4j.Slf4j;

/***
 * The goal of this class is to provide as close as possible movement simulation to the original implementation in Simulation class
 */
@Slf4j
public final class MovementSimulation
{
    private MovementSimulation()
    {
    }

    /**
     * Creates first new MovementFrame based on the old one and the current MovementOptions to direct the object to the given targetEuler3
     *
     * @param prevFrame    the previous MovementFrame, won't change the values inside the old MovementFrame!
     * @param targetEuler3 the target to direct to in euler angles
     * @param options      the current MovementOptions
     * @param dt           the time passed
     * @return first new MovementFrame containing the new movement information
     */
    public static MovementFrame moveToDirection(final MovementFrame prevFrame, final Euler3 targetEuler3,
                                                final MovementOptions options, final float dt)
    {
        final Euler3 currentEuler = prevFrame.getEuler3();
        final Euler3 maxAngularSpeed = options.maxEulerSpeed(currentEuler);
        final Euler3 minAngularSpeed = options.minEulerSpeed(currentEuler);
        final Euler3 deltaEuler = (Euler3.sub(targetEuler3, currentEuler)).normalized(false);
        final float pitchTimeToPeak = Mathf.abs(2f * deltaEuler.pitch() / options.getPitchAcceleration());
        final float yawTimeToPeak = Mathf.abs(2f * deltaEuler.yaw() / options.getYawAcceleration());
        float desiredYawSpeed = 0f;
        if (deltaEuler.yaw() > Mathf.EPSILON)
        {
            desiredYawSpeed = options.getYawAcceleration() * yawTimeToPeak;
        } else if (deltaEuler.yaw() < -Mathf.EPSILON)
        {
            desiredYawSpeed = -options.getYawAcceleration() * yawTimeToPeak;
        }
        float desiredPitchSpeed = 0f;
        if (deltaEuler.pitch() > Mathf.EPSILON)
        {
            desiredPitchSpeed = options.getPitchAcceleration() * pitchTimeToPeak;
        } else if (deltaEuler.pitch() < -Mathf.EPSILON)
        {
            desiredPitchSpeed = -options.getPitchAcceleration() * pitchTimeToPeak;
        }
        final float yawAcceleration = (desiredYawSpeed - prevFrame.getEuler3Speed().yaw()) / dt;
        final float pitchAcceleration = (desiredPitchSpeed - prevFrame.getEuler3Speed().pitch()) / dt;

        final float bankAngleCmd = Mathf.clamp(-deltaEuler.yaw() * options.getMaxRoll() / 135f, -options.getMaxRoll(), options.getMaxRoll());
        final float rollAngleError = Mathf.normalizeAngle(bankAngleCmd - currentEuler.getRoll());
        final float rollAcceleration = (options.getRollAcceleration() * (rollAngleError / options.getMaxRoll() - prevFrame.getEuler3Speed().getRoll() * options.getRollFading() / options.getRollMaxSpeed()));

        final Euler3 deltaAngularSpeed = new Euler3(pitchAcceleration, yawAcceleration, rollAcceleration);
        deltaAngularSpeed.clamp(options.maxTurnAcceleration());
        deltaAngularSpeed.mult(dt);

        //this one is now the deltaAngularSpeed
        deltaAngularSpeed.add(prevFrame.getEuler3Speed());

        //deltaAngularSpeed.clamp(from, to);
        deltaAngularSpeed.clamp(minAngularSpeed, maxAngularSpeed);
        final Vector3 linearSpeed = advanceLinearSpeed(prevFrame, options, dt);
        final Vector3 strafeSpeed = advanceStrafingSpeed(prevFrame, options, 0f, 0f, dt);

        final Euler3 nextEuler = prevFrame.getFutureEuler3(dt);
        final Vector3 nextPosition = prevFrame.getFuturePosition(dt);


        //>>>>>>>>>>SAVE WITHOUT COPY<<<<<<<<<<<<<<<<<
        return new MovementFrame(nextPosition, nextEuler, linearSpeed, strafeSpeed, deltaAngularSpeed, (byte) 0);
    }

    public static MovementFrame moveToDirectionWithoutRoll(final MovementFrame prevFrame, final Euler3 targetEuler3,
                                                           final MovementOptions options)
    {
        return moveToDirectionWithoutRoll(prevFrame, targetEuler3, options, 0.1f);
    }

    private static MovementFrame moveToDirectionWithoutRoll(final MovementFrame prevFrame, final Euler3 targetEuler3,
                                                            final MovementOptions options,
                                                            final float dt)
    {
        //save, new rotation obj
        final Quaternion rotation = prevFrame.getEuler3Speed().quaternion();
        final Quaternion q = Quaternion.fromToRotation(prevFrame.getEuler3().direction(), targetEuler3.direction());
        final Quaternion lhs = MovementSimulation.scaleWithMax(q, 2f, options.getYawMaxSpeed());

        //final Quaternion q2 = Quaternion.mult(lhs, Quaternion.inverse(rotation));
        lhs.mult(Quaternion.inverse(rotation));

        final Quaternion to = MovementSimulation.scaleWithMax(lhs, 8f, options.getYawAcceleration());
        final Quaternion changePerSecond = Quaternion.rotateTowards(Quaternion.identity(), to, options.getYawAcceleration());
        final Quaternion to2 = Euler3.rotateOverTime(rotation, changePerSecond, dt);
        final Quaternion quat = Quaternion.rotateTowards(Quaternion.identity(), to2, options.getYawMaxSpeed());
        final Euler3 euler3Speed = Euler3.fromQuaternion(quat);
        final Vector3 linearSpeed = MovementSimulation.advanceLinearSpeed(prevFrame, options, dt);
        final Vector3 strafeSpeed = MovementSimulation.advanceStrafingSpeed(prevFrame, options, 0f, 0f, dt);

        //>>>>>>>>>>SAVE NO OLD VECTORS<<<<<<<<<<<<<<<<

        return new MovementFrame(
                prevFrame.getFuturePosition(dt), prevFrame.getFutureEuler3(dt),
                linearSpeed, strafeSpeed, euler3Speed, (byte) 2);
    }

    /**
     * Scales a given Quaternion with scale and max scale
     *
     * @param qArg  quaternion arg, won't change the given quaternion
     * @param scale how much
     * @param max   clamped max value
     * @return a new Quaternion object
     */
    protected static Quaternion scaleWithMax(final Quaternion qArg, final float scale, final float max)
    {
        final Quaternion q = qArg.copy();

        if (q.w() < 0f)
        {
            q.mult(-1f);
        }
        final Vector3 axis = new Vector3(); //do not change this, axis is call by reference
        final float num = q.toAngleAxis(axis);
        final float angle = Mathf.clamp(num * scale, 0f, max);
        return Quaternion.angleAxis(angle, axis);
    }


    /**
     * Updates the linearSpeed-Vector3
     *
     * @param prevFrame the old MovementFrame, no values will be changed
     * @param options   old MovementOptions, no values will be changed
     * @return first new Vector3 with the new linearSpeed
     */
    public static Vector3 advanceLinearSpeed(final MovementFrame prevFrame, final MovementOptions options, final float dt)
    {
        if (options.getGear() == Gear.RCS)
        {
            return prevFrame.getLinearSpeed().copy();
        }

        final Vector3 previousLinearVelocity = prevFrame.getLinearSpeed();
        final Vector3 lookDirection = prevFrame.lookDirection(); //lookDirection may be changed!
        //project current linear speed on the lookDirection
        final float projectedLinearSpeed = Vector3.dot(lookDirection, previousLinearVelocity);

        //projectedVelocityAlongLookDirection
        final Vector3 projectedLinearVelocityVector = Vector3.mult(lookDirection, projectedLinearSpeed);

        final Vector3 linearSpeedError = Vector3.sub(previousLinearVelocity, projectedLinearVelocityVector);
        final float magnitude = linearSpeedError.magnitude();
        linearSpeedError.normalize();
        final Vector3 inertiaCompensationVelocity = Vector3.mult(linearSpeedError, getNewSpeed(magnitude, 0f, options.getInertiaCompensation(), dt));
        final Vector3 targetVelocity = lookDirection.mult(getNewSpeed(projectedLinearSpeed, options.getSpeed(), options.getAcceleration(), dt));

        return inertiaCompensationVelocity.add(targetVelocity);
    }

    public static float getNewSpeed(final float current, final float target, final float acceleration, final float dt)
    {
        final float accelerationDt = acceleration * dt;
        final float diffSpeed = current - target;
        if (Mathf.abs(diffSpeed) < accelerationDt)
        {
            return target;
        }
        if (diffSpeed > 0f)
        {
            return current - accelerationDt;
        }
        return current + accelerationDt;
    }

    /**
     * Strafing speed update based on movement options
     *
     * @param prevFrame the previous MovementFrame, will not affect Frame!
     * @param options   the current MovementOptions
     * @param strafeX   will be changed during the method
     * @param strafeY   will be changed during the method
     * @return first new Vector containing the new strafing-speed
     * @apiNote tested against original implementation and calculates correct results!
     */
    public static Vector3 advanceStrafingSpeed(final MovementFrame prevFrame, final MovementOptions options,
                                               float strafeX, float strafeY, final float dt)
    {
        final Quaternion prevFrameRotation = prevFrame.getRotation();
        final Vector3 strafeSpeed = prevFrame.getStrafeSpeed();
        strafeX = Mathf.clampMin11(strafeX);
        strafeY = Mathf.clampMin11(strafeY);
        final Vector3 point = Quaternion.mult(Quaternion.inverse(prevFrameRotation), strafeSpeed); //no change of prevFrame
        final float newStrafeX = strafeX * options.getStrafeMaxSpeed();
        final float newStrafeY = strafeY * options.getStrafeMaxSpeed();

        final float x = getNewSpeed(point.getX(), newStrafeX, ((point.getX() * newStrafeX >= -1f) ? 1f : 2f) * options.getStrafeAcceleration(), dt);
        final float y = getNewSpeed(point.getY(), newStrafeY, ((point.getY() * newStrafeY >= -1f) ? 1f : 2f) * options.getStrafeAcceleration(), dt);
        final float z = getNewSpeed(point.getZ(), 0f, options.getStrafeAcceleration(), dt);
        final Vector3 tmpVec = new Vector3(x, y, z);

        return Quaternion.mult(prevFrameRotation, tmpVec);
    }


    /**
     * Creates the next MovementFrame based on time passed
     *
     * @param prevFrame won't change previous Frame
     * @param pitch     pitch bit
     * @param yaw       yaw bit
     * @param options   the current MovementOptions
     * @return first new MovementFrame
     */
    public static MovementFrame wasd(final MovementFrame prevFrame,
                                     final int pitch, final int yaw,
                                     final MovementOptions options)
    {
        //roll is never used so not important
        return wasd(prevFrame, pitch, yaw, options, 0.1f);
    }

    /**
     * Creates the next MovementFrame based on time passed
     *
     * @param prevFrame won't change previous Frame
     * @param pitch     pitch bit
     * @param yaw       yaw bit
     * @param options   the current MovementOptions
     * @param dt        time passed since last update
     * @return first new MovementFrame
     */
    private static MovementFrame wasd(final MovementFrame prevFrame, final int pitch, final int yaw,
                                      final MovementOptions options, final float dt)
    {
        final Euler3 prevEuler3Speed = prevFrame.getEuler3Speed();
        final Euler3 euler = prevFrame.getEuler3();
        final Euler3 to = options.maxEulerSpeed(euler);
        final Euler3 from = options.minEulerSpeed(euler);

        float roll = 0f;
        if (yaw > 0)
        {
            roll = -options.getMaxRoll();
        }
        if (yaw < 0)
        {
            roll = options.getMaxRoll();
        }

        final float normalizedRollAngle = Mathf.normalizeAngle(roll - euler.getRoll());
        final Euler3 zero = Euler3.zero();
        zero.setRoll(options.getRollAcceleration() * (normalizedRollAngle / options.getMaxRoll() -
                prevEuler3Speed.getRoll() * options.getRollFading() / options.getRollMaxSpeed()));
        if (yaw != 0)
        {
            zero.setYaw(yaw * options.getYawAcceleration());
        } else if (prevEuler3Speed.yaw() > Mathf.EPSILON)
        {
            zero.setYaw(-options.getYawAcceleration() * options.getYawFading());
            from.setYaw(0f);
        } else if (prevEuler3Speed.yaw() < -Mathf.EPSILON)
        {
            zero.setYaw(options.getYawAcceleration() * options.getYawFading());
            to.setYaw(0f);
        }
        if (pitch != 0)
        {
            zero.setPitch((float) pitch * options.getPitchAcceleration());
        } else if (prevEuler3Speed.pitch() > Mathf.EPSILON)
        {
            zero.setPitch(-options.getPitchAcceleration() * options.getPitchFading());
            from.setPitch(0f);
        } else if (prevEuler3Speed.pitch() < -Mathf.EPSILON)
        {
            zero.setPitch(options.getPitchAcceleration() * options.getPitchFading());
            to.setPitch(0f);
        }
        final Euler3 euler3Speed = Euler3.add(prevEuler3Speed, Euler3.mult(zero, dt));
        euler3Speed.clamp(from, to);
        final Vector3 linearSpeed = MovementSimulation.advanceLinearSpeed(prevFrame, options, dt);
        final Vector3 strafeSpeed = MovementSimulation.advanceStrafingSpeed(prevFrame, options, 0f, 0f, dt);
        final Euler3 nextEuler = prevFrame.getFutureEuler3(dt);
        final byte mode = (byte) (pitch != 0 ? 1 : 0);
        return new MovementFrame(prevFrame.getFuturePosition(dt), nextEuler, linearSpeed, strafeSpeed, euler3Speed, mode);
    }

    /**
     * New qweasd maneuver type
     *
     * @param prevFrame won't be affected
     * @param pitch     pitch value
     * @param yaw       yaw value
     * @param roll      roll value
     * @param options   current MovementOptions
     * @return first new MovementFrame
     */
    public static MovementFrame qweasd(final MovementFrame prevFrame, final float pitch, final float yaw,
                                       final float roll, final MovementOptions options, final float dt)
    {
        final Quaternion prevFrameRotation = prevFrame.getRotation();
        final Vector3 vector = new Vector3(pitch, yaw, roll);
        final Euler3 scaleEuler3 = Euler3.mult(options.maxTurnAcceleration(), dt);
        //final Vector3 scale = scaleEuler3.componentsToVector3();
        //vector.scale(scale);
        vector.scale(scaleEuler3);
        //rotation speed
        Vector3 euler3SpeedVector3 = prevFrame.getEuler3Speed().componentsToVector3(); //euler3speed seems okay
        final Vector3 vector3 = Quaternion.mult(Quaternion.inverse(prevFrameRotation), euler3SpeedVector3);
        if (vector.getX() == 0f)
        {
            //vector.setX(Simulation.slowingThrust(vector3.getX(), scale.getX()));
            vector.setX(MovementSimulation.slowingThrust(vector3.getX(), scaleEuler3.pitch()));
        }
        if (vector.getY() == 0f)
        {
            vector.setY(MovementSimulation.slowingThrust(vector3.getY(), scaleEuler3.yaw()));
        }
        if (vector.getZ() == 0f)
        {
            vector.setZ(MovementSimulation.slowingThrust(vector3.getZ(), scaleEuler3.getRoll()));
        }
        final Vector3 b2 = prevFrameRotation.mult(vector);

        final Vector3 v = Vector3.add(euler3SpeedVector3, b2);
        euler3SpeedVector3 = MovementSimulation.clampToRotatedBox(v, options.maxTurnSpeed().componentsToVector3(), prevFrameRotation);

        //Euler3 euler3Speed = default(Euler3);
        //as of now I guess default(Struct) causes the struct to be initialized with 0 each
        final Euler3 euler3Speed = Euler3.createComponentsFromVector3(euler3SpeedVector3);
        final Vector3 linearSpeed = MovementSimulation.advanceLinearSpeed(prevFrame, options, dt);
        final Vector3 strafeSpeed = MovementSimulation.advanceStrafingSpeed(prevFrame, options, 0f, 0f, dt);

        return new MovementFrame(
                prevFrame.getFuturePosition(dt), prevFrame.getFutureEuler3(dt),
                linearSpeed, strafeSpeed, euler3Speed, (byte) 2
        );
    }

    /**
     *
     * @param v               not affected
     * @param halfSideLengths not affected
     * @param boxOrientation  won't be affected
     * @return first new vector clamed to the rotated box
     */
    public static Vector3 clampToRotatedBox(final Vector3 v, final Vector3 halfSideLengths,
                                            final Quaternion boxOrientation)
    {
        final Vector3 val = Quaternion.mult(Quaternion.inverse(boxOrientation), v);
        final Vector3 point = MovementSimulation.clamp(val, Vector3.invert(halfSideLengths), halfSideLengths);
        return Quaternion.mult(boxOrientation, point);
    }

    private static Vector3 clamp(final Vector3 val, final Vector3 min, final Vector3 max)
    {
        return Vector3.min(Vector3.max(val, min), max);

    }

    private static float slowingThrust(final float v, final float maxAccelThisFrame)
    {
        final float num = (float) ((v >= 0f) ? -1 : 1);
        return num * Mathf.min(Mathf.abs(v), Mathf.abs(maxAccelThisFrame));
    }

    public static MovementFrame turnByPitchYawStrikes(final MovementFrame prevFrame, final Vector3 pitchYawRollFactorArg,
                                                      final Vector2 strafeDirectionArg, final float strafeMagnitude,
                                                      final MovementOptions options)
    {
        return turnByPitchYawStrikes(prevFrame, pitchYawRollFactorArg, strafeDirectionArg, strafeMagnitude, options, 0.1f);
    }

    private static MovementFrame turnByPitchYawStrikes(final MovementFrame prevFrame, final Vector3 pitchYawRollFactorArg,
                                                       final Vector2 strafeDirection, final float strafeMagnitude,
                                                       final MovementOptions options, final float dt)
    {
        final Vector3 pitchYawRollFactor = pitchYawRollFactorArg.copy();


        final Vector3 vector = options.maxTurnAcceleration().componentsToVector3();
        final Vector3 vector2 = options.maxTurnSpeed().componentsToVector3();
        final Vector3 nextPosition = prevFrame.getFuturePosition(dt);
        final Euler3 nextEuler = prevFrame.getFutureEuler3(dt);
        final Quaternion rotation = nextEuler.quaternion();
        //Simulation.clampVectorComponents(pitchYawRollFactor, -1, 1);
        pitchYawRollFactor.clamp(-1, 1).scale(vector2);
        final Vector3 vector3 = MovementSimulation.advanceLinearSpeed(prevFrame, options, dt);
        final float strafeX = strafeDirection.getX() * strafeMagnitude;
        final float strafeY = strafeDirection.getY() * strafeMagnitude;
        final Vector3 vector4 = MovementSimulation.advanceStrafingSpeed(prevFrame, options, strafeX, strafeY, dt);
        final Quaternion quaternion = Quaternion.inverse(rotation);
        final Vector3 vector5 = Quaternion.mult(quaternion, prevFrame.getEuler3Speed().componentsToVector3());

        final Vector3 vector7 = Vector3.sub(pitchYawRollFactor, vector5);
        final Vector3 vector8 = Vector3.mult(vector, dt);
        MovementSimulation.clampVectorComponents(vector7, Vector3.invert(vector8), vector8);
        vector5.add(vector7);
        if (Mathf.abs(pitchYawRollFactor.getZ()) < dt)
        {
            vector5.setZ(MovementSimulation.getNewSpeed(vector5.getZ(), 0f, options.getRollAcceleration(), dt));
        }
        final Vector3 vector10 = Quaternion.mult(rotation, vector5);
        final Euler3 euler = new Euler3(vector10.getX(), vector10.getY(), vector10.getZ());
        return new MovementFrame(nextPosition, nextEuler, vector3, vector4, euler, (byte) 2);
    }


    /**
     * Clamps the given valueVector inside min and max
     *
     * @param valueVector call by ref
     * @param minVector   min arg
     * @param maxVector   max arg
     */
    public static void clampVectorComponents(final Vector3 valueVector, final Vector3 minVector, final Vector3 maxVector)
    {
        valueVector.set(
                Mathf.clamp(valueVector.getX(), minVector.getX(), maxVector.getX()),
                Mathf.clamp(valueVector.getY(), minVector.getY(), maxVector.getY()),
                Mathf.clamp(valueVector.getZ(), minVector.getZ(), maxVector.getZ())
        );
    }

    /**
     *
     * @param prevFrame    the old frame, won't affect the previousFrame
     * @param targetEuler3 the direction to move to
     * @param roll         current roll value
     * @param strafeX      only used by client
     * @param strafeY      only used by client
     * @param options      the current MovementOptions
     * @return first new MovementFrame displaying the current movement state
     */
    public static MovementFrame turnToDirectionStrikes(final MovementFrame prevFrame, final Euler3 targetEuler3,
                                                       float roll, final float strafeX, final float strafeY,
                                                       final MovementOptions options,
                                                       final float dt
    )
    {
        final Vector3 maxTurnAccelerationVector = options.maxTurnAcceleration().componentsToVector3();
        final Vector3 maxTurnSpeedVector = options.maxTurnSpeed().componentsToVector3();
        final Vector3 vector2 = findDamping(maxTurnAccelerationVector, maxTurnSpeedVector);
        float num = 3f;
        final Vector3 vector3 = options.maxTurnSpeed().componentsToVector3();
        final Vector3 nextPosition = prevFrame.getFuturePosition(dt);
        final Euler3 nextEuler = prevFrame.getFutureEuler3(dt);
        final Quaternion rotation = nextEuler.quaternion();
        final Vector3 vector4 = targetEuler3.quaternion().mult(Vector3.forward());
        roll = Mathf.clamp(roll, -1f, 1f);
        final Vector3 linearSpeed = advanceLinearSpeed(prevFrame, options, dt);
        final Quaternion quaternion = Quaternion.inverse(rotation);
        final Vector3 toDirection = quaternion.mult(vector4);
        final Vector3 vector5 = quaternion.mult(prevFrame.getEuler3Speed().componentsToVector3());
        Vector3 eulerAngles = Quaternion.fromToRotation(Vector3.forward(), toDirection).getEulerAngles();
        eulerAngles = keepVectorComponentsWithinPlusMinus180(eulerAngles);
        final Vector3 vector6 = findVelocityForRotationDelta(eulerAngles, Vector3.mult(vector2, num));
        final Vector3 scale = Vector3.one().sub(Vector3.mult(vector2, dt));
        final Vector3 vector7 = vector5.copy();
        vector7.scale(scale);
        final Vector3 scale2 = Vector3.one().sub(Vector3.mult(vector2, num * dt));
        final Vector3 vector8 = vector5.copy();
        vector8.scale(scale2);
        boolean flag = false;
        boolean flag2 = false;
        if (Mathf.abs(vector7.getX()) > Mathf.abs(vector6.getX()))
        {
            scale.setX(Mathf.max(scale2.getX(), vector6.getX() / vector5.getX()));
            flag = true;
        }
        if (Mathf.abs(vector7.getY()) > Mathf.abs(vector6.getY()))
        {
            scale.setY(Mathf.max(scale2.getY(), vector6.getY() / vector5.getY()));
            flag2 = true;
        }
        vector5.scale(scale);
        final Vector3 vector9 = scalePitchYawSoThatOneOfThemHasLengthOneAndSetZToZero(eulerAngles);
        final Vector3 scale3 = maxTurnAccelerationVector.copy();
        vector9.scale(scale3);
        final Vector3 vector10 = Vector3.sub(vector6, vector5);
        if (Mathf.abs(vector9.getX()) > Mathf.abs(vector10.getX()))
        {
            vector9.setX(vector10.getX());
        }
        if (Mathf.abs(vector9.getY()) > Mathf.abs(vector10.getY()))
        {
            vector9.setY(vector10.getY());
        }
        if (flag)
        {
            vector9.setX(0f);
        }
        if (flag2)
        {
            vector9.setY(0f);
        }
        final float num2 = maxTurnAccelerationVector.getZ() * roll;
        vector5.addZ(num2 * dt);
        Vector3 val = Vector3.add(vector5, Vector3.mult(vector9, dt));
        val = clamp(val, Vector3.invert(vector3), vector3);
        final Vector3 vector11 = val.copy();
        final Vector3 vector12 = rotation.mult(vector11);
        final Euler3 euler3Speed = new Euler3(vector12.getX(), vector12.getY(), vector12.getZ());
        final Vector3 strafeSpeed = advanceStrafingSpeed(prevFrame, options, 0f, 0f, dt);
        return new MovementFrame(nextPosition, nextEuler, linearSpeed, strafeSpeed, euler3Speed, 2);
    }

    private static Vector3 scalePitchYawSoThatOneOfThemHasLengthOneAndSetZToZero(final Vector3 sourceArg)
    {
        final Vector3 source = sourceArg.copy();
        float f = Mathf.max(Mathf.abs(source.getX()), Mathf.abs(source.getY()));
        f = Mathf.abs(f);
        if (Math.abs(f) < 0.001f)
        {
            return source;
        }

        return new Vector3(source.getX() / f, source.getY() / f, 0);
    }

    private static Vector3 findVelocityForRotationDelta(final Vector3 localDeltaAngles, final Vector3 damping)
    {
        final Vector3 vector = new Vector3();
        final float num = Quaternion.euler(localDeltaAngles).toAngleAxis(vector);
        Vector3 almostRv = new Vector3(damping.getX() * vector.getX(), damping.getY() * vector.getY(), damping.getZ() * vector.getZ());
        return Vector3.mult(almostRv, num);
    }

    private static Vector3 keepVectorComponentsWithinPlusMinus180(final Vector3 vector)
    {
        final Vector3 source = new Vector3(vector);
        source.mod(360f);

        source.setX(((source.getX() <= 180f) ? source.getX() : (source.getX() - 360f)));
        source.setX(((source.getX() >= -180f) ? source.getX() : (source.getX() + 360f)));
        source.setY(((source.getY() <= 180f) ? source.getY() : (source.getY() - 360f)));
        source.setY(((source.getY() >= -180f) ? source.getY() : (source.getY() + 360f)));
        source.setZ(((source.getZ() <= 180f) ? source.getZ() : (source.getZ() - 360f)));
        source.setZ(((source.getZ() >= -180f) ? source.getZ() : (source.getZ() + 360f)));
        return source;
    }

    private static Vector3 findDamping(final Vector3 acceleration, final Vector3 maximumVelocity)
    {
        return new Vector3(
                acceleration.getX() / maximumVelocity.getX(),
                acceleration.getY() / maximumVelocity.getY(),
                acceleration.getZ() / maximumVelocity.getZ()
        );
    }
}
