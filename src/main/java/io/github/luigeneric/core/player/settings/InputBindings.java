package io.github.luigeneric.core.player.settings;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.KeyCode;
import io.github.luigeneric.enums.KeyModifier;
import io.github.luigeneric.utils.AutoLock;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InputBindings implements IProtocolWrite
{
    private final Map<Action, InputBinding> inputBindingMap;
    private final Lock lock;

    public InputBindings(final Map<Action, InputBinding> inputBindingMap, final long userID)
    {
        this.inputBindingMap = inputBindingMap;
        this.lock = new ReentrantLock();
        MDC.put("userID", String.valueOf(userID));
    }
    public static InputBindings create(final long userID)
    {
        var tmp = new InputBindings(new HashMap<>(), userID);
        tmp.setupDefaults();
        return tmp;
    }

    public void set(final InputBinding inputBinding)
    {
        try(var l = new AutoLock(lock))
        {
            this.inputBindingMap.put(inputBinding.action, inputBinding);
        }
    }
    public void set(final List<InputBinding> inputBindings)
    {
        try(var l = new AutoLock(lock))
        {
            for (final InputBinding inputBinding : inputBindings)
            {
                this.inputBindingMap.put(inputBinding.action, inputBinding);
            }
        }
    }

    public Optional<InputBinding> get(final Action action)
    {
        try(var l = new AutoLock(lock))
        {
            return Optional.ofNullable(this.inputBindingMap.get(action));
        }
    }

    public void setupDefaults()
    {
        final List<InputBinding> inputBindings = new ArrayList<>();

        inputBindings.add(new InputBinding(Action.ToggleWindowPilotLog, KeyCode.I));
        inputBindings.add(new InputBinding(Action.ToggleWindowOptions, KeyCode.O));
        inputBindings.add(new InputBinding(Action.ToggleWindowLeaderboard, KeyCode.L));
        inputBindings.add(new InputBinding(Action.ToggleWindowWingRoster, KeyCode.B));
        inputBindings.add(new InputBinding(Action.ToggleWindowGalaxyMap, KeyCode.N));
        inputBindings.add(new InputBinding(Action.ToggleSystemMap3D, KeyCode.M));
        inputBindings.add(new InputBinding(Action.ToggleSystemMap2D, KeyCode.M, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.Map3DFocusYourShip, KeyCode.KeypadDivide));
        inputBindings.add(new InputBinding(Action.Map3DBackToOverview, KeyCode.KeypadMultiply));
        inputBindings.add(new InputBinding(Action.ToggleWindowStatusAssignments, KeyCode.A, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleWindowDuties, KeyCode.D, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleWindowSkills, KeyCode.Z, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleWindowShipStatus, KeyCode.S, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleWindowInFlightSupply, KeyCode.R, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleTournamentRanking, KeyCode.Tab));
        inputBindings.add(new InputBinding(Action.ToggleWindowTutorial, KeyCode.H, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.NearestEnemy, KeyCode.X));
        inputBindings.add(new InputBinding(Action.NearestFriendly, KeyCode.None));
        inputBindings.add(new InputBinding(Action.CancelTarget, KeyCode.C));
        inputBindings.add(new InputBinding(Action.SelectNearestMissile, KeyCode.Z));
        inputBindings.add(new InputBinding(Action.SelectNearestMine, KeyCode.None));
        inputBindings.add(new InputBinding(Action.Jump, KeyCode.J));
        inputBindings.add(new InputBinding(Action.CancelJump, KeyCode.K));
        inputBindings.add(new InputBinding(Action.TargetCamera, KeyCode.T, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ChaseCamera, KeyCode.C, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.FreeCamera, KeyCode.F, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.NoseCamera, KeyCode.N, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleCamera, KeyCode.V));
        inputBindings.add(new InputBinding(Action.ToggleShipName, KeyCode.P));
        inputBindings.add(new InputBinding(Action.ToggleCombatGUI, KeyCode.G, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleGuns, KeyCode.G));
        inputBindings.add(new InputBinding(Action.FireMissiles, KeyCode.F));
        inputBindings.add(new InputBinding(Action.TurnOrSlideLeft, KeyCode.A));
        inputBindings.add(new InputBinding(Action.TurnOrSlideRight, KeyCode.D));
        inputBindings.add(new InputBinding(Action.SlopeForwardOrSlideUp, KeyCode.W));
        inputBindings.add(new InputBinding(Action.SlopeBackwardOrSlideDown, KeyCode.S));
        inputBindings.add(new InputBinding(Action.RollLeft, KeyCode.Q));
        inputBindings.add(new InputBinding(Action.RollRight, KeyCode.E));
        inputBindings.add(new InputBinding(Action.AlignToHorizon, KeyCode.Period));
        inputBindings.add(new InputBinding(Action.SpeedUp, KeyCode.Equals));
        inputBindings.add(new InputBinding(Action.SlowDown, KeyCode.Minus));
        inputBindings.add(new InputBinding(Action.ZoomIn, KeyCode.Minus, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ZoomOut, KeyCode.Equals, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.Boost, KeyCode.Space));
        inputBindings.add(new InputBinding(Action.FullSpeed, KeyCode.PageUp));
        inputBindings.add(new InputBinding(Action.Stop, KeyCode.PageDown));
        inputBindings.add(new InputBinding(Action.Follow, KeyCode.Y));
        inputBindings.add(new InputBinding(Action.MatchSpeed, KeyCode.T));
        inputBindings.add(new InputBinding(Action.ToggleMovementMode, KeyCode.Mouse2));
        inputBindings.add(new InputBinding(Action.FocusChat, KeyCode.Return));
        inputBindings.add(new InputBinding(Action.UnfocusChat, KeyCode.Return, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.Reply, KeyCode.R));
        inputBindings.add(new InputBinding(Action.WeaponSlot1, KeyCode.Alpha1));
        inputBindings.add(new InputBinding(Action.WeaponSlot2, KeyCode.Alpha2));
        inputBindings.add(new InputBinding(Action.WeaponSlot3, KeyCode.Alpha3));
        inputBindings.add(new InputBinding(Action.WeaponSlot4, KeyCode.Alpha4));
        inputBindings.add(new InputBinding(Action.WeaponSlot5, KeyCode.Alpha5));
        inputBindings.add(new InputBinding(Action.WeaponSlot6, KeyCode.Alpha6));
        inputBindings.add(new InputBinding(Action.WeaponSlot7, KeyCode.Alpha7));
        inputBindings.add(new InputBinding(Action.WeaponSlot8, KeyCode.Alpha8));
        inputBindings.add(new InputBinding(Action.WeaponSlot9, KeyCode.Alpha9));
        inputBindings.add(new InputBinding(Action.WeaponSlot10, KeyCode.Alpha0));
        inputBindings.add(new InputBinding(Action.WeaponSlot11, KeyCode.LeftBracket));
        inputBindings.add(new InputBinding(Action.WeaponSlot12, KeyCode.RightBracket));
        inputBindings.add(new InputBinding(Action.AbilitySlot1, KeyCode.Alpha1, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot2, KeyCode.Alpha2, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot3, KeyCode.Alpha3, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot4, KeyCode.Alpha4, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot5, KeyCode.Alpha5, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot6, KeyCode.Alpha6, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot7, KeyCode.Alpha7, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot8, KeyCode.Alpha8, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot9, KeyCode.Alpha9, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.AbilitySlot10, KeyCode.Alpha0, KeyModifier.Shift));
        inputBindings.add(new InputBinding(Action.ToggleFps, KeyCode.P, KeyModifier.Shift));

        try(var l = new AutoLock(lock))
        {
            for (final InputBinding inputBinding : inputBindings)
            {
                this.inputBindingMap.put(inputBinding.action, inputBinding);
            }
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        try(var l = new AutoLock(lock))
        {
            bw.writeLength(this.inputBindingMap.size());
            for (final InputBinding inputBinding : this.inputBindingMap.values())
            {
                bw.writeDesc(inputBinding);
            }
        }
    }

    public Collection<InputBinding> getUnmodifiableInputBindings()
    {
        return Collections.unmodifiableCollection(inputBindingMap.values());
    }
}
