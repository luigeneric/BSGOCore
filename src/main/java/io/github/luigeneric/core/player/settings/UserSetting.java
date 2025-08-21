package io.github.luigeneric.core.player.settings;


import java.util.HashMap;
import java.util.Map;

public enum UserSetting
{
    CombatGui(1),
    ShowTutorial(2),
    CameraMode(3),
    InvertedVertical(5),
    MusicVolume(6),
    SoundVolume(7),
    Fullscreen(8),
    StatsIndication(10),
    HudIndicatorShowShipNames(11),
    LootPanelPosition(12),
    InventoryPanelPosition(13),
    CompletedTutorials(14),
    GraphicsQuality(15),
    Layout(16),
    AssignmentsCollapsed(17),
    ShowStarDust(18),
    ShowStarFog(20),
    ViewDistance(22),
    ShowGlowEffect(23),
    ShowChangeFaction(24),
    ChatShowPrefix(25),
    MouseWheelBinding(26),
    ChatViewLocal(27),
    ChatViewGlobal(28),
    AutoLoot(29),
    Fullframe(30),
    ShowPopups(32),
    ShowOutpostMessages(33),
    ShowHeavyFightingMessages(34),
    ShowAugmentMessages(35),
    ShowMiningShipMessages(36),
    ShowExperienceMessages(37),
    HudIndicatorShowWingNames(38),
    AdvancedFlightControls(39),
    AntiAliasing(40),
    UseProceduralTextures(43),
    ShowFpsAndPing(44),
    ShowBulletImpactFx(45),
    CombatText(46),
    FlakFieldDensity(47),
    ShowEnemyIndication(49),
    ShowFriendIndication(50),
    DeadZoneMouse(51),
    HudIndicatorShowMissionArrow(52),
    AutomaticAmmoReload(53),
    ShowWofConfirmation(54),
    DeadZoneJoystick(55),
    SensitivityJoystick(56),
    CameraZoom(57),
    JoystickGamepadEnabled(58),
    ShowXbox360Buttons(59),
    HudIndicatorShowTitles(60),
    HighResModels(61),
    HighResTextures(62),
    HighQualityParticles(63),
    AnisotropicFiltering(64),
    ShowCutscenes(65),
    MuteSound(66),
    ShowDamageOverlay(67),
    ShowShipSkins(68),
    ShowWeaponModules(69),
    SystemMap3DTransitionMode(70),
    SystemMap3DCameraView(71),
    SystemMap3DShowAsteroids(72),
    SystemMap3DShowDynamicMissions(73),
    SystemMap3DFormAsteroidGroups(74),
    HudIndicatorMinimizeDistance(90),
    HudIndicatorShowTargetNames(91),
    HudIndicatorTextSize(92),
    HudIndicatorColorScheme(93),
    HudIndicatorShowShipTierIcon(94),
    HudIndicatorBracketResizing(95),
    HudIndicatorDescriptionDisplayDistance(96),
    HudIndicatorSelectionCrosshair(97),
    HudIndicatorHealthBar(98),
    ShowAssignmentMessages(110),
    ShowXpBar(111),
    VSync(120),
    FramerateCapping(121),
    FogQuality(122);

    public static final int SIZE = Integer.SIZE;

    public final byte value;

    private static final class MappingsHolder
    {
        private static final Map<Integer, UserSetting> mappings = new HashMap<Integer, UserSetting>();
    }

    private static Map<Integer, UserSetting> getMappings()
    {
        return MappingsHolder.mappings;
    }

    UserSetting(final int value)
    {
        this.value = (byte) value;
        getMappings().put(value, this);
    }

    public static UserSetting forValue(final int value)
    {
        return getMappings().get(value);
    }
}

