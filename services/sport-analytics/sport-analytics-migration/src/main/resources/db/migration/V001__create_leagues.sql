CREATE TABLE leagues (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    country    VARCHAR(50)  NOT NULL,
    season     VARCHAR(10)  NOT NULL,
    api_id     INTEGER      UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_leagues_country ON leagues (country);
