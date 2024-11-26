# Фильмотоп

Приложение позволяет искать и оценивать фильмы, а также заводить новых друзей.

## Схема БД

Схема БД приведена ниже

![DB Schema](assets/filmorate.png)

## Запросы БД, обеспечивающие обработку методов API

1. Получение всех фильмов
    ```sql
    SELECT f.film_id,
           f.title,
           f.description,
           f.release_date,
           f.duration,
           f.mpa,
           string_agg(g.name, ',') as genres
    FROM films f
    LEFT JOIN film_genres fg ON f.film_id = fg.film_id 
    LEFT JOIN genres g ON fg.genre_id = g.genre_id
    GROUP BY f.film_id;
    ```
1. Получение фильма по id
    ```sql
    SELECT f.film_id,
           f.title,
           f.description,
           f.release_date,
           f.duration,
           f.mpa,
    string_agg(g.name, ',') as genres
    FROM films f
    LEFT JOIN film_genres fg ON f.film_id = fg.film_id
    LEFT JOIN genres g ON fg.genre_id = g.genre_id
    WHERE f.film_id = :id 
    GROUP BY f.film_id;
    ```
1. Создание фильма
    ```sql
    INSERT INTO films (title, description, release_date, duration, mpa)
    VALUES (:title, :description, :releaseDate, :duration, :mpa);
   
    INSERT INTO film_genres (film_id, genres_id) 
    VALUES (:filmId, :genresId);
    ```
1. Обновление фильма
    ```sql
    UPDATE films 
    SET title = :title, 
        description = :description, 
        release_date = :release_date, 
        duration = :duration, 
        mpa = :mpa
    WHERE film_id = :id;
    ```
1. Удаление всех фильмов
    ```sql
    DELETE FROM films;
    ```
1. Удаление фильма по id
    ```sql
    DELETE FROM films
    WHERE film_id = :id;
    ```
1. Топ 10 фильмов
    ```sql
    SELECT f.title, f.description, f.release_date, f.duration, f.mpa
    FROM films f
    INNER JOIN film_likes fl on fl.film_id = f.film_id
    GROUP BY f.title, f.description, f.release_date, f.duration, f.mpa
    ORDER BY COUNT(fl.film_id) DESC
    LIMIT :top;
    ```
1. Поставить фильму лайк
    ```sql
    INSERT INTO film_likes (film_id, user_id) 
    VALUES (:film_id, :user_id);
    ```
1. Убрать у фильма лайк
    ```sql
    DELETE FROM film_likes
    WHERE film_id = :film_id AND user_id = :user_id;
    ```
1. Получение всех пользователей
    ```sql
    SELECT u.user_id,
           u.name,
           u.email,
           u.login,
           u.birthday
    FROM users;
    ```
1. Получение пользователя по id
    ```sql
      SELECT u.user_id, 
           u.name, 
           u.email, 
           u.login, 
           u.birthday, 
           string_agg(u.friend_name, ',') AS friend_names 
    FROM (
      -- тут мы ищем друзей
      SELECT u.*, 
             friend.name AS friend_name
      FROM users u
      LEFT JOIN friendship f ON u.user_id = f.user_id -- получаем id пользователей, которым u послал запрос дружбы
      LEFT JOIN users friend ON friend.user_id = f.friend_id -- получаем пользователей, которым u послал запрос дружбы
      WHERE u.user_id = :id AND f.status = 'accepted' -- выбираем только
      
      UNION
      
      SELECT u.*, 
             friend.name AS friend_name
      FROM users u
      LEFT join friendship f ON u.user_id = f.friend_id -- получаем id пользователей, которые послали u запрос дружбы
      LEFT join users friend ON friend.user_id = f.user_id -- получаем пользователей, которые послали u запрос дружбы
      WHERE u.user_id = :id AND f.status = 'accepted'
    ) u
    GROUP BY u.user_id, u.name, u.email, u.login, u.birthday;
    ```
1. Создание пользователя
    ```sql
    INSERT INTO users (name, email, login, birthday)
    VALUES (:name, :email, :login, :birthday);
    ```
1. Обновление пользователя
    ```sql
    UPDATE users 
    SET name = :name, 
        email = :email, 
        login = :login, 
        birthday = :birthday
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
    SELECT * 
    FROM users 
    WHERE user_id IN (
      SELECT 
      CASE 
        WHEN user_id = :id 
        THEN friend_id 
        ELSE user_id 
      END AS friend_id
      FROM friendship
      WHERE (user_id = :id OR friend_id = :id) AND status = 'accepted'
    )
    ```
1. Получение общих друзей
   ```sql
   SELECT *
   FROM users
   WHERE user_id IN (
     SELECT
     CASE
     WHEN user_id = 2 THEN friend_id
     ELSE user_id
     END AS friend_id
     FROM friendship
     WHERE (user_id = 2 OR friend_id = 2) AND status = 'accepted'
   
     INTERSECT
   
     SELECT
     CASE
     WHEN user_id = 4 THEN friend_id
     ELSE user_id
     END AS friend_id
     FROM friendship
     WHERE (user_id = 4 OR friend_id = 4) AND status = 'accepted'
   );
   ```
1. Добавить друга
    ```sql
    INSERT INTO friendship (user_id, friend_id, status, requested_at, accepted_at)
    VALUES (:user_id, :friend_id, :status, :requested_at, :accepted_at);
    ```
1. Удаление из друзей 
    ```sql
    DELETE FROM friends
    WHERE (user_id = :id AND friend_id = :friendId) OR 
          (friend_id = :id AND user_id = :friendId)
    ```