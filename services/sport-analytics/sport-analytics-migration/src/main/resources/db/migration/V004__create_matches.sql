CREATE TABLE matches (
    id             SERIAL PRIMARY KEY,
    league_id      INTEGER      NOT NULL REFERENCES leagues (id),
    home_team_id   INTEGER      NOT NULL REFERENCES teams (id),
    away_team_id   INTEGER      NOT NULL REFERENCES teams (id),
    match_date     TIMESTAMP    NOT NULL,
    stadium        VARCHAR(100),
    attendance     INTEGER,
    home_score     INTEGER      CHECK (home_score >= 0),
    away_score     INTEGER      CHECK (away_score >= 0),
    status         VARCHAR(20)  NOT NULL DEFAULT 'scheduled',
    api_id         INTEGER      UNIQUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_matches_league_id ON matches (league_id);
CREATE INDEX idx_matches_match_date ON matches (match_date);
CREATE INDEX idx_matches_home_team_id ON matches (home_team_id);
CREATE INDEX idx_matches_away_team_id ON matches (away_team_id);
