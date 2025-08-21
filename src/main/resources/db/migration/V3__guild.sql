CREATE TABLE IF NOT EXISTS guild_roles
(
    id   INTEGER NOT NULL PRIMARY KEY,
    role TEXT    NOT NULL
);

INSERT INTO guild_roles(id, role)
VALUES (0, "None"),
       (1, "Recruit"),
       (2, "Pilot"),
       (3, "SeniorPilot"),
       (4, "FlightLeader"),
       (5, "GroupLeader"),
       (6, "Leader");

CREATE TABLE IF NOT EXISTS guilds
(
    id   INTEGER NOT NULL PRIMARY KEY,
    name TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS guild_rank_definitions
(
    guild_id    INTEGER NOT NULL,
    role_id     INTEGER NOT NULL, --byte
    rank_name   TEXT    NOT NULL,
    permissions INTEGER NOT NULL,
    PRIMARY KEY (guild_id, role_id),
    FOREIGN KEY (guild_id)
        REFERENCES guilds (id),
    FOREIGN KEY (role_id)
        REFERENCES guild_roles (id)
);

CREATE TABLE IF NOT EXISTS guild_member_infos
(
    guild_id       INTEGER NOT NULL,
    players_id     INTEGER NOT NULL,
    guild_roles_id INTEGER NOT NULL, --byte
    PRIMARY KEY (guild_id, players_id, guild_roles_id)
);