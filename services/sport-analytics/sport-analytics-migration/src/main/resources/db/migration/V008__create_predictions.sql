CREATE TABLE predictions (
    id                   SERIAL PRIMARY KEY,
    match_id             INTEGER        NOT NULL REFERENCES matches (id),
    home_win_prob        DECIMAL(4, 3)  NOT NULL CHECK (home_win_prob >= 0 AND home_win_prob <= 1),
    draw_prob            DECIMAL(4, 3)  NOT NULL CHECK (draw_prob >= 0 AND draw_prob <= 1),
    away_win_prob        DECIMAL(4, 3)  NOT NULL CHECK (away_win_prob >= 0 AND away_win_prob <= 1),
    predicted_home_score INTEGER        NOT NULL CHECK (predicted_home_score >= 0),
    predicted_away_score INTEGER        NOT NULL CHECK (predicted_away_score >= 0),
    model_version        VARCHAR(20)    NOT NULL DEFAULT 'dixon-coles-xg-v1',
    created_at           TIMESTAMP      NOT NULL DEFAULT now()
);
CREATE INDEX idx_predictions_match_id ON predictions (match_id);
