package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.*;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

	private final ServerProperties serverProperties;
	private final ServletWebServerApplicationContext webServerAppCtxt;

	private RestClient client;

	@BeforeEach
	void init() {
		client = RestClient.create("http://localhost:" + webServerAppCtxt.getWebServer().getPort());
	}

	@Nested
	class UserTests {

		@AfterEach
		void shutdown() {
			deleteAllUsers();
		}

		@Nested
		class CreateTests {
			@Test
			void givenValidUser_whenCreate_getSuccess() {
				ResponseEntity<User> resp = createUser("mail@mail.ru;dolore;Nick Name;1946-08-20");
				User user = resp.getBody();

				assertStatus(201, resp);
				assertNotNull(user);
				assertNotNull(user.getId());
				assertEquals("mail@mail.ru", user.getEmail());
				assertEquals("dolore", user.getLogin());
				assertEquals("Nick Name", user.getName());
				assertEquals(LocalDate.parse("1946-08-20"), user.getBirthday());
			}

			@Test
			void givenUserWithoutName_whenCreate_getUserWithLoginInsteadOfName() {
				ResponseEntity<User> resp = createUser("my@email.com;login;NULL;2024-01-01");
				User user = resp.getBody();

				assertStatus(201, resp);
				assertNotNull(user);
				assertEquals("login", user.getName());
			}

			@Test
			void givenNoLogin_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("hello@main.ru;NULL;Nick Name;1946-08-20"));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore ullamco;name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("@main.ru;dolore;Nick Name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore;Nick Name;2946-08-20"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoUsersYet_whenRequest_getEmptyArray() {
				ResponseEntity<User[]> resp = getAllUsers();
				User[] users = resp.getBody();

				assertStatus(200, resp);
				assertNotNull(users);
				assertEmpty(users);
			}

			@Test
			void givenUsers_whenGetAll_getAll() {
				createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				ResponseEntity<User[]> resp = getAllUsers();
				User[] users = resp.getBody();
				Set<String> actualNames = Arrays.stream(users).map(User::getLogin).collect(Collectors.toSet());
				Set<String> expectedNames = Set.of("bob", "jack");

				assertStatus(200, resp);
				assertNotNull(users);
				assertEquals(expectedNames, actualNames);
			}

			@Test
			void givenExistingUserId_whenGetById_getUser() {
				User user = createUser("bob@mail.ru;bob;Bob;2000-08-20").getBody();
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				ResponseEntity<User> resp = getUserById(user.getId());
				assertStatus(200, resp);

				User actualUser = resp.getBody();
				assertNotNull(actualUser);
				assertUserEquals(user, actualUser);
			}

			@Test
			void givenNonExistingUserId_whenGetById_getNotFound() {
				User user = new User(1L, "my@mail.ru", "login", "name",
						LocalDate.parse("2024-01-01"), new ArrayList<>());

				assertThrows(HttpClientErrorException.NotFound.class, () -> getUserById(user.getId()));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingUser_whenUpdate_getUpdated() {
				ResponseEntity<User> resp1 = createUser("my1@email.com;login1;name1;2024-01-01");
				ResponseEntity<User> resp2 = createUser("my2@email.com;login2;name2;2024-02-01");
				User user1 = resp1.getBody();
				User user2 = resp2.getBody();

				User updatedUser = new User(user1.getId(), "my-new@email.com", "new-login", "new-name",
						LocalDate.parse("2024-02-02"), List.of(user2));

				ResponseEntity<User> resp3 = updateUser(updatedUser);
				User actualUser = resp3.getBody();
				assertUserEquals(updatedUser, actualUser);
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {
				User user = new User(1L, "my@mail.ru", "login", "name",
						LocalDate.parse("2024-01-01"), new ArrayList<>());

				assertThrows(HttpClientErrorException.NotFound.class, () -> updateUser(user));
			}

			@Test
			void givenUserWithoutName_whenUpdate_getUserWithLoginInsteadOfName() {
				User user = createUser("my@email.com;login;name;2024-01-01").getBody();
				user.setName(null);

				ResponseEntity<User> resp = updateUser(user);

				assertStatus(200, resp);
				assertNotNull(resp.getBody());
				assertEquals("login", resp.getBody().getName());
			}

			@Test
			void givenNoLogin_whenSave_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01").getBody();
				user.setLogin(null);

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateUser(user));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01").getBody();
				user.setLogin("space in login");

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateUser(user));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01").getBody();
				user.setEmail("@email.com");

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateUser(user));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01").getBody();
				user.setBirthday(LocalDate.parse("2946-08-20"));

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateUser(user));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenUsers_whenDeleteAll_getDeleted() {
				createUser("my1@email.com;login1;name1;2024-01-01");
				createUser("my2@email.com;login2;name2;2024-02-01");

				ResponseEntity<Integer> resp = deleteAllUsers();

				assertStatus(200, resp);
				assertEquals(2, resp.getBody());

				User[] users = getAllUsers().getBody();
				assertEmpty(users);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();

				deleteUser(user1);

				User[] actualUsers = getAllUsers().getBody();
				assertEquals(1, actualUsers.length);
				assertEquals("name2", actualUsers[0].getName());
			}

			@Test
			void givenNonExistingFilm_whenDelete_getNotFound() {
				User user = new User(1L, "my@mail.ru", "login", "name",
						LocalDate.parse("2024-01-01"), new ArrayList<>());

				assertThrows(HttpClientErrorException.NotFound.class, () -> deleteUser(user));
			}
		}

		@Nested
		class FriendTests {
			@Test
			void givenExistingUsers_whenAddFriends_getFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();

				addFriend(user1.getId(), user2.getId());

				ResponseEntity<User[]> respUser1friends = getFriends(user1);
				ResponseEntity<User[]> respUser2friends = getFriends(user2);

				assertStatus(200, respUser1friends);
				assertStatus(200, respUser2friends);

				List<User> user1friends = Arrays.asList(respUser1friends.getBody());
				List<User> user2friends = Arrays.asList(respUser2friends.getBody());
				assertTrue(user1friends.contains(user2));
				assertTrue(user2friends.contains(user1));
			}

			@Test
			void givenNonExistingUsers_whenAddFriends_getNoFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId(), user1.getId() + 1));

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId() + 1, user1.getId()));
			}

			@Test
			void givenExistingUsers_whenRemoveFriends_getNoFriendshipAnyMore() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();
				addFriend(user1, user2);

				removeFromFriends(user1, user2);

				ResponseEntity<User[]> respUser1friends = getFriends(user1);
				ResponseEntity<User[]> respUser2friends = getFriends(user2);

				assertStatus(200, respUser1friends);
				assertStatus(200, respUser2friends);

				List<User> user1friends = Arrays.asList(respUser1friends.getBody());
				List<User> user2friends = Arrays.asList(respUser2friends.getBody());
				assertFalse(user1friends.contains(user2));
				assertFalse(user2friends.contains(user1));
			}

			@Test
			void givenUsersWithCommonFriends_whenGetCommon_getThem() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();
				User user3 = createUser("my3@email.com;login3;name3;2024-03-01").getBody();

				addFriend(user1.getId(), user2.getId());
				addFriend(user1.getId(), user3.getId());
				addFriend(user2.getId(), user1.getId());
				addFriend(user2.getId(), user3.getId());

				ResponseEntity<User[]> respCommonFriends = getCommonFriends(user1, user2);
				user3 = getUserById(user3.getId()).getBody(); // обновляем список друзей

				assertStatus(200, respCommonFriends);

				List<User> commonFriends = Arrays.asList(respCommonFriends.getBody());

				assertEquals(1, commonFriends.size());
				assertUserEquals(user3, commonFriends.getFirst());
			}
		}

		private ResponseEntity<User[]> getAllUsers() {
			return get("/users", User[].class);
		}

		private ResponseEntity<User> getUserById(long id) {
			return get("/users/" + id, User.class);
		}

		private ResponseEntity<User> updateUser(User user) {
			return put("/users", user, User.class);
		}

		private ResponseEntity<Void> deleteUser(User user) {
			return delete("/users/" + user.getId());
		}

		private ResponseEntity<User> addFriend(User user, User friend) {
			return put("/users/" + user.getId() + "/friends/" + friend.getId(), User.class);
		}

		private ResponseEntity<User> addFriend(Long userId, Long friendId) {
			return put("/users/" + userId + "/friends/" + friendId, User.class);
		}

		private ResponseEntity<User[]> getFriends(User user) {
			return get("/users/" + user.getId() + "/friends", User[].class);
		}

		private ResponseEntity<Void> removeFromFriends(User user, User friend) {
			return delete("/users/" + user.getId() + "/friends/" + friend.getId());
		}

		private ResponseEntity<User[]> getCommonFriends(User user1, User user2) {
			return get("/users/" + user1.getId() + "/friends/common/" + user2.getId(), User[].class);
		}
	}

	@Nested
	class FilmTests {

		@AfterEach
		void shutdown() {
			deleteAllFilms();
		}

		@Nested
		class CreateTests {
			@Test
			void givenValidUser_whenCreate_getSuccess() {
				ResponseEntity<Film> resp = createFilm("name;desc;2024-01-01;120");
				Film film = resp.getBody();

				assertStatus(201, resp);
				assertNotNull(film);
				assertNotNull(film.getId());
				assertEquals("name", film.getName());
				assertEquals("desc", film.getDescription());
				assertEquals(LocalDate.parse("2024-01-01"), film.getReleaseDate());
				assertEquals(120, film.getDuration());
			}

			@Test
			void givenFilmWithoutName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("NULL;desc;2024-01-01;120"));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm(";desc;2024-01-01;120"));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {

				String tooLongDesc = "d".repeat(201);

				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;" + tooLongDesc + ";2024-01-01;120"));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;desc;2024-01-01;-1"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoFilms_whenRequest_getEmptyArray() {
				ResponseEntity<Film[]> resp = getAllFilms();
				Film[] films = resp.getBody();

				assertStatus(200, resp);
				assertNotNull(films);
				assertEmpty(films);
			}

			@Test
			void givenFilms_whenGetAll_getAll() {
				createFilm("name1;desc1;2024-01-01;120");
				createFilm("name2;desc2;2024-02-01;180");

				ResponseEntity<Film[]> resp = getAllFilms();
				Film[] films = resp.getBody();
				Set<String> actualNames = Arrays.stream(films).map(Film::getName).collect(Collectors.toSet());
				Set<String> expectedNames = Set.of("name1", "name2");

				assertStatus(200, resp);
				assertNotNull(films);
				assertEquals(expectedNames, actualNames);
			}

			@Test
			void givenExistingFilmId_whenGetById_getIt() {
				Film film = createFilm("name;desc;2024-01-01;120").getBody();
				createFilm("name;desc;2024-01-01;120");

				ResponseEntity<Film> resp = getFilmById(film.getId());
				assertStatus(200, resp);

				Film actualFilm = resp.getBody();
				assertNotNull(actualFilm);
				assertFilmEquals(film, actualFilm);
			}

			@Test
			void givenNonExistingFilm_whenGetById_getNotFound() {
				Film film = new Film(1L, "name", "desc", "G", LocalDate.parse("2024-02-02"),
						180);

				assertThrows(HttpClientErrorException.NotFound.class, () -> getFilmById(film.getId()));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingFilm_whenUpdate_getUpdated() {
				ResponseEntity<Film> resp1 = createFilm("name1;desc1;2024-01-01;120");
				ResponseEntity<Film> resp2 = createFilm("name2;desc2;2024-02-01;180");
				Film film1 = resp1.getBody();
				Film film2 = resp2.getBody();

				Film updatedFilm = new Film(film1.getId(), "name", "desc", "G",
						LocalDate.parse("2024-02-02"), 180);

				ResponseEntity<Film> resp3 = updateFilm(updatedFilm);
				Film actualFilm = resp3.getBody();
				assertFilmEquals(updatedFilm, actualFilm);
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {
				Film film = new Film(1L, "name", "desc", "G",
						LocalDate.parse("2024-02-02"), 180);

				assertThrows(HttpClientErrorException.NotFound.class, () -> updateFilm(film));
			}

			@Test
			void givenFilmWithoutName_whenUpdate_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120").getBody();
				film.setName(null);

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateFilm(film));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120").getBody();
				film.setName("");

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateFilm(film));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120").getBody();
				film.setDescription("d".repeat(201));

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateFilm(film));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120").getBody();
				film.setDuration(-1);

				assertThrows(HttpClientErrorException.BadRequest.class, () -> updateFilm(film));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenFilms_whenDeleteAll_getDeleted() {
				createFilm("name1;desc1;2024-01-01;120");
				createFilm("name2;desc2;2024-02-01;180");

				ResponseEntity<Integer> resp = deleteAllFilms();

				assertStatus(200, resp);
				assertEquals(2, resp.getBody());

				Film[] films = getAllFilms().getBody();
				assertEmpty(films);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				Film film1 = createFilm("name1;desc1;2024-01-01;120").getBody();
				Film film2 = createFilm("name2;desc2;2024-02-01;180").getBody();

				deleteFilm(film1);

				Film[] actualFilms = getAllFilms().getBody();
				assertEquals(1, actualFilms.length);
				assertEquals("name2", actualFilms[0].getName());
			}

			@Test
			void givenNonExistingFilm_whenDelete_getNotFound() {
				Film film = new Film(1L, "name", "desc", "G",
						LocalDate.parse("2024-01-01"), 120);

				assertThrows(HttpClientErrorException.NotFound.class, () -> deleteFilm(film));
			}
		}

		private ResponseEntity<Film[]> getAllFilms() {
			return get("/films", Film[].class);
		}

		private ResponseEntity<Film> updateFilm(Film film) {
			return put("/films", film, Film.class);
		}

		private ResponseEntity<Void> deleteFilm(Film film) {
			return delete("/films/" + film.getId());
		}
	}

	@Nested
	class LikeTests {

		List<User> users;
		List<Film> films;

		@BeforeEach
		void setup() {

			users = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				User user = User.builder()
						.name("name" + i)
						.login("login" + i)
						.email("mail" + i + "@mail.com")
						.birthday(LocalDate.parse("2000-01-01"))
						.friendsId(new HashSet<>())
						.build();

				users.add(createUser(user).getBody());
			}

			films = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				Film film = Film.builder()
						.name("film name " + i)
						.description("film desc " + i)
						.releaseDate(LocalDate.parse("2010-01-01"))
						.duration(10 * i + 10)
						.rate(0)
						.build();

				films.add(createFilm(film).getBody());
			}
		}

		@AfterEach
		void shutdown() {
			deleteAllUsers();
			deleteAllFilms();
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenGetLikeCount_gotZero() {
			for (Film film : films) {
				assertEquals(0, film.getRate());
			}
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenLike_gotLikeCountIncrease() {

			ResponseEntity<Film> resp = like(films.get(7), users.get(3));

			assertStatus(200, resp);

			Film film = resp.getBody();
			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenLike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			like(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId()).getBody();
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenDislike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			ResponseEntity<Film> resp = dislike(films.get(7), users.get(8));

			assertStatus(200, resp);

			Film film = resp.getBody();
			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenDislike_gotLikeCountDecrease() {
			like(films.get(7), users.get(3));
			dislike(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId()).getBody();
			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(0, film.getRate());
		}

		@Test
		void givenNoLikesAtAll_whenAskDefaultPopular_getTenRandomFilms() {
			ResponseEntity<Film[]> filmsResp = getPopularFilms();
			Film[] films = filmsResp.getBody();

			assertStatus(200, filmsResp);
			assertNotNull(films);
			assertEquals(10, films.length);
		}

		@Test
		void givenNoLikesAtAll_whenAskPopularFive_getFiveRandomFilms() {
			ResponseEntity<Film[]> filmsResp = getPopularFilms(5);
			Film[] films = filmsResp.getBody();

			assertStatus(200, filmsResp);
			assertNotNull(films);
			assertEquals(5, films.length);
		}

		@Test
		void givenNineFilmsLiked_whenAskPopular10_getPopular() {
			// 9-ый фильм лайкаем 9 раз, 8-ой - 8 раз и т.д.
			for (int j = 9; j > 0; j--) {
				for (int i = 0; i < j; i++) {
					like(films.get(j), users.get(i));
				}
			}

			Film[] films = getPopularFilms(10).getBody();
			assertEquals(9, films[0].getRate());
			assertEquals(8, films[1].getRate());
			assertEquals(7, films[2].getRate());
			assertEquals(6, films[3].getRate());
			assertEquals(5, films[4].getRate());
			assertEquals(4, films[5].getRate());
			assertEquals(3, films[6].getRate());
			assertEquals(2, films[7].getRate());
			assertEquals(1, films[8].getRate());
			assertEquals(0, films[9].getRate());
		}
	}

	private ResponseEntity<User> createUser(String userString) {
		return createUser(parseUser(userString));
	}

	private ResponseEntity<User> createUser(User user) {
		return post("/users", user, User.class);
	}

	private ResponseEntity<Film> createFilm(String filmString) {
		return createFilm(parseFilm(filmString));
	}

	private ResponseEntity<Film> createFilm(Film film) {
		return post("/films", film, Film.class);
	}

	private ResponseEntity<Film> getFilmById(long id) {
		return get("/films/" + id, Film.class);
	}

	private ResponseEntity<Integer> deleteAllUsers() {
		return delete("/users", Integer.class);
	}

	private ResponseEntity<Integer> deleteAllFilms() {
		return delete("/films", Integer.class);
	}

	private ResponseEntity<Film> like(Film film, User user) {
		return put("/films/" + film.getId() + "/like/" + user.getId(), Film.class);
	}

	private ResponseEntity<Film> dislike(Film film, User user) {
		return delete("/films/" + film.getId() + "/like/" + user.getId(), Film.class);
	}

	private ResponseEntity<Film[]> getPopularFilms() {
		return get("/films/popular", Film[].class);
	}

	private ResponseEntity<Film[]> getPopularFilms(int count) {
		return get("/films/popular?count=" + count, Film[].class);
	}

	private User parseUser(String userString) {
		String[] chunks = userString.split(";");
		return new User(
				chunks[0],
				chunks[1].equals("NULL") ? null : chunks[1],
				chunks[2].equals("NULL") ? null : chunks[2],
				LocalDate.parse(chunks[3])
		);
	}

	private Film parseFilm(String filmString) {
		String[] chunks = filmString.split(";");
		return new Film(
				null,
				chunks[0].equals("NULL") ? null : chunks[0],
				chunks[1].equals("NULL") ? null : chunks[1],
				chunks[2],
				chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]),
				Integer.parseInt(chunks[4])
		);
	}

	private <T> ResponseEntity<T> get(String uri, Class<T> clazz) {
		log.info("get {}", uri);
		return client.get().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> post(String uri, Object body, Class<T> clazz) {
		log.info("post {}", uri);
		return client.post().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> put(String uri, Object body, Class<T> clazz) {
		log.info("put {}", uri);
		return client.put().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> put(String uri, Class<T> clazz) {
		log.info("put {}", uri);
		return client.put().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> delete(String uri, Class<T> clazz) {
		log.info("delete {}", uri);
		return client.delete().uri(uri).retrieve().toEntity(clazz);
	}

	private ResponseEntity<Void> delete(String uri) {
		log.info("delete {}", uri);
		return client.delete().uri(uri).retrieve().toBodilessEntity();
	}

	private void assertStatus(int statusCode, ResponseEntity<?> resp) {
		assertEquals(HttpStatusCode.valueOf(statusCode), resp.getStatusCode());
	}

}
