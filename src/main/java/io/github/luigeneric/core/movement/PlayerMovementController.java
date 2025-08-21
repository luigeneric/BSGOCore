package io.github.luigeneric.core.movement;

import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.MovementCard;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class PlayerMovementController extends DynamicMovementController
{
    private final Lock lock;
    private final long userID;
    public PlayerMovementController(final Transform transform, final MovementCard movementCard, final long userID)
    {
        super(transform, movementCard);
        this.lock = new ReentrantLock();
        MDC.put("userID", String.valueOf(userID));
        this.userID = userID;
    }

    @Override
    public void movementUpdateInProgress()
    {
        this.lock.lock();
    }

    @Override
    public void movementUpdateFinished()
    {
        this.lock.unlock();
    }

    @Override
    public void move(final Tick tick, final float dt) throws IllegalStateException
    {
        super.move(tick, dt);
        //log.debug("MovementControll {}", movementOptions);
    }
}
