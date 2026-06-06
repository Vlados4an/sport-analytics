ALTER TABLE matches ADD COLUMN round INTEGER;
CREATE INDEX idx_matches_round ON matches (round);
