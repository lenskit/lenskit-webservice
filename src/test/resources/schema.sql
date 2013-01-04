DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
    id BIGINT not null,
    user BIGINT not null,
    item BIGINT not null,
    rating REAL,
    timestamp BIGINT,
    revision TEXT not null,
    primary key (id)
);
