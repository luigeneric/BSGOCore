CREATE TABLE IF NOT EXISTS game_locations(
                                             id INTEGER NOT NULL PRIMARY KEY,
                                             name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS locations(
                                        players_id INTEGER NOT NULL,
                                        sector_id INTEGER NOT NULL,
                                        game_locations_id INTEGER NOT NULL,
                                        previous_game_locations_id INTEGER,


                                        PRIMARY KEY (players_id),
                                        FOREIGN KEY (players_id)
                                            REFERENCES players(id),
                                        FOREIGN KEY (game_locations_id)
                                            REFERENCES game_locations(id),
                                        FOREIGN KEY (previous_game_locations_id)
                                            REFERENCES game_locations(id)
);

INSERT INTO game_locations(id, name)
VALUES
    (0, "Unknown"),
    (1, "Space"),
    (2, "Room"),
    (3, "Story"),
    (4, "Disconnect"),
    (5, "Arena"),
    (6, "BattleSpace"),
    (7, "Tournament"),
    (8, "Tutorial"),
    (9, "Teaser"),
    (10, "Avatar"),
    (11, "Starter"),
    (12, "Zone");