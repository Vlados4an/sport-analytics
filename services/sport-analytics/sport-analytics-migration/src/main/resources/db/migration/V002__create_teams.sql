CREATE TABLE teams (
    id         SERIAL PRIMARY KEY,
    league_id  INTEGER      NOT NULL REFERENCES leagues (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    short_name VARCHAR(20),
    logo_url   TEXT,
    api_id     INTEGER      UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_teams_league_id ON teams (league_id);
