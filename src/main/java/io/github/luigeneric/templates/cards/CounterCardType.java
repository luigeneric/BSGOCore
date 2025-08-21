package io.github.luigeneric.templates.cards;

public enum CounterCardType
{
    aesirs_killed(64371525),
    avengers_killed(173249509),
    berserkers_killed(182923925),
    brimirs_killed(219954581),
    gungnirs_killed(50025957),
    halberds_killed(143972261),
    jotunns_killed(77889557),
    mauls_killed(53014821),
    raptors_killed(241523877),
    rhinos_killed(250643893),
    vanirs_killed(177944757),
    viper_mk3s_killed(237166309),
    viper_mk7_killed(178117637),
    vipers_killed(152762549),


    arena(6851650),
    asteroids_scanned(10577685),
    asteroids_mined(61526949),
    debris_looted(11402165),

    dynamic_mission_freighter_medium_success(65917668),
    dynamic_mission_medium_won(76400511),
    dynamic_mission_freighter_easy_success(68409156),
    dynamic_mission_easy_won(136028543),

    dynamic_mission_hard_won(138322223),
    dynamic_mission_easy_completed(210329397),
    dynamic_mission_medium_completed(16726325),
    dynamic_mission_hard_completed(208428933),

    wave_alpha_finished(0x2b27e65),
    wave_beta_finished(0x1622025),
    wave_gamma_finished(0x59f7ed5),
    select_faction_hax(27529097),
    locker_from_hax(56576009),
    repair_hax(134337257),
    locker_to_hax(101663385),
    select_hax(196068009),
    jump_hax(205937049),
    total_deaths(35463892),
    pve_deaths(95221316),
    pvp_deaths(95221652),
    pvp_action_killer(63121075),
    pvp_action_assist(39302357),
    pvp_action_buffer(326),
    pvp_action_debuffer(327),
    pvp_action_savior(21363731),
    enemies_killed(48097509),
    deflected_torpedo(44658416),

    titanium_mined(67875749),
    tylium_mined(111400053),
    water_mined(138317429),

    death_payment_popup_rules(70669140),
    last_water_exchange(79210422),
    pve_killed(102329861),
    pvp_killed(102330325),
    time_played(103386117),
    wof_played(108358261),
    mining_ships_called(118239717),
    patrol(124303709),
    mining_ships_income(125347686),
    missions_completed(126566101),
    recruits_invited(127680069),
    sectors_visited(130082917),
    mining_ships_killed(142881253),
    freighters_killed(160002101),
    tylium_burned(161454293),
    opposite_faction_killed(164239893),
    wave_delta_finished(168197797),
    wave_alpha(189918754),
    wave_gamma(190495986),
    wave_delta(190675810),
    story_missions(197138420),
    stationaries_killed(205553269),
    wave_beta(213250338),
    comets_killed(110),

    last_user_interaction_spam_check(221726988),
    arena_points_1x1_t1(224365282),
    arena_points_1x1_t2(224365283),
    arena_points_1x1_t3(224365284),
    number_of_deaths_since_payment_popup(228630769),
    story_missions_unsubmitted(229439477),
    /**
     * Unknown use, refer to damage_dealt!
     */
    @Deprecated
    damage_done(232732470),
    damage_dealt(233996773),
    damage_received(233996774),
    ancients_killed(233054565),

    hack_dradis_stats_differ(241522355),
    hack_dradis_send_disabled(257042837),

    daily_login(256499151),

    outposts_killed(231),
    outposts_damage_dealt(232),
    outposts_damage_received(233),

    drones_killed(260734533);


    public final long cardGuid;
    CounterCardType(final long cardGuid)
    {
        this.cardGuid = cardGuid;
    }
}
