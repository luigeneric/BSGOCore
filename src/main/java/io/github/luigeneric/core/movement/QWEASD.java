package io.github.luigeneric.core.movement;


import io.github.luigeneric.core.player.settings.Action;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class QWEASD
{
    private int bitmask;
    private boolean inputChanged;


    public QWEASD()
    {
        this.bitmask = 0;
    }

    public QWEASD(final int bitmask)
    {
        this.bitmask = bitmask;
    }

    public int getBitmask()
    {
        return this.bitmask;
    }

    public void ResetKeyStates()
    {
        bitmask = 0;
    }

    public void Flush()
    {
        inputChanged = false;
    }

    public Vector2 deriveDirectionVectorFromPressedKeys()
    {
        Vector3 zero = Vector3.zero();
        zero.add(Vector3.mult(Vector3.up(), bit(0)));
        zero.add(Vector3.mult(Vector3.left(), bit(1)));
        zero.add(Vector3.mult(Vector3.down(), bit(2)));
        zero.add(Vector3.mult(Vector3.right(), bit(3)));
        //zero += bit(0) * Vector3.up();
        //zero += bit(1) * Vector3.left();
        //zero += bit(2) * Vector3.down();
        //zero += bit(3) * Vector3.right();
        return new Vector2(zero.getX(), zero.getY());
    }


    /**
     *
     * @param action enum type
     * @param bActive
     * @return
     */
    public boolean tryToggleAction(Action action, boolean bActive)
    {
        int bit;
        switch (action)
        {
            case SlopeForwardOrSlideUp -> bit = 1;
            case TurnOrSlideLeft -> bit = 2;
            case SlopeBackwardOrSlideDown -> bit = 4;
            case TurnOrSlideRight -> bit = 8;
            case RollLeft -> bit = 16;
            case RollRight -> bit = 32;
            default ->
            {
                return false;
            }
        }
        inputChanged = true;
        SetBit(bit, bActive);
        return true;
    }

    public void SetBit(int bit, boolean bActive)
    {
        if (bActive)
        {
            bitmask |=  bit;
        }
        else
        {
            bitmask &= (63 - bit);
        }
    }

    public void setBitmask(int bitmask)
    {
        this.bitmask = bitmask;
    }

    public boolean isAnyKeyPressed()
    {
        return bitmask > 0;
    }

    private int bit(int n)
    {
        return (bitmask & (1 << n)) >> n;
    }


    public int pitch()
    {
        return this.bit(0) - this.bit(2);
    }
    public int yaw()
    {
        return this.bit(3) - this.bit(1);
    }
    public int roll()
    {
        return this.bit(4) - this.bit(5);
    }


    @Override
    public String toString()
    {
        return "QWEASD{" +
                "bitmask=" + bitmask +
                ", inputChanged=" + inputChanged +
                '}';
    }
}
