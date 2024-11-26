# Фильмотоп

Приложение позволяет искать и оценивать фильмы, а также заводить новых друзей.

## Схема БД

Схема БД приведена ниже

![DB Schema](assets/filmorate.png)

## Запросы БД, обеспечивающие обработку методов API

1. Получение всех фильмов
    ```sql
   SELECT f.film_id as "film_id",
          f.film_name as "film_name",
          f.description as "description",
          f.release_date as "release_date",
          f.duration as "duration",
          fr.mpa_id as "mpa_id",
          fr.mpa_name as "mpa_name",
          count(fl.film_id) as "rate"
   FROM films f
   INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
   LEFT JOIN film_likes fl ON fl.film_id = f.film_id
   GROUP BY f.film_id, fr.mpa_id;
    ```
1. Получение фильма по id
    ```sql
   SELECT f.film_id as "film_id",
          f.film_name as "film_name",
          f.description as "description",
          f.release_date as "release_date",
          f.duration as "duration",
          fr.mpa_id as "mpa_id",
          fr.mpa_name as "mpa_name",
          count(fl.film_id) as "rate"
   FROM films f
   INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
   LEFT JOIN film_likes fl ON fl.film_id = f.film_id
   WHERE f.film_id = :filmId
   GROUP BY f.film_id, fr.mpa_id;
    ```
1. Создание фильма
    ```sql
    INSERT INTO films(film_name, description, release_date, duration, mpa_id)
    VALUES (:name, :description, :releaseDate, :duration, :mpaId);
   
    INSERT INTO film_genres (film_id, genres_id) 
    VALUES (:filmId, :genresId);
    ```
1. Обновление фильма
    ```sql
   UPDATE films
   SET film_name = :name,
       description = :description,
       release_date = :releaseDate,
       duration = :duration,
       mpa_id = :mpaId
   WHERE film_id = :filmId;
    ```
1. Удаление всех фильмов
    ```sql
    DELETE FROM films;
    ```
1. Удаление фильма по id
    ```sql
   DELETE FROM films
   WHERE film_id = :filmId;
    ```
1. Топ 10 фильмов
    ```sql
   SELECT f.film_id as "film_id",
          f.film_name as "film_name",
          f.description as "description",
          f.release_date as "release_date",
          f.duration as "duration",
          fr.mpa_id as "mpa_id",
          fr.mpa_name as "mpa_name",
          count(fl.film_id) as "rate"
   FROM films f
   INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
   LEFT JOIN film_likes fl ON fl.film_id = f.film_id
   GROUP BY f.film_id, fr.mpa_id
   ORDER BY count(fl.film_id) desc
   LIMIT :count;
    ```
1. Поставить фильму лайк
    ```sql
   INSERT INTO film_likes (film_id, user_id)
   VALUES (:filmId, :userId);
    ```
1. Убрать у фильма лайк
    ```sql
   DELETE FROM film_likes
   WHERE film_id = :filmId AND user_id = :userId;
    ```
1. Получение всех пользователей
    ```sql
   SELECT user_id as "user_id",
          user_name as "user_name",
          email as "email",
          login as "login",
          birthday as "birthday"
   FROM users;
    ```
1. Получение пользователя по id
    ```sql
   SELECT * FROM users
   WHERE user_id = :id;
    ```
1. Создание пользователя
    ```sql
   INSERT INTO users(user_name, email, login, birthday)
   VALUES (:name, :email, :login, :birthday);
    ```
1. Обновление пользователя
    ```sql
   UPDATE users SET user_name = :name, email = :email, login = :login, birthday = :birthday
   WHERE user_id = :id;
    ```
1. Удаление всех пользователей
    ```sql
    DELETE FROM users;
    ```
1. Удаление пользователя по id
    ```sql
   DELETE FROM users
   WHERE user_id = :id;
    ```
1. Получение друзей пользователя
    ```sql
   SELECT friend_id
   FROM friendship
   WHERE user_id = :userId

   UNION

   SELECT user_id
   FROM friendship
   WHERE friend_id = :userId AND status = 'accepted'
    ```
1. Получение общих друзей
   ```sql
     (SELECT friend_id
     FROM friendship
     WHERE user_id = :id1
   
     UNION
   
     SELECT user_id
     FROM friendship
     WHERE friend_id = :id1 AND status = 'accepted')
   
     INTERSECT
   
     (SELECT friend_id
     FROM friendship
     WHERE user_id = :id2
   
     UNION
   
     SELECT user_id
     FROM friendship
     WHERE friend_id = :id2 AND status = 'accepted');
   ```
1. Добавить друга
    ```sql
    INSERT INTO friendship (friend_id, user_id, status, requested_at)
     VALUES (:friendId, :userId, :status, :requestedAt);
    ```
1. Удаление из друзей 
    ```sql
   DELETE FROM friendship
   WHERE friend_id = :friendId AND user_id = :userId
    ```