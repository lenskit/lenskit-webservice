-- Script to set up SQL table for unit tests
-- Note that this script depends on a very specifc table layout.
-- If column names or layout change, this script becomes invalid!

INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (5, 23, 256, 4.0, 20000, '4b297694-bb35-42d5-80f3-ff1d89ad20dc');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (10, 735, 256, 3.5, 48000, '2ddada40-f5f5-4e51-8e5b-73f8666ef842');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (15, 23, 32, 4.4, 32000, '22bf4033-d9cf-49e7-9733-30bd6b5975da');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (25, 491, 2048, 5.0, 14000, 'b9e37828-a11a-4580-b7ed-605889987274');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (35, 306, 256, 3.8, 23000, 'baaf010c-5c44-48f1-97cc-68b5eab1f95b');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (70, 938, 512, 4.7, 82000, '8e7d1ef6-190e-45eb-a2df-807530f12611');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (75, 23, 1024, 5.0, 93000, 'd91a3c49-ca10-4273-88ad-b254578ac6f2');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (95, 23, 8192, 3.8, 37000, '1048416b-b6b9-4007-8457-8dae0a139d5e');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (45, 837, 256, 2.9, 83000, '3ccc0b0a-dfed-4dbc-a7af-e10131ffabbe');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (40, 294, 256, 1.5, 90000, '045fa69b-b416-4bf6-ad8b-e97b9becf806');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (85, 23, 256, null, 130000, '377bc200-8780-4baa-a921-ddd990a6075b');
INSERT INTO ratings (id, user, item, rating, timestamp, revision) VALUES (80, 23, 8192, null, 150000, '80929495-8aab-49c5-8ce1-0ced6bc9c520');
