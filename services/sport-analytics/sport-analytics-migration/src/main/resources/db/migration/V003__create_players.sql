CREATE TABLE players (
    id         SERIAL PRIMARY KEY,
    team_id    INTEGER      NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    position   VARCHAR(25),
    number     INTEGER,
    api_id     INTEGER      UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_players_team_id ON players (team_id);
