ALTER TABLE matches ADD COLUMN referee VARCHAR(80);
ALTER TABLE matches ADD COLUMN stats_loaded BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE matches ADD COLUMN metrics_calculated BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE matches SET stats_loaded = TRUE WHERE status = 'finished';
