package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.MissileExplosionView;
import io.github.luigeneric.templates.utils.MissileType;

public class MissileCard extends Card
{
    private final MissileExplosionView explosionView;
    private final MissileType missileType;

    public MissileCard(long cardGuid, MissileExplosionView explosionView, MissileType type)
    {
        super(cardGuid, CardView.Missile);
        this.explosionView = explosionView;
        this.missileType = type;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(explosionView.value);
        bw.writeByte(missileType.value);
    }

    public MissileExplosionView getExplosionView()
    {
        return explosionView;
    }

    public MissileType getMissileType()
    {
        return missileType;
    }
}
