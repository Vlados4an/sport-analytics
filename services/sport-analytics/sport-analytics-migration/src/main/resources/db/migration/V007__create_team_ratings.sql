CREATE TABLE team_ratings (
    id             SERIAL PRIMARY KEY,
    team_id        INTEGER        NOT NULL REFERENCES teams (id),
    league_id      INTEGER        NOT NULL REFERENCES leagues (id),
    rating         DECIMAL(6, 2)  NOT NULL CHECK (rating >= 0 AND rating <= 100),
    attack_rating  DECIMAL(6, 2),
    defense_rating DECIMAL(6, 2),
    form           DECIMAL(6, 2),
    calculated_at  TIMESTAMP      NOT NULL DEFAULT now()
);
CREATE INDEX idx_team_ratings_team_id ON team_ratings (team_id);
CREATE INDEX idx_team_ratings_league_id ON team_ratings (league_id);
