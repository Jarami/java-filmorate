DROP TABLE IF EXISTS film_review_rates;
DROP TABLE IF EXISTS film_reviews;
DROP TABLE IF EXISTS films_directors;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS films_genres_relation;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS film_mpa;
DROP TABLE IF EXISTS events CASCADE;
DROP TYPE IF EXISTS event_type CASCADE;
DROP TYPE IF EXISTS operation_type CASCADE;

CREATE TABLE IF NOT EXISTS film_mpa (
    mpa_id SERIAL PRIMARY KEY,
    mpa_name VARCHAR(10) NOT NULL UNIQUE,

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
  genre_name VARCHAR NOT NULL UNIQUE,

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

CREATE TABLE IF NOT EXISTS film_reviews (
  review_id BIGSERIAL PRIMARY KEY,
  film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  is_positive BOOLEAN NOT NULL
);
COMMENT ON TABLE film_reviews IS 'Таблица отзывов';
COMMENT ON COLUMN film_reviews.user_id IS 'ID пользователя, написавшего отзыв';
COMMENT ON COLUMN film_reviews.film_id IS 'ID фильма, к которому написан отзыв';
COMMENT ON COLUMN film_reviews.content IS 'Содержимое отзыва';
COMMENT ON COLUMN film_reviews.is_positive IS 'Положительный ли отзыв';

CREATE TABLE IF NOT EXISTS film_review_rates (
    review_rate_id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES film_reviews (review_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    rate INTEGER NOT NULL,
    CONSTRAINT film_review_rates_unique UNIQUE (review_id, user_id)
);
COMMENT ON TABLE film_review_rates IS 'Таблица отзывов';
COMMENT ON COLUMN film_review_rates.review_id IS 'ID отзыва, которому поставлен лайк/дизлайк';
COMMENT ON COLUMN film_review_rates.user_id IS 'ID пользователя, поставившего лайк/дизлайк';
COMMENT ON COLUMN film_review_rates.rate IS 'рейтинг (+1 - если лайк, и -1 если дизлайк)';

CREATE TYPE event_type AS ENUM ('LIKE', 'REVIEW', 'FRIEND');
CREATE TYPE operation_type AS ENUM ('REMOVE', 'ADD', 'UPDATE');

CREATE TABLE IF NOT EXISTS events (
    event_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    event_timestamp timestamp NOT NULL,
    user_id bigint NOT NULL,
    event event_type NOT NULL,
    operation operation_type NOT NULL,
    entity_id bigint NOT NULL,
    CONSTRAINT fk_events_user_id FOREIGN KEY(user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS directors (
    director_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);
COMMENT ON TABLE directors IS 'Таблица режиссеров';
COMMENT ON COLUMN directors.director_id IS 'Идентификатор режиссера';
COMMENT ON COLUMN directors.name IS 'Имя режиссера';


CREATE TABLE IF NOT EXISTS films_directors (
    film_id BIGINT REFERENCES films (film_id) ON DELETE CASCADE,
    director_id INTEGER REFERENCES directors (director_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);
COMMENT ON TABLE films_directors IS 'Связь таблиц фильмов и режиссеров';
COMMENT ON COLUMN films_directors.film_id IS 'Идентификатор фильма';
COMMENT ON COLUMN films_directors.director_id IS 'Идентификатор режиссера';
