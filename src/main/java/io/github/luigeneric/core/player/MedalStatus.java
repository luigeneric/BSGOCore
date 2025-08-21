package io.github.luigeneric.core.player;


import io.github.luigeneric.enums.AssistMedal;
import io.github.luigeneric.enums.KillerMedal;
import io.github.luigeneric.enums.PvpMedal;
import io.github.luigeneric.enums.TournamentMedal;

public record MedalStatus(PvpMedal pvpMedal, TournamentMedal tournamentMedal, KillerMedal killerMedal, AssistMedal assistMedal)
{
    public MedalStatus()
    {
        this(PvpMedal.None, TournamentMedal.None, KillerMedal.None, AssistMedal.None);
    }
}
