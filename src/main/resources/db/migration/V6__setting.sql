CREATE TABLE IF NOT EXISTS settings_value_types
(
    id   INTEGER NOT NULL PRIMARY KEY,
    type TEXT    NOT NULL
);

INSERT INTO settings_value_types(id, type)
VALUES (0, "Unknown"),
       (1, "Float"),
       (2, "Boolean"),
       (3, "Integer"),
       (4, "Float2"),
       (5, "HelpScreenType"),
       (6, "Byte");

-- for Float, Boolean, Integer, Byte
CREATE TABLE IF NOT EXISTS players_settings_value_bytes
(
    players_id      INTEGER NOT NULL,
    user_setting_id INTEGER NOT NULL,
    value           REAL    NOT NULL,
    PRIMARY KEY (players_id, user_setting_id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);

CREATE TABLE IF NOT EXISTS input_bindings_values
(
    players_id          INTEGER NOT NULL,
    action_id           INTEGER NOT NULL,
    device_trigger_code INTEGER NOT NULL,
    device_mod_code     INTEGER NOT NULL,
    device              INTEGER NOT NULL,
    flags               INTEGER NOT NULL,
    profile_number      INTEGER NOT NULL,

    PRIMARY KEY (players_id, action_id),
    FOREIGN KEY (players_id)
        REFERENCES players (id)
);