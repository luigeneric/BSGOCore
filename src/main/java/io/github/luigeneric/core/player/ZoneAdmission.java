package io.github.luigeneric.core.player;

import io.github.luigeneric.templates.shipitems.ShipItem;

import java.time.LocalDateTime;
import java.util.List;

public record ZoneAdmission(long zoneGuid, List<ShipItem> itemPrice, LocalDateTime date){}
