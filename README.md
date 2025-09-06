# java-filmorate
Template repository for Filmorate project.

## Схема базы данных

![Database Schema](database_schema.png)


## Примеры SQL запросов

**Получить все фильмы:**

```sql
SELECT * 
FROM films;
```

**Получить всех пользователей:**

```sql
SELECT * 
FROM users;
```

**Получение жанров фильма**

```sql
SELECT g.genre_id, g.name
FROM genres g
JOIN film_genres fg ON g.genre_id = fg.genre_id
WHERE fg.film_id = 789
ORDER BY g.genre_id;
```

**Добавление лайка фильму**

```sql
INSERT INTO likes (film_id, user_id)
VALUES (789, 123);
```

**Получение топ-10 популярных фильмов**

```sql
SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
       m.name as mpa_name, COUNT(l.user_id) as likes_count
FROM films f
JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
LEFT JOIN likes l ON f.film_id = l.film_id
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.name
ORDER BY likes_count DESC
LIMIT 10;
```

**Получение фильмов по жанру "Комедия"**

```sql
SELECT f.film_id, f.name, f.release_date, f.duration, m.name as mpa
FROM films f
JOIN film_genres fg ON f.film_id = fg.film_id
JOIN genres g ON fg.genre_id = g.genre_id
JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
WHERE g.name = 'Комедия'
ORDER BY f.release_date DESC;
```

**Удаление друга**

```sql
DELETE FROM friends 
WHERE (user_id = 123 AND friend_id = 456)
OR (user_id = 456 AND friend_id = 123);
```