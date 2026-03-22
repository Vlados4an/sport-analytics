CREATE TABLE match_stats (
    id              SERIAL PRIMARY KEY,
    match_id        INTEGER        NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    team_id         INTEGER        NOT NULL REFERENCES teams (id),
    possession      DECIMAL(5, 2)  CHECK (possession >= 0 AND possession <= 100),
    shots           INTEGER        CHECK (shots >= 0),
    shots_on_target INTEGER        CHECK (shots_on_target >= 0),
    corners         INTEGER        CHECK (corners >= 0),
    fouls           INTEGER        CHECK (fouls >= 0),
    yellow_cards    INTEGER        CHECK (yellow_cards >= 0),
    red_cards       INTEGER        CHECK (red_cards >= 0),
    offsides        INTEGER        CHECK (offsides >= 0),
    passes          INTEGER        CHECK (passes >= 0),
    pass_accuracy   DECIMAL(5, 2)  CHECK (pass_accuracy >= 0 AND pass_accuracy <= 100),
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    UNIQUE (match_id, team_id)
);
CREATE INDEX idx_match_stats_match_id ON match_stats (match_id);
CREATE INDEX idx_match_stats_team_id ON match_stats (team_id);
