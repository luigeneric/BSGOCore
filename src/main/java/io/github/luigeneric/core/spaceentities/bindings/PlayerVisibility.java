package io.github.luigeneric.core.spaceentities.bindings;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ChangeVisibilityReason;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerVisibility implements IProtocolWrite
{
    private boolean isVisible;
    private ChangeVisibilityReason changeVisibilityReason;
    private GhostJumpState ghostJumpState;

    private long lastChangeVisibility;
    private final AtomicBoolean visibilityChanged;
    private final AtomicLong anchoredObjectId;

    public PlayerVisibility(final boolean isVisible, final ChangeVisibilityReason changeVisibilityReason,
                            final GhostJumpState ghostJumpState)
    {
        this.isVisible = isVisible;
        this.changeVisibilityReason = Objects.requireNonNull(changeVisibilityReason);;
        this.ghostJumpState = ghostJumpState;
        this.anchoredObjectId = new AtomicLong();
        this.lastChangeVisibility = System.currentTimeMillis();
        this.visibilityChanged = new AtomicBoolean();

        this.changeVisibility(false, ChangeVisibilityReason.Default);
    }
    public PlayerVisibility(final boolean isVisible)
    {
        this(isVisible, ChangeVisibilityReason.Default, GhostJumpState.NOT_STARTED);
    }

    public void changeVisibility(final boolean isVisible, final ChangeVisibilityReason changeVisibilityReason,
                                 final long anchoredTarget)
            throws NullPointerException
    {
        this.changeVisibilityReason = Objects
                .requireNonNull(changeVisibilityReason, "Visibility change reason was null!");

        this.isVisible = isVisible;
        this.anchoredObjectId.set(anchoredTarget);


        this.lastChangeVisibility = System.currentTimeMillis();
        this.visibilityChanged.set(true);
    }

    public long getAnchoredObjectId()
    {
        return anchoredObjectId.get();
    }

    public void changeVisibility(final boolean isVisible, final ChangeVisibilityReason changeVisibilityReason)
            throws NullPointerException
    {
        changeVisibility(isVisible, changeVisibilityReason, 0);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeBoolean(this.isVisible);
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public ChangeVisibilityReason getChangeVisibilityReason()
    {
        return changeVisibilityReason;
    }

    public boolean requiredJumpIn(final Tick currentTick)
    {
        if (this.isVisible)
            return false;

        final boolean isGhostStarted = this.ghostJumpState == GhostJumpState.STARTED;
        if (!isGhostStarted)
            return false;

        final boolean isDefault = this.changeVisibilityReason == ChangeVisibilityReason.Default;
        if (!isDefault)
            return false;

        //current time must be higher than last time + 10 seconds must be lower
        return (this.lastChangeVisibility + 10_000) < currentTick.getTimeStamp();
    }

    public boolean checkVisibilityRequiresUpdate()
    {
        return visibilityChanged.compareAndSet(true, false);
    }

    /**
     * Starts the ghost jump in effect
     * @return true if start process was correct, false if GhostJump was already started before or is already started!
     */
    public boolean startGhostJump()
    {
        if (this.ghostJumpState != GhostJumpState.NOT_STARTED)
            return false;

        this.ghostJumpState = GhostJumpState.STARTED;
        this.changeVisibility(false, ChangeVisibilityReason.Default);
        return true;
    }
    public void finishGhostJumpInIfNotFinished()
    {
        if (this.ghostJumpState != GhostJumpState.STARTED)
            return;

        if (this.changeVisibilityReason != ChangeVisibilityReason.Default)
        {
            return;
        }

        this.changeVisibility(true, ChangeVisibilityReason.Jump);
        this.ghostJumpState = GhostJumpState.FINISHED;
    }

    @Override
    public String toString()
    {
        return "PlayerVisibility{" +
                "isVisible=" + isVisible +
                ", changeVisibilityReason=" + changeVisibilityReason +
                ", ghostJumpState=" + ghostJumpState +
                ", lastChangeVisibility=" + lastChangeVisibility +
                ", visibilityChanged=" + visibilityChanged +
                '}';
    }

    public enum GhostJumpState
    {
        NOT_STARTED,
        STARTED,
        FINISHED
    }
}
