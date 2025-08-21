package io.github.luigeneric.core.protocols.game;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Info(2),
    WhoIs(4),
    Move(6),
    ObjectLeft(7),
    WeaponShot(13),
    MissileDecoyed(18),
    SyncMove(20),
    Cast(22),
    StopSlotAbility(24),
    Scan(34),
    CombatInfo(40),
    AskStartQueue(47),
    AskJump(49),
    Collide(55),
    FTLCharge(58),
    VirusBlocked(59),
    RemoveMe(69),
    TimeOrigin(70),
    StopGroupJump(76),
    LeaderStopGroupJump(77),
    NotEnoughTylium(81),
    UpdateRoles(83),
    @Deprecated
    PaintTheTarget(84),
    @Deprecated
    UnpaintTheTarget(85),
    StopJump(86),
    ChangeVisibility(87),
    UpdateFactionGroup(88),
    MineField(90),
    ObjectState(91),
    FlareReleased(92),
    LostAbilityTarget(93),
    LostJumpTransponder(94),
    DockingDelay(95),
    ChangedPlayerSpeed(96),
    ShortCircuitResult(97),
    OutpostStateBroadcast(98),
    RespawnOptions(99),
    AnchorDeclined(100),
    @Deprecated(since = "unknown, client removed this and is empty")
    DetachedToSpace(104),
    RetachedToSpace(105),
    CargoInteraction(106);


    public final int value;
    private static final Map<Integer, ServerMessage> map = new HashMap<>();

    ServerMessage(final int value)
    {
        this.value = value;
    }

    static
    {
        for (final ServerMessage pageType : ServerMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ServerMessage valueOf(int pageType)
    {
        return map.get(pageType);
    }

    public int getValue()
    {
        return this.value;
    }
}
