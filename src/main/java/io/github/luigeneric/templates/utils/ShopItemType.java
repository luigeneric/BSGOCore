package io.github.luigeneric.templates.utils;

public enum ShopItemType
{
    None,
    // Token: 0x0400204C RID: 8268
    Resource,
    // Token: 0x0400204D RID: 8269
    Augment,
    // Token: 0x0400204E RID: 8270
    Round,
    // Token: 0x0400204F RID: 8271
    Flare,
    // Token: 0x04002050 RID: 8272
    Mine,
    // Token: 0x04002051 RID: 8273
    Missile,
    // Token: 0x04002052 RID: 8274
    Power,
    // Token: 0x04002053 RID: 8275
    Repair,
    // Token: 0x04002054 RID: 8276
    PointDefense,
    // Token: 0x04002055 RID: 8277
    Flak,
    // Token: 0x04002056 RID: 8278
    JumpTargetTransponder,
    // Token: 0x04002057 RID: 8279
    Junk,
    // Token: 0x04002058 RID: 8280
    Radio,
    // Token: 0x04002059 RID: 8281
    TechAnalysis,
    // Token: 0x0400205A RID: 8282
    Weapon,
    // Token: 0x0400205B RID: 8283
    Computer,
    // Token: 0x0400205C RID: 8284
    Hull,
    // Token: 0x0400205D RID: 8285
    Engine,
    // Token: 0x0400205E RID: 8286
    ShipPaint,
    // Token: 0x0400205F RID: 8287
    Avionics,
    // Token: 0x04002060 RID: 8288
    StarterKit,
    // Token: 0x04002061 RID: 8289
    Torpedo,
    // Token: 0x04002062 RID: 8290
    Ship,
    // Token: 0x04002063 RID: 8291
    MetalPlate,
    // Token: 0x04002064 RID: 8292
    AntiCapitalMissile,
    // Token: 0x04002065 RID: 8293
    RadiationControl,
    // Token: 0x04002066 RID: 8294
    Unknown;

    public final byte value;
    ShopItemType()
    {
        this.value = (byte) this.ordinal();
    }
}
