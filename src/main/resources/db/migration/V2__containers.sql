CREATE TABLE IF NOT EXISTS container_types
(
    id   INTEGER NOT NULL PRIMARY KEY,
    type TEXT    NOT NULL
);

INSERT INTO container_types(id, type)
VALUES (1, "Hold"),
       (2, "Locker"),
       (3, "ShipSlot"),
       (4, "Shop"),
       (5, "Loot"),
       (6, "BlackHole"),
       (7, "Mail"),
       (8, "EventShop");



CREATE TABLE IF NOT EXISTS containers
(
    container_types_id INTEGER NOT NULL,
    players_id         INTEGER NOT NULL,
    PRIMARY KEY (container_types_id, players_id),
    FOREIGN KEY (container_types_id)
        REFERENCES container_types (id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS ship_systems
(
    players_id    INTEGER NOT NULL,
    containers_id INTEGER NOT NULL,
    server_id     INTEGER NOT NULL,
    guid          INTEGER NOT NULL,
    durability    REAL    NOT NULL,
    PRIMARY KEY (players_id, containers_id, server_id, guid),
    FOREIGN KEY (players_id)
        REFERENCES players (id),
    FOREIGN KEY (containers_id)
        REFERENCES containers (id)
);

CREATE TABLE IF NOT EXISTS item_countables
(
    players_id    INTEGER NOT NULL,
    containers_id INTEGER NOT NULL,
    server_id     INTEGER NOT NULL,
    guid          INTEGER NOT NULL,
    count         INTEGER NOT NULL,
    PRIMARY KEY (players_id, containers_id, server_id, guid),
    FOREIGN KEY (players_id)
        REFERENCES players (id),
    FOREIGN KEY (containers_id)
        REFERENCES containers (id)
);