package io.github.luigeneric.core.player.settings;

import java.util.Map;

public enum Action
{
    None(0),
    ToggleWindowOptions(19),
    ToggleWindowPilotLog(20),
    ToggleWindowTutorial(21),
    ToggleWindowStatusInfo(22),
    ToggleWindowDuties(23),
    ToggleWindowStatusAssignments(24),
    ToggleWindowSkills(25),
    ToggleWindowShipStatus(26),
    ToggleWindowInFlightSupply(27),
    ToggleWindowInventory(28),
    ToggleWindowLeaderboard(29),
    NearestEnemy(30),
    NearestFriendly(31),
    CancelTarget(33),
    SelectNearestMissile(34),
    ToggleWindowWingRoster(35),
    ToggleWindowGalaxyMap(36),
    ToggleSystemMap3D(37),
    TurnOrSlideLeft(38),
    TurnOrSlideRight(39),
    SlopeForwardOrSlideUp(40),
    SlopeBackwardOrSlideDown(41),
    SpeedUp(42),
    SlowDown(43),
    ZoomIn(44),
    ZoomOut(45),
    FullSpeed(46),
    Boost(47),
    Stop(48),
    Follow(49),
    MatchSpeed(50),
    Jump(51),
    CancelJump(52),
    TargetCamera(53),
    ChaseCamera(54),
    FreeCamera(55),
    NoseCamera(56),
    ToggleCamera(57),
    ToggleShipName(58),
    ToggleCombatGUI(59),
    FireMissiles(60),
    ToggleGuns(61),
    FocusChat(62),
    UnfocusChat(63),
    Reply(64),
    TakeAllLoot(65),
    RollLeft(66),
    RollRight(67),
    AlignToHorizon(68),
    ToggleMovementMode(69),
    SelectNearestMine(70),
    ToggleGunsOnHold(71),
    ToggleBoost(72),
    ToggleFlak(73),
    TogglePointDefence(74),
    WeaponSlot1(101),
    WeaponSlot2(102),
    WeaponSlot3(103),
    WeaponSlot4(104),
    WeaponSlot5(105),
    WeaponSlot6(106),
    WeaponSlot7(107),
    WeaponSlot8(108),
    WeaponSlot9(109),
    WeaponSlot10(110),
    WeaponSlot11(111),
    WeaponSlot12(112),
    WEAPON_MAX(113),
    AbilitySlot1(114),
    AbilitySlot2(115),
    AbilitySlot3(116),
    AbilitySlot4(117),
    AbilitySlot5(118),
    AbilitySlot6(119),
    AbilitySlot7(120),
    AbilitySlot8(121),
    AbilitySlot9(122),
    AbilitySlot10(123),
    ABILITY_MAX(126),
    ToggleWindowHelp(127),
    ToggleFps(128),
    ToggleTournamentRanking(129),
    ToggleAdvancedFlightControls(130),
    ToggleSquadWindow(131),
    ToggleFullscreen(132),
    JoystickTurnLeft(150),
    JoystickTurnRight(151),
    JoystickTurnUp(152),
    JoystickTurnDown(153),
    JoystickRollLeft(154),
    JoystickRollRight(155),
    JoystickSpeedController(156),
    JoystickLookLeft(157),
    JoystickLookRight(158),
    JoystickLookUp(159),
    JoystickLookDown(160),
    JoystickStrafeLeft(161),
    JoystickStrafeRight(162),
    JoystickStrafeUp(163),
    JoystickStrafeDown(164),
    SelectScreenCenterObject(200),
    ToggleSystemMap2D(250),
    Map3DFocusYourShip(251),
    Map3DBackToOverview(252);

    public static final int SIZE = Integer.SIZE;

    public final int intValue;

    private static final class MappingsHolder
    {
        private static final Map<Integer, Action> mappings = new java.util.HashMap<>();
    }

    private static Map<Integer, Action> getMappings()
    {
        return MappingsHolder.mappings;
    }

    Action(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public static Action forValue(int value)
    {
        return getMappings().get(value);
    }
}

