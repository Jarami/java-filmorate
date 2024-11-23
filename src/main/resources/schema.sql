DROP TABLE IF EXISTS films_genres_relation;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS genres;

CREATE TABLE IF NOT EXISTS films (
  film_id BIGSERIAL PRIMARY KEY,
  rating varchar NOT NULL,
  title varchar NOT NULL,
  description varchar(200) NOT NULL,
  release_date date NOT NULL,
  duration integer NOT NULL,

  CONSTRAINT films_rating_in_list CHECK (rating in ('G', 'PG', 'PG-13', 'R', 'NC-17')),
  CONSTRAINT films_release_date_after CHECK (release_date >= '1895-12-28'),
  CONSTRAINT films_duration_positive CHECK (duration > 0)
);
COMMENT ON TABLE films IS 'Таблица фильмов';
COMMENT ON COLUMN films.rating IS 'Рейтинг фильма MPA';
COMMENT ON COLUMN films.title IS 'Название фильма';
COMMENT ON COLUMN films.description IS 'Описание фильма';
COMMENT ON COLUMN films.release_date IS 'Дата выхода фильма';
COMMENT ON COLUMN films.duration IS 'Продолжительность фильма (в минутах)';

CREATE TABLE IF NOT EXISTS genres (
  genre_id SERIAL PRIMARY KEY,
  name varchar NOT NULL,

  CONSTRAINT genres_name_in_list CHECK (name in ('comedy', 'drama', 'cartoon', 'thriller', 'documentary', 'action'))
);
COMMENT ON TABLE genres IS 'Таблица жанров';
COMMENT ON COLUMN genres.name IS 'Название жанра';


CREATE TABLE IF NOT EXISTS films_genres_relation (
  film_id bigint REFERENCES films (film_id),
  genre_id integer REFERENCES genres (genre_id),
  PRIMARY KEY (film_id, genre_id)
);
COMMENT ON TABLE films_genres_relation IS 'Таблица связи фильмов и жанров';

CREATE TABLE IF NOT EXISTS users (
  user_id BIGSERIAL PRIMARY KEY,
  name varchar,
  email varchar NOT NULL UNIQUE,
  login varchar NOT NULL,
  birthday date NOT NULL,

--  CONSTRAINT users_login_valid CHECK (login !~ '[ \\t\\v\\b\\r\\n\\u00a0]'),
  CONSTRAINT users_birthday_valid CHECK (birthday < current_date),
  CONSTRAINT users_email_valid CHECK ( email ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' )
);
COMMENT ON TABLE users IS 'Таблица пользователей';
COMMENT ON COLUMN users.name IS 'Имя пользователя';
COMMENT ON COLUMN users.email IS 'Почта пользователя';
COMMENT ON COLUMN users.login IS 'Логин пользователя';
COMMENT ON COLUMN users.birthday IS 'Дата рождения пользователя';


CREATE TABLE IF NOT EXISTS friendship (
  friendship_id BIGINT PRIMARY KEY,
  sending_user_id bigint NOT NULL REFERENCES users (user_id),
  receiving_user_id bigint NOT NULL REFERENCES users (user_id),
  status varchar NOT NULL,
  requested_at timestamp NOT NULL,
  accepted_at timestamp,
  declined_at timestamp,
  CONSTRAINT friendship_users_differ CHECK (sending_user_id != receiving_user_id),
  CONSTRAINT friendship_unique UNIQUE (sending_user_id, receiving_user_id)
);
COMMENT ON TABLE friendship IS 'Таблица друзей';
COMMENT ON COLUMN friendship.status IS 'pending, accepted, declined';


CREATE TABLE IF NOT EXISTS film_likes (
  film_id bigint REFERENCES films (film_id),
  user_id bigint REFERENCES users (user_id),
  PRIMARY KEY (film_id, user_id)
);
