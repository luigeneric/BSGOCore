package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Algorithm3D;
import io.github.luigeneric.templates.utils.SpotDesc;

public abstract class LaunchAbstractBase extends Maneuver
{
    protected final SpaceObject launcherSpaceObject;
    protected final SpotDesc spotDesc;
    protected final float relativeSpeed;

    public LaunchAbstractBase(final ManeuverType maneuverType, final SpaceObject launcherSpaceObject, final SpotDesc spotDesc,
                              final float relativeSpeed)
    {
        super(maneuverType);
        this.launcherSpaceObject = launcherSpaceObject;
        this.spotDesc = spotDesc;
        this.relativeSpeed = relativeSpeed;
    }

    /**
     * The MovementFrame for the start
     * @return
     */
    protected MovementFrame getLaunchFrame(final float dt)
    {
        final MovementFrame currentLauncherFrame = this.launcherSpaceObject.getMovementController().getLastFrame();

        //final Transform localTransform = spotDesc.getLocalTransform();
        //final Transform globalTransform = currentLauncherFrame.getNextTransform(dt);
        //final Transform relativeTransform = localTransform.applyTransform(globalTransform);

        final Quaternion relativeRotation =
                Algorithm3D.getRelativeRotationTo(currentLauncherFrame.getFutureEuler3(dt).quaternion(), spotDesc.getLocalRotation());
        final Vector3 relativePosition = Algorithm3D.getPointPositionRelativeTo(
                this.spotDesc.getLocalPosition(), currentLauncherFrame.getFuturePosition(dt), currentLauncherFrame.getRotation());
        final Vector3 rotMultForward = Quaternion.mult(relativeRotation, Vector3.forward());
        final Euler3 direction = Euler3.direction(rotMultForward).normalized(false);
        final Vector3 b = Vector3.mult(rotMultForward, this.relativeSpeed);

        return new MovementFrame(relativePosition, direction,
                Vector3.add(currentLauncherFrame.getLinearSpeed(), b),
                Vector3.zero(), Euler3.zero(), 0);
    }
}
