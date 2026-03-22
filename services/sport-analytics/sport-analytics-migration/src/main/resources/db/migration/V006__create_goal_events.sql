CREATE TABLE goal_events (
    id          SERIAL  PRIMARY KEY,
    match_id    INTEGER NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    team_id     INTEGER NOT NULL REFERENCES teams (id),
    player_id   INTEGER REFERENCES players (id),
    minute      INTEGER NOT NULL CHECK (minute >= 0),
    is_penalty  BOOLEAN NOT NULL DEFAULT false,
    is_own_goal BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_goal_events_match_id ON goal_events (match_id);
