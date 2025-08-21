CREATE TABLE IF NOT EXISTS factions
(
    id   INTEGER NOT NULL,
    name TEXT    NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO factions(id, name)
VALUES (0, "Neutral"),
       (1, "Colonial"),
       (2, "Cylon"),
       (3, "Ancient");

CREATE TABLE IF NOT EXISTS players
(
    id               INTEGER NOT NULL,
    name             TEXT    NOT NULL,
    faction_id       INTEGER NOT NULL,
    roles_bits       INTEGER NOT NULL, --roles is a bitfield!
    last_logout_date TEXT    NOT NULL,
    last_wof_date    TEXT    NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (faction_id)
        REFERENCES factions (id)
);

CREATE TABLE IF NOT EXISTS avatar_items
(
    players_id INTEGER NOT NULL,
    item_id    INTEGER NOT NULL,
    value      TEXT    NOT NULL,

    PRIMARY KEY (players_id, item_id)
        FOREIGN KEY (players_id)
        REFERENCES players(id)
);

CREATE TABLE IF NOT EXISTS caps
(
    players_id    INTEGER NOT NULL,
    guid          INTEGER NOT NULL,
    value         INTEGER NOT NULL,
    last_cap_date TEXT    NOT NULL,


    PRIMARY KEY (players_id, guid),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS counters
(
    players_id INTEGER NOT NULL,
    guid       INTEGER NOT NULL,
    value      REAL    NOT NULL,

    PRIMARY KEY (players_id, guid),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS factor
(
    id               INTEGER NOT NULL,
    players_id       INTEGER NOT NULL,
    factor_source_id INTEGER NOT NULL,
    factor_type_id   INTEGER NOT NULL,
    value            REAL    NOT NULL,
    end_time         TEXT    NOT NULL,

    PRIMARY KEY (players_id, id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS mails
(
    mail_id            INTEGER NOT NULL,
    players_id         INTEGER NOT NULL,
    mail_template_guid INTEGER NOT NULL,
    received_timestamp TEXT    NOT NULL,
    parameters         TEXT    NOT NULL,
    PRIMARY KEY (players_id, mail_id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS mission_books
(
    player_id                     INTEGER NOT NULL PRIMARY KEY,
    last_time_missions_fetch_date TEXT    NOT NULL,
    FOREIGN KEY (player_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS player_missions
(
    player_id              INTEGER NOT NULL,
    mission_id             INTEGER NOT NULL,
    mission_guid           INTEGER NOT NULL,
    associated_sector_guid INTEGER NOT NULL,
    counter_guid           INTEGER NOT NULL,
    current_count          INTEGER NOT NULL,
    need_count             INTEGER NOT NULL,
    PRIMARY KEY (player_id, mission_id, mission_guid, counter_guid),
    FOREIGN KEY (player_id)
        REFERENCES players (id)
);