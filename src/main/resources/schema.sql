DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS films_genres_relation;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS film_mpa;

CREATE TABLE IF NOT EXISTS film_mpa (
    mpa_id SERIAL PRIMARY KEY,
    mpa_name VARCHAR(10) NOT NULL,

    CONSTRAINT film_mpa_name_in_list CHECK (mpa_name in ('G', 'PG', 'PG-13', 'R', 'NC-17'))
);
COMMENT ON TABLE film_mpa IS 'Таблица рейтингов MPA';
COMMENT ON COLUMN film_mpa.mpa_id IS 'Идентификатор рейтинга';
COMMENT ON COLUMN film_mpa.mpa_name IS 'Название рейтинга';

CREATE TABLE IF NOT EXISTS films (
  film_id BIGSERIAL PRIMARY KEY,
  film_name VARCHAR NOT NULL,
  description VARCHAR(200) NOT NULL,
  release_date DATE NOT NULL,
  duration INTEGER NOT NULL,
  mpa_id INTEGER REFERENCES film_mpa (mpa_id) ON DELETE CASCADE,

  CONSTRAINT films_release_date_after CHECK (release_date >= '1895-12-28'),
  CONSTRAINT films_duration_positive CHECK (duration > 0)
);
COMMENT ON TABLE films IS 'Таблица фильмов';
COMMENT ON COLUMN films.film_name IS 'Название фильма';
COMMENT ON COLUMN films.description IS 'Описание фильма';
COMMENT ON COLUMN films.release_date IS 'Дата выхода фильма';
COMMENT ON COLUMN films.duration IS 'Продолжительность фильма (в минутах)';

CREATE TABLE IF NOT EXISTS film_genres (
  genre_id SERIAL PRIMARY KEY,
  genre_name VARCHAR NOT NULL,

  CONSTRAINT genres_name_in_list CHECK (genre_name in ('Комедия', 'Драма', 'Мультфильм', 'Триллер', 'Документальный', 'Боевик'))
);
COMMENT ON TABLE film_genres IS 'Таблица жанров';
COMMENT ON COLUMN film_genres.genre_name IS 'Название жанра';


CREATE TABLE IF NOT EXISTS films_genres_relation (
  film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
  genre_id INTEGER REFERENCES film_genres (genre_id) ON DELETE CASCADE,
  PRIMARY KEY (film_id, genre_id)
);
COMMENT ON TABLE films_genres_relation IS 'Таблица связи фильмов и жанров';

CREATE TABLE IF NOT EXISTS users (
  user_id BIGSERIAL PRIMARY KEY,
  user_name VARCHAR,
  email VARCHAR NOT NULL UNIQUE,
  login VARCHAR NOT NULL,
  birthday DATE NOT NULL,

  CONSTRAINT users_birthday_valid CHECK (birthday < current_date),
  CONSTRAINT users_email_valid CHECK ( email ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' )
);
COMMENT ON TABLE users IS 'Таблица пользователей';
COMMENT ON COLUMN users.user_name IS 'Имя пользователя';
COMMENT ON COLUMN users.email IS 'Почта пользователя';
COMMENT ON COLUMN users.login IS 'Логин пользователя';
COMMENT ON COLUMN users.birthday IS 'Дата рождения пользователя';

CREATE TABLE IF NOT EXISTS friendship (
  friendship_id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  friend_id BIGINT NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  status VARCHAR NOT NULL,
  requested_at timestamp NOT NULL,
  accepted_at timestamp,
  CONSTRAINT friendship_users_differ CHECK (user_id != friend_id),
  CONSTRAINT friendship_unique UNIQUE (user_id, friend_id)
);
COMMENT ON TABLE friendship IS 'Таблица друзей';
COMMENT ON COLUMN friendship.user_id IS 'ID пользователя, отправившего запрос дружбы';
COMMENT ON COLUMN friendship.friend_id IS 'ID пользователя, кому отправили запрос дружбы';
COMMENT ON COLUMN friendship.status IS 'Статус запроса дружбы (pending, accepted, declined)';
COMMENT ON COLUMN friendship.requested_at IS 'Когда был отправлен запрос дружбы';
COMMENT ON COLUMN friendship.accepted_at IS 'Когда был принят запрос дружбы';


CREATE TABLE IF NOT EXISTS film_likes (
  film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
  PRIMARY KEY (film_id, user_id)
);
