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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.*;

@Slf4j
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

	private final ServerProperties serverProperties;
	private final ServletWebServerApplicationContext webServerAppCtxt;

	private RestClient client;
	private List<FilmGenre> allGenres;
	private List<FilmMpa> allMpa;
	private Map<String, FilmGenre> genreByName;
	private Map<Integer, FilmGenre> genreById;
	private Map<String, FilmMpa> mpaByName;
	private Map<Integer, FilmMpa> mpaById;

	@BeforeEach
	void init() {
		client = RestClient.create("http://localhost:" + webServerAppCtxt.getWebServer().getPort());
		allGenres = getAllGenres();
		allMpa = getAllMpa();
		genreByName = new HashMap<>();
		allGenres.forEach(g -> genreByName.put(g.getName(), g));
		mpaByName = new HashMap<>();
		allMpa.forEach(m -> mpaByName.put(m.getName(), m));
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
				User user = createUser("mail@mail.ru;dolore;Nick Name;1946-08-20");

				assertNotNull(user);
				assertNotNull(user.getId());
				assertEquals("mail@mail.ru", user.getEmail());
				assertEquals("dolore", user.getLogin());
				assertEquals("Nick Name", user.getName());
				assertEquals("1946-08-20", user.getBirthday().toString());
			}

			@Test
			void givenUserWithoutName_whenCreate_getUserWithLoginInsteadOfName() {
				User user = createUser("my@email.com;login;NULL;2024-01-01");

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
				User[] users = getAllUsers();

				assertNotNull(users);
				assertEmpty(users);
			}

			@Test
			void givenUsers_whenGetAll_getAll() {
				createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				User[] users = getAllUsers();

				Set<String> actualLogins = Arrays.stream(users).map(User::getLogin).collect(Collectors.toSet());
				Set<String> expectedLogins = Set.of("bob", "jack");

				assertNotNull(users);
				assertEquals(expectedLogins, actualLogins);
			}

			@Test
			void givenExistingUserId_whenGetById_getUser() {
				User user = createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				User actualUser = getUserById(user.getId());

				assertNotNull(actualUser);
				assertUserEquals(user, actualUser);
			}

			@Test
			void givenNonExistingUserId_whenGetById_getNotFound() {
				User user = User.builder()
						.id(1L)
						.email("my@mail.ru")
						.login("login")
						.name("name")
						.birthday(LocalDate.parse("2024-01-01"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> getUserById(user.getId()));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingUser_whenUpdate_getUpdated() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				UpdateUserRequest updatedUser = UpdateUserRequest.builder()
						.id(user1.getId())
						.email("my-new@email.com")
						.login("new-login")
						.name("new-name")
						.birthday(LocalDate.parse("2024-02-02"))
						.build();

				User actualUser = updateUser(updatedUser);
				assertUserEquals(updatedUser, actualUser);
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {
				UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
						.id(1L)
						.email("my@mail.ru")
						.login("login")
						.name("name")
						.birthday(LocalDate.parse("2024-01-01"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenNoLogin_whenSave_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.login(null)
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.login("space in login")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.email("@email.com")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");
				user.setBirthday(LocalDate.parse("2946-08-20"));

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.birthday(LocalDate.parse("2946-08-20"))
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenUsers_whenDeleteAll_getDeleted() {
				createUser("my1@email.com;login1;name1;2024-01-01");
				createUser("my2@email.com;login2;name2;2024-02-01");

				Integer deletedUsers = deleteAllUsers();

				assertEquals(2, deletedUsers);

				User[] users = getAllUsers();
				assertEmpty(users);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				deleteUser(user1.getId());

				User[] actualUsers = getAllUsers();
				assertEquals(1, actualUsers.length);
				assertEquals("name2", actualUsers[0].getName());
			}

			@Test
			void givenNonExistingUser_whenDelete_getNotFound() {
				User user = createUser("my1@email.com;login1;name1;2024-01-01");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> deleteUser(user.getId() + 1));
			}
		}

		@Nested
		class FriendTests {
			@Test
			void givenExistingUsers_whenAddFriends_getFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				addFriend(user1.getId(), user2.getId());

				List<User> user1friends = Arrays.asList(getFriends(user1));
				List<User> user2friends = Arrays.asList(getFriends(user2));

				assertTrue(user1friends.contains(user2));
				assertFalse(user2friends.contains(user1));
			}

			@Test
			void givenNonExistingUsers_whenAddFriends_getNoFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId(), user1.getId() + 1));

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId() + 1, user1.getId()));
			}

			@Test
			void givenExistingUsers_whenRemoveFriends_getNoFriendshipAnyMore() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
				addFriend(user1, user2);

				removeFromFriends(user1, user2);

				List<User> user1friends = Arrays.asList(getFriends(user1));

				assertFalse(user1friends.contains(user2));
			}

			@Test
			void givenUsersWithCommonFriends_whenGetCommon_getThem() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
				User user3 = createUser("my3@email.com;login3;name3;2024-03-01");

				addFriend(user1.getId(), user2.getId());
				addFriend(user1.getId(), user3.getId());
				addFriend(user2.getId(), user1.getId());
				addFriend(user2.getId(), user3.getId());

				List<User> commonFriends = Arrays.asList(getCommonFriends(user1, user2));

				assertEquals(1, commonFriends.size());
				assertEquals(user3.getId(), commonFriends.getFirst().getId());
			}
		}

		private User[] getAllUsers() {
			return get("/users", User[].class).getBody();
		}

		private User getUserById(long id) {
			return get("/users/" + id, User.class).getBody();
		}

		private User updateUser(String userString) {
			UpdateUserRequest updateUserRequest = parseUpdateUserRequest(userString);
			return updateUser(updateUserRequest);
		}

		private User updateUser(UpdateUserRequest updateUserRequest) {
			return put("/users", updateUserRequest, User.class).getBody();
		}

		private Void deleteUser(long id) {
			return delete("/users/" + id).getBody();
		}

		private User addFriend(User user, User friend) {
			return put("/users/" + user.getId() + "/friends/" + friend.getId(), User.class).getBody();
		}

		private User addFriend(Long userId, Long friendId) {
			return put("/users/" + userId + "/friends/" + friendId, User.class).getBody();
		}

		private User[] getFriends(User user) {
			return get("/users/" + user.getId() + "/friends", User[].class).getBody();
		}

		private Void removeFromFriends(User user, User friend) {
			return delete("/users/" + user.getId() + "/friends/" + friend.getId()).getBody();
		}

		private User[] getCommonFriends(User user1, User user2) {
			return get("/users/" + user1.getId() + "/friends/common/" + user2.getId(), User[].class).getBody();
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

				Film film = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				assertNotNull(film);
				assertNotNull(film.getId());
				assertEquals("name", film.getName());
				assertEquals("desc", film.getDescription());
				assertEquals("2024-01-01", film.getReleaseDate().toString());
				assertEquals("G", film.getMpa().getName());
				assertEquals(Set.of("Комедия","Драма"), getGenreNames(film));
				assertEquals(120, film.getDuration());
			}

			@Test
			void givenFilmWithoutName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("NULL;desc;2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm(";desc;2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {

				String tooLongDesc = "d".repeat(201);

				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;" + tooLongDesc + ";2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;desc;2024-01-01;-1;G;Комедия,Драма"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoFilms_whenRequest_getEmptyArray() {
				Film[] films = getAllFilms();

				assertNotNull(films);
				assertEmpty(films);
			}

			@Test
			void givenFilms_whenGetAll_getAll() {
				createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				Film[] films = getAllFilms();
				Set<String> actualNames = Arrays.stream(films).map(Film::getName).collect(Collectors.toSet());
				Set<String> expectedNames = Set.of("name1", "name2");

				assertNotNull(films);
				assertEquals(expectedNames, actualNames);
			}

			@Test
			void givenExistingFilmId_whenGetById_getIt() {
				Film film1 = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				Film actualFilm = getFilmById(film1.getId());

				assertNotNull(actualFilm);
				assertFilmEquals(film1, actualFilm);
			}

			@Test
			void givenNonExistingFilm_whenGetById_getNotFound() {
				Film film = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> getFilmById(film.getId() + 1));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingFilm_whenUpdate_getUpdated() {
				Film film1 = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
						.id(film1.getId())
						.name("name")
						.description("desc")
						.mpa(mpaDto("PG"))
						.genres(genreDto("Комедия,Триллер"))
						.releaseDate(LocalDate.parse("2024-05-01"))
						.build();

				updateFilm(updateFilmRequest);

				Film updatedFilm = getFilmById(film1.getId());

				assertEquals("name", updatedFilm.getName());
				assertEquals("desc", updatedFilm.getDescription());
				assertEquals("PG", updatedFilm.getMpa().getName());
				assertEquals(Set.of("Комедия","Триллер"), getGenreNames(updatedFilm));
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
						.id(1L)
						.name("name")
						.description("desc")
						.releaseDate(LocalDate.parse("2024-02-02"))
						.duration(180)
						.mpa(mpaDto("G"))
						.genres(genreDto("Драма,Документальный"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithoutName_whenUpdate_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
					.name(null)
					.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.name("")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.description("d".repeat(201))
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.duration(-1)
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenFilms_whenDeleteAll_getDeleted() {
				createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				Integer deletedFilms = deleteAllFilms();

				assertEquals(2, deletedFilms);

				Film[] films = getAllFilms();
				assertEmpty(films);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				Film film1 = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				deleteFilm(film1.getId());

				Film[] actualFilms = getAllFilms();
				assertEquals(1, actualFilms.length);
				assertEquals("name2", actualFilms[0].getName());
			}

			@Test
			void givenNonExistingFilm_whenDelete_getNotFound() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> deleteFilm(film.getId() + 1));
			}
		}

		private Film[] getAllFilms() {
			return get("/films", Film[].class).getBody();
		}

		private ResponseEntity<Film> updateFilm(UpdateFilmRequest updateFilmRequest) {
			return put("/films", updateFilmRequest, Film.class);
		}

		private ResponseEntity<Void> deleteFilm(long id) {
			return delete("/films/" + id);
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
				NewUserRequest newUserRequest = NewUserRequest.builder()
						.name("name" + i)
						.login("login" + i)
						.email("mail" + i + "@mail.com")
						.birthday(LocalDate.parse("2000-01-01"))
						.build();

				users.add(createUser(newUserRequest));
			}

			films = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				NewFilmRequest newFilmRequest = NewFilmRequest.builder()
						.name("film name " + i)
						.description("film desc " + i)
						.releaseDate(LocalDate.parse("2010-01-01"))
						.duration(10 * i + 10)
						.mpa(mpaDto("G"))
						.genres(genreDto("Комедия"))
						.build();

				films.add(createFilm(newFilmRequest));
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

			ResponseDto responseDto = like(films.get(7), users.get(3));
			Film film = getFilmById(films.get(7).getId());

			assertTrue(responseDto.isSuccess());
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenLike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			like(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenDislike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			ResponseDto resp = dislike(films.get(7), users.get(8));
			assertFalse(resp.isSuccess());

			Film film = getFilmById(films.get(7).getId());

			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenDislike_gotLikeCountDecrease() {
			like(films.get(7), users.get(3));
			dislike(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId());
			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(0, film.getRate());
		}

		@Test
		void givenNoLikesAtAll_whenAskDefaultPopular_getTenRandomFilms() {
			Film[] films = getPopularFilms();

			assertNotNull(films);
			assertEquals(10, films.length);
		}

		@Test
		void givenNoLikesAtAll_whenAskPopularFive_getFiveRandomFilms() {
			Film[] films = getPopularFilms(5);

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

			Film[] films = getPopularFilms(10);
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

	private User createUser(String userString) {
		NewUserRequest newUserRequest = parseNewUserRequest(userString);
		return createUser(newUserRequest);
	}

	private User createUser(NewUserRequest newUserRequest) {
		return post("/users", newUserRequest, User.class).getBody();
	}

	private User createUser(User user) {
		return createUser(getUserString(user));
	}

//	private ResponseEntity<User> createUserResp(String userString) {
//		return createUserResp(parseUser(userString));
//	}
//
//	private ResponseEntity<User> createUserResp(User user) {
//		return post("/users", user, User.class);
//	}

	private Film createFilm(String filmString) {
		NewFilmRequest newFilmRequest = parseNewFilmRequest(filmString);
		return createFilm(newFilmRequest);
	}

	private Film createFilm(NewFilmRequest newFilmRequest) {
		FilmDto filmDto = post("/films", newFilmRequest, FilmDto.class).getBody();
		return toFilm(filmDto);
	}

	private Film toFilm(FilmDto filmDto) {

		if (filmDto == null) {
			return null;
		}

		return Film.builder()
				.id(filmDto.getId())
				.name(filmDto.getName())
				.description((filmDto.getDescription()))
				.releaseDate(filmDto.getReleaseDate())
				.duration(filmDto.getDuration())
				.rate(filmDto.getRate())
				.mpa(filmDto.getMpa())
				.genres(filmDto.getGenres())
				.build();
	}

//	private Film createFilm(Film film) {
//		return createFilmResp(film).getBody();
//	}
//
//	private ResponseEntity<Film> createFilmResp(String filmString) {
//		return createFilmResp(parseFilm(filmString));
//	}

//	private ResponseEntity<FilmDto> createFilmResp(NewFilmRequest newFilmRequest) {
//		return post("/films", newFilmRequest, FilmDto.class);
//	}

	private List<FilmGenre> getAllGenres() {
		return Arrays.stream(Objects.requireNonNull(get("/genres", FilmGenre[].class).getBody())).toList();
	}

	private List<FilmMpa> getAllMpa() {
		return Arrays.stream(Objects.requireNonNull(get("/mpa", FilmMpa[].class).getBody())).toList();
	}

	private Film getFilmById(long id) {
		FilmDto filmDto = get("/films/" + id, FilmDto.class).getBody();
		return toFilm(filmDto);
	}

	private Integer deleteAllUsers() {
		return delete("/users", Integer.class).getBody();
	}

	private Integer deleteAllFilms() {
		return delete("/films", Integer.class).getBody();
	}

	private ResponseDto like(Film film, User user) {
		return put("/films/" + film.getId() + "/like/" + user.getId(), ResponseDto.class).getBody();
	}

	private ResponseDto dislike(Film film, User user) {
		return delete("/films/" + film.getId() + "/like/" + user.getId(), ResponseDto.class).getBody();
	}

	private Film[] getPopularFilms() {
		return get("/films/popular", Film[].class).getBody();
	}

	private Film[] getPopularFilms(int count) {
		return get("/films/popular?count=" + count, Film[].class).getBody();
	}

//	private User parseUser(String userString) {
//		String[] chunks = userString.split(";");
//		return new User(
//				chunks[0],
//				chunks[1].equals("NULL") ? null : chunks[1],
//				chunks[2].equals("NULL") ? null : chunks[2],
//				LocalDate.parse(chunks[3])
//		);
//	}

	private NewUserRequest parseNewUserRequest(String userString) {

		String[] chunks = userString.split(";");
		return NewUserRequest.builder()
				.email(chunks[0].equals("NULL") ? null : chunks[0])
				.login(chunks[1].equals("NULL") ? null : chunks[1])
				.name(chunks[2].equals("NULL") ? null : chunks[2])
				.birthday(chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]))
				.build();
	}

	private UpdateUserRequest parseUpdateUserRequest(String userString) {

		String[] chunks = userString.split(";");
		return UpdateUserRequest.builder()
				.email(chunks[0].equals("NULL") ? null : chunks[0])
				.login(chunks[1].equals("NULL") ? null : chunks[1])
				.name(chunks[2].equals("NULL") ? null : chunks[2])
				.birthday(chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]))
				.build();
	}

//	private ResponseEntity<FilmDto> sendNewFilmRequestResp(String requestString) {
//		NewFilmRequest newFilmRequest = parseNewFilmRequest(requestString);
//		return post("/films", newFilmRequest, FilmDto.class);
//	}
//
//	private FilmDto sendNewFilmRequest(String requestString) {
//		return sendNewFilmRequestResp(requestString).getBody();
//	}
//
//	private ResponseEntity<FilmDto> sendUpdateFilmRequestResp(String requestString) {
//		UpdateFilmRequest updateFilmRequest = parseUpdateFilmRequest(requestString);
//		return put("/films", updateFilmRequest, FilmDto.class);
//	}
//
//	private FilmDto sendUpdateFilmRequest(String requestString) {
//		return sendUpdateFilmRequestResp(requestString).getBody();
//	}

//	private Film parseFilm(String filmString) {
//		String[] chunks = filmString.split(";");
//
//		FilmMpa filmMpa = chunks[4].equals("NULL") ? null : mpaByName.get(chunks[4]);
//		List<FilmGenre> filmGenres = chunks[5].equals("NULL") ? null :
//				Arrays.stream(chunks[5].split(",")).map(g -> genreByName.get(g)).toList();
//
//		return Film.builder()
//				.name(chunks[0].equals("NULL") ? null : chunks[0])
//				.description(chunks[1].equals("NULL") ? null : chunks[1])
//				.releaseDate(chunks[2].equals("NULL") ? null : LocalDate.parse(chunks[2]))
//				.duration(Integer.parseInt(chunks[3]))
//				.mpa(filmMpa)
//				.genres(filmGenres)
//				.build();
//	}

	private NewFilmRequest parseNewFilmRequest(String requestString) {
		String[] chunks = requestString.split(";");

		String mpaName = chunks[4].equals("NULL") ? null : chunks[4];
		String genreNames = chunks[5].equals("NULL") ? null : chunks[5];

		return NewFilmRequest.builder()
				.name(chunks[0].equals("NULL") ? null : chunks[0])
				.description(chunks[1].equals("NULL") ? null : chunks[1])
				.releaseDate(chunks[2].equals("NULL") ? null : LocalDate.parse(chunks[2]))
				.duration(Integer.parseInt(chunks[3]))
				.mpa(mpaDto(mpaName))
				.genres(genreDto(genreNames))
				.build();
	}

	private UpdateFilmRequest parseUpdateFilmRequest(String requestString) {
		String[] chunks = requestString.split(";");

		String mpaName = chunks[4].equals("NULL") ? null : chunks[4];
		String genreNames = chunks[5].equals("NULL") ? null : chunks[5];

		return UpdateFilmRequest.builder()
				.name(chunks[0].equals("NULL") ? null : chunks[0])
				.description(chunks[1].equals("NULL") ? null : chunks[1])
				.releaseDate(chunks[2].equals("NULL") ? null : LocalDate.parse(chunks[2]))
				.duration(Integer.parseInt(chunks[3]))
				.mpa(mpaDto(mpaName))
				.genres(genreDto(genreNames))
				.build();
	}

	private FilmMpaDto mpaDto(String mpaName) {
		if (mpaName == null) {
			return null;
		}

		FilmMpa filmMpa = mpaByName.get(mpaName);
		return FilmMpaMapper.mapToDto(filmMpa);
	}

	private List<FilmGenreDto> genreDto(String genreNames) {

		if (genreNames == null) {
			return null;
		}

		return Arrays.stream(genreNames.split(","))
				.map(genreName -> genreByName.get(genreName))
				.map(FilmGenreMapper::mapToDto)
				.toList();
	}

	private String getGenresString(Film film) {
		return film.getGenres().stream().map(FilmGenre::getName).collect(Collectors.joining(","));
	}

	private Set<String> getGenreNames(Film film) {
		return film.getGenres().stream().map(FilmGenre::getName).collect(Collectors.toSet());
	}

	private String getFilmString(Film f) {
		return String.join(";", f.getName(), f.getDescription(), f.getReleaseDate().toString(),
				String.valueOf(f.getDuration()), f.getMpa().getName(), getGenresString(f));
	}

	private String getUserString(User u) {
		return String.join(";", u.getEmail(), u.getLogin(), u.getName(), u.getBirthday().toString());
	}

	private String getUserString(NewUserRequest u) {
		return String.join(";", u.getEmail(), u.getLogin(), u.getName(), u.getBirthday().toString());
	}

	private String getUserString(UpdateUserRequest u) {
		return String.join(";", u.getEmail(), u.getLogin(), u.getName(), u.getBirthday().toString());
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
