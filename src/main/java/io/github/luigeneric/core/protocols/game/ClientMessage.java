package io.github.luigeneric.core.protocols.game;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    WhoIs(3),
    SubscribeInfo(10),
    UnSubscribeInfo(11),
    MoveToDirection(12),
    MoveToDirectionWithoutRoll(13),
    CastSlotAbility(21),
    CastImmutableSlotAbility(22),
    LockTarget(25),
    WASD(29),
    QWEASD(30),
    Mining(35),
    Loot(41),
    TakeLootItems(43),
    Dock(45),
    Jump(46),
    AnsStartQueue(48),
    AnsJump(50),
    @Deprecated
    Follow(52),
    Quit(54),
    SetSpeed(56),
    SetGear(57),
    JumpIn(61),
    MoveInfo(63),
    StopJump(65),
    SelectRespawnLocation(70),
    GroupJump(72),
    StopGroupJump(73),
    RequestJumpToTarget(75),
    CompleteJump(76),
    RequestUnanchor(77),
    RequestAnchor(78),
    RequestLaunchStrikes(79),
    CancelMiningRequest(82),
    RequestJumpToBeacon(85),
    ToggleAbilityOn(86),
    ToggleAbilityOff(87),
    UpdateAbilityTargets(88),
    GroupJumpToBeacon(89),
    /**
     * That stuff if you click with right mouse on 3d map
     */
    TurnToDirectionStrikes(100),
    TurnByPitchYawStrikes(101),
    CancelDocking(102),
    GroupJumpToTarget(103),
    CargoInteraction(106);


    public final int value;
    private static final Map<Integer, ClientMessage> map = new HashMap<>();

    ClientMessage(int value)
    {
        this.value = value;
    }

    static
    {
        for (ClientMessage pageType : ClientMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ClientMessage valueOf(final int pageType)
    {
        return map.get(pageType);
    }
}
