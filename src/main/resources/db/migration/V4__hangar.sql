CREATE TABLE IF NOT EXISTS hangars
(
    players_id   INTEGER NOT NULL,
    active_index INTEGER NOT NULL,

    PRIMARY KEY (players_id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS players_hangar_ships
(
    players_id INTEGER NOT NULL,
    server_id  INTEGER NOT NULL,
    guid       INTEGER NOT NULL,
    durability REAL    NOT NULL,
    name       TEXT    NOT NULL,

    PRIMARY KEY (players_id, server_id, guid),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);


CREATE TABLE IF NOT EXISTS shipSlots
(
    players_id INTEGER NOT NULL,
    ship_id    INTEGER NOT NULL,
    server_id  INTEGER NOT NULL,
    guid       INTEGER NOT NULL,
    durability REAL    NOT NULL,
    PRIMARY KEY (players_id, server_id, ship_id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);