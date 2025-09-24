MERGE INTO mpa_ratings (id, name) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

MERGE INTO genres (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

INSERT INTO event_types (name) VALUES
('LIKE'),
('REVIEW'),
('FRIEND');

INSERT INTO operations (name) VALUES
 ('ADD'),
 ('REMOVE'),
 ('UPDATE');
